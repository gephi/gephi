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
package org.gephi.graph.dhns.event;

import org.gephi.graph.api.GraphEvent;
import org.gephi.graph.api.GraphEvent.EventType;
import org.gephi.graph.api.GraphEventData;
import org.gephi.graph.api.GraphView;

/**
 *
 * @author Mathieu Bastian
 */
public final class GraphEventImpl implements GraphEvent {

    private final EventType type;
    private final GraphEventDataImpl data;
    private final GraphView view;

    public GraphEventImpl(GraphView view, EventType type, GraphEventDataImpl data) {
        this.type = type;
        this.data = data;
        this.view = view;
    }

    public EventType getEventType() {
        return type;
    }

    public GraphEventData getData() {
        return data;
    }

    public GraphView getSource() {
        return view;
    }

    public boolean is(EventType... type) {
        for(EventType t : type) {
            if(t.equals(this.type)) {
                return true;
            }
        }
        return false;
    }
}
