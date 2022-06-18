/*
 Copyright 2008-2011 Gephi
 Authors : Mathieu Jacomy, Tiago Peixoto
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

package org.gephi.statistics.plugin;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.apache.commons.math3.special.Gamma;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.graph.api.Table;
import org.gephi.statistics.spi.Statistics;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * @author Mathieu Jacomy & Tiago Peixoto
 */

public class StatisticalInferenceClustering implements Statistics, LongTask {

    public static final String STAT_INF_CLASS = "stat_inf_class";
    private final boolean useWeight = false;
    private boolean isCanceled;
    private StatisticalInferenceClustering.CommunityStructure structure;
    private ProgressTicket progress;
    private double descriptionLength;

    private static double lBinom(double n, double m) {
        return Gamma.logGamma(n + 1) - Gamma.logGamma(n - m + 1) - Gamma.logGamma(m + 1);
    }

    @Override
    public boolean cancel() {
        this.isCanceled = true;
        return true;
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progress = progressTicket;
    }

    @Override
    public void execute(GraphModel graphModel) {
        Graph graph = graphModel.getUndirectedGraphVisible();
        execute(graph);
    }

    public void execute(Graph graph) {
        isCanceled = false;

        Table nodeTable = graph.getModel().getNodeTable();
        ColumnUtils.cleanUpColumns(nodeTable, new String[] {STAT_INF_CLASS}, Integer.class);

        Column modCol = nodeTable.getColumn(STAT_INF_CLASS);
        if (modCol == null) {
            nodeTable.addColumn(STAT_INF_CLASS, "Inferred Class", Integer.class, 0);
        }

        graph.readLock();
        try {
            structure = new StatisticalInferenceClustering.CommunityStructure(graph);
            int[] comStructure = new int[graph.getNodeCount()];

            if (graph.getNodeCount() > 0) {//Fixes issue #713 Modularity Calculation Throws Exception On Empty Graph
                HashMap<String, Double> computedStatInfMetrics =
                    computePartition(graph, structure, comStructure, useWeight);
                descriptionLength = computedStatInfMetrics.getOrDefault("descriptionLength", 0.0);
            } else {
                descriptionLength = 0;
            }

            saveValues(comStructure, graph, structure);
        } finally {
            graph.readUnlock();
        }
    }

    protected HashMap<String, Double> computePartition(Graph graph,
                                                       StatisticalInferenceClustering.CommunityStructure theStructure,
                                                       int[] comStructure,
                                                       boolean weighted) {
        isCanceled = false;
        Progress.start(progress);
        Random rand = new Random();

        HashMap<String, Double> results = new HashMap<>();

        if (isCanceled) {
            return results;
        }
        boolean someChange = true;
        boolean initRound = true;
        while (someChange) {
            //System.out.println("Number of partitions: "+theStructure.communities.size());
            someChange = false;
            boolean localChange = true;
            while (localChange) {
                localChange = false;
                int start = 0;
                // Randomize
                start = Math.abs(rand.nextInt()) % theStructure.N;

                int step = 0;
                for (int i = start; step < theStructure.N; i = (i + 1) % theStructure.N) {
                    step++;
                    StatisticalInferenceClustering.Community bestCommunity =
                        updateBestCommunity(theStructure, i, initRound);
                    if ((theStructure.nodeCommunities[i] != bestCommunity) && (bestCommunity != null)) {
                        //double S_before = computeDescriptionLength(graph, theStructure);
                        //System.out.println("Move node "+i+" to com "+bestCommunity.id+" : S_before="+S_before);
                        theStructure.moveNodeTo(i, bestCommunity);
                        //double S_after = computeDescriptionLength(graph, theStructure);
                        //System.out.println("Move node "+i+" to com "+bestCommunity.id+" : S_after="+S_after+ " (Diff = "+(S_after - S_before)+")");
                        localChange = true;
                    }
                    if (isCanceled) {
                        return results;
                    }
                }
                someChange = localChange || someChange;
                initRound = false;
                if (isCanceled) {
                    return results;
                }
            }

            if (someChange) {
                theStructure.zoomOut();
            }
        }

        fillComStructure(graph, theStructure, comStructure);
        double computedDescriptionLength = computeDescriptionLength(graph, theStructure);

        results.put("descriptionLength", computedDescriptionLength);

        return results;
    }

    public double delta(int node,
                        StatisticalInferenceClustering.Community community,
                        StatisticalInferenceClustering.CommunityStructure theStructure,
                        Double e_in,
                        Double e_out,
                        Double E,
                        Double B,
                        Double N
    ) {
        //System.out.println("*** Compute delta for node "+node+" with respect to community "+community.id+" ***");

        // Node degree
        double k = theStructure.weights[node];
        // Node weight: how many real nodes (not meta-nodes) the group represents
        double nodeWeight = theStructure.graphNodeCount[node];

        // Number of edges of target community (with itself or another one)
        double e_r_target = community.weightSum;
        // Number of edges within target community
        Double e_rr_target = community.internalWeightSum;
        // Number of real graph nodes of target community
        int n_r_target = community.graphNodeCount;

        // Number of edges of current (where the node belongs) community (with itself or another one)
        double e_r_current = theStructure.nodeCommunities[node].weightSum;
        // Number of edges within current community (where the node belongs)
        Double e_rr_current = theStructure.nodeCommunities[node].internalWeightSum;
        // Number of real graph nodes of current community
        int n_r_current = theStructure.nodeCommunities[node].graphNodeCount;

        // Description length: before
        double S_b = 0.;
        S_b -= Gamma.logGamma(e_out + 1);
        if (e_out > 0) {
            S_b += e_out * lBinom(B, 2);
        }
        S_b += Gamma.logGamma(e_r_current + 1);
        S_b += Gamma.logGamma(e_r_target + 1);
        S_b -= (e_rr_current) * Math.log(2) + Gamma.logGamma(e_rr_current + 1);
        S_b -= (e_rr_target) * Math.log(2) + Gamma.logGamma(e_rr_target + 1);
        S_b -= Gamma.logGamma(n_r_current + 1);
        S_b -= Gamma.logGamma(n_r_target + 1);
        S_b += lBinom(n_r_current + e_r_current - 1, e_r_current);
        S_b += lBinom(n_r_target + e_r_target - 1, e_r_target);
        S_b += lBinom(B + e_in - 1, e_in);
        if (B > 1) {
            S_b += Math.log(E + 1);
        }
        S_b += lBinom(N - 1, B - 1);

        // Count the gains and losses
        // -> loop over the neighbors
        double delta_e_out = 0.;
        double delta_e_in = 0.;
        double delta_e_r_current = -k;
        double delta_e_r_target = +k;
        double delta_e_rr_current = 0.;
        double delta_e_rr_target = 0.;
        for (ComputationEdge e : theStructure.topology[node]) {
            int nei = e.target;
            Float w = e.weight;
            if (nei == node) {
                // Node self-loops
                delta_e_rr_current -= w;
                delta_e_rr_target += w;
            } else {
                // Losses (as if the node disappeared)
                if (theStructure.nodeCommunities[node] == theStructure.nodeCommunities[nei]) {
                    // The neighbor is in current community, so
                    // the node will leave the neighbor's community
                    delta_e_rr_current -= w;
                    delta_e_in -= w;
                } else {
                    // The neighbor is not in current community, so
                    // the node will not leave the neighbor's community
                    delta_e_out -= w;
                }
                // Gains (as if the node reappeared)
                if (community == theStructure.nodeCommunities[nei]) {
                    // The neighbor is in target community, so
                    // the node will arrive in the neighbor's community
                    delta_e_rr_target += w; // add weight between node and community -> OK
                    delta_e_in += w;
                } else {
                    // The neighbor is not in target community, so
                    // the node will not arrive in the neighbor's community
                    delta_e_out += w;
                }
            }
        }
        Double delta_B = 0.;
        if (theStructure.nodeCommunities[node].weightSum == theStructure.weights[node]) {
            // The node is the only one in the community
            delta_B = -1.;
        }
        // Note: if it were possible to add the node to an empty group, we would have to check that
        // the target group is empty or not, and if so, add one to delta_B.

        // Description length: after
        double S_a = 0.;
        S_a -= Gamma.logGamma(e_out + delta_e_out + 1);
        if (e_out + delta_e_out > 0) {
            S_a += (e_out + delta_e_out) * lBinom(B + delta_B, 2);
        }
        S_a += Gamma.logGamma(e_r_target + delta_e_r_target + 1);
        S_a -= (e_rr_target + delta_e_rr_target) * Math.log(2) + Gamma.logGamma(e_rr_target + delta_e_rr_target + 1);
        S_a -= Gamma.logGamma(n_r_target + nodeWeight + 1);
        S_a += lBinom(n_r_target + nodeWeight + e_r_target + delta_e_r_target - 1, e_r_target + delta_e_r_target);
        if (delta_B == 0) {
            // These calculations only apply if current category
            // would still exist after moving the node
            // (i.e. if it was not the last one)
            S_a += Gamma.logGamma(e_r_current + delta_e_r_current + 1);
            S_a -= (e_rr_current + delta_e_rr_current) * Math.log(2) +
                Gamma.logGamma(e_rr_current + delta_e_rr_current + 1);
            S_a -= Gamma.logGamma(n_r_current - nodeWeight + 1);
            S_a +=
                lBinom(n_r_current - nodeWeight + e_r_current + delta_e_r_current - 1, e_r_current + delta_e_r_current);
        }

        S_a += lBinom(B + delta_B + e_in + delta_e_in - 1, e_in + delta_e_in);
        if (B + delta_B > 1) {
            S_a += Math.log(E + 1);
        }
        S_a += lBinom(N - 1, B + delta_B - 1);

        return S_a - S_b;
    }

    private StatisticalInferenceClustering.Community updateBestCommunity(
        StatisticalInferenceClustering.CommunityStructure theStructure, int node_id, boolean initialization) {
        // Total number of edges (graph size)
        Double E = theStructure.graphWeightSum;
        // Total number of edges from one community to the same one
        Double e_in = theStructure.communities.stream().mapToDouble(c -> c.internalWeightSum).sum();
        // Total number of edges from one community to another
        Double e_out = E - e_in;
        // Total number of communities
        Double B = (double) theStructure.communities.size();
        // Total number of nodes (not metanodes!!!)
        Double N = (double) theStructure.graph.getNodeCount();

        //System.out.println("Test best community for node "+node_id+" (currently com "+theStructure.nodeCommunities[node_id].id+") Initialization: "+initialization);

        double best = Double.MAX_VALUE;
        StatisticalInferenceClustering.Community bestCommunity = null;
        Set<StatisticalInferenceClustering.Community> iter = theStructure.nodeConnectionsWeight[node_id].keySet();
        for (StatisticalInferenceClustering.Community com : iter) {
            if (com != theStructure.nodeCommunities[node_id]) {
                double deltaValue = delta(node_id, com, theStructure, e_in, e_out, E, B, N);
                if (Double.isNaN(deltaValue)) {
                    // TODO: change this to an exception
                    System.out.println(
                        "WARNING - ALGO ERROR - Statistical inference - DELTA is NaN (this is not supposed to happen)");
                }
                //System.out.println("Node "+node_id+" => com "+com.id+" DELTA="+deltaValue);
                if ((deltaValue < 0 || (initialization && Math.exp(-deltaValue) < Math.random())) &&
                    deltaValue < best) {
                    best = deltaValue;
                    bestCommunity = com;
                }
            }
        }

        if (bestCommunity == null) {
            //System.out.println("(NO CHANGE) com "+theStructure.nodeCommunities[node_id].id);
            bestCommunity = theStructure.nodeCommunities[node_id];
        } else {
            //System.out.println("Best community is "+bestCommunity.id);
        }
        return bestCommunity;
    }

    double computeDescriptionLength(Graph graph, StatisticalInferenceClustering.CommunityStructure theStructure) {
        // Total number of edges (graph size)
        double E = theStructure.graphWeightSum;
        // Total number of edges from one community to the same one
        double e_in = theStructure.communities.stream().mapToDouble(c -> c.internalWeightSum).sum();
        // Total number of edges from one community to another
        double e_out = E - e_in;
        // Total number of communities
        Double B = (double) theStructure.communities.size();
        // Total number of nodes (not metanodes!!!)
        Double N = (double) theStructure.graph.getNodeCount();

        // Description length
        double S = 0.;

        S -= Gamma.logGamma(e_out + 1);
        if (e_out > 0) {
            S += e_out * lBinom(B, 2);
        }
        for (Community community : theStructure.communities) {
            // Number of edges of community (with itself or another one)
            double e_r = community.weightSum;
            // Number of edges within community
            double e_rr = community.internalWeightSum;
            // Number of nodes in the  community
            int n_r = community.graphNodeCount;

            S += Gamma.logGamma(e_r + 1);
            S -= (e_rr) * Math.log(2) + Gamma.logGamma(e_rr + 1);
            S -= Gamma.logGamma(n_r + 1);
            S += lBinom(n_r + e_r - 1, e_r);
        }

        S += lBinom(B + e_in - 1, e_in);

        if (B > 1) {
            S += Math.log(E + 1);
        }

        S += lBinom(N - 1, B - 1);
        S += Gamma.logGamma(N + 1);
        S += Math.log(N);

        for (Node n : graph.getNodes()) {
            // degree
            double k = graph.getDegree(n);
            S -= Gamma.logGamma(k + 1);
        }

        return S;
    }

    private int[] fillComStructure(Graph graph, StatisticalInferenceClustering.CommunityStructure theStructure,
                                   int[] comStructure) {
        int count = 0;

        for (StatisticalInferenceClustering.Community com : theStructure.communities) {
            for (Integer node : com.nodes) {
                StatisticalInferenceClustering.Community hidden = theStructure.invMap.get(node);
                for (Integer nodeInt : hidden.nodes) {
                    comStructure[nodeInt] = count;
                }
            }
            count++;
        }
        return comStructure;
    }

    private void saveValues(int[] struct, Graph graph, StatisticalInferenceClustering.CommunityStructure theStructure) {
        Table nodeTable = graph.getModel().getNodeTable();

        Column modCol = nodeTable.getColumn(STAT_INF_CLASS);
        for (Node n : graph.getNodes()) {
            int n_index = theStructure.map.get(n);
            n.setAttribute(modCol, struct[n_index]);
        }
    }

    @Override
    public String getReport() {
        //Distribution series
        Map<Integer, Integer> sizeDist = new HashMap<>();
        for (Node n : structure.graph.getNodes()) {
            Integer v = (Integer) n.getAttribute(STAT_INF_CLASS);
            if (!sizeDist.containsKey(v)) {
                sizeDist.put(v, 0);
            }
            sizeDist.put(v, sizeDist.get(v) + 1);
        }

        XYSeries dSeries = ChartUtils.createXYSeries(sizeDist, "Size Distribution");

        XYSeriesCollection dataset1 = new XYSeriesCollection();
        dataset1.addSeries(dSeries);

        JFreeChart chart = ChartFactory.createXYLineChart(
            "Size Distribution",
            "Stat Inf Class",
            "Size (number of nodes)",
            dataset1,
            PlotOrientation.VERTICAL,
            true,
            false,
            false);
        chart.removeLegend();
        ChartUtils.decorateChart(chart);
        ChartUtils.scaleChart(chart, dSeries, false);
        String imageFile = ChartUtils.renderChart(chart, "communities-size-distribution.png");

        NumberFormat f = new DecimalFormat("#0.000");

        String report = "<HTML> <BODY> <h1>Statistical Inference Report </h1> "
            + "<hr>"
            + "<br> <h2> Results: </h2>"
            + "Description Length: " + f.format(descriptionLength) + "<br>"
            + "Number of Communities: " + structure.communities.size()
            + "<br /><br />" + imageFile
            + "<br /><br />" + "<h2> Algorithm: </h2>"
            + "Statistical inference of assortative community structures<br />"
            + "Lizhi Zhang, Tiago P. Peixoto<br />"
            + "Phys. Rev. Research 2 043271 (2020)<br />"
            + "https://dx.doi.org/10.1103/PhysRevResearch.2.043271<br /><br />"
            + "<br /><br />"
            + "Bayesian stochastic blockmodeling<br />"
            + "Tiago P. Peixoto<br />"
            + "Chapter in “Advances in Network Clustering and Blockmodeling,” edited by<br />"
            + "P. Doreian, V. Batagelj, A. Ferligoj (Wiley, 2019)<br />"
            + "https://dx.doi.org/10.1002/9781119483298.ch11<br />"
            + "</BODY> </HTML>";

        return report;
    }

    public double getDescriptionLength() {
        return descriptionLength;
    }

    static class Community {
        static int count = 0;
        protected int id;
        double weightSum; // e_r, i.e. sum of edge weights for the community, inside and outside altogether
        // Note: here we count the internal edges twice
        double internalWeightSum; // e_rr, i.e. sum of internal edge weights
        int graphNodeCount; // How many real nodes (useful after zoomOut)
        StatisticalInferenceClustering.CommunityStructure structure;
        List<Integer> nodes;
        HashMap<StatisticalInferenceClustering.Community, Float> connectionsWeight;
        HashMap<StatisticalInferenceClustering.Community, Integer> connectionsCount;

        public Community(StatisticalInferenceClustering.Community com) {
            this.id = count++;
            this.weightSum = 0;
            structure = com.structure;
            connectionsWeight = new HashMap<>();
            connectionsCount = new HashMap<>();
            nodes = new ArrayList<>();
        }

        public Community(StatisticalInferenceClustering.CommunityStructure structure) {
            this.id = count++;
            this.weightSum = 0;
            this.structure = structure;
            connectionsWeight = new HashMap<>();
            connectionsCount = new HashMap<>();
            nodes = new ArrayList<>();
        }

        public int size() {
            return nodes.size();
        }

        public void seed(int node) {
            nodes.add(node);
            weightSum += structure.weights[node];
            internalWeightSum += structure.internalWeights[node];
            graphNodeCount += structure.graphNodeCount[node];
        }

        public boolean add(int node) {
            nodes.add(node);
            weightSum += structure.weights[node];
            graphNodeCount += structure.graphNodeCount[node];
            return true;
        }

        public boolean remove(int node) {
            boolean result = nodes.remove((Integer) node);
            weightSum -= structure.weights[node];
            graphNodeCount -= structure.graphNodeCount[node];
            if (nodes.isEmpty()) {
                structure.communities.remove(this);
            }
            return result;
        }

        public String getMonitoring() {
            String monitoring = "";
            int count = 0;
            for (int nodeIndex : nodes) {
                if (count++ > 0) {
                    monitoring += " ";
                }
                monitoring += nodeIndex;
            }
            return monitoring;
        }
    }

    class CommunityStructure {

        HashMap<StatisticalInferenceClustering.Community, Float>[] nodeConnectionsWeight;
        HashMap<StatisticalInferenceClustering.Community, Integer>[] nodeConnectionsCount;
        HashMap<Node, Integer> map;
        StatisticalInferenceClustering.Community[] nodeCommunities;
        Graph graph;
        double[] graphNodeCount; // number of graph nodes represented by that node
        double[] weights; // The weighted degree of the nodes (in short)
        double[] internalWeights; // The sum of internal edges weights
        double graphWeightSum; // The weighted sum of degrees
        List<ComputationEdge>[] topology;
        List<StatisticalInferenceClustering.Community> communities;
        int N;
        HashMap<Integer, StatisticalInferenceClustering.Community> invMap;


        CommunityStructure(Graph graph) {
            //System.out.println("### INIT COMMUNITY STRUCTURE");
            this.graph = graph;
            N = graph.getNodeCount();
            invMap = new HashMap<>();
            // nodeConnectionsWeight is basically a table of, for each node, then for each community,
            // how many connections they have.
            nodeConnectionsWeight = new HashMap[N];
            // nodeConnectionsCount is basically the same thing but unweighted. Remarkably, in case of parallel edges,
            // but not taking weights into account, nodeConnectionsWeight will still count 1 for each parallel edges,
            // while nodeConnectionsCount will count just 1.
            nodeConnectionsCount = new HashMap[N];
            // graphNodeCount is the number of real nodes (graph nodes) in each nodes. This is necessary because
            // each node might in fact be a community of nodes (see zoomOut method)
            graphNodeCount = new double[N];
            // nodeCommunities is an index of which community each node belongs to
            nodeCommunities = new StatisticalInferenceClustering.Community[N];
            map = new HashMap<>(); // keeps track of the integer ids of the nodes
            // The topology is basically an index of the outbound computation edges for each node
            topology = new ArrayList[N];
            communities = new ArrayList<>();
            int index = 0;
            weights = new double[N]; // The weight is basically the weighted degree of a node
            internalWeights = new double[N];

            NodeIterable nodesIterable = graph.getNodes();
            for (Node node : nodesIterable) {
                map.put(node, index);
                nodeCommunities[index] = new StatisticalInferenceClustering.Community(this);

                nodeConnectionsWeight[index] = new HashMap<>();
                nodeConnectionsCount[index] = new HashMap<>();
                weights[index] = 0; // Note: weight is degree, but we add that later on
                graphNodeCount[index] = 1;
                internalWeights[index] = 0;
                nodeCommunities[index].seed(index);
                StatisticalInferenceClustering.Community hidden =
                    new StatisticalInferenceClustering.Community(structure);
                hidden.nodes.add(index);
                invMap.put(index, hidden);
                communities.add(nodeCommunities[index]);
                index++;
                if (isCanceled) {
                    nodesIterable.doBreak();
                    return;
                }
            }

            int[] edgeTypes = graph.getModel().getEdgeTypes();

            nodesIterable = graph.getNodes();
            for (Node node : nodesIterable) {
                int node_index = map.get(node);
                StatisticalInferenceClustering.Community com = nodeCommunities[node_index];
                topology[node_index] = new ArrayList<>();

                Set<Node> uniqueNeighbors = new HashSet<>(graph.getNeighbors(node).toCollection());
                for (Node neighbor : uniqueNeighbors) {
                    if (node == neighbor) {
                        continue;
                    }
                    int neighbor_index = map.get(neighbor);
                    float weight = 0;

                    //Sum all parallel edges weight:
                    for (int edgeType : edgeTypes) {
                        for (Edge edge : graph.getEdges(node, neighbor, edgeType)) {
                            if (useWeight) {
                                // TODO: the algorithm only works with integer weights
                                //weight += edge.getWeight(graph.getView());
                            } else {
                                weight += 1;
                            }
                        }
                    }

                    //Finally add a single edge with the summed weight of all parallel edges:
                    //Fixes issue #1419 Getting null pointer error when trying to calculate modularity
                    weights[node_index] += weight;
                    com.weightSum += weight;
                    if (node_index == neighbor_index) {
                        internalWeights[node_index] += weight;
                    }
                    ComputationEdge ce = new ComputationEdge(node_index, neighbor_index, weight);
                    topology[node_index].add(ce);
                    StatisticalInferenceClustering.Community adjCom = nodeCommunities[neighbor_index];

                    //System.out.println("Add links from node "+node_index+" to community "+adjCom.id);
                    nodeConnectionsWeight[node_index].put(adjCom, weight);
                    nodeConnectionsCount[node_index].put(adjCom, 1);

                    StatisticalInferenceClustering.Community nodeCom = nodeCommunities[node_index];
                    //System.out.println("Add links from community "+nodeCom.id+" to community "+adjCom.id);
                    nodeCom.connectionsWeight.put(adjCom, weight);
                    nodeCom.connectionsCount.put(adjCom, 1);

                    //System.out.println("Add links from node "+neighbor_index+" to community "+nodeCom.id);
                    nodeConnectionsWeight[neighbor_index].put(nodeCom, weight);
                    nodeConnectionsCount[neighbor_index].put(nodeCom, 1);

                    //System.out.println("Add links from community "+adjCom.id+" to community "+nodeCom.id);
                    adjCom.connectionsWeight.put(nodeCom, weight);
                    adjCom.connectionsCount.put(nodeCom, 1);

                    graphWeightSum += weight;
                }


                if (isCanceled) {
                    nodesIterable.doBreak();
                    return;
                }
            }
            graphWeightSum /= 2.0;
        }

        private void addNodeTo(int node, StatisticalInferenceClustering.Community to) {
            //System.out.println("### ADD NODE "+node+" TO COMMUNITY "+to.id);
            to.add(node);
            nodeCommunities[node] = to;

            for (ComputationEdge e : topology[node]) {
                int neighbor = e.target;

                ////////
                //Add Node Connection to this community
                //System.out.println("Add links from node "+neighbor+" to community "+to.id);
                //System.out.println("Add links from node "+neighbor+" to community "+to.id);
                nodeConnectionsWeight[neighbor].merge(to, e.weight, Float::sum);
                nodeConnectionsCount[neighbor].merge(to, 1, Integer::sum);

                ///////////////////
                StatisticalInferenceClustering.Community adjCom = nodeCommunities[neighbor];
                //System.out.println("Add links from community "+adjCom.id+" to community "+to.id);
                //System.out.println("Add links from community "+adjCom.id+" to community "+to.id);
                adjCom.connectionsWeight.merge(to, e.weight, Float::sum);
                adjCom.connectionsCount.merge(to, 1, Integer::sum);

                if (node == neighbor) {
                    continue;
                }

                //System.out.println("Add links from node "+node+" to community "+adjCom.id);
                //System.out.println("Add links from node "+node+" to community "+adjCom.id);
                nodeConnectionsWeight[node].merge(adjCom, e.weight, Float::sum);
                nodeConnectionsCount[node].merge(adjCom, 1, Integer::sum);

                if (to != adjCom) {
                    //System.out.println("Add links from community "+to.id+" to community "+adjCom.id);
                    //System.out.println("Add links from community "+to.id+" to community "+adjCom.id);
                    to.connectionsWeight.merge(adjCom, e.weight, Float::sum);

                    to.connectionsCount.merge(adjCom, 1, Integer::sum);

                }
            }
            to.internalWeightSum += nodeConnectionsWeight[node].getOrDefault(to, 0.f);
        }

        private void removeNodeFromItsCommunity(int node) {
            //System.out.println("### REMOVE NODE FROM ITS COMMUNITY "+node);
            StatisticalInferenceClustering.Community community = nodeCommunities[node];
            community.internalWeightSum -= nodeConnectionsWeight[node].getOrDefault(community, 0.f);
            for (ComputationEdge e : topology[node]) {
                int neighbor = e.target;

                ////////
                //Remove Node Connection to this community
                Float edgesTo = nodeConnectionsWeight[neighbor].get(community);
                Integer countEdgesTo = nodeConnectionsCount[neighbor].get(community);
                if (countEdgesTo - 1 == 0) {
                    //System.out.println("REMOVE links from node "+neighbor+" to community "+community.id);
                    nodeConnectionsWeight[neighbor].remove(community);
                    nodeConnectionsCount[neighbor].remove(community);
                } else {
                    //System.out.println("Add links from node "+neighbor+" to community "+community.id);
                    nodeConnectionsWeight[neighbor].put(community, edgesTo - e.weight);
                    nodeConnectionsCount[neighbor].put(community, countEdgesTo - 1);
                }

                ///////////////////
                //Remove Adjacent Community's connection to this community
                StatisticalInferenceClustering.Community adjCom = nodeCommunities[neighbor];
                Float oEdgesto = adjCom.connectionsWeight.get(community);
                Integer oCountEdgesto = adjCom.connectionsCount.get(community);
                if (oCountEdgesto - 1 == 0) {
                    //System.out.println("Remove links from community "+adjCom.id+" to community "+community.id+" *");
                    adjCom.connectionsWeight.remove(community);
                    adjCom.connectionsCount.remove(community);
                } else {
                    //System.out.println("Remove links from community "+adjCom.id+" to community "+community.id);
                    adjCom.connectionsWeight.put(community, oEdgesto - e.weight);
                    adjCom.connectionsCount.put(community, oCountEdgesto - 1);
                }

                if (node == neighbor) {
                    continue;
                }

                if (adjCom != community) {
                    Float comEdgesto = community.connectionsWeight.get(adjCom);
                    Integer comCountEdgesto = community.connectionsCount.get(adjCom);
                    if (comCountEdgesto - 1 == 0) {
                        //System.out.println("Remove links from community "+community.id+" to community "+adjCom.id+" *");
                        community.connectionsWeight.remove(adjCom);
                        community.connectionsCount.remove(adjCom);
                    } else {
                        //System.out.println("Remove links from community "+community.id+" to community "+adjCom.id);
                        community.connectionsWeight.put(adjCom, comEdgesto - e.weight);
                        community.connectionsCount.put(adjCom, comCountEdgesto - 1);
                    }
                }

                Float nodeEgesTo = nodeConnectionsWeight[node].get(adjCom);
                Integer nodeCountEgesTo = nodeConnectionsCount[node].get(adjCom);
                if (nodeCountEgesTo - 1 == 0) {
                    //System.out.println("REMOVE links from node "+node+" to community "+adjCom.id+ " *");
                    nodeConnectionsWeight[node].remove(adjCom);
                    nodeConnectionsCount[node].remove(adjCom);
                } else {
                    //System.out.println("REMOVE links from node "+node+" to community "+adjCom.id);
                    nodeConnectionsWeight[node].put(adjCom, nodeEgesTo - e.weight);
                    nodeConnectionsCount[node].put(adjCom, nodeCountEgesTo - 1);
                }

            }
            community.remove(node);
        }

        private void moveNodeTo(int node, StatisticalInferenceClustering.Community to) {
            //System.out.println("### MOVE NODE "+node+" TO COM "+to.id);
            removeNodeFromItsCommunity(node);
            addNodeTo(node, to);
        }

        protected void _moveNodeTo(int node, StatisticalInferenceClustering.Community to) {
            // NOTE: THIS IS FOR UNIT TEST PURPOSE ONLY
            moveNodeTo(node, to);
        }

        protected void _zoomOut() {
            // NOTE: THIS IS FOR UNIT TEST PURPOSE ONLY
            zoomOut();
        }

        private void zoomOut() {
            //System.out.println("### ZOOM OUT");
            int M = communities.size();
            // The new topology uses preexisting communities as nodes
            ArrayList<ComputationEdge>[] newTopology = new ArrayList[M];
            int index = 0;
            // nodeCommunities is an index of the communities per node.
            // In this context, the preexisting communities will become the nodes
            // of new upper-level communities (meta-communities).
            nodeCommunities = new StatisticalInferenceClustering.Community[M];
            nodeConnectionsWeight = new HashMap[M];
            nodeConnectionsCount = new HashMap[M];
            double[] oldGraphNodeCount = graphNodeCount.clone();
            HashMap<Integer, StatisticalInferenceClustering.Community> newInvMap = new HashMap<>();
            for (int i = 0; i < communities.size(); i++) {
                // For each community "com", that we want to transform into a node in the new topology...
                StatisticalInferenceClustering.Community com = communities.get(i);
                nodeConnectionsWeight[index] = new HashMap<>();
                nodeConnectionsCount[index] = new HashMap<>();

                newTopology[index] = new ArrayList<>();
                // For each community "com", we create a meta-community nodeCommunities[index] containing only it
                nodeCommunities[index] = new StatisticalInferenceClustering.Community(com);
                // iter is the set of communities with which com has (weighted) links.
                Set<StatisticalInferenceClustering.Community> iter = com.connectionsWeight.keySet();
                // weightSum is the number of edges from the community (into itself or not)
                double weightSum = 0;
                double graphNodeSum = 0;

                StatisticalInferenceClustering.Community hidden =
                    new StatisticalInferenceClustering.Community(structure);
                for (Integer nodeInt : com.nodes) {
                    graphNodeSum += oldGraphNodeCount[nodeInt];
                    StatisticalInferenceClustering.Community oldHidden = invMap.get(nodeInt);
                    hidden.nodes.addAll(oldHidden.nodes);
                }
                newInvMap.put(index, hidden);
                for (StatisticalInferenceClustering.Community adjCom : iter) {
                    // adjCom is an adjacent community to com
                    int target = communities.indexOf(adjCom);
                    float weight = com.connectionsWeight.get(adjCom);
                    if (target == index) {
                        weightSum += 2. * weight;
                    } else {
                        weightSum += weight;
                    }
                    ComputationEdge e = new ComputationEdge(index, target, weight);
                    newTopology[index].add(e);
                }
                weights[index] = weightSum;
                graphNodeCount[index] = graphNodeSum;
                internalWeights[index] = com.internalWeightSum;
                nodeCommunities[index].seed(index);

                index++;
            }
            communities.clear();

            for (int i = 0; i < M; i++) {
                StatisticalInferenceClustering.Community com = nodeCommunities[i];
                communities.add(com);
                for (ComputationEdge e : newTopology[i]) {
                    //System.out.println("Add links from node "+i+" to community "+nodeCommunities[e.target].id);
                    nodeConnectionsWeight[i].put(nodeCommunities[e.target], e.weight);
                    nodeConnectionsCount[i].put(nodeCommunities[e.target], 1);
                    //System.out.println("Add links from community "+com.id+" to community "+nodeCommunities[e.target].id);
                    com.connectionsWeight.put(nodeCommunities[e.target], e.weight);
                    com.connectionsCount.put(nodeCommunities[e.target], 1);
                }

            }

            N = M;
            topology = newTopology;
            invMap = newInvMap;
        }

        public String getMonitoring() {
            String monitoring = "";

            for (StatisticalInferenceClustering.Community com : communities) {
                monitoring += "com" + com.id + "[";
                int count = 0;
                for (Integer node : com.nodes) {
                    StatisticalInferenceClustering.Community hidden = invMap.get(node);
                    if (count++ > 0) {
                        monitoring += " ";
                    }
                    monitoring += "n" + node + "(" + hidden.getMonitoring() + ")";
                }
                monitoring += "]  ";
            }

            return monitoring;
        }

        // Useful for monitoring and debugging
        public boolean checkIntegrity() {
            boolean integrity = true;
            Double E = graphWeightSum;
            Double e_in = communities.stream().mapToDouble(c -> c.internalWeightSum).sum();
            Double e_out = E - e_in;
            Double B = Double.valueOf(communities.size());
            Double N = Double.valueOf(graph.getNodeCount());

            // Check the integrity of nodeConnectionsWeight
            double nodeComWeightSum = 0;
            for (int node = 0; node < nodeConnectionsWeight.length; node++) {
                HashMap<StatisticalInferenceClustering.Community, Float> hm = nodeConnectionsWeight[node];
                Collection<Float> values = hm.values();
                nodeComWeightSum += values.stream().mapToDouble(v -> (double) v).sum();
            }

            // TODO: what should be done, in fact,
            // is to check that for each node the sum of nodeConnectionsWeight
            // equals its degree.

            return integrity;
        }
    }

    static class ComputationEdge {

        int source;
        int target;
        float weight;

        public ComputationEdge(int s, int t, float w) {
            source = s;
            target = t;
            weight = w;
        }
    }
}
