/*
 * Copyright Clement Levallois 2021-2023. License Attribution 4.0 Intertnational (CC BY 4.0)
 */
package org.gephi.layout.plugin.forceAtlas2;

import org.gephi.graph.api.Edge;
import org.gephi.layout.plugin.forceAtlas2.ForceFactorySpeed.AttractionForce;

/**
 *
 * @author LEVALLOIS
 */
public class EdgeSpeedProcessor implements Runnable {

    private final double[] nodesInfo;
    private final AttractionForce attraction;
    private final int sourceNodeIndexInNodeInfo;
    private final int targetNodeIndexInNodeInfo;
    private final double edgeWeight;

    public EdgeSpeedProcessor(AttractionForce attraction, double[] nodesInfo, int sourceNodeIndexInNodeInfo, int targetNodeIndexInNodeInfo, double edgeWeight) {
        this.nodesInfo = nodesInfo;
        this.attraction = attraction;
        this.sourceNodeIndexInNodeInfo = sourceNodeIndexInNodeInfo;
        this.targetNodeIndexInNodeInfo = targetNodeIndexInNodeInfo;
        this.edgeWeight = edgeWeight;
    }

    @Override
    public void run() {
        attraction.applyAttraction(nodesInfo, sourceNodeIndexInNodeInfo, targetNodeIndexInNodeInfo, edgeWeight);
    }
}
