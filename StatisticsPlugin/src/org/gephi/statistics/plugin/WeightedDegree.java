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
import java.util.Iterator;
import java.util.Map;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.HierarchicalDirectedGraph;
import org.gephi.graph.api.HierarchicalGraph;
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
    private Map<Float, Integer> degreeDist;
    private Map<Float, Integer> inDegreeDist;
    private Map<Float, Integer> outDegreeDist;

    public double getAverageDegree() {
        return avgWDegree;
    }

    public void execute(GraphModel graphModel, AttributeModel attributeModel) {
        HierarchicalGraph graph = graphModel.getHierarchicalGraphVisible();
        execute(graph, attributeModel);
    }

    public void execute(HierarchicalGraph graph, AttributeModel attributeModel) {
        isDirected = graph instanceof DirectedGraph;
        isCanceled = false;
        degreeDist = new HashMap<Float, Integer>();
        inDegreeDist = new HashMap<Float, Integer>();
        outDegreeDist = new HashMap<Float, Integer>();

        AttributeTable nodeTable = attributeModel.getNodeTable();
        AttributeColumn degCol = nodeTable.getColumn(WDEGREE);
        AttributeColumn inCol = nodeTable.getColumn(WINDEGREE);
        AttributeColumn outCol = nodeTable.getColumn(WOUTDEGREE);
        if (degCol == null) {
            degCol = nodeTable.addColumn(WDEGREE, "Weighted Degree", AttributeType.INT, AttributeOrigin.COMPUTED, 0);
        }
        if (isDirected) {
            if (inCol == null) {
                inCol = nodeTable.addColumn(WINDEGREE, "Weighted In-Degree", AttributeType.INT, AttributeOrigin.COMPUTED, 0);
            }
            if (outCol == null) {
                outCol = nodeTable.addColumn(WOUTDEGREE, "Weighted Out-Degree", AttributeType.INT, AttributeOrigin.COMPUTED, 0);
            }
        }

        graph.readLock();

        Progress.start(progress, graph.getNodeCount());
        int i = 0;

        for (Node n : graph.getNodes()) {
            AttributeRow row = (AttributeRow) n.getNodeData().getAttributes();
            float totalWeight = 0;
            if (isDirected) {
                HierarchicalDirectedGraph hdg = graph.getGraphModel().getHierarchicalDirectedGraph();
                float totalInWeight = 0;
                float totalOutWeight = 0;
                for (Iterator it = graph.getEdgesAndMetaEdges(n).iterator(); it.hasNext();) {
                    Edge e = (Edge) it.next();
                    if (e.getSource().equals(n)) {
                        totalOutWeight += e.getWeight();
                    }
                    if (e.getTarget().equals(n)) {
                        totalInWeight += e.getWeight();
                    }
                }
                totalWeight = totalInWeight + totalOutWeight;
                row.setValue(inCol, totalInWeight);
                row.setValue(outCol, totalOutWeight);
                if (!inDegreeDist.containsKey(totalInWeight)) {
                    inDegreeDist.put(totalInWeight, 0);
                }
                inDegreeDist.put(totalInWeight, inDegreeDist.get(totalInWeight) + 1);
                if (!outDegreeDist.containsKey(totalOutWeight)) {
                    outDegreeDist.put(totalOutWeight, 0);
                }
                outDegreeDist.put(totalOutWeight, outDegreeDist.get(totalOutWeight) + 1);
            } else {
                for (Iterator it = graph.getEdgesAndMetaEdges(n).iterator(); it.hasNext();) {
                    Edge e = (Edge) it.next();
                    totalWeight += e.getWeight();
                }
            }

            row.setValue(degCol, totalWeight);
            avgWDegree += totalWeight;

            if (!degreeDist.containsKey(totalWeight)) {
                degreeDist.put(totalWeight, 0);
            }
            degreeDist.put(totalWeight, degreeDist.get(totalWeight) + 1);

            if (isCanceled) {
                break;
            }
            i++;
            Progress.progress(progress, i);
        }

        avgWDegree /= (isDirected) ? 2 * graph.getNodeCount() : graph.getNodeCount();

        graph.readUnlockAll();
    }

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
                    + "<br /><br />"+degreeImageFile
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
                + "<br /><br />"+degreeImageFile
                + "<br /><br />"+indegreeImageFile
                + "<br /><br />"+outdegreeImageFile
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
