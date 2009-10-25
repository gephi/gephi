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
package org.gephi.filters;

import org.gephi.filters.api.Filter;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodePredicate;
import org.gephi.graph.api.Predicate;
import org.gephi.graph.api.TopologicalPredicate;

/**
 *
 * @author Mathieu Bastian
 */
public class DegreeRangeFilter implements Filter {

    private int minimum;
    private int maximum;
    private int lowerBound;
    private int upperBound;

    public Predicate getPredicate() {
        return new DegreeRangePredicate();
    }

    public int getLowerBound() {
        return lowerBound;
    }

    public int getMaximum() {
        return maximum;
    }

    public int getMinimum() {
        return minimum;
    }

    public int getUpperBound() {
        return upperBound;
    }

    private class DegreeRangePredicate implements NodePredicate, TopologicalPredicate
    {
        private Graph graph;

        public boolean evaluate(Node element) {
            int degree = graph.getDegree(element);
            return degree >= lowerBound && degree <=upperBound;
        }

        public void setup(Graph graph) {
            this.graph = graph;
            minimum = Integer.MAX_VALUE;
            maximum = Integer.MIN_VALUE;
            for(Node n : graph.getNodes().toArray()) {
                int degree = graph.getDegree(n);
                minimum = Math.min(minimum, degree);
                maximum = Math.max(maximum, degree);
            }
        }

        public void unsetup() {
            graph = null;
        }
    }
}
