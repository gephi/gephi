/*
Copyright 2008-2010 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
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
package org.gephi.datalaboratory.impl;

import org.gephi.datalaboratory.api.GraphElementsController;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Implementation of the GraphElementsController interface 
 * declared in the Data Laboratory API
 * @author Eduardo Ramos <eduramiba@gmail.com>
 * @see GraphElementsController
 */
@ServiceProvider(service = GraphElementsController.class)
public class GraphElementsControllerImpl implements GraphElementsController {

    public void deleteNode(Node node) {
        removeNode(node, getGraph());
    }

    public void deleteNodes(Node[] nodes) {
        Graph graph = getGraph();
        for (Node node : nodes) {
            removeNode(node, graph);
        }
    }

    public void deleteEdge(Edge edge) {
        removeEdge(edge, getGraph());
    }

    public void deleteEdges(Edge[] edges) {
        Graph graph = getGraph();
        for (Edge edge : edges) {
            removeEdge(edge, graph);
        }
    }

    private void removeNode(Node node, Graph graph) {
        if (graph.contains(node)) {
            graph.removeNode(node);
        }
    }

    private void removeEdge(Edge edge, Graph graph) {
        if (graph.contains(edge)) {
            graph.removeEdge(edge);
        }
    }

    public boolean groupNodes(Node[] nodes) {
        if (canGroupNodes(nodes)) {
            HierarchicalGraph hg = getHierarchicalGraph();
            Node group=hg.groupNodes(nodes);
            hg.readLock();
            group.getNodeData().setLabel(NbBundle.getMessage(GraphElementsControllerImpl.class, "Group.nodeCount.label", hg.getChildrenCount(group)));
            hg.readUnlock();
            return true;
        } else {
            return false;
        }
    }

    public boolean canGroupNodes(Node[] nodes) {
        HierarchicalGraph hg = getHierarchicalGraph();
        Node parent = hg.getParent(nodes[0]);
        for (Node n : nodes) {
            if (hg.getParent(n) != parent) {
                return false;
            }
        }
        return true;
    }

    public boolean ungroupNodes(Node node) {
        if(canUngroupNodes(node)){
            HierarchicalGraph hg = getHierarchicalGraph();
            hg.ungroupNodes(node);
            return true;
        }else{
            return false;
        }
    }

    public boolean canUngroupNodes(Node node) {
        boolean canUngroup;
        HierarchicalGraph hg = getHierarchicalGraph();
        hg.readLock();
        canUngroup=hg.getChildrenCount(node)>0;
        hg.readUnlock();
        return canUngroup;
    }

    private Graph getGraph() {
        return Lookup.getDefault().lookup(GraphController.class).getModel().getGraph();
    }

    private HierarchicalGraph getHierarchicalGraph() {
        return Lookup.getDefault().lookup(GraphController.class).getModel().getHierarchicalGraph();
    }
}
