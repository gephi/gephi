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
import org.gephi.graph.dhns.core.DurableTreeList.DurableAVLNode;
import org.gephi.graph.dhns.node.PreNode;

/**
 * TreeListIterator for only PreNode
 *
 * @author Mathieu Bastian
 */
public class PreNodeTreeListIterator extends AbstractNodeIterator implements Iterator<PreNode> {

    protected final DurableTreeList treeList;
    protected DurableAVLNode next;
    protected int nextIndex;
    protected int diffIndex;
    protected DurableAVLNode currentNode;

    public PreNodeTreeListIterator(DurableTreeList treeList, int fromIndex) throws IndexOutOfBoundsException {
        this.treeList = treeList;
        this.nextIndex = fromIndex;
        this.diffIndex = 2;
    }

    public PreNodeTreeListIterator(DurableTreeList treeList) throws IndexOutOfBoundsException {
        this(treeList, 0);
    }

    public boolean hasNext() {
        while (true) {
            if (nextIndex < treeList.size()) {
                if (diffIndex > 1) {
                    currentNode = treeList.getNode(nextIndex);
                } else {
                    currentNode = currentNode.next();
                }

                if (currentNode.getValue().isClone()) {
                    nextIndex = currentNode.getValue().getPre() + 1 + currentNode.getValue().size;
                    diffIndex = nextIndex - currentNode.getValue().pre;
                    if (nextIndex >= treeList.size()) {
                        return false;
                    }
                } else {
                    return true;
                }
            } else {
                return false;
            }
        }
    }

    public PreNode next() {
        nextIndex++;
        diffIndex = 1;
        return (PreNode) currentNode.getValue();
    }

    public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
