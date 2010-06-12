/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.neo4j.attributes;

import org.gephi.data.attributes.api.AttributeEvent;

/**
 *
 * @author Mathieu Bastian
 */
public final class AttributeEventImpl implements AttributeEvent {

    private final EventType type;
    private final Object source;
    private final Object data;

    public AttributeEventImpl(EventType type, Object source, Object data) {
        this.type = type;
        this.source = source;
        this.data = data;
    }

    public EventType getEventType() {
        return type;
    }

    public Object getSource() {
        return source;
    }

    public Object getData() {
        return data;
    }
}
