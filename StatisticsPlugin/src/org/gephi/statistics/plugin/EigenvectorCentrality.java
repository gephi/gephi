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
import java.util.Hashtable;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.EdgeIterator;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.statistics.spi.Statistics;
import org.gephi.utils.TempDirUtils;
import org.gephi.utils.TempDirUtils.TempDir;
import org.gephi.utils.longtask.spi.LongTask;
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

/**
 *
 * @author pjmcswee
 */
public class EigenvectorCentrality implements Statistics, LongTask {

    private int mNumRuns = 100;
    private double[] mCentralities;
    private double mSumChange;
    private ProgressTicket mProgress;
    /** */
    private boolean mIsCanceled;
    private boolean mDirected;
    private String mGraphRevision;

    /**
     * 
     * @param pNumRuns
     */
    public void setNumRuns(int pNumRuns) {
        mNumRuns = pNumRuns;
    }

    /**
     * 
     * @return
     */
    public int getNumRuns() {
        return mNumRuns;
    }

    /**
     * 
     * @return
     */
    public boolean isDirected() {
        return mDirected;
    }

    /**
     * 
     * @param pDirected
     */
    public void setDirected(boolean pDirected) {
        mDirected = pDirected;
    }

    /**
     * 
     * @param graphModel
     * @param attributeModel
     */
    public void execute(GraphModel graphModel, AttributeModel attributeModel) {

        AttributeTable nodeTable = attributeModel.getNodeTable();
        AttributeColumn pangeRanksCol = nodeTable.getColumn("eigencentrality");
        if (pangeRanksCol == null) {
            pangeRanksCol = nodeTable.addColumn("eigencentrality", "Eigenvector Centrality", AttributeType.DOUBLE, AttributeOrigin.COMPUTED, new Double(0));
        }

        Graph graph = null;
        if (mDirected) {
            graph = graphModel.getDirectedGraph();
        } else {
            graph = graphModel.getUndirectedGraph();
        }
        this.mGraphRevision = "(" + graph.getNodeVersion() + ", " + graph.getEdgeVersion() + ")";
        int N = graph.getNodeCount();
        graph.readLock();

        double[] tmp = new double[N];
        mCentralities = new double[N];

        Hashtable<Integer, Node> indicies = new Hashtable<Integer, Node>();
        Hashtable<Node, Integer> invIndicies = new Hashtable<Node, Integer>();
        int count = 0;
        for (Node u : graph.getNodes()) {
            indicies.put(count, u);
            invIndicies.put(u, count);
            mCentralities[count] = 1;
            count++;
        }
        for (int s = 0; s < mNumRuns; s++) {
            double max = 0;
            for (int i = 0; i < N; i++) {
                Node u = indicies.get(i);
                EdgeIterable iter = null;
                if (mDirected) {
                    iter = ((DirectedGraph) graph).getInEdges(u);
                } else {
                    iter = graph.getEdges(u);
                }

                for (Edge e : iter) {
                    Node v = graph.getOpposite(u, e);
                    Integer id = invIndicies.get(v);
                    tmp[i] += mCentralities[id];
                }
                max = Math.max(max, tmp[i]);
                if (this.mIsCanceled) {
                    return;
                }
            }
            System.out.println(s + "\t" + max);
            mSumChange = 0;
            for (int k = 0; k < N; k++) {
                mSumChange += Math.abs(mCentralities[k] - (tmp[k] / max));
                mCentralities[k] = tmp[k] / max;
                //tmp[k] = 0;
                if (this.mIsCanceled) {
                    return;
                }
            }
            if (this.mIsCanceled) {
                return;
            }
        }

        for (int i = 0; i < N; i++) {
            Node s = indicies.get(i);
            AttributeRow row = (AttributeRow) s.getNodeData().getAttributes();
            row.setValue(pangeRanksCol, mCentralities[i]);
            if (this.mIsCanceled) {
                return;
            }
        }
        graph.readUnlock();
    }

    /**
     * 
     * @return
     */
    public String getReport() {

        double max = 0;
        XYSeries series = new XYSeries("Series 2");
        for (int i = 0; i < this.mCentralities.length; i++) {
            series.add(i, mCentralities[i]);
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Eigenvector Centralities",
                "Nodes",
                "Eigenvector Centrality",
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
            String fileName = "eigen.png";
            File file1 = tempDir.createFile(fileName);
            imageFile = "<IMG SRC=\"file:" + file1.getAbsolutePath() + "\" " + "WIDTH=\"600\" HEIGHT=\"400\" BORDER=\"0\" USEMAP=\"#chart\"></IMG>";
            ChartUtilities.saveChartAsPNG(file1, chart, 600, 400, info);
        } catch (IOException e) {
            System.out.println(e.toString());
        }
        String report = new String("<HTML> <BODY> <h1>PageRank Report </h1> "
                + "<hr> <br> <h2>Network Revision Number:</h2>"
                + mGraphRevision
                + "<h2> Parameters: </h2>"
                + "Network Interpretation:  " + (this.mDirected ? "directed" : "undirected") + "<br>"
                + "Number of iterations: " + this.mNumRuns + "<br>"
                + "Sum change: " + this.mSumChange
                + "<br> <h2> Results: </h2>"
                + imageFile
                + "</BODY></HTML>");

        return report;

    }

    public boolean cancel() {
        this.mIsCanceled = true;
        return true;
    }

    public void setProgressTicket(ProgressTicket progressTicket) {
        this.mProgress = progressTicket;

    }
}
