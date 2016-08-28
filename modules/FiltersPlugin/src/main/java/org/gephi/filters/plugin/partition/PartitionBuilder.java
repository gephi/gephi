/*
 Copyright 2008-2010 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
 Website : http://www.gephi.org

 This file is part of Gephi.

 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

 Copyright 2011 Gephi Consortium. All rights reserved.

 The contents of this file are subject to the terms of either the GNU
 General Public License Version 3 only ("GPL") or the Common
 Development and Distribution License("CDDL") (collectively, the
 "License"). You may not use this file except in compliance with the
 License. You can obtain a copy of the License at
 http://gephi.org/about/legal/license-notice/
 or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
 specific language governing permissions and limitations under the
 License.  When distributing the software, include this License Header
 Notice in each file and include the License files at
 /cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
 License Header, with the fields enclosed by brackets [] replaced by
 your own identifying information:
 "Portions Copyrighted [year] [name of copyright owner]"

 If you wish your version of this file to be governed by only the CDDL
 or only the GPL Version 3, indicate your decision by adding
 "[Contributor] elects to include this software in this distribution
 under the [CDDL or GPL Version 3] license." If you do not indicate a
 single choice of license, a recipient has the option to distribute
 your version of this file under either the CDDL, the GPL Version 3 or
 to extend the choice of license to its licensees as provided above.
 However, if you add GPL Version 3 code and therefore, elected the GPL
 Version 3 license, then the option applies only if the new code is
 made subject to such option by the copyright holder.

 Contributor(s):

 Portions Copyrighted 2011 Gephi Consortium.
 */
package org.gephi.filters.plugin.partition;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.appearance.api.AppearanceController;
import org.gephi.appearance.api.AppearanceModel;
import org.gephi.appearance.api.Partition;
import org.gephi.filters.api.FilterLibrary;
import org.gephi.filters.spi.Category;
import org.gephi.filters.spi.CategoryBuilder;
import org.gephi.filters.spi.EdgeFilter;
import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.FilterBuilder;
import org.gephi.filters.spi.FilterProperty;
import org.gephi.filters.spi.NodeFilter;
import org.gephi.graph.api.AttributeUtils;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.project.api.Workspace;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = CategoryBuilder.class)
public class PartitionBuilder implements CategoryBuilder {

    private final static Category PARTITION = new Category(
            NbBundle.getMessage(PartitionBuilder.class, "PartitionBuilder.name"),
            null,
            FilterLibrary.ATTRIBUTES);

    @Override
    public Category getCategory() {
        return PARTITION;
    }

    @Override
    public FilterBuilder[] getBuilders(Workspace workspace) {
        List<FilterBuilder> builders = new ArrayList<>();
        GraphModel gm = Lookup.getDefault().lookup(GraphController.class).getGraphModel(workspace);
        Graph graph = gm.getGraph();
        AppearanceModel am = Lookup.getDefault().lookup(AppearanceController.class).getModel(workspace);

        //Force refresh
        am.getNodeFunctions(graph);

        for (Column nodeCol : gm.getNodeTable()) {
            if (!nodeCol.isProperty()) {
                if (am.getNodePartition(graph, nodeCol) != null) {
                    PartitionFilterBuilder builder = new PartitionFilterBuilder(nodeCol, am);
                    builders.add(builder);
                }
            }
        }

        for (Column edgeCol : gm.getEdgeTable()) {
            if (!edgeCol.isProperty()) {
                if (am.getEdgePartition(graph, edgeCol) != null) {
                    PartitionFilterBuilder builder = new PartitionFilterBuilder(edgeCol, am);
                    builders.add(builder);
                }
            }
        }

        return builders.toArray(new FilterBuilder[0]);
    }

    private static class PartitionFilterBuilder implements FilterBuilder {

        private final Column column;
        private final AppearanceModel model;

        public PartitionFilterBuilder(Column column, AppearanceModel model) {
            this.column = column;
            this.model = model;
        }

        @Override
        public Category getCategory() {
            return PARTITION;
        }

        @Override
        public String getName() {
            return column.getTitle() + " (" + (AttributeUtils.isNodeColumn(column)
                    ? NbBundle.getMessage(PartitionFilterBuilder.class, "PartitionFilterBuilder.name.node")
                    : NbBundle.getMessage(PartitionFilterBuilder.class, "PartitionFilterBuilder.name.edge")) + ")";
        }

        @Override
        public Icon getIcon() {
            return null;
        }

        @Override
        public String getDescription() {
            return NbBundle.getMessage(PartitionBuilder.class, "PartitionBuilder.description");
        }

        @Override
        public PartitionFilter getFilter(Workspace workspace) {
            if (AttributeUtils.isNodeColumn(column)) {
                return new NodePartitionFilter(column, model);
            } else {
                return new EdgePartitionFilter(column, model);
            }
        }

        @Override
        public JPanel getPanel(Filter filter) {
            PartitionUI ui = Lookup.getDefault().lookup(PartitionUI.class);
            if (ui != null) {
                return ui.getPanel((PartitionFilter) filter);
            }
            return null;
        }

        @Override
        public void destroy(Filter filter) {
        }
    }

    public static class NodePartitionFilter extends PartitionFilter implements NodeFilter {

        public NodePartitionFilter(Column column, AppearanceModel model) {
            super(column, model);
        }

        @Override
        public boolean init(Graph graph) {
            partition = appearanceModel.getNodePartition(graph.getModel().getGraph(), column);
            return partition != null;
        }
    }

    public static class EdgePartitionFilter extends PartitionFilter implements EdgeFilter {

        public EdgePartitionFilter(Column column, AppearanceModel model) {
            super(column, model);
        }

        @Override
        public boolean init(Graph graph) {
            partition = appearanceModel.getEdgePartition(graph.getModel().getGraph(), column);
            return partition != null;
        }
    }

    public static abstract class PartitionFilter implements Filter {

        protected static final Object NULL = new Object();
        protected AppearanceModel appearanceModel;
        protected Column column;
        protected Partition partition;
        protected FilterProperty[] filterProperties;
        protected Set<Object> parts;

        public PartitionFilter(Column column, AppearanceModel model) {
            this.column = column;
            this.appearanceModel = model;
            parts = new HashSet<>();
        }

        @Override
        public String getName() {
            return NbBundle.getMessage(PartitionBuilder.class, "PartitionBuilder.name") + " (" + column.getTitle() + ")";
        }

        public boolean evaluate(Graph graph, Node node) {
            Object value = partition.getValue(node, graph);
            if (value == null) {
                return parts.contains(NULL);
            } else {
                return parts.contains(value);
            }
        }

        public boolean evaluate(Graph graph, Edge edge) {
            Object value = partition.getValue(edge, graph);
            if (value == null) {
                return parts.contains(NULL);
            } else {
                return parts.contains(value);
            }
        }

        public void finish() {
        }

        public void addPart(Object value) {
            if (value == null) {
                if (parts.add(NULL)) {
                    getProperties()[1].setValue(parts);
                }
            } else if (parts.add(value)) {
                getProperties()[1].setValue(parts);
            }
        }

        public void removePart(Object value) {
            if (value == null) {
                if (parts.remove(NULL)) {
                    getProperties()[1].setValue(parts);
                }
            } else if (parts.remove(value)) {
                getProperties()[1].setValue(parts);
            }
        }

        public void unselectAll() {
            getProperties()[1].setValue(new HashSet<>());
        }

        public void selectAll() {
            getProperties()[1].setValue(new HashSet<>(partition.getValues()));
        }

        @Override
        public FilterProperty[] getProperties() {
            if (filterProperties == null) {
                filterProperties = new FilterProperty[0];
                try {
                    filterProperties = new FilterProperty[]{
                        FilterProperty.createProperty(this, Column.class, "column"),
                        FilterProperty.createProperty(this, Set.class, "parts")};
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return filterProperties;
        }

        public Partition getPartition() {
            return partition;
        }

        public Set<Object> getParts() {
            return parts;
        }

        public Column getColumn() {
            return column;
        }

        public void setColumn(Column column) {
        }

        public void setParts(Set<Object> parts) {
            this.parts = parts;
        }
    }
}
