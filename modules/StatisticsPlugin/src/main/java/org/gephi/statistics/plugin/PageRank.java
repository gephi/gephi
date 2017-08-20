/*
 Copyright 2008-2011 Gephi
 Authors : Patick J. McSweeney <pjmcswee@syr.edu>, Sebastien Heymann <seb@gephi.org>
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

import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.graph.api.Table;
import org.gephi.graph.api.UndirectedGraph;
import org.gephi.statistics.spi.Statistics;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.openide.util.Lookup;

/**
 * Ref: Sergey Brin, Lawrence Page, The Anatomy of a Large-Scale Hypertextual Web Search Engine, in Proceedings of the seventh International Conference on the World Wide Web (WWW1998):107-117
 *
 * @author pjmcswee
 */
public class PageRank implements Statistics, LongTask {

    public static final String PAGERANK = "pageranks";
    /**
     *
     */
    private ProgressTicket progress;
    /**
     *
     */
    private boolean isCanceled;
    /**
     *
     */
    private double epsilon = 0.001;
    /**
     *
     */
    private double probability = 0.85;
    private boolean useEdgeWeight = false;
    /**
     *
     */
    private double[] pageranks;
    /**
     *
     */
    private boolean isDirected;

    public PageRank() {
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        if (graphController != null && graphController.getGraphModel() != null) {
            isDirected = graphController.getGraphModel().isDirected();
        }
    }

    public void setDirected(boolean isDirected) {
        this.isDirected = isDirected;
    }

    /**
     *
     * @return
     */
    public boolean getDirected() {
        return isDirected;
    }

    @Override
    public void execute(GraphModel graphModel) {
        Graph graph;
        if (isDirected) {
            graph = graphModel.getDirectedGraphVisible();
        } else {
            graph = graphModel.getUndirectedGraphVisible();
        }
        execute(graph);
    }

    public void execute(Graph graph) {
        isCanceled = false;

        Column column = initializeAttributeColunms(graph.getModel());

        graph.readLock();
        try {
            HashMap<Node, Integer> indicies = createIndiciesMap(graph);

            pageranks = calculatePagerank(graph, indicies, isDirected, useEdgeWeight, epsilon, probability);

            saveCalculatedValues(graph, column, indicies, pageranks);
        } finally {
            graph.readUnlockAll();
        }
    }

    private Column initializeAttributeColunms(GraphModel graphModel) {
        Table nodeTable = graphModel.getNodeTable();
        Column pagerankCol = nodeTable.getColumn(PAGERANK);

        if (pagerankCol == null) {
            pagerankCol = nodeTable.addColumn(PAGERANK, "PageRank", Double.class, new Double(0));
        }

        return pagerankCol;
    }

    private void saveCalculatedValues(Graph graph, Column attributeColumn, HashMap<Node, Integer> indicies,
            double[] nodePagrank) {
        for (Node s : graph.getNodes()) {
            int s_index = indicies.get(s);

            s.setAttribute(attributeColumn, nodePagrank[s_index]);
        }
    }

    private void setInitialValues(Graph graph, Map<Node, Integer> indicies, double[] pagerankValues, double[] weights, boolean directed, boolean useWeights) {
        final int N = graph.getNodeCount();
        for (Node s : graph.getNodes()) {
            final int index = indicies.get(s);
            pagerankValues[index] = 1.0 / N;
            if (useWeights) {
                double sum = 0;
                EdgeIterable eIter;
                if (directed) {
                    eIter = ((DirectedGraph) graph).getOutEdges(s);
                } else {
                    eIter = ((UndirectedGraph) graph).getEdges(s);
                }
                for (Edge edge : eIter) {
                    if(!edge.isSelfLoop()){
                        sum += edge.getWeight();
                    }
                }
                weights[index] = sum;
            }
        }
    }

    private double calculateR(Graph graph, double[] pagerankValues, HashMap<Node, Integer> indicies, boolean directed, double prob) {
        int N = graph.getNodeCount();
        double r = (1.0 - prob) / N;//Initialize to damping factor

        //Calculate dangling nodes (nodes without out edges) contribution to all other nodes.
        //Necessary for all nodes page rank values sum to be 1
        NodeIterable nodesIterable = graph.getNodes();
        double danglingNodesRankContrib = 0;
        for (Node s : graph.getNodes()) {
            int s_index = indicies.get(s);
            int outDegree;
            if (directed) {
                outDegree = ((DirectedGraph) graph).getOutDegree(s);
            } else {
                outDegree = graph.getDegree(s);
            }
            if (outDegree == 0) {
                danglingNodesRankContrib += pagerankValues[s_index];
            }

            if (isCanceled) {
                nodesIterable.doBreak();
                break;
            }
        }
        danglingNodesRankContrib *= prob / N;
        r += danglingNodesRankContrib;

        return r;
    }

    private Map<Node, Set<Node>> calculateInNeighborsPerNode(Graph graph, boolean directed) {
        Map<Node, Set<Node>> inNeighborsPerNode = new Object2ObjectOpenHashMap<>();

        NodeIterable nodesIterable = graph.getNodes();
        for (Node node : nodesIterable) {
            Set<Node> nodeInNeighbors = new ObjectOpenHashSet<>();

            EdgeIterable edgesIterable;
            if (directed) {
                edgesIterable = ((DirectedGraph) graph).getInEdges(node);
            } else {
                edgesIterable = graph.getEdges(node);
            }

            for (Edge edge : edgesIterable) {
                if (!edge.isSelfLoop()) {
                    Node neighbor = graph.getOpposite(node, edge);
                    nodeInNeighbors.add(neighbor);
                }

                if (isCanceled) {
                    edgesIterable.doBreak();
                    break;
                }
            }

            inNeighborsPerNode.put(node, nodeInNeighbors);

            if (isCanceled) {
                nodesIterable.doBreak();
                break;
            }
        }

        return inNeighborsPerNode;
    }

    private Map<Node, Object2DoubleOpenHashMap<Node>> calculateInWeightPerNodeAndNeighbor(Graph graph, boolean directed, boolean useWeights) {
        Object2ObjectOpenHashMap<Node, Object2DoubleOpenHashMap<Node>> inWeightPerNodeAndNeighbor = new Object2ObjectOpenHashMap<>();

        if (useWeights) {
            NodeIterable nodesIterable = graph.getNodes();
            for (Node node : nodesIterable) {
                Object2DoubleOpenHashMap<Node> inWeightPerNeighbor = new Object2DoubleOpenHashMap<>();
                inWeightPerNeighbor.defaultReturnValue(0);

                EdgeIterable edgesIterable;
                if (directed) {
                    edgesIterable = ((DirectedGraph) graph).getInEdges(node);
                } else {
                    edgesIterable = graph.getEdges(node);
                }

                for (Edge edge : edgesIterable) {
                    if (!edge.isSelfLoop()) {
                        Node neighbor = graph.getOpposite(node, edge);
                        inWeightPerNeighbor.addTo(neighbor, edge.getWeight());
                    }

                    if (isCanceled) {
                        edgesIterable.doBreak();
                        break;
                    }
                }

                if (isCanceled) {
                    nodesIterable.doBreak();
                    break;
                }
                
                inWeightPerNodeAndNeighbor.put(node, inWeightPerNeighbor);
            }
        }

        return inWeightPerNodeAndNeighbor;
    }

    private double updateValueForNode(Graph graph, Node node, double[] pagerankValues, double[] weights,
            HashMap<Node, Integer> indicies, boolean directed, boolean useWeights, double r, double prob,
            Map<Node, Set<Node>> inNeighborsPerNode, final Object2DoubleOpenHashMap<Node> inWeightPerNeighbor) {
        double res = r;

        double sumNeighbors = 0;
        for (Node neighbor : inNeighborsPerNode.get(node)) {
            int neigh_index = indicies.get(neighbor);

            if (useWeights) {
                double weight = inWeightPerNeighbor.getDouble(neighbor) / weights[neigh_index];
                sumNeighbors += pagerankValues[neigh_index] * weight;
            } else {
                int outDegree;
                if (directed) {
                    outDegree = ((DirectedGraph) graph).getOutDegree(neighbor);
                } else {
                    outDegree = graph.getDegree(neighbor);
                }
                sumNeighbors += (pagerankValues[neigh_index] / outDegree);
            }
        }

        res += prob * sumNeighbors;

        return res;
    }

    double[] calculatePagerank(Graph graph, HashMap<Node, Integer> indicies,
            boolean directed, boolean useWeights, double eps, double prob) {
        int N = graph.getNodeCount();
        double[] pagerankValues = new double[N];
        double[] temp = new double[N];

        Progress.start(progress);
        final double[] weights = useWeights ? new double[N] : null;
        final Map<Node, Set<Node>> inNeighborsPerNode = calculateInNeighborsPerNode(graph, directed);
        final Map<Node, Object2DoubleOpenHashMap<Node>> inWeightPerNodeAndNeighbor = calculateInWeightPerNodeAndNeighbor(graph, directed, useWeights);

        setInitialValues(graph, indicies, pagerankValues, weights, directed, useWeights);

        while (true) {
            boolean done = true;

            double r = calculateR(graph, pagerankValues, indicies, directed, prob);
            NodeIterable nodesIterable = graph.getNodes();
            for (Node s : nodesIterable) {
                int s_index = indicies.get(s);
                temp[s_index] = updateValueForNode(graph, s, pagerankValues, weights, indicies, directed, useWeights, r, prob, inNeighborsPerNode, inWeightPerNodeAndNeighbor.get(s));

                if ((temp[s_index] - pagerankValues[s_index]) / pagerankValues[s_index] >= eps) {
                    done = false;
                }

                if (isCanceled) {
                    nodesIterable.doBreak();
                    return pagerankValues;
                }

            }
            pagerankValues = temp;
            temp = new double[N];
            if ((done) || (isCanceled)) {
                break;
            }

        }
        return pagerankValues;
    }

    public HashMap<Node, Integer> createIndiciesMap(Graph graph) {
        HashMap<Node, Integer> newIndicies = new HashMap<>();
        int index = 0;
        for (Node s : graph.getNodes()) {
            newIndicies.put(s, index);
            index++;
        }
        return newIndicies;
    }

    /**
     *
     * @return
     */
    @Override
    public String getReport() {
        //distribution of values
        Map<Double, Integer> dist = new HashMap<>();
        for (int i = 0; i < pageranks.length; i++) {
            Double d = pageranks[i];
            if (dist.containsKey(d)) {
                Integer v = dist.get(d);
                dist.put(d, v + 1);
            } else {
                dist.put(d, 1);
            }
        }

        //Distribution series
        XYSeries dSeries = ChartUtils.createXYSeries(dist, "PageRanks");

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(dSeries);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "PageRank Distribution",
                "Score",
                "Count",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                false,
                false);
        chart.removeLegend();
        ChartUtils.decorateChart(chart);
        ChartUtils.scaleChart(chart, dSeries, true);
        String imageFile = ChartUtils.renderChart(chart, "pageranks.png");

        String report = "<HTML> <BODY> <h1>PageRank Report </h1> "
                + "<hr> <br />"
                + "<h2> Parameters: </h2>"
                + "Epsilon = " + epsilon + "<br>"
                + "Probability = " + probability
                + "<br> <h2> Results: </h2>"
                + imageFile
                + "<br /><br />" + "<h2> Algorithm: </h2>"
                + "Sergey Brin, Lawrence Page, <i>The Anatomy of a Large-Scale Hypertextual Web Search Engine</i>, in Proceedings of the seventh International Conference on the World Wide Web (WWW1998):107-117<br />"
                + "</BODY> </HTML>";

        return report;

    }

    /**
     *
     * @return
     */
    @Override
    public boolean cancel() {
        isCanceled = true;
        return true;
    }

    /**
     *
     * @param progressTicket
     */
    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        progress = progressTicket;
    }

    /**
     *
     * @param prob
     */
    public void setProbability(double prob) {
        probability = prob;
    }

    /**
     *
     * @param eps
     */
    public void setEpsilon(double eps) {
        epsilon = eps;
    }

    /**
     *
     * @return
     */
    public double getProbability() {
        return probability;
    }

    /**
     *
     * @return
     */
    public double getEpsilon() {
        return epsilon;
    }

    public boolean isUseEdgeWeight() {
        return useEdgeWeight;
    }

    public void setUseEdgeWeight(boolean useEdgeWeight) {
        this.useEdgeWeight = useEdgeWeight;
    }
}
