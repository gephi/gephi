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
import org.gephi.graph.dhns.core.DurableTreeList.DurableAVLNode;
import org.gephi.datastructure.avl.ResetableIterator;
import org.gephi.graph.api.Node;
import org.gephi.graph.dhns.node.AbstractNode;
import org.gephi.graph.dhns.proposition.Proposition;
import org.gephi.graph.dhns.proposition.Tautology;

/**
 * {@link AbstractNode} iterator for enabled and visible nodes.
 * 
 * @author Mathieu Bastian
 */
public class TreeIterator extends AbstractNodeIterator implements Iterator<Node>, ResetableIterator {

    protected int treeSize;
    protected DurableTreeList treeList;
    protected int nextIndex;
    protected int diffIndex;
    protected DurableAVLNode currentNode;

    //Proposition
    protected Proposition<AbstractNode> proposition;

    public TreeIterator(TreeStructure treeStructure, Proposition<AbstractNode> proposition) {
        this.treeList = treeStructure.getTree();
        nextIndex = 1;
        diffIndex = 2;
        treeSize = treeList.size();
        if (proposition == null) {
            this.proposition = new Tautology();
        } else {
            this.proposition = proposition;
        }
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

                if (currentNode.getValue().isClone()) {
                    nextIndex = currentNode.getValue().getPre() + 1 + currentNode.getValue().size;
                    diffIndex = nextIndex - currentNode.getValue().pre;
                    if (nextIndex >= treeList.size()) {
                        return false;
                    }
                } else {
                    while (!proposition.evaluate(currentNode.getValue())) {
                        ++nextIndex;
                        if (nextIndex >= treeSize) {
                            return false;
                        }
                        currentNode = currentNode.next();
                    }
                    return true;
                }
                
            /*if (!proposition.evaluate(currentNode.getValue())) {
            nextIndex = currentNode.getValue().getPre() + 1 + currentNode.getValue().size;
            diffIndex = nextIndex - currentNode.getValue().pre;
            } else {
            return true;
            }*/

            } else {
                return false;
            }
        }
    }

    public AbstractNode next() {
        nextIndex = currentNode.getValue().getPre() + 1 + currentNode.getValue().size;
        diffIndex = nextIndex - currentNode.getValue().pre;
        AbstractNode res = currentNode.getValue();
        return res;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}
