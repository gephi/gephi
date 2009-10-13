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
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Predicate;
import org.gephi.graph.dhns.core.Dhns;
import org.gephi.graph.dhns.filter.DynamicEdgePredicate;
import org.gephi.graph.dhns.filter.DynamicNodePredicate;
import org.gephi.graph.dhns.filter.FilterControl;

/**
 *
 * @author Mathieu Bastian
 */
public class DynamicGraphImpl<T extends Graph> implements DynamicGraph {

    //Graph reference
    private T graph;
    private Dhns dhns;

    //Predicates
    private DynamicNodePredicate nodePredicate;
    private DynamicEdgePredicate edgePredicate;
    private float from = 0;
    private float to = 1;

    public DynamicGraphImpl(Dhns dhns, T graph) {
        this.graph = graph;
        this.dhns = dhns;
        nodePredicate = new DynamicNodePredicate(from, to);
        edgePredicate = new DynamicEdgePredicate(from, to);
        graph.getFilters().addPredicate(edgePredicate);
        graph.getFilters().addPredicate(nodePredicate);
        
    }

    public void setRange(float from, float to) {
        this.from = from;
        this.to = to;
        Predicate[] oldPredicate = new Predicate[] {edgePredicate, nodePredicate};
        nodePredicate = new DynamicNodePredicate(from, to);
        edgePredicate = new DynamicEdgePredicate(from, to);
        graph.getFilters().updatePredicate(oldPredicate, new Predicate[] {edgePredicate, nodePredicate});
        //((FilterControl) graph.getFilters()).predicateParametersUpdates();
    }

    public float getRangeFrom() {
        return from;
    }

    public float getRangeTo() {
        return to;
    }

    public T getGraph() {
        return graph;
    }
}
