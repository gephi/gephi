/*
Copyright 2008-2011 Gephi
Authors : Patick J. McSweeney <pjmcswee@syr.edu>, Sebastien Heymann <seb@gephi.org>
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
package org.gephi.statistics.plugin;

import java.util.HashMap;
import java.util.Map;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.HierarchicalDirectedGraph;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.HierarchicalUndirectedGraph;
import org.gephi.graph.api.Node;
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
 * Ref: Sergey Brin, Lawrence Page, The Anatomy of a Large-Scale Hypertextual Web Search Engine, 
 * in Proceedings of the seventh International Conference on the World Wide Web (WWW1998):107-117
 *
 * @author pjmcswee
 */
public class PageRank implements Statistics, LongTask {

    public static final String PAGERANK = "pageranks";
    /** */
    private ProgressTicket progress;
    /** */
    private boolean isCanceled;
    /** */
    private double epsilon = 0.001;
    /** */
    private double probability = 0.85;
    private boolean useEdgeWeight = false;
    /** */
    private double[] pageranks;
    /** */
    private boolean isDirected;

    public PageRank() {
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        if (graphController != null && graphController.getModel() != null) {
            isDirected = graphController.getModel().isDirected();
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

    public void execute(GraphModel graphModel, AttributeModel attributeModel) {
        HierarchicalGraph graph;
        if (isDirected) {
            graph = graphModel.getHierarchicalDirectedGraphVisible();
        } else {
            graph = graphModel.getHierarchicalUndirectedGraphVisible();
        }
        execute(graph, attributeModel);
    }

    public void execute(HierarchicalGraph hgraph, AttributeModel attributeModel) {
        isCanceled = false;

        hgraph.readLock();

        int N = hgraph.getNodeCount();
        pageranks = new double[N];
        double[] temp = new double[N];
        HashMap<Node, Integer> indicies = new HashMap<Node, Integer>();
        int index = 0;

        Progress.start(progress);
        double[] weights = null;
        if (useEdgeWeight) {
            weights = new double[N];
        }

        for (Node s : hgraph.getNodes()) {
            indicies.put(s, index);
            pageranks[index] = 1.0f / N;
            if (useEdgeWeight) {
                double sum = 0;
                EdgeIterable eIter;
                if (isDirected) {
                    eIter = ((HierarchicalDirectedGraph) hgraph).getOutEdgesAndMetaOutEdges(s);
                } else {
                    eIter = ((HierarchicalUndirectedGraph) hgraph).getEdgesAndMetaEdges(s);
                }
                for (Edge edge : eIter) {
                    sum += edge.getWeight();
                }
                weights[index] = sum;
            }
            index++;
        }

        while (true) {
            double r = 0;
            for (Node s : hgraph.getNodes()) {
                int s_index = indicies.get(s);
                boolean out;
                if (isDirected) {
                    out = ((HierarchicalDirectedGraph) hgraph).getTotalOutDegree(s) > 0;
                } else {
                    out = hgraph.getTotalDegree(s) > 0;
                }

                if (out) {
                    r += (1.0 - probability) * (pageranks[s_index] / N);
                } else {
                    r += (pageranks[s_index] / N);
                }
                if (isCanceled) {
                    hgraph.readUnlockAll();
                    return;
                }
            }

            boolean done = true;
            for (Node s : hgraph.getNodes()) {
                int s_index = indicies.get(s);
                temp[s_index] = r;

                EdgeIterable eIter;
                if (isDirected) {
                    eIter = ((HierarchicalDirectedGraph) hgraph).getInEdgesAndMetaInEdges(s);
                } else {
                    eIter = ((HierarchicalUndirectedGraph) hgraph).getEdgesAndMetaEdges(s);
                }

                for (Edge edge : eIter) {
                    Node neighbor = hgraph.getOpposite(s, edge);
                    int neigh_index = indicies.get(neighbor);
                    int normalize;
                    if (isDirected) {
                        normalize = ((HierarchicalDirectedGraph) hgraph).getTotalOutDegree(neighbor);
                    } else {
                        normalize = ((HierarchicalUndirectedGraph) hgraph).getTotalDegree(neighbor);
                    }
                    if (useEdgeWeight) {
                        double weight = edge.getWeight() / weights[neigh_index];
                        temp[s_index] += probability * pageranks[neigh_index] * weight;
                    } else {
                        temp[s_index] += probability * (pageranks[neigh_index] / normalize);
                    }

                }

                if ((temp[s_index] - pageranks[s_index]) / pageranks[s_index] >= epsilon) {
                    done = false;
                }

                if (isCanceled) {
                    hgraph.readUnlockAll();
                    return;
                }

            }
            pageranks = temp;
            temp = new double[N];
            if ((done) || (isCanceled)) {
                break;
            }

        }

        AttributeTable nodeTable = attributeModel.getNodeTable();
        AttributeColumn pangeRanksCol = nodeTable.getColumn(PAGERANK);
        if (pangeRanksCol == null) {
            pangeRanksCol = nodeTable.addColumn(PAGERANK, "PageRank", AttributeType.DOUBLE, AttributeOrigin.COMPUTED, new Double(0));
        }

        for (Node s : hgraph.getNodes()) {
            int s_index = indicies.get(s);
            AttributeRow row = (AttributeRow) s.getNodeData().getAttributes();
            row.setValue(pangeRanksCol, pageranks[s_index]);
        }

        hgraph.readUnlockAll();
    }

    /**
     *
     * @return
     */
    public String getReport() {
        //distribution of values
        Map<Double, Integer> dist = new HashMap<Double, Integer>();
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
    public boolean cancel() {
        isCanceled = true;
        return true;
    }

    /**
     *
     * @param progressTicket
     */
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
