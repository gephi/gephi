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
package org.gephi.graph.dhns.filter;

import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodePredicate;
import org.gephi.graph.api.TopologicalPredicate;

/**
 *
 * @author Mathieu Bastian
 */
public class DegreePredicate implements TopologicalPredicate, NodePredicate {

    private int degreeRangeStart;
    private int degreeRangeEnd;
    private Graph graph;

    public DegreePredicate(int start, int end) {
        this.degreeRangeStart = start;
        this.degreeRangeEnd = end;
    }

    public void setup(Graph graph) {
        this.graph = graph;
    }

    public void unsetup() {
        graph = null;
    }

    public boolean evaluate(Node element) {
        int degree = graph.getDegree(element);
        System.out.println(element.getNodeData().getLabel() + " : " + degree);
        return degree >= degreeRangeStart && degree <= degreeRangeEnd;
    }
}
