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

import org.gephi.datastructure.avl.param.ParamAVLIterator;
import org.gephi.graph.api.EdgePredicate;
import org.gephi.graph.api.NodePredicate;
import org.gephi.graph.api.Predicate;
import org.gephi.graph.dhns.core.Dhns;
import org.gephi.graph.dhns.core.GraphStructure;
import org.gephi.graph.dhns.core.SettingsManager;
import org.gephi.graph.dhns.edge.AbstractEdge;
import org.gephi.graph.dhns.edge.MetaEdgeImpl;
import org.gephi.graph.dhns.edge.iterators.AbstractEdgeIterator;
import org.gephi.graph.dhns.edge.iterators.EdgeIterator;
import org.gephi.graph.dhns.filter.FlatClusteredViewPredicate;
import org.gephi.graph.dhns.filter.Tautology;
import org.gephi.graph.dhns.node.AbstractNode;
import org.gephi.graph.dhns.node.iterators.AbstractNodeIterator;
import org.gephi.graph.dhns.node.iterators.TreeIterator;
import org.gephi.graph.dhns.utils.avl.AbstractEdgeTree;
import org.gephi.graph.dhns.utils.avl.MetaEdgeTree;

/**
 *
 * @author Mathieu Bastian
 */
public class ViewResult {

    private Dhns dhns;
    private GraphStructure graphStructure;
    private boolean undirected = false;
    //Result
    private ViewNodeTree hierarchyLayerNodeTree;
    private ViewEdgeTree hierarchyLayerEdgeTree;
    private ViewNodeTree clusteredLayerNodeTree;
    private ViewEdgeTree clusteredLayerEdgeTree;
    private AbstractEdgeTree metaEdgeTree;
    private Predicate hierarchyLayerNodeTreePredicate = Tautology.instance;
    private Predicate hierarchyLayerEdgeTreePredicate = Tautology.instance;
    private Predicate clusteredLayerNodeTreePredicate = Tautology.instance;
    private Predicate clusteredLayerEdgeTreePredicate = Tautology.instance;

    public ViewResult(Dhns dhns, GraphStructure graphStructure, boolean undirected) {
        this.dhns = dhns;
        this.graphStructure = graphStructure;
        this.undirected = undirected;
    }

    public void filter(Predicate predicate) {
        if (hierarchyLayerNodeTree == null) {
            //Create the ViewNodeTree
            Predicate<AbstractNode> nodePredicate = Tautology.instance;
            if (predicate instanceof NodePredicate) {
                nodePredicate = predicate;
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
        filterInterIntraEdges();
        computeViewMetaEdges();
    }

    private ViewNodeTree createViewNodeTree(Predicate<AbstractNode> predicate) {
        ViewNodeTree viewNodeTree = new ViewNodeTree();
        TreeIterator treeIterator = new TreeIterator(graphStructure.getStructure(), false, predicate);

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
            ViewNodeTree.ViewNodeAVLNode treeNodeSource = viewNodeTree.getNode(edge.getSource().getNumber());
            ViewNodeTree.ViewNodeAVLNode treeNodeTarget = viewNodeTree.getNode(edge.getTarget().getNumber());
            if (treeNodeTarget != null) {
                viewEdgeTree.add(edge);
                treeNodeSource.incOutDegree();
                treeNodeTarget.incInDegree();
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

    //Post Processes
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

    private void filterInterIntraEdges() {
        SettingsManager settings = dhns.getSettingsManager();
        if (!settings.isInterClusterEdges() || !settings.isIntraClusterEdges()) {
            for (AbstractEdgeIterator itr = clusteredLayerEdgeTree.iterator(); itr.hasNext();) {
                AbstractEdge edge = itr.next();
                AbstractNode source = edge.getSource();
                AbstractNode target = edge.getTarget();
                if (!settings.isInterClusterEdges() && source.parent == target.parent) {
                    itr.remove();
                } else if (!settings.isIntraClusterEdges() && source.parent != target.parent) {
                    itr.remove();
                }
            }
        }
    }

    private void computeViewMetaEdges() {
        metaEdgeTree = new AbstractEdgeTree();
        if (!dhns.getSettingsManager().isAutoMetaEdgeCreation()) {
            return;
        }
        ParamAVLIterator<AbstractEdge> edgeItr = new ParamAVLIterator<AbstractEdge>();
        TreeIterator treeIterator = new TreeIterator(graphStructure.getStructure(), true, clusteredLayerNodeTreePredicate);
        for (; treeIterator.hasNext();) {
            AbstractNode currentNode = treeIterator.next();
            if (currentNode.size == 0) {
                //Leaf
                if (!currentNode.getEdgesOutTree().isEmpty()) {
                    for (edgeItr.setNode(currentNode.getEdgesOutTree()); edgeItr.hasNext();) {
                        AbstractEdge edge = edgeItr.next();
                        if (!edge.isSelfLoop()) {
                            AbstractNode[] enabledAncestors = graphStructure.getStructure().getEnabledAncestorsOrSelf(edge.getTarget());
                            if (enabledAncestors != null) {
                                for (int j = 0; j < enabledAncestors.length; j++) {
                                    AbstractNode targetNode = enabledAncestors[j];
                                    if (targetNode != edge.getTarget()) {
                                        createViewMetaEdge(edge.getSource(), targetNode, edge);
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                //Cluster
                int clusterEnd = currentNode.getPre() + currentNode.size;
                for (int i = currentNode.pre; i <= clusterEnd; i++) {
                    AbstractNode desc = graphStructure.getStructure().getNodeAt(i);
                    if (desc.getEdgesOutTree().getCount() > 0) {
                        for (edgeItr.setNode(desc.getEdgesOutTree()); edgeItr.hasNext();) {
                            AbstractEdge edge = edgeItr.next();
                            if (!edge.isSelfLoop()) {
                                int targetPre = edge.getTarget().getPre();
                                if (!(targetPre <= clusterEnd && targetPre >= currentNode.pre)) {
                                    AbstractNode[] enabledAncestors = graphStructure.getStructure().getEnabledAncestorsOrSelf(edge.getTarget());
                                    if (enabledAncestors != null) {
                                        for (int j = 0; j < enabledAncestors.length; j++) {
                                            AbstractNode targetNode = enabledAncestors[j];
                                            if (!(currentNode == edge.getSource() && targetNode == edge.getTarget())) {
                                                createViewMetaEdge(currentNode, targetNode, edge);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void createViewMetaEdge(AbstractNode source, AbstractNode target, AbstractEdge edge) {
        if (edge.getSource() == source && edge.getTarget() == target) {
            return;
        }
        if (source == target) {
            return;
        }

        ViewNodeTree.ViewNodeAVLNode treeNodeSource = clusteredLayerNodeTree.getNode(source.getNumber());
        ViewNodeTree.ViewNodeAVLNode treeNodeTarget = clusteredLayerNodeTree.getNode(target.getNumber());
        MetaEdgeImpl metaEdge;
        if (treeNodeSource.metaEdgesOutTree != null) {
            metaEdge = treeNodeSource.metaEdgesOutTree.getItem(target.getNumber());
            if (metaEdge == null) {
                if (treeNodeTarget.metaEdgesInTree == null) {
                    treeNodeTarget.metaEdgesInTree = new MetaEdgeTree(target);
                }
                metaEdge = new MetaEdgeImpl(dhns.getIdGen().newEdgeId(), source, target);
                metaEdgeTree.add(metaEdge);
                treeNodeSource.metaEdgesOutTree.add(metaEdge);
                treeNodeTarget.metaEdgesInTree.add(metaEdge);
            }
        } else {
            treeNodeSource.metaEdgesOutTree = new MetaEdgeTree(source);
            metaEdge = new MetaEdgeImpl(dhns.getIdGen().newEdgeId(), source, target);
            metaEdgeTree.add(metaEdge);
            treeNodeSource.metaEdgesOutTree.add(metaEdge);
            if (treeNodeTarget.metaEdgesInTree == null) {
                treeNodeTarget.metaEdgesInTree = new MetaEdgeTree(target);
            }
            treeNodeTarget.metaEdgesInTree.add(metaEdge);
        }
        metaEdge.addEdge(edge);
        dhns.getSettingsManager().getMetaEdgeBuilder().pushEdge(edge, metaEdge);
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

    public int getClusteredDegree(AbstractNode node) {
        ViewNodeTree.ViewNodeAVLNode n = clusteredLayerNodeTree.getNode(node.getNumber());
        int res = 0;
        if (n != null) {
            res = n.indegree + n.outdegree;
        }
        return res;
    }

    public int getClusteredOutDegree(AbstractNode node) {
        ViewNodeTree.ViewNodeAVLNode n = clusteredLayerNodeTree.getNode(node.getNumber());
        int res = 0;
        if (n != null) {
            res = n.outdegree;
        }
        return res;
    }

    public int getClusteredInDegree(AbstractNode node) {
        ViewNodeTree.ViewNodeAVLNode n = clusteredLayerNodeTree.getNode(node.getNumber());
        int res = 0;
        if (n != null) {
            res = n.indegree;
        }
        return res;
    }

    public ViewNodeTree.ViewNodeAVLNode getClusteredNode(AbstractNode node) {
        return clusteredLayerNodeTree.getNode(node.getNumber());
    }

    public AbstractEdgeIterator getMetaEdgeIterator() {
        return metaEdgeTree.iterator();
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
