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
import org.gephi.graph.dhns.node.PreNode;
import org.gephi.graph.dhns.core.DurableTreeList.DurableAVLNode;
import org.gephi.graph.dhns.core.TreeStructure;
import org.gephi.datastructure.avl.ResetableIterator;
import org.gephi.graph.api.Node;
import org.gephi.graph.dhns.proposition.Proposition;
import org.gephi.graph.dhns.proposition.Tautology;

/**
 * {@link PreNode} iterator for children of a node, enabled or not.
 *
 * @author Mathieu Bastian
 * @see DescendantIterator
 */
public class DescendantIterator extends AbstractNodeIterator implements Iterator<Node>, ResetableIterator {

    protected int treeSize;
    protected DurableTreeList treeList;
    protected int nextIndex;
    protected int diffIndex;
    protected DurableAVLNode currentNode;
    protected boolean loopStart = true;

    //Proposition
    protected Proposition proposition;

    public DescendantIterator(TreeStructure treeStructure, Proposition proposition) {
        this.treeList = treeStructure.getTree();
        nextIndex = 0;
        diffIndex = 2;
        treeSize = treeList.size();
        if (proposition == null) {
            this.proposition = new Tautology();
        } else {
            this.proposition = proposition;
        }
    }

    public DescendantIterator(TreeStructure treeStructure, PreNode node, Proposition proposition) {
        this(treeStructure, proposition);
        setNode(node);
    }

    public void setNode(PreNode node) {
        nextIndex = node.getPre() + 1;
        treeSize = node.getPre() + node.size + 1;
        diffIndex = 2;
    }

    public boolean hasNext() {
        while (loopStart || !proposition.evaluate(currentNode.getValue())) {

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

    public PreNode next() {
        nextIndex++;
        diffIndex=1;
        loopStart = true;
        return currentNode.getValue();
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}
