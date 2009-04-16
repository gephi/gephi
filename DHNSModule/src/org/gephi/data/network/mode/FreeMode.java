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
package org.gephi.data.network.mode;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.gephi.data.network.Dhns;
import org.gephi.data.network.api.FreeModifier;
import org.gephi.graph.api.Sight;
import org.gephi.data.network.edge.DhnsEdge;
import org.gephi.data.network.edge.EdgeImpl;
import org.gephi.data.network.edge.FreeEdgeProcessing;
import org.gephi.data.network.edge.PreEdge;
import org.gephi.data.network.edge.VirtualEdge;
import org.gephi.data.network.node.NodeImpl;
import org.gephi.data.network.node.PreNode;
import org.gephi.data.network.node.treelist.SightTreeIterator;
import org.gephi.data.network.sight.SightImpl;
import org.gephi.data.network.sight.SightManagerImpl;
import org.gephi.data.network.tree.TreeStructure;
import org.gephi.data.network.utils.avl.PreNodeAVLTree;
import org.gephi.data.network.utils.avl.SightAVLTree.SightAVLIterator;
import org.gephi.datastructure.avl.param.ParamAVLIterator;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;

/**
 *
 * @author Mathieu Bastian
 */
public class FreeMode implements Mode, FreeModifier {

    private Dhns dhns;
    private FreeEdgeProcessing edgeProcessing;
    private TreeStructure treeStructure;
    private SightManagerImpl sightManager;
    //Executor
    ExecutorService executor = Executors.newSingleThreadExecutor();

    public FreeMode(Dhns dhns) {
        this.dhns = dhns;
        this.treeStructure = dhns.getTreeStructure();
        this.sightManager = dhns.getSightManager();
        edgeProcessing = new FreeEdgeProcessing(treeStructure);
    }

    public void init() {
        SightImpl sight = sightManager.getMainSight();

        /*SightTreeIterator enabledNodes = new SightTreeIterator(treeStructure, sight);
        for (; enabledNodes.hasNext();) {
            PreNode n = enabledNodes.next();
            NodeImpl ni = n.getNode();
            ni.setX(n.getPre() * 50);
            ni.setY(n.getPost() * 50);
        }*/
        edgeProcessing.init(sightManager.getMainSight());
        sightManager.getMainSight().getSightCache().reset();
    }

    public void expand(final Node node, final Sight sight) {
        executor.execute(new Runnable() {

            public void run() {
                expandBlock(node, sight);
            }
        });
    }

    public void expandBlock(Node node, Sight sight) {
        dhns.getWriteLock().lock();
        if (node.getLevel() < treeStructure.treeHeight) {
            expand(((NodeImpl) node).getPreNode(), (SightImpl) sight);
            sightManager.updateSight((SightImpl)sight);
        }
        dhns.getWriteLock().unlock();
    }

    public void retract(final Node node, final Sight sight) {
        executor.execute(new Runnable() {

            public void run() {
                retractBlock(node, sight);
            }
        });
    }

    public void retractBlock(Node node, Sight sight) {
        dhns.getWriteLock().lock();
        retract(((NodeImpl) node).getPreNode(), (SightImpl) sight);
        dhns.getWriteLock().unlock();
    }

    public void addNode(final Node node, final Node parent) {
        executor.execute(new Runnable() {

            public void run() {
                addNodeBlock(node, parent);
            }
        });
    }

    public void addNodeBlock(Node node, Node parent) {
        dhns.getWriteLock().lock();
        PreNode parentNode;
        if (parent == null) {
            parentNode = treeStructure.getRoot();
        } else {
            parentNode = ((NodeImpl) parent).getPreNode();
        }
        PreNode preNode = new PreNode(0, 0, 0, parentNode);
        preNode.setNode((NodeImpl) node);
        preNode.addSight(sightManager.getMainSight());

        addNode(preNode);
        dhns.getWriteLock().unlock();
    }

    public void deleteNode(final Node node) {
        executor.execute(new Runnable() {

            public void run() {
                deleteNodeBlock(node);
            }
        });
    }

    public void deleteNodeBlock(Node node) {
        dhns.getWriteLock().lock();
        deleteNode(((NodeImpl) node).getPreNode());
        dhns.getWriteLock().unlock();
    }

    public void addEdge(final Edge edge) {
        executor.execute(new Runnable() {

            public void run() {
                addEdgeBlock(edge);
            }
        });
    }

    public void addEdgeBlock(Edge edge) {
        dhns.getWriteLock().lock();
        EdgeImpl edgeImpl = (EdgeImpl) edge;
        PreEdge preEdge = new PreEdge(edgeImpl.getSource().getPreNode(), edgeImpl.getTarget().getPreNode());
        preEdge.setEdge(edge);

        addEdge(preEdge);
        dhns.getWriteLock().unlock();
    }

    public void deleteEdge(final Edge edge) {
        executor.execute(new Runnable() {

            public void run() {
                deleteEdgeBlock(edge);
            }
        });
    }

    public void deleteEdgeBlock(Edge edge) {
        dhns.getWriteLock().lock();
        EdgeImpl edgeImpl = (EdgeImpl) edge;
        dhns.getWriteLock().unlock();
    }

    //------------------------------------------
    private void expand(PreNode preNode, SightImpl sightImpl) {
        PreNodeAVLTree nodeToReprocess = new PreNodeAVLTree();

        //Enable children
        PreNode child = null;
        for (int i = preNode.pre + 1; i <= preNode.pre + preNode.size;) {
            child = treeStructure.getNodeAt(i);
            child.setEnabled(sightImpl, true);

            i += child.size + 1;
        }

        //Reprocess edge-hosting neighbour
        edgeProcessing.appendEdgeHostingNeighbours(preNode, nodeToReprocess, preNode.pre, sightImpl);
        edgeProcessing.reprocessInducedEdges(nodeToReprocess, preNode, sightImpl);

        //Process induced edges of direct children
        for (int i = preNode.pre + 1; i <= preNode.pre + preNode.size;) {
            child = treeStructure.getNodeAt(i);
            edgeProcessing.processLocalInducedEdges(child, sightImpl);
            i += child.size + 1;
        }

        //Clean current node
        preNode.setEnabled(sightImpl, false);
        edgeProcessing.clearVirtualEdges(preNode, sightImpl);
    }

    private void retract(PreNode parent, SightImpl sight) {
        PreNodeAVLTree nodeToReprocess = new PreNodeAVLTree();

        //Enable node
        parent.setEnabled(sight, true);

        //Disable children
        PreNode child = null;
        for (int i = parent.pre + 1; i <= parent.pre + parent.size;) {
            child = treeStructure.getNodeAt(i);
            child.setEnabled(sight, false);
            edgeProcessing.appendEdgeHostingNeighbours(child, nodeToReprocess, parent.pre, sight);

            i += child.size + 1;
        }

        edgeProcessing.reprocessInducedEdges(nodeToReprocess, parent, sight);
        edgeProcessing.processLocalInducedEdges(parent, sight);

        for (int i = parent.pre + 1; i <= parent.pre + parent.size;) {
            child = treeStructure.getNodeAt(i);
            edgeProcessing.clearVirtualEdges(child, sight);
            i += child.size + 1;
        }
    }

    private void addNode(PreNode node) {
        treeStructure.insertAsChild(node, node.parent);
    }

    private void addEdge(PreEdge edge) {
        PreNode minNode = edge.minNode;
        PreNode maxNode = edge.maxNode;

        //Add physical edges
        minNode.getForwardEdges().add(edge);
        maxNode.getBackwardEdges().add(edge);

        //Virtual edges
        if (minNode.hasSights() && maxNode.hasSights()) {
            //Common sight
            for (SightAVLIterator itr = minNode.getSightIterator(); itr.hasNext();) {
                SightImpl sight = itr.next();
                if (maxNode.isInSight(sight)) {
                    //Get nodes' parent
                    PreNode minParent = treeStructure.getEnabledAncestorOrSelf(minNode, sight);
                    PreNode maxParent = treeStructure.getEnabledAncestorOrSelf(maxNode, sight);

                    if (minParent != null && maxParent != null && minParent != maxParent) {
                        DhnsEdge dhnsEdge = minParent.getVirtualEdge(edge, maxParent.getPre(), sight);
                        if (dhnsEdge != null) {
                            VirtualEdge virtualEdge = (VirtualEdge) dhnsEdge;
                            virtualEdge.addPhysicalEdge(edge);
                        } else {
                            //Create the virtual edge
                            edgeProcessing.createVirtualEdge(edge, minParent, maxParent, sight);
                        }
                    }
                }
            }
        }
    }

    private void deleteNode(PreNode node) {

        ParamAVLIterator<PreEdge> iterator = null;
        int nodeSize = node.getPre() + node.size;

        //Del edges
        for (int i = node.pre; i <= nodeSize; i++) //Children & Self
        {
            PreNode child = treeStructure.getNodeAt(i);

            boolean hasBackwardEdges = child.countBackwardEdges() > 0;
            boolean hasForwardEdges = child.countForwardEdges() > 0;

            if (iterator == null && (hasForwardEdges || hasBackwardEdges)) {
                iterator = new ParamAVLIterator<PreEdge>();
            }

            //Delete Backward edges
            if (hasBackwardEdges) {
                iterator.setNode(child.getBackwardEdges());
                for (; iterator.hasNext();) {
                    PreEdge edge = iterator.next();
                    delEdge(edge);
                }
            }

            //Delete Forward edges
            if (hasForwardEdges) {
                iterator.setNode(child.getForwardEdges());
                for (; iterator.hasNext();) {
                    PreEdge edge = iterator.next();
                    delEdge(edge);
                }
            }
        }

        treeStructure.deleteDescendantAndSelf(node);
    }

    private void delEdge(PreEdge edge) {
        PreNode minNode = edge.minNode;
        PreNode maxNode = edge.maxNode;

        if (minNode.hasSights() && maxNode.hasSights()) {
            //Common sight
            for (SightAVLIterator itr = minNode.getSightIterator(); itr.hasNext();) {
                SightImpl sight = itr.next();
                if (maxNode.isInSight(sight)) {
                    //Nodes share the same sight, they may have virtual edge
                    PreNode minParent = treeStructure.getEnabledAncestorOrSelf(minNode, sight);
                    PreNode maxParent = treeStructure.getEnabledAncestorOrSelf(maxNode, sight);

                    if (minParent != null && maxParent != null && minParent != maxParent) {

                        DhnsEdge dhnsEdge = minParent.getVirtualEdge(edge, maxParent.getPre(), sight);

                        if (dhnsEdge != null) {
                            VirtualEdge virtualEdge = (VirtualEdge) dhnsEdge;
                            virtualEdge.removePhysicalEdge(edge);

                            if (virtualEdge.isEmpty()) {    //No more physical edges represents the virtual edges
                                minParent.removeVirtualEdge(virtualEdge, sight);
                                maxParent.removeVirtualEdge(virtualEdge, sight);
                            }
                        }
                    }
                }
            }
        }

        //Delete physical edges
        minNode.getForwardEdges().remove(edge);
        maxNode.getBackwardEdges().remove(edge);
    }
}
