/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.gephi.graph.dhns.core;

import java.util.HashMap;
import java.util.Map;
import org.gephi.utils.collection.avl.ParamAVLIterator;
import org.gephi.graph.api.Attributes;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
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
            nodeCopy.setEnabledInDegree(node.getEnabledInDegree());
            nodeCopy.setEnabledOutDegree(node.getEnabledOutDegree());
            nodeCopy.setEnabledMutualDegree(node.getEnabledMutualDegree());
            AbstractNode parentCopy = node.parent != null ? newStructure.getNodeAt(node.parent.getPre()) : null;
            newStructure.insertAsChild(nodeCopy, parentCopy);
            newGraphStructure.addToDictionnary(nodeCopy);
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
                        edgeCopy = factory.newEdge(edge.getEdgeData().getId(), sourceCopy, targetCopy, edge.getWeight(), edge.isDirected());
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
                    newGraphStructure.addToDictionnary(edgeCopy);
                }
            }
        }
        newGraphStructure.getMainView().setNodesEnabled(view.getNodesEnabled());
        newGraphStructure.getMainView().setEdgesCountTotal(view.getEdgesCountTotal());
        newGraphStructure.getMainView().setEdgesCountEnabled(view.getEdgesCountEnabled());
        newGraphStructure.getMainView().setMutualEdgesTotal(view.getMutualEdgesTotal());
        newGraphStructure.getMainView().setMutualEdgesEnabled(view.getMutualEdgesEnabled());
        destination.getWriteLock().unlock();
        dhns.getReadLock().unlock();
    }

    public void duplicateNodes(Dhns destination, Node[] nodes) {
        Map<AbstractNode, AbstractNode> nodeMap = new HashMap<AbstractNode, AbstractNode>();

         GraphFactoryImpl factory = destination.factory();
         Graph destGraph = destination.getGraph();
        dhns.getReadLock().lock();
        destination.getWriteLock().lock();

        //Nodes
        for(Node sourceNode : nodes) {
            AbstractNode absSourceNode = (AbstractNode)sourceNode;
            AbstractNode nodeCopy = factory.newNode(sourceNode.getNodeData().getId());
            destGraph.addNode(nodeCopy);
            duplicateNodeData((NodeDataImpl)sourceNode.getNodeData(), (NodeDataImpl)nodeCopy.getNodeData());
            nodeMap.put(absSourceNode, nodeCopy);
        }
        
        //Edges
        ParamAVLIterator<AbstractEdge> edgeIterator = new ParamAVLIterator<AbstractEdge>();
        for(Node sourceNode : nodes) {
            AbstractNode absSourceNode = (AbstractNode)sourceNode;
            AbstractNode nodeCopy = nodeMap.get(absSourceNode);
            int sourceView = absSourceNode.getViewId();
            if (!absSourceNode.getEdgesOutTree().isEmpty()) {
                for (edgeIterator.setNode(absSourceNode.getEdgesOutTree()); edgeIterator.hasNext();) {
                    AbstractEdge edge = edgeIterator.next();
                    AbstractNode originalTargetNode = edge.getTarget(sourceView);
                    AbstractNode copyTargetNode = nodeMap.get(originalTargetNode);
                    if(copyTargetNode!=null) {
                        AbstractEdge edgeCopy = factory.newEdge(edge.getEdgeData().getId(), nodeCopy, copyTargetNode, edge.getWeight(), edge.isDirected());
                        destGraph.addEdge(edgeCopy);
                        duplicateEdgeData(edge.getEdgeData(), edgeCopy.getEdgeData());
                    }
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
        dest.getTextData().setColor(source.getTextData().getR(), source.getTextData().getG(), source.getTextData().getB(), source.getTextData().getAlpha());
        dest.getTextData().setSize(source.getTextData().getSize());
        dest.getTextData().setVisible(source.getTextData().isVisible());

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
        dest.getTextData().setColor(source.getTextData().getR(), source.getTextData().getG(), source.getTextData().getB(), source.getTextData().getAlpha());
        dest.getTextData().setSize(source.getTextData().getSize());
        dest.getTextData().setVisible(source.getTextData().isVisible());

        //Attributes
        Attributes sourceAttributes = source.getAttributes();
        for (int i = 0; i < sourceAttributes.countValues(); i++) {
            dest.getAttributes().setValue(i, sourceAttributes.getValue(i));
        }
    }
}
