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
import org.gephi.data.network.tree.TreeStructure;
import org.gephi.data.network.node.PreNode;
import org.gephi.data.network.node.treelist.PreNodeTreeList.AVLNode;
import org.gephi.data.network.sight.Sight;
import org.gephi.datastructure.avl.ResetableIterator;

/**
 * {@link PreNode} iterator for enabled nodes.
 * 
 * @author Mathieu Bastian
 * @see PreNodeTreeList
 */
public class SingleTreeIterator implements Iterator<PreNode>, ResetableIterator {

    protected int treeSize;
    protected PreNodeTreeList treeList;
    protected int nextIndex;
    protected int diffIndex;
    ;
    protected AVLNode currentNode;
    protected Sight sight;

    public SingleTreeIterator(TreeStructure treeStructure, Sight sight) {
        this.treeList = treeStructure.getTree();
        nextIndex = 0;
        diffIndex = 2;
        treeSize = treeList.size();

        this.sight = sight;
    }

    public void reset() {
        nextIndex = 0;
        diffIndex = 2;
    }

    public boolean hasNext() {
        if (nextIndex < treeSize) {
            if (diffIndex > 1) {
                currentNode = treeList.root.get(nextIndex);
            } else {
                currentNode = currentNode.next();
            }

            while (!currentNode.value.isEnabled(sight) || !currentNode.value.isInSight(sight)) {
                if (currentNode.value.isEnabled(sight)) {
                    nextIndex = currentNode.value.pre + 1 + currentNode.value.size;
                    if (nextIndex >= treeSize) {
                        return false;
                    }
                    currentNode = treeList.root.get(nextIndex);
                } else {
                    ++nextIndex;
                    if (nextIndex >= treeSize) {
                        return false;
                    }
                    currentNode = currentNode.next();
                }

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

    public void setSight(Sight sight) {
        this.sight = sight;
    }
}
