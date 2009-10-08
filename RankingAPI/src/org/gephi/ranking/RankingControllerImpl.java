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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.Node;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
public class RankingControllerImpl implements RankingController {

    private RankingModelImpl rankingModelImpl = new RankingModelImpl();
    private RankingEventBus rankingEventBus = new RankingEventBus();

    public RankingModel getRankingModel() {
        return rankingModelImpl;
    }

    public void transform(Transformer transformer) {
        AbstractTransformer abstractTransformer = (AbstractTransformer) transformer;
        Ranking ranking = abstractTransformer.getRanking();

        RankingResultImpl rankingResult = new RankingResultImpl();
        rankingResult.transformer = transformer;
        rankingResult.ranking = ranking;

        Graph graph = Lookup.getDefault().lookup(GraphController.class).getVisibleDirectedGraph();
        if (ranking instanceof NodeRanking) {
            for (Node node : graph.getNodes().toArray()) {
                Object rank = ranking.getValue(node);
                Object result = null;
                if (rank != null && transformer.isInBounds(rank)) {
                    result = transformer.transform(node, rank);
                }
                rankingResult.addResult(node, rank, result);
            }
        } else {
            for (Edge edge : graph.getEdges().toArray()) {
                Object rank = ranking.getValue(edge);
                Object result = null;
                if (rank != null && transformer.isInBounds(rank)) {
                    result = transformer.transform(edge, rank);
                }
                rankingResult.addResult(edge, rank, result);
            }
        }
        rankingEventBus.publishResults(rankingResult);
    }

    public ColorTransformer getColorTransformer(Ranking ranking) {
        ColorTransformer colorTransformer = TransformerFactory.getColorTransformer(ranking);
        Graph graph = Lookup.getDefault().lookup(GraphController.class).getVisibleDirectedGraph();
        if (ranking instanceof NodeRanking) {
            setNodeMinMax((NodeRanking) ranking, graph, colorTransformer);
        } else {
            setEdgeMinMax((EdgeRanking) ranking, graph, colorTransformer);
        }

        return colorTransformer;
    }

    public Lookup getEventBus() {
        return rankingEventBus.getLookup();
    }

    public SizeTransformer getSizeTransformer(Ranking ranking) {
        SizeTransformer sizeTransformer = TransformerFactory.getSizeTransformer(ranking);
        Graph graph = Lookup.getDefault().lookup(GraphController.class).getVisibleDirectedGraph();
        if (ranking instanceof NodeRanking) {
            setNodeMinMax((NodeRanking) ranking, graph, sizeTransformer);
        } else {
            setEdgeMinMax((EdgeRanking) ranking, graph, sizeTransformer);
        }
        return sizeTransformer;
    }

    private void setNodeMinMax(NodeRanking ranking, Graph graph, Transformer transformer) {
        if (ranking.getType().equals(Double.class)) {
            Double minValue = Double.POSITIVE_INFINITY;
            Double maxValue = Double.NEGATIVE_INFINITY;
            for (Node node : graph.getNodes().toArray()) {
                Double value = (Double) ranking.getValue(node);
                if (value != null) {
                    minValue = Math.min(value, minValue);
                    maxValue = Math.max(value, maxValue);
                }
            }
            transformer.setMinimumValue(minValue);
            transformer.setMaximumValue(maxValue);
        } else if (ranking.getType().equals(Float.class)) {
            Float minValue = Float.POSITIVE_INFINITY;
            Float maxValue = Float.NEGATIVE_INFINITY;
            for (Node node : graph.getNodes().toArray()) {
                Float value = (Float) ranking.getValue(node);
                if (value != null) {
                    minValue = Math.min(value, minValue);
                    maxValue = Math.max(value, maxValue);
                }
            }
            transformer.setMinimumValue(minValue);
            transformer.setMaximumValue(maxValue);
        } else if (ranking.getType().equals(Integer.class)) {
            Integer minValue = Integer.MAX_VALUE;
            Integer maxValue = Integer.MIN_VALUE;
            for (Node node : graph.getNodes().toArray()) {
                Integer value = (Integer) ranking.getValue(node);
                if (value != null) {
                    minValue = Math.min(value, minValue);
                    maxValue = Math.max(value, maxValue);
                }
            }
            transformer.setMinimumValue(minValue);
            transformer.setMaximumValue(maxValue);
        } else if (ranking.getType().equals(Long.class)) {
            Long minValue = Long.MAX_VALUE;
            Long maxValue = Long.MIN_VALUE;
            for (Node node : graph.getNodes().toArray()) {
                Long value = (Long) ranking.getValue(node);
                if (value != null) {
                    minValue = Math.min(value, minValue);
                    maxValue = Math.max(value, maxValue);
                }
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
                if (value != null) {
                    minValue = Math.min(value, minValue);
                    maxValue = Math.max(value, maxValue);
                }
            }
            transformer.setMinimumValue(minValue);
            transformer.setMaximumValue(maxValue);
        } else if (ranking.getType().equals(Float.class)) {
            Float minValue = Float.POSITIVE_INFINITY;
            Float maxValue = Float.NEGATIVE_INFINITY;
            for (Edge edge : graph.getEdges().toArray()) {
                Float value = (Float) ranking.getValue(edge);
                if (value != null) {
                    minValue = Math.min(value, minValue);
                    maxValue = Math.max(value, maxValue);
                }
            }
            transformer.setMinimumValue(minValue);
            transformer.setMaximumValue(maxValue);
        } else if (ranking.getType().equals(Integer.class)) {
            Integer minValue = Integer.MAX_VALUE;
            Integer maxValue = Integer.MIN_VALUE;
            for (Edge edge : graph.getEdges().toArray()) {
                Integer value = (Integer) ranking.getValue(edge);
                if (value != null) {
                    minValue = Math.min(value, minValue);
                    maxValue = Math.max(value, maxValue);
                }
            }
            transformer.setMinimumValue(minValue);
            transformer.setMaximumValue(maxValue);
        } else if (ranking.getType().equals(Long.class)) {
            Long minValue = Long.MAX_VALUE;
            Long maxValue = Long.MIN_VALUE;
            for (Edge edge : graph.getEdges().toArray()) {
                Long value = (Long) ranking.getValue(edge);
                if (value != null) {
                    minValue = Math.min(value, minValue);
                    maxValue = Math.max(value, maxValue);
                }
            }
            transformer.setMinimumValue(minValue);
            transformer.setMaximumValue(maxValue);
        }
    }

    private static class RankingResultImpl implements RankingResult {

        private Transformer transformer;
        private Ranking ranking;
        private List<RankingResultLine> lines = new ArrayList<RankingResultLine>();

        public Transformer getTransformer() {
            return transformer;
        }

        public Ranking getRanking() {
            return ranking;
        }

        public void addResult(Object target, Object rank, Object result) {
            if (target == null || rank == null || result == null) {
                return;
            }
            lines.add(new RankingResultLineImpl(target, rank, result));
        }

        public List<RankingResultLine> getResultLines() {
            Collections.sort(lines, new Comparator() {

                public int compare(Object o1, Object o2) {
                    RankingResultLineImpl r1 = (RankingResultLineImpl) o1;
                    RankingResultLineImpl r2 = (RankingResultLineImpl) o2;
                    return ((Comparable) r1.rank).compareTo(r2.rank);
                }
            });
            return lines;
        }

        private static class RankingResultLineImpl implements RankingResultLine {

            private Object target;
            private Object rank;
            private Object result;

            public RankingResultLineImpl(Object target, Object rank, Object result) {
                this.target = target;
                this.rank = rank;
                this.result = result;
            }

            public Object getTarget() {
                return target;
            }

            public Object getResult() {
                return result;
            }

            public Object getRank() {
                return rank;
            }
        }
    }
}
