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
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.type.DynamicDouble;
import org.gephi.data.attributes.type.DynamicInteger;
import org.gephi.data.attributes.type.Interval;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.HierarchicalDirectedGraph;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;
import org.gephi.statistics.plugin.ChartUtils;
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
public class DynamicDegree implements DynamicStatistics, LongTask {

    public static final String DYNAMIC_AVGDEGREE = "dynamic_avgdegree";
    public static final String DYNAMIC_INDEGREE = "dynamic_indegree";
    public static final String DYNAMIC_OUTDEGREE = "dynamic_outdegree";
    public static final String DYNAMIC_DEGREE = "dynamic_degree";
    //Data
    private GraphModel graphModel;
    private double window;
    private double tick;
    private Interval bounds;
    private boolean isDirected;
    private boolean averageOnly;
    private boolean cancel = false;
    //Cols
    private AttributeColumn dynamicInDegreeColumn;
    private AttributeColumn dynamicOutDegreeColumn;
    private AttributeColumn dynamicDegreeColumn;
    //Average
    private AttributeColumn dynamicAverageDegreeColumn;
    private DynamicDouble averages;

    public DynamicDegree() {
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        if (graphController != null && graphController.getModel() != null) {
            isDirected = graphController.getModel().isDirected();
        }
    }

    public void execute(GraphModel graphModel, AttributeModel attributeModel) {
        this.graphModel = graphModel;
        this.isDirected = graphModel.isDirected();

        //Attributes cols
        if (!averageOnly) {
            AttributeTable nodeTable = attributeModel.getNodeTable();
            dynamicInDegreeColumn = nodeTable.getColumn(DYNAMIC_INDEGREE);
            dynamicOutDegreeColumn = nodeTable.getColumn(DYNAMIC_OUTDEGREE);
            dynamicDegreeColumn = nodeTable.getColumn(DYNAMIC_DEGREE);
            if (isDirected) {
                if (dynamicInDegreeColumn == null) {
                    dynamicInDegreeColumn = nodeTable.addColumn(DYNAMIC_INDEGREE, NbBundle.getMessage(DynamicDegree.class, "DynamicDegree.nodecolumn.InDegree"), AttributeType.DYNAMIC_INT, AttributeOrigin.COMPUTED, new DynamicInteger());
                }
                if (dynamicOutDegreeColumn == null) {
                    dynamicOutDegreeColumn = nodeTable.addColumn(DYNAMIC_OUTDEGREE, NbBundle.getMessage(DynamicDegree.class, "DynamicDegree.nodecolumn.OutDegree"), AttributeType.DYNAMIC_INT, AttributeOrigin.COMPUTED, new DynamicInteger());
                }
            }
            if (dynamicDegreeColumn == null) {
                dynamicDegreeColumn = nodeTable.addColumn(DYNAMIC_DEGREE, NbBundle.getMessage(DynamicDegree.class, "DynamicDegree.nodecolumn.Degree"), AttributeType.DYNAMIC_INT, AttributeOrigin.COMPUTED, new DynamicInteger());
            }
        }

        //Avg Column
        AttributeTable graphTable = attributeModel.getGraphTable();
        dynamicAverageDegreeColumn = graphTable.getColumn(DYNAMIC_AVGDEGREE);
        if (dynamicAverageDegreeColumn == null) {
            dynamicAverageDegreeColumn = graphTable.addColumn(DYNAMIC_AVGDEGREE, NbBundle.getMessage(DynamicDegree.class, "DynamicDegree.graphcolumn.AvgDegree"), AttributeType.DYNAMIC_DOUBLE, AttributeOrigin.COMPUTED, new DynamicDouble());
        }
    }

    public String getReport() {
        //Transform to Map
        Map<Double, Double> map = new HashMap<Double, Double>();
        for (Interval<Double> interval : averages.getIntervals()) {
            map.put(interval.getLow(), interval.getValue());
        }

        //Time series
        XYSeries dSeries = ChartUtils.createXYSeries(map, "Degree Time Series");

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(dSeries);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Degree Time Series",
                "Time",
                "Average Degree",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                false,
                false);

        chart.removeLegend();
        ChartUtils.decorateChart(chart);
        ChartUtils.scaleChart(chart, dSeries, false);
        String degreeImageFile = ChartUtils.renderChart(chart, "degree-ts.png");

        NumberFormat f = new DecimalFormat("#0.000000");

        String report = "<HTML> <BODY> <h1>Dynamic Degree Report </h1> "
                + "<hr>"
                + "<br> Bounds: from " + f.format(bounds.getLow()) + " to " + f.format(bounds.getHigh())
                + "<br> Window: " + window
                + "<br> Tick: " + tick
                + "<br><br><h2> Average degrees over time: </h2>"
                + "<br /><br />" + degreeImageFile;

        /*for (Interval<Double> averages : averages) {
        report += averages.toString(dynamicModel.getTimeFormat().equals(DynamicModel.TimeFormat.DOUBLE)) + "<br />";
        }*/
        report += "<br /><br /></BODY></HTML>";
        return report;
    }

    public void loop(GraphView window, Interval interval) {
        HierarchicalGraph graph = graphModel.getHierarchicalGraph(window);
        HierarchicalDirectedGraph directedGraph = null;
        if (isDirected) {
            directedGraph = graphModel.getHierarchicalDirectedGraph(window);
        }

        long sum = 0;
        for (Node n : graph.getNodes().toArray()) {
            int degree = graph.getTotalDegree(n);

            if (!averageOnly) {
                Interval<Integer> degreeInInterval = new Interval<Integer>(interval, degree);
                DynamicInteger val = (DynamicInteger) n.getAttributes().getValue(dynamicDegreeColumn.getIndex());
                if (val == null) {
                    val = new DynamicInteger(degreeInInterval);
                } else {
                    val = new DynamicInteger(val, degreeInInterval);
                }
                n.getAttributes().setValue(dynamicDegreeColumn.getIndex(), val);

                if (isDirected) {
                    int indegree = directedGraph.getTotalInDegree(n);
                    Interval<Integer> inDegreeInInterval = new Interval<Integer>(interval, indegree);
                    DynamicInteger inVal = (DynamicInteger) n.getAttributes().getValue(dynamicInDegreeColumn.getIndex());
                    if (inVal == null) {
                        inVal = new DynamicInteger(inDegreeInInterval);
                    } else {
                        inVal = new DynamicInteger(inVal, inDegreeInInterval);
                    }
                    n.getAttributes().setValue(dynamicInDegreeColumn.getIndex(), inVal);

                    int outdegree = directedGraph.getTotalOutDegree(n);
                    Interval<Integer> outDegreeInInterval = new Interval<Integer>(interval, outdegree);
                    DynamicInteger outVal = (DynamicInteger) n.getAttributes().getValue(dynamicOutDegreeColumn.getIndex());
                    if (outVal == null) {
                        outVal = new DynamicInteger(outDegreeInInterval);
                    } else {
                        outVal = new DynamicInteger(outVal, outDegreeInInterval);
                    }
                    n.getAttributes().setValue(dynamicOutDegreeColumn.getIndex(), outVal);
                }
            }
            sum += degree;
            if (cancel) {
                break;
            }
        }

        double avg = sum / (double) graph.getNodeCount();

        averages = new DynamicDouble(averages, new Interval<Double>(interval.getLow(), interval.getHigh(), false, true, avg));
    }

    public void end() {
        graphModel.getGraphVisible().getAttributes().setValue(dynamicAverageDegreeColumn.getIndex(), averages);
    }

    public void setBounds(Interval bounds) {
        this.bounds = bounds;
    }

    public void setWindow(double window) {
        this.window = window;
    }

    public void setTick(double tick) {
        this.tick = tick;
    }

    public double getWindow() {
        return window;
    }

    public double getTick() {
        return tick;
    }

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

    public boolean cancel() {
        cancel = true;
        return true;
    }

    public void setProgressTicket(ProgressTicket progressTicket) {
    }
}
