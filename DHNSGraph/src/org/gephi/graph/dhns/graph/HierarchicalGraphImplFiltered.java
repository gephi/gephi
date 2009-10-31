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

import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.graph.api.Predicate;
import org.gephi.graph.api.View;
import org.gephi.graph.dhns.core.Dhns;
import org.gephi.graph.dhns.core.GraphStructure;
import org.gephi.graph.dhns.node.AbstractNode;
import org.gephi.graph.dhns.node.iterators.AbstractNodeIterator;
import org.gephi.graph.dhns.node.iterators.ChildrenIterator;
import org.gephi.graph.dhns.node.iterators.DescendantIterator;
import org.gephi.graph.dhns.node.iterators.LevelIterator;
import org.gephi.graph.dhns.node.iterators.TreeIterator;
import org.gephi.graph.dhns.views.ViewImpl;

/**
 *
 * @author Mathieu Bastian
 */
public abstract class HierarchicalGraphImplFiltered extends HierarchicalGraphImpl {

    public HierarchicalGraphImplFiltered(Dhns dhns, GraphStructure graphStructure, View view) {
        super(dhns, graphStructure);
        this.view = (ViewImpl) view;
    }

    @Override
    public boolean contains(Node node) {
        if (node == null) {
            throw new NullPointerException();
        }
        readLock();
        view.checkUpdate();
        AbstractNode absNode = (AbstractNode) node;
        boolean res = false;
        if (absNode.isValid()) {
            res = view.getClusteredLayerNodePredicate().evaluate(absNode);
        }
        readUnlock();
        return res;
    }

    @Override
    public NodeIterable getChildren(Node node) {
        readLock();
        view.checkUpdate();
        AbstractNode absNode = checkNode(node);
        Predicate<AbstractNode> predicate = view.getHierarchyLayerNodePredicate();
        return dhns.newNodeIterable(new ChildrenIterator(structure.getStructure(), absNode, predicate));
    }

    @Override
    public int getChildrenCount(Node node) {
        readLock();
        view.checkUpdate();
        AbstractNode absNode = checkNode(node);
        int count = 0;
        Predicate<AbstractNode> predicate = view.getHierarchyLayerNodePredicate();
        ChildrenIterator itr = new ChildrenIterator(structure.getStructure(), absNode, predicate);
        for (; itr.hasNext();) {
            itr.next();
            count++;
        }

        readUnlock();
        return count;
    }

    @Override
    public NodeIterable getDescendant(Node node) {
        readLock();
        view.checkUpdate();
        AbstractNode absNode = checkNode(node);
        Predicate<AbstractNode> predicate = view.getHierarchyLayerNodePredicate();
        return dhns.newNodeIterable(new DescendantIterator(structure.getStructure(), absNode, predicate));
    }

    @Override
    public NodeIterable getNodes() {
        readLock();
        view.checkUpdate();
        AbstractNodeIterator iterator = view.getClusteredLayerNodeIterator();
        return dhns.newNodeIterable(iterator);
    }

    @Override
    public NodeIterable getNodes(int level) {
        level += 1;     //Because we ignore the virtual root
        readLock();
        view.checkUpdate();
        int height = structure.getStructure().treeHeight;
        if (level > height) {
            readUnlock();
            throw new IllegalArgumentException("Level must be between 0 and the height of the tree, currently height=" + (height - 1));
        }
        Predicate<AbstractNode> predicate = view.getHierarchyLayerNodePredicate();
        return dhns.newNodeIterable(new LevelIterator(structure.getStructure(), level, predicate));
    }

    @Override
    public NodeIterable getNodesTree() {
        readLock();
        view.checkUpdate();
        Predicate<AbstractNode> predicate = view.getHierarchyLayerNodePredicate();
        return dhns.newNodeIterable(new TreeIterator(structure.getStructure(), false, predicate));
    }

    @Override
    public int getNodeCount() {
        return view.getClusteredNodesCount();
    }

    @Override
    public int getLevelSize(int level) {
        level += 1;     //Because we ignore the virtual root
        readLock();
        view.checkUpdate();
        int height = structure.getStructure().treeHeight;
        if (level > height) {
            readUnlock();
            throw new IllegalArgumentException("Level must be between 0 and the height of the tree, currently height=" + (height - 1));
        }
        int res = 0;
        for (LevelIterator itr = new LevelIterator(structure.getStructure(), level, view.getHierarchyLayerNodePredicate()); itr.hasNext();) {
            itr.next();
            res++;
        }

        readUnlock();
        return res;
    }

    @Override
    public Node getParent(Node node) {
        readLock();
        view.checkUpdate();
        AbstractNode absNode = checkNode(node);
        Node parent = null;
        if (absNode.parent != structure.getStructure().getRoot()) {
            parent = absNode.parent;
        }
        readUnlock();
        return parent;
    }

    @Override
    public NodeIterable getTopNodes() {
        readLock();
        view.checkUpdate();
        return dhns.newNodeIterable(new ChildrenIterator(structure.getStructure(), view.getHierarchyLayerNodePredicate()));
    }

    @Override
    public boolean isAncestor(Node node, Node ancestor) {
        readLock();
        view.checkUpdate();
        return super.isAncestor(node, ancestor);
    }

    @Override
    public boolean isDescendant(Node node, Node descendant) {
        readLock();
        view.checkUpdate();
        return super.isDescendant(node, descendant);
    }

    @Override
    public boolean isFollowing(Node node, Node following) {
        readLock();
        view.checkUpdate();
        return super.isFollowing(node, following);
    }

    @Override
    public boolean isPreceding(Node node, Node preceding) {
        readLock();
        view.checkUpdate();
        return super.isPreceding(node, preceding);
    }

    @Override
    public boolean isParent(Node node, Node parent) {
        readLock();
        view.checkUpdate();
        return super.isParent(node, parent);
    }
}
