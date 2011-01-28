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
        if (interpolator != null) {
            normalizedValue = interpolator.interpolate(normalizedValue);
        }
        return linearGradient.getValue(normalizedValue);
    }

    public static class LinearGradient {

        private Color[] colors;
        private float[] positions;

        public LinearGradient(Color colors[], float[] positions) {
            if (colors == null || positions == null) {
                throw new NullPointerException();
            }
            if (colors.length != positions.length) {
                throw new IllegalArgumentException();
            }
            this.colors = colors;
            this.positions = positions;
        }

        public Color getValue(float pos) {
            for (int a = 0; a < positions.length - 1; a++) {
                if (positions[a] == pos) {
                    return colors[a];
                }
                if (positions[a] < pos && pos < positions[a + 1]) {
                    float v = (pos - positions[a]) / (positions[a + 1] - positions[a]);
                    return tween(colors[a], colors[a + 1], v);
                }
            }
            if (pos <= positions[0]) {
                return colors[0];
            }
            if (pos >= positions[positions.length - 1]) {
                return colors[colors.length - 1];
            }
            return null;
        }

        private Color tween(Color c1, Color c2, float p) {
            return new Color(
                    (int) (c1.getRed() * (1 - p) + c2.getRed() * (p)),
                    (int) (c1.getGreen() * (1 - p) + c2.getGreen() * (p)),
                    (int) (c1.getBlue() * (1 - p) + c2.getBlue() * (p)),
                    (int) (c1.getAlpha() * (1 - p) + c2.getAlpha() * (p)));
        }

        public Color[] getColors() {
            return colors;
        }

        public float[] getPositions() {
            return positions;
        }

        public void setColors(Color[] colors) {
            this.colors = colors;
        }

        public void setPositions(float[] positions) {
            this.positions = positions;
        }
    }
}
