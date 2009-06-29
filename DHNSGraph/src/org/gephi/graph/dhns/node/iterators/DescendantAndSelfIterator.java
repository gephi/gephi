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
import org.gephi.graph.dhns.node.PreNode;
import org.gephi.graph.dhns.core.TreeStructure;
import org.gephi.datastructure.avl.ResetableIterator;
import org.gephi.graph.api.Node;
import org.gephi.graph.dhns.proposition.Proposition;

/**
 * {@link PreNode} iterator for children of a node, enabled or not.
 *
 * @author Mathieu Bastian
 * @see DescendantAndSelfIterator
 */
public class DescendantAndSelfIterator extends DescendantIterator implements Iterator<Node>, ResetableIterator {

    public DescendantAndSelfIterator(TreeStructure treeStructure, Proposition proposition) {
        super(treeStructure, proposition);
    }

    public DescendantAndSelfIterator(TreeStructure treeStructure, PreNode node, Proposition proposition) {
        super(treeStructure, node, proposition);
    }

    @Override
    public void setNode(PreNode node) {
        nextIndex = node.getPre();
        treeSize = node.getPre() + node.size + 1;
        diffIndex = 2;
    }
}