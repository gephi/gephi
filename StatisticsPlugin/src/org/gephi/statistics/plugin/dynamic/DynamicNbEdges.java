/*
Copyright 2008-2011 Gephi
Authors : Sébastien Heymann <sebastien.heymann@gephi.org>
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
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.type.Interval;
import org.gephi.dynamic.api.DynamicController;
import org.gephi.dynamic.api.DynamicModel;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.statistics.plugin.ChartUtils;
import org.gephi.statistics.spi.DynamicStatistics;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.openide.util.Lookup;

/**
 *
 * @author Sébastien Heymann
 */
public class DynamicNbEdges implements DynamicStatistics {

    //Data
    private GraphModel graphModel;
    private DynamicModel dynamicModel;
    private double window;
    private double tick;
    private Interval bounds;
    //Result
    //private List<Interval<Integer>> counts;
    private Map<Double, Integer> countTs;

    public void execute(GraphModel graphModel, AttributeModel model) {
        this.graphModel = graphModel;
        //this.counts = new ArrayList<Interval<Integer>>();
        this.countTs = new HashMap<Double, Integer>();
        this.dynamicModel = Lookup.getDefault().lookup(DynamicController.class).getModel(graphModel.getWorkspace());
    }

    public String getReport() {
        //Time series
        XYSeries dSeries = ChartUtils.createXYSeries(countTs, "Nb Edges Time Series");

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(dSeries);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "# Edges Time Series",
                "Time",
                "# Edges",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                false,
                false);

        chart.removeLegend();
        ChartUtils.decorateChart(chart);
        ChartUtils.scaleChart(chart, dSeries, false);
        String imageFile = ChartUtils.renderChart(chart, "nb-edges-ts.png");

        NumberFormat f = new DecimalFormat("#0.000");

        String report = "<HTML> <BODY> <h1>Dynamic Number of Edges Report </h1> "
                + "<hr>"
                + "<br> Bounds: from " + f.format(bounds.getLow()) + " to " + f.format(bounds.getHigh())
                + "<br> Window: " + window
                + "<br> Tick: " + tick
                + "<br><br><h2> Number of edges over time: </h2>"
                + "<br /><br />" + imageFile;

        /*for (Interval<Integer> count : counts) {
        report += count.toString(dynamicModel.getTimeFormat().equals(DynamicModel.TimeFormat.DOUBLE)) + "<br />";
        }*/
        report += "<br /><br /></BODY></HTML>";
        return report;
    }

    public void loop(GraphView window, Interval interval) {
        HierarchicalGraph graph = graphModel.getHierarchicalGraph(window);

        int count = graph.getEdgeCount();
        //counts.add(new Interval<Integer>(interval, count));
        countTs.put(interval.getHigh(), count);
    }

    public void end() {
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
}
