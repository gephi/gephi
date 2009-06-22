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
package org.gephi.graph.dhns.graph;

import java.util.concurrent.locks.Lock;
import org.gephi.graph.api.Node;
import org.gephi.graph.dhns.node.iterators.AbstractNodeIterator;

/**
 * Iterator for {@link NodeIterableImpl} which validates the given condition.
 *
 * @author Mathieu Bastian
 */
public class NodeIteratorConditionImpl extends NodeIteratorImpl {

    protected Condition<Node> condition;
    protected Node pointer;

    public NodeIteratorConditionImpl(AbstractNodeIterator iterator, Lock lock, Condition<Node> condition) {
        super(iterator, lock);
    }

    @Override
    public boolean hasNext() {
        while (iterator.hasNext()) {
            pointer = iterator.next();
            if (condition.isValid(pointer)) {
                return true;
            }
        }
        if (lock != null) {
            lock.unlock();
        }
        return false;
    }

    @Override
    public Node next() {
        return pointer;
    }
}
