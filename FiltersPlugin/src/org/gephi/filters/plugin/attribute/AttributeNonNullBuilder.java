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
package org.gephi.filters.plugin.attribute;

import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeUtils;
import org.gephi.filters.api.FilterLibrary;
import org.gephi.filters.spi.Category;
import org.gephi.filters.spi.CategoryBuilder;
import org.gephi.filters.spi.EdgeFilter;
import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.FilterBuilder;
import org.gephi.filters.spi.FilterProperty;
import org.gephi.filters.spi.NodeFilter;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = CategoryBuilder.class)
public class AttributeNonNullBuilder implements CategoryBuilder {

    private final static Category NONNULL = new Category(
            NbBundle.getMessage(AttributeEqualBuilder.class, "AttributeNonNullBuilder.name"),
            null,
            FilterLibrary.ATTRIBUTES);

    public Category getCategory() {
        return NONNULL;
    }

    public FilterBuilder[] getBuilders() {
        List<FilterBuilder> builders = new ArrayList<FilterBuilder>();
        AttributeModel am = Lookup.getDefault().lookup(AttributeController.class).getModel();
        for (AttributeColumn c : am.getNodeTable().getColumns()) {
            AttributeNonNullFilterBuilder b = new AttributeNonNullFilterBuilder(c);
            builders.add(b);
        }
        for (AttributeColumn c : am.getEdgeTable().getColumns()) {
            AttributeNonNullFilterBuilder b = new AttributeNonNullFilterBuilder(c);
            builders.add(b);
        }
        return builders.toArray(new FilterBuilder[0]);
    }

    private static class AttributeNonNullFilterBuilder implements FilterBuilder {

        private final AttributeColumn column;

        public AttributeNonNullFilterBuilder(AttributeColumn column) {
            this.column = column;
        }

        public Category getCategory() {
            return NONNULL;
        }

        public String getName() {
            return "<font color='#000000'>" + column.getTitle() + "</font> "
                    + "<font color='#999999'><i>" + column.getType().toString() + " "
                    + (AttributeUtils.getDefault().isNodeColumn(column) ? "(Node)" : "(Edge)") + "</i></font>";
        }

        public Icon getIcon() {
            return null;
        }

        public String getDescription() {
            return NbBundle.getMessage(AttributeEqualBuilder.class, "AttributeNonNullBuilder.description");
        }

        public AttributeNonNullFilter getFilter() {
            if (AttributeUtils.getDefault().isNodeColumn(column)) {
                NodeAttributeNonNullFilter f = new NodeAttributeNonNullFilter();
                f.setColumn(column);
                return f;
            } else {
                EdgeAttributeNonNullFilter f = new EdgeAttributeNonNullFilter();
                f.setColumn(column);
                return f;
            }
        }

        public JPanel getPanel(Filter filter) {
            return null;
        }

        public void destroy(Filter filter) {
        }
    }

    public static class NodeAttributeNonNullFilter extends AttributeNonNullFilter implements NodeFilter {
    }

    public static class EdgeAttributeNonNullFilter extends AttributeNonNullFilter implements EdgeFilter {
    }

    public static class AttributeNonNullFilter implements Filter {

        private FilterProperty[] filterProperties;
        private AttributeColumn column;

        public String getName() {
            return NbBundle.getMessage(AttributeEqualBuilder.class, "AttributeNonNullBuilder.name");
        }

        public boolean init(Graph graph) {
            HierarchicalGraph hg = (HierarchicalGraph)graph;
            if (AttributeUtils.getDefault().isNodeColumn(column)) {
                if (graph.getNodeCount() == 0) {
                    return false;
                }
            } else if (AttributeUtils.getDefault().isEdgeColumn(column)) {
                if (hg.getTotalEdgeCount() == 0) {
                    return false;
                }
            }
            return true;
        }

        public boolean evaluate(Graph graph, Edge edge) {
            return edge.getEdgeData().getAttributes().getValue(column.getIndex()) != null;
        }

        public boolean evaluate(Graph graph, Node node) {
            return node.getNodeData().getAttributes().getValue(column.getIndex()) != null;
        }

        public void finish() {
        }

        public FilterProperty[] getProperties() {
            if (filterProperties == null) {
                filterProperties = new FilterProperty[0];
                try {
                    filterProperties = new FilterProperty[]{
                                FilterProperty.createProperty(this, AttributeColumn.class, "column"),};
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return filterProperties;
        }

        public AttributeColumn getColumn() {
            return column;
        }

        public void setColumn(AttributeColumn column) {
            this.column = column;
        }
    }
}
