/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.graph.dhns.event;

import org.gephi.graph.api.GraphEvent;
import org.gephi.graph.api.GraphEvent.EventType;
import org.gephi.graph.api.GraphView;

/**
 *
 * @author Mathieu Bastian
 */
public abstract class AbstractEvent<T> {

    private final GraphEvent.EventType eventType;
    private final T data;
    private final GraphView view;

    public AbstractEvent(EventType eventType, GraphView view, T data) {
        this.eventType = eventType;
        this.data = data;
        this.view = view;
    }

    public T getData() {
        return data;
    }

    public EventType getEventType() {
        return eventType;
    }

    public GraphView getView() {
        return view;
    }
}

