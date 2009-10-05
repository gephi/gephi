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

import java.awt.Color;

/**
 *
 * @author Mathieu Bastian
 */
public class TransformerFactory {

    public static ColorTransformer getColorTransformer(Ranking ranking) {
        AbstractColorTransformer colorTransformer = null;
        if (ranking.getType().equals(Double.class)) {
            colorTransformer = getDoubleColorTransformer();
        } else if (ranking.getType().equals(Float.class)) {
            colorTransformer = getFloatColorTransformer();
        } else if (ranking.getType().equals(Integer.class)) {
            colorTransformer = getIntegerColorTransformer();
        } else if (ranking.getType().equals(Long.class)) {
            colorTransformer = getLongColorTransformer();
        }
        return colorTransformer;
    }

    public static SizeTransformer getSizeTransformer(Ranking ranking) {
        AbstractSizeTransformer sizeTransformer = null;
        if (ranking.getType().equals(Double.class)) {
            sizeTransformer = getDoubleSizeTransformer();
        } else if (ranking.getType().equals(Float.class)) {
            sizeTransformer = getFloatSizeTransformer();
        } else if (ranking.getType().equals(Integer.class)) {
            sizeTransformer = getIntegerSizeTransformer();
        } else if (ranking.getType().equals(Long.class)) {
            sizeTransformer = getLongSizeTransformer();
        }
        return sizeTransformer;
    }

    private static AbstractColorTransformer<Double> getDoubleColorTransformer() {
        return new AbstractColorTransformer<Double>() {

            public Color transform(Double value) {
                float ratio = (float) (value - lowerBound / (upperBound - lowerBound));
                return linearGradient.getValue(ratio);
            }
        };
    }

    private static AbstractColorTransformer<Float> getFloatColorTransformer() {
        return new AbstractColorTransformer<Float>() {

            public Color transform(Float value) {
                float ratio = value - lowerBound / (upperBound - lowerBound);
                return linearGradient.getValue(ratio);
            }
        };
    }

    private static AbstractColorTransformer<Integer> getIntegerColorTransformer() {
        return new AbstractColorTransformer<Integer>() {

            public Color transform(Integer value) {
                float ratio = (float) (value - lowerBound) / (upperBound - lowerBound);
                return linearGradient.getValue(ratio);
            }
        };
    }

    private static AbstractColorTransformer<Long> getLongColorTransformer() {
        return new AbstractColorTransformer<Long>() {

            public Color transform(Long value) {
                float ratio = (float) ((double) (value - lowerBound) / (upperBound - lowerBound));
                return linearGradient.getValue(ratio);
            }
        };
    }

    private static AbstractSizeTransformer<Double> getDoubleSizeTransformer() {
        return new AbstractSizeTransformer<Double>() {

            public float transform(Double value) {
                float ratio = (float) (value - lowerBound / (upperBound - lowerBound));
                return ratio * maxSize + minSize;
            }
        };
    }

    private static AbstractSizeTransformer<Float> getFloatSizeTransformer() {
        return new AbstractSizeTransformer<Float>() {

            public float transform(Float value) {
                float ratio = value - lowerBound / (upperBound - lowerBound);
                return ratio * maxSize + minSize;
            }
        };
    }

    private static AbstractSizeTransformer<Integer> getIntegerSizeTransformer() {
        return new AbstractSizeTransformer<Integer>() {

            public float transform(Integer value) {
                float ratio = (float) (value - lowerBound) / (upperBound - lowerBound);
                return ratio * maxSize + minSize;
            }
        };
    }

    private static AbstractSizeTransformer<Long> getLongSizeTransformer() {
        return new AbstractSizeTransformer<Long>() {

            public float transform(Long value) {
                float ratio = (float) ((double) (value - lowerBound) / (upperBound - lowerBound));
                return ratio * maxSize + minSize;
            }
        };
    }
}
