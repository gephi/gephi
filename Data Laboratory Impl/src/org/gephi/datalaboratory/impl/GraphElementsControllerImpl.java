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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
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
import org.gephi.graph.api.NodeData;
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
        Node newNode = buildNode(label);
        getGraph().addNode(newNode);
        return newNode;
    }

    public Node createNode(String label, String id) {
        Graph graph = getGraph();
        if (graph.getNode(id) == null) {
            Node newNode = buildNode(label, id);
            graph.addNode(newNode);
            return newNode;
        } else {
            return null;
        }
    }

    public Node duplicateNode(Node node) {
        if (isNodeInGraph(node)) {
            HierarchicalGraph hg = getHierarchicalGraph();

            Node copy = copyNodeRecursively(node, hg.getParent(node), hg);//Add copy to the same level as the original node
            return copy;
        } else {
            return null;
        }
    }

    public void duplicateNodes(Node[] nodes) {
        for (Node n : nodes) {
            duplicateNode(n);
        }
    }

    public Edge createEdge(Node source, Node target, boolean directed) {
        Edge newEdge;
        if (source != target) {//Cannot create self-loop
            if (directed) {
                newEdge = buildEdge(source, target, true);
                if (getDirectedGraph().addEdge(newEdge)) {//The edge will be created if it does not already exist.
                    return newEdge;
                } else {
                    return null;
                }
            } else {
                newEdge = buildEdge(source, target, false);
                if (getUndirectedGraph().addEdge(newEdge)) {//The edge will be created if it does not already exist.
                    return newEdge;
                } else {
                    return null;
                }
            }
        } else {
            return null;
        }
    }

    public Edge createEdge(String id, Node source, Node target, boolean directed) {
        Edge newEdge;
        if (getGraph().getEdge(id) == null) {
            if (source != target) {//Cannot create self-loop
                if (directed) {
                    newEdge = buildEdge(id, source, target, true);
                    if (getDirectedGraph().addEdge(newEdge)) {//The edge will be created if it does not already exist.
                        return newEdge;
                    } else {
                        return null;
                    }
                } else {
                    newEdge = buildEdge(id, source, target, false);
                    if (getUndirectedGraph().addEdge(newEdge)) {//The edge will be created if it does not already exist.
                        return newEdge;
                    } else {
                        return null;
                    }
                }
            } else {
                return null;
            }
        } else {
            return null;
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
            //Set the group node label to the same used int visualization module:
            group.getNodeData().setLabel(NbBundle.getMessage(GraphElementsControllerImpl.class, "Group.nodeCount.label", getNodeChildrenCount(hg, group)));
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
            ungroupNodes(hg.getDescendant(node).toArray());
            ungroupNode(node);
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
        canUngroup = getNodeChildrenCount(hg, node) > 0;//The node has children
        return canUngroup;
    }

    public boolean moveNodeToGroup(Node node, Node group) {
        if (canMoveNodeToGroup(node, group)) {
            getHierarchicalGraph().moveToGroup(node, group);
            return true;
        } else {
            return false;
        }
    }

    public void moveNodesToGroup(Node[] nodes, Node group) {
        for (Node n : nodes) {
            moveNodeToGroup(n, group);
        }
    }

    public Node[] getAvailableGroupsToMoveNodes(Node[] nodes) {
        if (canGroupNodes(nodes)) {
            HierarchicalGraph hg = getHierarchicalGraph();
            Set<Node> nodesSet = new HashSet<Node>();
            nodesSet.addAll(Arrays.asList(nodes));

            //All have the same parent, get children and check what of them are groups and are not in the nodes array:
            Node parent = hg.getParent(nodes[0]);
            Node[] possibleGroups;
            //If no parent, get nodes at level 0:
            if (parent != null) {
                possibleGroups = hg.getChildren(parent).toArray();
            } else {
                possibleGroups = hg.getNodes(0).toArray();
            }
            ArrayList<Node> availableGroups = new ArrayList<Node>();

            for (Node n : possibleGroups) {
                if (!nodesSet.contains(n) && getNodeChildrenCount(hg, n) > 0) {
                    availableGroups.add(n);
                }
            }

            return availableGroups.toArray(new Node[0]);
        } else {
            return null;
        }
    }

    public boolean canMoveNodeToGroup(Node node, Node group) {
        HierarchicalGraph hg = getHierarchicalGraph();
        return node != group && hg.getParent(node) == hg.getParent(group) && canUngroupNode(group) && isNodeInGraph(node);
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

    public int getNodesCount() {
        Graph graph = getGraph();
        graph.readLock();
        int nodesCount = graph.getNodeCount();
        graph.readUnlock();
        return nodesCount;
    }

    public int getEdgesCount() {
        Graph graph = getGraph();
        graph.readLock();
        int edgesCount = graph.getEdgeCount();
        graph.readUnlock();
        return edgesCount;
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

    private DirectedGraph getDirectedGraph() {
        return Lookup.getDefault().lookup(GraphController.class).getModel().getDirectedGraph();
    }

    private UndirectedGraph getUndirectedGraph() {
        return Lookup.getDefault().lookup(GraphController.class).getModel().getUndirectedGraph();
    }

    private HierarchicalGraph getHierarchicalGraph() {
        return Lookup.getDefault().lookup(GraphController.class).getModel().getHierarchicalGraph();
    }

    private Node buildNode(String label) {
        Node newNode = Lookup.getDefault().lookup(GraphController.class).getModel().factory().newNode();
        newNode.getNodeData().setLabel(label);
        return newNode;
    }

    private Node buildNode(String label, String id) {
        Node newNode = Lookup.getDefault().lookup(GraphController.class).getModel().factory().newNode(id);
        getGraph().setId(newNode, id);
        newNode.getNodeData().setLabel(label);
        return newNode;
    }

    private Edge buildEdge(Node source, Node target, boolean directed) {
        Edge newEdge = Lookup.getDefault().lookup(GraphController.class).getModel().factory().newEdge(source, target, 1.0f, directed);
        return newEdge;
    }

    private Edge buildEdge(String id, Node source, Node target, boolean directed) {
        Edge newEdge = Lookup.getDefault().lookup(GraphController.class).getModel().factory().newEdge(id, source, target, 1.0f, directed);
        return newEdge;
    }

    private Node copyNodeRecursively(Node node, Node parent, HierarchicalGraph hg) {
        NodeData nodeData = node.getNodeData();
        Node copy = buildNode(nodeData.getLabel());
        NodeData copyData = copy.getNodeData();

        //Copy properties (position, size and color):
        copyData.setX(nodeData.x());
        copyData.setY(nodeData.y());
        copyData.setZ(nodeData.z());
        copyData.setSize(nodeData.getSize());
        copyData.setColor(nodeData.r(), nodeData.g(), nodeData.b());
        copyData.setAlpha(nodeData.alpha());

        //Copy attributes:
        AttributeRow row = (AttributeRow) nodeData.getAttributes();
        for (int i = 0; i < row.countValues(); i++) {
            if (row.getValues()[i].getColumn().getOrigin() == AttributeOrigin.DATA) {
                copyData.getAttributes().setValue(i, row.getValue(i));
            }
        }

        if (parent != null) {
            hg.addNode(copy, parent);
        } else {
            hg.addNode(copy);
        }

        //Copy the children of the original node if any:
        Node[] children = hg.getChildren(node).toArray();
        if (children != null) {
            for (Node child : children) {
                copyNodeRecursively(child, copy, hg);
            }
        }

        return copy;
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

    private int getNodeChildrenCount(HierarchicalGraph hg, Node n) {
        hg.readLock();
        int childrenCount = hg.getChildrenCount(n);
        hg.readUnlock();
        return childrenCount;
    }
}
