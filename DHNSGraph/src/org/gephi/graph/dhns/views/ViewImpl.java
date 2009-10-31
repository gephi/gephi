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
package org.gephi.graph.dhns.views;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import org.gephi.graph.api.GraphEvent.EventType;
import org.gephi.graph.api.Predicate;
import org.gephi.graph.api.View;
import org.gephi.graph.dhns.core.Dhns;
import org.gephi.graph.dhns.core.GraphVersion;
import org.gephi.graph.dhns.edge.AbstractEdge;
import org.gephi.graph.dhns.edge.iterators.AbstractEdgeIterator;
import org.gephi.graph.dhns.node.AbstractNode;
import org.gephi.graph.dhns.node.iterators.AbstractNodeIterator;
import org.openide.util.Exceptions;

/**
 *
 * @author Mathieu Bastian
 */
public class ViewImpl implements View {

    //Settings
    private HierarchyFiltering hierarchyFiltering = HierarchyFiltering.FLAT;
    private Dhns dhns;
    private final List<Predicate> predicates = new ArrayList<Predicate>();

    //Update
    private GraphVersion graphVersion;
    private ReentrantLock lock;
    private DispatchThread dispatchThread;
    private boolean blocking = true;

    //Status
    private int nodeVersion = -1;
    private int edgeVersion = -1;

    //Result
    private ViewResult result;

    public ViewImpl(Dhns dhns) {
        this.dhns = dhns;
        this.graphVersion = dhns.getGraphVersion();
        dispatchThread = new DispatchThread();
        lock = new ReentrantLock();
        result = new ViewResult(dhns.getGraphStructure(), false);
    }

    public void checkUpdate() {
        if (lock.isHeldByCurrentThread()) {
            return;
        }

        int nv = graphVersion.getNodeVersion();
        int ev = graphVersion.getEdgeVersion();
        if (nodeVersion != nv || edgeVersion != ev) {
            lock.lock();
            update();
            nodeVersion = nv;
            edgeVersion = ev;
            lock.unlock();
        }
    }

    private void update() {
        for (Predicate p : predicates) {
            result.filter(p);
        }
        result.postProcess();
    }

    public void addPredicate(Predicate predicate) {
        if (blocking) {
            lock.lock();
            predicates.add(predicate);
            lock.unlock();
        } else {
            if (!dispatchThread.isAlive()) {
                dispatchThread.start();
            }
            dispatchThread.addPredicate(predicate);
        }
    }

    public void removePredicate(Predicate predicate) {
        dispatchThread.removePredicate(predicate);
    }

    public void updatePredicate(Predicate oldPredicate, Predicate newPredicate) {
        dispatchThread.updatePredicate(new Predicate[]{oldPredicate}, new Predicate[]{newPredicate});
    }

    public void updatePredicate(Predicate[] oldPredicate, Predicate[] newPredicate) {
        dispatchThread.updatePredicate(oldPredicate, newPredicate);
    }

    public void predicateParametersUpdates() {
        dhns.getGraphVersion().incNodeAndEdgeVersion();
        dhns.getEventManager().fireEvent(EventType.NODES_AND_EDGES_UPDATED);
    }

    public void setHierarchyFiltering(HierarchyFiltering hierarchyFiltering) {
        this.hierarchyFiltering = hierarchyFiltering;
    }

    //Iterators
    public AbstractNodeIterator getHierarchyLayerNodeIterator() {
        return result.getHierarchyLayerNodeIterator();
    }

    public AbstractNodeIterator getClusteredLayerNodeIterator() {
        return result.getClusteredLayerNodeIterator();
    }

    public AbstractEdgeIterator getHierarchyLayerEdgeIterator() {
        return result.getHierarchyLayerEdgeIterator();
    }

    public AbstractEdgeIterator getClusteredLayerEdgeIterator() {
        return result.getClusteredLayerEdgeIterator();
    }

    //Predicates
    public Predicate<AbstractNode> getHierarchyLayerNodePredicate() {
        return result.getHierarchyLayerNodePredicate();
    }

    public Predicate<AbstractEdge> getHierarchyLayerEdgePredicate() {
        return result.getHierarchyLayerEdgePredicate();
    }

    public Predicate<AbstractNode> getClusteredLayerNodePredicate() {
        return result.getClusteredLayerNodePredicate();
    }

    public Predicate<AbstractEdge> getClusteredLayerEdgePredicate() {
        return result.getClusteredLayeEdgePredicate();
    }

    //Count
    public int getClusteredNodesCount() {
        return result.getClusteredNodesCount();
    }

    public int getClusteredEdgesCount() {
        return result.getClusteredEdgesCount();
    }

    private class DispatchThread extends Thread {

        private final List<Predicate> predicateQueue = new ArrayList<Predicate>();
        private final Object monitor = new Object();
        private boolean waiting = false;
        private boolean started = false;

        public DispatchThread() {
            super("View update dispatch thread");
        }

        @Override
        public void run() {
            synchronized (monitor) {
                started = true;
                while (true) {
                    try {
                        monitor.wait();
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }

                    //Action
                    waiting = true;
                    dhns.getWriteLock().lock();
                    synchronized (predicateQueue) {
                        predicates.clear();
                        predicates.addAll(predicateQueue);
                    }
                    dhns.getWriteLock().unlock();
                    waiting = false;
                }
            }
        }

        public void addPredicate(Predicate predicate) {
            synchronized (predicateQueue) {
                predicateQueue.add(predicate);
            }
            while (!started) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            synchronized (monitor) {
                monitor.notify();
            }
        }

        public void removePredicate(Predicate predicate) {
            synchronized (predicateQueue) {
                predicateQueue.remove(predicate);
            }
            synchronized (monitor) {
                monitor.notify();
            }
        }

        public void updatePredicate(Predicate[] oldPredicate, Predicate[] newPredicate) {
            synchronized (predicateQueue) {
                for (int i = 0; i < oldPredicate.length; i++) {
                    predicateQueue.set(predicateQueue.indexOf(oldPredicate[i]), newPredicate[i]);
                }
            }
            if (waiting) {
                return;
            }
            synchronized (monitor) {
                monitor.notify();
            }
        }
    }
}
