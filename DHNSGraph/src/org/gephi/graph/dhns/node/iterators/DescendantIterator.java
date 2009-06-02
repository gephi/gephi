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

import org.gephi.graph.dhns.core.DurableTreeList;
import java.util.Iterator;
import org.gephi.graph.dhns.node.PreNode;
import org.gephi.graph.dhns.core.TreeStructure;
import org.gephi.datastructure.avl.ResetableIterator;
import org.gephi.graph.api.Node;

/**
 * Descendent iterator for all nodes.
 *
 * @author Mathieu Bastian
 */
public class DescendantIterator extends AbstractNodeIterator implements Iterator<Node>, ResetableIterator {

    protected TreeStructure treeStructure;
    protected DurableTreeList.DurableAVLNode next;
    protected int nextIndex = 0;
    protected int limit;

    public DescendantIterator(TreeStructure treeStructure){
        this.treeStructure = treeStructure;
        limit = treeStructure.getTreeSize();
    }

    public DescendantIterator(TreeStructure treeStructure, PreNode node){
        this.treeStructure = treeStructure;
        setNode(node);
    }

    public void setNode(PreNode node) {
        nextIndex = node.getPre()+1;
        limit = node.getPre()+node.size+1;
    }

    public boolean hasNext() {
        return (nextIndex < limit);
    }

    public PreNode next() {
        if (next == null) {
            next = treeStructure.getTree().getNode(nextIndex);
        } else {
            next = next.next();
        }

        PreNode value = next.getValue();
        ++nextIndex;
        return value;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}
