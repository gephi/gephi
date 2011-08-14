/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
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
package org.gephi.ranking;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;
import org.gephi.ranking.api.Interpolator;
import org.gephi.ranking.api.Ranking;
import org.gephi.ranking.api.Transformer;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;

/**
 * Scheduled thread executor executing the ranking at a fixed delay.
 * 
 * @author Mathieu Bastian
 */
public class RankingAutoTransformer implements Runnable {

    private static final long DEFAULT_DELAY = 500;
    private ScheduledExecutorService executor;
    private final RankingModelImpl model;
    private final GraphController graphController;
    private final GraphModel graphModel;

    public RankingAutoTransformer(RankingModelImpl model) {
        this.model = model;
        graphController = Lookup.getDefault().lookup(GraphController.class);
        graphModel = graphController.getModel(model.getWorkspace());
    }

    public void start() {
        if (executor == null) {
            executor = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {

                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "Ranking Auto Transformer");
                    return t;
                }
            });
            executor.scheduleWithFixedDelay(this, 0, getDelayInMs(), TimeUnit.MILLISECONDS);
        }
    }

    public void stop() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
            executor = null;
        }
    }

    public void run() {
        for (RankingModelImpl.AutoRanking autoRanking : model.getAutoRankings()) {
            HierarchicalGraph graph = graphModel.getHierarchicalGraphVisible();
            Interpolator interpolator = model.getInterpolator();
            Ranking ranking = autoRanking.getRanking();
            Transformer transformer = autoRanking.getTransformer();

            if (ranking.getElementType().equals(Ranking.NODE_ELEMENT)) {
                for (Node node : graph.getNodes().toArray()) {
                    Number value = ranking.getValue(node);
                    if (value != null) {
                        float normalizedValue = ranking.normalize(value);
                        if (transformer.isInBounds(normalizedValue)) {
                            normalizedValue = interpolator.interpolate(normalizedValue);
                            transformer.transform(node.getNodeData(), normalizedValue);
                        }
                    }
                }
            } else if (ranking.getElementType().equals(Ranking.EDGE_ELEMENT)) {
                for (Edge edge : graph.getEdgesAndMetaEdges().toArray()) {
                    Number value = ranking.getValue(edge);
                    if (value != null) {
                        float normalizedValue = ranking.normalize(value);
                        if (transformer.isInBounds(normalizedValue)) {
                            normalizedValue = interpolator.interpolate(normalizedValue);
                            transformer.transform(edge.getEdgeData(), normalizedValue);
                        }
                    }
                }
            }
        }
    }

    private long getDelayInMs() {
        long defaultDelay = NbPreferences.forModule(RankingAutoTransformer.class).getLong("Ranking_Auto_Transformer_Default_Delay", DEFAULT_DELAY);
        return NbPreferences.forModule(RankingAutoTransformer.class).getLong("Ranking_Auto_Transformer_Delay", defaultDelay);
    }
}
