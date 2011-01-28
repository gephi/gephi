/*
Copyright 2008-2010 Gephi
Authors : Patick J. McSweeney <pjmcswee@syr.edu>, Sebastien Heymann <seb@gephi.org>
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
package org.gephi.statistics.plugin;

import java.io.File;
import java.io.IOException;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.statistics.spi.Statistics;
import org.gephi.graph.api.*;
import org.gephi.utils.TempDirUtils;
import org.gephi.utils.TempDirUtils.TempDir;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
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
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * This class measures how closely the degree distribution of a
 * network follows a power-law scale.  An alpha value between 2 and 3
 * implies a power law.
 * @author pjmcswees
 */
public class DegreeDistribution implements Statistics, LongTask {

    /**  The combined In/Out-degree distribution. */
    private double[][] combinedDistribution;
    /** The In-degree distribution. */
    private double[][] inDistribution;
    /** The out-degree distribution. */
    private double[][] outDistribution;
    /** Remember if the metric has been canceled.  */
    private boolean isCanceled;
    /** The progress meter for this metric. */
    private ProgressTicket progress;
    /**  Indicates if this network should be directed or undirected.*/
    private boolean isDirected;
    /** The powerlaw value for the combined in/out-degree of this network. */
    private double combinedAlpha;
    /** The powerlaw value for the in-degree of this network. */
    private double inAlpha;
    /** The powerlaw value for the out-degree of this network. */
    private double outAlpha;
    /** The powerlaw value for the combined in/out-degree of this network. */
    private double combinedBeta;
    /** The powerlaw value for the in-degree of this network. */
    private double inBeta;
    /** The powerlaw value for the out-degree of this network. */
    private double outBeta;

    public DegreeDistribution() {
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        if (graphController != null && graphController.getModel() != null) {
            isDirected = graphController.getModel().isDirected();
        }
    }

    /**
     * @param isDirected Indicates the metric's interpretation of this network.
     */
    public void setDirected(boolean isDirected) {
        this.isDirected = isDirected;
    }

    public boolean isDirected() {
        return isDirected;
    }

    /**
     * @return The combined in/out-degree power law value for this network.
     */
    public double getCombinedPowerLaw() {
        return this.combinedAlpha;
    }

    /**
     * @return The combined in/out-degree power law value for this network.
     */
    public double getInPowerLaw() {
        return this.inAlpha;
    }

    /**
     * @return The combined in/out-degree power law value for this network.
     */
    public double getOutPowerLaw() {
        return this.outAlpha;
    }

    /**
     * Calculates the degree distribution for this network.
     * Either a combined degree distribution or separate
     * in-degree distribution and out-degree distribution
     * is calculated based on the mDirected variable.
     *
     * @param graphModel
     */
    public void execute(GraphModel graphModel, AttributeModel attributeModel) {
        //Get the graph from the graphController, based
        //on the mDirected variable.
        HierarchicalGraph graph;
        if (isDirected) {
            graph = graphModel.getHierarchicalDirectedGraphVisible();
        } else {
            graph = graphModel.getHierarchicalUndirectedGraphVisible();
        }
        execute(graph, attributeModel);
    }

    public void execute(HierarchicalGraph graph, AttributeModel attributeModel) {
        //Mark this as not yet canceled.
        isCanceled = false;

        graph.readLock();

        //Start 
        Progress.start(progress, graph.getNodeCount());


        //Consider the in and out degree of every node
        if (isDirected) {
            inDistribution = new double[2][2 * graph.getNodeCount()];
            outDistribution = new double[2][2 * graph.getNodeCount()];
        } else {
            combinedDistribution = new double[2][2 * graph.getNodeCount()];
        }


        int nodeCount = 0;
        for (Node node : graph.getNodes()) {
            if (isDirected) {
                int inDegree = ((HierarchicalDirectedGraph) graph).getTotalInDegree(node);
                int outDegree = ((HierarchicalDirectedGraph) graph).getTotalOutDegree(node);
                inDistribution[1][inDegree]++;
                outDistribution[1][outDegree]++;
                inDistribution[0][inDegree] = inDegree;
                outDistribution[0][outDegree] = outDegree;
            } else {
                int combinedDegree = ((HierarchicalUndirectedGraph) graph).getTotalDegree(node);
                combinedDistribution[1][combinedDegree]++;
                combinedDistribution[0][combinedDegree] = combinedDegree;
            }
            Progress.progress(progress, nodeCount);
            nodeCount++;
            if (isCanceled) {
                graph.readUnlockAll();
                return;
            }
        }

        graph.readUnlock();

        if (isDirected) {
            double[] inFit = new double[2];
            double[] outFit = new double[2];
            leastSquares(inDistribution[1], inFit);
            leastSquares(outDistribution[1], outFit);
            inAlpha = inFit[1];
            inBeta = inFit[0];
            outAlpha = outFit[1];
            outBeta = outFit[0];
        } else {
            double[] fit = new double[2];
            leastSquares(combinedDistribution[1], fit);
            combinedAlpha = fit[1];
            combinedBeta = fit[0];
        }
    }

    /**
     * Fits the logarithm distribution/degree to a straight line of the form:
     *	a + b *x which is then interpreted as a*x^y in the non-logarithmic scale
     *
     * @param dist The distribution of node degrees to fit to a logarithmized straight line
     *
     * @return An array of 2 doubles
     *					index 0: b
     *					index 1: a
     *
     *  For more see Wolfram Least Squares Fitting
     */
    public void leastSquares(double[] dist, double[] res) {
        //Vararibles to compute
        double SSxx = 0;
        double SSxy = 0;
        //double SSyy = 0;

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

            if (isCanceled) {
                return;
            }

        }
        avgX /= nonZero;
        avgY /= nonZero;

        //compute the variance of log(x)
        for (int i = 1; i < dist.length; i++) {
            if (dist[i] > 0) {
                SSxx += Math.pow(Math.log(i) - avgX, 2);
                //SSyy += Math.pow(Math.log(dist[i]) - avgY, 2);
                SSxy += (Math.log(i) - avgX) * (Math.log(dist[i]) - avgY);
            }
        }

        //Compute and return the results
        res[0] = SSxy / SSxx;
        res[1] = avgY - res[0] * avgX;
    }

    /**
     * @return A String report based on the interpretation of the network.
     */
    public String getReport() {
        return (isDirected) ? getDirectedReport() : getUndirectedReport();
    }

    /**
     *
     * @return The directed version of the report.
     */
    private String getDirectedReport() {
        double inMax = 0;
        XYSeries inSeries2 = new XYSeries("Series 2");
        for (int i = 1; i < inDistribution[1].length; i++) {
            if (inDistribution[1][i] > 0) {
                inSeries2.add((Math.log(inDistribution[0][i]) / Math.log(Math.E)), (Math.log(inDistribution[1][i]) / Math.log(Math.E)));
                inMax = (float) Math.max((Math.log(inDistribution[0][i]) / Math.log(Math.E)), inMax);
            }
        }
        double inA = inAlpha;
        double inB = inBeta;

        String inImageFile = "";
        String outImageFile = "";
        try {

            XYSeries inSeries1 = new XYSeries(inAlpha + " ");
            inSeries1.add(0, inA);
            inSeries1.add(inMax, inA + inB * inMax);

            XYSeriesCollection inDataset = new XYSeriesCollection();
            inDataset.addSeries(inSeries1);
            inDataset.addSeries(inSeries2);

            JFreeChart inChart = ChartFactory.createXYLineChart(
                    "In-Degree Distribution",
                    "In-Degree",
                    "Occurrence",
                    inDataset,
                    PlotOrientation.VERTICAL,
                    true,
                    false,
                    false);
            XYPlot inPlot = (XYPlot) inChart.getPlot();
            XYLineAndShapeRenderer inRenderer = new XYLineAndShapeRenderer();
            inRenderer.setSeriesLinesVisible(0, true);
            inRenderer.setSeriesShapesVisible(0, false);
            inRenderer.setSeriesLinesVisible(1, false);
            inRenderer.setSeriesShapesVisible(1, true);
            inRenderer.setSeriesShape(1, new java.awt.geom.Ellipse2D.Double(0, 0, 1, 1));
            inPlot.setBackgroundPaint(java.awt.Color.WHITE);
            inPlot.setDomainGridlinePaint(java.awt.Color.GRAY);
            inPlot.setRangeGridlinePaint(java.awt.Color.GRAY);

            inPlot.setRenderer(inRenderer);

            final ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());


            TempDir tempDir = TempDirUtils.createTempDir();
            final String fileName = "inDistribution.png";
            final File file1 = tempDir.createFile(fileName);
            inImageFile = "<IMG SRC=\"file:" + file1.getAbsolutePath() + "\" " + "WIDTH=\"600\" HEIGHT=\"400\" BORDER=\"0\" USEMAP=\"#chart\"></IMG>";
            ChartUtilities.saveChartAsPNG(file1, inChart, 600, 400, info);


            double outMax = 0;
            XYSeries outSeries2 = new XYSeries("Series 2");
            for (int i = 1; i < outDistribution[1].length; i++) {
                if (outDistribution[1][i] > 0) {
                    outSeries2.add((Math.log(outDistribution[0][i]) / Math.log(Math.E)), (Math.log(outDistribution[1][i]) / Math.log(Math.E)));
                    outMax = (float) Math.max((Math.log(outDistribution[0][i]) / Math.log(Math.E)), outMax);
                }
            }
            double outA = outAlpha;
            double outB = outBeta;


            XYSeries outSeries1 = new XYSeries(outAlpha + " ");
            outSeries1.add(0, outA);
            outSeries1.add(outMax, outA + outB * outMax);

            XYSeriesCollection outDataset = new XYSeriesCollection();
            outDataset.addSeries(outSeries1);
            outDataset.addSeries(outSeries2);

            JFreeChart outchart = ChartFactory.createXYLineChart(
                    "Out-Degree Distribution",
                    "Out-Degree",
                    "Occurrence",
                    outDataset,
                    PlotOrientation.VERTICAL,
                    true,
                    false,
                    false);
            XYPlot outPlot = (XYPlot) outchart.getPlot();
            XYLineAndShapeRenderer outRenderer = new XYLineAndShapeRenderer();
            outRenderer.setSeriesLinesVisible(0, true);
            outRenderer.setSeriesShapesVisible(0, false);
            outRenderer.setSeriesLinesVisible(1, false);
            outRenderer.setSeriesShapesVisible(1, true);
            outRenderer.setSeriesShape(1, new java.awt.geom.Ellipse2D.Double(0, 0, 1, 1));
            outPlot.setBackgroundPaint(java.awt.Color.WHITE);
            outPlot.setDomainGridlinePaint(java.awt.Color.GRAY);
            outPlot.setRangeGridlinePaint(java.awt.Color.GRAY);

            outPlot.setRenderer(outRenderer);

            final ChartRenderingInfo info2 = new ChartRenderingInfo(new StandardEntityCollection());
            final String fileName2 = "outDistribution.png";
            final File file2 = tempDir.createFile(fileName2);
            outImageFile = "<IMG SRC=\"file:" + file2.getAbsolutePath() + "\" " + "WIDTH=\"600\" HEIGHT=\"400\" BORDER=\"0\" USEMAP=\"#chart\"></IMG>";
            ChartUtilities.saveChartAsPNG(file2, outchart, 600, 400, info2);
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }




        String report = "<HTML> <BODY> <h1>Degree Distribution Metric Report </h1> "
                + "<hr>"
                + "<br>"
                + "<h2> Parameters: </h2>"
                + "Network Interpretation:  " + (isDirected ? "directed" : "undirected") + "<br>"
                + "<br> <h2> Results: </h2>"
                + "In-Degree Power Law: -" + inAlpha + "\n <BR>"
                + inImageFile + "<br>Out-Degree Power Law: -" + outAlpha + "\n <BR>"
                + outImageFile + "</BODY> </HTML>";


        return report;
    }

    /**
     *
     * @return The undirected version of this report.
     */
    private String getUndirectedReport() {
        double max = 0;
        XYSeries series2 = new XYSeries("Series 2");
        for (int i = 1; i < combinedDistribution[1].length; i++) {
            if (combinedDistribution[1][i] > 0) {
                series2.add((Math.log(combinedDistribution[0][i]) / Math.log(Math.E)), (Math.log(combinedDistribution[1][i]) / Math.log(Math.E)));
                max = (float) Math.max((Math.log(combinedDistribution[0][i]) / Math.log(Math.E)), max);
            }
        }
        double a = combinedAlpha;
        double b = combinedBeta;


        XYSeries series1 = new XYSeries(combinedAlpha + " ");
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


        String imageFile = "";
        try {
            final ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());
            TempDir tempDir = TempDirUtils.createTempDir();
            final String fileName = "distribution.png";
            final File file1 = tempDir.createFile(fileName);
            imageFile = "<IMG SRC=\"file:" + file1.getAbsolutePath() + "\" " + "WIDTH=\"600\" HEIGHT=\"400\" BORDER=\"0\" USEMAP=\"#chart\"></IMG>";
            ChartUtilities.saveChartAsPNG(file1, chart, 600, 400, info);

        } catch (IOException e) {
            System.out.println(e.toString());
        }


        String report = "<HTML> <BODY> <h1>Degree Distribution Metric Report </h1> "
                + "<hr>"
                + "<br>"
                + "<h2> Parameters: </h2>"
                + "Network Interpretation:  " + (isDirected ? "directed" : "undirected") + "<br>"
                + "<br> <h2> Results: </h2>"
                + "Degree Power Law: -" + combinedAlpha + "\n <BR>"
                + imageFile + "</BODY> </HTML>";
        return report;
    }

    /**
     * @return Indicates that the metric canceled flag was set.
     */
    public boolean cancel() {
        isCanceled = true;
        return true;
    }

    /**
     * @param progressTicket Sets the progress meter for the metric.
     */
    public void setProgressTicket(ProgressTicket pProgressTicket) {
        progress = pProgressTicket;
    }
}
