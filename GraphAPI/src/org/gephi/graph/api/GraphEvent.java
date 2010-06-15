/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
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
package org.gephi.graph.api;

/**
 * Graph event interface, for listening to global changes.
 * <ul>
 * <li><b>NODES_UPDATED:</b> Nodes have been added or removed</li>
 * <li><b>EDGES_UPDATED:</b> Edges have been added or removed</li>
 * <li><b>NODES_AND_EDGES_UPDATED:</b> Nodes and edges have been added or removed</li>
 * <li><b>VIEWS_UPDATED:</b> The current visible view changed</li>
 * </ul>
 * @author Mathieu Bastian
 * @see GraphListener
 */
public interface GraphEvent {

    /**
     * <ul>
     * <li><b>NODES_UPDATED:</b> Nodes have been added or removed</li>
     * <li><b>EDGES_UPDATED:</b> Edges have been added or removed</li>
     * <li><b>NODES_AND_EDGES_UPDATED:</b> Nodes and edges have been added or removed</li>
     * <li><b>VIEWS_UPDATED:</b> The current visible view changed</li>
     * </ul>
     */
    public enum EventType {

        NODES_ADDED,
        NODES_REMOVED,
        EDGES_ADDED,
        EDGES_REMOVED,
        MOVE_NODE,
        VISIBLE_VIEW,
        NEW_VIEW,
        DESTROY_VIEW,
        CLEAR_NODES,
        CLEAR_EDGES,
        EXPAND,
        RETRACT,
        META_EDGES_UPDATE
    };

    public EventType getEventType();

    public GraphEventData getData();

    public GraphView getSource();

    public boolean is(EventType... type);
}
