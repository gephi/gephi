/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
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
        duplicate(destination, dhns.getGraphStructure().getMainView());
    }

    public void duplicate(Dhns destination, GraphViewImpl view) {
        GraphFactoryImpl factory = destination.factory();
        dhns.readLock();
        destination.writeLock();
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

        //Metaedges
        newGraphStructure.getMainView().getStructureModifier().getEdgeProcessor().computeMetaEdges();
        destination.writeUnlock();
        dhns.readUnlock();
    }

    public void duplicateNodes(Dhns destination, Node[] nodes) {
        Map<AbstractNode, AbstractNode> nodeMap = new HashMap<AbstractNode, AbstractNode>();

        GraphFactoryImpl factory = destination.factory();
        Graph destGraph = null;
        if (dhns.isDirected()) {
            destGraph = destination.getDirectedGraph();
        } else if (dhns.isUndirected()) {
            destGraph = destination.getUndirectedGraph();
        } else {
            destGraph = destination.getMixedGraph();
        }
        dhns.readLock();
        destination.writeLock();

        //Nodes
        for (Node sourceNode : nodes) {
            AbstractNode absSourceNode = (AbstractNode) sourceNode;
            AbstractNode nodeCopy = factory.newNode(sourceNode.getNodeData().getId());
            destGraph.addNode(nodeCopy);
            duplicateNodeData((NodeDataImpl) sourceNode.getNodeData(), (NodeDataImpl) nodeCopy.getNodeData());
            nodeMap.put(absSourceNode, nodeCopy);
        }

        //Edges
        ParamAVLIterator<AbstractEdge> edgeIterator = new ParamAVLIterator<AbstractEdge>();
        for (Node sourceNode : nodes) {
            AbstractNode absSourceNode = (AbstractNode) sourceNode;
            AbstractNode nodeCopy = nodeMap.get(absSourceNode);
            int sourceView = absSourceNode.getViewId();
            if (!absSourceNode.getEdgesOutTree().isEmpty()) {
                for (edgeIterator.setNode(absSourceNode.getEdgesOutTree()); edgeIterator.hasNext();) {
                    AbstractEdge edge = edgeIterator.next();
                    AbstractNode originalTargetNode = edge.getTarget(sourceView);
                    AbstractNode copyTargetNode = nodeMap.get(originalTargetNode);
                    if (copyTargetNode != null) {
                        AbstractEdge edgeCopy = factory.newEdge(edge.getEdgeData().getId(), nodeCopy, copyTargetNode, edge.getWeight(), edge.isDirected());
                        destGraph.addEdge(edgeCopy);
                        duplicateEdgeData(edge.getEdgeData(), edgeCopy.getEdgeData());
                    }
                }
            }
            if (!absSourceNode.getMetaEdgesOutTree().isEmpty()) {
                for (edgeIterator.setNode(absSourceNode.getMetaEdgesOutTree()); edgeIterator.hasNext();) {
                    AbstractEdge edge = edgeIterator.next();
                    AbstractNode originalTargetNode = edge.getTarget(sourceView);
                    AbstractNode copyTargetNode = nodeMap.get(originalTargetNode);
                    if (copyTargetNode != null) {
                        AbstractEdge edgeCopy = factory.newEdge(edge.getEdgeData().getId(), nodeCopy, copyTargetNode, edge.getWeight(), edge.isDirected());
                        destGraph.addEdge(edgeCopy);
                        duplicateEdgeData(edge.getEdgeData(), edgeCopy.getEdgeData());
                    }
                }
            }
        }

        destination.writeUnlock();
        dhns.readUnlock();
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
