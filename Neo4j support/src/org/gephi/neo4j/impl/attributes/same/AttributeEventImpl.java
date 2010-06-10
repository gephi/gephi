package org.gephi.neo4j.impl.attributes.same;


import org.gephi.data.attributes.api.AttributeEvent;


public class AttributeEventImpl implements AttributeEvent {

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
