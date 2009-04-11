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
package org.gephi.data.network.edge;

import org.gephi.data.network.tree.TreeStructure;
import org.gephi.data.network.utils.avl.PreNodeAVLTree;
import org.gephi.data.network.edge.PreEdge.EdgeType;
import org.gephi.data.network.node.PreNode;
import org.gephi.data.network.node.treelist.SingleTreeIterator;
import org.gephi.data.network.sight.Sight;
import org.gephi.datastructure.avl.param.ParamAVLIterator;

/**
 *
 * @author Mathieu Bastian
 */
public class EdgeProcessing {

    private TreeStructure treeStructure;
    private ParamAVLIterator<PreEdge> preEdgeIterator;

    public EdgeProcessing(TreeStructure treeStructure) {
        this.treeStructure = treeStructure;
        preEdgeIterator = new ParamAVLIterator<PreEdge>();
    }

    public void clearVirtualEdges(Sight sight) {
        SingleTreeIterator enabledNodes = new SingleTreeIterator(treeStructure, sight);
        while (enabledNodes.hasNext()) {
            PreNode currentNode = enabledNodes.next();
            currentNode.getVirtualEdgesIN(sight).clear();
            currentNode.getVirtualEdgesOUT(sight).clear();
        }
    }

    public void processInducedEdges(Sight sight) {

        SingleTreeIterator enabledNodes = new SingleTreeIterator(treeStructure, sight);
        while (enabledNodes.hasNext()) {
            PreNode currentNode = enabledNodes.next();
            processInducedEdges(currentNode, sight);
        }

    }

    public void processInducedEdges(PreNode currentNode, Sight sight) {
        if (currentNode.isLeaf()) {
            if (currentNode.countForwardEdges() > 0) {
                //Leaf
                preEdgeIterator.setNode(currentNode.getForwardEdges());
                while (preEdgeIterator.hasNext()) {
                    PreEdge edge = preEdgeIterator.next();
                    PreNode edgeNode = edge.maxNode;

                    if (edgeNode.pre > currentNode.pre && edgeNode.isInSight(sight)) {
                        if (edgeNode.isEnabled(sight)) {
                            //Link between two leafs
                            //System.out.println("Lien entre 2 feuilles. "+currentNode.pre+" "+edge.edgeType+" "+edgeNode.pre);
                            createEdge(edge, currentNode, edgeNode, sight);
                        } else {
                            PreNode clusterAncestor = treeStructure.getEnabledAncestorOrSelf(edgeNode, sight);
                            if (clusterAncestor != null && !checkDouble(clusterAncestor, currentNode.pre, edge)) {
                                //The linked node is a cluster and has never been visited from this leaf

                                //Link between a leaf and a cluster
                                //System.out.println("Lien entre une feuille et un cluster. "+currentNode.pre+" "+edge.edgeType+" "+clusterAncestor.pre);
                                VirtualEdge newEdge = createEdge(edge, currentNode, clusterAncestor, sight);

                                //Set the trace
                                clusterAncestor.preTrace = currentNode.pre;
                                clusterAncestor.lastEdge = newEdge;
                            }
                        }
                    }
                }
            }
        } else {
            //Cluster
            int clusterEnd = currentNode.pre + currentNode.size;
            for (int i = currentNode.pre + 1; i <= clusterEnd; i++) {
                PreNode desc = treeStructure.getNodeAt(i);
                if (desc.isLeaf() && desc.countForwardEdges() > 0) {
                    preEdgeIterator.setNode(desc.getForwardEdges());
                    while (preEdgeIterator.hasNext()) {
                        PreEdge edge = preEdgeIterator.next();
                        PreNode edgeNode = edge.maxNode;

                        if (edgeNode.pre > clusterEnd && edgeNode.isInSight(sight) && !checkDouble(edgeNode, currentNode.pre, edge)) {
                            if (edgeNode.isEnabled(sight)) {
                                //Link between two leafs
                                //System.out.println("Lien entre 1 cluster et une feuille "+currentNode.pre+" "+edge.edgeType+" "+edgeNode.pre);
                                VirtualEdge newEdge = createEdge(edge, currentNode, edgeNode, sight);

                                //Set the trace
                                edgeNode.preTrace = currentNode.pre;
                                edgeNode.lastEdge = newEdge;
                            } else {
                                PreNode clusterAncestor = treeStructure.getEnabledAncestorOrSelf(edgeNode, sight);
                                if (clusterAncestor != null && !checkDouble(clusterAncestor, currentNode.pre, edge)) {
                                    //The linked node is a cluster and has never been visited from this leaf

                                    //Link between a leaf and a cluster
                                    //System.out.println("Lien entre un cluster et un cluster. "+currentNode.pre+" "+edge.edgeType+" "+clusterAncestor.pre);
                                    VirtualEdge newEdge = createEdge(edge, currentNode, clusterAncestor, sight);

                                    //Set the trace
                                    edgeNode.preTrace = currentNode.pre;
                                    clusterAncestor.preTrace = currentNode.pre;
                                    edgeNode.lastEdge = newEdge;
                                    clusterAncestor.lastEdge = newEdge;
                                }
                            }
                        }
                    }
                }

                desc.reinitTrace();
            }
        }

        //Reinit trace
        currentNode.reinitTrace();
    }

    public void processLocalInducedEdges(PreNode currentNode, Sight sight) {
        if (currentNode.isLeaf()) {
            if (currentNode.countForwardEdges() > 0) {
                //Leaf
                preEdgeIterator.setNode(currentNode.getForwardEdges());
                while (preEdgeIterator.hasNext()) {
                    PreEdge edge = preEdgeIterator.next();
                    PreNode edgeNode = edge.maxNode;

                    if (edgeNode.pre > currentNode.pre && edgeNode.isInSight(sight)) {
                        if (edgeNode.isEnabled(sight)) {
                            //Link between two leafs
                            //System.out.println("Lien entre 2 feuilles. "+currentNode.pre+" "+edge.edgeType+" "+edgeNode.pre);
                            createEdge(edge, currentNode, edgeNode, sight);
                        } else {
                            PreNode clusterAncestor = treeStructure.getEnabledAncestorOrSelf(edgeNode, sight);
                            if (clusterAncestor != null && !checkDouble(clusterAncestor, currentNode.pre, edge)) {
                                //The linked node is a cluster and has never been visited from this leaf

                                //Link between a leaf and a cluster
                                //System.out.println("Lien entre une feuille et un cluster. "+currentNode.pre+" "+edge.edgeType+" "+clusterAncestor.pre);
                                VirtualEdge newEdge = createEdge(edge, currentNode, clusterAncestor, sight);

                                //Set the trace
                                clusterAncestor.preTrace = currentNode.pre;
                                clusterAncestor.lastEdge = newEdge;
                            }
                        }
                    }
                }
            }
        } else {
            //Cluster
            int clusterEnd = currentNode.pre + currentNode.size;
            for (int i = currentNode.pre + 1; i <= clusterEnd; i++) {
                PreNode desc = treeStructure.getNodeAt(i);
                if (desc.isLeaf() && desc.countForwardEdges() > 0) {
                    preEdgeIterator.setNode(desc.getForwardEdges());
                    while (preEdgeIterator.hasNext()) {
                        PreEdge edge = preEdgeIterator.next();
                        PreNode edgeNode = edge.maxNode;

                        if (edgeNode.pre > clusterEnd && edgeNode.isInSight(sight) && !checkDouble(edgeNode, currentNode.pre, edge)) {
                            if (edgeNode.isEnabled(sight)) {
                                //Link between two leafs
                                //System.out.println("Lien entre 1 cluster et une feuille"+currentNode.pre+" "+edge.edgeType+" "+edgeNode.pre);
                                VirtualEdge newEdge = createEdge(edge, currentNode, edgeNode, sight);

                                //Set the trace
                                edgeNode.preTrace = currentNode.pre;
                                edgeNode.lastEdge = newEdge;
                            } else {
                                PreNode clusterAncestor = treeStructure.getEnabledAncestorOrSelf(edgeNode, sight);
                                if (clusterAncestor != null && !checkDouble(clusterAncestor, currentNode.pre, edge)) {
                                    //The linked node is a cluster and has never been visited from this leaf

                                    //Link between a leaf and a cluster
                                    //System.out.println("Lien entre un cluster et un cluster. "+currentNode.pre+" "+edge.edgeType+" "+clusterAncestor.pre);
                                    VirtualEdge newEdge = createEdge(edge, currentNode, clusterAncestor, sight);

                                    //Set the trace
                                    //edgeNode.preTrace = currentNode.pre;
                                    clusterAncestor.preTrace = currentNode.pre;
                                    clusterAncestor.lastEdge = newEdge;
                                }
                            }
                        }
                    }
                }
            }
        }

        //Reinit trace
        currentNode.reinitTrace();
    }

    public void reprocessInducedEdges(Iterable<PreNode> enabledNodes, PreNode center, Sight sight) {
        int centerLimit = center.pre + center.size;
        ParamAVLIterator<PreEdge> preEdgeIterator = new ParamAVLIterator<PreEdge>();
        for (PreNode currentNode : enabledNodes) {
            //System.out.println("reprocess "+currentNode.pre);
            if (currentNode.isLeaf()) {
                if (currentNode.countForwardEdges() > 0) {
                    preEdgeIterator.setNode(currentNode.getForwardEdges());
                    while (preEdgeIterator.hasNext()) {
                        PreEdge edge = preEdgeIterator.next();
                        PreNode edgeNode = edge.maxNode;

                        if (edgeNode.pre > center.pre && edgeNode.pre <= centerLimit && edgeNode.isInSight(sight)) {
                            if (edgeNode.isEnabled(sight)) {
                                //Link between two leafs
                                //System.out.println("Lien entre 2 feuilles. "+currentNode.pre+" "+edge.edgeType+" "+edgeNode.pre);
                                createEdge(edge, currentNode, edgeNode, sight);
                            } else {
                                PreNode clusterAncestor = treeStructure.getEnabledAncestorOrSelf(edgeNode, sight);
                                if (clusterAncestor != null && !checkDouble(clusterAncestor, currentNode.pre, edge)) {
                                    //The linked node is a cluster and has never been visited from this leaf

                                    //Link between a leaf and a cluster
                                    //System.out.println("Lien entre une feuille et un cluster. "+currentNode.pre+" "+edge.edgeType+" "+clusterAncestor.pre);
                                    VirtualEdge newEdge = createEdge(edge, currentNode, clusterAncestor, sight);

                                    //Set the trace
                                    clusterAncestor.preTrace = currentNode.pre;
                                    clusterAncestor.lastEdge = newEdge;
                                }
                            }
                        }
                    }
                }
            } else {
                //Cluster
                int clusterEnd = currentNode.pre + currentNode.size;
                for (int i = currentNode.pre + 1; i <= clusterEnd; i++) {
                    PreNode desc = treeStructure.getNodeAt(i);
                    if (desc.isLeaf() && desc.countForwardEdges() > 0) {
                        preEdgeIterator.setNode(desc.getForwardEdges());
                        while (preEdgeIterator.hasNext()) {
                            PreEdge edge = preEdgeIterator.next();
                            PreNode edgeNode = edge.maxNode;
                            if (edgeNode.pre > center.pre && edgeNode.pre <= centerLimit && edgeNode.isInSight(sight) && !checkDouble(edgeNode, currentNode.pre, edge)) {
                                if (edgeNode.isEnabled(sight)) {
                                    //Link between two leafs
                                    //System.out.println("Lien entre 1 cluster et une feuille"+currentNode.pre+" "+edge.edgeType+" "+edgeNode.pre);
                                    VirtualEdge newEdge = createEdge(edge, currentNode, edgeNode, sight);

                                    //Set the trace
                                    edgeNode.preTrace = currentNode.pre;
                                    edgeNode.lastEdge = newEdge;
                                } else {
                                    PreNode clusterAncestor = treeStructure.getEnabledAncestorOrSelf(edgeNode, sight);
                                    if (clusterAncestor != null && !checkDouble(clusterAncestor, currentNode.pre, edge)) {
                                        //The linked node is a cluster and has never been visited from this leaf

                                        //Link between a leaf and a cluster
                                        //System.out.println("Lien entre un cluster et un cluster. "+currentNode.pre+" "+edge.edgeType+" "+clusterAncestor.pre);
                                        VirtualEdge newEdge = createEdge(edge, currentNode, clusterAncestor, sight);

                                        //Set the trace
                                        clusterAncestor.preTrace = currentNode.pre;
                                        clusterAncestor.lastEdge = newEdge;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            //Reinit trace
            currentNode.reinitTrace();
        }
    }

    private boolean checkDouble(PreNode edgeNode, int pre, PreEdge edge) {
        if (edgeNode.preTrace == pre) {
            if (edgeNode.preTraceType > 0 && edgeNode.preTraceType != edge.edgeType.id) {
                edgeNode.preTraceType = -1;
                return false;
            }
            //System.out.println(edgeNode.lastEdge.getPreNodeFrom().pre+" -> "+edgeNode.lastEdge.getPreNodeTo().pre+"    |    cardinal++");
            //edgeNode.lastEdge.incCardinal(edge.cardinal);
            edgeNode.lastEdge.addPhysicalEdge(edge);
            return true;
        }
        edgeNode.preTraceType = edge.edgeType.id;
        return false;
    }

    private VirtualEdge createEdge(PreEdge edge, PreNode currentNode, PreNode edgeNode, Sight sight) {
        VirtualEdge newEdge = null;

        if (edge.edgeType == EdgeType.IN) {
            newEdge = new VirtualEdge(edgeNode, currentNode);
            newEdge.addPhysicalEdge(edge);
            edgeNode.getVirtualEdgesOUT(sight).add(newEdge);
            currentNode.getVirtualEdgesIN(sight).add(newEdge);
        } else if (edge.edgeType == EdgeType.OUT) {
            newEdge = new VirtualEdge(currentNode, edgeNode);
            newEdge.addPhysicalEdge(edge);
            edgeNode.getVirtualEdgesIN(sight).add(newEdge);
            currentNode.getVirtualEdgesOUT(sight).add(newEdge);
        }
        return newEdge;
    }

    public void appendEdgeHostingNeighbours(PreNode node, PreNodeAVLTree physicalNeighbours, int preLimit, Sight sight) {
        if (node.getVirtualEdgesIN(sight).getCount() > 0) {
            for (DhnsEdge e : node.getVirtualEdgesIN(sight)) {
                PreNode neighbour = e.getPreNodeFrom();
                if (neighbour.pre < preLimit) {
                    physicalNeighbours.add(neighbour);
                }
            }
        }

        if (node.getVirtualEdgesOUT(sight).getCount() > 0) {
            for (DhnsEdge e : node.getVirtualEdgesOUT(sight)) {
                PreNode neighbour = e.getPreNodeTo();
                if (neighbour.pre < preLimit) {
                    physicalNeighbours.add(neighbour);
                }
            }
        }
    }

    public void clearVirtualEdges(PreNode node, Sight sight) {
        if (node.getVirtualEdgesIN(sight).getCount() > 0) {
            for (DhnsEdge n : node.getVirtualEdgesIN(sight)) {
                n.getPreNodeFrom().getVirtualEdgesOUT(sight).remove(n);
                n.getPreNodeFrom().reinitTrace();
            }

            node.getVirtualEdgesIN(sight).clear();
        }

        if (node.getVirtualEdgesOUT(sight).getCount() > 0) {
            for (DhnsEdge n : node.getVirtualEdgesOUT(sight)) {
                n.getPreNodeTo().getVirtualEdgesIN(sight).remove(n);
                n.getPreNodeTo().reinitTrace();
            }

            node.getVirtualEdgesOUT(sight).clear();
        }
    }

    public void clearPhysicalEdges(PreNode node) {
        if (node.getBackwardEdges().getCount() > 0) {
            for (PreEdge mirroredEdge : node.getBackwardEdges()) {
                mirroredEdge.minNode.getForwardEdges().remove(mirroredEdge);
            }

            node.getBackwardEdges().clear();
        }

        if (node.getForwardEdges().getCount() > 0) {
            for (PreEdge physicalEdge : node.getForwardEdges()) {
                physicalEdge.maxNode.getBackwardEdges().remove(physicalEdge);
            }

            node.getForwardEdges().clear();
        }
    }

    public VirtualEdge createVirtualEdge(PreEdge physicalEdge, PreNode minParent, PreNode maxParent, Sight sight) {
        VirtualEdge virtualEdge = null;
        if (physicalEdge.edgeType == EdgeType.IN) {
            virtualEdge = new VirtualEdge(maxParent, minParent);
            virtualEdge.addPhysicalEdge(physicalEdge);
            maxParent.getVirtualEdgesOUT(sight).add(virtualEdge);
            minParent.getVirtualEdgesIN(sight).add(virtualEdge);
        } else {
            virtualEdge = new VirtualEdge(minParent, maxParent);
            virtualEdge.addPhysicalEdge(physicalEdge);
            maxParent.getVirtualEdgesIN(sight).add(virtualEdge);
            minParent.getVirtualEdgesOUT(sight).add(virtualEdge);
        }
        return virtualEdge;
    }
}
