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

import java.util.Hashtable;
import java.util.LinkedList;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.statistics.spi.Statistics;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;

/**
 *
 * @author pjmcswee
 */
public class ConnectedComponents implements Statistics, LongTask {

    private boolean mDirected;
    private ProgressTicket mProgress;
    private boolean mIsCanceled;
    private String mGraphRevision;
    private int mComponentCount;
    private int mStronglyCount;
    int count;

    public void execute(GraphModel graphModel, AttributeModel attributeModel) {
        count = 1;
       
        weaklyConnected(graphModel, attributeModel);
        if (mDirected) {
            top_tarjans(graphModel, attributeModel);
        }
    }

    public void weaklyConnected(GraphModel graphModel, AttributeModel attributeModel) {
        mIsCanceled = false;
        mComponentCount = 0;
        AttributeTable nodeTable = attributeModel.getNodeTable();
        AttributeColumn componentCol = nodeTable.getColumn("componentnumber");
        if (componentCol == null) {
            componentCol = nodeTable.addColumn("componentnumber", "Component ID", AttributeType.INT, AttributeOrigin.COMPUTED, new Integer(0));
        }

        Graph graph = graphModel.getUndirectedGraphVisible();
        

        graph.readLock();
        mGraphRevision = "(" + graph.getNodeVersion() + ", " + graph.getEdgeVersion() + ")";

        Hashtable<Node, Integer> indicies = new Hashtable<Node, Integer>();
        int index = 0;
        for (Node s : graph.getNodes()) {
            indicies.put(s, index);
            index++;
        }


        int N = graph.getNodeCount();

        //Keep track of which nodes have been seen
        int[] color = new int[N];

        Progress.start(mProgress, graph.getNodeCount());
        int seenCount = 0;
        while (seenCount < N) {
            //The search Q
            LinkedList<Node> Q = new LinkedList<Node>();
            //The component-list
            LinkedList<Node> component = new LinkedList<Node>();

            //Seed the seach Q
            for (Node first : graph.getNodes()) {
                if (color[indicies.get(first)] == 0) {
                    Q.add(first);

                    break;
                }
            }

            //While there are more nodes to search
            while (!Q.isEmpty()) {
                if (mIsCanceled) {
                    graph.readUnlock();
                    return;
                }
                //Get the next Node and add it to the component list
                Node u = Q.removeFirst();
                component.add(u);

                //Iterate over all of u's neighbors
                EdgeIterable edgeIter =  graph.getEdges(u);
                
                //For each neighbor
                for (Edge edge : edgeIter) {
                    Node reachable = graph.getOpposite(u, edge);
                    int id = indicies.get(reachable);
                    //If this neighbor is unvisited
                    if (color[id] == 0) {
                        color[id] = 1;
                        //Add it to the search Q
                        Q.addLast(reachable);
                        //Mark it as used 

                        Progress.progress(mProgress, seenCount);
                    }
                }
                color[indicies.get(u)] = 2;
                seenCount++;
            }
            for (Node s : component) {
                AttributeRow row = (AttributeRow) s.getNodeData().getAttributes();
                row.setValue(componentCol, mComponentCount);
            }
            mComponentCount++;
        }
        graph.readUnlock();
    }

    public void top_tarjans(GraphModel graphModel, AttributeModel attributeModel) {
        mIsCanceled = false;
        mStronglyCount = 0;
        AttributeTable nodeTable = attributeModel.getNodeTable();
        AttributeColumn componentCol = nodeTable.getColumn("strongcompnum");
        if (componentCol == null) {
            componentCol = nodeTable.addColumn("strongcompnum", "Strongly-Connected ID", AttributeType.INT, AttributeOrigin.COMPUTED, new Integer(0));
        }

        DirectedGraph graph = graphModel.getDirectedGraphVisible();
        graph.readLock();
        mGraphRevision = "(" + graph.getNodeVersion() + ", " + graph.getEdgeVersion() + ")";

        Hashtable<Node, Integer> indicies = new Hashtable<Node, Integer>();
        int v = 0;
        for (Node s : graph.getNodes()) {
            indicies.put(s, v);
            v++;
        }
        int N = graph.getNodeCount();
        int[] index = new int[N];
        int[] low_index = new int[N];

        while (true) {
            //The search Q
            LinkedList<Node> S = new LinkedList<Node>();
            //The component-list
            LinkedList<Node> component = new LinkedList<Node>();
            //Seed the seach Q
            Node first = null;
            for (Node u : graph.getNodes()) {
                if (index[indicies.get(u)] == 0) {
                    first = u;
                    break;
                }
            }
            if (first == null) {
                return;
            }
            tarjans(componentCol, S, graph, first, index, low_index, indicies);
        }
    }

    /**
     * 
     * @param col
     * @param S
     * @param graph
     * @param f
     * @param index
     * @param low_index
     * @param indicies
     */
    public void tarjans(AttributeColumn col, LinkedList<Node> S,  DirectedGraph graph, Node f, int[] index, int[] low_index, Hashtable<Node, Integer> indicies) {
        int id = indicies.get(f);
        index[id] = count;
        low_index[id] = count;
        count++;
        S.addFirst(f);
        EdgeIterable edgeIter = graph.getOutEdges(f);
        for (Edge e : edgeIter) {
            Node u = graph.getOpposite(f, e);
            int x = indicies.get(u);
            if (index[x] == 0) {
                tarjans(col, S, graph, u, index, low_index, indicies);
                low_index[id] = Math.min(low_index[x], low_index[id]);
            } else if (S.contains(u)) {
                low_index[id] = Math.min(low_index[id], index[x]);
            }
        }
        if (low_index[id] == index[id]) {
            Node v = null;
            while (v != f) {
                v = S.removeFirst();
                AttributeRow row = (AttributeRow) v.getNodeData().getAttributes();
                row.setValue(col, mStronglyCount);
            }
            mStronglyCount++;
        }
    }

    public int getConnectedComponentsCount() {
        return mComponentCount;
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
     * @return
     */
    public String getReport() {
        String report = new String("<HTML> <BODY> <h1>Connected Components Report </h1> "
                + "<hr> <br> <h2>Network Revision Number:</h2>"
                + mGraphRevision
                + "<br>"
                + "<h2> Parameters: </h2>"
                + "Network Interpretation:  " + (this.mDirected ? "directed" : "undirected") + "<br>"
                + "<br> <h2> Results: </h2>"
                + "Weakly Connected Components: " + mComponentCount + "<br>"

                + (mDirected?"Stronlgy Connected Components: " + this.mStronglyCount + "<br>":"")
                + "</BODY></HTML>");

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
