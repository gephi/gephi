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
import org.gephi.graph.api.GraphListener;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.graph.api.Tree;
import org.gephi.graph.dhns.core.Dhns;
import org.gephi.graph.dhns.core.PropositionManager;
import org.gephi.graph.dhns.edge.AbstractEdge;
import org.gephi.graph.dhns.node.AbstractNode;
import org.gephi.graph.dhns.node.CloneNode;
import org.gephi.graph.dhns.node.PreNode;
import org.gephi.graph.dhns.node.iterators.TreeListIterator;
import org.gephi.graph.dhns.node.iterators.ChildrenIterator;
import org.gephi.graph.dhns.node.iterators.DescendantIterator;
import org.gephi.graph.dhns.node.iterators.LevelIterator;
import org.gephi.graph.dhns.node.iterators.TreeIterator;
import org.gephi.graph.dhns.proposition.Proposition;
import org.gephi.graph.dhns.proposition.PropositionImpl;
import org.gephi.graph.dhns.proposition.Tautology;
import org.gephi.graph.dhns.tree.HierarchyTreeImpl;
import org.gephi.graph.dhns.view.View;

/**
 * Abstract Clustered graph class. Implements methods for both directed and undirected graphs.
 *
 * @author Mathieu Bastian
 */
public abstract class ClusteredGraphImpl extends AbstractGraphImpl implements ClusteredGraph {

    protected PropositionImpl<AbstractNode> nodeProposition;
    protected PropositionImpl<AbstractEdge> edgeProposition;
    protected PropositionImpl<AbstractNode> nodeEnabledProposition;
    protected boolean allowMultilevel = true;
    protected View view;

    public ClusteredGraphImpl(Dhns dhns, boolean visible) {
        this.dhns = dhns;
        this.view = dhns.getViewManager().getMainView();
        this.nodeProposition = new PropositionImpl<AbstractNode>();
        this.edgeProposition = new PropositionImpl<AbstractEdge>();
        this.nodeEnabledProposition = new PropositionImpl<AbstractNode>();

        PropositionManager propositionManager = dhns.getPropositionManager();
        nodeEnabledProposition.addPredicate(propositionManager.newEnablePredicateNode(view));
        if (visible) {
            nodeProposition.addPredicate(propositionManager.getVisiblePredicateNode());
            edgeProposition.addPredicate(propositionManager.getVisiblePredicateEdge());
            nodeEnabledProposition.addPredicate(propositionManager.getVisiblePredicateNode());
        }
    }

    public boolean addNode(Node node, Node parent) {
        if (node == null) {
            throw new IllegalArgumentException("Node can't be null");
        }
        AbstractNode absNode = (AbstractNode) node;
        AbstractNode absParent = null;
        if (parent != null) {
            absParent = checkNode(parent);
        }
        if (!allowMultilevel && absNode.isValid()) {
            return false;
        }
        if (allowMultilevel && parent != null && absNode.isValid()) {
            //Verify the parent node is not a descendant of node
            readLock();
            if (isDescendant(node, parent)) {
                readUnlock();
                throw new IllegalArgumentException("Parent can't be a descendant of node");
            }
            readUnlock();
        }
        if (!absNode.hasAttributes()) {
            absNode.setAttributes(dhns.getGraphFactory().newNodeAttributes());
        }
        dhns.getStructureModifier().addNode(view, node, parent);
        return true;
    }

    public boolean addNode(Node node) {
        return addNode(node, null);
    }

    public boolean contains(Node node) {
        if (node == null) {
            throw new NullPointerException();
        }
        AbstractNode absNode = (AbstractNode) node;
        readLock();
        boolean res = false;
        if (absNode.isValid()) {
            if (nodeProposition.evaluate(absNode) && dhns.getTreeStructure().getTree().contains(absNode)) {
                res = true;
            }
        }
        readUnlock();
        return res;
    }

    public NodeIterable getNodes() {
        readLock();
        return dhns.newNodeIterable(new TreeIterator(dhns.getTreeStructure(), nodeEnabledProposition));
    }

    public int getNodeCount() {
        readLock();
        int count = 0;
        if (nodeProposition.isTautology()) {
            count = dhns.getTreeStructure().getTreeSize() - 1;// -1 Exclude virtual root
        } else {
            for (TreeListIterator itr = new TreeListIterator(dhns.getTreeStructure().getTree(), 1); itr.hasNext();) {
                if (nodeProposition.evaluate(itr.next())) {
                    count++;
                }
            }
        }
        readUnlock();
        return count;
    }

    public NodeIterable getNodes(int level) {
        level += 1;     //Because we ignore the virtual root
        readLock();
        int height = dhns.getTreeStructure().treeHeight;
        if (level > height) {
            readUnlock();
            throw new IllegalArgumentException("Level must be between 0 and the height of the tree, currently height=" + (height - 1));
        }
        return dhns.newNodeIterable(new LevelIterator(dhns.getTreeStructure(), level, nodeProposition));
    }

    public int getLevelSize(int level) {
        level += 1;     //Because we ignore the virtual root
        readLock();
        int height = dhns.getTreeStructure().treeHeight;
        if (level > height) {
            readUnlock();
            throw new IllegalArgumentException("Level must be between 0 and the height of the tree, currently height=" + (height - 1));
        }
        int res = 0;
        for (LevelIterator itr = new LevelIterator(dhns.getTreeStructure(), level, nodeProposition); itr.hasNext();) {
            itr.next();
            res++;
        }

        readUnlock();
        return res;
    }

    public boolean isSelfLoop(Edge edge) {
        checkEdge(edge);
        return edge.getSource() == edge.getTarget();
    }

    public boolean isAdjacent(Edge edge1, Edge edge2) {
        if (edge1 == edge2) {
            throw new IllegalArgumentException("Edges can't be the same");
        }
        checkEdge(edge1);
        checkEdge(edge2);
        return edge1.getSource() == edge2.getSource() ||
                edge1.getSource() == edge2.getTarget() ||
                edge1.getTarget() == edge2.getSource() ||
                edge1.getTarget() == edge2.getTarget();
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
        AbstractNode absNode = checkNode(node);
        if (!absNode.isValid()) {
            return false;
        }
        dhns.getStructureModifier().deleteNode(view, node);
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
        dhns.getStructureModifier().clearMetaEdges(view, node);
    }

    public int getChildrenCount(Node node) {
        AbstractNode absNode = checkNode(node);
        readLock();
        int count = 0;
        ChildrenIterator itr = new ChildrenIterator(dhns.getTreeStructure(), absNode, nodeProposition);
        for (; itr.hasNext();) {
            itr.next();
            count++;
        }
        readUnlock();
        return count;
    }

    public Node getParent(Node node) {
        AbstractNode absNode = checkNode(node);
        readLock();
        Node parent = null;
        if (absNode.parent != dhns.getTreeStructure().getRoot()) {
            parent = absNode.parent;
        }
        readUnlock();
        return parent;
    }

    public NodeIterable getChildren(Node node) {
        AbstractNode absNode = checkNode(node);
        readLock();
        return dhns.newNodeIterable(new ChildrenIterator(dhns.getTreeStructure(), absNode, nodeProposition));
    }

    public NodeIterable getDescendant(Node node) {
        AbstractNode absNode = checkNode(node);
        readLock();
        return dhns.newNodeIterable(new DescendantIterator(dhns.getTreeStructure(), absNode, nodeProposition));
    }

    public NodeIterable getTopNodes() {
        readLock();
        return dhns.newNodeIterable(new ChildrenIterator(dhns.getTreeStructure(), nodeProposition));
    }

    public boolean isDescendant(Node node, Node descendant) {
        AbstractNode abstractNode = checkNode(node);
        AbstractNode preDesc = checkNode(descendant);
        readLock();
        boolean res = false;
        if (allowMultilevel) {
            //Check if clones of descendants are descendants
            PreNode preNode = preDesc.getOriginalNode();
            res = preDesc.getPre() > abstractNode.getPre() && preDesc.getPost() < abstractNode.getPost();
            CloneNode cn = preNode.getClones();
            while (cn != null) {
                res = res || cn.getPre() > abstractNode.getPre() && cn.getPost() < abstractNode.getPost();
                cn = cn.getNext();
            }
        } else {
            res = preDesc.getPre() > abstractNode.getPre() && preDesc.getPost() < abstractNode.getPost();
        }
        readUnlock();
        return res;
    }

    public boolean isAncestor(Node node, Node ancestor) {
        return isDescendant(ancestor, node);
    }

    public boolean isFollowing(Node node, Node following) {
        AbstractNode absNode = checkNode(node);
        AbstractNode preFoll = checkNode(following);
        readLock();
        boolean res = preFoll.getPre() > absNode.getPre() && preFoll.getPost() > absNode.getPost();
        readUnlock();
        return res;
    }

    public boolean isPreceding(Node node, Node preceding) {
        return isFollowing(preceding, node);
    }

    public boolean isParent(Node node, Node parent) {
        AbstractNode absNode = checkNode(node);
        checkNode(parent);
        readLock();
        PreNode preNode = absNode.getOriginalNode();
        boolean res = preNode.parent == parent;
        if (allowMultilevel && !res) {
            CloneNode cn = preNode.getClones();
            while (cn != null) {
                res = res | cn.parent == parent;
            }
        }
        readUnlock();
        return res;
    }

    public int getHeight() {
        readLock();
        int res = dhns.getTreeStructure().treeHeight - 1;
        readUnlock();
        return res;
    }

    public int getLevel(Node node) {
        AbstractNode absNode = checkNode(node);
        readLock();
        int res = absNode.level - 1;
        readUnlock();
        return res;
    }

    public boolean expand(Node node) {
        AbstractNode absNode = checkNode(node);
        if (absNode.size == 0 || !absNode.isEnabled(view)) {
            return false;
        }
        dhns.getStructureModifier().expand(view, node);
        return true;
    }

    public boolean retract(Node node) {
        AbstractNode absNode = checkNode(node);
        if (absNode.size == 0 || absNode.isEnabled(view)) {
            return false;
        }
        dhns.getStructureModifier().retract(view, node);
        return true;
    }

    public void moveToGroup(Node node, Node nodeGroup) {
        checkNode(node);
        checkNode(nodeGroup);
        if (isDescendant(node, nodeGroup)) {
            throw new IllegalArgumentException("nodeGroup can't be a descendant of node");
        }
        dhns.getStructureModifier().moveToGroup(view, node, nodeGroup);
    }

    public void removeFromGroup(Node node) {
        AbstractNode absNode = checkNode(node);
        if (absNode.parent.parent == null) {   //Equal root
            throw new IllegalArgumentException("Node parent can't be the root of the tree");
        }
        dhns.getStructureModifier().moveToGroup(view, node, absNode.parent.parent);
    }

    public Node groupNodes(Node[] nodes) {
        if (nodes == null || nodes.length == 0) {
            throw new IllegalArgumentException("nodes can't be null or empty");
        }
        AbstractNode parent = null;
        for (int i = 0; i < nodes.length; i++) {
            AbstractNode node = checkNode(nodes[i]);
            if (parent == null) {
                parent = node.parent;
            } else if (parent != node.parent) {
                throw new IllegalArgumentException("All nodes must have the same parent");
            }
        }
        Node group = dhns.getStructureModifier().group(view, nodes);
        return group;
    }

    public void ungroupNodes(Node nodeGroup) {
        AbstractNode absNode = checkNode(nodeGroup);
        if (absNode.size == 0) {
            throw new IllegalArgumentException("nodeGroup can't be empty");
        }

        dhns.getStructureModifier().ungroup(view, absNode);
    }

    public boolean isInView(Node node) {
        AbstractNode absNode = checkNode(node);
        readLock();
        boolean res = absNode.isEnabled(view);
        readUnlock();
        return res;
    }

    public void resetView() {
        dhns.getStructureModifier().resetView(view);
    }

    public void setVisible(Node node, boolean visible) {
        AbstractNode absNode = checkNode(node);
        writeLock();
        absNode.setVisible(visible);
        writeUnlock();
    }

    public void setVisible(Edge edge, boolean visible) {
        AbstractEdge absEdge = checkEdge(edge);
        writeLock();
        absEdge.setVisible(visible);
        writeUnlock();
    }

    public void addGraphListener(GraphListener graphListener) {
        dhns.getEventManager().addListener(graphListener);
    }

    public void removeGraphListener(GraphListener graphListener) {
        dhns.getEventManager().removeListener(graphListener);
    }

    public boolean isDirected() {
        return dhns.isDirected();
    }

    public boolean isUndirected() {
        return dhns.isUndirected();
    }

    public boolean isMixed() {
        return dhns.isMixed();
    }

    public boolean isClustered() {
        return getHeight() > 0;
    }

    public Tree getHierarchyTree() {
        return new HierarchyTreeImpl(dhns);
    }
}
