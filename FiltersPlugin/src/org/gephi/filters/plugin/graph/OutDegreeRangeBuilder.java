/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
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
import org.gephi.graph.api.DirectedGraph;
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
public class OutDegreeRangeBuilder implements FilterBuilder {

    public Category getCategory() {
        return FilterLibrary.TOPOLOGY;
    }

    public String getName() {
        return NbBundle.getMessage(OutDegreeRangeBuilder.class, "OutDegreeRangeBuilder.name");
    }

    public Icon getIcon() {
        return null;
    }

    public String getDescription() {
        return null;
    }

    public Filter getFilter() {
        return new OutDegreeRangeFilter();
    }

    public JPanel getPanel(Filter filter) {
        RangeUI ui = Lookup.getDefault().lookup(RangeUI.class);
        if (ui != null) {
            return ui.getPanel((OutDegreeRangeFilter) filter);
        }
        return null;
    }

    public static class OutDegreeRangeFilter implements RangeFilter {

        private Integer min = 0;
        private Integer max = 0;
        private Range range = new Range(0, 0);

        public String getName() {
            return NbBundle.getMessage(OutDegreeRangeBuilder.class, "OutDegreeRangeBuilder.name");
        }

        public OutDegreeRangeFilter() {
            refreshMinMax();
        }

        private void refreshMinMax() {
            GraphModel gm = Lookup.getDefault().lookup(GraphController.class).getModel();
            DirectedGraph graph = gm.getDirectedGraphVisible();
            min = Integer.MAX_VALUE;
            max = Integer.MIN_VALUE;
            for (Node n : graph.getNodes()) {
                int degree = graph.getOutDegree(n);
                min = Math.min(min, degree);
                max = Math.max(max, degree);
            }
            Integer lowerBound = range.getLowerInteger();
            Integer upperBound = range.getUpperInteger();
            if ((Integer) min > lowerBound || (Integer) max < lowerBound || lowerBound.equals(upperBound)) {
                lowerBound = (Integer) min;
            }
            if ((Integer) min > upperBound || (Integer) max < upperBound || lowerBound.equals(upperBound)) {
                upperBound = (Integer) max;
            }
            range = new Range(lowerBound, upperBound);
        }

        public boolean evaluate(Graph graph, Node node) {
            int degree = ((DirectedGraph)graph).getOutDegree(node);
            return range.isInRange(degree);
        }

        public Object[] getValues() {
            List<Integer> degrees = new ArrayList<Integer>();
            GraphModel gm = Lookup.getDefault().lookup(GraphController.class).getModel();
            DirectedGraph graph = gm.getDirectedGraphVisible();
            for (Node n : graph.getNodes()) {
                degrees.add(graph.getInDegree(n));
            }
            return degrees.toArray();
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
