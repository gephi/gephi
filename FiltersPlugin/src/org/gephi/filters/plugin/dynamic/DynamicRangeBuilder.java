/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.filters.plugin.dynamic;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.api.AttributeUtils;
import org.gephi.data.attributes.type.TimeInterval;
import org.gephi.filters.api.Range;
import org.gephi.filters.spi.Category;
import org.gephi.filters.spi.CategoryBuilder;
import org.gephi.filters.spi.EdgeFilter;
import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.FilterBuilder;
import org.gephi.filters.spi.FilterProperty;
import org.gephi.filters.spi.NodeFilter;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.timeline.api.TimelineController;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = CategoryBuilder.class)
public class DynamicRangeBuilder implements CategoryBuilder {

    private final static Category DYNAMIC = new Category(
            NbBundle.getMessage(DynamicRangeBuilder.class, "DynamicRangeBuilder.category"),
            null,
            null);

    public Category getCategory() {
        return DYNAMIC;
    }

    public FilterBuilder[] getBuilders() {
        List<FilterBuilder> builders = new ArrayList<FilterBuilder>();
        AttributeModel am = Lookup.getDefault().lookup(AttributeController.class).getModel();
        AttributeColumn[] columns = am.getNodeTable().getColumns();
        for (AttributeColumn c : columns) {
            if (c.getType().equals(AttributeType.TIME_INTERVAL)) {
                builders.add(new DynamicRangeFilterBuilder(c));
            }
        }
        return builders.toArray(new FilterBuilder[0]);
    }

    private static class DynamicRangeFilterBuilder implements FilterBuilder {

        private final AttributeColumn column;

        public DynamicRangeFilterBuilder(AttributeColumn column) {
            this.column = column;
        }

        public Category getCategory() {
            return DYNAMIC;
        }

        public String getName() {
            return column.getTitle();
        }

        public Icon getIcon() {
            return null;
        }

        public String getDescription() {
            return null;
        }

        public DynamicRangeFilter getFilter() {
            return new DynamicRangeFilter(column);
        }

        public JPanel getPanel(Filter filter) {
            final DynamicRangeFilter dynamicRangeFilter = (DynamicRangeFilter) filter;
            JPanel panel = new JPanel();
            final TopComponent topComponent = WindowManager.getDefault().findTopComponent("TimelineTopComponent");
            final JButton button = new JButton(topComponent.isOpened() ? "Close Timeline" : "Open Timeline");
            button.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {

                    if (!topComponent.isOpened()) {
                        topComponent.open();
                        topComponent.requestActive();
                        button.setText("Close Timeline");
                    } else {
                        topComponent.close();
                        button.setText("Open Timeline");
                    }

                    //topComponent.close();
                    dynamicRangeFilter.refreshRange();
                    TimelineController timelineController = Lookup.getDefault().lookup(TimelineController.class);
                    timelineController.getModel().setFilterProperty(dynamicRangeFilter.getRangeProperty());
                    timelineController.setMin(dynamicRangeFilter.getMinimum());
                    timelineController.setMax(dynamicRangeFilter.getMaximum());
                }
            });
            panel.add(button);
            return panel;
        }
    }

    public static class DynamicRangeFilter implements NodeFilter, EdgeFilter {

        private AttributeColumn column;
        private Range range = new Range(0.0, 0.0);
        private FilterProperty[] filterProperties;
        private Double min;
        private Double max;

        public DynamicRangeFilter(AttributeColumn column) {
            this.column = column;
        }

        public boolean init(Graph graph) {
            return true;
        }

        public boolean evaluate(Graph graph, Node node) {
            Object obj = node.getNodeData().getAttributes().getValue(column.getIndex());
            if (obj != null) {
                TimeInterval timeInterval = (TimeInterval) obj;
                return timeInterval.isInRange(range.getLowerDouble(), range.getUpperDouble());
            }
            return true;
        }

        public boolean evaluate(Graph graph, Edge edge) {
            Object obj = edge.getEdgeData().getAttributes().getValue(column.getIndex());
            if (obj != null) {
                TimeInterval timeInterval = (TimeInterval) obj;
                return timeInterval.isInRange(range.getLowerDouble(), range.getUpperDouble());
            }
            return true;
        }

        public void finish() {
        }

        public String getName() {
            return NbBundle.getMessage(DynamicRangeBuilder.class, "DynamicRangeBuilder.name");
        }

        public FilterProperty[] getProperties() {
            if (filterProperties == null) {
                filterProperties = new FilterProperty[0];
                try {
                    filterProperties = new FilterProperty[]{
                                FilterProperty.createProperty(this, AttributeColumn.class, "column"),
                                FilterProperty.createProperty(this, Range.class, "range")};
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            return filterProperties;
        }

        public FilterProperty getRangeProperty() {
            return getProperties()[1];
        }

        public void refreshRange() {
            GraphModel gm = Lookup.getDefault().lookup(GraphController.class).getModel();
            Graph graph = gm.getGraph();
            min = Double.POSITIVE_INFINITY;
            max = Double.NEGATIVE_INFINITY;
            if (AttributeUtils.getDefault().isNodeColumn(column)) {
                for (Node n : graph.getNodes()) {
                    Object val = n.getNodeData().getAttributes().getValue(column.getIndex());
                    if (val != null) {
                        Double valMin = ((TimeInterval) val).getMin();
                        Double valMax = ((TimeInterval) val).getMax();
                        if (valMin != Double.NEGATIVE_INFINITY) {
                            min = Math.min(min, valMin);
                        }
                        if (valMax != Double.POSITIVE_INFINITY) {
                            max = Math.max(max, valMax);
                        }
                    }
                }
            } else {
                for (Edge e : graph.getEdges()) {
                    Object val = e.getEdgeData().getAttributes().getValue(column.getIndex());
                    if (val != null) {
                        Double valMin = ((TimeInterval) val).getMin();
                        Double valMax = ((TimeInterval) val).getMax();
                        if (valMin != Double.NEGATIVE_INFINITY) {
                            min = Math.min(min, valMin);
                        }
                        if (valMax != Double.POSITIVE_INFINITY) {
                            max = Math.max(max, valMax);
                        }
                    }
                }
            }
            range = new Range(min, max);
        }

        public Double getMinimum() {
            return min;
        }

        public Double getMaximum() {
            return max;
        }

        public AttributeColumn getColumn() {
            return column;
        }

        public void setColumn(AttributeColumn column) {
            this.column = column;
        }

        public Range getRange() {
            return range;
        }

        public void setRange(Range range) {
            this.range = range;
        }
    }
}
