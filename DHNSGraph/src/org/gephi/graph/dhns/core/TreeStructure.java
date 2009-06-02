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
package org.gephi.graph.dhns.core;

import org.gephi.graph.dhns.node.PreNode;

/**
 * Holds nodes tree and manage basic operations.
 *
 * @author Mathieu Bastian
 */
public class TreeStructure {

    DurableTreeList tree;
    PreNode root;
    public int treeHeight;
    private PreNode cacheNode;

    public TreeStructure() {
        this(0);
    }

    public TreeStructure(int treeCapacity) {
        tree = new DurableTreeList();
        root = new PreNode(-1,0, 0, 0, null);
        root.setEnabled(false);
        tree.add(root);
    }

    public PreNode getNodeAt(int pre) {
        /*if(cacheNode!=null && cacheNode.avlNode.isConsistent() && cacheNode.pre == pre-1)
        {
        cacheNode = cacheNode.avlNode.next().getValue();
        return cacheNode;
        }

        cacheNode = tree.get(pre);
        return cacheNode;*/
        return tree.get(pre);
    }

    public PreNode getEnabledAncestorOrSelf(PreNode node) {
        PreNode parent = node;
        while (!parent.isEnabled()) {
            parent = parent.parent;
            if (parent == null || parent.pre == 0) {
                return null;
            }
        }
        return parent;
    }

    public PreNode getEnabledAncestor(PreNode node) {
        PreNode parent = node.parent;
        while (!parent.isEnabled()) {
            if (parent.pre == 0) {
                return null;
            }
            parent = parent.parent;
        }
        return parent;
    }

    public void insertAtEnd(PreNode node) {
        node.pre = tree.size();

        tree.add(node);
    }

    public void insertAsChild(PreNode node, PreNode parent) {
        node.parent = parent;
        node.pre = parent.pre + parent.size + 1;
        node.level = parent.level + 1;
        if (node.level > treeHeight) {
            treeHeight++;
        }

        tree.add(node.pre, node);
        incrementAncestorsSize(node);
    }

    public void deleteAtPre(PreNode node) {
        int pre = node.getPre();
        tree.remove(pre);
        for (int i = 0; i < node.size; i++) {
            tree.remove(pre);
        }

    }

    public void deleteDescendantAndSelf(PreNode node) {
        deleteAtPre(node);
        decrementAncestorSize(node, node.size + 1);
    }

    public void incrementAncestorsSize(PreNode node) {
        while (node.parent != null) {
            node = node.parent;
            node.size++;
            node.getPost();
        }
    }

    public void decrementAncestorSize(PreNode node, int shift) {
        while (node.parent != null) {
            node = node.parent;
            node.size -= shift;
            node.getPost();
        }
    }

    public void showTreeAsTable() {
        System.out.println("pre\tsize\tlevel\tparent\tpost\tenabled");
        System.out.println("-------------------------------------------------------");

        int pre = 0;
        for (PreNode p : tree) {
            System.out.println(p.pre + "\t" + p.size + "\t" + p.level + "\t" + p.parent + "\t" + p.post + "\t" + p.isEnabled());
            pre++;
        }
    }

    public void clear()
    {
        tree.clear();
        root=null;
        treeHeight=0;
    }

    public int getTreeSize() {
        return tree.size();
    }

    public DurableTreeList getTree() {
        return tree;
    }

    public PreNode getRoot() {
        return root;
    }
}
