/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
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
package org.gephi.ranking.plugin.transformer;

import java.io.Serializable;
import org.gephi.ranking.api.Transformer;

/**
 * Abstract transformer implementation. Use the given ranking and interpolator to
 * transform object's appearance.
 * 
 * @author Mathieu Bastian
 */
public abstract class AbstractTransformer<Target> implements Transformer<Target>, Serializable {

    protected float lowerBound = 0f;
    protected float upperBound = 1f;

    public AbstractTransformer() {
    }

    public AbstractTransformer(float lowerBound, float upperBound) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    @Override
    public float getLowerBound() {
        return lowerBound;
    }

    @Override
    public void setLowerBound(float lowerBound) {
        this.lowerBound = lowerBound;
    }

    @Override
    public float getUpperBound() {
        return upperBound;
    }

    @Override
    public void setUpperBound(float upperBound) {
        this.upperBound = upperBound;
    }

    @Override
    public boolean isInBounds(float value) {
        return value >= lowerBound && value <= upperBound;
    }
}
