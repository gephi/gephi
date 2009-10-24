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
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphEvent.EventType;
import org.gephi.graph.api.Predicate;
import org.gephi.graph.api.View;
import org.gephi.graph.dhns.core.Dhns;
import org.gephi.graph.dhns.core.GraphStructure;
import org.gephi.graph.dhns.core.GraphVersion;
import org.gephi.graph.dhns.graph.ClusteredGraphImpl;
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

    //Results
    private GraphStructure graphStructure;

    //Update
    private GraphVersion graphVersion;
    private ReentrantLock lock;
    private DispatchThread dispatchThread;
    //Status
    private int nodeVersion = -1;
    private int edgeVersion = -1;

    public ViewImpl(Dhns dhns) {
        this.dhns = dhns;
        this.graphStructure = dhns.getGraphStructure();
        this.graphVersion = dhns.getGraphVersion();
        dispatchThread = new DispatchThread();
        lock = new ReentrantLock();
    }

    public GraphStructure getGraphStructure() {
        return graphStructure;
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

       
    }

  

    public void addPredicate(Predicate predicate) {
        if (!dispatchThread.isAlive()) {
            dispatchThread.start();
        }

        dispatchThread.addPredicate(predicate);
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

    private class DispatchThread extends Thread {

        private final List<Predicate> predicateQueue = new ArrayList<Predicate>();
        private final Object monitor = new Object();
        private boolean waiting = false;

        public DispatchThread() {
            super("View update dispatch thread");
        }

        @Override
        public void run() {
            synchronized (monitor) {
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
