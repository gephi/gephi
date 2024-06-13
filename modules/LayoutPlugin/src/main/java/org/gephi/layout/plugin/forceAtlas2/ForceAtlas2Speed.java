/*
 Copyright 2008-2011 Gephi
 Authors : Mathieu Jacomy <mathieu.jacomy@gmail.com>
 Website : http://www.gephi.org

 This file is part of Gephi.

 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

 Copyright 2011 Gephi Consortium. All rights reserved.

 The contents of this file are subject to the terms of either the GNU
 General Public License Version 3 only ("GPL") or the Common
 Development and Distribution License("CDDL") (collectively, the
 "License"). You may not use this file except in compliance with the
 License. You can obtain a copy of the License at
 http://gephi.org/about/legal/license-notice/
 or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
 specific language governing permissions and limitations under the
 License.  When distributing the software, include this License Header
 Notice in each file and include the License files at
 /cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
 License Header, with the fields enclosed by brackets [] replaced by
 your own identifying information:
 "Portions Copyrighted [year] [name of copyright owner]"

 If you wish your version of this file to be governed by only the CDDL
 or only the GPL Version 3, indicate your decision by adding
 "[Contributor] elects to include this software in this distribution
 under the [CDDL or GPL Version 3] license." If you do not indicate a
 single choice of license, a recipient has the option to distribute
 your version of this file under either the CDDL, the GPL Version 3 or
 to extend the choice of license to its licensees as provided above.
 However, if you add GPL Version 3 code and therefore, elected the GPL
 Version 3 license, then the option applies only if the new code is
 made subject to such option by the copyright holder.

 Contributor(s):

 Portions Copyrighted 2011 Gephi Consortium.
 */
package org.gephi.layout.plugin.forceAtlas2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Interval;
import org.gephi.graph.api.Node;
import org.gephi.layout.plugin.AbstractLayout;
import org.gephi.layout.plugin.forceAtlas2.ForceFactorySpeed.AttractionForce;
import org.gephi.layout.plugin.forceAtlas2.ForceFactorySpeed.RepulsionForce;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutProperty;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * ForceAtlas 2 Layout, manages each step of the computations.
 *
 * @author Mathieu Jacomy
 */
public class ForceAtlas2Speed implements Layout {

    private final ForceAtlas2Builder layoutBuilder;
    double outboundAttCompensation = 1;
    private GraphModel graphModel;
    private Graph graph;
    private double edgeWeightInfluence;
    private double jitterTolerance;
    private double scalingRatio;
    private double gravity;
    private double speed;
    private double speedEfficiency;
    private boolean outboundAttractionDistribution;
    private boolean adjustSizes;
    private boolean barnesHutOptimize;
    private double barnesHutTheta;
    private boolean linLogMode;
    private boolean normalizeEdgeWeights;
    private boolean strongGravityMode;
    private boolean invertedEdgeWeightsMode;
    private int threadCount;
    private int currentThreadCount;
    private RegionSpeed rootRegion;
    private ExecutorService pool;
    private double[] nodesInfo;
    private int[] nodesIndicesToIndexInNodesInfoArray;
    private int[] nodesIndicesToNodeStoreId;
    private int[] nodesStoredIdsToNodesIndicesInNodesInfo;
    private Object[] nodesStoredIdsToNodesUserId;
    private boolean isDynamicWeight;
    private Interval interval;
    private Node[] nodes;
    private Edge[] edges;
    private static final int VALUES_PER_NODE = 10;
    private long durationRepulsion = 0;
    private long durationAttraction = 0;
    private boolean multiThreading = true;

    public ForceAtlas2Speed(ForceAtlas2Builder layoutBuilder, boolean multiThreading) {
        this.layoutBuilder = layoutBuilder;
//        this.threadCount = Math.min(4, Math.max(1, Runtime.getRuntime().availableProcessors() - 1));
        this.threadCount = (Runtime.getRuntime().availableProcessors() * 2) - 1;
        this.multiThreading = multiThreading;
    }

    public long getDurationRepulsion() {
        return durationRepulsion;
    }

    @Override
    public void initAlgo() {
        if (graphModel == null) {
            return;
        }
        AbstractLayout.ensureSafeLayoutNodePositions(graphModel);
        speed = 1.;
        speedEfficiency = 1.;
        pool = Executors.newFixedThreadPool(threadCount);
        currentThreadCount = threadCount;
        graph = graphModel.getGraphVisible();

        int nodeCount = graph.getNodeCount();

        // STARTING VALUE OF NODE INDICES IS ONE BECAUSE THE ZERO VALUE IS ALREADY USED AS A DEFAULT VALUE FOR AN ABSENT ELEMENT IN AN ARRAY
        int nodeIndex = 1;

        nodesInfo = new double[(nodeCount + 1) * VALUES_PER_NODE];
        nodesIndicesToIndexInNodesInfoArray = new int[nodeCount + 1];
        nodesIndicesToNodeStoreId = new int[nodeCount + 1];
        nodesStoredIdsToNodesUserId = new Object[graph.getModel().getMaxNodeStoreId()];
        nodesStoredIdsToNodesIndicesInNodesInfo = new int[graph.getModel().getMaxNodeStoreId()];
        nodes = graph.getNodes().toArray();
        edges = graph.getEdges().toArray();

        int arrayIndex = 0;
        for (Node n : nodes) {
            nodesInfo[arrayIndex++] = nodeIndex++; // incremental int as a node index in the nodesInfo workhorse
            nodesIndicesToIndexInNodesInfoArray[nodeIndex - 1] = arrayIndex - 1; // keeping a list of nodes indices in the nodesInfo array
            nodesIndicesToNodeStoreId[nodeIndex - 1] = n.getStoreId(); // keeping a map of nodes indices in the nodesInfo array to the storeId of the nodes
            nodesStoredIdsToNodesIndicesInNodesInfo[n.getStoreId()] = arrayIndex - 1; // keeping a map of storedIds of each nodes to their nodes indices in the nodesInfo array
            nodesStoredIdsToNodesUserId[n.getStoreId()] = n.getId(); // keeping a list of nodes indices in the nodesInfo array
            nodesInfo[arrayIndex++] = graph.getDegree(n) + 1;  // node mass
            nodesInfo[arrayIndex++] = n.x();  // node x
            nodesInfo[arrayIndex++] = n.y();  // node y
            nodesInfo[arrayIndex++] = 0d;  // node old dx
            nodesInfo[arrayIndex++] = 0d;  // node old dy
            nodesInfo[arrayIndex++] = 0d;  // node dx
            nodesInfo[arrayIndex++] = 0d;  // node dy
            nodesInfo[arrayIndex++] = n.size();  // node size
            nodesInfo[arrayIndex++] = n.isFixed() ? 1d : 0d; // 1 is node is fixed, zero otherwise
            // If outboundAttractionDistribution active, compensate.
        }
        if (isOutboundAttractionDistribution()) {
            outboundAttCompensation = 0;
            for (int i = 1; i < nodesInfo.length; i += VALUES_PER_NODE) {
                outboundAttCompensation += nodesInfo[i];
            }

        }
        isDynamicWeight = graphModel.getEdgeTable().getColumn("weight").isDynamic();
        interval = graph.getView().getTimeInterval();
    }

    private double getEdgeWeight(Edge edge, boolean isDynamicWeight, Interval interval) {
        double w = edge.getWeight();
        if (isDynamicWeight) {
            w = edge.getWeight(interval);
        }
        if (isInvertedEdgeWeightsMode()) {
            return w == 0 ? 0 : 1 / w;
        }
        return w;
    }

    @Override
    public void goAlgo() {

        for (int nodeIndex : nodesIndicesToIndexInNodesInfoArray) {
            nodesInfo[nodeIndex + 4] = nodesInfo[nodeIndex + 6];
            nodesInfo[nodeIndex + 5] = nodesInfo[nodeIndex + 7];
            nodesInfo[nodeIndex + 6] = 0;
            nodesInfo[nodeIndex + 7] = 0;
        }

        long startRepulsion = System.currentTimeMillis();

        // If Barnes Hut active, initialize root region
        if (isBarnesHutOptimize()) {
            rootRegion = new RegionSpeed(nodesInfo, nodesIndicesToIndexInNodesInfoArray, nodesIndicesToIndexInNodesInfoArray.length);
            rootRegion.buildSubRegions();
        }

//            // We make more tasks than threads because some tasks may need more time to compute.
        ArrayList<Future> threads = new ArrayList();
        RepulsionForce repulsionWithorWithoutSizeAdjustment = ForceFactorySpeed.builder.buildRepulsion(isAdjustSizes(), getScalingRatio());

        if (multiThreading) {

            // Repulsion (and gravity)
            // NB: Muti-threaded
            int taskCount = 8 * currentThreadCount;  // The threadPool Executor Service will manage the fetching of tasks and threads.

            for (int t = taskCount; t > 0; t--) {
                int from = (int) Math.floor(nodes.length * (t - 1) / taskCount);
                int to = (int) Math.floor(nodes.length * t / taskCount);
                RepulsionForce repulsionForceWithorWithoutGravity = isStrongGravityMode() ? (ForceFactorySpeed.builder.getStrongGravity(getScalingRatio())) : repulsionWithorWithoutSizeAdjustment;
                Future future = pool.submit(new NodesThreadSpeed(nodesInfo, nodesIndicesToIndexInNodesInfoArray, from, to, isBarnesHutOptimize(), getBarnesHutTheta(), getGravity(),
                        repulsionForceWithorWithoutGravity, getScalingRatio(), rootRegion, repulsionWithorWithoutSizeAdjustment, VALUES_PER_NODE));
                threads.add(future);
            }
        }
        else {
            RepulsionForce repulsionForceWithorWithoutGravity = isStrongGravityMode() ? (ForceFactorySpeed.builder.getStrongGravity(getScalingRatio())) : repulsionWithorWithoutSizeAdjustment;
            Future futureThread = pool.submit(new NodesThreadSpeed(nodesInfo, nodesIndicesToIndexInNodesInfoArray, 0, nodesIndicesToIndexInNodesInfoArray.length, isBarnesHutOptimize(), getBarnesHutTheta(), getGravity(),
                    repulsionForceWithorWithoutGravity, getScalingRatio(), rootRegion, repulsionWithorWithoutSizeAdjustment, VALUES_PER_NODE));
            threads.add(futureThread);
        }
        for (Future future : threads) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException("Unable to layout " + this.getClass().getSimpleName() + ".", e);
            }
        }

        long endOfRepulsion = System.currentTimeMillis();
        durationRepulsion = durationRepulsion + (endOfRepulsion - startRepulsion);

        long startAttraction = System.currentTimeMillis();

        // Attraction
        AttractionForce attraction = ForceFactorySpeed.builder
                .buildAttraction(isLinLogMode(), isOutboundAttractionDistribution(), isAdjustSizes(),
                        1 * ((isOutboundAttractionDistribution()) ? (outboundAttCompensation) : (1)));
        if (getEdgeWeightInfluence() == 0) {
            Arrays.stream(edges).parallel().forEach(e -> {
                int sourceNodeStoreId = e.getSource().getStoreId();
                int targetNodeStoreId = e.getTarget().getStoreId();
                int sourceNodeIndexInNodeInfo = nodesStoredIdsToNodesIndicesInNodesInfo[sourceNodeStoreId];
                int targetNodeIndexInNodeInfo = nodesStoredIdsToNodesIndicesInNodesInfo[targetNodeStoreId];
                attraction.applyAttraction(nodesInfo, sourceNodeIndexInNodeInfo, targetNodeIndexInNodeInfo, 1);
            });
        } else if (getEdgeWeightInfluence() == 1) {
            if (isNormalizeEdgeWeights()) {
                Double w;
                Double edgeWeightMin = Double.MAX_VALUE;
                Double edgeWeightMax = Double.MIN_VALUE;
                for (Edge e : edges) {
                    w = getEdgeWeight(e, isDynamicWeight, interval);
                    edgeWeightMin = Math.min(w, edgeWeightMin);
                    edgeWeightMax = Math.max(w, edgeWeightMax);
                }
                if (edgeWeightMin < edgeWeightMax) {
                    for (Edge e : edges) {
                        w = (getEdgeWeight(e, isDynamicWeight, interval) - edgeWeightMin) / (edgeWeightMax - edgeWeightMin);
                        int sourceNodeStoreId = e.getSource().getStoreId();
                        int targetNodeStoreId = e.getTarget().getStoreId();
                        int sourceNodeIndexInNodeInfo = nodesStoredIdsToNodesIndicesInNodesInfo[sourceNodeStoreId];
                        int targetNodeIndexInNodeInfo = nodesStoredIdsToNodesIndicesInNodesInfo[targetNodeStoreId];
                        attraction.applyAttraction(nodesInfo, sourceNodeIndexInNodeInfo, targetNodeIndexInNodeInfo, w);
                    }
                } else {
                    Arrays.stream(edges).parallel().forEach(e -> {
                        int sourceNodeStoreId = e.getSource().getStoreId();
                        int targetNodeStoreId = e.getTarget().getStoreId();
                        int sourceNodeIndexInNodeInfo = nodesStoredIdsToNodesIndicesInNodesInfo[sourceNodeStoreId];
                        int targetNodeIndexInNodeInfo = nodesStoredIdsToNodesIndicesInNodesInfo[targetNodeStoreId];
                        attraction.applyAttraction(nodesInfo, sourceNodeIndexInNodeInfo, targetNodeIndexInNodeInfo, 1);
                    });
                }
            } else {
                Arrays.stream(edges).parallel().forEach(e -> {
                    int sourceNodeStoreId = e.getSource().getStoreId();
                    int targetNodeStoreId = e.getTarget().getStoreId();
                    int sourceNodeIndexInNodeInfo = nodesStoredIdsToNodesIndicesInNodesInfo[sourceNodeStoreId];
                    int targetNodeIndexInNodeInfo = nodesStoredIdsToNodesIndicesInNodesInfo[targetNodeStoreId];
                    attraction.applyAttraction(nodesInfo, sourceNodeIndexInNodeInfo, targetNodeIndexInNodeInfo, getEdgeWeight(e, isDynamicWeight, interval));
                });
            }
        } else {
            if (isNormalizeEdgeWeights()) {
                Double w;
                Double edgeWeightMin = Double.MAX_VALUE;
                Double edgeWeightMax = Double.MIN_VALUE;
                for (Edge e : edges) {
                    w = getEdgeWeight(e, isDynamicWeight, interval);
                    edgeWeightMin = Math.min(w, edgeWeightMin);
                    edgeWeightMax = Math.max(w, edgeWeightMax);
                }
                if (edgeWeightMin < edgeWeightMax) {
                    for (Edge e : edges) {
                        w = (getEdgeWeight(e, isDynamicWeight, interval) - edgeWeightMin) / (edgeWeightMax - edgeWeightMin);
                        int sourceNodeStoreId = e.getSource().getStoreId();
                        int targetNodeStoreId = e.getTarget().getStoreId();
                        int sourceNodeIndexInNodeInfo = nodesStoredIdsToNodesIndicesInNodesInfo[sourceNodeStoreId];
                        int targetNodeIndexInNodeInfo = nodesStoredIdsToNodesIndicesInNodesInfo[targetNodeStoreId];
                        attraction.applyAttraction(nodesInfo, sourceNodeIndexInNodeInfo, targetNodeIndexInNodeInfo, Math.pow(w, edgeWeightInfluence));
                    }
                } else {
                    Arrays.stream(edges).parallel().forEach(e -> {
                        int sourceNodeStoreId = e.getSource().getStoreId();
                        int targetNodeStoreId = e.getTarget().getStoreId();
                        int sourceNodeIndexInNodeInfo = nodesStoredIdsToNodesIndicesInNodesInfo[sourceNodeStoreId];
                        int targetNodeIndexInNodeInfo = nodesStoredIdsToNodesIndicesInNodesInfo[targetNodeStoreId];
                        attraction.applyAttraction(nodesInfo, sourceNodeIndexInNodeInfo, targetNodeIndexInNodeInfo, 1);
                    });
                }
            } else {
                Arrays.stream(edges).parallel().forEach(e -> {
                    int sourceNodeStoreId = e.getSource().getStoreId();
                    int targetNodeStoreId = e.getTarget().getStoreId();
                    int sourceNodeIndexInNodeInfo = nodesStoredIdsToNodesIndicesInNodesInfo[sourceNodeStoreId];
                    int targetNodeIndexInNodeInfo = nodesStoredIdsToNodesIndicesInNodesInfo[targetNodeStoreId];
                    attraction.applyAttraction(nodesInfo, sourceNodeIndexInNodeInfo, targetNodeIndexInNodeInfo, Math.pow(getEdgeWeight(e, isDynamicWeight, interval), edgeWeightInfluence));
                });
            }
        }

        long endOfAttraction = System.currentTimeMillis();
        durationAttraction = durationAttraction + (endOfAttraction - startAttraction);

        // Auto adjust speed
        double totalSwinging = 0d;  // How much irregular movement
        double totalEffectiveTraction = 0d;  // Hom much useful movement
        for (int nodeIndex : nodesIndicesToIndexInNodesInfoArray) {
            if (nodesInfo[nodeIndex + 9] == 0) {
                double swinging
                        = Math.sqrt(Math.pow(nodesInfo[nodeIndex + 4] - nodesInfo[nodeIndex + 6], 2) + Math.pow(nodesInfo[nodeIndex + 5] - nodesInfo[nodeIndex + 7], 2));
                totalSwinging += nodesInfo[nodeIndex + 1]
                        * swinging;   // If the node has a burst change of direction, then it's not converging.
                totalEffectiveTraction += nodesInfo[nodeIndex + 1] * 0.5
                        * Math.sqrt(Math.pow(nodesInfo[nodeIndex + 4] + nodesInfo[nodeIndex + 6], 2) + Math.pow(nodesInfo[nodeIndex + 5] + nodesInfo[nodeIndex + 7], 2));
            }
        }
        // We want that swingingMovement < tolerance * convergenceMovement

        // Optimize jitter tolerance
        // The 'right' jitter tolerance for this network. Bigger networks need more tolerance. Denser networks need less tolerance. Totally empiric.
        double estimatedOptimalJitterTolerance = 0.05 * Math.sqrt(nodes.length);
        double minJT = Math.sqrt(estimatedOptimalJitterTolerance);
        double maxJT = 10;
        double jt = jitterTolerance * Math.max(minJT,
                Math.min(maxJT, estimatedOptimalJitterTolerance * totalEffectiveTraction / Math.pow(nodes.length, 2)));

        double minSpeedEfficiency = 0.05;

        // Protection against erratic behavior
        if (totalSwinging / totalEffectiveTraction > 2.0) {
            if (speedEfficiency > minSpeedEfficiency) {
                speedEfficiency *= 0.5;
            }
            jt = Math.max(jt, jitterTolerance);
        }

        double targetSpeed = jt * speedEfficiency * totalEffectiveTraction / totalSwinging;

        // Speed efficiency is how the speed really corresponds to the swinging vs. convergence tradeoff
        // We adjust it slowly and carefully
        if (totalSwinging > jt * totalEffectiveTraction) {
            if (speedEfficiency > minSpeedEfficiency) {
                speedEfficiency *= 0.7;
            }
        } else if (speed < 1000) {
            speedEfficiency *= 1.3;
        }

        // But the speed shoudn't rise too much too quickly, since it would make the convergence drop dramatically.
        double maxRise = 0.5;   // Max rise: 50%
        speed = speed + Math.min(targetSpeed - speed, maxRise * speed);

        // Apply forces
        if (isAdjustSizes()) {
            // If nodes overlap prevention is active, it's not possible to trust the swinging mesure.
            for (int nodeIncrementalIndex = 0; nodeIncrementalIndex < nodesIndicesToNodeStoreId.length; nodeIncrementalIndex++) {
                int nodeIndex = nodesIndicesToIndexInNodesInfoArray[nodeIncrementalIndex];
                if (nodesInfo[nodeIndex + 9] == 0) {

                    // Adaptive auto-speed: the speed of each node is lowered
                    // when the node swings.
// Compute differences only once
                    double dx = nodesInfo[nodeIndex + 4] - nodesInfo[nodeIndex + 6];
                    double dy = nodesInfo[nodeIndex + 5] - nodesInfo[nodeIndex + 7];

// Compute squared distances
                    double dxSquared = dx * dx;
                    double dySquared = dy * dy;

// Compute swinging term
                    double distance = Math.sqrt(dxSquared + dySquared);
                    double swinging = nodesInfo[nodeIndex + 1] * distance;

// Compute factor
                    double sqrtSpeedSwinging = Math.sqrt(speed * swinging);
                    double factor = 0.1 * speed / (1.0 + sqrtSpeedSwinging);

// Compute df
                    double deltaX = nodesInfo[nodeIndex + 6];
                    double deltaY = nodesInfo[nodeIndex + 7];
                    double deltaDistance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

// Adjust factor with minimum function
                    factor = Math.min(factor * deltaDistance, 10.0) / deltaDistance;

                    double x = nodesInfo[nodeIndex + 2] + nodesInfo[nodeIndex + 6] * factor;
                    double y = nodesInfo[nodeIndex + 3] + nodesInfo[nodeIndex + 7] * factor;

                    int nodStoredId = nodesIndicesToNodeStoreId[nodeIncrementalIndex];

                    Object nodeUserId = nodesStoredIdsToNodesUserId[nodStoredId];
                    Node node = graph.getNode(nodeUserId);

                    node.setX((float) x);
                    node.setY((float) y);
                    nodesInfo[nodeIndex + 2] = x;
                    nodesInfo[nodeIndex + 3] = y;
                }
            }
        } else {
            for (int nodeIncrementalIndex = 0; nodeIncrementalIndex < nodesIndicesToNodeStoreId.length; nodeIncrementalIndex++) {
                int nodeIndex = nodesIndicesToIndexInNodesInfoArray[nodeIncrementalIndex];
                if (nodesInfo[nodeIndex + 9] == 0) {

                    // Adaptive auto-speed: the speed of each node is lowered
                    // when the node swings.
// Compute differences only once
                    double dx = nodesInfo[nodeIndex + 4] - nodesInfo[nodeIndex + 6];
                    double dy = nodesInfo[nodeIndex + 5] - nodesInfo[nodeIndex + 7];

// Compute squared distances
                    double dxSquared = dx * dx;
                    double dySquared = dy * dy;

// Compute swinging term
                    double distance = Math.sqrt(dxSquared + dySquared);
                    double swinging = nodesInfo[nodeIndex + 1] * distance;

// Compute factor
                    double sqrtSpeedSwinging = Math.sqrt(speed * swinging);
                    double factor = speed / (1.0 + sqrtSpeedSwinging);

                    double x = nodesInfo[nodeIndex + 2] + nodesInfo[nodeIndex + 6] * factor;
                    double y = nodesInfo[nodeIndex + 3] + nodesInfo[nodeIndex + 7] * factor;

                    int nodStoredId = nodesIndicesToNodeStoreId[nodeIncrementalIndex];

                    Object nodeUserId = nodesStoredIdsToNodesUserId[nodStoredId];
                    Node node = graph.getNode(nodeUserId);

                    node.setX((float) x);
                    node.setY((float) y);
                    nodesInfo[nodeIndex + 2] = x;
                    nodesInfo[nodeIndex + 3] = y;

                }
            }
        }

    }

    @Override
    public boolean canAlgo() {
        return graphModel != null;
    }

    @Override
    public void endAlgo() {
        pool.shutdown();
    }

    @Override
    public LayoutProperty[] getProperties() {
        List<LayoutProperty> properties = new ArrayList<>();
        final String FORCEATLAS2_TUNING = NbBundle.getMessage(getClass(), "ForceAtlas2.tuning");
        final String FORCEATLAS2_BEHAVIOR = NbBundle.getMessage(getClass(), "ForceAtlas2.behavior");
        final String FORCEATLAS2_PERFORMANCE = NbBundle.getMessage(getClass(), "ForceAtlas2.performance");
        final String FORCEATLAS2_THREADS = NbBundle.getMessage(getClass(), "ForceAtlas2.threads");

        try {
            properties.add(LayoutProperty.createProperty(
                    this, Double.class,
                    NbBundle.getMessage(getClass(), "ForceAtlas2.scalingRatio.name"),
                    FORCEATLAS2_TUNING,
                    "ForceAtlas2.scalingRatio.name",
                    NbBundle.getMessage(getClass(), "ForceAtlas2.scalingRatio.desc"),
                    "getScalingRatio", "setScalingRatio"));

            properties.add(LayoutProperty.createProperty(
                    this, Boolean.class,
                    NbBundle.getMessage(getClass(), "ForceAtlas2.strongGravityMode.name"),
                    FORCEATLAS2_TUNING,
                    "ForceAtlas2.strongGravityMode.name",
                    NbBundle.getMessage(getClass(), "ForceAtlas2.strongGravityMode.desc"),
                    "isStrongGravityMode", "setStrongGravityMode"));

            properties.add(LayoutProperty.createProperty(
                    this, Double.class,
                    NbBundle.getMessage(getClass(), "ForceAtlas2.gravity.name"),
                    FORCEATLAS2_TUNING,
                    "ForceAtlas2.gravity.name",
                    NbBundle.getMessage(getClass(), "ForceAtlas2.gravity.desc"),
                    "getGravity", "setGravity"));

            properties.add(LayoutProperty.createProperty(
                    this, Boolean.class,
                    NbBundle.getMessage(getClass(), "ForceAtlas2.distributedAttraction.name"),
                    FORCEATLAS2_BEHAVIOR,
                    "ForceAtlas2.distributedAttraction.name",
                    NbBundle.getMessage(getClass(), "ForceAtlas2.distributedAttraction.desc"),
                    "isOutboundAttractionDistribution", "setOutboundAttractionDistribution"));

            properties.add(LayoutProperty.createProperty(
                    this, Boolean.class,
                    NbBundle.getMessage(getClass(), "ForceAtlas2.linLogMode.name"),
                    FORCEATLAS2_BEHAVIOR,
                    "ForceAtlas2.linLogMode.name",
                    NbBundle.getMessage(getClass(), "ForceAtlas2.linLogMode.desc"),
                    "isLinLogMode", "setLinLogMode"));

            properties.add(LayoutProperty.createProperty(
                    this, Boolean.class,
                    NbBundle.getMessage(getClass(), "ForceAtlas2.adjustSizes.name"),
                    FORCEATLAS2_BEHAVIOR,
                    "ForceAtlas2.adjustSizes.name",
                    NbBundle.getMessage(getClass(), "ForceAtlas2.adjustSizes.desc"),
                    "isAdjustSizes", "setAdjustSizes"));

            properties.add(LayoutProperty.createProperty(
                    this, Double.class,
                    NbBundle.getMessage(getClass(), "ForceAtlas2.edgeWeightInfluence.name"),
                    FORCEATLAS2_BEHAVIOR,
                    "ForceAtlas2.edgeWeightInfluence.name",
                    NbBundle.getMessage(getClass(), "ForceAtlas2.edgeWeightInfluence.desc"),
                    "getEdgeWeightInfluence", "setEdgeWeightInfluence"));

            properties.add(LayoutProperty.createProperty(
                    this, Boolean.class,
                    NbBundle.getMessage(getClass(), "ForceAtlas2.normalizeEdgeWeights.name"),
                    FORCEATLAS2_BEHAVIOR,
                    "ForceAtlas2.normalizeEdgeWeights.name",
                    NbBundle.getMessage(getClass(), "ForceAtlas2.normalizeEdgeWeights.desc"),
                    "isNormalizeEdgeWeights", "setNormalizeEdgeWeights"));

            properties.add(LayoutProperty.createProperty(
                    this, Boolean.class,
                    NbBundle.getMessage(getClass(), "ForceAtlas2.invertedEdgeWeightsMode.name"),
                    FORCEATLAS2_BEHAVIOR,
                    "ForceAtlas2.invertedEdgeWeightsMode.name",
                    NbBundle.getMessage(getClass(), "ForceAtlas2.invertedEdgeWeightsMode.desc"),
                    "isInvertedEdgeWeightsMode", "setInvertedEdgeWeightsMode"));

            properties.add(LayoutProperty.createProperty(
                    this, Double.class,
                    NbBundle.getMessage(getClass(), "ForceAtlas2.jitterTolerance.name"),
                    FORCEATLAS2_PERFORMANCE,
                    "ForceAtlas2.jitterTolerance.name",
                    NbBundle.getMessage(getClass(), "ForceAtlas2.jitterTolerance.desc"),
                    "getJitterTolerance", "setJitterTolerance"));

            properties.add(LayoutProperty.createProperty(
                    this, Boolean.class,
                    NbBundle.getMessage(getClass(), "ForceAtlas2.barnesHutOptimization.name"),
                    FORCEATLAS2_PERFORMANCE,
                    "ForceAtlas2.barnesHutOptimization.name",
                    NbBundle.getMessage(getClass(), "ForceAtlas2.barnesHutOptimization.desc"),
                    "isBarnesHutOptimize", "setBarnesHutOptimize"));

            properties.add(LayoutProperty.createProperty(
                    this, Double.class,
                    NbBundle.getMessage(getClass(), "ForceAtlas2.barnesHutTheta.name"),
                    FORCEATLAS2_PERFORMANCE,
                    "ForceAtlas2.barnesHutTheta.name",
                    NbBundle.getMessage(getClass(), "ForceAtlas2.barnesHutTheta.desc"),
                    "getBarnesHutTheta", "setBarnesHutTheta"));

            properties.add(LayoutProperty.createProperty(
                    this, Integer.class,
                    NbBundle.getMessage(getClass(), "ForceAtlas2.threads.name"),
                    FORCEATLAS2_THREADS,
                    "ForceAtlas2.threads.name",
                    NbBundle.getMessage(getClass(), "ForceAtlas2.threads.desc"),
                    "getThreadsCount", "setThreadsCount"));

        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }

        return properties.toArray(new LayoutProperty[0]);
    }

    @Override
    public void resetPropertiesValues() {
        int nodesCount = 0;

        if (graphModel != null) {
            nodesCount = graphModel.getGraphVisible().getNodeCount();
        }

        // Tuning
        if (nodesCount >= 100) {
            setScalingRatio(2.0);
        } else {
            setScalingRatio(10.0);
        }
        setStrongGravityMode(false);
        setInvertedEdgeWeightsMode(false);
        setGravity(1.);

        // Behavior
        setOutboundAttractionDistribution(false);
        setLinLogMode(false);
        setAdjustSizes(false);
        setEdgeWeightInfluence(1.);
        setNormalizeEdgeWeights(false);

        // Performance
        setJitterTolerance(1d);
        setBarnesHutOptimize(nodesCount >= 1000);
        setBarnesHutTheta(1.2);
        setThreadsCount(Math.max(1, Runtime.getRuntime().availableProcessors() - 1));
    }

    @Override
    public LayoutBuilder getBuilder() {
        return layoutBuilder;
    }

    @Override
    public void setGraphModel(GraphModel graphModel) {
        this.graphModel = graphModel;
        // Trick: reset here to take the profile of the graph in account for default values
        resetPropertiesValues();
    }

    public Double getBarnesHutTheta() {
        return barnesHutTheta;
    }

    public void setBarnesHutTheta(Double barnesHutTheta) {
        this.barnesHutTheta = barnesHutTheta;
    }

    public Double getEdgeWeightInfluence() {
        return edgeWeightInfluence;
    }

    public void setEdgeWeightInfluence(Double edgeWeightInfluence) {
        this.edgeWeightInfluence = edgeWeightInfluence;
    }

    public Double getJitterTolerance() {
        return jitterTolerance;
    }

    public void setJitterTolerance(Double jitterTolerance) {
        this.jitterTolerance = jitterTolerance;
    }

    public Boolean isLinLogMode() {
        return linLogMode;
    }

    public void setLinLogMode(Boolean linLogMode) {
        this.linLogMode = linLogMode;
    }

    public Boolean isNormalizeEdgeWeights() {
        return normalizeEdgeWeights;
    }

    public void setNormalizeEdgeWeights(Boolean normalizeEdgeWeights) {
        this.normalizeEdgeWeights = normalizeEdgeWeights;
    }

    public Double getScalingRatio() {
        return scalingRatio;
    }

    public void setScalingRatio(Double scalingRatio) {
        this.scalingRatio = scalingRatio;
    }

    public Boolean isStrongGravityMode() {
        return strongGravityMode;
    }

    public void setStrongGravityMode(Boolean strongGravityMode) {
        this.strongGravityMode = strongGravityMode;
    }

    public Boolean isInvertedEdgeWeightsMode() {
        return invertedEdgeWeightsMode;
    }

    public void setInvertedEdgeWeightsMode(Boolean invertedEdgeWeightsMode) {
        this.invertedEdgeWeightsMode = invertedEdgeWeightsMode;
    }

    public Double getGravity() {
        return gravity;
    }

    public void setGravity(Double gravity) {
        this.gravity = gravity;
    }

    public Integer getThreadsCount() {
        return threadCount;
    }

    public void setThreadsCount(Integer threadCount) {
        this.threadCount = Math.max(1, threadCount);
    }

    public Boolean isOutboundAttractionDistribution() {
        return outboundAttractionDistribution;
    }

    public void setOutboundAttractionDistribution(Boolean outboundAttractionDistribution) {
        this.outboundAttractionDistribution = outboundAttractionDistribution;
    }

    public Boolean isAdjustSizes() {
        return adjustSizes;
    }

    public void setAdjustSizes(Boolean adjustSizes) {
        this.adjustSizes = adjustSizes;
    }

    public Boolean isBarnesHutOptimize() {
        return barnesHutOptimize;
    }

    public void setBarnesHutOptimize(Boolean barnesHutOptimize) {
        this.barnesHutOptimize = barnesHutOptimize;
    }

    public long getDurationAttraction() {
        return durationAttraction;
    }

}
