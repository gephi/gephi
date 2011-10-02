/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
 */
package org.gephi.data.attributes.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
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
    private final static long DELAY = 100;
    //Architecture
    private final AbstractAttributeModel model;
    private final List<AttributeListener> listeners;
    private final AtomicReference<Thread> thread = new AtomicReference<Thread>();
    private final LinkedBlockingQueue<AbstractEvent> eventQueue;
    private final Object lock = new Object();
    private final LinkedList<Integer> rateList = new LinkedList<Integer>();
    private double avgRate = 1.0;
    //Flag
    private boolean stop;

    public AttributeEventManager(AbstractAttributeModel model) {
        this.model = model;
        this.eventQueue = new LinkedBlockingQueue<AbstractEvent>();
        this.listeners = Collections.synchronizedList(new ArrayList<AttributeListener>());
    }

    @Override
    public void run() {
        int rate = 0;
        while (!stop) {
            if (rate == (int) avgRate) {
                try {
                    Thread.sleep(DELAY);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                int w = (int) (eventQueue.size() * 0.1f);
                w = Math.max(1, w);
                updateRate(w);
                rate++;
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
            rate++;

            while (eventQueue.isEmpty()) {
                rate = (int) avgRate;
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

    private double updateRate(int n) {
        int windowLength = 10;
        if (rateList.size() == windowLength) {
            Integer oldest = rateList.poll();
            avgRate = ((avgRate * windowLength) - oldest) / (windowLength - 1);
        }
        avgRate = ((avgRate * rateList.size()) + n) / (rateList.size() + 1);
        rateList.add(n);
        return avgRate;
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
