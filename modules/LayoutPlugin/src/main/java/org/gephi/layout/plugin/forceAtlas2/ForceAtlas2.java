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
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Interval;
import org.gephi.graph.api.Node;
import org.gephi.layout.plugin.AbstractLayout;
import org.gephi.layout.plugin.forceAtlas2.force.IAttractionEdge;
import org.gephi.layout.plugin.forceAtlas2.force.IGravity;
import org.gephi.layout.plugin.forceAtlas2.force.IRepulsionNode;
import org.gephi.layout.plugin.forceAtlas2.force.IRepulsionRegion;
import org.gephi.layout.plugin.forceAtlas2.force.LinearAttractionAntiCollisionEdge;
import org.gephi.layout.plugin.forceAtlas2.force.LinearAttractionDegreeDistributedAntiCollisionEdge;
import org.gephi.layout.plugin.forceAtlas2.force.LinearAttractionEdge;
import org.gephi.layout.plugin.forceAtlas2.force.LinearAttractionMassDistributedEdge;
import org.gephi.layout.plugin.forceAtlas2.force.LinearRepulsionNode;
import org.gephi.layout.plugin.forceAtlas2.force.LinearRepulsionNodeAntiCollision;
import org.gephi.layout.plugin.forceAtlas2.force.LinearRepulsionRegion;
import org.gephi.layout.plugin.forceAtlas2.force.LinearRepulsionRegionAntiCollision;
import org.gephi.layout.plugin.forceAtlas2.force.LogAttractionAntiCollisionEdge;
import org.gephi.layout.plugin.forceAtlas2.force.LogAttractionDegreeDistributedAntiCollisionEdge;
import org.gephi.layout.plugin.forceAtlas2.force.LogAttractionDegreeDistributedEdge;
import org.gephi.layout.plugin.forceAtlas2.force.LogAttractionEdge;
import org.gephi.layout.plugin.forceAtlas2.force.NormalGravity;
import org.gephi.layout.plugin.forceAtlas2.force.StrongGravity;
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
public class ForceAtlas2 implements Layout {
    
    public class ForceAtlas2Params{
    // Instead of sending individual parameters that makes hard to generalize method, 
    // we group that as a struct that is shared on all forces
       
        
    public ForceAtlas2Params(double edgeWeightInfluence, double jitterTolerance, double scalingRatio, double gravity, double speed, double speedEfficiency, boolean outboundAttractionDistribution, boolean adjustSizes, boolean barnesHutOptimize, double barnesHutTheta, boolean linLogMode, boolean normalizeEdgeWeights, boolean strongGravityMode, boolean invertedEdgeWeightsMode,  double outboundAttCompensation) {
        this.edgeWeightInfluence = edgeWeightInfluence;
        this.jitterTolerance = jitterTolerance;
        this.scalingRatio = scalingRatio;
        this.gravity = gravity;
        this.speed = speed;
        this.speedEfficiency = speedEfficiency;
        this.outboundAttractionDistribution = outboundAttractionDistribution;
        this.adjustSizes = adjustSizes;
        this.barnesHutOptimize = barnesHutOptimize;
        this.barnesHutTheta = barnesHutTheta;
        this.linLogMode = linLogMode;
        this.normalizeEdgeWeights = normalizeEdgeWeights;
        this.strongGravityMode = strongGravityMode;
        this.invertedEdgeWeightsMode = invertedEdgeWeightsMode;
        this.outboundAttCompensation = outboundAttCompensation;
    }
    final public double edgeWeightInfluence;
    final public double jitterTolerance;
    final public double scalingRatio;
    final public double gravity;
    final public double speed;
    final public double speedEfficiency;
    final public boolean outboundAttractionDistribution;
    final public boolean adjustSizes;
    final public boolean barnesHutOptimize;
    final public double barnesHutTheta;
    final public boolean linLogMode;
    final public boolean normalizeEdgeWeights;
    final public boolean strongGravityMode;
    final public boolean invertedEdgeWeightsMode;
    final public double outboundAttCompensation;
    }
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
    private Region rootRegion;
    private ExecutorService pool;

    public ForceAtlas2(ForceAtlas2Builder layoutBuilder) {
        this.layoutBuilder = layoutBuilder;
        this.threadCount = Math.min(4, Math.max(1, Runtime.getRuntime().availableProcessors() - 1));
    }
    private IAttractionEdge getAttractionForce(ForceAtlas2.ForceAtlas2Params params){
        if (params.adjustSizes) {
            if (params.linLogMode) {
                if (params.outboundAttractionDistribution) {
                    return new LogAttractionDegreeDistributedAntiCollisionEdge(params);
                } else {
                    return new LogAttractionAntiCollisionEdge(params);
                }
            } else {
                if (params.outboundAttractionDistribution) {
                    return new LinearAttractionDegreeDistributedAntiCollisionEdge(params);
                } else {
                    return new LinearAttractionAntiCollisionEdge(params);
                }
            }
        } else {
            if (params.linLogMode) {
                if (params.outboundAttractionDistribution) {
                    return new LogAttractionDegreeDistributedEdge(params);
                } else {
                    return new LogAttractionEdge(params);
                }
            } else {
                if (params.outboundAttractionDistribution) {
                    return new LinearAttractionMassDistributedEdge(params);
                } else {
                    return new LinearAttractionEdge(params);
                }
            }
        }
       }
    @Override
    public void initAlgo() {
        AbstractLayout.ensureSafeLayoutNodePositions(graphModel);

        speed = 1.;
        speedEfficiency = 1.;

        graph = graphModel.getGraphVisible();

        graph.readLock();
        try {
            Node[] nodes = graph.getNodes().toArray();

            // Initialise layout data
            for (Node n : nodes) {
                if (n.getLayoutData() == null || !(n.getLayoutData() instanceof ForceAtlas2LayoutData)) {
                    ForceAtlas2LayoutData nLayout = new ForceAtlas2LayoutData();
                    n.setLayoutData(nLayout);
                }
                ForceAtlas2LayoutData nLayout = n.getLayoutData();
                nLayout.mass = 1 + graph.getDegree(n);
                nLayout.old_dx = 0;
                nLayout.old_dy = 0;
                nLayout.dx = 0;
                nLayout.dy = 0;
            }

            pool = Executors.newFixedThreadPool(threadCount);
            currentThreadCount = threadCount;
        } finally {
            graph.readUnlockAll();
        }
    }

    private double getEdgeWeight(Edge edge, boolean isDynamicWeight, Interval interval) {
        double w = edge.getWeight();
        if (isDynamicWeight)
            w = edge.getWeight(interval);
        if (isInvertedEdgeWeightsMode())
            return w == 0 ? 0 : 1/w;
        return w;
    }


    @Override
    public void goAlgo() {
        
        if (graphModel == null) {
            return;
        }
        graph = graphModel.getGraphVisible();
        graph.readLock();
        boolean isDynamicWeight = graphModel.getEdgeTable().getColumn("weight").isDynamic();
        Interval interval = graph.getView().getTimeInterval();

        try {
            Node[] nodes = graph.getNodes().toArray();
            Edge[] edges = graph.getEdges().toArray();

            // Initialise layout data
            for (Node n : nodes) {
                if (n.getLayoutData() == null || !(n.getLayoutData() instanceof ForceAtlas2LayoutData)) {
                    ForceAtlas2LayoutData nLayout = new ForceAtlas2LayoutData();
                    n.setLayoutData(nLayout);
                }
                ForceAtlas2LayoutData nLayout = n.getLayoutData();
                nLayout.mass = 1 + graph.getDegree(n);
                nLayout.old_dx = nLayout.dx;
                nLayout.old_dy = nLayout.dy;
                nLayout.dx = 0;
                nLayout.dy = 0;
            }

            // If Barnes Hut active, initialize root region
            if (isBarnesHutOptimize()) {
                rootRegion = new Region(nodes);
                rootRegion.buildSubRegions();
            }

            // If outboundAttractionDistribution active, compensate.
            if (isOutboundAttractionDistribution()) {
                outboundAttCompensation = 0;
                for (Node n : nodes) {
                    ForceAtlas2LayoutData nLayout = n.getLayoutData();
                    outboundAttCompensation += nLayout.mass;
                }
                outboundAttCompensation /= nodes.length;
            }
            ForceAtlas2Params params = new ForceAtlas2Params(
                       this.edgeWeightInfluence ,
                       this.jitterTolerance ,
                       this.scalingRatio,
                       this.gravity ,
                       this.speed,
                       this.speedEfficiency,
                       this.outboundAttractionDistribution,
                       this.adjustSizes ,
                       this.barnesHutOptimize ,
                       this.barnesHutTheta,
                       this.linLogMode ,
                       this.normalizeEdgeWeights ,
                       this.strongGravityMode ,
                       this.invertedEdgeWeightsMode,
                       (isOutboundAttractionDistribution()) ? (outboundAttCompensation) : (1)
                   );
            
            // Repulsion (and gravity)
            // NB: Muti-threaded
     
     
            IGravity gravityForce = params.strongGravityMode ?  new StrongGravity(params): new NormalGravity(params) ;
            IRepulsionNode repulsionNode = params.adjustSizes ? new LinearRepulsionNodeAntiCollision(params): new LinearRepulsionNode(params);
            IRepulsionRegion repulsionRegion = params.adjustSizes ?  new LinearRepulsionRegionAntiCollision(params):new LinearRepulsionRegion(params);
            
            
            
            int taskCount = 8 * currentThreadCount;  // The threadPool Executor Service will manage the fetching of tasks and threads.
            // We make more tasks than threads because some tasks may need more time to compute.
            try {
                pool.invokeAll(IntStream.rangeClosed(1, taskCount).parallel().mapToObj((t) ->{
                            int from = (int) Math.floor(nodes.length * (t - 1) / taskCount);
                            int to = (int) Math.floor(nodes.length * t / taskCount);
                            return Executors.callable(new NodesThread(nodes, from, to,  params, rootRegion, gravityForce, repulsionNode, repulsionRegion));
                        }).collect(Collectors.toList()));
                  
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
 
            // Attraction
          
            IAttractionEdge attractionForce = this.getAttractionForce(params);
            if (params.edgeWeightInfluence == 0) {
                for (Edge e : edges) {

                    attractionForce.accept(e, 1.0);
                }
            } else if (params.edgeWeightInfluence == 1) {
                if (params.normalizeEdgeWeights) {
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
                
                            attractionForce.accept(e, w);
                        }
                    } else {
                        for (Edge e : edges) {
         
                            attractionForce.accept(e,1.);
                        }
                    }
                } else {
                    for (Edge e : edges) {
       
                        attractionForce.accept(e,getEdgeWeight(e, isDynamicWeight, interval));
                    }
                }
            } else {
                if (params.normalizeEdgeWeights) {
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
                             attractionForce.accept(e,w);
                        }
                    } else {
                        for (Edge e : edges) {

                             attractionForce.accept(e,1.);
                        }
                    }
                } else {
                    for (Edge e : edges) {

                        attractionForce.accept(e, Math.pow(getEdgeWeight(e, isDynamicWeight, interval), params.edgeWeightInfluence));
                    }
                }
            }

            // Auto adjust speed
            double totalSwinging = 0d;  // How much irregular movement
            double totalEffectiveTraction = 0d;  // Hom much useful movement
            for (Node n : nodes) {
                ForceAtlas2LayoutData nLayout = n.getLayoutData();
                if (!n.isFixed()) {
                    double swinging =
                        Math.sqrt(Math.pow(nLayout.old_dx - nLayout.dx, 2) + Math.pow(nLayout.old_dy - nLayout.dy, 2));
                    totalSwinging += nLayout.mass *
                        swinging;   // If the node has a burst change of direction, then it's not converging.
                    totalEffectiveTraction += nLayout.mass * 0.5 *
                        Math.sqrt(Math.pow(nLayout.old_dx + nLayout.dx, 2) + Math.pow(nLayout.old_dy + nLayout.dy, 2));
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
            if (params.adjustSizes) {
                // If nodes overlap prevention is active, it's not possible to trust the swinging mesure.
                for (Node n : nodes) {
                    ForceAtlas2LayoutData nLayout = n.getLayoutData();
                    if (!n.isFixed()) {

                        // Adaptive auto-speed: the speed of each node is lowered
                        // when the node swings.
                        double swinging = nLayout.mass * Math.sqrt(
                            (nLayout.old_dx - nLayout.dx) * (nLayout.old_dx - nLayout.dx) +
                                (nLayout.old_dy - nLayout.dy) * (nLayout.old_dy - nLayout.dy));
                        double factor = 0.1 * speed / (1f + Math.sqrt(speed * swinging));

                        double df = Math.sqrt(Math.pow(nLayout.dx, 2) + Math.pow(nLayout.dy, 2));
                        factor = Math.min(factor * df, 10.) / df;

                        double x = n.x() + nLayout.dx * factor;
                        double y = n.y() + nLayout.dy * factor;

                        n.setX((float) x);
                        n.setY((float) y);
                    }
                }
            } else {
                for (Node n : nodes) {
                    ForceAtlas2LayoutData nLayout = n.getLayoutData();
                    if (!n.isFixed()) {

                        // Adaptive auto-speed: the speed of each node is lowered
                        // when the node swings.
                        double swinging = nLayout.mass * Math.sqrt(
                            (nLayout.old_dx - nLayout.dx) * (nLayout.old_dx - nLayout.dx) +
                                (nLayout.old_dy - nLayout.dy) * (nLayout.old_dy - nLayout.dy));
                        //double factor = speed / (1f + Math.sqrt(speed * swinging));
                        double factor = speed / (1f + Math.sqrt(speed * swinging));

                        double x = n.x() + nLayout.dx * factor;
                        double y = n.y() + nLayout.dy * factor;

                        n.setX((float) x);
                        n.setY((float) y);
                    }
                }
            }
        } finally {
            graph.readUnlockAll();
        }
    }

    @Override
    public boolean canAlgo() {
        return graphModel != null;
    }

    @Override
    public void endAlgo() {
        graph.readLock();
        try {
            for (Node n : graph.getNodes()) {
                n.setLayoutData(null);
            }
            pool.shutdown();
        } finally {
            graph.readUnlockAll();
        }
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
}
