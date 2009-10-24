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
package org.gephi.graph.dhns.subgraph;

import org.gephi.datastructure.avl.param.ParamAVLIterator;
import org.gephi.graph.api.EdgePredicate;
import org.gephi.graph.api.NodePredicate;
import org.gephi.graph.api.Predicate;
import org.gephi.graph.dhns.core.GraphStructure;
import org.gephi.graph.dhns.core.TreeStructure;
import org.gephi.graph.dhns.edge.AbstractEdge;
import org.gephi.graph.dhns.edge.MixedEdgeImpl;
import org.gephi.graph.dhns.edge.ProperEdgeImpl;
import org.gephi.graph.dhns.edge.SelfLoopImpl;
import org.gephi.graph.dhns.node.AbstractNode;
import org.gephi.graph.dhns.node.CloneNode;
import org.gephi.graph.dhns.node.PreNode;
import org.gephi.graph.dhns.node.iterators.TreeListIterator;

/**
 *
 * @author Mathieu Bastian
 */
public class SubGraphManager {

    public static void filterSubGraph(GraphStructure graphStructure, Predicate predicate) {
        if (predicate instanceof NodePredicate) {
            filterSubGraph(graphStructure, (NodePredicate) predicate);
        } else if (predicate instanceof EdgePredicate) {
            filterSubGraph(graphStructure, (EdgePredicate) predicate);
        } else {
            throw new IllegalArgumentException("Predicate must be either NodePredicate or EdgePredicate");
        }
    }

    public static void filterSubGraph(GraphStructure graphStructure, NodePredicate nodePredicate) {

        TreeStructure treeStructure = graphStructure.getStructure();
        ParamAVLIterator<AbstractEdge> edgeIterator = new ParamAVLIterator<AbstractEdge>();
        for (TreeListIterator itr = new TreeListIterator(treeStructure.getTree(), 1); itr.hasNext();) {
            AbstractNode node = itr.next();
            if (!nodePredicate.evaluate(node)) {
                AbstractNode descendant = node;
                treeStructure.decrementAncestorSize(node, node.size);
                for (int i = 0; i <= node.size; i++) {       //Delete descendant and self
                    itr.remove();
                    descendant.parent = null;
                    //Clear edges
                    if (descendant.getEdgesInTree().getCount() > 0) {
                        for (edgeIterator.setNode(descendant.getEdgesInTree()); edgeIterator.hasNext();) {
                            AbstractEdge edge = edgeIterator.next();
                            edge.getSource().getEdgesOutTree().remove(edge);
                        }
                    }
                    if (i < node.size) {
                        itr.hasNext();
                        descendant = itr.next();
                    }
                }
            }
        }
    }

    public static void filterSubGraph(GraphStructure graphStructure, EdgePredicate edgePredicate) {

        ParamAVLIterator<AbstractEdge> edgeIterator = new ParamAVLIterator<AbstractEdge>();
        TreeStructure treeStructure = graphStructure.getStructure();
        for (TreeListIterator itr = new TreeListIterator(treeStructure.getTree(), 1); itr.hasNext();) {
            AbstractNode node = itr.next();
            if (!node.getEdgesOutTree().isEmpty()) {
                for (edgeIterator.setNode(node.getEdgesOutTree()); edgeIterator.hasNext();) {
                    AbstractEdge edge = edgeIterator.next();
                    if (!edgePredicate.evaluate(edge)) {
                        edge.getTarget().getEdgesInTree().remove(edge);
                        edgeIterator.remove();
                    }
                }
            }
        }
    }

    public static GraphStructure copyGraphStructure(GraphStructure graphStructure) {
        GraphStructure graphStructureCopy = new GraphStructure();
        TreeStructure treeStructureCopy = graphStructureCopy.getStructure();

        //Nodes
        for (TreeListIterator itr = new TreeListIterator(graphStructure.getStructure().getTree(), 1); itr.hasNext();) {
            AbstractNode node = itr.next();
            AbstractNode nodeCopy;
            if (node.isClone()) {
                nodeCopy = new CloneNode(treeStructureCopy.getNodeAt(node.getOriginalNode().getPre()));
            } else {
                nodeCopy = new PreNode((PreNode) node);
            }
            AbstractNode parentCopy = node.parent != null ? treeStructureCopy.getNodeAt(node.parent.getPre()) : null;
            treeStructureCopy.insertAsChild(nodeCopy, parentCopy);
            graphStructureCopy.getNodeDictionnary().add(nodeCopy);
        }

        //Edges
        ParamAVLIterator<AbstractEdge> edgeIterator = new ParamAVLIterator<AbstractEdge>();
        for (TreeListIterator itr = new TreeListIterator(graphStructure.getStructure().getTree(), 1); itr.hasNext();) {
            AbstractNode node = itr.next();
            if (!node.isClone() && !node.getEdgesOutTree().isEmpty()) {
                for (edgeIterator.setNode(node.getEdgesOutTree()); edgeIterator.hasNext();) {
                    AbstractEdge edge = edgeIterator.next();
                    AbstractEdge edgeCopy;
                    AbstractNode sourceCopy = treeStructureCopy.getNodeAt(edge.getSource().getPre());
                    AbstractNode targetCopy = treeStructureCopy.getNodeAt(edge.getTarget().getPre());
                    if (edge.isSelfLoop()) {
                        edgeCopy = new SelfLoopImpl(edge, sourceCopy);
                    } else if (edge.isMixed()) {
                        edgeCopy = new MixedEdgeImpl(edge, sourceCopy, targetCopy, edge.isDirected());
                    } else {
                        edgeCopy = new ProperEdgeImpl(edge, sourceCopy, targetCopy);
                    }
                    sourceCopy.getEdgesOutTree().add(edgeCopy);
                    targetCopy.getEdgesInTree().add(edgeCopy);
                    graphStructureCopy.getEdgeDictionnary().add(edgeCopy);
                }
            }
        }
        return graphStructureCopy;
    }
}
