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

import org.gephi.datastructure.avl.param.ParamAVLIterator;
import org.gephi.graph.dhns.edge.AbstractEdge;
import org.gephi.graph.dhns.edge.EdgeImpl;
import org.gephi.graph.dhns.edge.MetaEdgeImpl;
import org.gephi.graph.dhns.node.PreNode;
import org.gephi.graph.dhns.node.iterators.TreeListIterator;

/**
 * Business class for managing Edges and MetaEdges.
 *
 * @author Mathieu Bastian
 */
public class EdgeProcessor {

    private TreeStructure treeStructure;
    private ParamAVLIterator<AbstractEdge> edgeIterator;

    public EdgeProcessor(Dhns dhns) {
        this.treeStructure = dhns.getTreeStructure();
    }

    public void clearEdges(PreNode node) {
        if (node.getEdgesInTree().getCount() > 0) {
            edgeIterator.setNode(node.getEdgesInTree());
            while (edgeIterator.hasNext()) {
                AbstractEdge edge = edgeIterator.next();
                removeEdgeFromMetaEdge((EdgeImpl) edge);
                edge.getSource().getEdgesOutTree().remove((EdgeImpl) edge);
            }
            node.getEdgesInTree().clear();
        }

        if (node.getEdgesOutTree().getCount() > 0) {
            edgeIterator.setNode(node.getEdgesOutTree());
            while (edgeIterator.hasNext()) {
                AbstractEdge edge = edgeIterator.next();
                removeEdgeFromMetaEdge((EdgeImpl) edge);
                edge.getTarget().getEdgesInTree().remove((EdgeImpl) edge);
            }
            node.getMetaEdgesOutTree().clear();
        }
    }

    public void clearMetaEdges(PreNode node) {
        if (node.getMetaEdgesInTree().getCount() > 0) {
            edgeIterator.setNode(node.getMetaEdgesInTree());
            while (edgeIterator.hasNext()) {
                AbstractEdge edge = edgeIterator.next();
                edge.getSource().getMetaEdgesOutTree().remove((MetaEdgeImpl) edge);
            }
            node.getMetaEdgesInTree().clear();
        }

        if (node.getMetaEdgesOutTree().getCount() > 0) {
            edgeIterator.setNode(node.getMetaEdgesOutTree());
            while (edgeIterator.hasNext()) {
                AbstractEdge edge = edgeIterator.next();
                edge.getTarget().getMetaEdgesInTree().remove((MetaEdgeImpl) edge);
            }
            node.getMetaEdgesOutTree().clear();
        }
    }

    public void clearAllEdges() {
        for (TreeListIterator itr = new TreeListIterator(treeStructure.getTree()); itr.hasNext();) {
            PreNode node = itr.next();
            node.getEdgesInTree().clear();
            node.getEdgesOutTree().clear();
            node.getMetaEdgesInTree().clear();
            node.getMetaEdgesOutTree().clear();
        }
    }

    public void computeMetaEdges(PreNode node) {
        int clusterEnd = node.pre + node.size;
        for (int i = node.pre; i <= clusterEnd; i++) {
            PreNode desc = treeStructure.getNodeAt(i);
            if (desc.getEdgesOutTree().getCount() > 0) {
                edgeIterator.setNode(desc.getEdgesOutTree());
                while (edgeIterator.hasNext()) {
                    AbstractEdge edge = edgeIterator.next();
                    PreNode targetNode = treeStructure.getEnabledAncestorOrSelf(edge.getTarget());
                    if (targetNode != null && targetNode != edge.getTarget()) {
                        //Create Meta Edge if not exist
                        createMetaEdge(node, targetNode, (EdgeImpl) edge);
                    }
                }
            }
            if (desc.getEdgesInTree().getCount() > 0) {
                edgeIterator.setNode(desc.getEdgesInTree());
                while (edgeIterator.hasNext()) {
                    AbstractEdge edge = edgeIterator.next();
                    PreNode sourceNode = treeStructure.getEnabledAncestorOrSelf(edge.getSource());
                    if (sourceNode != null && sourceNode != edge.getSource()) {
                        //Create Meta Edge if not exist
                        createMetaEdge(sourceNode, node, (EdgeImpl) edge);
                    }
                }
            }
        }
    }

    public void createMetaEdge(PreNode source, PreNode target, EdgeImpl edge) {
        if (edge.getSource() == source && edge.getTarget() == target) {
            return;
        }

        MetaEdgeImpl metaEdge = getMetaEdge(source, target);
        if (metaEdge != null) {
            metaEdge.addEdge(edge);
        } else {
            createMetaEdge(source, target);
        }
    }

    public void createMetaEdge(PreNode source, PreNode target) {
        MetaEdgeImpl newEdge = new MetaEdgeImpl(source, target);
        source.getMetaEdgesOutTree().add(newEdge);
        target.getMetaEdgesInTree().add(newEdge);
    }

    public void createMetaEdge(EdgeImpl edge) {
        PreNode sourceParent = treeStructure.getEnabledAncestorOrSelf(edge.getSource());
        PreNode targetParent = treeStructure.getEnabledAncestorOrSelf(edge.getTarget());

        if (sourceParent != null && targetParent != null && sourceParent != targetParent) {
            createMetaEdge(sourceParent, targetParent, edge);
        }
    }

    public void removeEdgeFromMetaEdge(EdgeImpl edge) {
        MetaEdgeImpl metaEdge = getMetaEdge(edge);
        if (metaEdge != null) {
            metaEdge.removeEdge(edge);
            if (metaEdge.isEmpty()) {
                metaEdge.getSource().getMetaEdgesOutTree().remove(metaEdge);
                metaEdge.getTarget().getMetaEdgesInTree().remove(metaEdge);
            }
        }
    }

    public MetaEdgeImpl getMetaEdge(PreNode source, PreNode target) {
        return source.getMetaEdgesOutTree().getItem(target.getNumber());
    }

    public MetaEdgeImpl getMetaEdge(EdgeImpl edge) {
        PreNode sourceParent = treeStructure.getEnabledAncestorOrSelf(edge.getSource());
        PreNode targetParent = treeStructure.getEnabledAncestorOrSelf(edge.getTarget());

        if (sourceParent != null && targetParent != null && sourceParent != targetParent) {
            return getMetaEdge(sourceParent, targetParent);
        }
        return null;
    }
}
