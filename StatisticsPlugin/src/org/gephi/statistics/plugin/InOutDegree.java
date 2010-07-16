/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
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

import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.statistics.spi.Statistics;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;

public class InOutDegree implements Statistics, LongTask {

    public static final String INDEGREE = "indegree";
    public static final String OUTDEGREE = "outdegree";
    public static final String DEGREE = "degree";
    /** The Average Node In-Degree. */
    private double mAvgInDegree;
    /** The Average Node Out-Degree. */
    private double mAvgOutDegree;
    /** Remembers if the Cancel function has been called. */
    private boolean mIsCanceled;
    /** Keep track of the work done. */
    private ProgressTicket mProgress;
    /**     */
    private double mAvgDegree;
    /**  */
    private String mGraphRevision;

    /**
     *
     * @return
     */
    public double getAverageDegree() {
        return mAvgDegree;
    }

    /**
     *
     * @param graphModel
     */
    public void execute(GraphModel graphModel, AttributeModel attributeModel) {
        mIsCanceled = false;
        mAvgInDegree = mAvgOutDegree = 0.0;

        //Attributes cols
        AttributeTable nodeTable = attributeModel.getNodeTable();
        AttributeColumn inCol = nodeTable.getColumn(INDEGREE);
        AttributeColumn outCol = nodeTable.getColumn(OUTDEGREE);
        AttributeColumn degCol = nodeTable.getColumn(DEGREE);
        if (graphModel.isDirected()) {

            if (inCol == null) {
                inCol = nodeTable.addColumn(INDEGREE, "In Degree", AttributeType.INT, AttributeOrigin.COMPUTED, 0);
            }
            if (outCol == null) {
                outCol = nodeTable.addColumn(OUTDEGREE, "Out Degree", AttributeType.INT, AttributeOrigin.COMPUTED, 0);
            }
        }
        if (degCol == null) {
            degCol = nodeTable.addColumn(DEGREE, "Degree", AttributeType.INT, AttributeOrigin.COMPUTED, 0);
        }

        Graph graph = graphModel.getGraphVisible();
        int i = 0;

        graph.readLock();

        this.mGraphRevision = "(" + graph.getNodeVersion() + ", " + graph.getEdgeVersion() + ")";

        Progress.start(mProgress, graph.getNodeCount());

        for (Node n : graph.getNodes()) {
            AttributeRow row = (AttributeRow) n.getNodeData().getAttributes();
            if (graph instanceof DirectedGraph) {
                DirectedGraph directedGraph = (DirectedGraph) graph;
                row.setValue(inCol, directedGraph.getInDegree(n));
                row.setValue(outCol, directedGraph.getOutDegree(n));
                mAvgInDegree += directedGraph.getInDegree(n);
                mAvgOutDegree += directedGraph.getOutDegree(n);
            }
            row.setValue(degCol, graph.getDegree(n));
            mAvgDegree += graph.getDegree(n);

            if (mIsCanceled) {
                break;
            }
            i++;
            Progress.progress(mProgress, i);
        }

        mAvgInDegree /= graph.getNodeCount();
        mAvgOutDegree /= graph.getNodeCount();
        mAvgDegree /= graph.getNodeCount();

        graph.readUnlockAll();
    }

    /**
     *
     * @return
     */
    public String getReport() {


        String report = new String("<HTML> <BODY> <h1>In-Out Degree Report </h1> "
                + "<hr> <br> <h2>Network Revision Number:</h2>"
                + mGraphRevision
                + "<br>"
                //+"<h2> Parameters: </h2>" +
                //"Network Interpretation:  "  +(this. ? "directed": "undirected") +"<br>"
                + "<br> <h2> Results: </h2>"
                + "Average Degree: " + mAvgDegree
                + "<br >Average In Degree: " + mAvgInDegree
                + "<br >Average Out Degree: " + mAvgOutDegree
                + "</BODY></HTML>");

        return report;
        //return new String("Average In Degree: " + mAvgInDegree + "<br> Average out Degree: " + mAvgOutDegree);
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
