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
import org.gephi.datastructure.avl.param.ParamAVLIterator;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphEvent.EventType;
import org.gephi.graph.api.NodePredicate;
import org.gephi.graph.api.Predicate;
import org.gephi.graph.api.TopologicalPredicate;
import org.gephi.graph.api.View;
import org.gephi.graph.dhns.core.Dhns;
import org.gephi.graph.dhns.core.GraphVersion;
import org.gephi.graph.dhns.edge.AbstractEdge;
import org.gephi.graph.dhns.edge.MetaEdgeBuilder;
import org.gephi.graph.dhns.edge.MetaEdgeImpl;
import org.gephi.graph.dhns.edge.iterators.AbstractEdgeIterator;
import org.gephi.graph.dhns.filter.ChildrenClusteredViewPredicate;
import org.gephi.graph.dhns.filter.FlatClusteredViewPredicate;
import org.gephi.graph.dhns.filter.FullClusteredViewPredicate;
import org.gephi.graph.dhns.filter.HierarchyFilteringPredicate;
import org.gephi.graph.dhns.graph.ClusteredGraphImpl;
import org.gephi.graph.dhns.node.AbstractNode;
import org.gephi.graph.dhns.node.iterators.AbstractNodeIterator;
import org.gephi.graph.dhns.node.iterators.TreeIterator;
import org.gephi.graph.dhns.proposition.Tautology;
import org.gephi.graph.dhns.utils.avl.AbstractEdgeTree;
import org.gephi.graph.dhns.utils.avl.AbstractNodeTree;
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
    private AbstractNodeTree nodeTree;
    private AbstractEdgeTree edgeTree;
    private AbstractEdgeTree metaEdgeTree;
    private NodeInFilterResultPredicate nodePredicate;
    private EdgeInFilterResultPredicate edgePredicate;
    private MetaEdgeInFilterResultPredicate metaEdgePredicate;
    //Update
    private GraphVersion graphVersion;
    private ReentrantLock lock;
    private DispatchThread dispatchThread;
    private ClusteredGraphImpl graph;
    //Status
    protected boolean active = false;
    private int nodeVersion = -1;
    private int edgeVersion = -1;

    public ViewImpl(Dhns dhns) {
        this.dhns = dhns;
        this.graphVersion = dhns.getGraphVersion();
        dispatchThread = new DispatchThread();
        lock = new ReentrantLock();
        this.nodeTree = new AbstractNodeTree();
        this.edgeTree = new AbstractEdgeTree();
        this.metaEdgeTree = new AbstractEdgeTree();
        nodePredicate = new NodeInFilterResultPredicate();
        edgePredicate = new EdgeInFilterResultPredicate();
        metaEdgePredicate = new MetaEdgeInFilterResultPredicate();
    }

    protected void checkUpdate() {
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

        //Initial
        active = false;
        AbstractEdgeTree initialEdges = new AbstractEdgeTree();
        AbstractNodeTree initialNodes = new AbstractNodeTree();
        AbstractEdgeTree initialMetaEdges = new AbstractEdgeTree();
        for (TreeIterator treeIterator = new TreeIterator(dhns.getTreeStructure(), Tautology.instance); treeIterator.hasNext();) {
            initialNodes.add(treeIterator.next());
        }
        for (Edge edge : graph.getEdges()) {
            initialEdges.add((AbstractEdge) edge);
        }
        for (Edge metaEdge : graph.getMetaEdges()) {
            initialMetaEdges.add((AbstractEdge) metaEdge);
        }

        nodeTree = initialNodes;
        edgeTree = initialEdges;
        metaEdgeTree = initialMetaEdges;
        active = true;

        if (!predicates.isEmpty() && predicates.get(predicates.size() - 1) instanceof HierarchyFilteringPredicate) {
            predicates.remove(predicates.size() - 1);
        }
        switch (hierarchyFiltering) {
            case FLAT:
                predicates.add(new FlatClusteredViewPredicate());
                break;
            case CHILDREN:
                predicates.add(new ChildrenClusteredViewPredicate());
                break;
            case FULL:
                predicates.add(new FullClusteredViewPredicate());
                break;
        }
        AbstractEdgeTree beforeHierarchyFilteringEdges = null;

        for (Predicate p : predicates) {
            AbstractEdgeTree pEdgeTree = edgeTree;
            AbstractNodeTree pNodeTree = nodeTree;

            if (p instanceof HierarchyFilteringPredicate) {
                beforeHierarchyFilteringEdges = edgeTree;
            }

            if (p instanceof NodePredicate) {
                pNodeTree = new AbstractNodeTree();
                for (AbstractNodeIterator itr = nodeIterator(); itr.hasNext();) {
                    AbstractNode node = itr.next();
                    boolean val;
                    if (p instanceof TopologicalPredicate) {
                        val = ((TopologicalPredicate) p).evaluate(node, graph);
                    } else {
                        val = p.evaluate(node);
                    }
                    if (val) {
                        pNodeTree.add(node);
                    }
                }

                //Clean edges
                pEdgeTree = new AbstractEdgeTree();
                for (AbstractEdgeIterator itr = edgeTree.iterator(); itr.hasNext();) {
                    AbstractEdge e = itr.next();
                    if (pNodeTree.contains(e.getSource()) && pNodeTree.contains(e.getTarget())) {
                        pEdgeTree.add(e);
                    }
                }
            } else {
                pEdgeTree = new AbstractEdgeTree();
                for (AbstractEdgeIterator itr = edgeIterator(); itr.hasNext();) {
                    AbstractEdge edge = itr.next();
                    boolean val;
                    if (p instanceof TopologicalPredicate) {
                        val = ((TopologicalPredicate) p).evaluate(edge, graph);
                    } else {
                        val = p.evaluate(edge);
                    }
                    if (val) {
                        pEdgeTree.add(edge);
                    }
                }
            }

            nodeTree = pNodeTree;
            edgeTree = pEdgeTree;
        }

        //Apply hierarchical clustering view
        ParamAVLIterator<AbstractEdge> innerEdgeIterator = new ParamAVLIterator<AbstractEdge>();
        MetaEdgeBuilder metaEdgeBuilder = dhns.getSettingsManager().getMetaEdgeBuilder();       //Update weight
        for (AbstractEdgeIterator itr = metaEdgeTree.iterator(); itr.hasNext();) {
            AbstractEdge e = itr.next();
            if (!nodeTree.contains(e.getSource()) || !nodeTree.contains(e.getTarget())) {
                itr.remove();
            } else {
                MetaEdgeImpl metaEdge = (MetaEdgeImpl) e;
                boolean atLeastOneInnerEdgeIsNotFiltered = false;
                for (innerEdgeIterator.setNode(metaEdge.getEdges()); innerEdgeIterator.hasNext();) {
                    if (beforeHierarchyFilteringEdges.contains(innerEdgeIterator.next())) {
                        atLeastOneInnerEdgeIsNotFiltered = true;
                        break;
                    }
                }
                if (!atLeastOneInnerEdgeIsNotFiltered) {
                    itr.remove();
                }
            }
        }
    }

    public boolean isActive() {
        return active;
    }

    public boolean evaluateNode(AbstractNode node) {
        if (active) {
            checkUpdate();
            return nodePredicate.evaluate(node);
        }
        return true;
    }

    public boolean evaluateEdge(AbstractEdge edge) {
        if (active) {
            checkUpdate();
            return edgePredicate.evaluate(edge);
        }
        return true;
    }

    public int getNodeCount() {
        checkUpdate();
        return nodeTree.getCount();
    }

    public int getEdgeCount() {
        checkUpdate();
        return edgeTree.getCount();
    }

    public Predicate<AbstractNode> getNodePredicate() {
        if (active) {
            checkUpdate();
            return nodePredicate;
        }
        return Tautology.instance;
    }

    public Predicate<AbstractEdge> getEdgePredicate() {
        if (active) {
            checkUpdate();
            return edgePredicate;
        }
        return Tautology.instance;
    }

    public Predicate<AbstractEdge> getMetaEdgePredicate() {
        if (active) {
            checkUpdate();
            return metaEdgePredicate;
        }
        return Tautology.instance;
    }

    public AbstractNodeIterator nodeIterator() {
        checkUpdate();
        return nodeTree.iterator();
    }

    public AbstractEdgeIterator edgeIterator() {
        checkUpdate();
        return edgeTree.iterator();
    }

    public AbstractEdgeIterator metaEdgeIterator() {
        checkUpdate();
        return metaEdgeTree.iterator();
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

    public void setGraph(Graph graph) {
        this.graph = (ClusteredGraphImpl) graph;
        active = dhns.isHierarchical();
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

    private class NodeInFilterResultPredicate implements Predicate<AbstractNode> {

        public boolean evaluate(AbstractNode element) {
            return nodeTree.contains(element);
        }
    }

    private class EdgeInFilterResultPredicate implements Predicate<AbstractEdge> {

        public boolean evaluate(AbstractEdge element) {
            return edgeTree.contains(element);
        }
    }

    private class MetaEdgeInFilterResultPredicate implements Predicate<AbstractEdge> {

        public boolean evaluate(AbstractEdge element) {
            return metaEdgeTree.contains(element);
        }
    }
}
