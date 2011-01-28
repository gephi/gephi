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
package org.gephi.data.attributes.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;
import org.gephi.data.attributes.AbstractAttributeModel;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeEvent;
import org.gephi.data.attributes.api.AttributeListener;
import org.gephi.data.attributes.api.AttributeValue;

/**
 *
 * @author Mathieu Bastian
 */
public class AttributeEventManager implements Runnable {

    //Const
    private final static long DELAY = 1;
    //Architecture
    private final AbstractAttributeModel model;
    private final List<AttributeListener> listeners;
    private final AtomicReference<Thread> thread = new AtomicReference<Thread>();
    private final LinkedBlockingQueue<AbstractEvent> eventQueue;
    private final Object lock = new Object();
    //Flag
    private boolean stop;

    public AttributeEventManager(AbstractAttributeModel model) {
        this.model = model;
        this.eventQueue = new LinkedBlockingQueue<AbstractEvent>();
        this.listeners = Collections.synchronizedList(new ArrayList<AttributeListener>());
    }

    @Override
    public void run() {
        while (!stop) {
            try {
                Thread.sleep(DELAY);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            List<Object> eventCompress = null;
            List<Object> eventCompressObjects = null;

            AbstractEvent precEvt = null;
            AbstractEvent evt = null;
            while ((evt = eventQueue.peek()) != null) {
                if (precEvt != null) {
                    if ((evt instanceof ValueEvent || evt instanceof ColumnEvent) && precEvt.getEventType().equals(evt.getEventType()) && precEvt.getAttributeTable() == evt.getAttributeTable()) {     //Same type
                        if (eventCompress == null) {
                            eventCompress = new ArrayList<Object>();
                            eventCompress.add(precEvt.getData());
                        }
                        if (evt instanceof ValueEvent) {
                            if (eventCompressObjects == null) {
                                eventCompressObjects = new ArrayList<Object>();
                                eventCompressObjects.add(((ValueEvent) precEvt).getObject());
                            }
                            eventCompressObjects.add(((ValueEvent) evt).getObject());
                        }

                        eventCompress.add(evt.getData());
                    } else {
                        break;
                    }
                }
                eventQueue.poll();
                precEvt = evt;
            }

            if (precEvt != null) {
                AttributeEvent event = createEvent(precEvt, eventCompress, eventCompressObjects);
                for (AttributeListener l : listeners.toArray(new AttributeListener[0])) {
                    l.attributesChanged(event);
                }
            }

            while (eventQueue.isEmpty()) {
                try {
                    synchronized (lock) {
                        lock.wait();
                    }
                } catch (InterruptedException e) {
                }
            }
        }
    }

    private AttributeEvent createEvent(AbstractEvent event, List<Object> compress, List<Object> compressObjects) {
        final AttributeEventDataImpl eventData = new AttributeEventDataImpl();
        final AttributeEventImpl attributeEvent = new AttributeEventImpl(event.getEventType(), event.getAttributeTable(), eventData);
        if (event instanceof ValueEvent) {
            AttributeValue[] values;
            Object[] objects;
            if (compress != null) {
                values = compress.toArray(new AttributeValue[0]);
                objects = compressObjects.toArray();
            } else {
                values = new AttributeValue[]{(AttributeValue) event.getData()};
                objects = new Object[]{((ValueEvent) event).getObject()};
            }
            eventData.setValues(values);
            eventData.setObjects(objects);
        } else if (event instanceof ColumnEvent) {
            AttributeColumn[] columns;
            if (compress != null) {
                columns = compress.toArray(new AttributeColumn[0]);
            } else {
                columns = new AttributeColumn[]{(AttributeColumn) event.getData()};
            }
            eventData.setColumns(columns);
        }
        return attributeEvent;
    }

    public void stop(boolean stop) {
        this.stop = stop;
    }

    public void fireEvent(AbstractEvent event) {
        eventQueue.add(event);
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    public void start() {
        Thread t = new Thread(this);
        t.setDaemon(true);
        t.setName("attribute-event-bus");
        if (this.thread.compareAndSet(null, t)) {
            t.start();
        }
    }

    public boolean isRunning() {
        return thread.get() != null;
    }

    public void addAttributeListener(AttributeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeAttributeListener(AttributeListener listener) {
        listeners.remove(listener);
    }
}
