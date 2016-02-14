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
import javax.swing.JPanel;
import org.gephi.filters.api.FilterLibrary;
import org.gephi.filters.api.Range;
import org.gephi.filters.plugin.AbstractAttributeFilter;
import org.gephi.filters.plugin.AbstractAttributeFilterBuilder;
import org.gephi.filters.plugin.graph.RangeUI;
import org.gephi.filters.spi.*;
import org.gephi.graph.api.*;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = CategoryBuilder.class)
public class AttributeRangeBuilder implements CategoryBuilder {

    private final static Category RANGE = new Category(
            NbBundle.getMessage(AttributeRangeBuilder.class, "AttributeRangeBuilder.name"),
            null,
            FilterLibrary.ATTRIBUTES);

    @Override
    public Category getCategory() {
        return RANGE;
    }

    @Override
    public FilterBuilder[] getBuilders(Workspace workspace) {
        List<FilterBuilder> builders = new ArrayList<>();
        GraphModel am = Lookup.getDefault().lookup(GraphController.class).getGraphModel(workspace);
        List<Column> columns = new ArrayList<>();
        columns.addAll(am.getNodeTable().toList());
        columns.addAll(am.getEdgeTable().toList());
        for (Column c : columns) {
            if (!c.isProperty() && !c.isArray()) {
                if (AttributeUtils.isNumberType(c.getTypeClass())) {
                    AttributeRangeFilterBuilder b = new AttributeRangeFilterBuilder(c);
                    builders.add(b);
                }
            }
        }
        return builders.toArray(new FilterBuilder[0]);
    }

    private static class AttributeRangeFilterBuilder extends AbstractAttributeFilterBuilder {

        public AttributeRangeFilterBuilder(Column column) {
            super(column,
                    RANGE,
                    NbBundle.getMessage(AttributeEqualBuilder.class, "AttributeRangeBuilder.description"),
                    null);
        }

        @Override
        public AttributeRangeFilter getFilter(Workspace workspace) {
            return AttributeUtils.isNodeColumn(column) ? new AttributeRangeFilter.Node(column) : new AttributeRangeFilter.Edge(column);
        }

        @Override
        public JPanel getPanel(Filter filter) {
            RangeUI ui = Lookup.getDefault().lookup(RangeUI.class);
            if (ui != null) {
                return ui.getPanel((AttributeRangeFilter) filter);
            }
            return null;
        }
    }

    public static abstract class AttributeRangeFilter<K extends Element> extends AbstractAttributeFilter<K> implements RangeFilter {

        private Range range;

        public AttributeRangeFilter(Column column) {
            super(NbBundle.getMessage(AttributeRangeBuilder.class, "AttributeRangeBuilder.name"),
                    column);

            //Add property
            addProperty(Range.class, "range");
        }

        @Override
        public boolean init(Graph graph) {
            if (AttributeUtils.isNodeColumn(column)) {
                if (graph.getNodeCount() == 0) {
                    return false;
                }
            } else if (AttributeUtils.isEdgeColumn(column)) {
                if (graph.getEdgeCount() == 0) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean evaluate(Graph graph, Element element) {
            Object val = element.getAttribute(column, graph.getView());
            if (val != null) {
                return range.isInRange((Number) val);
            }
            return false;

        }

        @Override
        public void finish() {
        }

        @Override
        public Number[] getValues(Graph graph) {
            List<Number> vals = new ArrayList<>();
            if (AttributeUtils.isNodeColumn(column)) {
                for (Element n : graph.getNodes()) {
                    Object val = n.getAttribute(column, graph.getView());
                    if (val != null) {
                        vals.add((Number) val);
                    }
                }
            } else {
                for (Element e : graph.getEdges()) {
                    Object val = e.getAttribute(column, graph.getView());
                    if (val != null) {
                        vals.add((Number) val);
                    }
                }
            }
            return vals.toArray(new Number[0]);
        }

        @Override
        public FilterProperty getRangeProperty() {
            return getProperties()[1];
        }

        public Range getRange() {
            return range;
        }

        public void setRange(Range range) {
            this.range = range;
        }

        public static class Node extends AttributeRangeFilter<org.gephi.graph.api.Node> implements NodeFilter {

            public Node(Column column) {
                super(column);
            }
        }

        public static class Edge extends AttributeRangeFilter<org.gephi.graph.api.Edge> implements EdgeFilter {

            public Edge(Column column) {
                super(column);
            }
        }
    }
}
