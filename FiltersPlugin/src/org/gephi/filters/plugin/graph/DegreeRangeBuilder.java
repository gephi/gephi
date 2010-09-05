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
package org.gephi.filters.plugin.graph;

import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.filters.plugin.RangeFilter;
import org.gephi.filters.api.FilterLibrary;
import org.gephi.filters.api.Range;
import org.gephi.filters.spi.Category;
import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.FilterBuilder;
import org.gephi.filters.spi.FilterProperty;
import org.gephi.filters.spi.NodeFilter;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = FilterBuilder.class)
public class DegreeRangeBuilder implements FilterBuilder {

    public Category getCategory() {
        return FilterLibrary.TOPOLOGY;
    }

    public String getName() {
        return NbBundle.getMessage(DegreeRangeBuilder.class, "DegreeRangeBuilder.name");
    }

    public Icon getIcon() {
        return null;
    }

    public String getDescription() {
        return NbBundle.getMessage(DegreeRangeBuilder.class, "DegreeRangeBuilder.description");
    }

    public DegreeRangeFilter getFilter() {
        return new DegreeRangeFilter();
    }

    public JPanel getPanel(Filter filter) {
        RangeUI ui = Lookup.getDefault().lookup(RangeUI.class);
        if (ui != null) {
            return ui.getPanel((DegreeRangeFilter) filter);
        }
        return null;
    }

    public void destroy(Filter filter) {
    }

    public static class DegreeRangeFilter implements RangeFilter, NodeFilter {

        private Integer min = 0;
        private Integer max = 0;
        private Range range;
        //States
        private List<Integer> values;

        public String getName() {
            return NbBundle.getMessage(DegreeRangeBuilder.class, "DegreeRangeBuilder.name");
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
            values = new ArrayList<Integer>(graph.getNodeCount());
            min = Integer.MAX_VALUE;
            max = Integer.MIN_VALUE;
            return true;
        }

        public boolean evaluate(Graph graph, Node node) {
            int degree = graph.getDegree(node);
            min = Math.min(min, degree);
            max = Math.max(max, degree);
            values.add(new Integer(degree));
            return range.isInRange(degree);
        }

        public void finish() {
            refreshRange();
        }

        public Object[] getValues() {
            if (values == null) {
                GraphModel gm = Lookup.getDefault().lookup(GraphController.class).getModel();
                Graph graph = gm.getGraph();
                Integer[] degrees = new Integer[graph.getNodeCount()];
                int i = 0;
                min = Integer.MAX_VALUE;
                max = Integer.MIN_VALUE;
                for (Node n : graph.getNodes()) {
                    int degree = graph.getDegree(n);
                    min = Math.min(min, degree);
                    max = Math.max(max, degree);
                    degrees[i++] = degree;
                }
                refreshRange();
                return degrees;
            } else {
                return values.toArray(new Integer[0]);
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
