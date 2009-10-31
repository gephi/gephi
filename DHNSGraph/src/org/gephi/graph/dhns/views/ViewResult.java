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

import org.gephi.graph.api.EdgePredicate;
import org.gephi.graph.api.NodePredicate;
import org.gephi.graph.api.Predicate;
import org.gephi.graph.dhns.core.GraphStructure;
import org.gephi.graph.dhns.edge.AbstractEdge;
import org.gephi.graph.dhns.edge.iterators.AbstractEdgeIterator;
import org.gephi.graph.dhns.edge.iterators.EdgeIterator;
import org.gephi.graph.dhns.filter.FlatClusteredViewPredicate;
import org.gephi.graph.dhns.filter.Tautology;
import org.gephi.graph.dhns.node.AbstractNode;
import org.gephi.graph.dhns.node.iterators.AbstractNodeIterator;
import org.gephi.graph.dhns.node.iterators.TreeIterator;

/**
 *
 * @author Mathieu Bastian
 */
public class ViewResult {

    private GraphStructure graphStructure;
    private boolean undirected = false;

    //Result
    private ViewNodeTree hierarchyLayerNodeTree;
    private ViewEdgeTree hierarchyLayerEdgeTree;
    private ViewNodeTree clusteredLayerNodeTree;
    private ViewEdgeTree clusteredLayerEdgeTree;
    private Predicate hierarchyLayerNodeTreePredicate = Tautology.instance;
    private Predicate hierarchyLayerEdgeTreePredicate = Tautology.instance;
    private Predicate clusteredLayerNodeTreePredicate = Tautology.instance;
    private Predicate clusteredLayerEdgeTreePredicate = Tautology.instance;

    public ViewResult(GraphStructure graphStructure, boolean undirected) {
        this.graphStructure = graphStructure;
        this.undirected = undirected;
    }

    public void filter(Predicate predicate) {
        if (hierarchyLayerNodeTree == null) {
            //Create the ViewNodeTree
            NodePredicate nodePredicate = (NodePredicate) Tautology.instance;
            if (predicate instanceof NodePredicate) {
                nodePredicate = (NodePredicate) predicate;
            }
            hierarchyLayerNodeTree = createViewNodeTree(nodePredicate);
            Predicate<AbstractEdge> edgePredicate = Tautology.instance;
            if (predicate instanceof EdgePredicate) {
                edgePredicate = predicate;
            }
            hierarchyLayerEdgeTree = createViewEdgeTree(edgePredicate, hierarchyLayerNodeTree);
            hierarchyLayerNodeTreePredicate = new HierarchyLayerNodePredicate();
            hierarchyLayerEdgeTreePredicate = new HierarchyLayerEdgePredicate();
        } else {
            if (predicate instanceof NodePredicate) {
                filterViewNodeTree(hierarchyLayerNodeTree, (NodePredicate) predicate);
            } else {
                filterViewEdgeTree(hierarchyLayerEdgeTree, (EdgePredicate) predicate);
            }
        }
    }

    public void postProcess() {
        filterClustered();
    }

    private void filterClustered() {
        FlatClusteredViewPredicate clusteredPredicate = new FlatClusteredViewPredicate();
        if (clusteredPredicate instanceof FlatClusteredViewPredicate) {
            clusteredLayerNodeTree = createClusteredFlatViewNodeTree(hierarchyLayerNodeTreePredicate);
        } else {
            clusteredLayerNodeTree = createClusteredViewNodeTree(hierarchyLayerNodeTreePredicate);
        }
        clusteredLayerEdgeTree = createViewEdgeTree(hierarchyLayerEdgeTreePredicate, clusteredLayerNodeTree);
        clusteredLayerNodeTreePredicate = new ClusteredLayerNodePredicate();
        clusteredLayerEdgeTreePredicate = new ClusteredLayerEdgePredicate();
    }

    /*private void setDefaultIterator() {
    nodeIterator = new TreeIterator(graphStructure.getStructure(), false, Tautology.instance);
    edgeIterator = new EdgeIterator(graphStructure.getStructure(), nodeIterator, undirected, Tautology.instance, Tautology.instance);
    }*/
    private ViewNodeTree createViewNodeTree(NodePredicate predicate) {
        ViewNodeTree viewNodeTree = new ViewNodeTree();
        TreeIterator treeIterator = new TreeIterator(graphStructure.getStructure(), false, (Predicate) predicate);

        for (; treeIterator.hasNext();) {
            AbstractNode node = treeIterator.next();
            viewNodeTree.add(node);
        }
        return viewNodeTree;
    }

    private ViewEdgeTree createViewEdgeTree(Predicate<AbstractEdge> predicate, ViewNodeTree viewNodeTree) {
        ViewEdgeTree viewEdgeTree = new ViewEdgeTree();
        EdgeIterator edgeIterator = new EdgeIterator(graphStructure.getStructure(), viewNodeTree.iterator(), undirected, Tautology.instance, predicate);

        for (; edgeIterator.hasNext();) {
            AbstractEdge edge = edgeIterator.next();
            if (viewNodeTree.contains(edge.getTarget())) {
                viewEdgeTree.add(edge);
            }
        }
        return viewEdgeTree;
    }

    private void filterViewNodeTree(ViewNodeTree viewNodeTree, NodePredicate predicate) {
        for (AbstractNodeIterator itr = viewNodeTree.iterator(); itr.hasNext();) {
            AbstractNode node = itr.next();
            if (!predicate.evaluate(node)) {
                itr.remove();
            }
        }
    }

    private void filterViewEdgeTree(ViewEdgeTree viewEdgeTree, EdgePredicate predicate) {
        for (AbstractEdgeIterator itr = viewEdgeTree.iterator(); itr.hasNext();) {
            AbstractEdge edge = itr.next();
            if (!predicate.evaluate(edge)) {
                itr.remove();
            }
        }
    }

    private ViewNodeTree createClusteredFlatViewNodeTree(Predicate predicate) {
        ViewNodeTree viewNodeTree = new ViewNodeTree();
        TreeIterator treeIterator = new TreeIterator(graphStructure.getStructure(), true, predicate);
        for (; treeIterator.hasNext();) {
            AbstractNode node = treeIterator.next();
            viewNodeTree.add(node);
        }
        return viewNodeTree;
    }

    private ViewNodeTree createClusteredViewNodeTree(Predicate predicate) {
        ViewNodeTree viewNodeTree = new ViewNodeTree();
        AbstractNodeIterator itr;
        if (hierarchyLayerNodeTree == null) {
            itr = new TreeIterator(graphStructure.getStructure(), false, (Predicate) predicate);
            for (; itr.hasNext();) {
                AbstractNode node = itr.next();
                viewNodeTree.add(node);
            }
        } else {
            itr = hierarchyLayerNodeTree.iterator();
            for (; itr.hasNext();) {
                AbstractNode node = itr.next();
                if (predicate.evaluate(node)) {
                    viewNodeTree.add(node);
                }
            }
        }

        return viewNodeTree;
    }

    //Getters
    public AbstractNodeIterator getHierarchyLayerNodeIterator() {
        if (hierarchyLayerNodeTree != null) {
            return hierarchyLayerNodeTree.iterator();
        }
        return new TreeIterator(graphStructure.getStructure(), false, Tautology.instance);
    }

    public AbstractNodeIterator getClusteredLayerNodeIterator() {
        return clusteredLayerNodeTree.iterator();
    }

    public AbstractEdgeIterator getHierarchyLayerEdgeIterator() {
        if (hierarchyLayerEdgeTree != null) {
            return hierarchyLayerEdgeTree.iterator();
        }
        return new EdgeIterator(graphStructure.getStructure(), new TreeIterator(graphStructure.getStructure(), false, Tautology.instance), undirected, Tautology.instance, Tautology.instance);
    }

    public AbstractEdgeIterator getClusteredLayerEdgeIterator() {
        return clusteredLayerEdgeTree.iterator();
    }

    public Predicate<AbstractNode> getHierarchyLayerNodePredicate() {
        return hierarchyLayerNodeTreePredicate;
    }

    public Predicate<AbstractNode> getClusteredLayerNodePredicate() {
        return clusteredLayerNodeTreePredicate;
    }

    public Predicate<AbstractEdge> getHierarchyLayerEdgePredicate() {
        return hierarchyLayerEdgeTreePredicate;
    }

    public Predicate<AbstractEdge> getClusteredLayeEdgePredicate() {
        return clusteredLayerEdgeTreePredicate;
    }

    public int getClusteredNodesCount() {
        return clusteredLayerNodeTree.getCount();
    }

    public int getClusteredEdgesCount() {
        return clusteredLayerEdgeTree.getCount();
    }

    //Inner Predicates
    private class HierarchyLayerNodePredicate implements Predicate<AbstractNode> {

        public boolean evaluate(AbstractNode element) {
            return hierarchyLayerNodeTree.contains(element);
        }
    }

    private class ClusteredLayerNodePredicate implements Predicate<AbstractNode> {

        public boolean evaluate(AbstractNode element) {
            return clusteredLayerNodeTree.contains(element);
        }
    }

    private class HierarchyLayerEdgePredicate implements Predicate<AbstractEdge> {

        public boolean evaluate(AbstractEdge element) {
            return hierarchyLayerEdgeTree.contains(element);
        }
    }

    private class ClusteredLayerEdgePredicate implements Predicate<AbstractEdge> {

        public boolean evaluate(AbstractEdge element) {
            return clusteredLayerEdgeTree.contains(element);
        }
    }
}
