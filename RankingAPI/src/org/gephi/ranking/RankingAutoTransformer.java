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
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeEvent;
import org.gephi.data.attributes.api.AttributeListener;
import org.gephi.data.attributes.api.AttributeModel;
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
public class RankingAutoTransformer implements Runnable, AttributeListener {

    private static final long DEFAULT_DELAY = 500;  //ms
    private ScheduledExecutorService executor;
    private final RankingModelImpl model;
    private final GraphController graphController;
    private final AttributeModel attributeModel;
    private final GraphModel graphModel;
    //Verisonning states
    private int lastView = -1;
    private int lastVersion = -1;
    private boolean valueChanged = false;

    public RankingAutoTransformer(RankingModelImpl model) {
        this.model = model;
        graphController = Lookup.getDefault().lookup(GraphController.class);
        graphModel = graphController.getModel(model.getWorkspace());
        attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel(model.getWorkspace());
    }

    public void start() {
        if (executor == null) {
            //Attribute listening
            attributeModel.addAttributeListener(this);

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
        //Attribute stop listening
        attributeModel.removeAttributeListener(this);
        lastVersion = -1;
        lastView = -1;
        valueChanged = false;

        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
            executor = null;
        }
    }

    public void run() {
        HierarchicalGraph graph = graphModel.getHierarchicalGraphVisible();
        int nodeVersion = graph.getNodeVersion();
        int edgeVersion = graph.getEdgeVersion();
        int viewId = graphModel.getVisibleView().getViewId();

        //Test if something changed        
        if (viewId == lastView && (nodeVersion + edgeVersion) == lastVersion && !valueChanged) {
            return;
        }
        lastView = viewId;
        lastVersion = edgeVersion + nodeVersion;
        valueChanged = false;

        for (RankingModelImpl.AutoRanking autoRanking : model.getAutoRankings()) {
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

    public void attributesChanged(AttributeEvent event) {
        if (event.getEventType().equals(AttributeEvent.EventType.SET_VALUE)) {
            valueChanged = true;
        }
    }

    private long getDelayInMs() {
        long defaultDelay = NbPreferences.forModule(RankingAutoTransformer.class).getLong("Ranking_Auto_Transformer_Default_Delay", DEFAULT_DELAY);
        return NbPreferences.forModule(RankingAutoTransformer.class).getLong("Ranking_Auto_Transformer_Delay", defaultDelay);
    }
}
