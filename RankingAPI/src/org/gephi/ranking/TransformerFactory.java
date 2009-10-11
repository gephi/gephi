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

import org.gephi.ranking.api.ColorTransformer;
import org.gephi.ranking.api.SizeTransformer;
import org.gephi.ranking.api.NodeRanking;
import org.gephi.ranking.api.Ranking;
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
            colorTransformer = new NodeColorTransformer();
        } else {
            colorTransformer = new EdgeColorTransformer();

        }
        colorTransformer.setRanking(ranking);
        return colorTransformer;
    }

    public static SizeTransformer getSizeTransformer(Ranking ranking) {
        AbstractSizeTransformer sizeTransformer = new NodeSizeTransformer();

        sizeTransformer.setRanking(ranking);
        return sizeTransformer;
    }

    private static class NodeColorTransformer extends AbstractColorTransformer<Node> {

        public Object transform(Node target, float normalizedValue) {
            Color color = getColor(normalizedValue);
            target.getNodeData().setColor(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f);
            return color;
        }
    }

    private static class EdgeColorTransformer extends AbstractColorTransformer<Edge> {

        public Object transform(Edge target, float normalizedValue) {
            Color color = getColor(normalizedValue);
            target.getEdgeData().setColor(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f);
            return color;
        }
    }

    private static class NodeSizeTransformer extends AbstractSizeTransformer<Node> {

        public Object transform(Node target, float normalizedValue) {
            float size = getSize(normalizedValue);
            target.getNodeData().setSize(size);
            return Float.valueOf(size);
        }
    }
}
