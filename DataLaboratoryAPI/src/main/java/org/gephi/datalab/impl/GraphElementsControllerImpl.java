/*
Copyright 2008-2010 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
 */
package org.gephi.datalab.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.properties.PropertiesColumn;
import org.gephi.datalab.api.AttributeColumnsController;
import org.gephi.datalab.api.GraphElementsController;
import org.gephi.datalab.spi.rows.merge.AttributeRowsMergeStrategy;
import org.gephi.graph.api.Attributes;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeData;
import org.gephi.graph.api.UndirectedGraph;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
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

    private static final float DEFAULT_NODE_SIZE = 10f;
    private static final float DEFAULT_EDGE_WEIGHT = 1f;

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
        HierarchicalGraph hg = getHierarchicalGraph();

        Node copy = copyNodeRecursively(node, hg.getParent(node), hg);//Add copy to the same level as the original node
        return copy;
    }

    public void duplicateNodes(Node[] nodes) {
        for (Node n : nodes) {
            duplicateNode(n);
        }
    }

    public Edge createEdge(Node source, Node target, boolean directed) {
        Edge newEdge;
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
    }

    public Edge createEdge(String id, Node source, Node target, boolean directed) {
        Edge newEdge;
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
    }

    public void createEdges(Node source, Node[] allNodes, boolean directed) {
        for (Node n : allNodes) {
            if (n != source) {
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
            HierarchicalGraph graph = getHierarchicalGraph();
            try {
                float centroidX = 0;
                float centroidY = 0;
                int len = 0;
                float sizes = 0;
                float r = 0;
                float g = 0;
                float b = 0;
                Node group = graph.groupNodes(nodes);
                group.getNodeData().setLabel(NbBundle.getMessage(GraphElementsControllerImpl.class, "Group.nodeCount.label", nodes.length));
                group.getNodeData().setSize(10f);
                for (Node child : nodes) {
                    centroidX += child.getNodeData().x();
                    centroidY += child.getNodeData().y();
                    len++;
                    sizes += child.getNodeData().getSize() / 10f;
                    r += child.getNodeData().r();
                    g += child.getNodeData().g();
                    b += child.getNodeData().b();
                }
                centroidX /= len;
                centroidY /= len;
                group.getNodeData().setSize(sizes);
                group.getNodeData().setColor(r / len, g / len, b / len);
                group.getNodeData().setX(centroidX);
                group.getNodeData().setY(centroidY);
            } catch (Exception e) {
                graph.readUnlockAll();
                NotifyDescriptor.Message nd = new NotifyDescriptor.Message(e.getMessage());
                DialogDisplayer.getDefault().notifyLater(nd);
                return false;
            }
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
        boolean canUngroup;
        HierarchicalGraph hg = getHierarchicalGraph();
        canUngroup = getNodeChildrenCount(hg, node) > 0;//The node has children
        return canUngroup;
    }

    public Node mergeNodes(Node[] nodes, Node selectedNode, AttributeRowsMergeStrategy[] mergeStrategies, boolean deleteMergedNodes) {
        AttributeTable nodesTable = Lookup.getDefault().lookup(AttributeController.class).getModel().getNodeTable();
        if (selectedNode == null) {
            selectedNode = nodes[0];//Use first node as selected node if null
        }
        
        //Create empty new node:
        Node newNode = createNode("");

        //Set properties (position, size and color) using the selected node properties:
        NodeData newNodeData = newNode.getNodeData();
        NodeData selectedNodeData = selectedNode.getNodeData();
        newNodeData.setX(selectedNodeData.x());
        newNodeData.setY(selectedNodeData.y());
        newNodeData.setZ(selectedNodeData.z());
        newNodeData.setSize(selectedNodeData.getSize());
        newNodeData.setColor(selectedNodeData.r(), selectedNodeData.g(), selectedNodeData.b());
        newNodeData.setAlpha(selectedNodeData.alpha());

        //Prepare node rows:
        Attributes[] rows = new Attributes[nodes.length];
        for (int i = 0; i < nodes.length; i++) {
            rows[i] = nodes[i].getAttributes();
        }

        //Merge attributes:        
        AttributeColumnsController ac = Lookup.getDefault().lookup(AttributeColumnsController.class);
        ac.mergeRowsValues(nodesTable, mergeStrategies, rows, selectedNode.getAttributes(), newNode.getAttributes());

        Set<Node> nodesSet=new HashSet<Node>();
        nodesSet.addAll(Arrays.asList(nodes));
        
        //Assign edges to the new node:
        Edge newEdge;
        for (Node node : nodes) {
            for (Edge edge : getNodeEdges(node)) {
                if (edge.getSource() == node) {
                    if (nodesSet.contains(edge.getTarget())) {
                        newEdge = createEdge(newNode, newNode, edge.isDirected());//Self loop because of edge between merged nodes
                    } else {
                        newEdge = createEdge(newNode, edge.getTarget(), edge.isDirected());
                    }
                } else {
                    if (nodesSet.contains(edge.getSource())) {
                        newEdge = createEdge(newNode, newNode, edge.isDirected());//Self loop because of edge between merged nodes
                    } else {
                        newEdge = createEdge(edge.getSource(), newNode, edge.isDirected());
                    }
                }

                if (newEdge != null) {//Edge may not be created if repeated
                    //Copy edge attributes:
                    AttributeRow row = (AttributeRow) edge.getAttributes();
                    for (int i = 0; i < row.countValues(); i++) {
                        if (row.getAttributeValueAt(i).getColumn().getIndex() != PropertiesColumn.EDGE_ID.getIndex()) {
                            newEdge.getAttributes().setValue(i, row.getValue(i));
                        }
                    }
                }
            }
        }

        //Finally delete merged nodes:
        if (deleteMergedNodes) {
            deleteNodes(nodes);
        }

        return newNode;
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
        return node != group && hg.getParent(node) == hg.getParent(group) && canUngroupNode(group);
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
        HierarchicalGraph hg = getHierarchicalGraph();
        return hg.getParent(node) != null;
    }

    public void setNodeFixed(Node node, boolean fixed) {
        node.getNodeData().setFixed(fixed);
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
        newNode.getNodeData().setSize(DEFAULT_NODE_SIZE);
        newNode.getNodeData().setLabel(label);
        return newNode;
    }

    private Node buildNode(String label, String id) {
        Node newNode = Lookup.getDefault().lookup(GraphController.class).getModel().factory().newNode(id);
        newNode.getNodeData().setSize(DEFAULT_NODE_SIZE);
        newNode.getNodeData().setLabel(label);
        return newNode;
    }

    private Edge buildEdge(Node source, Node target, boolean directed) {
        Edge newEdge = Lookup.getDefault().lookup(GraphController.class).getModel().factory().newEdge(source, target, DEFAULT_EDGE_WEIGHT, directed);
        return newEdge;
    }

    private Edge buildEdge(String id, Node source, Node target, boolean directed) {
        Edge newEdge = Lookup.getDefault().lookup(GraphController.class).getModel().factory().newEdge(id, source, target, DEFAULT_EDGE_WEIGHT, directed);
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
            if (row.getAttributeValueAt(i).getColumn().getIndex() != PropertiesColumn.NODE_ID.getIndex()) {
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
        graph.removeNode(node);
    }

    private void removeEdge(Edge edge, Graph graph) {
        graph.removeEdge(edge);
    }

    private int getNodeChildrenCount(HierarchicalGraph hg, Node n) {
        hg.readLock();
        int childrenCount = hg.getChildrenCount(n);
        hg.readUnlock();
        return childrenCount;
    }
}
