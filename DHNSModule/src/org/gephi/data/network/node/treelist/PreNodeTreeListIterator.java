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

/**
 * Basic iterator for the {@link PreNodeTreeList}.
 * 
 * @author Mathieu Bastian
 */
public class PreNodeTreeListIterator implements Iterator<PreNode> {

    /** The TreeList list */
    protected final PreNodeTreeList treeList;
    /**
     * Cache of the next node that will be returned by {@link #next()}.
     */
    protected AVLNode next;
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
    public PreNodeTreeListIterator(PreNodeTreeList treeList, int fromIndex) throws IndexOutOfBoundsException {
        super();
        this.treeList = treeList;
        this.nextIndex = fromIndex;
    }

    public PreNodeTreeListIterator(PreNodeTreeList treeList) throws IndexOutOfBoundsException {
        this(treeList, 0);
    }

    public boolean hasNext() {
        return (nextIndex < treeList.size);
    }

    public PreNode next() {
        if (next == null) {
            next = treeList.root.get(nextIndex);
        } else {
            next = next.next();
        }

        PreNode value = next.value;
        value.avlNode.setIndex(nextIndex);
        ++nextIndex;
        return value;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
