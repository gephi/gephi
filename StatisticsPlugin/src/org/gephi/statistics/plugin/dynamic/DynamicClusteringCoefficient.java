/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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
import org.gephi.data.attributes.type.Interval;
import org.gephi.dynamic.api.DynamicController;
import org.gephi.dynamic.api.DynamicModel;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;
import org.gephi.statistics.plugin.ChartUtils;
import org.gephi.statistics.plugin.ClusteringCoefficient;
import org.gephi.statistics.spi.DynamicStatistics;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
public class DynamicClusteringCoefficient implements DynamicStatistics {

    public static final String DYNAMIC_CLUSTERING_COEFFICIENT = "dynamic_clustering";
    //Data
    private GraphModel graphModel;
    private DynamicModel dynamicModel;
    private double window;
    private double tick;
    private Interval bounds;
    private boolean isDirected;
    private boolean averageOnly;
    private ClusteringCoefficient clusteringCoefficientStat;
    //Cols
    private AttributeColumn dynamicCoefficientColumn;
    //Result
    //private List<Interval<Double>> averages;
    private Map<Double, Double> coefficientTs;

    public DynamicClusteringCoefficient() {
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        if (graphController != null && graphController.getModel() != null) {
            isDirected = graphController.getModel().isDirected();
        }
    }

    public void execute(GraphModel graphModel, AttributeModel attributeModel) {
        this.graphModel = graphModel;
        //this.averages = new ArrayList<Interval<Double>>();
        this.coefficientTs = new HashMap<Double, Double>();
        this.isDirected = graphModel.isDirected();
        this.dynamicModel = Lookup.getDefault().lookup(DynamicController.class).getModel(graphModel.getWorkspace());
        this.clusteringCoefficientStat = new ClusteringCoefficient();
        clusteringCoefficientStat.setDirected(isDirected);

        //Attributes cols
        if (!averageOnly) {
            AttributeTable nodeTable = attributeModel.getNodeTable();
            dynamicCoefficientColumn = nodeTable.getColumn(DYNAMIC_CLUSTERING_COEFFICIENT);
            if (dynamicCoefficientColumn == null) {
                dynamicCoefficientColumn = nodeTable.addColumn(DYNAMIC_CLUSTERING_COEFFICIENT, "Dynamic Clustering Coefficient", AttributeType.DYNAMIC_DOUBLE, AttributeOrigin.COMPUTED, new DynamicDouble());
            }
        }
    }

    public String getReport() {
        //Time series
        XYSeries dSeries = ChartUtils.createXYSeries(coefficientTs, "Clustering Coefficient Time Series");

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

        NumberFormat f = new DecimalFormat("#0.000");

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

    public void loop(GraphView window, Interval interval) {
        HierarchicalGraph graph = null;
        if (isDirected) {
            graph = graphModel.getHierarchicalDirectedGraph(window);
        } else {
            graph = graphModel.getHierarchicalUndirectedGraph(window);
        }

        graph.readLock();

        clusteringCoefficientStat.triangles(graph);

        //Columns
        if (!averageOnly) {
            double[] coefficients = clusteringCoefficientStat.getCoefficientReuslts();
            int i = 0;
            for (Node n : graph.getNodes()) {
                double coef = coefficients[i++];
                Interval<Double> valInterval = new Interval<Double>(interval, coef);
                DynamicDouble val = (DynamicDouble) n.getAttributes().getValue(dynamicCoefficientColumn.getIndex());
                if (val == null) {
                    val = new DynamicDouble(valInterval);
                } else {
                    val = new DynamicDouble(val, valInterval);
                }
                n.getAttributes().setValue(dynamicCoefficientColumn.getIndex(), val);
            }
        }

        graph.readUnlockAll();

        //Average
        double avg = clusteringCoefficientStat.getAverageClusteringCoefficient();
        coefficientTs.put(interval.getHigh(), avg);
    }

    public void end() {
        clusteringCoefficientStat = null;
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
}
