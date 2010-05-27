/*
Copyright 2008-2010 Gephi
Authors : Cezary Bartosiak
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

package org.gephi.data.attributes.type;

import java.util.List;

/**
 * It is essentially a map from intervals to object which can be queried for a
 * value associated with a particular interval of time.
 *
 * @author Cezary Bartosiak
 * @param <T>
 */
public final class IntervalTree<T> {
    public IntervalTree() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public IntervalTree(List<Interval<T>> intervals) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void add(Interval<T> interval) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void addAll(List<Interval<T>> intervals) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void clear() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean contains(Interval<T> interval) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isEmpty() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean remove(Interval<T> interval) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public List<Interval<T>> search(double point) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public List<Interval<T>> search(double min, double max) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int size() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
