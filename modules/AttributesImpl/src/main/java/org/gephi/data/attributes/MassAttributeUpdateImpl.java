
package org.gephi.data.attributes;

import java.util.ArrayList;
import org.gephi.graph.api.MassAttributeUpdate;
import org.gephi.data.attributes.event.AbstractEvent;
import org.gephi.data.attributes.event.AttributeEventManager;

public class MassAttributeUpdateImpl implements MassAttributeUpdate {

    private AttributeEventManager sink;
    private ArrayList<AbstractEvent> events;
    private static final int SIZE_LIMIT = 1000000;

    public MassAttributeUpdateImpl(AttributeEventManager s) {
	sink = s;
	events = new ArrayList<AbstractEvent>();
    }

    public void queueEvent(AbstractEvent ev) {
	events.add(ev);
	if(events.size() == MassAttributeUpdateImpl.SIZE_LIMIT)
	    flush();
    }

    public void flush() {
	sink.fireEvents(events);
	events = new ArrayList<AbstractEvent>();
    }

    public void close() {
	if(events.size() > 0)
	    sink.fireEvents(events);
    }

}
