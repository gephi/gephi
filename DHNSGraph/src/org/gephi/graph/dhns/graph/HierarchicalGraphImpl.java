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

import java.util.Map;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.ImmutableTreeNode;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeData;
import org.gephi.graph.api.NodeIterable;
import org.gephi.graph.dhns.core.Dhns;
import org.gephi.graph.dhns.core.GraphViewImpl;
import org.gephi.graph.dhns.edge.AbstractEdge;
import org.gephi.graph.dhns.node.AbstractNode;
import org.gephi.graph.dhns.node.iterators.ChildrenIterator;
import org.gephi.graph.dhns.node.iterators.DescendantIterator;
import org.gephi.graph.dhns.node.iterators.LevelIterator;
import org.gephi.graph.dhns.node.iterators.TreeIterator;
import org.gephi.graph.dhns.predicate.Predicate;
import org.gephi.graph.dhns.predicate.Tautology;
import org.gephi.graph.dhns.utils.TreeNodeWrapper;

/**
 *
 * @author Mathieu Bastian
 */
public abstract class HierarchicalGraphImpl extends AbstractGraphImpl implements HierarchicalGraph {

    protected Predicate<AbstractNode> enabledNodePredicate;

    public HierarchicalGraphImpl(Dhns dhns, GraphViewImpl view) {
        super(dhns, view);
        enabledNodePredicate = new Predicate<AbstractNode>() {

            public boolean evaluate(AbstractNode element) {
                return element.isEnabled();
            }
        };
    }

    public abstract HierarchicalGraphImpl copy(Dhns dhns, GraphViewImpl view);

    public boolean addNode(Node node, Node parent) {
        if (node == null) {
            throw new IllegalArgumentException("Node can't be null");
        }
        AbstractNode absNode = (AbstractNode) node;
        AbstractNode absParent = null;
        if (parent != null) {
            absParent = checkNode(parent);
        }
        if (absNode.isValid(view.getViewId())) {
            return false;
        }
        if (absNode.avlNode != null) { //exist in another view
            if (absNode.getInView(view.getViewId()) != null) {
                return false;
            }
            absNode = new AbstractNode(absNode.getNodeData(), view.getViewId());
        }
        if (!absNode.getNodeData().hasAttributes()) {
            absNode.getNodeData().setAttributes(dhns.factory().newNodeAttributes(absNode.getNodeData()));
        }
        view.getStructureModifier().addNode(absNode, absParent);
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
        boolean res = false;
        if (absNode.isValid(view.getViewId())) {
            res = structure.getTree().contains(absNode);
        } else if ((absNode = absNode.getInView(view.getViewId())) != null) {
            res = true;
        }
        return res;
    }

    public Node getNode(int id) {
        Node node = dhns.getGraphStructure().getNodeFromDictionnary(id);
        return node;
    }

    public Edge getEdge(int id) {
        Edge edge = dhns.getGraphStructure().getEdgeFromDictionnary(id);
        return edge;
    }

    public Node getNode(String id) {
        if (id == null) {
            throw new NullPointerException();
        }
        NodeData nd = dhns.getGraphStructure().getNodeIDDictionnary().get(id);
        if (nd != null) {
            return nd.getNode(view.getViewId());
        }
        return null;
    }

    public Edge getEdge(String id) {
        if (id == null) {
            throw new NullPointerException();
        }
        return dhns.getGraphStructure().getEdgeIDDIctionnary().get(id);
    }

    public NodeIterable getNodes() {
        readLock();
        return dhns.newNodeIterable(new TreeIterator(structure, true, Tautology.instance));
    }

    public NodeIterable getNodesTree() {
        readLock();
        return dhns.newNodeIterable(new TreeIterator(structure, false, Tautology.instance));
    }

    public int getNodeCount() {
        //int count = structure.getTreeSize() - 1;// -1 Exclude virtual root
        int count = view.getNodesEnabled();
        return count;
    }

    public NodeIterable getNodes(int level) {
        level += 1;     //Because we ignore the virtual root
        readLock();
        int height = structure.getTreeHeight();
        if (level > height) {
            readUnlock();
            throw new IllegalArgumentException("Level must be between 0 and the height of the tree, currently height=" + (height - 1));
        }
        return dhns.newNodeIterable(new LevelIterator(structure, level, Tautology.instance));

    }

    public int getLevelSize(int level) {
        level += 1;     //Because we ignore the virtual root
        int height = structure.getTreeHeight();
        if (level > height) {
            throw new IllegalArgumentException("Level must be between 0 and the height of the tree, currently height=" + (height - 1));
        }
        int res = structure.getLevelSize(level);
        return res;
    }

    public boolean isSelfLoop(Edge edge) {
        AbstractEdge absEdge = checkEdge(edge);
        return absEdge.getSource(view.getViewId()) == absEdge.getTarget(view.getViewId());
    }

    public boolean isAdjacent(Edge edge1, Edge edge2) {
        if (edge1 == edge2) {
            throw new IllegalArgumentException("Edges can't be the same");
        }
        AbstractEdge absEdge1 = checkEdge(edge1);
        AbstractEdge absEdge2 = checkEdge(edge2);
        return absEdge1.getSource(view.getViewId()) == absEdge2.getSource(view.getViewId())
                || absEdge1.getSource(view.getViewId()) == absEdge2.getTarget(view.getViewId())
                || absEdge1.getTarget(view.getViewId()) == absEdge2.getSource(view.getViewId())
                || absEdge1.getTarget(view.getViewId()) == absEdge2.getTarget(view.getViewId());
    }

    public Node getOpposite(Node node, Edge edge) {
        checkNode(node);
        AbstractEdge absEdge = checkEdge(edge);
        if (absEdge.getSource(view.getViewId()) == node) {
            return absEdge.getTarget(view.getViewId());
        } else if (absEdge.getTarget(view.getViewId()) == node) {
            return absEdge.getSource(view.getViewId());
        }
        throw new IllegalArgumentException("Node must be either source or target of the edge.");
    }

    public boolean removeNode(Node node) {
        AbstractNode absNode = checkNode(node);
        view.getStructureModifier().deleteNode(absNode);
        return true;
    }

    public void clear() {
        view.getStructureModifier().clear();
    }

    public void clearEdges() {
        view.getStructureModifier().clearEdges();
    }

    public void clearEdges(Node node) {
        AbstractNode absNode = checkNode(node);
        view.getStructureModifier().clearEdges(absNode);
    }

    public void clearMetaEdges(Node node) {
        AbstractNode absNode = checkNode(node);
        view.getStructureModifier().clearMetaEdges(absNode);
    }

    public void setId(Node node, String id) {
        if (node == null) {
            throw new NullPointerException("node can't be null");
        }
        AbstractNode absNode = (AbstractNode) node;
        String oldId = absNode.getNodeData().setId(id);
        Map<String, NodeData> map = view.getGraphModel().getGraphStructure().getNodeIDDictionnary();
        if (oldId != null) {
            map.remove(oldId);
        }
        map.put(id, node.getNodeData());
    }

    public void setId(Edge edge, String id) {
        if (edge == null) {
            throw new NullPointerException("edge can't be null");
        }
        AbstractEdge absEdge = (AbstractEdge) edge;
        String oldId = absEdge.getEdgeData().setId(id);
        Map<String, Edge> map = view.getGraphModel().getGraphStructure().getEdgeIDDIctionnary();
        if (oldId != null) {
            map.remove(oldId);
        }
        map.put(id, edge);
    }

    public ImmutableTreeNode wrapToTreeNode() {
        TreeNodeWrapper wrapper = new TreeNodeWrapper(structure);
        ImmutableTreeNode treeNode;
        readLock();
        treeNode = wrapper.wrap(new TreeIterator(structure, false, Tautology.instance));
        readUnlock();
        return treeNode;
    }

    public int getChildrenCount(Node node) {
        AbstractNode absNode = checkNode(node);
        int count = 0;
        ChildrenIterator itr = new ChildrenIterator(structure, absNode, Tautology.instance);
        for (; itr.hasNext();) {
            itr.next();
            count++;
        }
        return count;
    }

    public int getDescendantCount(Node node) {
        AbstractNode absNode = checkNode(node);
        return absNode.size;
    }

    public Node getParent(Node node) {
        AbstractNode absNode = checkNode(node);
        Node parent = null;
        if (absNode.parent != structure.getRoot()) {
            parent = absNode.parent;
        }
        return parent;
    }

    public NodeIterable getChildren(Node node) {
        readLock();
        AbstractNode absNode = checkNode(node);
        return dhns.newNodeIterable(new ChildrenIterator(structure, absNode, Tautology.instance));
    }

    public NodeIterable getDescendant(Node node) {
        readLock();
        AbstractNode absNode = checkNode(node);
        return dhns.newNodeIterable(new DescendantIterator(structure, absNode, Tautology.instance));
    }

    public NodeIterable getTopNodes() {
        readLock();
        return dhns.newNodeIterable(new ChildrenIterator(structure, Tautology.instance));
    }

    public boolean isDescendant(Node node, Node descendant) {
        AbstractNode absNode = checkNode(node);
        AbstractNode absDesc = checkNode(descendant);
        boolean res = false;
        res = absDesc.getPre() > absNode.getPre() && absDesc.getPost() < absNode.getPost();
        return res;
    }

    public boolean isAncestor(Node node, Node ancestor) {
        return isDescendant(ancestor, node);
    }

    public boolean isFollowing(Node node, Node following) {
        AbstractNode absNode = checkNode(node);
        AbstractNode absFoll = checkNode(following);
        boolean res = absFoll.getPre() > absNode.getPre() && absFoll.getPost() > absNode.getPost();
        return res;
    }

    public boolean isPreceding(Node node, Node preceding) {
        return isFollowing(preceding, node);
    }

    public boolean isParent(Node node, Node parent) {
        AbstractNode absNode = checkNode(node);
        AbstractNode absParent = checkNode(parent);
        boolean res = absNode.parent == absParent;
        return res;
    }

    public int getHeight() {
        int res = structure.getTreeHeight() - 1;
        return res;
    }

    public int getLevel(Node node) {
        AbstractNode absNode = checkNode(node);
        int res = absNode.level - 1;
        return res;
    }

    public void moveToGroup(Node node, Node nodeGroup) {
        AbstractNode absNode = checkNode(node);
        AbstractNode absGroup = checkNode(nodeGroup);
        if (isDescendant(absNode, absGroup)) {
            throw new IllegalArgumentException("nodeGroup can't be a descendant of node");
        }
        view.getStructureModifier().moveToGroup(absNode, absGroup);
    }

    public void removeFromGroup(Node node) {
        AbstractNode absNode = checkNode(node);
        if (absNode.parent.parent == null) {   //Equal root
            throw new IllegalArgumentException("Node parent can't be the root of the tree");
        }
        view.getStructureModifier().moveToGroup(absNode, absNode.parent.parent);
    }

    public Node groupNodes(Node[] nodes) {
        if (nodes == null || nodes.length == 0) {
            throw new IllegalArgumentException("nodes can't be null or empty");
        }
        AbstractNode[] absNodes = new AbstractNode[nodes.length];
        AbstractNode parent = null;
        for (int i = 0; i < nodes.length; i++) {
            AbstractNode node = checkNode(nodes[i]);
            absNodes[i] = node;
            if (parent == null) {
                parent = node.parent;
            } else if (parent != node.parent) {
                throw new IllegalArgumentException("All nodes must have the same parent");
            }
        }

        Node group = view.getStructureModifier().group(absNodes);
        return group;
    }

    public void ungroupNodes(Node nodeGroup) {
        AbstractNode absNode = checkNode(nodeGroup);
        if (absNode.size == 0) {
            throw new IllegalArgumentException("nodeGroup can't be empty");
        }

        view.getStructureModifier().ungroup(absNode);
    }

    public boolean expand(Node node) {
        AbstractNode absNode = checkNode(node);
        if (absNode.size == 0 || !absNode.isEnabled()) {
            return false;
        }
        view.getStructureModifier().expand(absNode);
        return true;
    }

    public boolean retract(Node node) {
        AbstractNode absNode = checkNode(node);
        if (absNode.size == 0 || absNode.isEnabled()) {
            return false;
        }
        view.getStructureModifier().retract(absNode);
        return true;
    }

    public boolean isInView(Node node) {
        AbstractNode absNode = checkNode(node);
        boolean res = absNode.isEnabled();
        return res;
    }

    public void resetViewToLeaves() {
        view.getStructureModifier().resetViewToLeaves();
    }

    public void resetViewToLevel(int level) {
        readLock();
        level += 1;     //Because we ignore the virtual root
        int height = structure.getTreeHeight();
        if (level > height) {
            readUnlock();
            throw new IllegalArgumentException("Level must be between 0 and the height of the tree, currently height=" + (height - 1));
        }
        readUnlock();
        view.getStructureModifier().resetViewToLevel(level);
    }

    public void resetViewToTopNodes() {
        view.getStructureModifier().resetViewToTopNodes();
    }
}
