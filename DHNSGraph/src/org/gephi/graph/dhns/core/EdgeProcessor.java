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

import org.gephi.graph.api.GraphEvent.EventType;
import org.gephi.utils.collection.avl.ParamAVLIterator;
import org.gephi.graph.dhns.edge.AbstractEdge;
import org.gephi.graph.dhns.edge.MetaEdgeImpl;
import org.gephi.graph.dhns.event.EdgeEvent;
import org.gephi.graph.dhns.node.AbstractNode;
import org.gephi.graph.dhns.node.iterators.TreeIterator;
import org.gephi.graph.dhns.node.iterators.TreeListIterator;
import org.gephi.graph.dhns.predicate.Tautology;

/**
 * Business class for managing Edges and MetaEdges.
 *
 * @author Mathieu Bastian
 */
public class EdgeProcessor {

    //Architecture
    private final TreeStructure treeStructure;
    private final IDGen idGen;
    private final Dhns dhns;
    private final GraphViewImpl view;
    private final int viewId;
    //Cache
    private ParamAVLIterator<AbstractEdge> edgeIterator;

    public EdgeProcessor(Dhns dhns, GraphViewImpl view) {
        this.dhns = dhns;
        this.treeStructure = view.getStructure();
        this.idGen = dhns.getIdGen();
        this.view = view;
        this.viewId = view.getViewId();
        this.edgeIterator = new ParamAVLIterator<AbstractEdge>();
    }

    public AbstractEdge[] clearEdges(AbstractNode node) {
        int edgesCount = node.getEdgesInTree().getCount() + node.getEdgesOutTree().getCount();
        if (edgesCount == 0) {
            return null;
        }
        AbstractEdge[] clearedEdges = new AbstractEdge[edgesCount];
        int i = 0;

        if (node.getEdgesInTree().getCount() > 0) {
            edgeIterator.setNode(node.getEdgesInTree());
            while (edgeIterator.hasNext()) {
                AbstractEdge edge = edgeIterator.next();
                removeEdgeFromMetaEdge(edge);
                AbstractNode source = edge.getSource(viewId);
                view.decEdgesCountTotal(1);
                boolean mutual = !edge.isSelfLoop() && node.getEdgesOutTree().hasNeighbour(source);
                if (node.isEnabled() && source.isEnabled()) {
                    view.decEdgesCountEnabled(1);
                    node.decEnabledInDegree();
                    source.decEnabledOutDegree();
                    if (mutual) {
                        source.decEnabledMutualDegree();
                        node.decEnabledMutualDegree();
                        view.decMutualEdgesEnabled(1);
                    }
                }
                if (mutual) {
                    view.decMutualEdgesTotal(1);
                }

                source.getEdgesOutTree().remove(edge);
                clearedEdges[i] = edge;
                i++;
            }
            node.getEdgesInTree().clear();
        }

        if (node.getEdgesOutTree().getCount() > 0) {
            edgeIterator.setNode(node.getEdgesOutTree());
            while (edgeIterator.hasNext()) {
                AbstractEdge edge = edgeIterator.next();
                removeEdgeFromMetaEdge(edge);
                AbstractNode target = edge.getTarget(viewId);

                if (!edge.isSelfLoop()) {
                    view.decEdgesCountTotal(1);
                    if (node.isEnabled()) {
                        node.decEnabledOutDegree();
                        target.decEnabledInDegree();
                        view.decEdgesCountEnabled(1);
                    }
                }

                edge.getTarget(viewId).getEdgesInTree().remove(edge);
                clearedEdges[i] = edge;
                i++;
            }
            node.getEdgesOutTree().clear();
        }
        return clearedEdges;
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
                edge.getSource(viewId).getMetaEdgesOutTree().remove((MetaEdgeImpl) edge);
            }
            node.getMetaEdgesInTree().clear();
        }

        if (node.getMetaEdgesOutTree().getCount() > 0) {
            edgeIterator.setNode(node.getMetaEdgesOutTree());
            while (edgeIterator.hasNext()) {
                AbstractEdge edge = edgeIterator.next();
                edge.getTarget(viewId).getMetaEdgesInTree().remove((MetaEdgeImpl) edge);
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
                int targetPre = metaEdge.getTarget(viewId).getPre();
                if (targetPre >= rangeStart && targetPre <= rangeLimit) {
                    //The meta edge has to be removed because it's in the range
                    edgeIterator.remove();
                    metaEdge.getTarget(viewId).getMetaEdgesInTree().remove(metaEdge);
                }
            }
        }

        if (enabledNode.getMetaEdgesInTree().getCount() > 0) {
            edgeIterator.setNode(enabledNode.getMetaEdgesInTree());
            while (edgeIterator.hasNext()) {
                MetaEdgeImpl metaEdge = (MetaEdgeImpl) edgeIterator.next();
                int sourcePre = metaEdge.getSource(viewId).getPre();
                if (sourcePre >= rangeStart && sourcePre <= rangeLimit) {
                    //The meta edge has to be removed because it's in the range
                    edgeIterator.remove();
                    metaEdge.getSource(viewId).getMetaEdgesOutTree().remove(metaEdge);
                }
            }
        }
    }

    public void clearAllEdges() {
        for (TreeListIterator itr = new TreeListIterator(treeStructure.getTree()); itr.hasNext();) {
            AbstractNode node = itr.next();
            node.getEdgesInTree().clear();
            node.getEdgesOutTree().clear();
            node.setEnabledInDegree(0);
            node.setEnabledOutDegree(0);
            node.setEnabledMutualDegree(0);
            node.clearMetaEdges();
        }
        view.setEdgesCountTotal(0);
        view.setEdgesCountEnabled(0);
        view.setMutualEdgesEnabled(0);
        view.setMutualEdgesTotal(0);
        dhns.getGraphStructure().clearEdgeDictionnary();
    }

    public void clearAllMetaEdges() {
        for (TreeListIterator itr = new TreeListIterator(treeStructure.getTree()); itr.hasNext();) {
            AbstractNode node = itr.next();
            node.clearMetaEdges();
        }
    }

    public void computeMetaEdges(AbstractNode node, AbstractNode enabledAncestor) {
        if (!dhns.getSettingsManager().isAutoMetaEdgeCreation()) {
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
                    AbstractNode[] enabledAncestors = treeStructure.getEnabledAncestorsOrSelf(edge.getTarget(viewId));
                    if (enabledAncestors != null) {
                        for (int j = 0; j < enabledAncestors.length; j++) {
                            AbstractNode targetNode = enabledAncestors[j];
                            if (!(targetNode == edge.getTarget(viewId) && enabledAncestor == edge.getSource(viewId))) {
                                createMetaEdge(enabledAncestor, targetNode, edge);
                            }
                        }
                    }

//                    AbstractNode targetNode = treeStructure.getEnabledAncestorOrSelf(edge.getTarget(viewId));
//                    if (targetNode != null && !(targetNode == edge.getTarget(viewId) && enabledAncestor == edge.getSource(viewId))) {
//                    //Create Meta Edge if not exist
//                    createMetaEdge(enabledAncestor, targetNode, edge);
//                    }
                }
            }
            if (desc.getEdgesInTree().getCount() > 0) {
                edgeIterator.setNode(desc.getEdgesInTree());
                while (edgeIterator.hasNext()) {
                    AbstractEdge edge = edgeIterator.next();
                    AbstractNode[] enabledAncestors = treeStructure.getEnabledAncestorsOrSelf(edge.getSource(viewId));
                    if (enabledAncestors != null) {
                        for (int j = 0; j < enabledAncestors.length; j++) {
                            AbstractNode sourceNode = enabledAncestors[j];
                            if (!(sourceNode == edge.getSource(viewId) && enabledAncestor == edge.getTarget(viewId))) {
                                createMetaEdge(sourceNode, enabledAncestor, edge);
                            }
                        }
                    }
//                    AbstractNode sourceNode = treeStructure.getEnabledAncestorOrSelf(edge.getSource(viewId));
//                    if (sourceNode != null && !(sourceNode == edge.getSource(viewId) && enabledAncestor == edge.getTarget(viewId))) {
//                        //Create Meta Edge if not exist
//                        createMetaEdge(sourceNode, enabledAncestor, edge);
//                    }
                }
            }
        }
    }

    public void computeMetaEdges() {
        for (TreeIterator itr = new TreeIterator(treeStructure, true, Tautology.instance); itr.hasNext();) {
            AbstractNode node = itr.next();
            computeMetaEdges(node, node);
        }
    }

    private void createMetaEdge(AbstractNode source, AbstractNode target, AbstractEdge edge) {
        AbstractNode edgeSource = edge.getSource(viewId);
        AbstractNode edgeTarget = edge.getTarget(viewId);
        if (edgeSource == source && edgeTarget == target) {
            return;
        }
        if (source == target) {
            return;
        }

        MetaEdgeImpl metaEdge = getMetaEdge(source, target);
        if (metaEdge == null) {
            metaEdge = createMetaEdge(source, target);
        }
        if (metaEdge != null) {
            if (metaEdge.addEdge(edge)) {
                dhns.getSettingsManager().getMetaEdgeBuilder().pushEdge(edge, edgeSource, edgeTarget, metaEdge);
            }
        }
    }

    private MetaEdgeImpl createMetaEdge(AbstractNode source, AbstractNode target) {
        if (source == target) {
            return null;
        }
        MetaEdgeImpl newEdge = dhns.factory().newMetaEdge(source, target);
        source.getMetaEdgesOutTree().add(newEdge);
        target.getMetaEdgesInTree().add(newEdge);
        return newEdge;
    }

    public void createMetaEdge(AbstractEdge edge) {
        if (!dhns.getSettingsManager().isAutoMetaEdgeCreation()) {
            return;
        }
        if (edge.isSelfLoop()) {
            return;
        }

        AbstractNode[] sourceAncestors = treeStructure.getEnabledAncestorsOrSelf(edge.getSource(viewId));
        AbstractNode[] targetAncestors = treeStructure.getEnabledAncestorsOrSelf(edge.getTarget(viewId));

        if (sourceAncestors != null && targetAncestors != null) {
            for (int i = 0; i < sourceAncestors.length; i++) {
                for (int j = 0; j < targetAncestors.length; j++) {
                    AbstractNode sourceParent = sourceAncestors[i];
                    AbstractNode targetParent = targetAncestors[j];
                    if (sourceParent != targetParent) {
                        createMetaEdge(sourceParent, targetParent, edge);
                    }
                }
            }
        }

//        AbstractNode sourceParent = treeStructure.getEnabledAncestorOrSelf(edge.getSource(viewId));
//        AbstractNode targetParent = treeStructure.getEnabledAncestorOrSelf(edge.getTarget(viewId));
//
//        if (sourceParent != null && targetParent != null && sourceParent != targetParent) {
//            createMetaEdge(sourceParent, targetParent, edge);
//        }
    }

    public void removeEdgeFromMetaEdge(AbstractEdge edge) {
        if (!dhns.getSettingsManager().isAutoMetaEdgeCreation()) {
            return;
        }
        if (edge.isSelfLoop()) {
            return;
        }


        MetaEdgeImpl metaEdge = getMetaEdge(edge);
        if (metaEdge != null) {
            if (metaEdge.removeEdge(edge)) {
                AbstractNode edgeSource = edge.getSource(viewId);
                AbstractNode edgeTarget = edge.getTarget(viewId);
                dhns.getSettingsManager().getMetaEdgeBuilder().pullEdge(edge, edgeSource, edgeTarget, metaEdge);
            }
            if (metaEdge.isEmpty()) {
                metaEdge.getSource(viewId).getMetaEdgesOutTree().remove(metaEdge);
                metaEdge.getTarget(viewId).getMetaEdgesInTree().remove(metaEdge);
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
        AbstractNode sourceParent = treeStructure.getEnabledAncestorOrSelf(edge.getSource(viewId));
        AbstractNode targetParent = treeStructure.getEnabledAncestorOrSelf(edge.getTarget(viewId));

        if (sourceParent != null && targetParent != null && sourceParent != targetParent) {
            return getMetaEdge(sourceParent, targetParent);
        }
        return null;
    }

    public void incrementEdgesCounting(AbstractNode enabledNode, AbstractNode parent) {
        for (edgeIterator.setNode(enabledNode.getEdgesOutTree()); edgeIterator.hasNext();) {
            AbstractEdge edge = edgeIterator.next();
            AbstractNode target = edge.getTarget(view.getViewId());
            if (target.isEnabled()) {
                view.incEdgesCountEnabled(1);
                enabledNode.incEnabledOutDegree();
                target.incEnabledInDegree();
                if (target.getEdgesOutTree().hasNeighbour(enabledNode)) {
                    if (parent == null || (parent != null && target.parent != parent) || (parent != null && target.parent == parent && target.getId() < enabledNode.getId())) {
                        view.incMutualEdgesEnabled(1);
                        enabledNode.incEnabledMutualDegree();
                        target.incEnabledMutualDegree();
                    }
                }
            }
        }
        for (edgeIterator.setNode(enabledNode.getEdgesInTree()); edgeIterator.hasNext();) {
            AbstractEdge edge = edgeIterator.next();
            AbstractNode source = edge.getSource(view.getViewId());
            if (source.isEnabled() && (parent == null || source.parent != parent)) {
                view.incEdgesCountEnabled(1);
                enabledNode.incEnabledInDegree();
                source.incEnabledOutDegree();
            }
        }
    }

    public void decrementEdgesCouting(AbstractNode disabledNode, AbstractNode parent) {
        for (edgeIterator.setNode(disabledNode.getEdgesOutTree()); edgeIterator.hasNext();) {
            AbstractEdge edge = edgeIterator.next();
            AbstractNode target = edge.getTarget(view.getViewId());
            if (target.isEnabled() || (parent != null && target.parent == parent)) {
                target.decEnabledInDegree();
                disabledNode.decEnabledOutDegree();
                view.decEdgesCountEnabled(1);
                if (target.getEdgesOutTree().hasNeighbour(disabledNode) && (parent == null || (parent != null && target.parent == parent && target.getId() < disabledNode.getId()))) {
                    target.decEnabledMutualDegree();
                    disabledNode.decEnabledMutualDegree();
                    view.decMutualEdgesEnabled(1);
                }
            }
        }
        for (edgeIterator.setNode(disabledNode.getEdgesInTree()); edgeIterator.hasNext();) {
            AbstractEdge edge = edgeIterator.next();
            AbstractNode source = edge.getSource(view.getViewId());
            if (source.isEnabled()) {
                view.decEdgesCountEnabled(1);
                disabledNode.decEnabledInDegree();
                source.decEnabledOutDegree();
            }
        }
    }

    public void resetEdgesCounting(AbstractNode node) {
        node.setEnabledInDegree(0);
        node.setEnabledOutDegree(0);
        node.setEnabledMutualDegree(0);
    }

    public void computeEdgesCounting(AbstractNode node) {
        for (edgeIterator.setNode(node.getEdgesOutTree()); edgeIterator.hasNext();) {
            AbstractEdge edge = edgeIterator.next();
            AbstractNode target = edge.getTarget(view.getViewId());
            if (target.isEnabled()) {
                target.incEnabledInDegree();
                node.incEnabledOutDegree();
                view.incEdgesCountEnabled(1);
                if (target.getEdgesOutTree().hasNeighbour(node) && target.getId() < node.getId()) {
                    target.incEnabledMutualDegree();
                    node.incEnabledMutualDegree();
                    view.incMutualEdgesEnabled(1);
                }
            }
        }
    }
}
