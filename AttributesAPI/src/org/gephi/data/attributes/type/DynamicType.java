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

import org.gephi.data.attributes.api.AttributeEstimator;

/**
 * A special type which provides methods of getting/setting values of any time
 * interval. It is internally implemented using Interval Tree for efficiency.
 *
 * @author Cezary Bartosiak
 * @param <T>
 */
public abstract class DynamicType<T> {
    public DynamicType() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public T getValue(double min, double max, AttributeEstimator estimator) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setValue(double min, double max, T value) {
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
