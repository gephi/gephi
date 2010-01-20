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
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.statistics.spi.Statistics;
import org.gephi.utils.longtask.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;

public class InOutDegree implements Statistics, LongTask {

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

        //Attributes cols
        AttributeTable nodeTable = attributeModel.getNodeTable();
        AttributeColumn inCol = nodeTable.addColumn("indegree", "In Degree", AttributeType.INT, AttributeOrigin.COMPUTED, 0);
        AttributeColumn outCol = nodeTable.addColumn("outdegree", "Out Degree", AttributeType.INT, AttributeOrigin.COMPUTED, 0);

        DirectedGraph graph = graphModel.getDirectedGraph();
        int i = 0;

        this.mGraphRevision = "(" + graph.getNodeVersion() + ", " + graph.getEdgeVersion() + ")";

        Progress.start(mProgress, graph.getNodeCount());

        for (Node n : graph.getNodes()) {
            AttributeRow row = (AttributeRow) n.getNodeData().getAttributes();
            row.setValue(inCol, graph.getInDegree(n));
            row.setValue(outCol, graph.getOutDegree(n));
            mAvgInDegree += graph.getInDegree(n);
            mAvgOutDegree += graph.getOutDegree(n);
            if (mIsCanceled) {
                break;
            }
            i++;
            mProgress.progress(i);
        }

        mAvgDegree += mAvgInDegree + mAvgOutDegree;
        mAvgInDegree /= graph.getNodeCount();
        mAvgOutDegree /= graph.getNodeCount();
        mAvgDegree /= graph.getNodeCount();
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
                + "Average In Degree: " + mAvgInDegree
                + "<br >Average out Degree: " + mAvgOutDegree
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
