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
import org.gephi.graph.api.Node;

/**
 *
 * @author Mathieu Bastian
 */
public class NodeTreeModel implements TreeModel {

    private RootNode root;

    public NodeTreeModel(Node[] nodes) {
        this.root = new RootNode(nodes);
    }

    public Object getRoot() {
        return root;
    }

    public Object getChild(Object parent, int index) {
        if (parent == root) {
            return root.getChildAt(index);
        } else {
            Node node = (Node) parent;
            return node.getChildAt(index);
        }
    }

    public int getChildCount(Object parent) {
        if (parent == root) {
            return root.getChildrenCount();
        } else {
            Node node = (Node) parent;
            return node.getChildrenCount();
        }
    }

    public boolean isLeaf(Object node) {
        if (node == root) {
            return root.isLeaf();
        } else {
            Node n = (Node) node;
            return n.getChildrenCount() == 0;
        }
    }

    public void valueForPathChanged(TreePath path, Object newValue) {
    }

    public int getIndexOfChild(Object parent, Object child) {
        if (parent == null || child == null) {
            return -1;
        }
        if (parent == root) {
            return root.getIndexOfChild((Node) child);
        }
        return -1;//TODO for node
    }

    public void addTreeModelListener(TreeModelListener l) {
    }

    public void removeTreeModelListener(TreeModelListener l) {
    }

    class RootNode {

        private Node[] nodes;

        public RootNode(Node[] nodes) {
            this.nodes = nodes;
        }

        public Node getChildAt(int index) {
            return nodes[index];
        }

        public int getChildrenCount() {
            return nodes.length;
        }

        public boolean isLeaf() {
            return nodes.length == 0;
        }

        public int getIndexOfChild(Node node) {
            for (int i = 0; i < nodes.length; i++) {
                if (nodes[i] == node) {
                    return i;
                }
            }
            return -1;
        }
    }
}
