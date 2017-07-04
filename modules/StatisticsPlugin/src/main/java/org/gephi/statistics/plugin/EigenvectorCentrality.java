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
    /**
     *
     */
    private boolean isCanceled;
    private boolean isDirected;

    public EigenvectorCentrality() {
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        if (graphController != null && graphController.getGraphModel() != null) {
            isDirected = graphController.getGraphModel().isDirected();
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
     */
    @Override
    public void execute(GraphModel graphModel) {
        isCanceled = false;

        Graph graph;
        if (isDirected) {
            graph = graphModel.getDirectedGraphVisible();
        } else {
            graph = graphModel.getUndirectedGraphVisible();
        }
        execute(graph);
    }

    public void execute(Graph graph) {

        Column column = initializeAttributeColunms(graph.getModel());

        int N = graph.getNodeCount();
        graph.readLock();
        
        try {
            centralities = new double[N];

            Progress.start(progress, numRuns);

            HashMap<Integer, Node> indicies = new HashMap<>();
            HashMap<Node, Integer> invIndicies = new HashMap<>();
            fillIndiciesMaps(graph, centralities, indicies, invIndicies);

            sumChange = calculateEigenvectorCentrality(graph, centralities, indicies, invIndicies, isDirected, numRuns);

            saveCalculatedValues(graph, column, indicies, centralities);
        } finally {
            graph.readUnlock();
        }

        Progress.finish(progress);
    }

    private Column initializeAttributeColunms(GraphModel graphModel) {
        Table nodeTable = graphModel.getNodeTable();
        Column eigenCol = nodeTable.getColumn(EIGENVECTOR);
        if (eigenCol == null) {
            eigenCol = nodeTable.addColumn(EIGENVECTOR, "Eigenvector Centrality", Double.class, new Double(0));
        }
        return eigenCol;
    }

    private void saveCalculatedValues(Graph graph, Column attributeColumn, HashMap<Integer, Node> indicies,
            double[] eigCenrtalities) {

        int N = graph.getNodeCount();

        for (int i = 0; i < N; i++) {
            Node s = indicies.get(i);

            s.setAttribute(attributeColumn, eigCenrtalities[i]);
        }
    }

    public void fillIndiciesMaps(Graph graph, double[] eigCentralities, HashMap<Integer, Node> indicies, HashMap<Node, Integer> invIndicies) {
        if (indicies == null || invIndicies == null) {
            return;
        }

        int count = 0;
        for (Node u : graph.getNodes()) {
            indicies.put(count, u);
            invIndicies.put(u, count);
            eigCentralities[count] = 1;
            count++;
        }
    }

    private double computeMaxValueAndTempValues(Graph graph, HashMap<Integer, Node> indicies, HashMap<Node, Integer> invIndicies,
            double[] tempValues, double[] centralityValues, boolean directed) {

        double max = 0.;
        int N = graph.getNodeCount();

        for (int i = 0; i < N; i++) {
            Node u = indicies.get(i);
            EdgeIterable iter;
            if (directed) {
                iter = ((DirectedGraph) graph).getInEdges(u);
            } else {
                iter = graph.getEdges(u);
            }

            for (Edge e : iter) {
                Node v = graph.getOpposite(u, e);
                Integer id = invIndicies.get(v);
                tempValues[i] += centralityValues[id];
            }
            max = Math.max(max, tempValues[i]);
            if (isCanceled) {
                return max;
            }
        }

        return max;
    }

    private double updateValues(Graph graph, double[] tempValues, double[] centralityValues, double max) {
        double sumChanged = 0.;
        int N = graph.getNodeCount();

        for (int k = 0; k < N; k++) {
            if (max != 0) {
                sumChanged += Math.abs(centralityValues[k] - (tempValues[k] / max));
                centralityValues[k] = tempValues[k] / max;
            } else {
                centralityValues[k] = 0.0;
            }
            if (isCanceled) {
                return sumChanged;
            }
        }

        return sumChanged;
    }

    public double calculateEigenvectorCentrality(Graph graph, double[] eigCentralities,
            HashMap<Integer, Node> indicies, HashMap<Node, Integer> invIndicies,
            boolean directed, int numIterations) {

        int N = graph.getNodeCount();
        double sumChanged = 0.;
        double[] tmp = new double[N];

        for (int s = 0; s < numIterations; s++) {
            double max = computeMaxValueAndTempValues(graph, indicies, invIndicies, tmp, eigCentralities, directed);
            sumChanged = updateValues(graph, tmp, eigCentralities, max);
            if (isCanceled) {
                return sumChanged;
            }

            Progress.progress(progress);
        }

        return sumChanged;
    }

    /**
     *
     * @return
     */
    @Override
    public String getReport() {
        //distribution of values
        Map<Double, Integer> dist = new HashMap<>();
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
        chart.removeLegend();
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

    @Override
    public boolean cancel() {
        this.isCanceled = true;
        return true;
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progress = progressTicket;

    }
}
