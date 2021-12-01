package org.gephi.statistics.plugin;

import org.gephi.graph.api.*;
import org.gephi.statistics.spi.Statistics;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
//import org.apache.commons.math3.special.Gamma;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

public class StatisticalInferenceClustering implements Statistics, LongTask {

    private boolean isCanceled;
    private StatisticalInferenceClustering.CommunityStructure structure;
    public static final String STAT_INF_CLASS = "stat_inf_class";
    private ProgressTicket progress;
    private double descriptionLength;
    private boolean useWeight = false;

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

        graph.readLock();
        try {
            structure = new StatisticalInferenceClustering.CommunityStructure(graph);
            int[] comStructure = new int[graph.getNodeCount()];

            if (graph.getNodeCount() > 0) {//Fixes issue #713 Modularity Calculation Throws Exception On Empty Graph
                HashMap<String, Double> computedStatInfMetrics =
                        computePartition(graph, structure, comStructure, useWeight);
                descriptionLength = computedStatInfMetrics.get("descriptionLength");
            } else {
                descriptionLength = 0;
            }

            saveValues(comStructure, graph, structure);
        } finally {
            graph.readUnlock();
        }
    }

    protected HashMap<String, Double> computePartition(Graph graph, StatisticalInferenceClustering.CommunityStructure theStructure,
                                                        int[] comStructure,
                                                        boolean weighted) {
        isCanceled = false;
        Progress.start(progress);
        Random rand = new Random();

        double totalWeight = theStructure.graphWeightSum;
        double[] nodeDegrees = theStructure.weights.clone();

        HashMap<String, Double> results = new HashMap<>();

        if (isCanceled) {
            return results;
        }
        boolean someChange = true;
        while (someChange) {
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
                    StatisticalInferenceClustering.Community bestCommunity = updateBestCommunity(theStructure, i);
                    if ((theStructure.nodeCommunities[i] != bestCommunity) && (bestCommunity != null)) {
                        theStructure.moveNodeTo(i, bestCommunity);
                        localChange = true;
                    }
                    if (isCanceled) {
                        return results;
                    }
                }
                someChange = localChange || someChange;
                if (isCanceled) {
                    return results;
                }
            }

            if (someChange) {
                theStructure.zoomOut();
            }
        }

        fillComStructure(graph, theStructure, comStructure);
        double[] degreeCount = fillDegreeCount(graph, theStructure, comStructure, nodeDegrees, weighted);

        double computedDescriptionLength = finalDL(comStructure, degreeCount, graph, theStructure, totalWeight, weighted);

        results.put("descriptionLength", computedDescriptionLength);

        return results;
    }

    private StatisticalInferenceClustering.Community updateBestCommunity(StatisticalInferenceClustering.CommunityStructure theStructure, int node_id) {
        // Total number of edges (graph size)
        Double E = theStructure.graphWeightSum;
        // Total number of edges from one community to the same one
        Double e_in = theStructure.communities.stream().mapToDouble(c -> c.internalWeightSum).sum();
        // Total number of edges from one community to another
        Double e_out = E - e_in;

        double best = Double.MAX_VALUE;
        StatisticalInferenceClustering.Community bestCommunity = null;
        Set<StatisticalInferenceClustering.Community> iter = theStructure.nodeConnectionsWeight[node_id].keySet();
        for (StatisticalInferenceClustering.Community com : iter) {
            double deltaValue = delta(node_id, com, theStructure);
            if (deltaValue < best) {
                best = deltaValue;
                bestCommunity = com;
            }
        }
        return bestCommunity;
    }

    private double delta(int node, StatisticalInferenceClustering.Community community, StatisticalInferenceClustering.CommunityStructure theStructure) {
        Float edgesToFloat = theStructure.nodeConnectionsWeight[node].get(community);
        double edgesTo = 0;
        if (edgesToFloat != null) {
            edgesTo = edgesToFloat.doubleValue();
        }

        // We need:
        // * The number of nodes in each community
        // theStructure.communities.stream().forEach(e->e.nodes.size());
        // * The total number of edges that are between communities (not inside)
        // NOTE: WRONG
        // This is the sum, for each node, and for each community, of their weight (weighted connexions)
        // Arrays.stream(theStructure.nodeConnectionsWeight).map(hm -> hm.values().stream().mapToDouble(d->d).sum()).mapToDouble(d->d).sum();
        // * The total number of edges inside each community
        // * The number of communities
        // theStructure.communities.size()
        // * We need the degree of each node
        //

        // Number of edges of target community (with itself or another one)
        Double e_r_target = community.weightSum;
        // Number of edges within target community
        Double e_rr_target = community.internalWeightSum;
        // Number of nodes of target community
        int n_r_target = community.nodes.size();

        // Number of edges of current (where the node belongs) community (with itself or another one)
        Double e_r_current = theStructure.nodeCommunities[node].weightSum;
        // Number of edges within current community (where the node belongs)
        Double e_rr_current = theStructure.nodeCommunities[node].internalWeightSum;
        // Number of nodes of target community
        int n_r_current = theStructure.nodeCommunities[node].nodes.size();

        // Description length: before
        //Double S_b = Gamma.logGamma(e_r_target);

        // Count the gains and losses
        // -> loop over the neighbors

        // Check if some communities would disappear (?)
        // Aggregate the computations to obtain the delta for the change

        double weightSum = community.weightSum;
        double nodeWeight = theStructure.weights[node];
        double qValue = currentResolution * edgesTo - (nodeWeight * weightSum) / (2.0 * theStructure.graphWeightSum);
        if ((theStructure.nodeCommunities[node] == community) && (theStructure.nodeCommunities[node].size() > 1)) {
            qValue = currentResolution * edgesTo -
                    (nodeWeight * (weightSum - nodeWeight)) / (2.0 * theStructure.graphWeightSum);
        }
        if ((theStructure.nodeCommunities[node] == community) && (theStructure.nodeCommunities[node].size() == 1)) {
            qValue = 0.;
        }
        return qValue;
    }

    private int[] fillComStructure(Graph graph, StatisticalInferenceClustering.CommunityStructure theStructure, int[] comStructure) {
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

    private double[] fillDegreeCount(Graph graph, StatisticalInferenceClustering.CommunityStructure theStructure, int[] comStructure,
                                     double[] nodeDegrees, boolean weighted) {
        double[] degreeCount = new double[theStructure.communities.size()];

        for (Node node : graph.getNodes()) {
            int index = theStructure.map.get(node);
            if (weighted) {
                degreeCount[comStructure[index]] += nodeDegrees[index];
            } else {
                degreeCount[comStructure[index]] += graph.getDegree(node);
            }

        }
        return degreeCount;
    }

    private double finalDL(int[] struct, double[] degrees, Graph graph,
                           StatisticalInferenceClustering.CommunityStructure theStructure, double totalWeight,
                           boolean weighted) {

        int usedResolution = 1; // TODO: REMOVE ME (currentResolution should not appear in statistical inference)
        double res = 0;
        double[] internal = new double[degrees.length];
        for (Node n : graph.getNodes()) {
            int n_index = theStructure.map.get(n);
            for (Edge edge : graph.getEdges(n)) {
                Node neighbor = graph.getOpposite(n, edge);
                if (n == neighbor) {
                    continue;
                }
                int neigh_index = theStructure.map.get(neighbor);
                if (struct[neigh_index] == struct[n_index]) {
                    if (weighted) {
                        internal[struct[neigh_index]] += edge.getWeight(graph.getView());
                    } else {
                        internal[struct[neigh_index]]++;
                    }
                }
            }
        }
        for (int i = 0; i < degrees.length; i++) {
            internal[i] /= 2.0;
            res += usedResolution * (internal[i] / totalWeight) - Math.pow(degrees[i] / (2 * totalWeight), 2);//HERE
        }
        return res;
    }

    private void saveValues(int[] struct, Graph graph, StatisticalInferenceClustering.CommunityStructure theStructure) {
        Table nodeTable = graph.getModel().getNodeTable();
        Column modCol = nodeTable.getColumn(STAT_INF_CLASS);
        if (modCol == null) {
            modCol = nodeTable.addColumn(STAT_INF_CLASS, "Modularity Class", Integer.class, 0);
        }
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
                "Modularity Class",
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

    class CommunityStructure {

        HashMap<StatisticalInferenceClustering.Community, Float>[] nodeConnectionsWeight;
        HashMap<StatisticalInferenceClustering.Community, Integer>[] nodeConnectionsCount;
        HashMap<Node, Integer> map;
        StatisticalInferenceClustering.Community[] nodeCommunities;
        Graph graph;
        double[] weights; // The weighted degree of the nodes (in short)
        double graphWeightSum; // The weighted sum of degrees
        List<ComputationEdge>[] topology;
        List<StatisticalInferenceClustering.Community> communities;
        int N;
        HashMap<Integer, StatisticalInferenceClustering.Community> invMap;


        CommunityStructure(Graph graph) {
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
            // nodeCommunities is an index of which community each node belongs to
            nodeCommunities = new StatisticalInferenceClustering.Community[N];
            map = new HashMap<>(); // keeps track of the integer ids of the nodes
            // The topology is basically an index of the outbound computation edges for each node
            topology = new ArrayList[N];
            communities = new ArrayList<>();
            int index = 0;
            weights = new double[N]; // The weight is basically the weighted degree of a node

            NodeIterable nodesIterable = graph.getNodes();
            for (Node node : nodesIterable) {
                map.put(node, index);
                nodeCommunities[index] = new StatisticalInferenceClustering.Community(this);

                nodeConnectionsWeight[index] = new HashMap<>();
                nodeConnectionsCount[index] = new HashMap<>();
                weights[index] = 0;
                nodeCommunities[index].seed(index);
                StatisticalInferenceClustering.Community hidden = new StatisticalInferenceClustering.Community(structure);
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
                    ComputationEdge ce = new ComputationEdge(node_index, neighbor_index, weight);
                    topology[node_index].add(ce);
                    StatisticalInferenceClustering.Community adjCom = nodeCommunities[neighbor_index];

                    nodeConnectionsWeight[node_index].put(adjCom, weight);
                    nodeConnectionsCount[node_index].put(adjCom, 1);

                    StatisticalInferenceClustering.Community nodeCom = nodeCommunities[node_index];
                    nodeCom.connectionsWeight.put(adjCom, weight);
                    nodeCom.connectionsCount.put(adjCom, 1);

                    nodeConnectionsWeight[neighbor_index].put(nodeCom, weight);
                    nodeConnectionsCount[neighbor_index].put(nodeCom, 1);

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
            to.add(node);
            nodeCommunities[node] = to;

            for (ComputationEdge e : topology[node]) {
                int neighbor = e.target;

                ////////
                //Remove Node Connection to this community
                Float neighEdgesTo = nodeConnectionsWeight[neighbor].get(to);
                if (neighEdgesTo == null) {
                    nodeConnectionsWeight[neighbor].put(to, e.weight);
                } else {
                    nodeConnectionsWeight[neighbor].put(to, neighEdgesTo + e.weight);
                }
                Integer neighCountEdgesTo = nodeConnectionsCount[neighbor].get(to);
                if (neighCountEdgesTo == null) {
                    nodeConnectionsCount[neighbor].put(to, 1);
                } else {
                    nodeConnectionsCount[neighbor].put(to, neighCountEdgesTo + 1);
                }

                ///////////////////
                StatisticalInferenceClustering.Community adjCom = nodeCommunities[neighbor];
                Float wEdgesto = adjCom.connectionsWeight.get(to);
                if (wEdgesto == null) {
                    adjCom.connectionsWeight.put(to, e.weight);
                } else {
                    adjCom.connectionsWeight.put(to, wEdgesto + e.weight);
                }

                Integer cEdgesto = adjCom.connectionsCount.get(to);
                if (cEdgesto == null) {
                    adjCom.connectionsCount.put(to, 1);
                } else {
                    adjCom.connectionsCount.put(to, cEdgesto + 1);
                }

                Float nodeEdgesTo = nodeConnectionsWeight[node].get(adjCom);
                if (nodeEdgesTo == null) {
                    nodeConnectionsWeight[node].put(adjCom, e.weight);
                } else {
                    nodeConnectionsWeight[node].put(adjCom, nodeEdgesTo + e.weight);
                }

                Integer nodeCountEdgesTo = nodeConnectionsCount[node].get(adjCom);
                if (nodeCountEdgesTo == null) {
                    nodeConnectionsCount[node].put(adjCom, 1);
                } else {
                    nodeConnectionsCount[node].put(adjCom, nodeCountEdgesTo + 1);
                }

                if (to != adjCom) {
                    Float comEdgesto = to.connectionsWeight.get(adjCom);
                    if (comEdgesto == null) {
                        to.connectionsWeight.put(adjCom, e.weight);
                    } else {
                        to.connectionsWeight.put(adjCom, comEdgesto + e.weight);
                    }

                    Integer comCountEdgesto = to.connectionsCount.get(adjCom);
                    if (comCountEdgesto == null) {
                        to.connectionsCount.put(adjCom, 1);
                    } else {
                        to.connectionsCount.put(adjCom, comCountEdgesto + 1);
                    }

                }
            }
        }

        private void removeNodeFromItsCommunity(int node) {
            StatisticalInferenceClustering.Community community = nodeCommunities[node];
            for (ComputationEdge e : topology[node]) {
                int neighbor = e.target;

                ////////
                //Remove Node Connection to this community
                Float edgesTo = nodeConnectionsWeight[neighbor].get(community);
                Integer countEdgesTo = nodeConnectionsCount[neighbor].get(community);
                if (countEdgesTo - 1 == 0) {
                    nodeConnectionsWeight[neighbor].remove(community);
                    nodeConnectionsCount[neighbor].remove(community);
                } else {
                    nodeConnectionsWeight[neighbor].put(community, edgesTo - e.weight);
                    nodeConnectionsCount[neighbor].put(community, countEdgesTo - 1);
                }

                ///////////////////
                //Remove Adjacency Community's connection to this community
                StatisticalInferenceClustering.Community adjCom = nodeCommunities[neighbor];
                Float oEdgesto = adjCom.connectionsWeight.get(community);
                Integer oCountEdgesto = adjCom.connectionsCount.get(community);
                if (oCountEdgesto - 1 == 0) {
                    adjCom.connectionsWeight.remove(community);
                    adjCom.connectionsCount.remove(community);
                } else {
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
                        community.connectionsWeight.remove(adjCom);
                        community.connectionsCount.remove(adjCom);
                    } else {
                        community.connectionsWeight.put(adjCom, comEdgesto - e.weight);
                        community.connectionsCount.put(adjCom, comCountEdgesto - 1);
                    }
                }

                Float nodeEgesTo = nodeConnectionsWeight[node].get(adjCom);
                Integer nodeCountEgesTo = nodeConnectionsCount[node].get(adjCom);
                if (nodeCountEgesTo - 1 == 0) {
                    nodeConnectionsWeight[node].remove(adjCom);
                    nodeConnectionsCount[node].remove(adjCom);
                } else {
                    nodeConnectionsWeight[node].put(adjCom, nodeEgesTo - e.weight);
                    nodeConnectionsCount[node].put(adjCom, nodeCountEgesTo - 1);
                }

            }
            community.remove(node);
        }

        private void moveNodeTo(int node, StatisticalInferenceClustering.Community to) {
            removeNodeFromItsCommunity(node);
            addNodeTo(node, to);
        }

        private void zoomOut() {
            int M = communities.size();
            ArrayList<ComputationEdge>[] newTopology = new ArrayList[M];
            int index = 0;
            nodeCommunities = new StatisticalInferenceClustering.Community[M];
            nodeConnectionsWeight = new HashMap[M];
            nodeConnectionsCount = new HashMap[M];
            HashMap<Integer, StatisticalInferenceClustering.Community> newInvMap = new HashMap<>();
            for (int i = 0; i < communities.size(); i++) {//Community com : mCommunities) {
                StatisticalInferenceClustering.Community com = communities.get(i);
                nodeConnectionsWeight[index] = new HashMap<>();
                nodeConnectionsCount[index] = new HashMap<>();

                newTopology[index] = new ArrayList<>();
                nodeCommunities[index] = new StatisticalInferenceClustering.Community(com);
                Set<StatisticalInferenceClustering.Community> iter = com.connectionsWeight.keySet();
                double weightSum = 0;

                StatisticalInferenceClustering.Community hidden = new StatisticalInferenceClustering.Community(structure);
                for (Integer nodeInt : com.nodes) {
                    StatisticalInferenceClustering.Community oldHidden = invMap.get(nodeInt);
                    hidden.nodes.addAll(oldHidden.nodes);
                }
                newInvMap.put(index, hidden);
                for (StatisticalInferenceClustering.Community adjCom : iter) {
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
                nodeCommunities[index].seed(index);

                index++;
            }
            communities.clear();

            for (int i = 0; i < M; i++) {
                StatisticalInferenceClustering.Community com = nodeCommunities[i];
                communities.add(com);
                for (ComputationEdge e : newTopology[i]) {
                    nodeConnectionsWeight[i].put(nodeCommunities[e.target], e.weight);
                    nodeConnectionsCount[i].put(nodeCommunities[e.target], 1);
                    com.connectionsWeight.put(nodeCommunities[e.target], e.weight);
                    com.connectionsCount.put(nodeCommunities[e.target], 1);
                }

            }

            N = M;
            topology = newTopology;
            invMap = newInvMap;
        }
    }

    class Community {

        double weightSum; // e_r, i.e. sum of edge weights for the community, inside and outside altogether
        double internalWeightSum; // e_rr, i.e. sum of internal edge weights
        StatisticalInferenceClustering.CommunityStructure structure;
        List<Integer> nodes;
        HashMap<StatisticalInferenceClustering.Community, Float> connectionsWeight;
        HashMap<StatisticalInferenceClustering.Community, Integer> connectionsCount;

        public Community(StatisticalInferenceClustering.Community com) {
            structure = com.structure;
            connectionsWeight = new HashMap<>();
            connectionsCount = new HashMap<>();
            nodes = new ArrayList<>();
        }

        public Community(StatisticalInferenceClustering.CommunityStructure structure) {
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
            internalWeightSum = 0;
        }

        public boolean add(int node) {
            nodes.add(node);
            weightSum += structure.weights[node];
            internalWeightSum += structure.nodeConnectionsWeight[node].get(structure.nodeCommunities[node]);
            return true;
        }

        public boolean remove(int node) {
            boolean result = nodes.remove((Integer) node);
            weightSum -= structure.weights[node];
            internalWeightSum -= structure.nodeConnectionsWeight[node].get(structure.nodeCommunities[node]);
            if (nodes.isEmpty()) {
                structure.communities.remove(this);
            }
            return result;
        }
    }

    class ComputationEdge {

        int source;
        int target;
        float weight;

        public ComputationEdge(int s, int t, float w) {
            source = s;
            target = t;
            weight = w;
        }
    }

    public double getDescriptionLength() {
        return descriptionLength;
    }


}
