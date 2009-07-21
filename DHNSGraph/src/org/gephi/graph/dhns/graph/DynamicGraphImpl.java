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
package org.gephi.graph.dhns.graph;

import org.gephi.graph.api.DynamicGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgePredicate;
import org.gephi.graph.api.FilteredGraph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodePredicate;

/**
 *
 * @author Mathieu Bastian
 */
public class DynamicGraphImpl implements DynamicGraph {

    //Graph reference
    private FilteredGraph graph;

    //Range
    private int from;
    private int to;

    public DynamicGraphImpl(FilteredGraph graph) {
        this.graph = graph;
        graph.addNodePredicate(new DynamicNodePredicate());
        graph.addEdgePredicate(new DynamicEdgePredicate());
    }

    public void setRange(int from, int to) {
        this.from = from;
        this.to = to;
    }

    public int getRangeFrom() {
        return from;
    }

    public int getRangeTo() {
        return to;
    }

    private class DynamicNodePredicate implements NodePredicate {

        public boolean evaluate(Node element) {
            //Check if element is in the range
            return true;
        }
    }

    private class DynamicEdgePredicate implements EdgePredicate {

        public boolean evaluate(Edge element) {
            //Check if element is in the range
            return true;
        }
    }
}
