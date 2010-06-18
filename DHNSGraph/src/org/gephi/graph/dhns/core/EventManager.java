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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphEvent;
import org.gephi.graph.api.GraphListener;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Node;
import org.gephi.graph.dhns.event.AbstractEvent;
import org.gephi.graph.dhns.event.EdgeEvent;
import org.gephi.graph.dhns.event.GeneralEvent;
import org.gephi.graph.dhns.event.GraphEventDataImpl;
import org.gephi.graph.dhns.event.GraphEventImpl;
import org.gephi.graph.dhns.event.NodeEvent;
import org.gephi.graph.dhns.event.ViewEvent;

/**
 *
 * @author Mathieu Bastian
 */
public class EventManager implements Runnable {

    //Const
    private final static long DELAY = 1;
    //Architecture
    private final Dhns dhns;
    private final List<GraphListener> listeners;
    private final AtomicReference<Thread> thread = new AtomicReference<Thread>();
    private final LinkedBlockingQueue<AbstractEvent> eventQueue;
    private final Object lock = new Object();
    //Flag
    private boolean stop;

    public EventManager(Dhns dhns) {
        this.dhns = dhns;
        this.eventQueue = new LinkedBlockingQueue<AbstractEvent>();
        this.listeners = Collections.synchronizedList(new ArrayList<GraphListener>());
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

            AbstractEvent precEvt = null;
            AbstractEvent evt = null;
            while ((evt = eventQueue.peek()) != null) {
                if (precEvt != null) {
                    if ((evt instanceof NodeEvent || evt instanceof EdgeEvent) && precEvt.getEventType().equals(evt.getEventType()) && precEvt.getView()==evt.getView()) {     //Same type
                        if (eventCompress == null) {
                            eventCompress = new ArrayList<Object>();
                            eventCompress.add(precEvt.getData());
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
                GraphEvent event = createEvent(precEvt, eventCompress);
                for (GraphListener l : listeners.toArray(new GraphListener[0])) {
                    l.graphChanged(event);
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

    private GraphEvent createEvent(AbstractEvent event, List<Object> compress) {
        final GraphEventDataImpl eventData = (event instanceof GeneralEvent) ? null : new GraphEventDataImpl();
        final GraphEventImpl graphEventImpl = new GraphEventImpl(event.getView(), event.getEventType(), eventData);
        if (event instanceof NodeEvent) {
            Node[] nodes;
            if (compress != null) {
                nodes = compress.toArray(new Node[0]);
            } else {
                nodes = new Node[]{(Node) event.getData()};
            }
            switch (event.getEventType()) {
                case ADD_NODES:
                    eventData.setAddedNodes(nodes);
                    break;
                case REMOVE_NODES:
                    eventData.setRemovedNodes(nodes);
                    break;
                case EXPAND:
                    eventData.setExpandedNodes(nodes);
                    break;
                case RETRACT:
                    eventData.setRetractedNodes(nodes);
                    break;
                case MOVE_NODES:
                    eventData.setMovedNodes(nodes);
                    break;
            }
        } else if (event instanceof EdgeEvent) {
            Edge[] edges;
            if (compress != null) {
                edges = compress.toArray(new Edge[0]);
            } else {
                edges = new Edge[]{(Edge) event.getData()};
            }
            switch (event.getEventType()) {
                case ADD_EDGES:
                    eventData.setAddedEdges(edges);
                    break;
                case REMOVE_EDGES:
                    eventData.setRemovedEdges(edges);
                    break;
            }
        } else if (event instanceof ViewEvent) {
            eventData.setView((GraphView) event.getData());
        }
        return graphEventImpl;
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
        t.setName("graph-event-bus");
        if (this.thread.compareAndSet(null, t)) {
            t.start();
        }
    }

    public boolean isRunning() {
        return thread.get() != null;
    }

    public void addGraphListener(GraphListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeGraphListener(GraphListener listener) {
        listeners.remove(listener);
    }
}
