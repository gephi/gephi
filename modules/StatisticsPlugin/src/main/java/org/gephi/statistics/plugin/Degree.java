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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import org.gephi.graph.api.DirectedGraph;
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

public class Degree implements Statistics, LongTask {

    public static final String INDEGREE = "indegree";
    public static final String OUTDEGREE = "outdegree";
    public static final String DEGREE = "degree";
    public static final String AVERAGE_DEGREE = "avgdegree";
    private boolean isDirected; // only set inside this class
    /**
     * Remembers if the Cancel function has been called.
     */
    private boolean isCanceled;
    /**
     * Keep track of the work done.
     */
    private ProgressTicket progress;
    /**
     *
     */
    private double avgDegree;
    private Map<Integer, Integer> inDegreeDist;
    private Map<Integer, Integer> outDegreeDist;
    private Map<Integer, Integer> degreeDist;

    /**
     *
     * @return
     */
    public double getAverageDegree() {
        return avgDegree;
    }

    /**
     *
     * @param graphModel
     */
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

        avgDegree = calculateAverageDegree(graph, isDirected, true);

        graph.setAttribute(AVERAGE_DEGREE, avgDegree);

        graph.readUnlockAll();
    }

    protected int calculateInDegree(DirectedGraph directedGraph, Node n) {
        return directedGraph.getInDegree(n);
    }

    protected int calculateOutDegree(DirectedGraph directedGraph, Node n) {
        return directedGraph.getOutDegree(n);
    }

    protected int calculateDegree(Graph graph, Node n) {
        return graph.getDegree(n);
    }

    protected double calculateAverageDegree(Graph graph, boolean isDirected, boolean updateAttributes) {
        double averageDegree = 0;

        DirectedGraph directedGraph = null;

        if (isDirected) {
            directedGraph = (DirectedGraph) graph;
        }

        Progress.start(progress, graph.getNodeCount());

        for (Node n : graph.getNodes()) {
            int inDegree = 0;
            int outDegree = 0;
            int degree = 0;
            if (isDirected) {
                inDegree = calculateInDegree(directedGraph, n);
                outDegree = calculateOutDegree(directedGraph, n);
            }
            degree = calculateDegree(graph, n);

            if (updateAttributes) {
                n.setAttribute(DEGREE, degree);
                if (isDirected) {
                    n.setAttribute(INDEGREE, inDegree);
                    n.setAttribute(OUTDEGREE, outDegree);
                    updateDegreeDists(inDegree, outDegree, degree);
                } else {
                    updateDegreeDists(degree);

                }
            }

            averageDegree += degree;

            if (isCanceled) {
                break;
            }
            Progress.progress(progress);
        }

        averageDegree /= graph.getNodeCount();

        return averageDegree;
    }

    private void initializeAttributeColunms(GraphModel graphModel) {
        Table nodeTable = graphModel.getNodeTable();
        if (isDirected) {
            if (!nodeTable.hasColumn(INDEGREE)) {
                nodeTable.addColumn(INDEGREE, NbBundle.getMessage(Degree.class, "Degree.nodecolumn.InDegree"), Integer.class, 0);
            }
            if (!nodeTable.hasColumn(OUTDEGREE)) {
                nodeTable.addColumn(OUTDEGREE, NbBundle.getMessage(Degree.class, "Degree.nodecolumn.OutDegree"), Integer.class, 0);
            }
        }
        if (!nodeTable.hasColumn(DEGREE)) {
            nodeTable.addColumn(DEGREE, NbBundle.getMessage(Degree.class, "Degree.nodecolumn.Degree"), Integer.class, 0);
        }
    }

    private void initializeDegreeDists() {
        inDegreeDist = new HashMap<>();
        outDegreeDist = new HashMap<>();
        degreeDist = new HashMap<>();
    }

    private void updateDegreeDists(int inDegree, int outDegree, int degree) {
        if (!inDegreeDist.containsKey(inDegree)) {
            inDegreeDist.put(inDegree, 0);
        }
        inDegreeDist.put(inDegree, inDegreeDist.get(inDegree) + 1);
        if (!outDegreeDist.containsKey(outDegree)) {
            outDegreeDist.put(outDegree, 0);
        }
        outDegreeDist.put(outDegree, outDegreeDist.get(outDegree) + 1);
        if (!degreeDist.containsKey(degree)) {
            degreeDist.put(degree, 0);
        }
        degreeDist.put(degree, degreeDist.get(degree) + 1);
    }

    private void updateDegreeDists(int degree) {
        if (!degreeDist.containsKey(degree)) {
            degreeDist.put(degree, 0);
        }
        degreeDist.put(degree, degreeDist.get(degree) + 1);
    }

    /**
     *
     * @return
     */
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
            String degreeImageFile = ChartUtils.renderChart(chart1, "degree-distribution.png");

            NumberFormat f = new DecimalFormat("#0.000");

            report = "<HTML> <BODY> <h1>Degree Report </h1> "
                    + "<hr>"
                    + "<br> <h2> Results: </h2>"
                    + "Average Degree: " + f.format(avgDegree)
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
        chart1.removeLegend();
        ChartUtils.decorateChart(chart1);
        ChartUtils.scaleChart(chart1, dSeries, false);
        String degreeImageFile = ChartUtils.renderChart(chart1, "degree-distribution.png");

        JFreeChart chart2 = ChartFactory.createXYLineChart(
                "In-Degree Distribution",
                "Value",
                "Count",
                dataset2,
                PlotOrientation.VERTICAL,
                true,
                false,
                false);
        chart2.removeLegend();
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
        chart3.removeLegend();
        ChartUtils.decorateChart(chart3);
        ChartUtils.scaleChart(chart3, dSeries, false);
        String outdegreeImageFile = ChartUtils.renderChart(chart3, "outdegree-distribution.png");

        NumberFormat f = new DecimalFormat("#0.000");

        String report = "<HTML> <BODY> <h1>Degree Report </h1> "
                + "<hr>"
                + "<br> <h2> Results: </h2>"
                + "Average Degree: " + f.format(avgDegree)
                + "<br /><br />" + degreeImageFile
                + "<br /><br />" + indegreeImageFile
                + "<br /><br />" + outdegreeImageFile
                + "</BODY></HTML>";

        return report;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean cancel() {
        this.isCanceled = true;
        return true;
    }

    /**
     *
     * @param progressTicket
     */
    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progress = progressTicket;
    }
}
