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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gephi.datalab.api.AttributeColumnsController;
import org.gephi.datalab.api.GraphElementsController;
import org.gephi.datalab.spi.rows.merge.AttributeRowsMergeStrategy;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Table;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * Implementation of the GraphElementsController interface declared in the Data Laboratory API
 *
 * @author Eduardo Ramos
 * @see GraphElementsController
 */
@ServiceProvider(service = GraphElementsController.class)
public class GraphElementsControllerImpl implements GraphElementsController {

    private static final float DEFAULT_NODE_SIZE = 10f;
    private static final float DEFAULT_EDGE_WEIGHT = 1f;

    @Override
    public Node createNode(String label) {
        return createNode(label, getCurrentGraph());
    }

    @Override
    public Node createNode(String label, Graph graph) {
        Node newNode = buildNode(graph, label);
        graph.addNode(newNode);
        return newNode;
    }

    @Override
    public Node createNode(String label, String id) {
        return createNode(label, id, getCurrentGraph());
    }

    @Override
    public Node createNode(String label, String id, Graph graph) {
        if (graph.getNode(id) == null) {
            Node newNode = buildNode(graph, label, id);
            graph.addNode(newNode);
            return newNode;
        } else {
            return null;
        }
    }

    @Override
    public Node duplicateNode(Node node) {
        Graph g = getCurrentGraph();

        Node copy = copyNode(node, g);
        return copy;
    }

    @Override
    public void duplicateNodes(Node[] nodes) {
        for (Node n : nodes) {
            duplicateNode(n);
        }
    }

    @Override
    public Edge createEdge(Node source, Node target, boolean directed) {
        return createEdge(null, source, target, directed, getCurrentGraph());
    }

    @Override
    public Edge createEdge(Node source, Node target, boolean directed, Graph graph) {
        return createEdge(null, source, target, directed, graph);
    }

    @Override
    public Edge createEdge(String id, Node source, Node target, boolean directed) {
        return createEdge(id, source, target, directed, getCurrentGraph());
    }
    
    @Override
    public Edge createEdge(Node source, Node target, boolean directed, Object typeLabel) {
        return createEdge(null, source, target, directed, typeLabel, getCurrentGraph());
    }

    @Override
    public Edge createEdge(Node source, Node target, boolean directed, Object typeLabel, Graph graph) {
        return createEdge(null, source, target, directed, typeLabel, graph);
    }

    @Override
    public Edge createEdge(String id, Node source, Node target, boolean directed, Object typeLabel) {
        return createEdge(id, source, target, directed, typeLabel, getCurrentGraph());
    }

    @Override
    public Edge createEdge(String id, Node source, Node target, boolean directed, Graph graph) {
        return createEdge(id, source, target, directed, null, graph);
    }
    
    @Override
    public Edge createEdge(String id, Node source, Node target, boolean directed, Object typeLabel, Graph graph) {
        Edge newEdge = buildEdge(graph, id, source, target, directed, typeLabel);
        try {
            if (graph.addEdge(newEdge)) {//The edge will be created if it does not already exist.
                return newEdge;
            }
        } catch (Exception e) {
            Logger.getLogger("").log(
                    Level.SEVERE, 
                    "Error when adding edge [id = {0}, source = {1}, target = {2}, directed = {3}, typeLabel = {4}] to the graph. Exception message: {5}",
                    new Object[]{id, source.getId(), target.getId(), directed, typeLabel, e.getMessage()}
            );
        }
        return null;
    }

    @Override
    public void createEdges(Node source, Node[] allNodes, boolean directed) {
        for (Node n : allNodes) {
            if (n != source) {
                createEdge(source, n, directed);
            }
        }
    }

    @Override
    public void deleteNode(Node node) {
        removeNode(node, getCurrentGraph());
    }

    @Override
    public void deleteNodes(Node[] nodes) {
        Graph graph = getCurrentGraph();
        for (Node node : nodes) {
            removeNode(node, graph);
        }
    }

    @Override
    public void deleteEdge(Edge edge) {
        removeEdge(edge, getCurrentGraph());
    }

    @Override
    public void deleteEdges(Edge[] edges) {
        Graph graph = getCurrentGraph();
        for (Edge edge : edges) {
            removeEdge(edge, graph);
        }
    }

    @Override
    public void deleteEdgeWithNodes(Edge edge, boolean deleteSource, boolean deleteTarget) {
        if (deleteSource) {
            deleteNode(edge.getSource());
        }
        if (deleteTarget) {
            deleteNode(edge.getTarget());
        }
        removeEdge(edge, getCurrentGraph());//If no node is deleted, we need to remove the edge.
    }

    @Override
    public void deleteEdgesWithNodes(Edge[] edges, boolean deleteSource, boolean deleteTarget) {
        for (Edge edge : edges) {
            deleteEdgeWithNodes(edge, deleteSource, deleteTarget);
        }
    }

    @Override
    public Node mergeNodes(Graph graph, Node[] nodes, Node selectedNode, Column[] columns, AttributeRowsMergeStrategy[] mergeStrategies, boolean deleteMergedNodes) {
        Table edgesTable = graph.getModel().getEdgeTable();
        if (selectedNode == null) {
            selectedNode = nodes[0];//Use first node as selected node if null
        }

        //Create empty new node:
        Node newNode = createNode("", null, graph);

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
        ac.mergeRowsValues(columns, mergeStrategies, nodes, selectedNode, newNode);

        Set<Node> nodesSet = new HashSet<>();
        nodesSet.addAll(Arrays.asList(nodes));

        //Assign edges to the new node:
        Edge newEdge;
        for (Node node : nodes) {
            for (Edge edge : getNodeEdges(node)) {
                Node newEdgeSource;
                Node newEdgeTarget;
                if (edge.getSource() == node) {
                    newEdgeSource = newNode;
                    if (nodesSet.contains(edge.getTarget())) {
                        newEdgeTarget = newNode;//Self loop because of edge between merged nodes
                    } else {
                        newEdgeTarget = edge.getTarget();
                    }
                } else {
                    newEdgeTarget = newNode;
                    if (nodesSet.contains(edge.getSource())) {
                        newEdgeSource = newNode;//Self loop because of edge between merged nodes
                    } else {
                        newEdgeSource = edge.getSource();
                    }
                }
                if (graph.getEdge(newEdgeSource, newEdgeTarget) != null) {
                    //This edge already exists
                    continue;
                }

                newEdge = createEdge(newEdgeSource, newEdgeTarget, edge.isDirected(), edge.getTypeLabel(), graph);

                if (newEdge != null) {//Edge may not be created if repeated
                    //Copy edge attributes:
                    for (Column column : edgesTable) {
                        if (!column.isReadOnly()) {
                            Object value = edge.getAttribute(column);
                            if (value == null) {
                                newEdge.removeAttribute(column);
                            } else {
                                newEdge.setAttribute(column, edge.getAttribute(column));
                            }
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

    @Override
    public void setNodeFixed(Node node, boolean fixed) {
        node.setFixed(fixed);
    }

    @Override
    public void setNodesFixed(Node[] nodes, boolean fixed) {
        for (Node n : nodes) {
            setNodeFixed(n, fixed);
        }
    }

    @Override
    public boolean isNodeFixed(Node node) {
        return node.isFixed();
    }

    @Override
    public Node[] getNodeNeighbours(Node node) {
        return getCurrentGraph().getNeighbors(node).toArray();
    }

    @Override
    public Edge[] getNodeEdges(Node node) {
        return getCurrentGraph().getEdges(node).toArray();
    }

    @Override
    public int getNodesCount() {
        Graph graph = getCurrentGraph();
        return graph.getNodeCount();
    }

    @Override
    public int getEdgesCount() {
        Graph graph = getCurrentGraph();
        return graph.getEdgeCount();
    }

    @Override
    public boolean isNodeInGraph(Node node) {
        return getCurrentGraph().contains(node);
    }

    @Override
    public boolean areNodesInGraph(Node[] nodes) {
        Graph graph = getCurrentGraph();
        for (Node n : nodes) {
            if (!graph.contains(n)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isEdgeInGraph(Edge edge) {
        return getCurrentGraph().contains(edge);
    }

    @Override
    public boolean areEdgesInGraph(Edge[] edges) {
        Graph graph = getCurrentGraph();
        for (Edge e : edges) {
            if (!graph.contains(e)) {
                return false;
            }
        }
        return true;
    }

    /**
     * **********Private methods : ***********
     */
    private Graph getCurrentGraph() {
        return Lookup.getDefault().lookup(GraphController.class).getGraphModel().getGraph();
    }

    private Node buildNode(Graph graph, String label) {
        return buildNode(graph, label, null);
    }

    private Node buildNode(Graph graph, String label, String id) {
        Node newNode;
        if (id != null) {
            newNode = graph.getModel().factory().newNode(id);
        } else {
            newNode = graph.getModel().factory().newNode();
        }
        newNode.setSize(DEFAULT_NODE_SIZE);
        newNode.setLabel(label);
        
        //Set random position to the node:
        newNode.setX((float) ((0.01 + Math.random()) * 1000) - 500);
        newNode.setY((float) ((0.01 + Math.random()) * 1000) - 500);
        
        return newNode;
    }

    private Edge buildEdge(Graph graph, String id, Node source, Node target, boolean directed, Object typeLabel) {
        int type;
        if(typeLabel == null){
            type = graph.getModel().getEdgeType(null);
        } else {
            //Create the type if missing:
            type = graph.getModel().addEdgeType(typeLabel);
        }
        
        Edge newEdge;
        if (id != null) {
            newEdge = graph.getModel().factory().newEdge(id, source, target, type, DEFAULT_EDGE_WEIGHT, directed);
        } else {
            newEdge = graph.getModel().factory().newEdge(source, target, type, DEFAULT_EDGE_WEIGHT, directed);
        }
        return newEdge;
    }

    private Node copyNode(Node node, Graph graph) {
        Node copy = buildNode(graph, node.getLabel());

        //Copy properties (position, size and color):
        copy.setX(node.x());
        copy.setY(node.y());
        copy.setZ(node.z());
        copy.setSize(node.size());
        copy.setR(node.r());
        copy.setG(node.g());
        copy.setB(node.b());
        copy.setAlpha(node.alpha());

        Table nodeTable = graph.getModel().getNodeTable();

        //Copy attributes:
        for (Column column : nodeTable) {
            if (!column.isReadOnly()) {
                copy.setAttribute(column, node.getAttribute(column));
            }
        }

        graph.addNode(copy);

        return copy;
    }

    private void removeNode(Node node, Graph graph) {
        graph.removeNode(node);
    }

    private void removeEdge(Edge edge, Graph graph) {
        graph.removeEdge(edge);
    }
}
