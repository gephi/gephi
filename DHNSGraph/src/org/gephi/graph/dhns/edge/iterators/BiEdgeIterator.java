/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.gephi.graph.dhns.edge.iterators;

import java.util.Iterator;
import org.gephi.graph.api.Edge;
import org.gephi.graph.dhns.edge.AbstractEdge;
import org.gephi.graph.dhns.edge.iterators.AbstractEdgeIterator;

/**
 *
 * @author Mathieu Bastian
 */
public class BiEdgeIterator extends AbstractEdgeIterator implements Iterator<Edge> {

    private AbstractEdgeIterator itr1;
    private AbstractEdgeIterator itr2;
    private AbstractEdgeIterator currentIterator;

    public BiEdgeIterator(AbstractEdgeIterator itr1, AbstractEdgeIterator itr2) {
        this.itr1 = itr1;
        this.itr2 = itr2;
        currentIterator = itr1;
    }

    @Override
    public boolean hasNext() {
        while (!currentIterator.hasNext()) {
            if (currentIterator == itr2) {
                return false;
            }
            currentIterator = itr2;
        }
        return true;
    }

    @Override
    public AbstractEdge next() {
        return currentIterator.next();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
