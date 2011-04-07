/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
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
import org.gephi.filters.plugin.RangeFilter;
import org.gephi.filters.api.FilterLibrary;
import org.gephi.filters.api.Range;
import org.gephi.filters.plugin.DynamicAttributesHelper;
import org.gephi.filters.spi.Category;
import org.gephi.filters.spi.CategoryBuilder;
import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.FilterBuilder;
import org.gephi.filters.spi.FilterProperty;
import org.gephi.filters.plugin.graph.RangeUI;
import org.gephi.filters.spi.EdgeFilter;
import org.gephi.filters.spi.NodeFilter;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
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
public class AttributeRangeBuilder implements CategoryBuilder {

    private final static Category RANGE = new Category(
            NbBundle.getMessage(AttributeRangeBuilder.class, "AttributeRangeBuilder.name"),
            null,
            FilterLibrary.ATTRIBUTES);

    public Category getCategory() {
        return RANGE;
    }

    public FilterBuilder[] getBuilders() {
        List<FilterBuilder> builders = new ArrayList<FilterBuilder>();
        AttributeModel am = Lookup.getDefault().lookup(AttributeController.class).getModel();
        for (AttributeColumn c : am.getNodeTable().getColumns()) {
            if (AttributeUtils.getDefault().isNumberColumn(c) || AttributeUtils.getDefault().isDynamicNumberColumn(c)) {
                AttributeRangeFilterBuilder b = new AttributeRangeFilterBuilder(c);
                builders.add(b);
            }
        }
        for (AttributeColumn c : am.getEdgeTable().getColumns()) {
            if (AttributeUtils.getDefault().isNumberColumn(c) || AttributeUtils.getDefault().isDynamicNumberColumn(c)) {
                AttributeRangeFilterBuilder b = new AttributeRangeFilterBuilder(c);
                builders.add(b);
            }
        }
        return builders.toArray(new FilterBuilder[0]);
    }

    private static class AttributeRangeFilterBuilder implements FilterBuilder {

        private final AttributeColumn column;

        public AttributeRangeFilterBuilder(AttributeColumn column) {
            this.column = column;
        }

        public Category getCategory() {
            return RANGE;
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
            return NbBundle.getMessage(AttributeEqualBuilder.class, "AttributeRangeBuilder.description");
        }

        public AttributeRangeFilter getFilter() {
            if (AttributeUtils.getDefault().isNodeColumn(column)) {
                return new NodeAttributeRangeFilter(column);
            } else {
                return new EdgeAttributeRangeFilter(column);
            }
        }

        public JPanel getPanel(Filter filter) {
            RangeUI ui = Lookup.getDefault().lookup(RangeUI.class);
            if (ui != null) {
                return ui.getPanel((AttributeRangeFilter) filter);
            }
            return null;
        }

        public void destroy(Filter filter) {
        }
    }

    public static class NodeAttributeRangeFilter extends AttributeRangeFilter implements NodeFilter {

        public NodeAttributeRangeFilter(AttributeColumn column) {
            super(column);
        }
    }

    public static class EdgeAttributeRangeFilter extends AttributeRangeFilter implements EdgeFilter {

        public EdgeAttributeRangeFilter(AttributeColumn column) {
            super(column);
        }
    }

    public static class AttributeRangeFilter implements RangeFilter, Filter {

        private FilterProperty[] filterProperties;
        private Range range;
        private AttributeColumn column;
        private DynamicAttributesHelper dynamicHelper = new DynamicAttributesHelper(this, null);
        //States
        private Comparable[] values;

        public AttributeRangeFilter(AttributeColumn column) {
            this.column = column;
        }

        public String getName() {
            return column.getTitle() + " " + NbBundle.getMessage(AttributeRangeBuilder.class, "AttributeRangeBuilder.name");
        }

        public boolean init(Graph graph) {
            HierarchicalGraph hg = (HierarchicalGraph) graph;
            if (AttributeUtils.getDefault().isNodeColumn(column)) {
                if (graph.getNodeCount() == 0) {
                    return false;
                }
            } else if (AttributeUtils.getDefault().isEdgeColumn(column)) {
                if (hg.getTotalEdgeCount() == 0) {
                    return false;
                }
            }
            dynamicHelper = new DynamicAttributesHelper(this, hg);
            refreshValues(hg);
            return true;
        }

        public boolean evaluate(Graph graph, Node node) {
            Object val = node.getNodeData().getAttributes().getValue(column.getIndex());
            val = dynamicHelper.getDynamicValue(val);
            if (val != null) {
                return range.isInRange((Number) val);
            }
            return false;

        }

        public boolean evaluate(Graph graph, Edge edge) {
            Object val = edge.getEdgeData().getAttributes().getValue(column.getIndex());
            val = dynamicHelper.getDynamicValue(val);
            if (val != null) {
                return range.isInRange((Number) val);
            }
            return false;
        }

        public void finish() {
        }

        public void refreshValues(HierarchicalGraph graph) {
            List<Object> vals = new ArrayList<Object>();
            if (AttributeUtils.getDefault().isNodeColumn(column)) {
                for (Node n : graph.getNodes()) {
                    Object val = n.getNodeData().getAttributes().getValue(column.getIndex());
                    val = dynamicHelper.getDynamicValue(val);
                    if (val != null) {
                        vals.add(val);
                    }
                }
            } else {
                for (Edge e : graph.getEdgesAndMetaEdges()) {
                    Object val = e.getEdgeData().getAttributes().getValue(column.getIndex());
                    val = dynamicHelper.getDynamicValue(val);
                    if (val != null) {
                        vals.add(val);
                    }
                }
            }

            if (vals.isEmpty()) {
                vals.add(0);
            }

            values = ComparableArrayConverter.convert(vals);

            Number min = (Number) AttributeUtils.getDefault().getMin(column, values /*valuesArray*/);
            Number max = (Number) AttributeUtils.getDefault().getMax(column, values /*valuesArray*/);
            if (range == null) {
                range = new Range(min, max, min, max);
            } else {
                range.setMinMax(min, max);
            }
        }

        public Object[] getValues() {
            return values;
        }

        public FilterProperty[] getProperties() {
            if (filterProperties == null) {
                filterProperties = new FilterProperty[0];
                try {
                    filterProperties = new FilterProperty[]{
                                FilterProperty.createProperty(this, AttributeColumn.class, "column"),
                                FilterProperty.createProperty(this, Range.class, "range"),};
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return filterProperties;
        }

        public FilterProperty getRangeProperty() {
            return getProperties()[1];
        }

        public Range getRange() {
            return range;
        }

        public void setRange(Range range) {
            if (range.getMinimum() == null && range.getMaximum() == null) {
                //Opening project
                this.range = new Range(range.getLowerBound(), range.getUpperBound(), this.range.getMinimum(), this.range.getMaximum());
            } else {
                this.range = range;
            }
        }

        public AttributeColumn getColumn() {
            return column;
        }

        public void setColumn(AttributeColumn column) {
            this.column = column;
        }
    }
}
