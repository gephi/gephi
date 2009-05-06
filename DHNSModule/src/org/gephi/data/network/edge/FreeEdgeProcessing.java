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
import org.gephi.data.network.mode.EdgeProcessing;
import org.gephi.data.network.node.NodeImpl;
import org.gephi.data.network.node.PreNode;
import org.gephi.data.network.node.treelist.CompleteTreeIterator;
import org.gephi.data.network.node.treelist.VisibleTreeIterator;
import org.gephi.data.network.utils.avl.DhnsEdgeTree;
import org.gephi.datastructure.avl.param.ParamAVLIterator;

/**
 *
 * @author Mathieu Bastian
 */
public class FreeEdgeProcessing implements EdgeProcessing {

    private TreeStructure treeStructure;
    private ParamAVLIterator<PreEdge> preEdgeIterator;

    public FreeEdgeProcessing(TreeStructure treeStructure) {
        this.treeStructure = treeStructure;
        preEdgeIterator = new ParamAVLIterator<PreEdge>();
    }

    public void init() {
        processInducedEdges();
        //buildHierarchyViewMode(sightImpl);
    }

    public void clear() {
        VisibleTreeIterator enabledNodes = new VisibleTreeIterator(treeStructure);
        while (enabledNodes.hasNext()) {
            PreNode currentNode = enabledNodes.next();
            currentNode.clearDhnsEdges();
        }
    }

    public void processInducedEdges() {

        VisibleTreeIterator enabledNodes = new VisibleTreeIterator(treeStructure);
        while (enabledNodes.hasNext()) {
            PreNode currentNode = enabledNodes.next();
            processInducedEdges(currentNode);
        }

    }

    public void processInducedEdges(PreNode currentNode) {
        if (currentNode.isLeaf()) {
            if (currentNode.countForwardEdges() > 0) {
                //Leaf
                preEdgeIterator.setNode(currentNode.getForwardEdges());
                while (preEdgeIterator.hasNext()) {
                    PreEdge edge = preEdgeIterator.next();
                    PreNode edgeNode = edge.maxNode;

                    if (edgeNode.pre > currentNode.pre) {
                        if (edgeNode.isEnabled()) {
                            //Link between two leafs
                            //System.out.println("Lien entre 2 feuilles. "+currentNode.pre+" "+edge.edgeType+" "+edgeNode.pre);
                            createEdge(edge, currentNode, edgeNode);
                        } else {
                            PreNode clusterAncestor = treeStructure.getEnabledAncestorOrSelf(edgeNode);
                            if (clusterAncestor != null && !checkDouble(clusterAncestor, currentNode.pre, edge)) {
                                //The linked node is a cluster and has never been visited from this leaf

                                //Link between a leaf and a cluster
                                //System.out.println("Lien entre une feuille et un cluster. "+currentNode.pre+" "+edge.edgeType+" "+clusterAncestor.pre);
                                VirtualEdge newEdge = createEdge(edge, currentNode, clusterAncestor);

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

                        if (edgeNode.pre > clusterEnd && !checkDouble(edgeNode, currentNode.pre, edge)) {
                            if (edgeNode.isEnabled()) {
                                //Link between two leafs
                                //System.out.println("Lien entre 1 cluster et une feuille "+currentNode.pre+" "+edge.edgeType+" "+edgeNode.pre);
                                VirtualEdge newEdge = createEdge(edge, currentNode, edgeNode);

                                //Set the trace
                                edgeNode.preTrace = currentNode.pre;
                                edgeNode.lastEdge = newEdge;
                            } else {
                                PreNode clusterAncestor = treeStructure.getEnabledAncestorOrSelf(edgeNode);
                                if (clusterAncestor != null && !checkDouble(clusterAncestor, currentNode.pre, edge)) {
                                    //The linked node is a cluster and has never been visited from this leaf

                                    //Link between a leaf and a cluster
                                    //System.out.println("Lien entre un cluster et un cluster. "+currentNode.pre+" "+edge.edgeType+" "+clusterAncestor.pre);
                                    VirtualEdge newEdge = createEdge(edge, currentNode, clusterAncestor);

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

    public void processLocalInducedEdges(PreNode currentNode) {
        if (currentNode.isLeaf()) {
            if (currentNode.countForwardEdges() > 0) {
                //Leaf
                preEdgeIterator.setNode(currentNode.getForwardEdges());
                while (preEdgeIterator.hasNext()) {
                    PreEdge edge = preEdgeIterator.next();
                    PreNode edgeNode = edge.maxNode;

                    if (edgeNode.pre > currentNode.pre) {
                        if (edgeNode.isEnabled()) {
                            //Link between two leafs
                            //System.out.println("Lien entre 2 feuilles. "+currentNode.pre+" "+edge.edgeType+" "+edgeNode.pre);
                            createEdge(edge, currentNode, edgeNode);
                        } else {
                            PreNode clusterAncestor = treeStructure.getEnabledAncestorOrSelf(edgeNode);
                            if (clusterAncestor != null && !checkDouble(clusterAncestor, currentNode.pre, edge)) {
                                //The linked node is a cluster and has never been visited from this leaf

                                //Link between a leaf and a cluster
                                //System.out.println("Lien entre une feuille et un cluster. "+currentNode.pre+" "+edge.edgeType+" "+clusterAncestor.pre);
                                VirtualEdge newEdge = createEdge(edge, currentNode, clusterAncestor);

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

                        if (edgeNode.pre > clusterEnd && !checkDouble(edgeNode, currentNode.pre, edge)) {
                            if (edgeNode.isEnabled()) {
                                //Link between two leafs
                                //System.out.println("Lien entre 1 cluster et une feuille"+currentNode.pre+" "+edge.edgeType+" "+edgeNode.pre);
                                VirtualEdge newEdge = createEdge(edge, currentNode, edgeNode);

                                //Set the trace
                                edgeNode.preTrace = currentNode.pre;
                                edgeNode.lastEdge = newEdge;
                            } else {
                                PreNode clusterAncestor = treeStructure.getEnabledAncestorOrSelf(edgeNode);
                                if (clusterAncestor != null && !checkDouble(clusterAncestor, currentNode.pre, edge)) {
                                    //The linked node is a cluster and has never been visited from this leaf

                                    //Link between a leaf and a cluster
                                    //System.out.println("Lien entre un cluster et un cluster. "+currentNode.pre+" "+edge.edgeType+" "+clusterAncestor.pre);
                                    VirtualEdge newEdge = createEdge(edge, currentNode, clusterAncestor);

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

    public void reprocessInducedEdges(Iterable<PreNode> enabledNodes, PreNode center) {
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

                        if (edgeNode.pre > center.pre && edgeNode.pre <= centerLimit) {
                            if (edgeNode.isEnabled()) {
                                //Link between two leafs
                                //System.out.println("Lien entre 2 feuilles. "+currentNode.pre+" "+edge.edgeType+" "+edgeNode.pre);
                                createEdge(edge, currentNode, edgeNode);
                            } else {
                                PreNode clusterAncestor = treeStructure.getEnabledAncestorOrSelf(edgeNode);
                                if (clusterAncestor != null && !checkDouble(clusterAncestor, currentNode.pre, edge)) {
                                    //The linked node is a cluster and has never been visited from this leaf

                                    //Link between a leaf and a cluster
                                    //System.out.println("Lien entre une feuille et un cluster. "+currentNode.pre+" "+edge.edgeType+" "+clusterAncestor.pre);
                                    VirtualEdge newEdge = createEdge(edge, currentNode, clusterAncestor);

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
                            if (edgeNode.pre > center.pre && edgeNode.pre <= centerLimit && !checkDouble(edgeNode, currentNode.pre, edge)) {
                                if (edgeNode.isEnabled()) {
                                    //Link between two leafs
                                    //System.out.println("Lien entre 1 cluster et une feuille"+currentNode.pre+" "+edge.edgeType+" "+edgeNode.pre);
                                    VirtualEdge newEdge = createEdge(edge, currentNode, edgeNode);

                                    //Set the trace
                                    edgeNode.preTrace = currentNode.pre;
                                    edgeNode.lastEdge = newEdge;
                                } else {
                                    PreNode clusterAncestor = treeStructure.getEnabledAncestorOrSelf(edgeNode);
                                    if (clusterAncestor != null && !checkDouble(clusterAncestor, currentNode.pre, edge)) {
                                        //The linked node is a cluster and has never been visited from this leaf

                                        //Link between a leaf and a cluster
                                        //System.out.println("Lien entre un cluster et un cluster. "+currentNode.pre+" "+edge.edgeType+" "+clusterAncestor.pre);
                                        VirtualEdge newEdge = createEdge(edge, currentNode, clusterAncestor);

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

    private VirtualEdge createEdge(PreEdge edge, PreNode currentNode, PreNode edgeNode) {
        VirtualEdge newEdge = null;

        if (edge.edgeType == EdgeType.IN) {
            newEdge = new VirtualEdge(edgeNode, currentNode);
            newEdge.addPhysicalEdge(edge);
            edgeNode.addDhnsEdgeOUT(newEdge);
            currentNode.addDhnsEdgeIN(newEdge);
        } else if (edge.edgeType == EdgeType.OUT) {
            newEdge = new VirtualEdge(currentNode, edgeNode);
            newEdge.addPhysicalEdge(edge);
            edgeNode.addDhnsEdgeIN(newEdge);
            currentNode.addDhnsEdgeOUT(newEdge);
        }
        return newEdge;
    }

    public void appendEdgeHostingNeighbours(PreNode node, PreNodeAVLTree physicalNeighbours, int preLimit) {
        if (node.countDhnsEdgeIN() > 0) {
            for (DhnsEdge e : node.getDhnsEdgesIN()) {
                PreNode neighbour = e.getPreNodeFrom();
                if (neighbour.pre < preLimit) {
                    physicalNeighbours.add(neighbour);
                }
            }
        }

        if (node.countDhnsEdgeOUT() > 0) {
            for (DhnsEdge e : node.getDhnsEdgesOUT()) {
                PreNode neighbour = e.getPreNodeTo();
                if (neighbour.pre < preLimit) {
                    physicalNeighbours.add(neighbour);
                }
            }
        }
    }

    public void clearVirtualEdges(PreNode node) {
        if (node.countDhnsEdgeIN() > 0) {
            for (DhnsEdge n : node.getDhnsEdgesIN()) {
                n.getPreNodeFrom().removeDhnsEdgeOUT(n);
                n.getPreNodeFrom().reinitTrace();
            }

            node.clearDhnsEdgesIN();
        }

        if (node.countDhnsEdgeOUT() > 0) {
            for (DhnsEdge n : node.getDhnsEdgesOUT()) {
                n.getPreNodeTo().removeDhnsEdgeIN(n);
                n.getPreNodeTo().reinitTrace();
            }

            node.clearDhnsEdgesOUT();
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

    public VirtualEdge createVirtualEdge(PreEdge physicalEdge, PreNode minParent, PreNode maxParent) {
        VirtualEdge virtualEdge = null;
        if (physicalEdge.edgeType == EdgeType.IN) {
            virtualEdge = new VirtualEdge(maxParent, minParent);
            virtualEdge.addPhysicalEdge(physicalEdge);
            maxParent.addDhnsEdgeOUT(virtualEdge);
            minParent.addDhnsEdgeIN(virtualEdge);
        } else {
            virtualEdge = new VirtualEdge(minParent, maxParent);
            virtualEdge.addPhysicalEdge(physicalEdge);
            maxParent.addDhnsEdgeIN(virtualEdge);
            minParent.addDhnsEdgeOUT(virtualEdge);
        }
        return virtualEdge;
    }

    public void buildHierarchyViewMode() {
        CompleteTreeIterator enabledNodes = new CompleteTreeIterator(treeStructure);
        for (; enabledNodes.hasNext();) {
            PreNode n = enabledNodes.next();
            NodeImpl ni = n.getNode();
            ni.setX(n.getPre() * 50);
            ni.setY(n.getPost() * 50);
            if (n.parent != null) {
                DhnsEdgeTree edgeTree = n.parent.getDhnsEdgesOUT();
                HierarchyEdge he = new HierarchyEdge(n.parent, n);
                edgeTree.add(he);
            }
        }
    }
}
