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
package org.gephi.graph.dhns.utils;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.tree.TreeNode;
import org.gephi.datastructure.avl.simple.AVLItem;
import org.gephi.datastructure.avl.simple.SimpleAVLTree;
import org.gephi.graph.api.ImmutableTreeNode;
import org.gephi.graph.api.Node;
import org.gephi.graph.dhns.core.TreeStructure;
import org.gephi.graph.dhns.node.AbstractNode;
import org.gephi.graph.dhns.node.iterators.AbstractNodeIterator;

/**
 *
 * @author Mathieu Bastian
 */
public class TreeNodeWrapper {

    private SimpleAVLTree nodeTree;
    private TreeStructure treeStructure;

    public TreeNodeWrapper(TreeStructure treeStructure) {
        this.treeStructure = treeStructure;
    }

    public ImmutableTreeNode wrap(AbstractNodeIterator iterator) {
        nodeTree = new SimpleAVLTree();
        TreeNodeImpl root = new TreeNodeImpl(treeStructure.getRoot());
        nodeTree.add(root);

        for (; iterator.hasNext();) {
            AbstractNode node = iterator.next();
            TreeNodeImpl n = new TreeNodeImpl(node);
            if (node.parent != null) {
                TreeNodeImpl parent = (TreeNodeImpl) nodeTree.get(node.parent.getNumber());
                n.parent = parent;
                parent.children.add(n);
            }
            nodeTree.add(n);
        }

        //To array
        for (AVLItem item : nodeTree) {
            TreeNodeImpl node = (TreeNodeImpl) item;
            node.toArray();
        }
        return root;
    }

    private static class TreeNodeImpl implements ImmutableTreeNode, AVLItem {

        private TreeNodeImpl parent;
        private AbstractNode node;
        private List<TreeNodeImpl> children;
        private TreeNodeImpl[] childrenArray;

        public TreeNodeImpl(AbstractNode node) {
            this.node = node;
            this.children = new ArrayList<TreeNodeImpl>();
        }

        public TreeNode getChildAt(int childIndex) {
            return childrenArray[childIndex];
        }

        public int getChildCount() {
            return childrenArray.length;
        }

        public TreeNode getParent() {
            return parent;
        }

        public int getIndex(TreeNode node) {
            for (int i = 0; i < childrenArray.length; i++) {
                if (childrenArray[i] == node) {
                    return i;
                }
            }
            return -1;
        }

        public boolean getAllowsChildren() {
            return false;
        }

        public boolean isLeaf() {
            return childrenArray == null;
        }

        public Enumeration children() {
            return new IteratorEnumeration(childrenArray);
        }

        public int getNumber() {
            return node.getNumber();
        }

        public void toArray() {
            if (!children.isEmpty()) {
                childrenArray = children.toArray(new TreeNodeImpl[0]);
            }
            children = null;
        }

        public Node getNode() {
            return node;
        }
    }

    private static class IteratorEnumeration implements Enumeration {

        Object[] array;
        int index = 0;

        public IteratorEnumeration(Object[] array) {
            this.array = array;
        }

        public boolean hasMoreElements() {
            return index < array.length;
        }

        public Object nextElement() {
            return array[index++];
        }
    }
}
