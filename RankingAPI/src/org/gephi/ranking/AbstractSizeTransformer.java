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
public abstract class AbstractSizeTransformer<T> extends AbstractTransformer<T> implements SizeTransformer<T> {

    protected float minSize;
    protected float maxSize;

    public AbstractSizeTransformer() {
    }

    public AbstractSizeTransformer(T minimum, T maximum) {
        super(minimum, maximum);
    }

    public AbstractSizeTransformer(T minimum, T maximum, T lowerBound, T upperBound) {
        super(minimum, maximum, lowerBound, upperBound);
    }

    public AbstractSizeTransformer(T minimum, T maximum, T lowerBound, T upperBound, float minSize, float maxSize) {
        super(minimum, maximum, lowerBound, upperBound);
        this.minSize = minSize;
        this.maxSize = maxSize;
    }

    public AbstractSizeTransformer(T minimum, T maximum, float minSize, float maxSize) {
        super(minimum, maximum);
        this.minSize = minSize;
        this.maxSize = maxSize;
    }

    public float getMinSize() {
        return minSize;
    }

    public float getMaxSize() {
        return maxSize;
    }

    public void setMinSize(float minSize) {
        this.minSize = minSize;
    }

    public void setMaxSize(float maxSize) {
        this.maxSize = maxSize;
    }
}
