/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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
package org.gephi.statistics.plugin.dynamic;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Interval;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Table;
import org.gephi.graph.api.TimeRepresentation;
import org.gephi.graph.api.types.IntervalDoubleMap;
import org.gephi.graph.api.types.TimestampDoubleMap;
import org.gephi.statistics.plugin.ChartUtils;
import org.gephi.statistics.plugin.ClusteringCoefficient;
import org.gephi.statistics.spi.DynamicStatistics;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.ProgressTicket;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class DynamicClusteringCoefficient implements DynamicStatistics, LongTask {

    public static final String DYNAMIC_AVG_CLUSTERING_COEFFICIENT = "dynamic_avg_clustering";
    public static final String DYNAMIC_CLUSTERING_COEFFICIENT = "dynamic_clustering";
    //Data
    private GraphModel graphModel;
    private double window;
    private double tick;
    private Interval bounds;
    private boolean isDirected;
    private boolean averageOnly;
    private boolean cancel = false;
    private ClusteringCoefficient clusteringCoefficientStat;
    //Cols
    private Column dynamicCoefficientColumn;
    //Average
    private Column dynamicAverageCoefficientColumn;
    private Map<Double, Double> averages;

    public DynamicClusteringCoefficient() {
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        if (graphController != null && graphController.getGraphModel() != null) {
            isDirected = graphController.getGraphModel().isDirected();
        }
    }

    @Override
    public void execute(GraphModel graphModel) {
        this.graphModel = graphModel;
        this.isDirected = graphModel.isDirected();
        this.averages = new HashMap<>();

        //Attributes cols
        if (!averageOnly) {
            TimeRepresentation tr = graphModel.getConfiguration().getTimeRepresentation();

            Table nodeTable = graphModel.getNodeTable();
            dynamicCoefficientColumn = nodeTable.getColumn(DYNAMIC_CLUSTERING_COEFFICIENT);
            if (dynamicCoefficientColumn == null) {
                dynamicCoefficientColumn = nodeTable.addColumn(DYNAMIC_CLUSTERING_COEFFICIENT, NbBundle.getMessage(DynamicClusteringCoefficient.class, "DynamicClusteringCoefficient.nodecolumn.ClusteringCoefficient"), tr.equals(TimeRepresentation.INTERVAL) ? IntervalDoubleMap.class : TimestampDoubleMap.class, null);
            }
        }
    }

    @Override
    public String getReport() {
        //Time series
        XYSeries dSeries = ChartUtils.createXYSeries(averages, "Clustering Coefficient Time Series");

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(dSeries);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Clustering Coefficient",
                "Time",
                "Average Clustering Coefficient",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                false,
                false);

        chart.removeLegend();
        ChartUtils.decorateChart(chart);
        ChartUtils.scaleChart(chart, dSeries, false);
        String coefficientImageFile = ChartUtils.renderChart(chart, "coefficient-ts.png");

        NumberFormat f = new DecimalFormat("#0.000000");

        String report = "<HTML> <BODY> <h1>Dynamic Clustering Coefficient Report </h1> "
                + "<hr>"
                + "<br> Bounds: from " + f.format(bounds.getLow()) + " to " + f.format(bounds.getHigh())
                + "<br> Window: " + window
                + "<br> Tick: " + tick
                + "<br><br><h2> Average clustering cloefficient over time: </h2>"
                + "<br /><br />" + coefficientImageFile;

        /*for (Interval<Double> average : averages) {
        report += average.toString(dynamicModel.getTimeFormat().equals(DynamicModel.TimeFormat.DOUBLE)) + "<br />";
        }*/
        report += "<br /><br /></BODY></HTML>";
        return report;
    }

    @Override
    public void loop(GraphView window, Interval interval) {
        Graph graph = null;
        if (isDirected) {
            graph = graphModel.getDirectedGraph(window);
        } else {
            graph = graphModel.getUndirectedGraph(window);
        }
        TimeRepresentation tr = graphModel.getConfiguration().getTimeRepresentation();

        graph.readLock();

        clusteringCoefficientStat = new ClusteringCoefficient();
        clusteringCoefficientStat.setDirected(isDirected);
        clusteringCoefficientStat.triangles(graph);

        //Columns
        if (!averageOnly) {
            double[] coefficients = clusteringCoefficientStat.getCoefficientReuslts();
            int i = 0;
            for (Node n : graph.getNodes()) {
                double coef = coefficients[i++];

                switch (tr) {
                    case INTERVAL:
                        n.setAttribute(dynamicCoefficientColumn, coef, new Interval(interval.getLow(), interval.getLow() + tick));
                        break;
                    case TIMESTAMP:
                        n.setAttribute(dynamicCoefficientColumn, coef, interval.getLow());
                        n.setAttribute(dynamicCoefficientColumn, coef, interval.getHigh());
                        break;
                }

                if (cancel) {
                    break;
                }
            }
        }

        graph.readUnlockAll();

        //Average
        double avg = clusteringCoefficientStat.getAverageClusteringCoefficient();
        graphModel.getGraphVisible().setAttribute(DYNAMIC_AVG_CLUSTERING_COEFFICIENT, avg, interval.getLow());
        graphModel.getGraphVisible().setAttribute(DYNAMIC_AVG_CLUSTERING_COEFFICIENT, avg, interval.getHigh());

        averages.put(interval.getLow(), avg);
        averages.put(interval.getHigh(), avg);
    }

    @Override
    public void end() {
        clusteringCoefficientStat = null;
    }

    @Override
    public void setBounds(Interval bounds) {
        this.bounds = bounds;
    }

    @Override
    public void setWindow(double window) {
        this.window = window;
    }

    @Override
    public void setTick(double tick) {
        this.tick = tick;
    }

    @Override
    public double getWindow() {
        return window;
    }

    @Override
    public double getTick() {
        return tick;
    }

    @Override
    public Interval getBounds() {
        return bounds;
    }

    public void setDirected(boolean isDirected) {
        this.isDirected = isDirected;
    }

    public boolean isDirected() {
        return isDirected;
    }

    public void setAverageOnly(boolean averageOnly) {
        this.averageOnly = averageOnly;
    }

    public boolean isAverageOnly() {
        return averageOnly;
    }

    @Override
    public boolean cancel() {
        cancel = true;
        return true;
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
    }
}
