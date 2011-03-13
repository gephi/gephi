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
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeTable;
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
 *
 * @author pjmcswee
 */
public class EigenvectorCentrality implements Statistics, LongTask {

    public static final String EIGENVECTOR = "eigencentrality";
    private int numRuns = 100;
    private double[] centralities;
    private double sumChange;
    private ProgressTicket progress;
    /** */
    private boolean isCanceled;
    private boolean isDirected;

    public EigenvectorCentrality() {
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        if (graphController != null && graphController.getModel() != null) {
            isDirected = graphController.getModel().isDirected();
        }
    }

    public void setNumRuns(int numRuns) {
        this.numRuns = numRuns;
    }

    /**
     * 
     * @return
     */
    public int getNumRuns() {
        return numRuns;
    }

    /**
     * 
     * @return
     */
    public boolean isDirected() {
        return isDirected;
    }

    /**
     * 
     * @param isDirected
     */
    public void setDirected(boolean isDirected) {
        this.isDirected = isDirected;
    }

    /**
     *
     * @param graphModel
     * @param attributeModel
     */
    public void execute(GraphModel graphModel, AttributeModel attributeModel) {
        HierarchicalGraph graph = null;
        if (isDirected) {
            graph = graphModel.getHierarchicalDirectedGraphVisible();
        } else {
            graph = graphModel.getHierarchicalUndirectedGraphVisible();
        }
        execute(graph, attributeModel);
    }

    public void execute(HierarchicalGraph hgraph, AttributeModel attributeModel) {

        AttributeTable nodeTable = attributeModel.getNodeTable();
        AttributeColumn eigenCol = nodeTable.getColumn(EIGENVECTOR);
        if (eigenCol == null) {
            eigenCol = nodeTable.addColumn(EIGENVECTOR, "Eigenvector Centrality", AttributeType.DOUBLE, AttributeOrigin.COMPUTED, new Double(0));
        }

        int N = hgraph.getNodeCount();
        hgraph.readLock();

        double[] tmp = new double[N];
        centralities = new double[N];

        Progress.start(progress, numRuns);

        HashMap<Integer, Node> indicies = new HashMap<Integer, Node>();
        HashMap<Node, Integer> invIndicies = new HashMap<Node, Integer>();
        int count = 0;
        for (Node u : hgraph.getNodes()) {
            indicies.put(count, u);
            invIndicies.put(u, count);
            centralities[count] = 1;
            count++;
        }
        for (int s = 0; s < numRuns; s++) {
            double max = 0;
            for (int i = 0; i < N; i++) {
                Node u = indicies.get(i);
                EdgeIterable iter = null;
                if (isDirected) {
                    iter = ((HierarchicalDirectedGraph) hgraph).getInEdgesAndMetaInEdges(u);
                } else {
                    iter = ((HierarchicalUndirectedGraph) hgraph).getEdgesAndMetaEdges(u);
                }

                for (Edge e : iter) {
                    Node v = hgraph.getOpposite(u, e);
                    Integer id = invIndicies.get(v);
                    tmp[i] += centralities[id];
                }
                max = Math.max(max, tmp[i]);
                if (isCanceled) {
                    return;
                }
            }
            sumChange = 0;
            for (int k = 0; k < N; k++) {
                if (max != 0) {
                    sumChange += Math.abs(centralities[k] - (tmp[k] / max));
                    centralities[k] = tmp[k] / max;
                    //tmp[k] = 0;
                }
                if (isCanceled) {
                    return;
                }
            }
            if (isCanceled) {
                return;
            }

            Progress.progress(progress);
        }

        for (int i = 0; i < N; i++) {
            Node s = indicies.get(i);
            AttributeRow row = (AttributeRow) s.getNodeData().getAttributes();
            row.setValue(eigenCol, centralities[i]);
            if (isCanceled) {
                return;
            }
        }
        hgraph.readUnlock();

        Progress.finish(progress);
    }

    /**
     * 
     * @return
     */
    public String getReport() {
        //distribution of values
        Map<Double, Integer> dist = new HashMap<Double, Integer>();
        for (int i = 0; i < centralities.length; i++) {
            Double d = centralities[i];
            if (dist.containsKey(d)) {
                Integer v = dist.get(d);
                dist.put(d, v + 1);
            } else {
                dist.put(d, 1);
            }
        }

        //Distribution series
        XYSeries dSeries = ChartUtils.createXYSeries(dist, "Eigenvector Centralities");

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(dSeries);

        JFreeChart chart = ChartFactory.createScatterPlot(
                "Eigenvector Centrality Distribution",
                "Score",
                "Count",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                false,
                false);
        ChartUtils.decorateChart(chart);
        ChartUtils.scaleChart(chart, dSeries, true);
        String imageFile = ChartUtils.renderChart(chart, "eigenvector-centralities.png");
        
        String report = "<HTML> <BODY> <h1>Eigenvector Centrality Report</h1> "
                + "<hr>"
                + "<h2> Parameters: </h2>"
                + "Network Interpretation:  " + (isDirected ? "directed" : "undirected") + "<br>"
                + "Number of iterations: " + numRuns + "<br>"
                + "Sum change: " + sumChange
                + "<br> <h2> Results: </h2>"
                + imageFile
                + "</BODY></HTML>";

        return report;

    }

    public boolean cancel() {
        this.isCanceled = true;
        return true;
    }

    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progress = progressTicket;

    }
}
