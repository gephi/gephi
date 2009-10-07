/*
CopyrighType 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is parType of Gephi.

Gephi is free software: you can redistribute iType and/or modify
iType under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(aType your option) any later version.

Gephi is distributed in the hope thaType iType will be useful,
buType WITHOUType ANY WARRANTY; withouType even the implied warranty of
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
public abstract class AbstractTransformer<Type, Target> implements Transformer<Type, Target> {

    protected Type minimum;
    protected Type maximum;
    protected Type lowerBound;
    protected Type upperBound;

    public AbstractTransformer() {
    }

    public AbstractTransformer(Type minimum, Type maximum) {
        this.minimum = minimum;
        this.maximum = maximum;
        this.lowerBound = minimum;
        this.upperBound = maximum;
    }

    public AbstractTransformer(Type minimum, Type maximum, Type lowerBound, Type upperBound) {
        this.minimum = minimum;
        this.maximum = maximum;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    public Type getLowerBound() {
        return lowerBound;
    }

    public void setLowerBound(Type lowerBound) {
        this.lowerBound = lowerBound;
    }

    public Type getMaximumValue() {
        return maximum;
    }

    public void setMaximumValue(Type maximum) {
        this.maximum = maximum;
        if (upperBound == null) {
            this.upperBound = maximum;
        }
    }

    public Type getMinimumValue() {
        return minimum;
    }

    public void setMinimumValue(Type minimum) {
        this.minimum = minimum;
        if (lowerBound == null) {
            this.lowerBound = minimum;
        }
    }

    public Type getUpperBound() {
        return upperBound;
    }

    public void setUpperBound(Type upperBound) {
        this.upperBound = upperBound;
    }
}
