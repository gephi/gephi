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

import org.gephi.graph.api.DynamicData;
import org.gephi.graph.api.DynamicGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgePredicate;
import org.gephi.graph.api.FilteredGraph;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphEvent.EventType;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodePredicate;
import org.gephi.graph.dhns.core.Dhns;

/**
 *
 * @author Mathieu Bastian
 */
public class DynamicGraphImpl<T extends Graph> implements DynamicGraph {

    //Graph reference
    private T graph;
    private Dhns dhns;

    //Range
    private float from = 0;
    private float to = 1;

    public DynamicGraphImpl(Dhns dhns, T graph) {
        this.graph = graph;
        this.dhns = dhns;
        FilteredGraph filteredGraph = (FilteredGraph) graph;
        filteredGraph.addNodePredicate(new DynamicNodePredicate());
        filteredGraph.addEdgePredicate(new DynamicEdgePredicate());
    }

    public void setRange(float from, float to) {
        graph.writeLock();
        this.from = from;
        this.to = to;
        graph.writeUnlock();
        dhns.getGraphVersion().incNodeAndEdgeVersion();
        dhns.getEventManager().fireEvent(EventType.NODES_AND_EDGES_UPDATED);
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

    private class DynamicNodePredicate implements NodePredicate {

        public boolean evaluate(Node element) {
            //Check if element is in the range
            DynamicData dd = element.getNodeData().getDynamicData();
            if (dd.getRangeFrom() == -1 || dd.getRangeTo() == -1) {
                return true;
            }
            return dd.getRangeFrom() >= from && dd.getRangeTo() <= to;
        }
    }

    private class DynamicEdgePredicate implements EdgePredicate {

        public boolean evaluate(Edge element) {
            //Check if element is in the range
            DynamicData dd = element.getEdgeData().getDynamicData();
            if (dd.getRangeFrom() == -1 || dd.getRangeTo() == -1) {
                DynamicData ddTarget = element.getTarget().getNodeData().getDynamicData();
                if(!(ddTarget.getRangeFrom() >= from && ddTarget.getRangeTo() <= to)) {
                    return false;
                }
                return true;
            }
            return dd.getRangeFrom() >= from && dd.getRangeTo() <= to;
        }
    }
}
