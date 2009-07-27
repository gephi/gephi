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
package org.gephi.data.laboratory;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.gephi.graph.api.ClusteredDirectedGraph;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;

/**
 *
 * @author Mathieu Bastian
 */
public class NodeTreeModel implements TreeModel {

    private TreeNode root;
    private HierarchicalGraph graph;

    public NodeTreeModel(Node[] nodes, HierarchicalGraph graph) {
        this.graph = graph;
        this.root = new TreeNode(nodes);
    }

    public Object getRoot() {
        return root;
    }

    public Object getChild(Object parent, int index) {
        TreeNode node = (TreeNode) parent;
        return node.getChildAt(index);
    }

    public int getChildCount(Object parent) {

        TreeNode node = (TreeNode) parent;
        return node.getChildrenCount();

    }

    public boolean isLeaf(Object node) {

        TreeNode n = (TreeNode) node;
        return n.getChildrenCount() == 0;

    }

    public void valueForPathChanged(TreePath path, Object newValue) {
    }

    public int getIndexOfChild(Object parent, Object child) {
        if (parent == null || child == null) {
            return -1;
        }
        TreeNode node = (TreeNode) parent;
        return node.getIndexOfChild((TreeNode) child);
    }

    public void addTreeModelListener(TreeModelListener l) {
    }

    public void removeTreeModelListener(TreeModelListener l) {
    }

    class TreeNode {

        private Node node;
        private TreeNode[] children;

        public TreeNode(Node node) {
            this.node = node;
            Node[] ch = graph.getChildren(node).toArray();
            if (ch != null) {
                children = new TreeNode[ch.length];
                for (int i = 0; i < ch.length; i++) {
                    children[i] = new TreeNode(ch[i]);
                }
            }
        }

        public TreeNode(Node[] nodes) {
            children = new TreeNode[nodes.length];
            for (int i = 0; i < nodes.length; i++) {
                children[i] = new TreeNode(nodes[i]);
            }
        }

        public TreeNode getChildAt(int index) {
            return children[index];
        }

        public int getChildrenCount() {
            if (children != null) {
                return children.length;
            }
            return 0;
        }

        public boolean isLeaf() {
            if (children != null) {
                return false;
            }
            return true;
        }

        public int getIndexOfChild(TreeNode node) {
            for (int i = 0; i < children.length; i++) {
                if (children[i] == node) {
                    return i;
                }
            }
            return -1;
        }

        public String getLabel() {
            if (this == root) {
                return "root";
            } else {
                return node.getNodeData().getLabel();
            }
        }

        public Node getNode() {
            return node;
        }
    }
}
