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

import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterator;

/**
 * Iterator for {@link EdgeIterableImpl}.
 *
 * @author Mathieu Bastian
 */
public class EdgeIteratorImpl implements EdgeIterator {

    protected Iterator<Edge> iterator;
    protected Lock lock;

    public EdgeIteratorImpl(Iterator<Edge> iterator, Lock lock) {
        this.iterator = iterator;
        this.lock = lock;
    }

    public boolean hasNext() {
        boolean res = iterator.hasNext();
        if (!res && lock != null) {
            lock.unlock();
        }
        return res;
    }

    public Edge next() {
        return iterator.next();
    }

    public void remove() {
        iterator.remove();
    }
}
