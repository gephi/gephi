/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.graph.api;

/**
 * Wrap graph event elements, like added nodes.
 *
 * @author Mathieu Bastian
 * @see GraphEvent
 */
public interface GraphEventData {

    /**
     * Returns nodes added to the graph.
     * @return added nodes, or <code>null</code> if the event is not <code>ADD_NODES</code>
     */
    public Node[] addedNodes();

    /**
     * Returns nodes removed from the graph.
     * @return removed nodes, or <code>null</code> if the event is not <code>REMOVE_NODES</code>
     */
    public Node[] removedNodes();

    /**
     * Returns edges added to the graph.
     * @return added edges, or <code>null</code> if the event is not <code>ADD_EDGES</code>
     */
    public Edge[] addedEdges();

    /**
     * Returns edges removed from the graph.
     * @return removed edges, or <code>null</code> if the event is not <code>REMOVE_EDGES</code>
     */
    public Edge[] removedEdges();

    /**
     * Returns nodes expanded in the graph hierarchy.
     * @return expanded nodes, or <code>null</code> if the event is not <code>EXPAND</code>
     */
    public Node[] expandedNodes();

    /**
     * Returns nodes retracted in the graph hierarchy.
     * @return retracted nodes, or <code>null</code> if the event is not <code>RETRACT</code>
     */
    public Node[] retractedNodes();

    /**
     * Returns nodes moved in the graph hierarchy, their parent node has changed.
     * @return moved nodes, or <code>null</code> if the event is not <code>MOVE_NODES</code>
     */
    public Node[] movedNodes();

    /**
     * Returns the new view created in the model.
     * @return the new view, or <code>null</code> if the event is not <code>NEW_VIEW</code>
     */
    public GraphView newView();

    /**
     * Returns the view destroyed in the model.
     * @return the destroyed view, or <code>null</code> if the event is not <code>DESTROY_VIEW</code>
     */
    public GraphView destroyView();

    /**
     * Returns the current visible view.
     * @return the visible view, or <code>null</code> if the event is not <code>VISIBLE_VIEW</code>
     */
    public GraphView visibleView();
}
