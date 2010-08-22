/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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
package org.gephi.ranking.impl;

import org.gephi.ranking.api.ColorTransformer;
import org.gephi.ranking.api.SizeTransformer;
import org.gephi.ranking.api.NodeRanking;
import org.gephi.ranking.api.Ranking;
import java.awt.Color;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.gephi.ranking.api.LabelColorTransformer;
import org.gephi.ranking.api.LabelSizeTransformer;
import org.gephi.ranking.api.ObjectColorTransformer;
import org.gephi.ranking.api.ObjectSizeTransformer;

/**
 *
 * @author Mathieu Bastian
 */
public class TransformerFactory {

    public static ColorTransformer getObjectColorTransformer(Ranking ranking) {
        AbstractColorTransformer colorTransformer = null;
        if (ranking instanceof NodeRanking) {
            colorTransformer = new NodeColorTransformer();
        } else {
            colorTransformer = new EdgeColorTransformer();

        }
        colorTransformer.setRanking(ranking);
        return colorTransformer;
    }

    public static SizeTransformer getObjectSizeTransformer(Ranking ranking) {
        AbstractSizeTransformer sizeTransformer = null;
        if (ranking instanceof NodeRanking) {
            sizeTransformer = new NodeSizeTransformer();
        } else {
            sizeTransformer = new EdgeWeightTransformer();
        }
        sizeTransformer.setRanking(ranking);
        return sizeTransformer;
    }

    public static ColorTransformer getLabelColorTransformer(Ranking ranking) {
        AbstractColorTransformer colorTransformer = null;
        if (ranking instanceof NodeRanking) {
            colorTransformer = new NodeLabelColorTransformer();
        } else {
            colorTransformer = new EdgeLabelColorTransformer();

        }
        colorTransformer.setRanking(ranking);
        return colorTransformer;
    }

    public static SizeTransformer getLabelSizeTransformer(Ranking ranking) {
        AbstractSizeTransformer sizeTransformer = new NodeSizeTransformer();
        if (ranking instanceof NodeRanking) {
            sizeTransformer = new NodeLabelSizeTransformer();
        } else {
            sizeTransformer = new EdgeLabelSizeTransformer();
        }
        sizeTransformer.setRanking(ranking);
        return sizeTransformer;
    }

    private static class NodeColorTransformer extends AbstractColorTransformer<Node> implements ObjectColorTransformer<Node> {

        public Object transform(Node target, float normalizedValue) {
            Color color = getColor(normalizedValue);
            target.getNodeData().setColor(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f);
            return color;
        }
    }

    private static class EdgeColorTransformer extends AbstractColorTransformer<Edge> implements ObjectColorTransformer<Edge> {

        public Object transform(Edge target, float normalizedValue) {
            Color color = getColor(normalizedValue);
            target.getEdgeData().setColor(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f);
            return color;
        }
    }

    private static class NodeSizeTransformer extends AbstractSizeTransformer<Node> implements ObjectSizeTransformer<Node> {

        public Object transform(Node target, float normalizedValue) {
            float size = getSize(normalizedValue);
            target.getNodeData().setSize(size);
            return Float.valueOf(size);
        }
    }

    private static class EdgeWeightTransformer extends AbstractSizeTransformer<Edge> implements ObjectSizeTransformer<Edge> {

        public Object transform(Edge target, float normalizedValue) {
            float size = getSize(normalizedValue);
            target.setWeight(size);
            return Float.valueOf(size);
        }
    }

    private static class NodeLabelColorTransformer extends AbstractColorTransformer<Node> implements LabelColorTransformer<Node> {

        public Object transform(Node target, float normalizedValue) {
            Color color = getColor(normalizedValue);
            target.getNodeData().getTextData().setColor(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, 1f);
            return color;
        }
    }

    private static class EdgeLabelColorTransformer extends AbstractColorTransformer<Edge> implements LabelColorTransformer<Edge> {

        public Object transform(Edge target, float normalizedValue) {
            Color color = getColor(normalizedValue);
            target.getEdgeData().getTextData().setColor(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, 1f);
            return color;
        }
    }

    private static class NodeLabelSizeTransformer extends AbstractSizeTransformer<Node> implements LabelSizeTransformer<Node> {

        public Object transform(Node target, float normalizedValue) {
            float size = getSize(normalizedValue);
            target.getNodeData().getTextData().setSize(size);
            return Float.valueOf(size);
        }
    }

    private static class EdgeLabelSizeTransformer extends AbstractSizeTransformer<Edge> implements LabelSizeTransformer<Edge> {

        public Object transform(Edge target, float normalizedValue) {
            float size = getSize(normalizedValue);
            target.getEdgeData().getTextData().setSize(size);
            return Float.valueOf(size);
        }
    }
}
