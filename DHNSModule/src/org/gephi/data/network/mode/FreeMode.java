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
package org.gephi.data.network.mode;

import org.gephi.data.network.Dhns;
import org.gephi.data.network.api.FreeModifier;
import org.gephi.data.network.api.Sight;
import org.gephi.data.network.edge.EdgeImpl;
import org.gephi.data.network.edge.FreeEdgeProcessing;
import org.gephi.data.network.node.NodeImpl;
import org.gephi.data.network.node.PreNode;
import org.gephi.data.network.node.treelist.SightTreeIterator;
import org.gephi.data.network.sight.SightImpl;
import org.gephi.data.network.tree.TreeStructure;
import org.gephi.data.network.utils.avl.PreNodeAVLTree;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;

/**
 *
 * @author Mathieu Bastian
 */
public class FreeMode implements Mode,FreeModifier {

    private Dhns dhns;
    private FreeEdgeProcessing edgeProcessing;
    private TreeStructure treeStructure;

    public FreeMode(Dhns dhns)
    {
        this.dhns = dhns;
        this.treeStructure = dhns.getTreeStructure();
        edgeProcessing = new FreeEdgeProcessing(treeStructure);
    }

    public void init()
    {
        SightImpl sight = dhns.getSightManager().getMainSight();

        SightTreeIterator enabledNodes = new SightTreeIterator(treeStructure, sight);
        for (; enabledNodes.hasNext();) {
            PreNode n = enabledNodes.next();
            NodeImpl ni = n.getNode();
            ni.setX(n.getPre() * 50);
            ni.setY(n.getPost() * 50);
        }
        edgeProcessing.init(dhns.getSightManager().getMainSight());
    }

    public void expand(Node node, Sight sight) {
        if(node.getLevel()<treeStructure.treeHeight)
            expand(((NodeImpl) node).getPreNode(), (SightImpl)sight);
    }

    public void retract(Node node, Sight sight) {
        retract(((NodeImpl) node).getPreNode(), (SightImpl)sight);
    }

    public void addNode(Node node) {
        NodeImpl nodeImpl = (NodeImpl) node;
    }

    public void deleteNode(Node node) {
        NodeImpl nodeImpl = (NodeImpl) node;
    }

    public void addEdge(Edge edge) {
        EdgeImpl edgeImpl = (EdgeImpl) edge;
    }

    public void deleteEdge(Edge edge) {
        EdgeImpl edgeImpl = (EdgeImpl) edge;
    }

    //------------------------------------------
    private void expand(PreNode preNode, SightImpl sightImpl)
    {
        PreNodeAVLTree nodeToReprocess = new PreNodeAVLTree();

        //Enable children
        PreNode child = null;
        for (int i = preNode.pre + 1; i <= preNode.pre + preNode.size;) {
            child = treeStructure.getNodeAt(i);
            child.setEnabled(sightImpl, true);

            i += child.size + 1;
        }

        //Reprocess edge-hosting neighbour
        edgeProcessing.appendEdgeHostingNeighbours(preNode, nodeToReprocess, preNode.pre, sightImpl);
        edgeProcessing.reprocessInducedEdges(nodeToReprocess, preNode, sightImpl);

        //Process induced edges of direct children
        for (int i = preNode.pre + 1; i <= preNode.pre + preNode.size;) {
            child = treeStructure.getNodeAt(i);
            edgeProcessing.processLocalInducedEdges(child, sightImpl);
            i += child.size + 1;
        }

        //Clean current node
        preNode.setEnabled(sightImpl, false);
        edgeProcessing.clearVirtualEdges(preNode, sightImpl);

        sightImpl.getSightCache().reset();
    }

    private void retract(PreNode parent, SightImpl sight) {
        PreNodeAVLTree nodeToReprocess = new PreNodeAVLTree();

        //Enable node
        parent.setEnabled(sight, true);

        //Disable children
        PreNode child = null;
        for (int i = parent.pre + 1; i <= parent.pre + parent.size;) {
            child = treeStructure.getNodeAt(i);
            child.setEnabled(sight, false);
            edgeProcessing.appendEdgeHostingNeighbours(child, nodeToReprocess, parent.pre, sight);

            i += child.size + 1;
        }

        edgeProcessing.reprocessInducedEdges(nodeToReprocess, parent, sight);
        edgeProcessing.processLocalInducedEdges(parent, sight);

        for (int i = parent.pre + 1; i <= parent.pre + parent.size;) {
            child = treeStructure.getNodeAt(i);
            edgeProcessing.clearVirtualEdges(child, sight);
            i += child.size + 1;
        }
    }
}
