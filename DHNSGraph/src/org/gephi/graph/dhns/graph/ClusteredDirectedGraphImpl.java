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
package org.gephi.graph.dhns.graph;

import java.util.Iterator;
import org.gephi.graph.api.ClusteredDirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.graph.dhns.core.Dhns;
import org.gephi.graph.dhns.edge.AbstractEdge;
import org.gephi.graph.dhns.edge.EdgeImpl;
import org.gephi.graph.dhns.edge.MetaEdgeImpl;
import org.gephi.graph.dhns.edge.iterators.EdgeIterator;
import org.gephi.graph.dhns.edge.iterators.EdgeNodeIterator;
import org.gephi.graph.dhns.edge.iterators.MetaEdgeIterator;
import org.gephi.graph.dhns.edge.iterators.MetaEdgeNodeIterator;
import org.gephi.graph.dhns.edge.iterators.RangeEdgeIterator;
import org.gephi.graph.dhns.edge.iterators.VisibleEdgeIterator;
import org.gephi.graph.dhns.edge.iterators.VisibleEdgeNodeIterator;
import org.gephi.graph.dhns.edge.iterators.VisibleMetaEdgeIterator;
import org.gephi.graph.dhns.edge.iterators.VisibleMetaEdgeNodeIterator;
import org.gephi.graph.dhns.edge.iterators.VisibleRangeEdgeIterator;
import org.gephi.graph.dhns.node.AbstractNode;
import org.gephi.graph.dhns.node.PreNode;
import org.gephi.graph.dhns.node.iterators.ChildrenIterator;
import org.gephi.graph.dhns.node.iterators.CompleteTreeIterator;
import org.gephi.graph.dhns.node.iterators.DescendantIterator;
import org.gephi.graph.dhns.node.iterators.NeighborIterator;
import org.gephi.graph.dhns.node.iterators.TreeListIterator;
import org.gephi.graph.dhns.node.iterators.VisibleChildrenIterator;
import org.gephi.graph.dhns.node.iterators.VisibleDescendantIterator;
import org.gephi.graph.dhns.node.iterators.VisibleTreeIterator;

/**
 * Implementation of clustered directed graph.
 *
 * @author Mathieu Bastian
 */
public class ClusteredDirectedGraphImpl implements ClusteredDirectedGraph {

    private Dhns dhns;
    private boolean visible = true;

    public ClusteredDirectedGraphImpl(Dhns dhns, boolean visible) {
        this.dhns = dhns;
        this.visible = visible;
    }

    public void addEdge(Node source, Node target) {
        AbstractEdge edge = dhns.getGraphFactory().newEdge(source, target);
        dhns.getStructureModifier().addEdge(edge);
    }

    public void addEdge(Edge edge) {
        if (edge == null) {
            throw new NullPointerException();
        }
        AbstractEdge e = (AbstractEdge) edge;
        if (!e.hasAttributes()) {
            e.setAttributes(dhns.newEdgeAttributes());
        }
        dhns.getStructureModifier().addEdge(edge);
    }

    public void addNode(Node node) {
        if (node == null) {
            throw new NullPointerException();
        }
        AbstractNode n = (AbstractNode) node;
        if (!n.hasAttributes()) {
            n.setAttributes(dhns.newNodeAttributes());
        }
        dhns.getStructureModifier().addNode(node, null);
    }

    public NodeIterable getSuccessors(Node node) {
        if (node == null) {
            throw new NullPointerException();
        }
        readLock();
        PreNode preNode = (PreNode) node;
        if (visible) {
            return dhns.newNodeIterable(new NeighborIterator(new VisibleEdgeNodeIterator(preNode, VisibleEdgeNodeIterator.EdgeNodeIteratorMode.OUT), preNode));
        } else {
            return dhns.newNodeIterable(new NeighborIterator(new EdgeNodeIterator(preNode, EdgeNodeIterator.EdgeNodeIteratorMode.OUT), preNode));
        }
    }

    public NodeIterable getPredecessors(Node node) {
        if (node == null) {
            throw new NullPointerException();
        }
        readLock();
        PreNode preNode = (PreNode) node;
        if (visible) {
            return dhns.newNodeIterable(new NeighborIterator(new VisibleEdgeNodeIterator(preNode, VisibleEdgeNodeIterator.EdgeNodeIteratorMode.IN), preNode));
        } else {
            return dhns.newNodeIterable(new NeighborIterator(new EdgeNodeIterator(preNode, EdgeNodeIterator.EdgeNodeIteratorMode.IN), preNode));
        }
    }

    public boolean isSuccessor(Node node, Node successor) {
        return getEdge(node, successor) != null;
    }

    public boolean isPredecessor(Node node, Node predecessor) {
        return getEdge(predecessor, node) != null;
    }

    public int getInDegree(Node node) {
        if (node == null) {
            throw new NullPointerException();
        }
        readLock();
        PreNode preNode = (PreNode) node;
        int count = 0;
        if (visible && !preNode.getEdgesInTree().isEmpty()) {
            for (Iterator<EdgeImpl> itr = preNode.getEdgesInTree().iterator(); itr.hasNext();) {
                if (itr.next().isVisible()) {
                    count++;
                }
            }
        } else {
            count = preNode.getEdgesInTree().getCount();
        }
        readUnlock();
        return count;
    }

    public int getOutDegree(Node node) {
        if (node == null) {
            throw new NullPointerException();
        }
        readLock();
        PreNode preNode = (PreNode) node;
        int count = 0;
        if (visible && !preNode.getEdgesInTree().isEmpty()) {
            for (Iterator<EdgeImpl> itr = preNode.getEdgesOutTree().iterator(); itr.hasNext();) {
                if (itr.next().isVisible()) {
                    count++;
                }
            }
        } else {
            count = preNode.getEdgesOutTree().getCount();
        }
        readUnlock();
        return count;
    }

    public boolean contains(Node node) {
        if (node == null) {
            throw new NullPointerException();
        }
        readLock();
        PreNode preNode = (PreNode) node;
        boolean res = false;
        if (visible) {
            if (preNode.isVisible() && dhns.getTreeStructure().getTree().contains(preNode)) {
                res = true;
            }
        } else {
            if (dhns.getTreeStructure().getTree().contains(preNode)) {
                res = true;
            }
        }
        readUnlock();
        return res;
    }

    public boolean contains(Edge edge) {
        if (edge == null) {
            throw new NullPointerException();
        }
        readLock();
        boolean res = false;
        if (visible) {
            res = contains(edge.getSource()) && contains(edge.getTarget()) && edge.isVisible();
        } else {
            res = contains(edge.getSource()) && contains(edge.getTarget());
        }
        readUnlock();
        return res;
    }

    public NodeIterable getNodes() {
        readLock();
        if (visible) {
            return dhns.newNodeIterable(new VisibleTreeIterator(dhns.getTreeStructure()));
        } else {
            return dhns.newNodeIterable(new CompleteTreeIterator(dhns.getTreeStructure()));
        }
    }

    public EdgeIterable getEdges() {
        readLock();
        if (visible) {
            return dhns.newEdgeIterable(new VisibleEdgeIterator(dhns.getTreeStructure(), new VisibleTreeIterator(dhns.getTreeStructure())));
        } else {
            return dhns.newEdgeIterable(new EdgeIterator(dhns.getTreeStructure(), new CompleteTreeIterator(dhns.getTreeStructure())));
        }
    }

    public EdgeIterable getInEdges(Node node) {
        if (node == null) {
            throw new NullPointerException();
        }
        readLock();
        PreNode preNode = (PreNode) node;
        if (visible) {
            return dhns.newEdgeIterable(new VisibleEdgeNodeIterator(preNode, VisibleEdgeNodeIterator.EdgeNodeIteratorMode.IN));
        } else {
            return dhns.newEdgeIterable(new EdgeNodeIterator(preNode, EdgeNodeIterator.EdgeNodeIteratorMode.IN));
        }
    }

    public EdgeIterable getOutEdges(Node node) {
        if (node == null) {
            throw new NullPointerException();
        }
        readLock();
        PreNode preNode = (PreNode) node;
        if (visible) {
            return dhns.newEdgeIterable(new VisibleEdgeNodeIterator(preNode, VisibleEdgeNodeIterator.EdgeNodeIteratorMode.OUT));
        } else {
            return dhns.newEdgeIterable(new EdgeNodeIterator(preNode, EdgeNodeIterator.EdgeNodeIteratorMode.OUT));
        }
    }

    public EdgeIterable getEdges(Node node) {
        if (node == null) {
            throw new NullPointerException();
        }
        readLock();
        PreNode preNode = (PreNode) node;
        if (visible) {
            return dhns.newEdgeIterable(new VisibleEdgeNodeIterator(preNode, VisibleEdgeNodeIterator.EdgeNodeIteratorMode.BOTH));
        } else {
            return dhns.newEdgeIterable(new EdgeNodeIterator(preNode, EdgeNodeIterator.EdgeNodeIteratorMode.BOTH));
        }
    }

    public NodeIterable getNeigbors(Node node) {
        if (node == null) {
            throw new NullPointerException();
        }
        readLock();
        PreNode preNode = (PreNode) node;
        if (visible) {
            return dhns.newNodeIterable(new NeighborIterator(new VisibleEdgeNodeIterator(preNode, VisibleEdgeNodeIterator.EdgeNodeIteratorMode.BOTH), preNode));
        } else {
            return dhns.newNodeIterable(new NeighborIterator(new EdgeNodeIterator(preNode, EdgeNodeIterator.EdgeNodeIteratorMode.BOTH), preNode));
        }
    }

    public Edge getEdge(Node source, Node target) {
        if (source == null || target == null) {
            throw new NullPointerException();
        }
        readLock();
        PreNode sourceNode = (PreNode) source;
        PreNode targetNode = (PreNode) target;
        EdgeImpl res = null;
        if (visible) {
            EdgeImpl edge = sourceNode.getEdgesOutTree().getItem(targetNode.getNumber());
            if (edge != null && edge.isVisible()) {
                res = edge;
            }
        } else {
            res = sourceNode.getEdgesOutTree().getItem(targetNode.getNumber());
        }
        readUnlock();
        return res;
    }

    public int getNodeCount() {
        readLock();
        int count = 0;
        if (visible) {
            for (TreeListIterator itr = new TreeListIterator(dhns.getTreeStructure().getTree()); itr.hasNext();) {
                if (itr.next().isVisible()) {
                    count++;
                }
            }
        } else {
            count = dhns.getTreeStructure().getTreeSize();
        }
        readUnlock();
        return count;
    }

    public int getEdgeCount() {
        readLock();
        int count = 0;
        if (visible) {
            for (VisibleEdgeIterator itr = new VisibleEdgeIterator(dhns.getTreeStructure(), new VisibleTreeIterator(dhns.getTreeStructure())); itr.hasNext();) {
                itr.next();
                count++;
            }
        } else {
            for (EdgeIterator itr = new EdgeIterator(dhns.getTreeStructure(), new CompleteTreeIterator(dhns.getTreeStructure())); itr.hasNext();) {
                itr.next();
                count++;
            }
        }
        readUnlock();
        return count;
    }

    public Node getOpposite(Node node, Edge edge) {
        if (node == null || edge == null) {
            throw new NullPointerException();
        }
        if (edge.getSource() == node) {
            return edge.getTarget();
        } else if (edge.getTarget() == node) {
            return edge.getSource();
        }
        throw new IllegalArgumentException("Node must be either source or target of the edge.");
    }

    public int getDegree(Node node) {
        return getInDegree(node) + getOutDegree(node);
    }

    public boolean isSelfLoop(Edge edge) {
        if (edge == null) {
            throw new NullPointerException();
        }
        return edge.getSource() == edge.getTarget();
    }

    public boolean isNeighbor(Node node, Node neighbor) {
        return isSuccessor(node, neighbor) || isPredecessor(node, neighbor);
    }

    public void removeEdge(Edge edge) {
        if (edge == null) {
            throw new NullPointerException();
        }
        dhns.getStructureModifier().deleteEdge(edge);
    }

    public void removeNode(Node node) {
        if (node == null) {
            throw new NullPointerException();
        }
        dhns.getStructureModifier().deleteNode(node);
    }

    public void clear() {
        dhns.getStructureModifier().clear();
    }

    public void clearEdges() {
        dhns.getStructureModifier().clearEdges();
    }

    public void clearEdges(Node node) {
        if (node == null) {
            throw new NullPointerException();
        }
        dhns.getStructureModifier().clearEdges(node);
    }

    public void clearMetaEdges(Node node) {
        if (node == null) {
            throw new NullPointerException();
        }
        dhns.getStructureModifier().clearMetaEdges(node);
    }

    public void addNode(Node node, Node parent) {
        if (node == null) {
            throw new NullPointerException();
        }
        AbstractNode n = (AbstractNode) node;
        if (!n.hasAttributes()) {
            n.setAttributes(dhns.newNodeAttributes());
        }
        dhns.getStructureModifier().addNode(node, parent);
    }

    public int getChildrenCount(Node node) {
        if (node == null) {
            throw new NullPointerException();
        }
        readLock();
        PreNode preNode = (PreNode) node;
        int count = 0;
        ChildrenIterator itr = new ChildrenIterator(dhns.getTreeStructure(), preNode);
        for (; itr.hasNext();) {
            PreNode child = itr.next();
            if (!visible || child.isVisible()) {
                count++;
            }
        }
        readUnlock();
        return count;
    }

    public Node getParent(Node node) {
        if (node == null) {
            throw new NullPointerException();
        }
        readLock();
        PreNode preNode = (PreNode) node;
        if (preNode.parent == dhns.getTreeStructure().getRoot()) {
            return null;
        }
        Node parent = preNode.parent;
        readUnlock();
        return parent;
    }

    public NodeIterable getChildren(Node node) {
        if (node == null) {
            throw new NullPointerException();
        }
        readLock();
        PreNode preNode = (PreNode) node;
        if (visible) {
            return dhns.newNodeIterable(new VisibleChildrenIterator(dhns.getTreeStructure(), preNode));
        } else {
            return dhns.newNodeIterable(new ChildrenIterator(dhns.getTreeStructure(), preNode));
        }
    }

    public NodeIterable getDescendant(Node node) {
        if (node == null) {
            throw new NullPointerException();
        }
        readLock();
        PreNode preNode = (PreNode) node;
        if (visible) {
            return dhns.newNodeIterable(new VisibleDescendantIterator(dhns.getTreeStructure(), preNode));
        } else {
            return dhns.newNodeIterable(new DescendantIterator(dhns.getTreeStructure(), preNode));
        }
    }

    public EdgeIterable getInnerEdges(Node nodeGroup) {
        if (nodeGroup == null) {
            throw new NullPointerException();
        }
        readLock();
        PreNode preNode = (PreNode) nodeGroup;
        if (visible) {
            return dhns.newEdgeIterable(new VisibleRangeEdgeIterator(dhns.getTreeStructure(), preNode, preNode, true));
        } else {
            return dhns.newEdgeIterable(new RangeEdgeIterator(dhns.getTreeStructure(), preNode, preNode, true));
        }
    }

    public EdgeIterable getOuterEdges(Node nodeGroup) {
        if (nodeGroup == null) {
            throw new NullPointerException();
        }
        readLock();
        PreNode preNode = (PreNode) nodeGroup;
        if (visible) {
            return dhns.newEdgeIterable(new VisibleRangeEdgeIterator(dhns.getTreeStructure(), preNode, preNode, false));
        } else {
            return dhns.newEdgeIterable(new RangeEdgeIterator(dhns.getTreeStructure(), preNode, preNode, false));
        }
    }

    public NodeIterable getTopNodes() {
        readLock();
        if (visible) {
            return dhns.newNodeIterable(new VisibleChildrenIterator(dhns.getTreeStructure()));
        } else {
            return dhns.newNodeIterable(new ChildrenIterator(dhns.getTreeStructure()));
        }
    }

    public boolean isDescendant(Node node, Node descendant) {
        if (node == null || descendant == null) {
            throw new NullPointerException();
        }
        readLock();
        PreNode preNode = (PreNode) node;
        PreNode preDesc = (PreNode) descendant;
        boolean res = preDesc.getPre() > preNode.getPre() && preDesc.getPost() < preNode.getPost();
        readUnlock();
        return res;
    }

    public boolean isAncestor(Node node, Node ancestor) {
        return isDescendant(ancestor, node);
    }

    public boolean isFollowing(Node node, Node following) {
        if (node == null || following == null) {
            throw new NullPointerException();
        }
        readLock();
        PreNode preNode = (PreNode) node;
        PreNode preFoll = (PreNode) following;
        boolean res = preFoll.getPre() > preNode.getPre() && preFoll.getPost() > preNode.getPost();
        readUnlock();
        return res;
    }

    public boolean isPreceding(Node node, Node preceding) {
        return isFollowing(preceding, node);
    }

    public boolean isParent(Node node, Node parent) {
        if (node == null || parent == null) {
            throw new NullPointerException();
        }
        readLock();
        boolean res = ((PreNode) node).parent == parent;
        readUnlock();
        return res;
    }

    public int getHeight() {
        readLock();
        int res = dhns.getTreeStructure().treeHeight;
        readUnlock();
        return res;
    }

    public int getLevel(Node node) {
        readLock();
        int res = ((PreNode) node).level;
        readUnlock();
        return res;
    }

    public void expand(Node node) {
        if (node == null) {
            throw new NullPointerException();
        }
        dhns.getStructureModifier().expand(node);
    }

    public void retract(Node node) {
        if (node == null) {
            throw new NullPointerException();
        }
        dhns.getStructureModifier().retract(node);
    }

    public void addToGroup(Node node, Node nodeGroup) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeFromGroup(Node node) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void groupNodes(Node[] nodes) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void ungroupNodes(Node[] nodes) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public EdgeIterable getMetaEdges() {
        readLock();
        if (visible) {
            return dhns.newEdgeIterable(new VisibleMetaEdgeIterator(dhns.getTreeStructure(), new VisibleTreeIterator(dhns.getTreeStructure())));
        } else {
            return dhns.newEdgeIterable(new MetaEdgeIterator(dhns.getTreeStructure(), new CompleteTreeIterator(dhns.getTreeStructure())));
        }
    }

    public EdgeIterable getMetaEdges(Node node) {
        if (node == null) {
            throw new NullPointerException();
        }
        readLock();
        PreNode preNode = (PreNode) node;
        if (visible) {
            return dhns.newEdgeIterable(new VisibleMetaEdgeNodeIterator(preNode, VisibleMetaEdgeNodeIterator.EdgeNodeIteratorMode.BOTH));
        } else {
            return dhns.newEdgeIterable(new MetaEdgeNodeIterator(preNode, MetaEdgeNodeIterator.EdgeNodeIteratorMode.BOTH));
        }
    }

    public EdgeIterable getMetaInEdges(Node node) {
        if (node == null) {
            throw new NullPointerException();
        }
        readLock();
        PreNode preNode = (PreNode) node;
        if (visible) {
            return dhns.newEdgeIterable(new VisibleMetaEdgeNodeIterator(preNode, VisibleMetaEdgeNodeIterator.EdgeNodeIteratorMode.IN));
        } else {
            return dhns.newEdgeIterable(new MetaEdgeNodeIterator(preNode, MetaEdgeNodeIterator.EdgeNodeIteratorMode.IN));
        }
    }

    public EdgeIterable getMetaOutEdges(Node node) {
        if (node == null) {
            throw new NullPointerException();
        }
        readLock();
        PreNode preNode = (PreNode) node;
        if (visible) {
            return dhns.newEdgeIterable(new VisibleMetaEdgeNodeIterator(preNode, VisibleMetaEdgeNodeIterator.EdgeNodeIteratorMode.OUT));
        } else {
            return dhns.newEdgeIterable(new MetaEdgeNodeIterator(preNode, MetaEdgeNodeIterator.EdgeNodeIteratorMode.OUT));
        }
    }

    public int getMetaInDegree(Node node) {
        if (node == null) {
            throw new NullPointerException();
        }
        readLock();
        PreNode preNode = (PreNode) node;
        int count = 0;
        if (visible && !preNode.getMetaEdgesOutTree().isEmpty()) {
            for (Iterator<MetaEdgeImpl> itr = preNode.getMetaEdgesOutTree().iterator(); itr.hasNext();) {
                if (itr.next().isVisible()) {
                    count++;
                }
            }
        } else {
            count = preNode.getMetaEdgesOutTree().getCount();
        }
        readUnlock();
        return count;
    }

    public int getMetaOutDegree(Node node) {
        if (node == null) {
            throw new NullPointerException();
        }
        readLock();
        PreNode preNode = (PreNode) node;
        int count = 0;
        if (visible && !preNode.getMetaEdgesOutTree().isEmpty()) {
            for (Iterator<MetaEdgeImpl> itr = preNode.getMetaEdgesOutTree().iterator(); itr.hasNext();) {
                if (itr.next().isVisible()) {
                    count++;
                }
            }
        } else {
            count = preNode.getMetaEdgesOutTree().getCount();
        }
        readUnlock();
        return count;
    }

    public int getMetaDegree(Node node) {
        return getMetaInDegree(node) + getMetaOutDegree(node);
    }

    public void readLock() {
        //System.out.println(Thread.currentThread()+ "read lock");
        dhns.getReadLock().lock();
    }

    public void readUnlock() {
        //System.out.println(Thread.currentThread()+ "read unlock");
        dhns.getReadLock().unlock();
    }

    public void writeLock() {
        //System.out.println(Thread.currentThread()+ "write lock");
        dhns.getWriteLock().lock();
    }

    public void writeUnlock() {
        //System.out.println(Thread.currentThread()+ "write lock");
        dhns.getWriteLock().unlock();
    }

    public int getNodeVersion() {
        return dhns.getGraphVersion().getNodeVersion();
    }

    public int getEdgeVersion() {
        return dhns.getGraphVersion().getEdgeVersion();
    }
}
