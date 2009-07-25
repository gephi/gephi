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
import org.gephi.graph.dhns.edge.DefaultMetaEdgeBuilder;
import org.gephi.graph.dhns.edge.MetaEdgeImpl;
import org.gephi.graph.dhns.node.AbstractNode;
import org.gephi.graph.dhns.node.iterators.PreNodeTreeListIterator;
import org.gephi.graph.dhns.utils.avl.ViewAVLTree.ViewAVLIterator;
import org.gephi.graph.dhns.view.View;

/**
 * Business class for managing Edges and MetaEdges.
 *
 * @author Mathieu Bastian
 */
public class EdgeProcessor {

    //Architecture
    private TreeStructure treeStructure;
    private IDGen idGen;
    //Config
    private boolean enableMetaEdges = true;
    //Utils
    private MetaEdgeBuilder metaEdgeBuilder;
    //Cache
    private ParamAVLIterator<AbstractEdge> edgeIterator;
    private ViewAVLIterator viewIterator;

    public EdgeProcessor(Dhns dhns) {
        this.treeStructure = dhns.getTreeStructure();
        this.idGen = dhns.getIdGen();
        this.edgeIterator = new ParamAVLIterator<AbstractEdge>();
        this.viewIterator = new ViewAVLIterator();
        this.metaEdgeBuilder = new DefaultMetaEdgeBuilder();
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

    public void clearMetaEdges(View view, AbstractNode node) {
        if (node.getMetaEdgesInTree(view).getCount() > 0) {
            edgeIterator.setNode(node.getMetaEdgesInTree(view));
            while (edgeIterator.hasNext()) {
                AbstractEdge edge = edgeIterator.next();
                edge.getSource().getMetaEdgesOutTree(view).remove((MetaEdgeImpl) edge);
            }
            node.getMetaEdgesInTree(view).clear();
        }

        if (node.getMetaEdgesOutTree(view).getCount() > 0) {
            edgeIterator.setNode(node.getMetaEdgesOutTree(view));
            while (edgeIterator.hasNext()) {
                AbstractEdge edge = edgeIterator.next();
                edge.getTarget().getMetaEdgesInTree(view).remove((MetaEdgeImpl) edge);
            }
            node.getMetaEdgesOutTree(view).clear();
        }
    }

    public void clearMetaEdgesOutOfRange(View view, AbstractNode enabledNode, AbstractNode rangeNode) {
        int rangeStart = rangeNode.getPre();
        int rangeLimit = rangeStart + rangeNode.size;
        if (enabledNode.getMetaEdgesOutTree(view).getCount() > 0) {
            edgeIterator.setNode(enabledNode.getMetaEdgesOutTree(view));
            while (edgeIterator.hasNext()) {
                MetaEdgeImpl metaEdge = (MetaEdgeImpl) edgeIterator.next();
                int targetPre = metaEdge.getTarget().getPre();
                if (targetPre >= rangeStart && targetPre <= rangeLimit) {
                    //The meta edge has to be removed because it's in the range
                    edgeIterator.remove();
                    metaEdge.getTarget().getMetaEdgesInTree(view).remove(metaEdge);
                }
            }
        }

        if (enabledNode.getMetaEdgesInTree(view).getCount() > 0) {
            edgeIterator.setNode(enabledNode.getMetaEdgesInTree(view));
            while (edgeIterator.hasNext()) {
                MetaEdgeImpl metaEdge = (MetaEdgeImpl) edgeIterator.next();
                int sourcePre = metaEdge.getSource().getPre();
                if (sourcePre >= rangeStart && sourcePre <= rangeLimit) {
                    //The meta edge has to be removed because it's in the range
                    edgeIterator.remove();
                    metaEdge.getSource().getMetaEdgesOutTree(view).remove(metaEdge);
                }
            }
        }
    }

    public void clearAllEdges() {
        for (PreNodeTreeListIterator itr = new PreNodeTreeListIterator(treeStructure.getTree()); itr.hasNext();) {
            AbstractNode node = itr.next();
            node.getEdgesInTree().clear();
            node.getEdgesOutTree().clear();
            node.clearMetaEdges();
        }
    }

    public void clearAllMetaEdges() {
        for (PreNodeTreeListIterator itr = new PreNodeTreeListIterator(treeStructure.getTree()); itr.hasNext();) {
            AbstractNode node = itr.next();
            node.clearMetaEdges();
        }
    }

    public void clearAllMetaEdges(View view) {
        for (PreNodeTreeListIterator itr = new PreNodeTreeListIterator(treeStructure.getTree()); itr.hasNext();) {
            AbstractNode node = itr.next();
            node.getMetaEdgesInTree(view).clear();
            node.getMetaEdgesOutTree(view).clear();
        }
    }

    public void computeMetaEdges(View view, AbstractNode node, AbstractNode enabledAncestor) {
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
                    AbstractNode[] enabledAncestors = treeStructure.getEnabledAncestorsOrSelf(view, edge.getTarget());
                    if (enabledAncestors != null) {
                        for (int j = 0; j < enabledAncestors.length; j++) {
                            AbstractNode targetNode = enabledAncestors[j];
                            if (!(targetNode == edge.getTarget() && enabledAncestor == edge.getSource())) {
                                createMetaEdge(view, enabledAncestor, targetNode, edge);
                            }
                        }
                    }

//                    AbstractNode targetNode = treeStructure.getEnabledAncestorOrSelf(edge.getTarget());
//                    if (targetNode != null && !(targetNode == edge.getTarget() && enabledAncestor == edge.getSource())) {
//                    //Create Meta Edge if not exist
//                    createMetaEdge(enabledAncestor, targetNode, edge);
//                    }
                }
            }
            if (desc.getEdgesInTree().getCount() > 0) {
                edgeIterator.setNode(desc.getEdgesInTree());
                while (edgeIterator.hasNext()) {
                    AbstractEdge edge = edgeIterator.next();
                    AbstractNode[] enabledAncestors = treeStructure.getEnabledAncestorsOrSelf(view, edge.getSource());
                    if (enabledAncestors != null) {
                        for (int j = 0; j < enabledAncestors.length; j++) {
                            AbstractNode sourceNode = enabledAncestors[j];
                            if (!(sourceNode == edge.getSource() && enabledAncestor == edge.getTarget())) {
                                createMetaEdge(view, sourceNode, enabledAncestor, edge);
                            }
                        }
                    }
//                    AbstractNode sourceNode = treeStructure.getEnabledAncestorOrSelf(edge.getSource());
//                    if (sourceNode != null && !(sourceNode == edge.getSource() && enabledAncestor == edge.getTarget())) {
//                        //Create Meta Edge if not exist
//                        createMetaEdge(sourceNode, enabledAncestor, edge);
//                    }
                }
            }
        }
    }

    private void createMetaEdge(View view, AbstractNode source, AbstractNode target, AbstractEdge edge) {
        if (edge.getSource() == source && edge.getTarget() == target) {
            return;
        }
        if (source == target) {
            return;
        }

        MetaEdgeImpl metaEdge = getMetaEdge(view, source, target);
        if (metaEdge == null) {
            metaEdge = createMetaEdge(view, source, target);
        }
        if (metaEdge != null) {
             if(metaEdge.addEdge(edge)) {
                 metaEdgeBuilder.pushEdge(edge, metaEdge);
             }
        }
    }

    private MetaEdgeImpl createMetaEdge(View view, AbstractNode source, AbstractNode target) {
        if (source == target) {
            return null;
        }
        MetaEdgeImpl newEdge = new MetaEdgeImpl(idGen.newEdgeId(), source, target);
        source.getMetaEdgesOutTree(view).add(newEdge);
        target.getMetaEdgesInTree(view).add(newEdge);
        return newEdge;
    }

    public void createMetaEdge(AbstractEdge edge) {
        if (!enableMetaEdges) {
            return;
        }
        if (edge.isSelfLoop()) {
            return;
        }
        for (viewIterator.setNode(edge.getSource().getViews()); viewIterator.hasNext();) {
            View view = viewIterator.next();
            if (edge.getTarget().isInView(view)) {
                AbstractNode[] sourceAncestors = treeStructure.getEnabledAncestorsOrSelf(view, edge.getSource());
                AbstractNode[] targetAncestors = treeStructure.getEnabledAncestorsOrSelf(view, edge.getTarget());

                if (sourceAncestors != null && targetAncestors != null) {
                    for (int i = 0; i < sourceAncestors.length; i++) {
                        for (int j = 0; j < targetAncestors.length; j++) {
                            AbstractNode sourceParent = sourceAncestors[i];
                            AbstractNode targetParent = targetAncestors[j];
                            if (sourceParent != targetParent) {
                                createMetaEdge(view, sourceParent, targetParent, edge);
                            }
                        }
                    }
                }
            }
        }

//        AbstractNode sourceParent = treeStructure.getEnabledAncestorOrSelf(edge.getSource());
//        AbstractNode targetParent = treeStructure.getEnabledAncestorOrSelf(edge.getTarget());
//
//        if (sourceParent != null && targetParent != null && sourceParent != targetParent) {
//            createMetaEdge(sourceParent, targetParent, edge);
//        }
    }

    public void removeEdgeFromMetaEdge(AbstractEdge edge) {
        if (!enableMetaEdges) {
            return;
        }
        if (edge.isSelfLoop()) {
            return;
        }

        for (viewIterator.setNode(edge.getSource().getViews()); viewIterator.hasNext();) {
            View view = viewIterator.next();
            if (edge.getTarget().isInView(view)) {
                MetaEdgeImpl metaEdge = getMetaEdge(view, edge);
                if (metaEdge != null) {
                    if(metaEdge.removeEdge(edge)) {
                        metaEdgeBuilder.pullEdge(edge, metaEdge);
                    }
                    if (metaEdge.isEmpty()) {
                        metaEdge.getSource().getMetaEdgesOutTree(view).remove(metaEdge);
                        metaEdge.getTarget().getMetaEdgesInTree(view).remove(metaEdge);
                    }
                }
            }
        }
    }

    private MetaEdgeImpl getMetaEdge(View view, AbstractNode source, AbstractNode target) {
        if (source == target) {
            return null;
        }
        return source.getMetaEdgesOutTree(view).getItem(target.getNumber());
    }

    private MetaEdgeImpl getMetaEdge(View view, AbstractEdge edge) {
        if (edge.isSelfLoop()) {
            return null;
        }
        AbstractNode sourceParent = treeStructure.getEnabledAncestorOrSelf(view, edge.getSource());
        AbstractNode targetParent = treeStructure.getEnabledAncestorOrSelf(view, edge.getTarget());

        if (sourceParent != null && targetParent != null && sourceParent != targetParent) {
            return getMetaEdge(view, sourceParent, targetParent);
        }
        return null;
    }

    public interface MetaEdgeBuilder {

        public void pushEdge(AbstractEdge edge, MetaEdgeImpl metaEdge);

        public void pullEdge(AbstractEdge edge, MetaEdgeImpl metaEdge);
    }
}
