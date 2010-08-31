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
     * Graph event interface, that {@link GraphListener} receives when the graph is
     * modified. Each event is specific to a particular view, which can be get
     * from the <code>getSource()</code> method.
     * <p>
     * <table>
     * <tr><td><b>- ADD_NODES:</b></td><td>Add nodes to the graph</td></tr>
     * <tr><td><b>- REMOVE_NODES:</b></td><td>Remove nodes from the graph, with their edges</td></tr>
     * <tr><td><b>- ADD_EDGES:</b></td><td>Add edges to the graph</td></tr>
     * <tr><td><b>- REMOVE_EDGES:</b></td><td>Remove edges from the graph</td></tr>
     * <tr><td><b>- MOVE_NODE:</b></td><td>Move nodes on the hierarchy, parent node is changed</td></tr>
     * <tr><td><b>- VISIBLE_VIEW:</b></td><td>Current visible view is changed</td></tr>
     * <tr><td><b>- NEW_VIEW:</b></td><td>A new view is created</td></tr>
     * <tr><td><b>- DESTROY_VIEW:</b></td><td>A view is destroyed</td></tr>
     * <tr><td><b>- CLEAR_NODES:</b></td><td>Clear all nodes in the graph, and all edges</td></tr>
     * <tr><td><b>- CLEAR_EDGES:</b></td>Clear all edges in the graph<td></td></tr>
     * <tr><td><b>- EXPAND:</b></td><td>Expand nodes in the hierarchy</td></tr>
     * <tr><td><b>- RETRACT:</b></td><td>Retract nodes in the hierarchy</td></tr>
     * <tr><td><b>- META_EDGES_UPDATE</b></td><td>Meta-Edges are updated</td></tr></table>
     *
     * @author Mathieu Bastian
     * @see GraphView
     */
public interface GraphEvent {

    /**
     * <table>
     * <tr><td><b>- ADD_NODES:</b></td><td>Add nodes to the graph</td></tr>
     * <tr><td><b>- REMOVE_NODES:</b></td><td>Remove nodes from the graph, with their edges</td></tr>
     * <tr><td><b>- ADD_EDGES:</b></td><td>Add edges to the graph</td></tr>
     * <tr><td><b>- REMOVE_EDGES:</b></td><td>Remove edges from the graph</td></tr>
     * <tr><td><b>- MOVE_NODE:</b></td><td>Move nodes on the hierarchy, parent node is changed</td></tr>
     * <tr><td><b>- VISIBLE_VIEW:</b></td><td>Current visible view is changed</td></tr>
     * <tr><td><b>- NEW_VIEW:</b></td><td>A new view is created</td></tr>
     * <tr><td><b>- DESTROY_VIEW:</b></td><td>A view is destroyed</td></tr>
     * <tr><td><b>- CLEAR_NODES:</b></td><td>Clear all nodes in the graph, and all edges</td></tr>
     * <tr><td><b>- CLEAR_EDGES:</b></td>Clear all edges in the graph<td></td></tr>
     * <tr><td><b>- EXPAND:</b></td><td>Expand nodes in the hierarchy</td></tr>
     * <tr><td><b>- RETRACT:</b></td><td>Retract nodes in the hierarchy</td></tr>
     * <tr><td><b>- META_EDGES_UPDATE</b></td><td>Meta-Edges are updated</td></tr></table>
     */
    public enum EventType {

        ADD_NODES,
        REMOVE_NODES,
        ADD_EDGES,
        REMOVE_EDGES,
        MOVE_NODES,
        VISIBLE_VIEW,
        NEW_VIEW,
        DESTROY_VIEW,
        CLEAR_NODES,
        CLEAR_EDGES,
        EXPAND,
        RETRACT,
        META_EDGES_UPDATE
    };

    /**
     * Returns the type of event.
     * @return      the type of event, can't be <code>null</code>
     */
    public EventType getEventType();

    /**
     * Returns the data associated to this event.
     * @return      the graph event data
     */
    public GraphEventData getData();

    /**
     * Returns the view this event is triggered.
     * @return      the source of the vent
     */
    public GraphView getSource();

    /**
     * Returns <code>true</code> if this event is one of these in parameters.
     * @param type  the event types that are to be compared with this event
     * @return      <code>true</code> if this event is <code>type</code>,
     *              <code>false</code> otherwise
     */
    public boolean is(EventType... type);
}
