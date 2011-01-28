/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
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
