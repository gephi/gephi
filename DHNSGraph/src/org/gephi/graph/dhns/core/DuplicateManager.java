/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.graph.dhns.core;

import org.gephi.utils.collection.avl.ParamAVLIterator;
import org.gephi.graph.api.Attributes;
import org.gephi.graph.dhns.edge.AbstractEdge;
import org.gephi.graph.dhns.edge.EdgeDataImpl;
import org.gephi.graph.dhns.node.AbstractNode;
import org.gephi.graph.dhns.node.NodeDataImpl;
import org.gephi.graph.dhns.node.iterators.TreeListIterator;

/**
 *
 * @author Mathieu Bastian
 */
public class DuplicateManager {

    private final Dhns dhns;

    public DuplicateManager(Dhns dhns) {
        this.dhns = dhns;
    }

    public void duplicate(Dhns destination) {
//        duplicate(destination, dhns.getGraphStructure().getMainView());
    }

    public void duplicate(Dhns destination, GraphViewImpl view) {
        GraphFactoryImpl factory = destination.factory();
        dhns.getReadLock().lock();
        destination.getWriteLock().lock();
        TreeStructure treeStructure = view.getStructure();
        GraphStructure newGraphStructure = destination.getGraphStructure();
        TreeStructure newStructure = newGraphStructure.getMainView().getStructure();
        //Nodes
        for (TreeListIterator itr = new TreeListIterator(treeStructure.getTree(), 1); itr.hasNext();) {
            AbstractNode node = itr.next();
            AbstractNode nodeCopy = factory.newNode();
            duplicateNodeData(node.getNodeData(), nodeCopy.getNodeData());
            nodeCopy.setEnabled(node.isEnabled());
            AbstractNode parentCopy = node.parent != null ? newStructure.getNodeAt(node.parent.getPre()) : null;
            newStructure.insertAsChild(nodeCopy, parentCopy);
            newGraphStructure.getNodeDictionnary().add(nodeCopy);
        }

        //Edges
        ParamAVLIterator<AbstractEdge> edgeIterator = new ParamAVLIterator<AbstractEdge>();
        for (TreeListIterator itr = new TreeListIterator(treeStructure.getTree(), 1); itr.hasNext();) {
            AbstractNode node = itr.next();
            if (!node.getEdgesOutTree().isEmpty()) {
                for (edgeIterator.setNode(node.getEdgesOutTree()); edgeIterator.hasNext();) {
                    AbstractEdge edge = edgeIterator.next();
                    AbstractEdge edgeCopy;
                    AbstractNode sourceCopy = newStructure.getNodeAt(edge.getSource(view.getViewId()).getPre());
                    AbstractNode targetCopy = newStructure.getNodeAt(edge.getTarget(view.getViewId()).getPre());
                    if (edge.isMixed()) {
                        edgeCopy = factory.newEdge(sourceCopy, targetCopy, edge.getWeight(), edge.isDirected());
                        if (edge.isDirected()) {
                            destination.touchDirected();
                        } else {
                            destination.touchUndirected();
                        }
                    } else {
                        edgeCopy = factory.newEdge(sourceCopy, targetCopy);
                        edgeCopy.setWeight(edge.getWeight());
                        destination.touchDirected();
                    }
                    duplicateEdgeData(edge.getEdgeData(), edgeCopy.getEdgeData());
                    sourceCopy.getEdgesOutTree().add(edgeCopy);
                    targetCopy.getEdgesInTree().add(edgeCopy);
                    newGraphStructure.getEdgeDictionnary().add(edgeCopy);
                }
            }
        }
        destination.getWriteLock().unlock();
        dhns.getReadLock().unlock();
    }

    private void duplicateNodeData(NodeDataImpl source, NodeDataImpl dest) {
        dest.setX(source.x());
        dest.setY(source.y());
        dest.setZ(source.z());
        dest.setR(source.r());
        dest.setG(source.g());
        dest.setB(source.b());
        dest.setAlpha(source.alpha());
        dest.setSize(source.getSize());

        //Attributes
        Attributes sourceAttributes = source.getAttributes();
        for (int i = 0; i < sourceAttributes.countValues(); i++) {
            dest.getAttributes().setValue(i, sourceAttributes.getValue(i));
        }
    }

    private void duplicateEdgeData(EdgeDataImpl source, EdgeDataImpl dest) {
        dest.setR(source.r());
        dest.setG(source.g());
        dest.setB(source.b());
        dest.setAlpha(source.alpha());

        //Attributes
        Attributes sourceAttributes = source.getAttributes();
        for (int i = 0; i < sourceAttributes.countValues(); i++) {
            dest.getAttributes().setValue(i, sourceAttributes.getValue(i));
        }
    }
}
