package org.gephi.manageneighbors.api;

import org.gephi.graph.api.Node;

/**
 *
 * @author Jozef Barčák - barcak.jozef@gmail.com - 2012
 */
public interface GraphElementsController {
    
    /**
     * Hides connected leaves of an array of nodes.
     * @param nodes Array of nodes to hide connected leaves
     * @return <code>TRUE</code> if the nodes were successfully hided, <code>FALSE</code> otherwise
     */
    boolean hideLeaves(Node[] nodes);
    
    /**
     * Checks if an array of nodes can be hide.
     * @param nodes Array of nodes to check
     * @return True if the nodes can form be hide, false otherwise
     */
    boolean canHideNodes(Node[] nodes);
    
    /**
     * Show a node if it forms a group.
     * @param node Node to show
     * @return True if the node was succesfully showed, false otherwise
     */
    boolean showNode(Node node);
    
    /**
     * Show connected leaves of an array of nodes.
     * @param nodes Array of nodes to show connected leaves
     * @return <code>TRUE</code> if the nodes were successfully showed, <code>FALSE</code> otherwise
     */
    void showNodes(Node[] nodes);
    
    /**
     * Checks if the node can be showed (it forms a group of nodes).
     * @param node Node to check
     * @return True if the node can be showed, false otherwise
     */
    boolean canShowNode(Node node);
    
}
