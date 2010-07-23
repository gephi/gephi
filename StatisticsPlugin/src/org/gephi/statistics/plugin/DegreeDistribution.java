/*
Copyright 2008 WebAtlas
Authors : Patrick J. McSweeney (pjmcswee@syr.edu)
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

/**
 * This class measures how closely the degree distribution of a
 * network follows a power-law scale.  An alpha value between 2 and 3
 * implies a power law.
 * @author pjmcswees
 */
public class DegreeDistribution implements Statistics, LongTask {

    /**  The combined In/Out-degree distribution. */
    private double[][] mCombinedDistribution;
    /** The In-degree distribution. */
    private double[][] mInDistribution;
    /** The out-degree distribution. */
    private double[][] mOutDistribution;
    /** Remember if the metric has been canceled.  */
    private boolean mIsCanceled;
    /** The progress meter for this metric. */
    private ProgressTicket mProgress;
    /**  Indicates if this network should be directed or undirected.*/
    private boolean mDirected;
    /** The powerlaw value for the combined in/out-degree of this network. */
    private double mCombinedAlpha;
    /** The powerlaw value for the in-degree of this network. */
    private double mInAlpha;
    /** The powerlaw value for the out-degree of this network. */
    private double mOutAlpha;
    /** The powerlaw value for the combined in/out-degree of this network. */
    private double mCombinedBeta;
    /** The powerlaw value for the in-degree of this network. */
    private double mInBeta;
    /** The powerlaw value for the out-degree of this network. */
    private double mOutBeta;
    private String mGraphRevision;

    /**
     * @param pDirected Indicates the metric's interpretation of this network. 
     */
    public void setDirected(boolean pDirected) {
        this.mDirected = pDirected;
    }

    public boolean isDirected() {
        return mDirected;
    }

    /**
     * @return The combined in/out-degree power law value for this network.
     */
    public double getCombinedPowerLaw() {
        return this.mCombinedAlpha;
    }

    /**
     * @return The combined in/out-degree power law value for this network.
     */
    public double getInPowerLaw() {
        return this.mInAlpha;
    }

    /**
     * @return The combined in/out-degree power law value for this network.
     */
    public double getOutPowerLaw() {
        return this.mOutAlpha;
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
        Graph graph;
        if (this.mDirected) {
            graph = graphModel.getDirectedGraphVisible();
        } else {
            graph = graphModel.getUndirectedGraphVisible();
        }
        execute(graph, attributeModel);
    }

    public void execute(Graph graph, AttributeModel attributeModel) {
        //Mark this as not yet canceled.
        this.mIsCanceled = false;

        graph.readLock();

        this.mGraphRevision = "(" + graph.getNodeVersion() + ", " + graph.getEdgeVersion() + ")";

        //Start 
        Progress.start(mProgress, graph.getNodeCount());


        //Consider the in and out degree of every node
        if (this.mDirected) {
            this.mInDistribution = new double[2][2 * graph.getNodeCount()];
            this.mOutDistribution = new double[2][2 * graph.getNodeCount()];
        } else {
            this.mCombinedDistribution = new double[2][2 * graph.getNodeCount()];
        }


        int nodeCount = 0;
        for (Node node : graph.getNodes()) {
            if (this.mDirected) {
                int inDegree = ((DirectedGraph) graph).getInDegree(node);
                int outDegree = ((DirectedGraph) graph).getOutDegree(node);
                this.mInDistribution[1][inDegree]++;
                this.mOutDistribution[1][outDegree]++;
                this.mInDistribution[0][inDegree] = inDegree;
                this.mOutDistribution[0][outDegree] = outDegree;
            } else {
                int combinedDegree = ((UndirectedGraph) graph).getDegree(node);
                this.mCombinedDistribution[1][combinedDegree]++;
                this.mCombinedDistribution[0][combinedDegree] = combinedDegree;
            }
            Progress.progress(mProgress, nodeCount);
            nodeCount++;
            if (this.mIsCanceled) {
                graph.readUnlockAll();
                return;
            }
        }

        graph.readUnlock();

        if (this.mDirected) {
            double[] inFit = new double[2];
            double[] outFit = new double[2];
            leastSquares(this.mInDistribution[1], inFit);
            leastSquares(this.mOutDistribution[1], outFit);
            this.mInAlpha = inFit[1];
            this.mInBeta = inFit[0];
            this.mOutAlpha = outFit[1];
            this.mOutBeta = outFit[0];
        } else {
            double[] fit = new double[2];
            leastSquares(this.mCombinedDistribution[1], fit);
            this.mCombinedAlpha = fit[1];
            this.mCombinedBeta = fit[0];
        }
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
    public void leastSquares(double[] dist, double[] res) {
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

            if (this.mIsCanceled) {
                return;
            }

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
     * @return Indicates that this metric accepts parameters.
     */
    public boolean isParamerizable() {
        return true;
    }

    /**
     * @return A String report based on the interpretation of the network.
     */
    public String getReport() {
        if (this.mDirected) {
            return getDirectedReport();
        } else {
            return getUndirectedReport();
        }
    }

    /**
     *
     * @return The directed version of the report.
     */
    private String getDirectedReport() {
        double inMax = 0;
        XYSeries inSeries2 = new XYSeries("Series 2");
        for (int i = 1; i < this.mInDistribution[1].length; i++) {
            if (this.mInDistribution[1][i] > 0) {
                inSeries2.add((Math.log(this.mInDistribution[0][i]) / Math.log(Math.E)), (Math.log(this.mInDistribution[1][i]) / Math.log(Math.E)));
                inMax = (float) Math.max((Math.log(this.mInDistribution[0][i]) / Math.log(Math.E)), inMax);
            }
        }
        double inA = this.mInAlpha;
        double inB = this.mInBeta;

        String inImageFile = "";
        String outImageFile = "";
        try {

            XYSeries inSeries1 = new XYSeries(this.mInAlpha + " ");
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
            for (int i = 1; i < this.mOutDistribution[1].length; i++) {
                if (this.mOutDistribution[1][i] > 0) {
                    outSeries2.add((Math.log(this.mOutDistribution[0][i]) / Math.log(Math.E)), (Math.log(this.mOutDistribution[1][i]) / Math.log(Math.E)));
                    outMax = (float) Math.max((Math.log(this.mOutDistribution[0][i]) / Math.log(Math.E)), outMax);
                }
            }
            double outA = this.mOutAlpha;
            double outB = this.mOutBeta;


            XYSeries outSeries1 = new XYSeries(this.mOutAlpha + " ");
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




        String report = new String("<HTML> <BODY> <h1>Degree Distribution Metric Report </h1> "
                + "<hr> <br> <h2>Network Revision Number:</h2>"
                + mGraphRevision
                + "<br>"
                + "<h2> Parameters: </h2>"
                + "Network Interpretation:  " + (this.mDirected ? "directed" : "undirected") + "<br>"
                + "<br> <h2> Results: </h2>"
                + "In-Degree Power Law: -" + this.mInAlpha + "\n <BR>"
                + inImageFile + "<br>Out-Degree Power Law: -" + this.mOutAlpha + "\n <BR>"
                + outImageFile + "</BODY> </HTML>");


        return report;
    }

    /**
     *
     * @return The undirected version of this report.
     */
    private String getUndirectedReport() {
        double max = 0;
        XYSeries series2 = new XYSeries("Series 2");
        for (int i = 1; i < this.mCombinedDistribution[1].length; i++) {
            if (this.mCombinedDistribution[1][i] > 0) {
                series2.add((Math.log(this.mCombinedDistribution[0][i]) / Math.log(Math.E)), (Math.log(this.mCombinedDistribution[1][i]) / Math.log(Math.E)));
                max = (float) Math.max((Math.log(this.mCombinedDistribution[0][i]) / Math.log(Math.E)), max);
            }
        }
        double a = this.mCombinedAlpha;
        double b = this.mCombinedBeta;


        XYSeries series1 = new XYSeries(this.mCombinedAlpha + " ");
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


        String report = new String("<HTML> <BODY> <h1>Degree Distribution Metric Report </h1> "
                + "<hr> <br> <h2>Network Revision Number:</h2>"
                + mGraphRevision
                + "<br>"
                + "<h2> Parameters: </h2>"
                + "Network Interpretation:  " + (this.mDirected ? "directed" : "undirected") + "<br>"
                + "<br> <h2> Results: </h2>"
                + "In-Degree Power Law: -" + this.mInAlpha + "\n <BR>"
                + " Power: -" + this.mCombinedAlpha + "\n <BR>" + imageFile + "</BODY> </HTML>");
        return report;
    }

    /**
     * @return Indicates that the metric canceled flag was set.
     */
    public boolean cancel() {
        this.mIsCanceled = true;
        return true;
    }

    /**
     * @param progressTicket Sets the progress meter for the metric.
     */
    public void setProgressTicket(ProgressTicket pProgressTicket) {
        this.mProgress = pProgressTicket;
    }
}
