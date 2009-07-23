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
package org.gephi.graph.dhns.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import org.gephi.graph.api.GraphEvent;
import org.gephi.graph.api.GraphEvent.EventType;
import org.gephi.graph.api.GraphListener;

/**
 *
 * @author Mathieu Bastian
 */
public class EventManager {

    private Dhns dhns;
    private List<GraphListener> listeners;
    private boolean dispatchOnOtherThread = true;

    public EventManager(Dhns dhns) {
        this.dhns = dhns;
        listeners = new ArrayList<GraphListener>();
    }

    public void addListener(GraphListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(GraphListener listener) {
        listeners.remove(listener);
    }

    public List<GraphListener> getListeners() {
        return listeners;
    }

    public void fireEvent(EventType type) {
        if (!listeners.isEmpty()) {
            final GraphEvent event = new GraphEventImpl(type);
            if (dispatchOnOtherThread) {
                Executor eventBus = dhns.getController().getEventBus();
                eventBus.execute(new Runnable() {

                    public void run() {
                        dispatchEvent(event);
                    }
                });
            } else {
                dispatchEvent(event);
            }
        }
    }

    private void dispatchEvent(GraphEvent event) {
        for (GraphListener list : listeners) {
            list.graphChanged(event);
        }
    }

    private static class GraphEventImpl implements GraphEvent {

        private EventType type;

        public GraphEventImpl(EventType eventType) {
            this.type = eventType;
        }

        public EventType getEventType() {
            return type;
        }
    }
}
