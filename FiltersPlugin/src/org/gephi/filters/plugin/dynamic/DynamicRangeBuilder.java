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
        AttributeColumn[] nodeColumns = am.getNodeTable().getColumns();
        AttributeColumn[] edgeColumns = am.getEdgeTable().getColumns();
        for (AttributeColumn c : nodeColumns) {
            if (c.getType().equals(AttributeType.TIME_INTERVAL)) {
                if (am.getEdgeTable().getColumn(c.getId(), c.getType()) != null) {
                    AttributeColumn edgeColumn = am.getEdgeTable().getColumn(c.getId(), c.getType());    //Edge column with same name
                    builders.add(new DynamicRangeFilterBuilder(c, edgeColumn));
                } else {
                    builders.add(new DynamicRangeFilterBuilder(c, null));
                }
            }
        }
        for (AttributeColumn c : edgeColumns) {
            if (c.getType().equals(AttributeType.TIME_INTERVAL)) {
                if (am.getNodeTable().getColumn(c.getId(), c.getType()) == null) { //Column not already found
                    builders.add(new DynamicRangeFilterBuilder(null, c));
                }
            }
        }
        return builders.toArray(new FilterBuilder[0]);
    }

    private static class DynamicRangeFilterBuilder implements FilterBuilder {

        private final AttributeColumn nodeColumn;
        private final AttributeColumn edgeColumn;

        public DynamicRangeFilterBuilder(AttributeColumn nodeColumn, AttributeColumn edgeColumn) {
            this.nodeColumn = nodeColumn;
            this.edgeColumn = edgeColumn;
        }

        public Category getCategory() {
            return DYNAMIC;
        }

        public String getName() {
            return nodeColumn.getTitle();
        }

        public Icon getIcon() {
            return null;
        }

        public String getDescription() {
            return null;
        }

        public DynamicRangeFilter getFilter() {
            return new DynamicRangeFilter(nodeColumn, edgeColumn);
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

        private AttributeColumn nodeColumn;
        private AttributeColumn edgeColumn;
        private Range range = new Range(0.0, 0.0);
        private FilterProperty[] filterProperties;
        private Double min;
        private Double max;

        public DynamicRangeFilter(AttributeColumn nodeColumn, AttributeColumn edgeColumn) {
            this.nodeColumn = nodeColumn;
            this.edgeColumn = edgeColumn;
        }

        public boolean init(Graph graph) {
            return true;
        }

        public boolean evaluate(Graph graph, Node node) {
            if (nodeColumn != null) {
                Object obj = node.getNodeData().getAttributes().getValue(nodeColumn.getIndex());
                if (obj != null) {
                    TimeInterval timeInterval = (TimeInterval) obj;
                    return timeInterval.isInRange(range.getLowerDouble(), range.getUpperDouble());
                }
            }
            return true;
        }

        public boolean evaluate(Graph graph, Edge edge) {
            if (edgeColumn != null) {
                Object obj = edge.getEdgeData().getAttributes().getValue(edgeColumn.getIndex());
                if (obj != null) {
                    TimeInterval timeInterval = (TimeInterval) obj;
                    return timeInterval.isInRange(range.getLowerDouble(), range.getUpperDouble());
                }
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
            if (nodeColumn != null) {

                for (Node n : graph.getNodes()) {
                    Object val = n.getNodeData().getAttributes().getValue(nodeColumn.getIndex());
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
            if (edgeColumn != null) {
                for (Edge e : graph.getEdges()) {
                    Object val = e.getEdgeData().getAttributes().getValue(edgeColumn.getIndex());
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
            return nodeColumn;
        }

        public void setColumn(AttributeColumn column) {
            this.nodeColumn = column;
        }

        public Range getRange() {
            return range;
        }

        public void setRange(Range range) {
            this.range = range;
        }
    }
}
