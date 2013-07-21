/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.appearance.plugin;

import java.awt.Color;
import java.io.Serializable;
import java.util.Arrays;
import org.gephi.appearance.spi.RankingTransformer;
import org.gephi.graph.api.Element;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author mbastian
 */
@ServiceProvider(service = RankingTransformer.class)
public class RankingElementColorTransformer implements RankingTransformer<Element> {

    protected final LinearGradient linearGradient = new LinearGradient(new Color[]{Color.WHITE, Color.BLACK}, new float[]{0f, 1f});

    @Override
    public void transform(Element element, float rankingValue) {
        Color color = linearGradient.getValue(rankingValue);
        element.setColor(color);
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

    public LinearGradient getLinearGradient() {
        return linearGradient;
    }

    public static class LinearGradient implements Serializable, Cloneable {

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

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final LinearGradient other = (LinearGradient) obj;
            if (!Arrays.deepEquals(this.colors, other.colors)) {
                return false;
            }
            if (!Arrays.equals(this.positions, other.positions)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 17 * hash + Arrays.deepHashCode(this.colors);
            hash = 17 * hash + Arrays.hashCode(this.positions);
            return hash;
        }

        @Override
        protected Object clone() throws CloneNotSupportedException {
            LinearGradient cl = new LinearGradient(colors, positions);
            return cl;
        }
    }
}
