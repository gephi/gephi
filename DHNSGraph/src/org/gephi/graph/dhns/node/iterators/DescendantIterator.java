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
import org.gephi.datastructure.avl.ResetableIterator;
import org.gephi.graph.api.Node;
import org.gephi.graph.dhns.core.DurableTreeList.DurableAVLNode;
import org.gephi.graph.dhns.node.AbstractNode;
import org.gephi.graph.dhns.predicate.Predicate;

/**
 * {@link AbstractNode} iterator for descendant of a node or the whole structure.
 *
 * @author Mathieu Bastian
 * @see ChildrenIterator
 */
public class DescendantIterator extends AbstractNodeIterator implements Iterator<Node>, ResetableIterator {

    protected int treeSize;
    protected DurableTreeList treeList;
    protected int nextIndex;
    protected int diffIndex;
    protected DurableAVLNode currentNode;
    protected boolean loopStart = true;

    //Proposition
    protected Predicate<AbstractNode> predicate;

    public DescendantIterator(TreeStructure treeStructure, Predicate<AbstractNode> predicate) {
        this.treeList = treeStructure.getTree();
        nextIndex = 0;
        diffIndex = 2;
        treeSize = treeList.size();
        this.predicate = predicate;
    }

    public DescendantIterator(TreeStructure treeStructure, AbstractNode node, Predicate<AbstractNode> proposition) {
        this(treeStructure, proposition);
        setNode(node);
    }

    public void setNode(AbstractNode node) {
        nextIndex = node.getPre() + 1;
        treeSize = node.getPre() + node.size + 1;
        diffIndex = 2;
    }

    public boolean hasNext() {
        while (loopStart || !predicate.evaluate(currentNode.getValue())) {

            if (!loopStart) {
                nextIndex = currentNode.getValue().getPre() + 1 + currentNode.getValue().size;
                diffIndex = nextIndex - currentNode.getValue().pre;
            }
            loopStart = false;

            if (nextIndex < treeSize) {
                if (diffIndex > 1) {
                    currentNode = treeList.getNode(nextIndex);
                } else {
                    currentNode = currentNode.next();
                }
            } else {
                return false;
            }
        }
        return true;
    }

    public AbstractNode next() {
        nextIndex++;
        diffIndex = 1;
        loopStart = true;
        return currentNode.getValue();
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}
