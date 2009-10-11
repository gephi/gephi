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

import org.gephi.ranking.api.Interpolator;
import org.gephi.ranking.api.Transformer;
import org.gephi.ranking.api.Ranking;

/**
 *
 * @author Mathieu Bastian
 */
public abstract class AbstractTransformer<Target> implements Transformer<Target> {

    protected Ranking ranking;
    protected float lowerBound = 0f;
    protected float upperBound = 1f;
    protected Interpolator interpolator;

    public AbstractTransformer() {
    }

    public AbstractTransformer(float lowerBound, float upperBound) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    public float getLowerBound() {
        return lowerBound;
    }

    public void setLowerBound(float lowerBound) {
        this.lowerBound = lowerBound;
    }

    public float getUpperBound() {
        return upperBound;
    }

    public void setUpperBound(float upperBound) {
        this.upperBound = upperBound;
    }

    public boolean isInBounds(float value) {
        return value >= lowerBound && value <= upperBound;
    }

    public Ranking getRanking() {
        return ranking;
    }

    public void setRanking(Ranking ranking) {
        this.ranking = ranking;
    }

    public void setInterpolator(Interpolator interpolator) {
        this.interpolator = interpolator;
    }
}
