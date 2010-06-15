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
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public interface GraphElementsController {
    
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

    boolean ungroupNodeRecursively(Node node);

    void ungroupNodesRecursively(Node[] nodes);

    /**
     * Checks if the node can be ungrouped (it forms a group of nodes).
     * @param node Node to check
     * @return True if the node can be ungrouped, false otherwise
     */
    boolean canUngroupNode(Node node);

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
}
