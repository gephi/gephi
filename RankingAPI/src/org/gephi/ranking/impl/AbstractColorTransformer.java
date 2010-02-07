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

import org.gephi.ranking.api.ColorTransformer;
import java.awt.Color;
import org.gephi.ui.utils.GradientUtils.LinearGradient;

/**
 *
 * @author Mathieu Bastian
 */
public abstract class AbstractColorTransformer<Target> extends AbstractTransformer<Target> implements ColorTransformer<Target> {

    protected LinearGradient linearGradient = new LinearGradient(new Color[]{Color.WHITE, Color.BLACK}, new float[]{0f, 1f});

    public AbstractColorTransformer() {
    }

    public AbstractColorTransformer(float lowerBound, float upperBound) {
        super(lowerBound, upperBound);
    }

    public AbstractColorTransformer(float lowerBound, float upperBound, Color[] colors, float[] positions) {
        super(lowerBound, upperBound);
        this.linearGradient = new LinearGradient(colors, positions);
    }

    public AbstractColorTransformer(Color[] colors, float[] positions) {
        this.linearGradient = new LinearGradient(colors, positions);
    }

    public float[] getColorPositions() {
        return linearGradient.getPositions();
    }

    public Color[] getColors() {
        return linearGradient.getColors();
    }

    public void setColorPositions(float[] positions) {
        linearGradient.setPositions(positions);
    }

    public void setColors(Color[] colors) {
        linearGradient.setColors(colors);
    }

    public Color getColor(float normalizedValue) {
        if(interpolator!=null) {
            normalizedValue = interpolator.interpolate(normalizedValue);
        }
        return linearGradient.getValue(normalizedValue);
    }
}
