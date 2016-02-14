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

import java.util.HashMap;
import java.util.Map;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
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
 * Ref: Sergey Brin, Lawrence Page, The Anatomy of a Large-Scale Hypertextual
 * Web Search Engine, in Proceedings of the seventh International Conference on
 * the World Wide Web (WWW1998):107-117
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

    public void execute(Graph hgraph) {
        isCanceled = false;

        Column column = initializeAttributeColunms(hgraph.getModel());

        hgraph.readLock();

        HashMap<Node, Integer> indicies = createIndiciesMap(hgraph);

        pageranks = calculatePagerank(hgraph, indicies, isDirected, useEdgeWeight, epsilon, probability);

        saveCalculatedValues(hgraph, column, indicies, pageranks);

        hgraph.readUnlockAll();
    }

    private Column initializeAttributeColunms(GraphModel graphModel) {
        Table nodeTable = graphModel.getNodeTable();
        Column pagerankCol = nodeTable.getColumn(PAGERANK);

        if (pagerankCol == null) {
            pagerankCol = nodeTable.addColumn(PAGERANK, "PageRank", Double.class, new Double(0));
        }

        return pagerankCol;
    }

    private void saveCalculatedValues(Graph hgraph, Column attributeColumn, HashMap<Node, Integer> indicies,
            double[] nodePagrank) {
        for (Node s : hgraph.getNodes()) {
            int s_index = indicies.get(s);

            s.setAttribute(attributeColumn, nodePagrank[s_index]);
        }
    }

    private void setInitialValues(Graph hgraph, double[] pagerankValues, double[] weights, boolean directed, boolean useWeights) {
        int N = hgraph.getNodeCount();
        int index = 0;
        for (Node s : hgraph.getNodes()) {
            pagerankValues[index] = 1.0f / N;
            if (useWeights) {
                double sum = 0;
                EdgeIterable eIter;
                if (directed) {
                    eIter = ((DirectedGraph) hgraph).getOutEdges(s);
                } else {
                    eIter = ((UndirectedGraph) hgraph).getEdges(s);
                }
                for (Edge edge : eIter) {
                    sum += edge.getWeight();
                }
                weights[index] = sum;
            }
            index++;
        }
    }

    private double calculateR(Graph hgraph, double[] pagerankValues, HashMap<Node, Integer> indicies, boolean directed, double prob) {
        int N = hgraph.getNodeCount();
        double r = 0;
        for (Node s : hgraph.getNodes()) {
            int s_index = indicies.get(s);
            boolean out;
            if (directed) {
                out = ((DirectedGraph) hgraph).getOutDegree(s) > 0;
            } else {
                out = hgraph.getDegree(s) > 0;
            }

            if (out) {
                r += (1.0 - prob) * (pagerankValues[s_index] / N);
            } else {
                r += (pagerankValues[s_index] / N);
            }
            if (isCanceled) {
                hgraph.readUnlockAll();
                return r;
            }
        }
        return r;
    }

    private double updateValueForNode(Graph hgraph, Node s, double[] pagerankValues, double[] weights,
            HashMap<Node, Integer> indicies, boolean directed, boolean useWeights, double r, double prob) {
        double res = r;
        EdgeIterable eIter;
        if (directed) {
            eIter = ((DirectedGraph) hgraph).getInEdges(s);
        } else {
            eIter = hgraph.getEdges(s);
        }

        for (Edge edge : eIter) {
            Node neighbor = hgraph.getOpposite(s, edge);
            int neigh_index = indicies.get(neighbor);
            int normalize;
            if (directed) {
                normalize = ((DirectedGraph) hgraph).getOutDegree(neighbor);
            } else {
                normalize = hgraph.getDegree(neighbor);
            }
            if (useWeights) {
                double weight = edge.getWeight() / weights[neigh_index];
                res += prob * pagerankValues[neigh_index] * weight;
            } else {
                res += prob * (pagerankValues[neigh_index] / normalize);
            }
        }
        return res;
    }

    double[] calculatePagerank(Graph hgraph, HashMap<Node, Integer> indicies,
            boolean directed, boolean useWeights, double eps, double prob) {
        int N = hgraph.getNodeCount();
        double[] pagerankValues = new double[N];
        double[] temp = new double[N];

        Progress.start(progress);
        double[] weights = new double[N];

        setInitialValues(hgraph, pagerankValues, weights, directed, useWeights);

        while (true) {
            double r = calculateR(hgraph, pagerankValues, indicies, directed, prob);

            boolean done = true;
            for (Node s : hgraph.getNodes()) {
                int s_index = indicies.get(s);
                temp[s_index] = updateValueForNode(hgraph, s, pagerankValues, weights, indicies, directed, useWeights, r, prob);

                if ((temp[s_index] - pagerankValues[s_index]) / pagerankValues[s_index] >= eps) {
                    done = false;
                }

                if (isCanceled) {
                    hgraph.readUnlockAll();
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

    public HashMap<Node, Integer> createIndiciesMap(Graph hgraph) {
        HashMap<Node, Integer> newIndicies = new HashMap<>();
        int index = 0;
        for (Node s : hgraph.getNodes()) {
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
