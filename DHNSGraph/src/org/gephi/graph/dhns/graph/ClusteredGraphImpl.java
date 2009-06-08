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

import org.gephi.graph.api.ClusteredGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.graph.dhns.core.Dhns;
import org.gephi.graph.dhns.edge.AbstractEdge;
import org.gephi.graph.dhns.node.AbstractNode;
import org.gephi.graph.dhns.node.PreNode;
import org.gephi.graph.dhns.node.iterators.ChildrenIterator;
import org.gephi.graph.dhns.node.iterators.CompleteTreeIterator;
import org.gephi.graph.dhns.node.iterators.DescendantIterator;
import org.gephi.graph.dhns.node.iterators.TreeListIterator;
import org.gephi.graph.dhns.node.iterators.VisibleChildrenIterator;
import org.gephi.graph.dhns.node.iterators.VisibleDescendantIterator;
import org.gephi.graph.dhns.node.iterators.VisibleTreeIterator;

/**
 * Abstract Clustered graph class. Implements methods for both directed and undirected graphs.
 *
 * @author Mathieu Bastian
 */
public abstract class ClusteredGraphImpl implements ClusteredGraph {

    protected Dhns dhns;
    protected boolean visible = true;

    public ClusteredGraphImpl(Dhns dhns, boolean visible) {
        this.dhns = dhns;
        this.visible = visible;
    }

    public boolean addEdge(Edge edge) {
        AbstractEdge absEdge = checkEdge(edge);
        if(checkEdgeExist(absEdge.getSource(), absEdge.getTarget())) {
            //Edge already exist
            return false;
        }
        if (!absEdge.hasAttributes()) {
            absEdge.setAttributes(dhns.newEdgeAttributes());
        }
        dhns.getStructureModifier().addEdge(edge);
        return true;
    }

    public boolean addNode(Node node) {
        if (node == null) {
            throw new NullPointerException();
        }
        PreNode preNode = (PreNode) node;
        if (preNode.isValid()) {
            return false;     //Already added
        }
        if (!preNode.hasAttributes()) {
            preNode.setAttributes(dhns.newNodeAttributes());
        }
        dhns.getStructureModifier().addNode(node, null);
        return true;
    }

    public boolean contains(Node node) {
        if (node == null) {
            throw new NullPointerException();
        }
        PreNode preNode = (PreNode) node;
        readLock();
        if (!preNode.isValid()) {
            return false;
        }
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

    public NodeIterable getNodes() {
        readLock();
        if (visible) {
            return dhns.newNodeIterable(new VisibleTreeIterator(dhns.getTreeStructure()));
        } else {
            return dhns.newNodeIterable(new CompleteTreeIterator(dhns.getTreeStructure()));
        }
    }

    public int getNodeCount() {
        readLock();
        int count = 0;
        if (visible) {
            for (TreeListIterator itr = new TreeListIterator(dhns.getTreeStructure().getTree(), 1); itr.hasNext();) {
                if (itr.next().isVisible()) {
                    count++;
                }
            }
        } else {
            count = dhns.getTreeStructure().getTreeSize() - 1;// -1 Exclude virtual root
        }
        readUnlock();
        return count;
    }

    public boolean isSelfLoop(Edge edge) {
        checkEdge(edge);
        return edge.getSource() == edge.getTarget();
    }

    public boolean isAdjacent(Edge edge1, Edge edge2) {
        if(edge1==edge2) {
            throw new IllegalArgumentException("Edges can't be the same");
        }
        checkEdge(edge1);
        checkEdge(edge2);
        return edge1.getSource() == edge2.getSource() ||
                edge1.getSource() == edge2.getTarget() ||
                edge1.getTarget() == edge2.getSource() ||
                edge1.getTarget() == edge2.getSource();
    }

    public Node getOpposite(Node node, Edge edge) {
        checkNode(node);
        checkEdge(edge);
        if (edge.getSource() == node) {
            return edge.getTarget();
        } else if (edge.getTarget() == node) {
            return edge.getSource();
        }
        throw new IllegalArgumentException("Node must be either source or target of the edge.");
    }

    public boolean removeNode(Node node) {
        PreNode preNode = checkNode(node);
        if(!preNode.isValid()) {
            return false;
        }
        dhns.getStructureModifier().deleteNode(node);
        return true;
    }

    public void clear() {
        dhns.getStructureModifier().clear();
    }

    public void clearEdges() {
        dhns.getStructureModifier().clearEdges();
    }

    public void clearEdges(Node node) {
        checkNode(node);
        dhns.getStructureModifier().clearEdges(node);
    }

    public void clearMetaEdges(Node node) {
        checkNode(node);
        dhns.getStructureModifier().clearMetaEdges(node);
    }

    public boolean addNode(Node node, Node parent) {
        PreNode preNode = checkNode(node);
        if (parent != null) {
            checkNode(parent);
        }
        if(preNode.isValid()) {
            return false;
        }
        if (!preNode.hasAttributes()) {
            preNode.setAttributes(dhns.newNodeAttributes());
        }
        dhns.getStructureModifier().addNode(node, parent);
        return true;
    }

    public int getChildrenCount(Node node) {
        PreNode preNode = checkNode(node);
        readLock();
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
        PreNode preNode = checkNode(node);
        readLock();
        if (preNode.parent == dhns.getTreeStructure().getRoot()) {
            return null;
        }
        Node parent = preNode.parent;
        readUnlock();
        return parent;
    }

    public NodeIterable getChildren(Node node) {
        PreNode preNode = checkNode(node);
        readLock();
        if (visible) {
            return dhns.newNodeIterable(new VisibleChildrenIterator(dhns.getTreeStructure(), preNode));
        } else {
            return dhns.newNodeIterable(new ChildrenIterator(dhns.getTreeStructure(), preNode));
        }
    }

    public NodeIterable getDescendant(Node node) {
        PreNode preNode = checkNode(node);
        readLock();
        if (visible) {
            return dhns.newNodeIterable(new VisibleDescendantIterator(dhns.getTreeStructure(), preNode));
        } else {
            return dhns.newNodeIterable(new DescendantIterator(dhns.getTreeStructure(), preNode));
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
        PreNode preNode = checkNode(node);
        PreNode preDesc = checkNode(descendant);
        readLock();
        boolean res = preDesc.getPre() > preNode.getPre() && preDesc.getPost() < preNode.getPost();
        readUnlock();
        return res;
    }

    public boolean isAncestor(Node node, Node ancestor) {
        return isDescendant(ancestor, node);
    }

    public boolean isFollowing(Node node, Node following) {
        PreNode preNode = checkNode(node);
        PreNode preFoll = checkNode(following);
        readLock();
        boolean res = preFoll.getPre() > preNode.getPre() && preFoll.getPost() > preNode.getPost();
        readUnlock();
        return res;
    }

    public boolean isPreceding(Node node, Node preceding) {
        return isFollowing(preceding, node);
    }

    public boolean isParent(Node node, Node parent) {
        PreNode preNode = checkNode(node);
        checkNode(parent);
        readLock();
        boolean res = preNode.parent == parent;
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
        PreNode preNode = checkNode(node);
        readLock();
        int res = preNode.level;
        readUnlock();
        return res;
    }

    public void expand(Node node) {
        checkNode(node);
        dhns.getStructureModifier().expand(node);
    }

    public void retract(Node node) {
        checkNode(node);
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

    public void setVisible(Node node, boolean visible) {
        PreNode preNode = checkNode(node);
        writeLock();
        preNode.setVisible(visible);
        writeUnlock();
    }

    public void setVisible(Edge edge, boolean visible) {
        AbstractEdge absEdge = checkEdge(edge);
        writeLock();
        absEdge.setVisible(visible);
        writeUnlock();
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

    protected PreNode checkNode(Node node) {
        if (node == null) {
            throw new NullPointerException();
        }
        PreNode preNode = (PreNode) node;
        if (!preNode.isValid()) {
            throw new IllegalArgumentException("Node must be in the graph");
        }
        return preNode;
    }

    protected AbstractEdge checkEdge(Edge edge) {
        if (edge == null) {
            throw new NullPointerException();
        }
        AbstractEdge abstractEdge = (AbstractEdge) edge;
        if (!abstractEdge.isValid()) {
            throw new IllegalArgumentException("Nodes must be in the graph");
        }
        return abstractEdge;
    }

    protected boolean checkEdgeExist(PreNode source, PreNode target) {
        return source.getEdgesOutTree().hasNeighbour(target);
    }

    protected AbstractEdge getSymmetricEdge(AbstractEdge edge) {
        return edge.getTarget().getEdgesOutTree().getItem(edge.getSource().getNumber());
    }
}
