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
but WITHOUType ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.ranking.impl;

import org.gephi.ranking.api.SizeTransformer;

/**
 *
 * @author Mathieu Bastian
 */
public abstract class AbstractSizeTransformer<Target> extends AbstractTransformer<Target> implements SizeTransformer<Target> {

    protected float minSize = 1f;
    protected float maxSize = 4f;

    public AbstractSizeTransformer() {
    }

    public AbstractSizeTransformer(float lowerBound, float upperBound) {
        super(lowerBound, upperBound);
    }

    public AbstractSizeTransformer(float lowerBound, float upperBound, float minSize, float maxSize) {
        super(lowerBound, upperBound);
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

    public float getSize(float normalizedValue) {
        if (interpolator != null) {
            normalizedValue = interpolator.interpolate(normalizedValue);
        }
        return normalizedValue * (maxSize - minSize) + minSize;
    }
}
