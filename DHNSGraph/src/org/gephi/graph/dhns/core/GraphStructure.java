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
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.gephi.utils.collection.avl.ParamAVLIterator;
import org.gephi.graph.api.GraphEvent.EventType;
import org.gephi.graph.dhns.edge.AbstractEdge;
import org.gephi.graph.dhns.graph.HierarchicalGraphImpl;
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
    private final List<GraphViewImpl> views;
    private final AbstractNodeTree nodeDictionnary;
    private final AbstractEdgeTree edgeDictionnary;
    private GraphViewImpl visibleView;

    public GraphStructure(Dhns dhns) {
        this.dhns = dhns;
        nodeDictionnary = new AbstractNodeTree();
        edgeDictionnary = new AbstractEdgeTree();
        views = new ArrayList<GraphViewImpl>();

        //Main view
        mainView = new GraphViewImpl(dhns, 0);
        views.add(mainView);
        visibleView = mainView;
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
        GraphViewImpl view = new GraphViewImpl(dhns, viewId.getAndIncrement());
        TreeStructure newStructure = view.getStructure();
        dhns.getReadLock().lock();

        for (TreeListIterator itr = new TreeListIterator(mainView.getStructure().getTree(), 1); itr.hasNext();) {
            AbstractNode node = itr.next();
            AbstractNode nodeCopy = new AbstractNode(node.getNodeData(), view.getViewId());
            nodeCopy.setEnabled(node.isEnabled());
            AbstractNode parentCopy = node.parent != null ? newStructure.getNodeAt(node.parent.getPre()) : null;
            newStructure.insertAsChild(nodeCopy, parentCopy);
        }

        //Edges
        ParamAVLIterator<AbstractEdge> edgeIterator = new ParamAVLIterator<AbstractEdge>();
        for (TreeListIterator itr = new TreeListIterator(mainView.getStructure().getTree(), 1); itr.hasNext();) {
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
        dhns.getReadLock().unlock();
        views.add(view);
        return view;
    }

    public void destroyView(GraphViewImpl view) {
        if (views.contains(view)) {
            dhns.getReadLock().lock();
            for (TreeListIterator itr = new TreeListIterator(mainView.getStructure().getTree(), 1); itr.hasNext();) {
                AbstractNode node = itr.next();
                node.getNodeData().getNodes().remove(view.getViewId());
            }
            dhns.getReadLock().unlock();
            views.remove(view);
            if (this.visibleView == view) {
                this.visibleView = mainView;
                dhns.getEventManager().fireEvent(EventType.VIEWS_UPDATED);
            }
        }
    }

    public AbstractNodeTree getNodeDictionnary() {
        return nodeDictionnary;
    }

    public AbstractEdgeTree getEdgeDictionnary() {
        return edgeDictionnary;
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
        dhns.getEventManager().fireEvent(EventType.VIEWS_UPDATED);
    }
}
