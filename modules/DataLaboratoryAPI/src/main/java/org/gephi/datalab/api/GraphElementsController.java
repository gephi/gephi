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
package org.gephi.datalab.api;

import org.gephi.datalab.spi.rows.merge.AttributeRowsMergeStrategy;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;

/**
 * <p>This interface defines part of the Data Laboratory API basic actions.</p>
 * <p>It contains methods for manipulating the nodes and edges of the graph.</p>
 * <p>All the provided methods take care to check first that the nodes and edges to manipulate are in the graph.</p>
 * @author Eduardo Ramos
 */
public interface GraphElementsController {

    /**
     * Creates a node with default id and the given label in the current graph.
     * @param label Label for the node
     * @return The new created node
     */
    Node createNode(String label);
    
    /**
     * Creates a node with default id and the given label.
     * @param label Label for the node
     * @param graph Graph to insert the node into
     * @return The new created node
     */
    Node createNode(String label, Graph graph);

    /**
     * <p>Creates a node with the given id and label in the current graph.</p>
     * <p>If a node with that id already exists, no node will be created</p>
     * @param label Label for the node
     * @param id Id for the node
     * @return The new created node or null if a node with the given id already exists
     */
    Node createNode(String label, String id);
    
    /**
     * <p>Creates a node with the given id and label.</p>
     * <p>If a node with that id already exists, no node will be created</p>
     * @param label Label for the node
     * @param id Id for the node
     * @param graph Graph to insert the node into
     * @return The new created node or null if a node with the given id already exists
     */
    Node createNode(String label, String id, Graph graph);

    /**
     * <p>Duplicates a node if it is in the graph, and returns the new node.</p>
     * <p>If the node has children, they are also copied as children of the new node.</p>
     * <p>Sets the same properties and attributes for the node as the original node: id, label and <code>AttributeColumns</code> with <code>DATA</code> <code>AttributeOrigin</code>.
     * Does not copy <code>AttributeColumns</code> with <code>COMPUTED</code> <code>AttributeOrigin</code>.</p>
     * @param node Node to copy
     * @return New node
     */
    Node duplicateNode(Node node);

    /**
     * Tries to duplicate an array of nodes with the same behaviour as <code>duplicateNode</code> method.
     * @param nodes Array of nodes to duplicate
     */
    void duplicateNodes(Node[] nodes);

    /**
     * <p>Creates and edge between source and target node (if it does not already exist), directed or undirected, in the current graph.</p>
     * @param source Source node
     * @param target Target node
     * @param directed Indicates if the edge has to be directed
     * @return New edge if the edge was created succesfully, null otherwise
     */
    Edge createEdge(Node source, Node target, boolean directed);
    
    /**
     * <p>Creates and edge between source and target node (if it does not already exist), directed or undirected.</p>
     * @param source Source node
     * @param target Target node
     * @param directed Indicates if the edge has to be directed
     * @param graph Graph to insert the node into
     * @return New edge if the edge was created succesfully, null otherwise
     */
    Edge createEdge(Node source, Node target, boolean directed, Graph graph);

    /**
     * <p>Creates and edge between source and target node (if it does not already exist), directed or undirected.</p>
     * <p>If a edge with the given id already exists, no edge will be created.</p>
     * @param id Id for the new edge
     * @param source Source node
     * @param target Target node
     * @param directed Indicates if the edge has to be directed
     * @return New edge if the edge was created succesfully, null otherwise
     */
    Edge createEdge(String id, Node source, Node target, boolean directed);
    
    /**
     * <p>Creates and edge between source and target node (if it does not already exist), directed or undirected, in the current graph.</p>
     * <p>If a edge with the given id already exists, no edge will be created.</p>
     * @param id Id for the new edge
     * @param source Source node
     * @param target Target node
     * @param directed Indicates if the edge has to be directed
     * @param graph Graph to insert the node into
     * @return New edge if the edge was created succesfully, null otherwise
     */
    Edge createEdge(String id, Node source, Node target, boolean directed, Graph graph);
    
    /**
     * <p>Tries to create edges between the source node and all other edges, directed or undirected.</p>
     * <p>An edge won't be created if it already exists or is a self-loop.</p>
     * @param source Source node
     * @param allNodes All edges
     * @param directed Indicates if the edges have to be directed
     */
    void createEdges(Node source, Node[] allNodes, boolean directed);

    /**
     * Tries to delete a node checking first if it is on the graph.
     * @param node Node to delete
     */
    void deleteNode(Node node);

    /**
     * Tries to delete an array of nodes checking first if they are on the graph.
     * @param nodes Array of nodes to delete
     */
    void deleteNodes(Node[] nodes);

    /**
     * Tries to delete an edge checking first if it is on the graph.
     * @param edge Edge to delete
     */
    void deleteEdge(Edge edge);

    /**
     * Tries to delete an array of edges checking first if they are on the graph.
     * @param edges Array of edges to delete
     */
    void deleteEdges(Edge[] edges);

    /**
     * Tries to delete an edge checking first if it is on the graph
     * and also deletes its source and target node if it is indicated.
     * @param edge Edge to delete
     * @param deleteSource Indicates if the source node has to be deleted
     * @param deleteTarget Indicates if the target node has to be deleted
     */
    void deleteEdgeWithNodes(Edge edge, boolean deleteSource, boolean deleteTarget);

    /**
     * Tries to delete an array of edges checking first if they are on the graph
     * and also deletes their source and target node if it is indicated.
     * @param edges Array of edges to delete
     * @param deleteSource Indicates if the source nodes have to be deleted
     * @param deleteTarget Indicates if the target nodes have to be deleted
     */
    void deleteEdgesWithNodes(Edge[] edges, boolean deleteSource, boolean deleteTarget);

    /**
     * Merges 2 or more nodes into a new one node that has all the edges of the merged nodes.
     * An AttributeRowsMergeStrategy must be provided for each column of the nodes.
     * @param nodes Nodes to merge (at least 1)
     * @param selectedNode Main selected node of the nodes to merge (or null to use first node)
     * @param columns Columns to apply a merge strategy in each row
     * @param mergeStrategies Strategies to merge rows of each column in {@code columns}
     * @param deleteMergedNodes Indicates if merged nodes should be deleted
     * @return New resulting node
     */
    Node mergeNodes(Node[] nodes, Node selectedNode, Column[] columns, AttributeRowsMergeStrategy[] mergeStrategies, boolean deleteMergedNodes);

    /**
     * Sets the fixed state of a node to the indicated.
     * @param node Node to set fixed state
     * @param fixed Fixed state for the node
     */
    void setNodeFixed(Node node, boolean fixed);

    /**
     * Sets the fixed state of an array of nodes to the indicated.
     * @param nodes Array of nodes to set fixed state
     * @param fixed Fixed state for the nodes
     */
    void setNodesFixed(Node[] nodes, boolean fixed);

    /**
     * Checks the fixed state of a node.
     * @param node Node to check
     * @return Fixed state of the node
     */
    boolean isNodeFixed(Node node);

    /**
     * Prepares and returns an array with the neighbour nodes of the specified node.
     * @param node Node to get neighbours
     * @return Array of neighbour nodes
     */
    Node[] getNodeNeighbours(Node node);

    /**
     * Prepares and returns an array with the edges incident to the specified node.
     * @param node Node to get edges
     * @return Array of incident edges
     */
    Edge[] getNodeEdges(Node node);

    /**
     * Returns the number of nodes in the graph.
     * @return Nodes count
     */
    int getNodesCount();

    /**
     * Returns the number of edges in the graph.
     * @return Edges count
     */
    int getEdgesCount();

    /**
     * Checks if a node is contained in the main view graph.
     * @param node Node to check
     * @return True if the node is in the graph, false otherwise
     */
    boolean isNodeInGraph(Node node);

    /**
     * Checks if an array of nodes are contained in the main view graph.
     * @param nodes Array of nodes to check
     * @return True if all the nodes are in the graph, false otherwise
     */
    boolean areNodesInGraph(Node[] nodes);

    /**
     * Checks if an edge is contained in the main view graph.
     * @param edge Edge to check
     * @return True if the edge is in the graph, false otherwise
     */
    boolean isEdgeInGraph(Edge edge);

    /**
     * Checks if an array of edges are contained in the main view graph.
     * @param edges Edges to check
     * @return True if all the edges are in the graph, false otherwise
     */
    boolean areEdgesInGraph(Edge[] edges);
}
