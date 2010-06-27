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

import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.datalaboratory.api.GraphElementsController;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.MixedGraph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.UndirectedGraph;
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

    public Node createNode(String label) {
        Lookup.getDefault().lookup(AttributeController.class).getModel();//Make sure graph has AttributeModel, this can be first node.
        Node newNode = Lookup.getDefault().lookup(GraphController.class).getModel().factory().newNode();
        newNode.getNodeData().setLabel(label);        
        getGraph().addNode(newNode);
        return newNode;
    }

    public Node duplicateNode(Node node) {
        if (isNodeInGraph(node)) {
            Node copy=createNode(node.getNodeData().getLabel());
            AttributeRow row=(AttributeRow) node.getNodeData().getAttributes();
            for (int i = 0; i < row.countValues(); i++) {
                if(row.getValues()[i].getColumn().getOrigin()==AttributeOrigin.DATA){
                    copy.getNodeData().getAttributes().setValue(i, row.getValue(i));
                }
            }
            return copy;
        } else {
            return null;
        }
    }

    public void duplicateNodes(Node[] nodes){
        for(Node n:nodes){
            duplicateNode(n);
        }
    }

    public boolean createEdge(Node source, Node target, boolean directed) {
        if (isNodeInGraph(source) && isNodeInGraph(target)) {
            if (source != target) {//Cannot create self-loop
                if(directed){
                    return getDirectedGraph().addEdge(source, target);//The edge will be created if it does not already exist.
                }else{
                    return getUndirectedGraph().addEdge(source, target);//The edge will be created if it does not already exist.
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public void createEdges(Node source, Node[] allNodes, boolean directed) {
        if (isNodeInGraph(source) && areNodesInGraph(allNodes)) {
            for (Node n : allNodes) {
                createEdge(source, n, directed);
            }
        }
    }

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

    public void deleteEdgeWithNodes(Edge edge, boolean deleteSource, boolean deleteTarget) {
        if (deleteSource) {
            deleteNode(edge.getSource());
        }
        if (deleteTarget) {
            deleteNode(edge.getTarget());
        }
        removeEdge(edge, getGraph());//If no node is deleted, we need to remove the edge.
    }

    public void deleteEdgesWithNodes(Edge[] edges, boolean deleteSource, boolean deleteTarget) {
        for (Edge edge : edges) {
            deleteEdgeWithNodes(edge, deleteSource, deleteTarget);
        }
    }

    public boolean groupNodes(Node[] nodes) {
        if (canGroupNodes(nodes)) {
            HierarchicalGraph hg = getHierarchicalGraph();
            Node group = hg.groupNodes(nodes);
            hg.readLock();
            //Set the group node label to the same used int visualization module:
            group.getNodeData().setLabel(NbBundle.getMessage(GraphElementsControllerImpl.class, "Group.nodeCount.label", hg.getChildrenCount(group)));
            hg.readUnlock();
            return true;
        } else {
            return false;
        }
    }

    public boolean canGroupNodes(Node[] nodes) {
        if (!areNodesInGraph(nodes)) {
            return false;
        }
        HierarchicalGraph hg = getHierarchicalGraph();
        Node parent = hg.getParent(nodes[0]);
        for (Node n : nodes) {
            if (hg.getParent(n) != parent) {
                return false;
            }
        }
        return true;
    }

    public boolean ungroupNode(Node node) {
        if (canUngroupNode(node)) {
            HierarchicalGraph hg = getHierarchicalGraph();
            hg.ungroupNodes(node);
            return true;
        } else {
            return false;
        }
    }

    public void ungroupNodes(Node[] nodes) {
        for (Node n : nodes) {
            ungroupNode(n);
        }
    }

    public boolean ungroupNodeRecursively(Node node) {
        if (canUngroupNode(node)) {
            HierarchicalGraph hg = getHierarchicalGraph();
            //We can get directly all descendant nodes withoud using recursion and break the groups:
            for (Node n : hg.getDescendant(node).toArray()) {
                ungroupNode(n);
            }
            hg.ungroupNodes(node);
            return true;
        } else {
            return false;
        }
    }

    public void ungroupNodesRecursively(Node[] nodes) {
        for (Node n : nodes) {
            ungroupNodeRecursively(n);
        }
    }

    public boolean canUngroupNode(Node node) {
        if (!isNodeInGraph(node)) {
            return false;
        }
        boolean canUngroup;
        HierarchicalGraph hg = getHierarchicalGraph();
        hg.readLock();
        canUngroup = hg.getChildrenCount(node) > 0;//The node has children
        hg.readUnlock();
        return canUngroup;
    }

    public boolean removeNodeFromGroup(Node node) {
        if (isNodeInGroup(node)) {
            HierarchicalGraph hg = getHierarchicalGraph();
            Node parent = hg.getParent(node);
            hg.readLock();
            int childrenCount = hg.getChildrenCount(parent);
            hg.readUnlock();
            if (childrenCount == 1) {
                hg.ungroupNodes(parent);//Break group when the last child is removed.
            } else {
                hg.removeFromGroup(node);
            }
            return true;
        } else {
            return false;
        }
    }

    public void removeNodesFromGroup(Node[] nodes) {
        for (Node n : nodes) {
            removeNodeFromGroup(n);
        }
    }

    public boolean isNodeInGroup(Node node) {
        if (!isNodeInGraph(node)) {
            return false;
        }
        HierarchicalGraph hg = getHierarchicalGraph();
        return hg.getParent(node) != null;
    }

    public void setNodeFixed(Node node, boolean fixed) {
        if (isNodeInGraph(node)) {
            node.getNodeData().setFixed(fixed);
        }
    }

    public void setNodesFixed(Node[] nodes, boolean fixed) {
        for (Node n : nodes) {
            setNodeFixed(n, fixed);
        }
    }

    public boolean isNodeFixed(Node node) {
        return node.getNodeData().isFixed();
    }

    public Node[] getNodeNeighbours(Node node) {
        return getGraph().getNeighbors(node).toArray();
    }

    public Edge[] getNodeEdges(Node node) {
        return getGraph().getEdges(node).toArray();
    }

    public boolean isNodeInGraph(Node node) {
        return getGraph().contains(node);
    }

    public boolean areNodesInGraph(Node[] nodes) {
        Graph graph = getGraph();
        for (Node n : nodes) {
            if (!graph.contains(n)) {
                return false;
            }
        }
        return true;
    }

    public boolean isEdgeInGraph(Edge edge) {
        return getGraph().contains(edge);
    }

    public boolean areEdgesInGraph(Edge[] edges) {
        Graph graph = getGraph();
        for (Edge e : edges) {
            if (!graph.contains(e)) {
                return false;
            }
        }
        return true;
    }

    /************Private methods : ************/
    private Graph getGraph() {
        return Lookup.getDefault().lookup(GraphController.class).getModel().getGraph();
    }

    private MixedGraph getMixedGraph() {
        return Lookup.getDefault().lookup(GraphController.class).getModel().getMixedGraph();
    }

    private DirectedGraph getDirectedGraph(){
        return Lookup.getDefault().lookup(GraphController.class).getModel().getDirectedGraph();
    }

    private UndirectedGraph getUndirectedGraph(){
        return Lookup.getDefault().lookup(GraphController.class).getModel().getUndirectedGraph();
    }

    private HierarchicalGraph getHierarchicalGraph() {
        return Lookup.getDefault().lookup(GraphController.class).getModel().getHierarchicalGraph();
    }

    private void removeNode(Node node, Graph graph) {
        if (isNodeInGraph(node)) {
            graph.removeNode(node);
        }
    }

    private void removeEdge(Edge edge, Graph graph) {
        if (isEdgeInGraph(edge)) {
            graph.removeEdge(edge);
        }
    }
}
