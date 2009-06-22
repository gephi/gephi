/*
Copyright 2008 WebAtlas
Authors : Patrick J. McSweeney
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.statistics;

import java.io.File;
import java.io.IOException;
import javax.swing.JPanel;
import org.gephi.statistics.api.Statistics;
import org.gephi.graph.api.*;
import org.gephi.utils.longtask.LongTask;
import org.gephi.utils.progress.ProgressTicket;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.openide.util.NbBundle;

/**
 *
 * @author pjmcswees
 */
public class DegreeDistribution implements Statistics, LongTask {

    /** Calculate the degree distribution interms of in + out degree. */
    private boolean combined;
    /** Calcuate the degree distribution interms of the in and out degrees. */
    private boolean separated;
    private float[][] combinedDistribution;
    private boolean isCanceled;
    private ProgressTicket progress;
    private double[] fit;


    public String toString() {
        return new String("Degree Distribution");
    }

    /**
     * 
     * @return
     */
    public String getName() {
        return NbBundle.getMessage(ClusteringCoefficient.class, "DegreeDistribution_name");
    }

    /**
     *
     * @param synchReader
     */
    public void execute(GraphController graphController) {
        isCanceled = false;

        DirectedGraph digraph = graphController.getDirectedGraph();

        progress.start(digraph.getNodeCount());
        //Consider the in and out degree of every node
        combinedDistribution = new float[2][2 * digraph.getNodeCount()];

        int nodeCount = 0;
        for (Node node : digraph.getNodes()) {
            int combinedDegree = digraph.getInDegree(node) + digraph.getOutDegree(node);
            combinedDistribution[1][combinedDegree]++;
            combinedDistribution[0][combinedDegree] = combinedDegree;
            progress.progress(nodeCount);
            nodeCount++;
            if(isCanceled)
                return;
        }

        fit = new double[2];
        leastSquares(combinedDistribution[1],fit);
    }

    /**
     * Fits the logarithm distribution/degree to a straight line of the form:
     *	a + b *x which is then interrpreted as a*x^y in the non-logarithmic scale
     *
     * @param dist The distribution of node degrees to fit to a logarithmized straight line
     *
     * @return An array of 2 doubles
     *					index 0: b
     *					index 1: a
     *
     *  For more see Wolfram Least Squares Fitting
     */
    public void leastSquares(float[] dist, double[] res) {
        //Vararibles to compute
        double SSxx = 0;
        double SSxy = 0;
        double SSyy = 0;

        //Compute the average log(x) value when for positive (>0) values
        double avgX = 0;
        double avgY = 0;
        double nonZero = 0;
        for (int i = 1; i < dist.length; i++) {
            if (dist[i] > 0) {
                avgX += Math.log(i);
                avgY += Math.log(dist[i]);
                nonZero++;
            }

            if(isCanceled)
                return;

        }
        avgX /= nonZero;
        avgY /= nonZero;

        //compute the variance of log(x)
        for (int i = 1; i < dist.length; i++) {
            if (dist[i] > 0) {
                SSxx += Math.pow(Math.log(i) - avgX, 2);
                SSyy += Math.pow(Math.log(dist[i]) - avgY, 2);
                SSxy += (Math.log(i) - avgX) * (Math.log(dist[i]) - avgY);
            }
        }

        //Compute and return the results
        res[0] = SSxy / SSxx;
        res[1] = avgY - res[0] * avgX;
    }

    /**
     * 
     * @return
     */
    public boolean isParamerizable() {
        return false;
    }

    /**
     *
     * @return
     */
    public JPanel getPanel() {
        return null;
    }

    /**
     *
     * @return
     */
    public String getReport() {

        double max = 0;
        XYSeries series2 = new XYSeries("Series 2");
        for (int i = 1; i < combinedDistribution[1].length; i++) {
            if (combinedDistribution[1][i] > 0) {
                series2.add((Math.log(combinedDistribution[0][i]) / Math.log(Math.E)), (Math.log(combinedDistribution[1][i]) / Math.log(Math.E)));
                max = (float) Math.max((Math.log(combinedDistribution[0][i]) / Math.log(Math.E)), max);
            }
        }
        double a = fit[1];
        double b = fit[0];


        XYSeries series1 = new XYSeries(fit[1] + " ");
        series1.add(0, a);
        series1.add(max, a + b * max);

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series1);
        dataset.addSeries(series2);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Degree Distribution",
                "Degree",
                "Occurrence",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                false,
                false);
        XYPlot plot = (XYPlot) chart.getPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesLinesVisible(0, true);
        renderer.setSeriesShapesVisible(0, false);
        renderer.setSeriesLinesVisible(1, false);
        renderer.setSeriesShapesVisible(1, true);
        renderer.setSeriesShape(1, new java.awt.geom.Ellipse2D.Double(0, 0, 1, 1));
        plot.setBackgroundPaint(java.awt.Color.WHITE);
        plot.setDomainGridlinePaint(java.awt.Color.GRAY);
        plot.setRangeGridlinePaint(java.awt.Color.GRAY);

        plot.setRenderer(renderer);

        /*
        String imageFile = "<IMG SRC=\"test.png\" " + "WIDTH=\"600\" HEIGHT=\"400\" BORDER=\"0\" USEMAP=\"#chart\">";
        try {
            final ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());
            final File file1 = new File("test.png");
            ChartUtilities.saveChartAsPNG(file1, chart, 600, 400, info);
        } catch (IOException e) {
            System.out.println(e.toString());
        }
*/

        String imageFile = "";
        try {
            final ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());
            final File file1 = new File("distribution.png");
            String fullPath = file1.getAbsolutePath();

            fullPath = fullPath.replaceAll("\\\\", "\\\\\\\\");

            imageFile = "<IMG SRC=\"file:\\\\\\\\" + fullPath + "\" " + "WIDTH=\"600\" HEIGHT=\"400\" BORDER=\"0\" USEMAP=\"#chart\"></IMG>";

            File f2 = new File(fullPath);
            ChartUtilities.saveChartAsPNG(file1, chart, 600, 400, info);
        } catch (IOException e) {
            System.out.println(e.toString());
        }

        return imageFile;
    }

    /**
     * 
     * @return
     */
    public boolean cancel() {
        isCanceled = true;
        return true;
    }

    /**
     * 
     * @param progressTicket
     */
    public void setProgressTicket(ProgressTicket progressTicket) {
        progress = progressTicket;
    }
}
