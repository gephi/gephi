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

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import org.gephi.graph.api.Edge;
import org.gephi.utils.collection.avl.ParamAVLIterator;
import org.gephi.graph.api.GraphEvent.EventType;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeData;
import org.gephi.graph.dhns.edge.AbstractEdge;
import org.gephi.graph.dhns.event.AbstractEvent;
import org.gephi.graph.dhns.event.ViewEvent;
import org.gephi.graph.dhns.node.AbstractNode;
import org.gephi.graph.dhns.node.iterators.TreeListIterator;
import org.gephi.graph.dhns.utils.avl.AbstractEdgeTree;
import org.gephi.graph.dhns.utils.avl.AbstractNodeTree;

/**
 *
 * @author Mathieu Bastian
 */
public class GraphStructure {

    private final AtomicInteger viewId = new AtomicInteger(1);
    private final Dhns dhns;
    private final GraphViewImpl mainView;
    private final Queue<GraphViewImpl> views;
    private final AbstractNodeTree nodeDictionnary;
    private final AbstractEdgeTree edgeDictionnary;
    private final Map<String, NodeData> nodeIDDictionnary;   //Temporary, waiting for attributes indexing
    private final Map<String, Edge> edgeIDDIctionnary;
    private GraphViewImpl visibleView;
    //Destroy
    private final Object lock = new Object();
    private final ConcurrentLinkedQueue<GraphViewImpl> destroyQueue;

    public GraphStructure(Dhns dhns) {
        this.dhns = dhns;
        nodeDictionnary = new AbstractNodeTree();
        edgeDictionnary = new AbstractEdgeTree();
        nodeIDDictionnary = new HashMap<String, NodeData>();
        edgeIDDIctionnary = new HashMap<String, Edge>();
        views = new ConcurrentLinkedQueue<GraphViewImpl>();

        //Main view
        mainView = new GraphViewImpl(dhns, 0);
        views.add(mainView);
        visibleView = mainView;

        //Destructor
        destroyQueue = new ConcurrentLinkedQueue<GraphViewImpl>();
        ViewDestructorThread viewDestructorThread = new ViewDestructorThread(this);
        viewDestructorThread.start();
    }

    public GraphViewImpl[] getViews() {
        return views.toArray(new GraphViewImpl[0]);
    }

    public GraphViewImpl getMainView() {
        return mainView;
    }

    public GraphViewImpl createView(int viewId) {   //used by deserializer
        this.viewId.set(Math.max(viewId + 1, this.viewId.get()));
        return new GraphViewImpl(dhns, viewId);
    }

    public GraphViewImpl getNewView() {
        return copyView(mainView);
    }

    public GraphViewImpl copyView(GraphViewImpl view) {
        GraphViewImpl viewCopy = new GraphViewImpl(dhns, viewId.getAndIncrement());
        TreeStructure newStructure = viewCopy.getStructure();
        dhns.getReadLock().lock();

        for (TreeListIterator itr = new TreeListIterator(view.getStructure().getTree(), 1); itr.hasNext();) {
            AbstractNode node = itr.next();
            AbstractNode nodeCopy = new AbstractNode(node.getNodeData(), viewCopy.getViewId());
            nodeCopy.setEnabled(node.isEnabled());
            nodeCopy.setEnabledInDegree(node.getEnabledInDegree());
            nodeCopy.setEnabledOutDegree(node.getEnabledOutDegree());
            nodeCopy.setEnabledMutualDegree(node.getEnabledMutualDegree());
            AbstractNode parentCopy = node.parent != null ? newStructure.getNodeAt(node.parent.getPre()) : null;
            newStructure.insertAsChild(nodeCopy, parentCopy);
        }

        //Edges
        ParamAVLIterator<AbstractEdge> edgeIterator = new ParamAVLIterator<AbstractEdge>();
        for (TreeListIterator itr = new TreeListIterator(view.getStructure().getTree(), 1); itr.hasNext();) {
            AbstractNode node = itr.next();
            if (!node.getEdgesOutTree().isEmpty()) {
                for (edgeIterator.setNode(node.getEdgesOutTree()); edgeIterator.hasNext();) {
                    AbstractEdge edge = edgeIterator.next();
                    AbstractNode sourceCopy = newStructure.getNodeAt(edge.getSource().getPre());
                    AbstractNode targetCopy = newStructure.getNodeAt(edge.getTarget().getPre());
                    sourceCopy.getEdgesOutTree().add(edge);
                    targetCopy.getEdgesInTree().add(edge);
                }
            }
        }
        viewCopy.setNodesEnabled(view.getNodesEnabled());
        viewCopy.setEdgesCountTotal(view.getEdgesCountTotal());
        viewCopy.setEdgesCountEnabled(view.getEdgesCountEnabled());
        viewCopy.setMutualEdgesTotal(view.getMutualEdgesTotal());
        viewCopy.setMutualEdgesEnabled(view.getMutualEdgesEnabled());
        views.add(viewCopy);
        dhns.getEventManager().fireEvent(new ViewEvent(EventType.NEW_VIEW, viewCopy));
        dhns.getReadLock().unlock();
        return viewCopy;
    }

    public void destroyView(final GraphViewImpl view) {
        if (views.contains(view)) {
            destroyQueue.add(view);
            synchronized (this.lock) {
                lock.notify();
            }
        }
    }

    public void addToDictionnary(AbstractNode node) {
        nodeDictionnary.add(node);
        if (node.getNodeData().getId() != null) {
            nodeIDDictionnary.put(node.getNodeData().getId(), node.getNodeData());
        }
    }

    public void removeFromDictionnary(AbstractNode node) {
        nodeDictionnary.remove(node);
        if (node.getNodeData().getId() != null) {
            nodeIDDictionnary.remove(node.getNodeData().getId());
        }
    }

    public void addToDictionnary(AbstractEdge edge) {
        edgeDictionnary.add(edge);
        if (edge.getEdgeData().getId() != null) {
            edgeIDDIctionnary.put(edge.getEdgeData().getId(), edge);
        }
    }

    public void removeFromDictionnary(AbstractEdge edge) {
        edgeDictionnary.remove(edge);
        if (edge.getEdgeData().getId() != null) {
            edgeIDDIctionnary.remove(edge.getEdgeData().getId());
        }
    }

    public void clearNodeDictionnary() {
        nodeDictionnary.clear();
        nodeIDDictionnary.clear();
    }

    public void clearEdgeDictionnary() {
        edgeDictionnary.clear();
        edgeIDDIctionnary.clear();
    }

    public AbstractEdge getEdgeFromDictionnary(int id) {
        return edgeDictionnary.get(id);
    }

    public AbstractNode getNodeFromDictionnary(int id) {
        return nodeDictionnary.get(id);
    }

    public Map<String, NodeData> getNodeIDDictionnary() {
        return nodeIDDictionnary;
    }

    public Map<String, Edge> getEdgeIDDIctionnary() {
        return edgeIDDIctionnary;
    }

    public GraphViewImpl getVisibleView() {
        return visibleView;
    }

    public void setVisibleView(GraphViewImpl visibleView) {
        if (this.visibleView == visibleView) {
            return;
        }
        if (visibleView == null) {
            this.visibleView = mainView;
        } else {
            this.visibleView = visibleView;
        }
        dhns.getEventManager().fireEvent(new ViewEvent(EventType.VISIBLE_VIEW, this.visibleView));
    }

    private static class ViewDestructorThread extends Thread {

        private final WeakReference<GraphStructure> structureReference;
        private final int STD_TIMER = 300;
        private final int UNDESTRO_TIMER = 5000;
        private boolean running = true;

        public ViewDestructorThread(GraphStructure graphStructure) {
            super("DHNS View Destructor");
            setDaemon(true);
            structureReference = new WeakReference<GraphStructure>(graphStructure);
        }

        @Override
        public void run() {
            GraphStructure structure = null;
            while (running && (structure = structureReference.get()) != null) {
                while (structure.destroyQueue.isEmpty()) {
                    try {
                        synchronized (structure.lock) {
                            structure.lock.wait();
                        }
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
                boolean undestroyableViews = false;
                for (GraphViewImpl v : structure.destroyQueue.toArray(new GraphViewImpl[0])) {
                    if (!v.hasGraphReference()) {
                        destroyView(structure, v);
                        structure.destroyQueue.remove(v);
                    } else {
                        undestroyableViews = true;
                    }
                }
                try {
                    synchronized (structure.lock) {
                        structure.lock.wait(undestroyableViews ? UNDESTRO_TIMER : STD_TIMER);
                    }
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }

        private void destroyView(GraphStructure structure, GraphViewImpl view) {
            //System.out.println("Destroy view " + view.getViewId());
            structure.dhns.getWriteLock().lock();
            for (TreeListIterator itr = new TreeListIterator(structure.mainView.getStructure().getTree(), 1); itr.hasNext();) {
                AbstractNode node = itr.next();
                node.getNodeData().getNodes().remove(view.getViewId());
            }
            structure.views.remove(view);
            //System.out.println("Destroy view finished");
            structure.dhns.getEventManager().fireEvent(new ViewEvent(EventType.DESTROY_VIEW, view));
            structure.dhns.getWriteLock().unlock();
            if (structure.visibleView == view) {
                structure.visibleView = structure.mainView;
                structure.dhns.getEventManager().fireEvent(new ViewEvent(EventType.VISIBLE_VIEW, structure.mainView));
            }
        }
    }
}
