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

package gephi.data.network.cache;

import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Mathieu
 */
public class CachedIterator<Type> implements Iterator<Type>
{
    private Iterator<Type> iterator;
    private List<Type> list;

    public CachedIterator(Iterator<Type> iterator, List<Type> list)
    {
        this.iterator = iterator;
        this.list = list;
    }

    public boolean hasNext() {
        return iterator.hasNext();
    }

    public Type next() {
        Type t = iterator.next();
        list.add(t);
        return t;
    }

    public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
