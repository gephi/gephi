/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.filters.plugin.edge;

import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.filters.api.FilterLibrary;
import org.gephi.filters.api.Range;
import org.gephi.filters.plugin.RangeFilter;
import org.gephi.filters.plugin.graph.DegreeRangeBuilder;
import org.gephi.filters.plugin.graph.RangeUI;
import org.gephi.filters.spi.Category;
import org.gephi.filters.spi.EdgeFilter;
import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.FilterBuilder;
import org.gephi.filters.spi.FilterProperty;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = FilterBuilder.class)
public class EdgeWeightBuilder implements FilterBuilder {

    public Category getCategory() {
        return FilterLibrary.EDGE;
    }

    public String getName() {
        return NbBundle.getMessage(EdgeWeightBuilder.class, "EdgeWeightBuilder.name");
    }

    public Icon getIcon() {
        return null;
    }

    public String getDescription() {
        return null;
    }

    public Filter getFilter() {
        return new EdgeWeightFilter();
    }

    public JPanel getPanel(Filter filter) {
        RangeUI ui = Lookup.getDefault().lookup(RangeUI.class);
        if (ui != null) {
            return ui.getPanel((EdgeWeightFilter) filter);
        }
        return null;
    }

    public static class EdgeWeightFilter implements RangeFilter, EdgeFilter {

        private Float min = 0f;
        private Float max = 0f;
        private Range range = new Range(0f, 0f);
        //States
        private List<Float> values;

        public String getName() {
            return NbBundle.getMessage(EdgeWeightBuilder.class, "EdgeWeightBuilder.name");
        }

        private void refreshRange() {
            Float lowerBound = range.getLowerFloat();
            Float upperBound = range.getUpperFloat();
            if ((Float) min > lowerBound || (Float) max < lowerBound || lowerBound.equals(upperBound)) {
                lowerBound = (Float) min;
            }
            if ((Float) min > upperBound || (Float) max < upperBound || lowerBound.equals(upperBound)) {
                upperBound = (Float) max;
            }
            range = new Range(lowerBound, upperBound);
        }

        public boolean init(Graph graph) {
            values = new ArrayList<Float>(graph.getEdgeCount());
            min = Float.POSITIVE_INFINITY;
            max = Float.NEGATIVE_INFINITY;
            return true;
        }

        public boolean evaluate(Graph graph, Edge edge) {
            float weight = edge.getWeight();
            min = Math.min(min, weight);
            max = Math.max(max, weight);
            values.add(new Float(weight));
            return range.isInRange(weight);
        }

        public void finish() {
            refreshRange();
        }

        public Object[] getValues() {
            if (values == null) {
                GraphModel gm = Lookup.getDefault().lookup(GraphController.class).getModel();
                Graph graph = gm.getGraph();
                Float[] weights = new Float[graph.getEdgeCount()];
                int i = 0;
                min = Float.MAX_VALUE;
                max = Float.MIN_VALUE;
                for (Edge e : graph.getEdges()) {
                    float weight = e.getWeight();
                    min = Math.min(min, weight);
                    max = Math.max(max, weight);
                    weights[i++] = weight;
                }
                refreshRange();
                return weights;
            } else {
                return values.toArray(new Float[0]);
            }
        }

        public FilterProperty[] getProperties() {
            try {
                return new FilterProperty[]{
                            FilterProperty.createProperty(this, Range.class, "range")
                        };
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
            return new FilterProperty[0];
        }

        public FilterProperty getRangeProperty() {
            return getProperties()[0];
        }

        public Object getMinimum() {
            return min;
        }

        public Object getMaximum() {
            return max;
        }

        public Range getRange() {
            return range;
        }

        public void setRange(Range range) {
            this.range = range;
        }
    }
}
