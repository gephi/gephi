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
        private Range range;
        //States
        private List<Float> values;

        public String getName() {
            return NbBundle.getMessage(EdgeWeightBuilder.class, "EdgeWeightBuilder.name");
        }

        private void refreshRange() {
            if (range == null) {
                range = new Range(min, max);
            } else {
                range.trimBounds(min, max);
            }
        }

        public boolean init(Graph graph) {
            if (range == null) {
                getValues();
                refreshRange();
            }
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
