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
import org.gephi.graph.api.Edge;

/**
 *
 * @author Mathieu Bastian
 */
public class EdgeTreeModel implements TreeModel {

    private RootNode root;

    public EdgeTreeModel(Edge[] edges) {
        this.root = new RootNode(edges);
    }

    public Object getRoot() {
        return root;
    }

    public Object getChild(Object parent, int index) {
        if (parent == root) {
            return root.getChildAt(index);
        }
        return null;
    }

    public int getChildCount(Object parent) {
        if (parent == root) {
            return root.getChildrenCount();
        }
        return 0;
    }

    public boolean isLeaf(Object node) {
        if (node == root) {
            return root.isLeaf();
        }
        return true;
    }

    public void valueForPathChanged(TreePath path, Object newValue) {
    }

    public int getIndexOfChild(Object parent, Object child) {
        if (parent == null || child == null) {
            return -1;
        }
        if (parent == root) {
            return root.getIndexOfChild((Edge) child);
        }
        return -1;
    }

    public void addTreeModelListener(TreeModelListener l) {
    }

    public void removeTreeModelListener(TreeModelListener l) {
    }

    class RootNode {

        private Edge[] edges;

        public RootNode(Edge[] edges) {
            this.edges = edges;
        }

        public Edge getChildAt(int index) {
            return edges[index];
        }

        public int getChildrenCount() {

            return edges.length;
        }

        public boolean isLeaf() {
            return edges.length == 0;
        }

        public int getIndexOfChild(Edge edge) {
            for (int i = 0; i < edges.length; i++) {
                if (edges[i] == edge) {
                    return i;
                }
            }
            return -1;
        }
    }
}
