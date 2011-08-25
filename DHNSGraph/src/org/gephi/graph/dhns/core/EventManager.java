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
package org.gephi.graph.dhns.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
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
    private final static long DELAY = 100;
    //Architecture
    private final List<GraphListener> listeners;
    private final AtomicReference<Thread> thread = new AtomicReference<Thread>();
    private final LinkedBlockingQueue<AbstractEvent> eventQueue;
    private final Object lock = new Object();
    private final LinkedList<Integer> rateList = new LinkedList<Integer>();
    private double avgRate = 1.0;
    //Flag
    private boolean stop;

    public EventManager(Dhns dhns) {
        this.eventQueue = new LinkedBlockingQueue<AbstractEvent>();
        this.listeners = Collections.synchronizedList(new ArrayList<GraphListener>());
        rateList.add(1);
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

            AbstractEvent precEvt = null;
            AbstractEvent evt = null;
            while ((evt = eventQueue.peek()) != null) {
                if (precEvt != null) {
                    if ((evt instanceof NodeEvent || evt instanceof EdgeEvent) && precEvt.getEventType().equals(evt.getEventType()) && precEvt.getView() == evt.getView()) {     //Same type
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

    private GraphEvent createEvent(AbstractEvent event, List<Object> compress) {
        final GraphEventDataImpl eventData = (event instanceof GeneralEvent) ? null : new GraphEventDataImpl();
        final GraphEventImpl graphEventImpl = new GraphEventImpl(event.getView(), event.getEventType(), eventData);
        if (event instanceof NodeEvent || event instanceof EdgeEvent) {
            List<Node> nodes = null;
            List<Edge> edges = null;
            if (compress != null) {
                for (Object o : compress) {
                    if (o instanceof Node) {
                        if (nodes == null) {
                            nodes = new ArrayList<Node>();
                        }
                        nodes.add((Node) o);
                    } else {
                        if (edges == null) {
                            edges = new ArrayList<Edge>();
                        }
                        edges.add((Edge) o);
                    }
                }
                switch (event.getEventType()) {
                    case ADD_NODES_AND_EDGES:
                        if (nodes != null) {
                            eventData.setAddedNodes(nodes.toArray(new Node[0]));
                        }
                        if (edges != null) {
                            eventData.setAddedEdges(edges.toArray(new Edge[0]));
                        }
                        break;
                    case REMOVE_NODES_AND_EDGES:
                        if (nodes != null) {
                            eventData.setRemovedNodes(nodes.toArray(new Node[0]));
                        }
                        if (edges != null) {
                            eventData.setRemovedEdges(edges.toArray(new Edge[0]));
                        }
                        break;
                    case EXPAND:
                        eventData.setExpandedNodes(nodes.toArray(new Node[0]));
                        break;
                    case RETRACT:
                        eventData.setRetractedNodes(nodes.toArray(new Node[0]));
                        break;
                    case MOVE_NODES:
                        eventData.setMovedNodes(nodes.toArray(new Node[0]));
                        break;
                }
            } else {
                switch (event.getEventType()) {
                    case ADD_NODES_AND_EDGES:
                        if (event instanceof NodeEvent) {
                            eventData.setAddedNodes(new Node[]{(Node) event.getData()});
                        } else {
                            eventData.setAddedEdges(new Edge[]{(Edge) event.getData()});
                        }
                        break;
                    case REMOVE_NODES_AND_EDGES:
                        if (event instanceof NodeEvent) {
                            eventData.setRemovedNodes(new Node[]{(Node) event.getData()});
                        } else {
                            eventData.setRemovedEdges(new Edge[]{(Edge) event.getData()});
                        }
                        break;
                    case EXPAND:
                        eventData.setExpandedNodes(new Node[]{(Node) event.getData()});
                        break;
                    case RETRACT:
                        eventData.setRetractedNodes(new Node[]{(Node) event.getData()});
                        break;
                    case MOVE_NODES:
                        eventData.setMovedNodes(new Node[]{(Node) event.getData()});
                        break;
                }
            }
        } else if (event instanceof ViewEvent) {
            eventData.setView((GraphView) event.getData());
        }
        return graphEventImpl;
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
