/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
