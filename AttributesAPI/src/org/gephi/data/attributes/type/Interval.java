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

/**
 * This class represents an interval with some value.
 *
 * @author Cezary Bartosiak
 * @param <T>
 */
public final class Interval<T> implements Comparable<Interval<T>> {
    public Interval(double min, double max, T value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int compareTo(Interval<T> interval) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public T getValue() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setValue(T value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    double getMin() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    double getMax() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setMin() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setMax() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean overlapsWith(double point) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean overlapsWith(Interval<?> interval) {
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
