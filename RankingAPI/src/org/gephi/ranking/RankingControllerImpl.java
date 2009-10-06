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

import java.util.ArrayList;
import java.util.List;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Mathieu Bastian
 */
public class RankingControllerImpl implements RankingController {

    public AbstractLookup rankingLookup;
    public InstanceContent rankingContent;

    public Ranking[] getNodeRanking() {
        AttributeController attributeController = Lookup.getDefault().lookup(AttributeController.class);
        List<Ranking> rankingList = new ArrayList<Ranking>();
        for (AttributeColumn column : attributeController.getTemporaryAttributeManager().getNodeClass().getAttributeColumns()) {
            if (RankingFactory.isNumberColumn(column)) {
                rankingList.add(RankingFactory.getNodeAttributeRanking(column));
            }
        }
        return rankingList.toArray(new Ranking[0]);
    }

    public ColorTransformer getColorTransformer(NodeRanking ranking) {
        ColorTransformer colorTransformer = TransformerFactory.getColorTransformer(ranking);
        Graph graph = Lookup.getDefault().lookup(GraphController.class).getVisibleDirectedGraph();
        setNodeMinMax(ranking, graph, colorTransformer);
        return colorTransformer;
    }

    public ColorTransformer getColorTransformer(EdgeRanking ranking) {
        ColorTransformer colorTransformer = TransformerFactory.getColorTransformer(ranking);
        Graph graph = Lookup.getDefault().lookup(GraphController.class).getVisibleDirectedGraph();
        setEdgeMinMax(ranking, graph, colorTransformer);
        return colorTransformer;
    }

    public SizeTransformer getSizeTransformer(NodeRanking ranking) {
        SizeTransformer sizeTransformer = TransformerFactory.getSizeTransformer(ranking);
        Graph graph = Lookup.getDefault().lookup(GraphController.class).getVisibleDirectedGraph();
        setNodeMinMax(ranking, graph, sizeTransformer);
        return sizeTransformer;
    }

    private void setNodeMinMax(NodeRanking ranking, Graph graph, Transformer transformer) {
        if (ranking.getType().equals(Double.class)) {
            Double minValue = Double.POSITIVE_INFINITY;
            Double maxValue = Double.NEGATIVE_INFINITY;
            for (Node node : graph.getNodes().toArray()) {
                Double value = (Double) ranking.getValue(node);
                minValue = Math.min(value, minValue);
                maxValue = Math.max(value, maxValue);
            }
            transformer.setMinimumValue(minValue);
            transformer.setMaximumValue(maxValue);
        } else if (ranking.getType().equals(Float.class)) {
            Float minValue = Float.POSITIVE_INFINITY;
            Float maxValue = Float.NEGATIVE_INFINITY;
            for (Node node : graph.getNodes().toArray()) {
                Float value = (Float) ranking.getValue(node);
                minValue = Math.min(value, minValue);
                maxValue = Math.max(value, maxValue);
            }
            transformer.setMinimumValue(minValue);
            transformer.setMaximumValue(maxValue);
        } else if (ranking.getType().equals(Integer.class)) {
            Integer minValue = Integer.MAX_VALUE;
            Integer maxValue = Integer.MIN_VALUE;
            for (Node node : graph.getNodes().toArray()) {
                Integer value = (Integer) ranking.getValue(node);
                minValue = Math.min(value, minValue);
                maxValue = Math.max(value, maxValue);
            }
            transformer.setMinimumValue(minValue);
            transformer.setMaximumValue(maxValue);
        } else if (ranking.getType().equals(Long.class)) {
            Long minValue = Long.MAX_VALUE;
            Long maxValue = Long.MIN_VALUE;
            for (Node node : graph.getNodes().toArray()) {
                Long value = (Long) ranking.getValue(node);
                minValue = Math.min(value, minValue);
                maxValue = Math.max(value, maxValue);
            }
            transformer.setMinimumValue(minValue);
            transformer.setMaximumValue(maxValue);
        }
    }

    private void setEdgeMinMax(EdgeRanking ranking, Graph graph, Transformer transformer) {
        if (ranking.getType().equals(Double.class)) {
            Double minValue = Double.POSITIVE_INFINITY;
            Double maxValue = Double.NEGATIVE_INFINITY;
            for (Edge edge : graph.getEdges().toArray()) {
                Double value = (Double) ranking.getValue(edge);
                minValue = Math.min(value, minValue);
                maxValue = Math.max(value, maxValue);
            }
            transformer.setMinimumValue(minValue);
            transformer.setMaximumValue(maxValue);
        } else if (ranking.getType().equals(Float.class)) {
            Float minValue = Float.POSITIVE_INFINITY;
            Float maxValue = Float.NEGATIVE_INFINITY;
            for (Edge edge : graph.getEdges().toArray()) {
                Float value = (Float) ranking.getValue(edge);
                minValue = Math.min(value, minValue);
                maxValue = Math.max(value, maxValue);
            }
            transformer.setMinimumValue(minValue);
            transformer.setMaximumValue(maxValue);
        } else if (ranking.getType().equals(Integer.class)) {
            Integer minValue = Integer.MAX_VALUE;
            Integer maxValue = Integer.MIN_VALUE;
            for (Edge edge : graph.getEdges().toArray()) {
                Integer value = (Integer) ranking.getValue(edge);
                minValue = Math.min(value, minValue);
                maxValue = Math.max(value, maxValue);
            }
            transformer.setMinimumValue(minValue);
            transformer.setMaximumValue(maxValue);
        } else if (ranking.getType().equals(Long.class)) {
            Long minValue = Long.MAX_VALUE;
            Long maxValue = Long.MIN_VALUE;
            for (Edge edge : graph.getEdges().toArray()) {
                Long value = (Long) ranking.getValue(edge);
                minValue = Math.min(value, minValue);
                maxValue = Math.max(value, maxValue);
            }
            transformer.setMinimumValue(minValue);
            transformer.setMaximumValue(maxValue);
        }
    }
}
