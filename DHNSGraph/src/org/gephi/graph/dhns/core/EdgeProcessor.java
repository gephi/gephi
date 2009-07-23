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
import org.gephi.graph.dhns.edge.MetaEdgeImpl;
import org.gephi.graph.dhns.node.AbstractNode;
import org.gephi.graph.dhns.node.iterators.PreNodeTreeListIterator;

/**
 * Business class for managing Edges and MetaEdges.
 *
 * @author Mathieu Bastian
 */
public class EdgeProcessor {

    private TreeStructure treeStructure;
    private ParamAVLIterator<AbstractEdge> edgeIterator;
    private IDGen idGen;
    private boolean enableMetaEdges = true;

    public EdgeProcessor(Dhns dhns) {
        this.treeStructure = dhns.getTreeStructure();
        this.idGen = dhns.getIdGen();
        this.edgeIterator = new ParamAVLIterator<AbstractEdge>();
    }

    public void clearEdges(AbstractNode node) {
        if (node.getEdgesInTree().getCount() > 0) {
            edgeIterator.setNode(node.getEdgesInTree());
            while (edgeIterator.hasNext()) {
                AbstractEdge edge = edgeIterator.next();
                removeEdgeFromMetaEdge(edge);
                edge.getSource().getEdgesOutTree().remove(edge);
            }
            node.getEdgesInTree().clear();
        }

        if (node.getEdgesOutTree().getCount() > 0) {
            edgeIterator.setNode(node.getEdgesOutTree());
            while (edgeIterator.hasNext()) {
                AbstractEdge edge = edgeIterator.next();
                removeEdgeFromMetaEdge(edge);
                edge.getTarget().getEdgesInTree().remove(edge);
            }
            node.getEdgesOutTree().clear();
        }
    }

    public void clearEdgesWithoutRemove(AbstractNode node) {
        if (node.getEdgesInTree().getCount() > 0) {
            edgeIterator.setNode(node.getEdgesInTree());
            while (edgeIterator.hasNext()) {
                AbstractEdge edge = edgeIterator.next();
                removeEdgeFromMetaEdge(edge);
            }
        }

        if (node.getEdgesOutTree().getCount() > 0) {
            edgeIterator.setNode(node.getEdgesOutTree());
            while (edgeIterator.hasNext()) {
                AbstractEdge edge = edgeIterator.next();
                removeEdgeFromMetaEdge(edge);
            }
        }
    }

    public void clearMetaEdges(AbstractNode node) {
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

    public void clearMetaEdgesOutOfRange(AbstractNode enabledNode, AbstractNode rangeNode) {
        int rangeStart = rangeNode.getPre();
        int rangeLimit = rangeStart + rangeNode.size;
        if (enabledNode.getMetaEdgesOutTree().getCount() > 0) {
            edgeIterator.setNode(enabledNode.getMetaEdgesOutTree());
            while (edgeIterator.hasNext()) {
                MetaEdgeImpl metaEdge = (MetaEdgeImpl) edgeIterator.next();
                int targetPre = metaEdge.getTarget().getPre();
                if (targetPre >= rangeStart && targetPre <= rangeLimit) {
                    //The meta edge has to be removed because it's in the range
                    edgeIterator.remove();
                    metaEdge.getTarget().getMetaEdgesInTree().remove(metaEdge);
                }
            }
        }

        if (enabledNode.getMetaEdgesInTree().getCount() > 0) {
            edgeIterator.setNode(enabledNode.getMetaEdgesInTree());
            while (edgeIterator.hasNext()) {
                MetaEdgeImpl metaEdge = (MetaEdgeImpl) edgeIterator.next();
                int sourcePre = metaEdge.getSource().getPre();
                if (sourcePre >= rangeStart && sourcePre <= rangeLimit) {
                    //The meta edge has to be removed because it's in the range
                    edgeIterator.remove();
                    metaEdge.getSource().getMetaEdgesOutTree().remove(metaEdge);
                }
            }
        }
    }

    public void clearAllEdges() {
        for (PreNodeTreeListIterator itr = new PreNodeTreeListIterator(treeStructure.getTree()); itr.hasNext();) {
            AbstractNode node = itr.next();
            node.getEdgesInTree().clear();
            node.getEdgesOutTree().clear();
            node.getMetaEdgesInTree().clear();
            node.getMetaEdgesOutTree().clear();
        }
    }

    public void clearAllMetaEdges() {
        for (PreNodeTreeListIterator itr = new PreNodeTreeListIterator(treeStructure.getTree()); itr.hasNext();) {
            AbstractNode node = itr.next();
            node.getMetaEdgesInTree().clear();
            node.getMetaEdgesOutTree().clear();
        }
    }

    public void computeMetaEdges(AbstractNode node, AbstractNode enabledAncestor) {
        if (!enableMetaEdges) {
            return;
        }
        if (enabledAncestor == null) {
            enabledAncestor = node;
        }
        int clusterEnd = node.getPre() + node.size;
        for (int i = node.pre; i <= clusterEnd; i++) {
            AbstractNode desc = treeStructure.getNodeAt(i);
            if (desc.getEdgesOutTree().getCount() > 0) {
                edgeIterator.setNode(desc.getEdgesOutTree());
                while (edgeIterator.hasNext()) {
                    AbstractEdge edge = edgeIterator.next();
                    AbstractNode targetNode = treeStructure.getEnabledAncestorOrSelf(edge.getTarget());
                    if (targetNode != null && !(targetNode == edge.getTarget() && enabledAncestor == edge.getSource())) {
                        //Create Meta Edge if not exist
                        createMetaEdge(enabledAncestor, targetNode, edge);
                    }
                }
            }
            if (desc.getEdgesInTree().getCount() > 0) {
                edgeIterator.setNode(desc.getEdgesInTree());
                while (edgeIterator.hasNext()) {
                    AbstractEdge edge = edgeIterator.next();
                    AbstractNode sourceNode = treeStructure.getEnabledAncestorOrSelf(edge.getSource());
                    if (sourceNode != null && !(sourceNode == edge.getSource() && enabledAncestor == edge.getTarget())) {
                        //Create Meta Edge if not exist
                        createMetaEdge(sourceNode, enabledAncestor, edge);
                    }
                }
            }
        }
    }

    private void createMetaEdge(AbstractNode source, AbstractNode target, AbstractEdge edge) {
        if (edge.getSource() == source && edge.getTarget() == target) {
            return;
        }
        if (source == target) {
            return;
        }

        MetaEdgeImpl metaEdge = getMetaEdge(source, target);
        if (metaEdge != null) {
            metaEdge.addEdge(edge);
        } else {
            metaEdge = createMetaEdge(source, target);
            if (metaEdge != null) {
                metaEdge.addEdge(edge);
            }
        }
    }

    private MetaEdgeImpl createMetaEdge(AbstractNode source, AbstractNode target) {
        if (source == target) {
            return null;
        }
        MetaEdgeImpl newEdge = new MetaEdgeImpl(idGen.newEdgeId(), source, target);
        source.getMetaEdgesOutTree().add(newEdge);
        target.getMetaEdgesInTree().add(newEdge);
        return newEdge;
    }

    public void createMetaEdge(AbstractEdge edge) {
        if (!enableMetaEdges) {
            return;
        }
        if (edge.isSelfLoop()) {
            return;
        }
        AbstractNode sourceParent = treeStructure.getEnabledAncestorOrSelf(edge.getSource());
        AbstractNode targetParent = treeStructure.getEnabledAncestorOrSelf(edge.getTarget());

        if (sourceParent != null && targetParent != null && sourceParent != targetParent) {
            createMetaEdge(sourceParent, targetParent, edge);
        }
    }

    public void removeEdgeFromMetaEdge(AbstractEdge edge) {
        if (!enableMetaEdges) {
            return;
        }
        if (edge.isSelfLoop()) {
            return;
        }
        MetaEdgeImpl metaEdge = getMetaEdge(edge);
        if (metaEdge != null) {
            metaEdge.removeEdge(edge);
            if (metaEdge.isEmpty()) {
                metaEdge.getSource().getMetaEdgesOutTree().remove(metaEdge);
                metaEdge.getTarget().getMetaEdgesInTree().remove(metaEdge);
            }
        }
    }

    private MetaEdgeImpl getMetaEdge(AbstractNode source, AbstractNode target) {
        if (source == target) {
            return null;
        }
        return source.getMetaEdgesOutTree().getItem(target.getNumber());
    }

    private MetaEdgeImpl getMetaEdge(AbstractEdge edge) {
        if (edge.isSelfLoop()) {
            return null;
        }
        AbstractNode sourceParent = treeStructure.getEnabledAncestorOrSelf(edge.getSource());
        AbstractNode targetParent = treeStructure.getEnabledAncestorOrSelf(edge.getTarget());

        if (sourceParent != null && targetParent != null && sourceParent != targetParent) {
            return getMetaEdge(sourceParent, targetParent);
        }
        return null;
    }
}
