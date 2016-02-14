/*
 Copyright 2008-2011 Gephi
 Authors : Sebastien Heymann <seb@gephi.org>
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
import java.util.HashMap;
import java.util.Map;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
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
import org.openide.util.NbBundle;

/**
 *
 * @author Sebastien Heymann
 */
public class WeightedDegree implements Statistics, LongTask {

    public static final String WDEGREE = "weighted degree";
    public static final String WINDEGREE = "weighted indegree";
    public static final String WOUTDEGREE = "weighted outdegree";
    private boolean isDirected; // only set inside this class
    private boolean isCanceled;
    private ProgressTicket progress;
    private double avgWDegree;
    private Map<Double, Integer> degreeDist;
    private Map<Double, Integer> inDegreeDist;
    private Map<Double, Integer> outDegreeDist;

    public double getAverageDegree() {
        return avgWDegree;
    }

    @Override
    public void execute(GraphModel graphModel) {
        Graph graph = graphModel.getGraphVisible();
        execute(graph);
    }

    public void execute(Graph graph) {
        isDirected = graph.isDirected();
        isCanceled = false;

        initializeDegreeDists();
        initializeAttributeColunms(graph.getModel());

        graph.readLock();

        avgWDegree = calculateAverageWeightedDegree(graph, isDirected, true);

        graph.readUnlockAll();
    }

    public double calculateAverageWeightedDegree(Graph graph, boolean isDirected, boolean updateAttributes) {
        double averageWeightedDegree = 0;

        DirectedGraph directedGraph = null;

        if (isDirected) {
            directedGraph = (DirectedGraph) graph;
        }

        Progress.start(progress, graph.getNodeCount());

        for (Node n : graph.getNodes()) {
            double totalWeight = 0;
            if (isDirected) {
                double totalInWeight = 0;
                double totalOutWeight = 0;
                for (Edge e : directedGraph.getEdges(n)) {
                    if (e.getSource().equals(n)) {
                        totalOutWeight += e.getWeight();
                    }
                    if (e.getTarget().equals(n)) {
                        totalInWeight += e.getWeight();
                    }
                }
                totalWeight = totalInWeight + totalOutWeight;

                n.setAttribute(WINDEGREE, totalInWeight);
                n.setAttribute(WOUTDEGREE, totalOutWeight);
                n.setAttribute(WDEGREE, totalWeight);

                updateDegreeDists(totalInWeight, totalOutWeight, totalWeight);
            } else {
                for (Edge e : graph.getEdges(n)) {
                    totalWeight += e.getWeight();
                }
                n.setAttribute(WDEGREE, totalWeight);
                updateDegreeDists(totalWeight);
            }

            averageWeightedDegree += totalWeight;

            if (isCanceled) {
                break;
            }
            Progress.progress(progress);
        }

        averageWeightedDegree /= (isDirected) ? 2 * graph.getNodeCount() : graph.getNodeCount();

        return averageWeightedDegree;

    }

    private void initializeDegreeDists() {
        degreeDist = new HashMap<>();
        inDegreeDist = new HashMap<>();
        outDegreeDist = new HashMap<>();
    }

    private void initializeAttributeColunms(GraphModel graphModel) {
        Table nodeTable = graphModel.getNodeTable();
        if (isDirected) {
            if (!nodeTable.hasColumn(WINDEGREE)) {
                nodeTable.addColumn(WINDEGREE, NbBundle.getMessage(WeightedDegree.class, "WeightedDegree.nodecolumn.InDegree"), Double.class, 0.0);
            }
            if (!nodeTable.hasColumn(WOUTDEGREE)) {
                nodeTable.addColumn(WOUTDEGREE, NbBundle.getMessage(WeightedDegree.class, "WeightedDegree.nodecolumn.OutDegree"), Double.class, 0.0);
            }
        }
        if (!nodeTable.hasColumn(WDEGREE)) {
            nodeTable.addColumn(WDEGREE, NbBundle.getMessage(WeightedDegree.class, "WeightedDegree.nodecolumn.Degree"), Double.class, 0.0);
        }
    }

    private void updateDegreeDists(double winDegree, double woutDegree, double wdegree) {
        if (!inDegreeDist.containsKey(winDegree)) {
            inDegreeDist.put(winDegree, 0);
        }
        inDegreeDist.put(winDegree, inDegreeDist.get(winDegree) + 1);
        if (!outDegreeDist.containsKey(woutDegree)) {
            outDegreeDist.put(woutDegree, 0);
        }
        outDegreeDist.put(woutDegree, outDegreeDist.get(woutDegree) + 1);
        if (!degreeDist.containsKey(wdegree)) {
            degreeDist.put(wdegree, 0);
        }
        degreeDist.put(wdegree, degreeDist.get(wdegree) + 1);
    }

    private void updateDegreeDists(double wdegree) {
        if (!degreeDist.containsKey(wdegree)) {
            degreeDist.put(wdegree, 0);
        }
        degreeDist.put(wdegree, degreeDist.get(wdegree) + 1);
    }

    @Override
    public String getReport() {
        String report = "";

        if (isDirected) {
            report = getDirectedReport();
        } else {
            //Distribution series
            XYSeries dSeries = ChartUtils.createXYSeries(degreeDist, "Degree Distribution");

            XYSeriesCollection dataset1 = new XYSeriesCollection();
            dataset1.addSeries(dSeries);

            JFreeChart chart1 = ChartFactory.createXYLineChart(
                    "Degree Distribution",
                    "Value",
                    "Count",
                    dataset1,
                    PlotOrientation.VERTICAL,
                    true,
                    false,
                    false);
            chart1.removeLegend();
            ChartUtils.decorateChart(chart1);
            ChartUtils.scaleChart(chart1, dSeries, false);
            String degreeImageFile = ChartUtils.renderChart(chart1, "w-degree-distribution.png");

            NumberFormat f = new DecimalFormat("#0.000");

            report = "<HTML> <BODY> <h1>Weighted Degree Report </h1> "
                    + "<hr>"
                    + "<br> <h2> Results: </h2>"
                    + "Average Weighted Degree: " + f.format(avgWDegree)
                    + "<br /><br />" + degreeImageFile
                    + "</BODY></HTML>";
        }
        return report;
    }

    public String getDirectedReport() {
        //Distribution series
        XYSeries dSeries = ChartUtils.createXYSeries(degreeDist, "Degree Distribution");
        XYSeries idSeries = ChartUtils.createXYSeries(inDegreeDist, "In-Degree Distribution");
        XYSeries odSeries = ChartUtils.createXYSeries(outDegreeDist, "Out-Degree Distribution");

        XYSeriesCollection dataset1 = new XYSeriesCollection();
        dataset1.addSeries(dSeries);

        XYSeriesCollection dataset2 = new XYSeriesCollection();
        dataset2.addSeries(idSeries);

        XYSeriesCollection dataset3 = new XYSeriesCollection();
        dataset3.addSeries(odSeries);

        JFreeChart chart1 = ChartFactory.createXYLineChart(
                "Degree Distribution",
                "Value",
                "Count",
                dataset1,
                PlotOrientation.VERTICAL,
                true,
                false,
                false);
        ChartUtils.decorateChart(chart1);
        ChartUtils.scaleChart(chart1, dSeries, false);
        String degreeImageFile = ChartUtils.renderChart(chart1, "w-degree-distribution.png");

        JFreeChart chart2 = ChartFactory.createXYLineChart(
                "In-Degree Distribution",
                "Value",
                "Count",
                dataset2,
                PlotOrientation.VERTICAL,
                true,
                false,
                false);
        ChartUtils.decorateChart(chart2);
        ChartUtils.scaleChart(chart2, dSeries, false);
        String indegreeImageFile = ChartUtils.renderChart(chart2, "indegree-distribution.png");

        JFreeChart chart3 = ChartFactory.createXYLineChart(
                "Out-Degree Distribution",
                "Value",
                "Count",
                dataset3,
                PlotOrientation.VERTICAL,
                true,
                false,
                false);
        ChartUtils.decorateChart(chart3);
        ChartUtils.scaleChart(chart3, dSeries, false);
        String outdegreeImageFile = ChartUtils.renderChart(chart3, "outdegree-distribution.png");

        NumberFormat f = new DecimalFormat("#0.000");

        String report = "<HTML> <BODY> <h1>Weighted Degree Report </h1> "
                + "<hr>"
                + "<br> <h2> Results: </h2>"
                + "Average Weighted Degree: " + f.format(avgWDegree)
                + "<br /><br />" + degreeImageFile
                + "<br /><br />" + indegreeImageFile
                + "<br /><br />" + outdegreeImageFile
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
