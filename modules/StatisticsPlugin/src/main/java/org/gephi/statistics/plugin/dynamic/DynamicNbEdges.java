/*
Copyright 2008-2011 Gephi
Authors : Sébastien Heymann <sebastien.heymann@gephi.org>
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
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Interval;
import org.gephi.statistics.plugin.ChartUtils;
import org.gephi.statistics.spi.DynamicStatistics;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 *
 * @author Sébastien Heymann
 */
public class DynamicNbEdges implements DynamicStatistics {

    public static final String NB_EDGES = "dynamic nbedges";
    //Data
    private GraphModel graphModel;
    private double window;
    private double tick;
    private Interval bounds;
    //Average
    private Map<Double, Integer> counts;

    @Override
    public void execute(GraphModel graphModel) {
        this.graphModel = graphModel;
        this.counts = new HashMap<Double, Integer>();
    }

    @Override
    public String getReport() {
        //Time series
        XYSeries dSeries = ChartUtils.createXYSeries(counts, "Nb Edges Time Series");

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

    @Override
    public void loop(GraphView window, Interval interval) {
        Graph graph = graphModel.getGraph(window);

        int count = graph.getEdgeCount();
        
        graph.setAttribute(NB_EDGES, count, interval.getLow());
        graph.setAttribute(NB_EDGES, count, interval.getHigh());

        counts.put(interval.getLow(), count);
        counts.put(interval.getHigh(), count);
    }

    @Override
    public void end() {
    }

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
}
