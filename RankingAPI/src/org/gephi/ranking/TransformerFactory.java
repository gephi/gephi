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
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;

/**
 *
 * @author Mathieu Bastian
 */
public class TransformerFactory {

    public static ColorTransformer getColorTransformer(Ranking ranking) {
        AbstractColorTransformer colorTransformer = null;
        if (ranking instanceof NodeRanking) {
            if (ranking.getType().equals(Double.class)) {
                colorTransformer = getNodeDoubleColorTransformer();
            } else if (ranking.getType().equals(Float.class)) {
                colorTransformer = getNodeFloatColorTransformer();
            } else if (ranking.getType().equals(Integer.class)) {
                colorTransformer = getNodeIntegerColorTransformer();
            } else if (ranking.getType().equals(Long.class)) {
                colorTransformer = getNodeLongColorTransformer();
            }
        } else {
            if (ranking.getType().equals(Double.class)) {
                colorTransformer = getEdgeDoubleColorTransformer();
            } else if (ranking.getType().equals(Float.class)) {
                colorTransformer = getEdgeFloatColorTransformer();
            } else if (ranking.getType().equals(Integer.class)) {
                colorTransformer = getEdgeIntegerColorTransformer();
            } else if (ranking.getType().equals(Long.class)) {
                colorTransformer = getEdgeLongColorTransformer();
            }
        }


        return colorTransformer;
    }

    public static SizeTransformer getSizeTransformer(Ranking ranking) {
        AbstractSizeTransformer sizeTransformer = null;
        if (ranking.getType().equals(Double.class)) {
            sizeTransformer = getNodeDoubleSizeTransformer();
        } else if (ranking.getType().equals(Float.class)) {
            sizeTransformer = getNodeFloatSizeTransformer();
        } else if (ranking.getType().equals(Integer.class)) {
            sizeTransformer = getNodeIntegerSizeTransformer();
        } else if (ranking.getType().equals(Long.class)) {
            sizeTransformer = getNodeLongSizeTransformer();
        }
        return sizeTransformer;
    }

    private static AbstractColorTransformer<Double, Node> getNodeDoubleColorTransformer() {
        return new AbstractColorTransformer<Double, Node>() {

            public Color getResult(Double value) {
                float ratio = (float) (value - lowerBound / (upperBound - lowerBound));
                return linearGradient.getValue(ratio);
            }

            public void transform(Node target, Double value) {
                Color color = getResult(value);
                target.getNodeData().setColor(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f);
            }
        };
    }

    private static AbstractColorTransformer<Float, Node> getNodeFloatColorTransformer() {
        return new AbstractColorTransformer<Float, Node>() {

            public Color getResult(Float value) {
                float ratio = value - lowerBound / (upperBound - lowerBound);
                return linearGradient.getValue(ratio);
            }

            public void transform(Node target, Float value) {
                Color color = getResult(value);
                target.getNodeData().setColor(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f);
            }
        };
    }

    private static AbstractColorTransformer<Integer, Node> getNodeIntegerColorTransformer() {
        return new AbstractColorTransformer<Integer, Node>() {

            public Color getResult(Integer value) {
                float ratio = (float) (value - lowerBound) / (upperBound - lowerBound);
                return linearGradient.getValue(ratio);
            }

            public void transform(Node target, Integer value) {
                Color color = getResult(value);
                target.getNodeData().setColor(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f);
            }
        };
    }

    private static AbstractColorTransformer<Long, Node> getNodeLongColorTransformer() {
        return new AbstractColorTransformer<Long, Node>() {

            public Color getResult(Long value) {
                float ratio = (float) ((double) (value - lowerBound) / (upperBound - lowerBound));
                return linearGradient.getValue(ratio);
            }

            public void transform(Node target, Long value) {
                Color color = getResult(value);
                target.getNodeData().setColor(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f);
            }
        };
    }

    private static AbstractSizeTransformer<Double, Node> getNodeDoubleSizeTransformer() {
        return new AbstractSizeTransformer<Double, Node>() {

            public float getResult(Double value) {
                float ratio = (float) (value - lowerBound / (upperBound - lowerBound));
                return ratio * maxSize + minSize;
            }

            public void transform(Node target, Double value) {
                target.getNodeData().setSize(getResult(value));
            }
        };
    }

    private static AbstractSizeTransformer<Float, Node> getNodeFloatSizeTransformer() {
        return new AbstractSizeTransformer<Float, Node>() {

            public float getResult(Float value) {
                float ratio = value - lowerBound / (upperBound - lowerBound);
                return ratio * maxSize + minSize;
            }

            public void transform(Node target, Float value) {
                target.getNodeData().setSize(getResult(value));
            }
        };
    }

    private static AbstractSizeTransformer<Integer, Node> getNodeIntegerSizeTransformer() {
        return new AbstractSizeTransformer<Integer, Node>() {

            public float getResult(Integer value) {
                float ratio = (float) (value - lowerBound) / (upperBound - lowerBound);
                return ratio * maxSize + minSize;
            }

            public void transform(Node target, Integer value) {
                target.getNodeData().setSize(getResult(value));
            }
        };
    }

    private static AbstractSizeTransformer<Long, Node> getNodeLongSizeTransformer() {
        return new AbstractSizeTransformer<Long, Node>() {

            public float getResult(Long value) {
                float ratio = (float) ((double) (value - lowerBound) / (upperBound - lowerBound));
                return ratio * maxSize + minSize;
            }

            public void transform(Node target, Long value) {
                target.getNodeData().setSize(getResult(value));
            }
        };
    }

    private static AbstractColorTransformer<Double, Edge> getEdgeDoubleColorTransformer() {
        return new AbstractColorTransformer<Double, Edge>() {

            public Color getResult(Double value) {
                float ratio = (float) (value - lowerBound / (upperBound - lowerBound));
                return linearGradient.getValue(ratio);
            }

            public void transform(Edge target, Double value) {
                Color color = getResult(value);
                target.getEdgeData().setColor(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f);
            }
        };
    }

    private static AbstractColorTransformer<Float, Edge> getEdgeFloatColorTransformer() {
        return new AbstractColorTransformer<Float, Edge>() {

            public Color getResult(Float value) {
                float ratio = value - lowerBound / (upperBound - lowerBound);
                return linearGradient.getValue(ratio);
            }

            public void transform(Edge target, Float value) {
                Color color = getResult(value);
                target.getEdgeData().setColor(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f);
            }
        };
    }

    private static AbstractColorTransformer<Integer, Edge> getEdgeIntegerColorTransformer() {
        return new AbstractColorTransformer<Integer, Edge>() {

            public Color getResult(Integer value) {
                float ratio = (float) (value - lowerBound) / (upperBound - lowerBound);
                return linearGradient.getValue(ratio);
            }

            public void transform(Edge target, Integer value) {
                Color color = getResult(value);
                target.getEdgeData().setColor(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f);
            }
        };
    }

    private static AbstractColorTransformer<Long, Edge> getEdgeLongColorTransformer() {
        return new AbstractColorTransformer<Long, Edge>() {

            public Color getResult(Long value) {
                float ratio = (float) ((double) (value - lowerBound) / (upperBound - lowerBound));
                return linearGradient.getValue(ratio);
            }

            public void transform(Edge target, Long value) {
                Color color = getResult(value);
                target.getEdgeData().setColor(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f);
            }
        };
    }
}
