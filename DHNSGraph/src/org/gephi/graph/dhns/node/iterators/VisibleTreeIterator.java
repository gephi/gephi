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
package org.gephi.graph.dhns.node.iterators;

import java.util.Iterator;
import org.gephi.graph.dhns.core.DurableTreeList;
import org.gephi.graph.dhns.core.TreeStructure;
import org.gephi.graph.dhns.node.PreNode;
import org.gephi.graph.dhns.core.DurableTreeList.DurableAVLNode;
import org.gephi.datastructure.avl.ResetableIterator;
import org.gephi.graph.api.Node;

/**
 * {@link PreNode} iterator for enabled and visible nodes.
 * 
 * @author Mathieu Bastian
 * @see CompleteTreeIterator
 */
public class VisibleTreeIterator extends AbstractNodeIterator implements Iterator<Node>, ResetableIterator {

    protected int treeSize;
    protected DurableTreeList treeList;
    protected int nextIndex;
    protected int diffIndex;
    protected DurableAVLNode currentNode;

    public VisibleTreeIterator(TreeStructure treeStructure) {
        this.treeList = treeStructure.getTree();
        nextIndex = 1;
        diffIndex = 2;
        treeSize = treeList.size();
    }

    public void reset() {
        nextIndex = 1;
        diffIndex = 2;
    }

    public boolean hasNext() {
        while (true) {
            if (nextIndex < treeSize) {
                if (diffIndex > 1) {
                    currentNode = treeList.getNode(nextIndex);
                } else {
                    currentNode = currentNode.next();
                }

                while (!currentNode.getValue().isEnabled()) {
                    ++nextIndex;
                    if (nextIndex >= treeSize) {
                        return false;
                    }
                    currentNode = currentNode.next();
                }

                if (!currentNode.getValue().isVisible()) {
                    nextIndex = currentNode.getValue().getPre() + 1 + currentNode.getValue().size;
                    diffIndex = nextIndex - currentNode.getValue().pre;
                } else {
                    return true;
                }

            } else {
                return false;
            }
        }
    }

    public PreNode next() {
        nextIndex = currentNode.getValue().getPre() + 1 + currentNode.getValue().size;
        diffIndex = nextIndex - currentNode.getValue().pre;
        PreNode res = currentNode.getValue();
        return res;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}
