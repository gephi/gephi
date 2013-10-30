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

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.gephi.attribute.api.Column;
import org.gephi.attribute.api.Table;
import org.gephi.datalab.api.AttributeColumnsController;
import org.gephi.datalab.api.GraphElementsController;
import org.gephi.datalab.spi.rows.merge.AttributeRowsMergeStrategy;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.UndirectedGraph;
import org.openide.util.Lookup;
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
    private static final int DEFAULT_EDGE_TYPE = 1;
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
        Graph g = getGraph();

        Node copy = copyNode(node, g);
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

    public Node mergeNodes(Node[] nodes, Node selectedNode, AttributeRowsMergeStrategy[] mergeStrategies, boolean deleteMergedNodes) {
        Table nodesTable = Lookup.getDefault().lookup(GraphController.class).getAttributeModel().getNodeTable();
        Table edgesTable = Lookup.getDefault().lookup(GraphController.class).getAttributeModel().getEdgeTable();
        if (selectedNode == null) {
            selectedNode = nodes[0];//Use first node as selected node if null
        }
        
        //Create empty new node:
        Node newNode = createNode("");

        //Set properties (position, size and color) using the selected node properties:
        newNode.setX(selectedNode.x());
        newNode.setY(selectedNode.y());
        newNode.setZ(selectedNode.z());
        
        newNode.setSize(selectedNode.size());
        
        newNode.setR(selectedNode.r());
        newNode.setG(selectedNode.g());
        newNode.setB(selectedNode.b());
        newNode.setAlpha(selectedNode.alpha());

        //Merge attributes:        
        AttributeColumnsController ac = Lookup.getDefault().lookup(AttributeColumnsController.class);
        ac.mergeRowsValues(nodesTable, mergeStrategies, nodes, selectedNode, newNode);

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
                    for (Column column : edgesTable) {
                        newEdge.setAttribute(column, edge.getAttribute(column));
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

    public void setNodeFixed(Node node, boolean fixed) {
        node.setFixed(fixed);
    }

    public void setNodesFixed(Node[] nodes, boolean fixed) {
        for (Node n : nodes) {
            setNodeFixed(n, fixed);
        }
    }

    public boolean isNodeFixed(Node node) {
        return node.isFixed();
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
        return Lookup.getDefault().lookup(GraphController.class).getGraphModel().getGraph();
    }

    private DirectedGraph getDirectedGraph() {
        return Lookup.getDefault().lookup(GraphController.class).getGraphModel().getDirectedGraph();
    }

    private UndirectedGraph getUndirectedGraph() {
        return Lookup.getDefault().lookup(GraphController.class).getGraphModel().getUndirectedGraph();
    }

    private Node buildNode(String label) {
        Node newNode = Lookup.getDefault().lookup(GraphController.class).getGraphModel().factory().newNode();
        newNode.setSize(DEFAULT_NODE_SIZE);
        newNode.setLabel(label);
        return newNode;
    }

    private Node buildNode(String label, String id) {
        Node newNode = Lookup.getDefault().lookup(GraphController.class).getGraphModel().factory().newNode(id);
        newNode.setSize(DEFAULT_NODE_SIZE);
        newNode.setLabel(label);
        return newNode;
    }

    private Edge buildEdge(Node source, Node target, boolean directed) {
        Edge newEdge = Lookup.getDefault().lookup(GraphController.class).getGraphModel().factory().newEdge(source, target, DEFAULT_EDGE_TYPE, DEFAULT_EDGE_WEIGHT, directed);
        return newEdge;
    }

    private Edge buildEdge(String id, Node source, Node target, boolean directed) {
        Edge newEdge = Lookup.getDefault().lookup(GraphController.class).getGraphModel().factory().newEdge(id, source, target, DEFAULT_EDGE_TYPE, DEFAULT_EDGE_WEIGHT, directed);
        return newEdge;
    }

    private Node copyNode(Node node, Graph g) {
        Node copy = buildNode(node.getLabel());

        //Copy properties (position, size and color):
        copy.setX(node.x());
        copy.setY(node.y());
        copy.setZ(node.z());
        copy.setSize(node.size());
        copy.setR(node.r());
        copy.setG(node.g());
        copy.setB(node.b());
        copy.setAlpha(node.alpha());

        Table nodeTable = Lookup.getDefault().lookup(GraphController.class).getAttributeModel().getNodeTable();
        
        //Copy attributes:
        for (Column column : nodeTable) {
            copy.setAttribute(column, node.getAttribute(column));
        }

        g.addNode(copy);

        return copy;
    }

    private void removeNode(Node node, Graph graph) {
        graph.removeNode(node);
    }

    private void removeEdge(Edge edge, Graph graph) {
        graph.removeEdge(edge);
    }
}
