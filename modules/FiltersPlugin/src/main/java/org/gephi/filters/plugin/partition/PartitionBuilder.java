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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
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
        AppearanceModel am = Lookup.getDefault().lookup(AppearanceController.class).getModel(workspace);

        //Force refresh
        am.getNodeFunctions();
        am.getEdgeFunctions();

        for (Column nodeCol : gm.getNodeTable()) {
            if (!nodeCol.isProperty()) {
                Partition partition = am.getNodePartition(nodeCol);
                if (partition != null) {
                    PartitionFilterBuilder builder = new PartitionFilterBuilder(partition);
                    builders.add(builder);
                }
            }
        }

        for (Column edgeCol : gm.getEdgeTable()) {
            if (!edgeCol.isProperty()) {
                Partition partition = am.getEdgePartition(edgeCol);
                if (partition != null) {
                    PartitionFilterBuilder builder = new PartitionFilterBuilder(partition);
                    builders.add(builder);
                }
            }
        }

        return builders.toArray(new FilterBuilder[0]);
    }

    private static class PartitionFilterBuilder implements FilterBuilder {

        private final Partition partition;

        public PartitionFilterBuilder(Partition partition) {
            this.partition = partition;
        }

        @Override
        public Category getCategory() {
            return PARTITION;
        }

        @Override
        public String getName() {
            return partition.getColumn().getTitle() + " (" + (AttributeUtils.isNodeColumn(partition.getColumn())
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
            AppearanceModel am = Lookup.getDefault().lookup(AppearanceController.class).getModel(workspace);
            if (AttributeUtils.isNodeColumn(partition.getColumn())) {
                return new NodePartitionFilter(am, partition);
            } else {
                return new EdgePartitionFilter(am, partition);
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

        public NodePartitionFilter(AppearanceModel appearanceModel, Partition partition) {
            super(appearanceModel, partition);
        }

        @Override
        public boolean init(Graph graph) {
            this.graph = graph.getModel().getGraph();
            return partition != null && partition.getColumn() != null;
        }

        @Override
        public void setColumn(Column column) {
            // Bugfix #2519
            // Persistence provider doesn't grab the correct builder when using category builder
            // This method assigns the proper partition based on the column parameter
            if(partition == null || partition.getColumn() != column) {
                appearanceModel.getNodeFunctions();
                this.partition = appearanceModel.getNodePartition(column);
            }
        }
    }

    public static class EdgePartitionFilter extends PartitionFilter implements EdgeFilter {

        public EdgePartitionFilter(AppearanceModel appearanceModel, Partition partition) {
            super(appearanceModel, partition);
        }

        @Override
        public boolean init(Graph graph) {
            this.graph = graph.getModel().getGraph();;
            return partition != null && partition.getColumn() != null;
        }

        @Override
        public void setColumn(Column column) {
            // Bugfix #2519
            // Persistence provider doesn't grab the correct builder when using category builder
            // This method assigns the proper partition based on the column parameter
            if(partition == null || partition.getColumn() != column) {
                appearanceModel.getEdgeFunctions();
                this.partition = appearanceModel.getEdgePartition(column);
            }
        }
    }

    public static abstract class PartitionFilter implements Filter {

        protected static final Object NULL = new Object();
        protected Partition partition;
        protected final AppearanceModel appearanceModel;
        protected FilterProperty[] filterProperties;
        protected Set<Object> parts;
        protected Graph graph;
        protected boolean flattenList;

        public PartitionFilter(Partition partition) {
            this(null, partition);
        }

        public PartitionFilter(AppearanceModel appearanceModel, Partition partition) {
            this.partition = partition;
            this.appearanceModel = appearanceModel;
            parts = new HashSet<>();
        }

        @Override
        public String getName() {
            return NbBundle.getMessage(PartitionBuilder.class, "PartitionBuilder.name") + " (" + partition.getColumn().getTitle() +
                ")";
        }

        public boolean evaluate(Graph graph, Node node) {
            Object value = partition.getValue(node, graph);
            if (value == null) {
                return parts.contains(NULL);
            } else if (flattenList && partition.getColumn().isArray()) {
                return listContains(value);
            } else {
                return parts.contains(value);
            }
        }

        private boolean listContains(Object value) {
            int length = Array.getLength(value);
            for (int i = 0; i < length; i++) {
                Object val = Array.get(value, i);
                if (parts.contains(val)) {
                    return true;
                }
            }
            return false;
        }

        public boolean evaluate(Graph graph, Edge edge) {
            Object value = partition.getValue(edge, graph);
            if (value == null) {
                return parts.contains(NULL);
            } else if (flattenList && partition.getColumn().isArray()) {
                return listContains(value);
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

        public Set getFlattenParts() {
            HashSet allParts = new HashSet();
            partition.getValues(graph).stream().filter(Objects::nonNull).forEach(value -> {
                int length = Array.getLength(value);
                for (int i = 0; i < length; i++) {
                    allParts.add(Array.get(value, i));
                }
            });
            return allParts;
        }

        public void selectAll() {
            if (flattenList && partition.getColumn().isArray()) {
                getProperties()[1].setValue(getFlattenParts());
            } else {
                getProperties()[1].setValue(new HashSet<>(partition.getValues(graph)));
            }
        }

        @Override
        public FilterProperty[] getProperties() {
            if (filterProperties == null) {
                filterProperties = new FilterProperty[0];
                try {
                    filterProperties = new FilterProperty[] {
                        FilterProperty.createProperty(this, Column.class, "column"),
                        FilterProperty.createProperty(this, Set.class, "parts"),
                        FilterProperty.createProperty(this, Boolean.class, "flattenList"),};
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return filterProperties;
        }

        public boolean isFlattenList() {
            return flattenList;
        }

        public void setFlattenList(boolean flattenList) {
            this.flattenList = flattenList;
        }

        public Partition getPartition() {
            return partition;
        }

        public Set<Object> getParts() {
            return parts;
        }

        public Graph getGraph() {
            return graph;
        }

        public void setParts(Set<Object> parts) {
            this.parts = parts;
        }

        public Column getColumn() {
            return partition.getColumn();
        }

        public void setColumn(Column column) {
        }
    }
}
