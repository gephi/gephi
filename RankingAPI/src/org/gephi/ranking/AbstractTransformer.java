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
package org.gephi.ranking;

/**
 *
 * @author Mathieu Bastian
 */
public class AbstractTransformer<T> implements Transformer<T> {

    protected T minimum;
    protected T maximum;
    protected T lowerBound;
    protected T upperBound;

    public AbstractTransformer() {
        
    }

    public AbstractTransformer(T minimum, T maximum) {
        this.minimum = minimum;
        this.maximum = maximum;
        this.lowerBound = minimum;
        this.upperBound = maximum;
    }

    public AbstractTransformer(T minimum, T maximum, T lowerBound, T upperBound) {
        this.minimum = minimum;
        this.maximum = maximum;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    public T getLowerBound() {
        return lowerBound;
    }

    public void setLowerBound(T lowerBound) {
        this.lowerBound = lowerBound;
    }

    public T getMaximumValue() {
        return maximum;
    }

    public void setMaximumValue(T maximum) {
        this.maximum = maximum;
    }

    public T getMinimumValue() {
        return minimum;
    }

    public void setMinimumValue(T minimum) {
        this.minimum = minimum;
    }

    public T getUpperBound() {
        return upperBound;
    }

    public void setUpperBound(T upperBound) {
        this.upperBound = upperBound;
    }
}
