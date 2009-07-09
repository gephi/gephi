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
import org.gephi.graph.dhns.node.AbstractNode;

/**
 * Basic iterator for the {@link DurableTreeList}.
 * 
 * @author Mathieu Bastian
 */
public class TreeListIterator implements Iterator<AbstractNode> {

    /** The TreeList list */
    protected final DurableTreeList treeList;
    /**
     * Cache of the next node that will be returned by {@link #next()}.
     */
    protected DurableAVLNode next;
    /**
     * The index of the last node that was returned.
     */
    protected int nextIndex;

    /**
     * Create a ListIterator for a list.
     * 
     * @param parent  the parent list
     * @param fromIndex  the index to start at
     */
    public TreeListIterator(DurableTreeList treeList, int fromIndex) throws IndexOutOfBoundsException {
        this.treeList = treeList;
        this.nextIndex = fromIndex;
    }

    public TreeListIterator(DurableTreeList treeList) throws IndexOutOfBoundsException {
        this(treeList, 0);
    }

    public boolean hasNext() {
        return (nextIndex < treeList.size());
    }

    public AbstractNode next() {
        if (next == null) {
            next = treeList.getNode(nextIndex);
        } else {
            next = next.next();
        }

        AbstractNode value = next.getValue();
        value.avlNode.setIndex(nextIndex);
        ++nextIndex;
        return value;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
