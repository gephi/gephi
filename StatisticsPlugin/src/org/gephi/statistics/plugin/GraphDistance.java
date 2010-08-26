/*
Copyright 2008-2010 Gephi
Authors : Patick J. McSweeney <pjmcswee@syr.edu>
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
import java.util.HashMap;
import java.util.Hashtable;
import org.gephi.statistics.spi.Statistics;
import org.gephi.graph.api.*;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Stack;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeType;
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
 *
 * @author pjmcswee
 */
public class GraphDistance implements Statistics, LongTask {

    public static final String BETWEENNESS = "betweenesscentrality";
    public static final String CLOSENESS = "closnesscentrality";
    public static final String ECCENTRICITY = "eccentricity";
    /** */
    private double[] mBetweenness;
    /** */
    private double[] mCloseness;
    /** */
    private double[] mEccentricity;
    /** */
    private int mDiameter;
    private int mRadius;
    /** */
    private double mAvgDist;
    /** */
    private int mN;
    /** */
    private boolean mDirected;
    /** */
    private ProgressTicket mProgress;
    /** */
    private boolean mIsCanceled;
    private int mShortestPaths;
    private boolean mRelativeValues;

    /**
     * 
     * @return
     */
    public double getPathLength() {
        return mAvgDist;
    }

    /**
     * 
     * @return
     */
    public double getDiameter() {
        return mDiameter;
    }

    /**
     * 
     * @param graphModel
     */
    public void execute(Graph graph, AttributeModel attributeModel) {
        mIsCanceled = false;
        AttributeTable nodeTable = attributeModel.getNodeTable();
        AttributeColumn eccentricityCol = nodeTable.getColumn(ECCENTRICITY);
        AttributeColumn closenessCol = nodeTable.getColumn(CLOSENESS);
        AttributeColumn betweenessCol = nodeTable.getColumn(BETWEENNESS);
        if (eccentricityCol == null) {
            eccentricityCol = nodeTable.addColumn(ECCENTRICITY, "Eccentricity", AttributeType.DOUBLE, AttributeOrigin.COMPUTED, new Double(0));
        }
        if (closenessCol == null) {
            closenessCol = nodeTable.addColumn(CLOSENESS, "Closeness Centrality", AttributeType.DOUBLE, AttributeOrigin.COMPUTED, new Double(0));
        }
        if (betweenessCol == null) {
            betweenessCol = nodeTable.addColumn(BETWEENNESS, "Betweenness Centrality", AttributeType.DOUBLE, AttributeOrigin.COMPUTED, new Double(0));
        }

        graph.readLock();

        mN = graph.getNodeCount();

        mBetweenness = new double[mN];
        mEccentricity = new double[mN];
        mCloseness = new double[mN];
        mDiameter = 0;
        mAvgDist = 0;
        mShortestPaths = 0;
        mRadius = Integer.MAX_VALUE;
        HashMap<Node, Integer> indicies = new HashMap<Node, Integer>();
        int index = 0;
        for (Node s : graph.getNodes()) {
            indicies.put(s, index);
            index++;
        }

        Progress.start(mProgress, graph.getNodeCount());
        int count = 0;
        for (Node s : graph.getNodes()) {
            Stack<Node> S = new Stack<Node>();

            LinkedList<Node>[] P = new LinkedList[mN];
            double[] theta = new double[mN];
            int[] d = new int[mN];
            for (int j = 0; j < mN; j++) {
                P[j] = new LinkedList<Node>();
                theta[j] = 0;
                d[j] = -1;
            }

            int s_index = indicies.get(s);

            theta[s_index] = 1;
            d[s_index] = 0;

            LinkedList<Node> Q = new LinkedList<Node>();
            Q.addLast(s);
            while (!Q.isEmpty()) {
                Node v = Q.removeFirst();
                S.push(v);
                int v_index = indicies.get(v);

                EdgeIterable edgeIter = null;
                if (mDirected) {
                    edgeIter = ((DirectedGraph) graph).getOutEdges(v);
                } else {
                    edgeIter = graph.getEdges(v);
                }

                for (Edge edge : edgeIter) {
                    Node reachable = graph.getOpposite(v, edge);

                    int r_index = indicies.get(reachable);
                    if (d[r_index] < 0) {
                        Q.addLast(reachable);
                        d[r_index] = d[v_index] + 1;
                    }
                    if (d[r_index] == (d[v_index] + 1)) {
                        theta[r_index] = theta[r_index] + theta[v_index];
                        P[r_index].addLast(v);
                    }
                }
            }
            double reachable = 0;
            for (int i = 0; i < mN; i++) {
                if (d[i] > 0) {
                    mAvgDist += d[i];
                    mEccentricity[s_index] = (int) Math.max(mEccentricity[s_index], d[i]);
                    mCloseness[s_index] += d[i];
                    mDiameter = Math.max(mDiameter, d[i]);
                    reachable++;
                }
            }

            mRadius = (int) Math.min(mEccentricity[s_index], mRadius);

            if (reachable != 0) {
                mCloseness[s_index] /= reachable;
            }

            mShortestPaths += reachable;

            double[] delta = new double[mN];
            while (!S.empty()) {
                Node w = S.pop();
                int w_index = indicies.get(w);
                ListIterator<Node> iter1 = P[w_index].listIterator();
                while (iter1.hasNext()) {
                    Node u = iter1.next();
                    int u_index = indicies.get(u);
                    delta[u_index] += (theta[u_index] / theta[w_index]) * (1 + delta[w_index]);
                }
                if (w != s) {
                    mBetweenness[w_index] += delta[w_index];
                }
            }
            count++;
            if (mIsCanceled) {
                graph.readUnlockAll();
                return;
            }
            Progress.progress(mProgress, count);
        }

        mAvgDist /= this.mShortestPaths;//mN * (mN - 1.0f);

        for (Node s : graph.getNodes()) {
            AttributeRow row = (AttributeRow) s.getNodeData().getAttributes();
            int s_index = indicies.get(s);

            if (!mDirected) {
                mBetweenness[s_index] /= 2;
            }
            if (this.mRelativeValues) {
                mCloseness[s_index] = 1.0 / mCloseness[s_index];
                mBetweenness[s_index] /= ((mN - 1) * (mN - 2)) / 2;
            }
            row.setValue(eccentricityCol, mEccentricity[s_index]);
            row.setValue(closenessCol, mCloseness[s_index]);
            row.setValue(betweenessCol, mBetweenness[s_index]);
        }
        graph.readUnlock();
    }

    /**
     *
     * @param graphModel
     */
    public void execute(GraphModel graphModel, AttributeModel attributeModel) {
        Graph graph = null;
        if (mDirected) {
            graph = graphModel.getDirectedGraphVisible();
        } else {
            graph = graphModel.getUndirectedGraphVisible();
        }
        execute(graph, attributeModel);
    }

    /**
     * 
     * @param pRelative
     */
    public void setRelative(boolean pRelative) {
        this.mRelativeValues = pRelative;
    }

    /**
     *
     * @param pRelative
     */
    public boolean useRelative() {
        return this.mRelativeValues;
    }

    /**
     * 
     * @param pDirected
     */
    public void setDirected(boolean pDirected) {
        this.mDirected = pDirected;
    }

    public boolean isDirected() {
        return mDirected;
    }

    /**
     * 
     * @param pVals
     * @param pName
     * @param pX
     * @param pY
     * @return
     */
    private String createImageFile(TempDir tempDir, double[] pVals, String pName, String pX, String pY) throws IOException {
        XYSeries series = new XYSeries(pName);
        for (int i = 0; i < mN; i++) {
            series.add(i, pVals[i]);
        }
        XYSeriesCollection dataSet = new XYSeriesCollection();
        dataSet.addSeries(series);

        JFreeChart chart = ChartFactory.createXYLineChart(
                pName,
                pX,
                pY,
                dataSet,
                PlotOrientation.VERTICAL,
                true,
                false,
                false);
        XYPlot plot = (XYPlot) chart.getPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesLinesVisible(0, false);
        renderer.setSeriesShapesVisible(0, true);
        renderer.setSeriesShape(0, new java.awt.geom.Ellipse2D.Double(0, 0, 1, 1));
        plot.setBackgroundPaint(java.awt.Color.WHITE);
        plot.setDomainGridlinePaint(java.awt.Color.GRAY);
        plot.setRangeGridlinePaint(java.awt.Color.GRAY);
        plot.setRenderer(renderer);

        String imageFile = "";

        ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());
        String fileName = pY + ".png";
        File file1 = tempDir.createFile(fileName);
        imageFile = "<IMG SRC=\"file:" + file1.getAbsolutePath() + "\" " + "WIDTH=\"600\" HEIGHT=\"400\" BORDER=\"0\" USEMAP=\"#chart\"></IMG>";

        ChartUtilities.saveChartAsPNG(file1, chart, 600, 400, info);

        return imageFile;
    }

    /**
     *
     * @return
     */
    public String getReport() {
        String htmlIMG1 = "";
        String htmlIMG2 = "";
        String htmlIMG3 = "";
        try {
            TempDir tempDir = TempDirUtils.createTempDir();
            htmlIMG1 = createImageFile(tempDir, mBetweenness, "Betweenness Centrality", "Nodes", "Betweenness");
            htmlIMG2 = createImageFile(tempDir, mCloseness, "Closeness Centrality", "Nodes", "Closeness");
            htmlIMG3 = createImageFile(tempDir, mEccentricity, "Eccentricity", "Nodes", "Eccentricity");
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        String report = "<HTML> <BODY> <h1>Graph Distance  Report </h1> "
                + "<hr>"
                + "<br>"
                + "<h2> Parameters: </h2>"
                + "Network Interpretation:  " + (this.mDirected ? "directed" : "undirected") + "<br>"
                + "<br> <h2> Results: </h2>"
                + "Diameter: " + this.mDiameter + "<br>"
                + "Radius: " + this.mRadius + "<br>"
                + "Average Path length: " + this.mAvgDist + "<br>"
                + "Number of shortest paths: " + this.mShortestPaths + "<br>"
                + htmlIMG1 + "<br>"
                + htmlIMG2 + "<br>"
                + htmlIMG3
                + "</BODY></HTML>";

        return report;
    }

    /**
     * 
     * @return
     */
    public boolean cancel() {
        mIsCanceled = true;
        return true;
    }

    /**
     *
     * @param progressTicket
     */
    public void setProgressTicket(ProgressTicket progressTicket) {
        mProgress = progressTicket;
    }
}
