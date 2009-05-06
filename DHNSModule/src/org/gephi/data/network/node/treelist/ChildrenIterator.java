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
package org.gephi.data.network.node.treelist;

import java.util.Iterator;
import org.gephi.data.network.node.PreNode;
import org.gephi.data.network.node.treelist.PreNodeTreeList.AVLNode;
import org.gephi.data.network.tree.TreeStructure;
import org.gephi.datastructure.avl.ResetableIterator;

/**
 * {@link PreNode} iterator for children of a node, enabled or not.
 *
 * @author Mathieu Bastian
 */
public class ChildrenIterator implements Iterator<PreNode>, ResetableIterator {

    protected int treeSize;
    protected PreNodeTreeList treeList;
    protected int nextIndex;
    protected int diffIndex;
    protected AVLNode currentNode;

    public ChildrenIterator(TreeStructure treeStructure) {
        this.treeList = treeStructure.getTree();
        nextIndex = 0;
        diffIndex = 2;
        treeSize = treeList.size();
    }

    public void setNode(PreNode node) {
        nextIndex = node.getPre() + 1;
        treeSize = node.getPre() + node.size + 1;
        diffIndex = 2;
    }

    public boolean hasNext() {
        if (nextIndex < treeSize) {
            if (diffIndex > 1) {
                currentNode = treeList.get(nextIndex).avlNode;
            } else {
                currentNode = currentNode.next();
            }
            return true;
        }
        return false;
    }

    public PreNode next() {
        nextIndex = currentNode.value.getPre() + 1 + currentNode.value.size;
        diffIndex = nextIndex - currentNode.value.pre;
        return currentNode.value;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}
