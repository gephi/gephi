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
package org.gephi.datalaboratory.api;

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;

/**
 * This interface defines part of the Data Laboratory API. It contains methods for manipulating
 * the nodes and edges of the graph.
 * All the provided methods take care to check first that the nodes and edges to manipulate are in the graph.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public interface GraphElementsController {

    /**
     * Creates a node with default id and the given label.
     * @param label Label for the node
     * @return The new created node
     */
    Node createNode(String label);

    /**
     * Creates a node with the given id and label.
     * If a node with that id already exists, no node will be created
     * @param label Label for the node
     * @param id Id for the node
     * @return The new created node or null if a node with the given id already exists
     */
    Node createNode(String label, String id);

    /**
     * Duplicates a node if it is in the graph, and returns the new node.
     * If the node has children, they are also copied as children of the new node.
     * Sets the same properties and attributes for the node as the original node: id, label and AttributeColumns with DATA AttributeOrigin.
     * Does not copy AttributeColumns with COMPUTED AttributeOrigin.
     * @param node Node to copy
     * @return New node
     */
    Node duplicateNode(Node node);

    /**
     * Tries to duplicate an array of nodes with the same behavious as <code>duplicateNode</code> method.
     * @param nodes Array of nodes to duplicate
     */
    void duplicateNodes(Node[] nodes);

    /**
     * Creates and edge between source and target node (if does not already exist), directed or undirected.
     * This will not create a self-loop.
     * @param source Source node
     * @param target Target node
     * @param directed Indicates if the edge has to be directed
     * @return New edge if the edge was created succesfully, null otherwise
     */
    Edge createEdge(Node source, Node target, boolean directed);

    /**
     * Creates and edge between source and target node (if does not already exist), directed or undirected.
     * If a edge with the given id already exists, no edge will be created.
     * This will not create a self-loop.
     * @param source Source node
     * @param target Target node
     * @param directed Indicates if the edge has to be directed
     * @return New edge if the edge was created succesfully, null otherwise
     */
    Edge createEdge(String id,Node source, Node target, boolean directed);

    /**
     * Tries to create edges between the source node and all other edges, directed or undirected.
     * An edge won't be created if it already exists or is a self-loop.
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
     * @param node Edge to delete
     */
    void deleteEdge(Edge edge);

    /**
     * Tries to delete an array of edges checking first if they are on the graph.
     * @param nodes Array of edges to delete
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
     * Groups an array of nodes if it is possible.
     * @param nodes Array of nodes to group
     * @return True if the nodes were succesfully grouped, false otherwise
     */
    boolean groupNodes(Node[] nodes);

    /**
     * Checks if an array of nodes can form a group.
     * @param nodes Array of nodes to check
     * @return True if the nodes can form a group, false otherwise
     */
    boolean canGroupNodes(Node[] nodes);

    /**
     * Ungroups a node if it forms a group.
     * @param nodes Node to ungroup
     * @return True if the node was succesfully ungrouped, false otherwise
     */
    boolean ungroupNode(Node node);

    /**
     * Tries to ungroup every node un the array of nodes checking first they form a group.
     * @param nodes Array of nodes to ungroup
     */
    void ungroupNodes(Node[] nodes);

    /**
     * Ungroups a node if it forms a group and also ungroups all its descendant.
     * @param node Node to ungroup recursively
     * @return True if the node was succesfully ungrouped, false otherwise
     */
    boolean ungroupNodeRecursively(Node node);

    /**
     * Tries to ungroup every node un the array of nodes checking first they form a group.
     * @param nodes Array of nodes to ungroup
     */
    void ungroupNodesRecursively(Node[] nodes);

    /**
     * Checks if the node can be ungrouped (it forms a group of nodes).
     * @param node Node to check
     * @return True if the node can be ungrouped, false otherwise
     */
    boolean canUngroupNode(Node node);

    /**
     * Moves a node to a group of nodes if it is possible.
     * To move a node to a group node, they must be different, have the same parent and the group node has to be a group of nodes.
     * @param node Node to move to group
     * @param group Group of nodes to move the node
     * @return True if the node was moved, false otherwise
     */
    boolean moveNodeToGroup(Node node, Node group);

    /**
     * Tries to move each node of the nodes array to the group node.
     * @param nodes Array of nodes to move
     * @param group Group node
     */
    void moveNodesToGroup(Node[] nodes, Node group);

    /**
     * Prepares and returns an array with the groups which the given nodes can be moved to.
     * This groups are the nodes that have the same parent as the given nodes and are not in the given nodes array.
     * @param nodes Nodes to get available groups to be moved
     * @return Available groups array of null if the nodes don't all have the same parent
     */
    Node[] getAvailableGroupsToMoveNodes(Node[] nodes);

    /**
     * Indicates if a given node can be moved to a group node.
     * To move a node to a group, they must have the same parent and the group node has to be a group of nodes.
     * @param node Node to check if can be moved
     * @param group Group node
     * @return True if it can be moved, false otherwise
     */
    boolean canMoveNodeToGroup(Node node, Node group);

    /**
     * Removes a node from its group if the node is in a group (has a parent).
     * Also breaks the group if the last node is removed.
     * @param node Node to remove from its group
     * @return True if the node was removed from a group, false otherwise
     */
    boolean removeNodeFromGroup(Node node);

    /**
     * Tries to remove every node in the array from its group checking first they are in a group.
     * Also breaks groups when the last node is removed.
     * @param nodes Arrays of nodes to remove from its group
     */
    void removeNodesFromGroup(Node[] nodes);

    /**
     * Checks if the node is in a group (has a parent).
     * @return True if the node is in a group, false otherwise
     */
    boolean isNodeInGroup(Node node);

    /**
     * Sets a fixed state of a node to the indicated.
     * @param node Node to set fixed state
     * @param fixed Fixed state for the node
     */
    void setNodeFixed(Node node, boolean fixed);

    /**
     * Sets a fixed state of an array of nodes to the indicated.
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
     * Checks if a node is contained in the graph.
     * @param node Node to check
     * @return True if the node is in the graph, false otherwise
     */
    boolean isNodeInGraph(Node node);

    /**
     * Checks if an array of nodes are contained in the graph.
     * @param nodes Array of nodes to check
     * @return True if all the nodes are in the graph, false otherwise
     */
    boolean areNodesInGraph(Node[] nodes);

    /**
     * Checks if an edge is contained in the graph.
     * @param edge Edge to check
     * @return True if the edge is in the graph, false otherwise
     */
    boolean isEdgeInGraph(Edge edge);

    /**
     * Checks if an array of edges are contained in the graph.
     * @param edges Edges to check
     * @return True if all the edges are in the graph, false otherwise
     */
    boolean areEdgesInGraph(Edge[] edges);
}
