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
package org.gephi.statistics.plugin;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Hashtable;
import org.gephi.statistics.spi.Statistics;
import org.gephi.graph.api.Node;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.NodeIterable;
import org.gephi.utils.longtask.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Lookup;

/**
 *
 * @author pjmcswee
 */
class Renumbering implements Comparator<EdgeWrapper> {

    public int compare(EdgeWrapper o1, EdgeWrapper o2) {
        if (o1.mWrapper.getID() < o2.mWrapper.getID()) {
            return -1;
        } else if (o1.mWrapper.getID() > o2.mWrapper.getID()) {
            return 1;
        } else {
            return 0;
        }
    }
}

/**
 * 
 * @author pjmcswee
 */
class EdgeWrapper {

    public int mCount;
    public ArrayWrapper mWrapper;

    public EdgeWrapper(int pCount, ArrayWrapper pWrapper) {
        mCount = pCount;
        mWrapper = pWrapper;
    }
}

/**
 * 
 * @author pjmcswee
 */
class ArrayWrapper implements Comparable {

    private EdgeWrapper[] mArray;
    private int mID;
    public Node node;

    /** Empty Constructor/ */
    ArrayWrapper() {
    }

    /**
     *
     * @return The ID of this array wrapper
     */
    public int getID() {
        return mID;
    }

    /**
     *
     * @return The adjacency array
     */
    public EdgeWrapper[] getArray() {
        return mArray;
    }

    public void setArray(EdgeWrapper[] pArray) {
        mArray = pArray;
    }

    /**
     *
     * @param pArray
     */
    ArrayWrapper(int pID, EdgeWrapper[] pArray) {
        mArray = pArray;
        mID = pID;
    }

    public void setID(int pID) {
        mID = pID;
    }

    /**
     *
     * @param pIndex
     * @return
     */
    public int get(int pIndex) {
        if (pIndex >= mArray.length) {
            return -1;
        }
        return mArray[pIndex].mWrapper.mID;
    }

    public int getCount(int pIndex) {
        if (pIndex >= mArray.length) {
            return -1;
        }
        return mArray[pIndex].mCount;
    }

    /**
     * 
     * @return
     */
    public int length() {
        return mArray.length;
    }

    /**
     * 
     * @param o
     * @return
     */
    public int compareTo(Object o) {
        ArrayWrapper aw = (ArrayWrapper) o;
        if (aw.length() < length()) {
            return -1;
        }
        if (aw.length() > length()) {
            return 1;
        }
        return 0;
    }
}

/**
 *
 * @author Patrick J. McSweeney
 */
public class ClusteringCoefficient implements Statistics, LongTask {

    /** The avergage Clustering Coefficient.*/
    private double avgClusteringCoeff;
    /** Indicates to use the brute force approach.*/
    private boolean bruteForce;
    /**Indicates should treat graph as undirected.*/
    private boolean directed;
    /** Indicates statistics should stop processing/*/
    private boolean isCanceled;
    /** Keeps track of Progress made. */
    private ProgressTicket progress;
    private int[] mTriangles;
    private ArrayWrapper[] mNetwork;
    private int mK;
    private int N;
    /** */
    private String mGraphRevision;

    /**
     *
     * @return
     */
    public double getAverageClusteringCoefficient() {
        return this.avgClusteringCoeff;
    }

    /**
     *
     * @param synchReader
     */
    public void execute(GraphModel graphModel, AttributeModel attributeModel) {
        isCanceled = false;
        Graph graph = graphModel.getUndirectedGraph();
        this.mGraphRevision = "(" + graph.getNodeVersion() + ", " + graph.getEdgeVersion() + ")";
        if (bruteForce) {
            bruteForce(graphModel, attributeModel);
            return;
        } else {
            triangles(graphModel, attributeModel);
            return;
        }
    }

    /**
     *
     * @param v
     * @return
     */
    public int closest_in_array(int v) {
        int right = mNetwork[v].length() - 1;

        /* optimization for extreme cases */
        if (right < 0) {
            return (-1);
        }
        if (mNetwork[v].get(0) >= v) {
            return (-1);
        }
        if (mNetwork[v].get(right) < v) {
            return (right);
        }
        if (mNetwork[v].get(right) == v) {
            return (right - 1);
        }

        int left = 0, mid;
        while (right > left) {
            mid = (left + right) / 2;
            if (v < mNetwork[v].get(mid)) {
                right = mid - 1;
            } else if (v > mNetwork[v].get(mid)) {
                left = mid + 1;
            } else {
                return (mid - 1);
            }
        }


        if (v > mNetwork[v].get(right)) {
            return (right);
        } else {

            return right - 1;
        }
    }

    /**
     *
     * @param v - The specific node to count the triangles on.
     */
    public void newVertex(int v) {
        int[] A = new int[N];

        for (int i = mNetwork[v].length() - 1; (i >= 0) && (mNetwork[v].get(i) > v); i--) {
            int neighbor = mNetwork[v].get(i);
            A[neighbor] = mNetwork[v].getCount(i);
        }
        for (int i = mNetwork[v].length() - 1; i >= 0; i--) {
            int neighbor = mNetwork[v].get(i);
            for (int j = closest_in_array(neighbor); j >= 0; j--) {
                int next = mNetwork[neighbor].get(j);
                if (A[next] > 0) {
                    mTriangles[next] += mNetwork[v].getCount(i);
                    mTriangles[v] += mNetwork[v].getCount(i);
                    mTriangles[neighbor] += A[next];
                }
            }
        }
    }

    /**
     *
     * @param u
     * @param v
     */
    public void tr_link_nohigh(int u, int v, int count) {
        int iu = 0, iv = 0, w;
        while ((iu < mNetwork[u].length()) && (iv < mNetwork[v].length())) {
            if (mNetwork[u].get(iu) < mNetwork[v].get(iv)) {
                iu++;
            } else if (mNetwork[u].get(iu) > mNetwork[v].get(iv)) {
                iv++;
            } else { /* neighbor in common */
                w = mNetwork[u].get(iu);
                if (w >= mK) {
                    mTriangles[w] += count;
                }
                iu++;
                iv++;
            }
        }
    }

    /**
     * 
     * @param graphModel
     */
    public void triangles(GraphModel graphModel, AttributeModel attributeModel) {
        Graph graph = graphModel.getUndirectedGraphVisible();
        int ProgressCount = 0;
        Progress.start(progress, 7 * graph.getNodeCount());

        if (!directed) {
            graph = graphModel.getUndirectedGraphVisible();
        } else {
            graph = graphModel.getDirectedGraphVisible();
        }

        graph.readLock();

        N = graph.getNodeCount();
        Node[] nodes = new Node[N];

        /** Create network for processing */
        mNetwork = new ArrayWrapper[N];

        /**  */
        Hashtable<Node, Integer> indicies = new Hashtable<Node, Integer>();
        int index = 0;
        for (Node s : graph.getNodes()) {
            indicies.put(s, index);
            mNetwork[index] = new ArrayWrapper();
            index++;
            Progress.progress(progress, ++ProgressCount);
        }

        index = 0;
        for (Node node : graph.getNodes()) {
            Hashtable<Node, EdgeWrapper> neighborTable = new Hashtable<Node, EdgeWrapper>();

            if (!directed) {
                for (Node neighbor : graph.getNeighbors(node)) {
                    neighborTable.put(neighbor, new EdgeWrapper(1, mNetwork[indicies.get(neighbor)]));
                }
            } else {
                for (Edge in : ((DirectedGraph) graph).getInEdges(node)) {
                    Node neighbor = in.getSource();
                    neighborTable.put(neighbor, new EdgeWrapper(1, mNetwork[indicies.get(neighbor)]));
                }

                for (Edge out : ((DirectedGraph) graph).getOutEdges(node)) {
                    Node neighbor = out.getTarget();
                    EdgeWrapper ew = neighborTable.get(neighbor);
                    if (ew == null) {
                        neighborTable.put(neighbor, new EdgeWrapper(1, mNetwork[indicies.get(neighbor)]));
                    } else {
                        ew.mCount++;
                    }
                }
            }

            EdgeWrapper[] edges = new EdgeWrapper[neighborTable.size()];
            int i = 0;
            for (EdgeWrapper e : neighborTable.values()) {
                edges[i] = e;
                i++;
            }
            mNetwork[index].node = node;
            mNetwork[index].setArray(edges);
            index++;
            Progress.progress(progress, ++ProgressCount);

            if (isCanceled) {
                graph.readUnlockAll();
                return;
            }
        }

        Arrays.sort(mNetwork);
        for (int j = 0; j < N; j++) {
            mNetwork[j].setID(j);
            Progress.progress(progress, ++ProgressCount);
        }

        for (int j = 0; j < N; j++) {
            Arrays.sort(mNetwork[j].getArray(), new Renumbering());
            Progress.progress(progress, ++ProgressCount);
        }

        mTriangles = new int[N];
        mK = (int) Math.sqrt(N);


        // ClusteringThread.init(network);
        for (int v = 0; v < mK && v < N; v++) {
            newVertex(v);
            Progress.progress(progress, ++ProgressCount);
        }

        /* remaining links */
        for (int v = N - 1; (v >= 0) && (v >= mK); v--) {
            for (int i = closest_in_array(v); i >= 0; i--) {
                int u = mNetwork[v].get(i);
                if (u >= mK) {
                    tr_link_nohigh(u, v, mNetwork[v].getCount(i));
                }
            }
            Progress.progress(progress, ++ProgressCount);

            if (isCanceled) {
                graph.readUnlockAll();
                return;
            }
        }

        avgClusteringCoeff = 0;
        AttributeTable nodeTable = attributeModel.getNodeTable();
        AttributeColumn clusteringCol = nodeTable.getColumn("clustering");
        if (clusteringCol == null) {
            clusteringCol = nodeTable.addColumn("clustering", "Clustering Coefficient", AttributeType.DOUBLE, AttributeOrigin.COMPUTED, new Double(0));
        }

        for (Node s : graph.getNodes()) {
            int v = indicies.get(s);
            if (mNetwork[v].length() > 1) {
                double cc = mTriangles[v];
                cc /= (mNetwork[v].length() * (mNetwork[v].length() - 1));
                if (!directed) {
                    cc *= 2.0f;
                }
                AttributeRow row = (AttributeRow) s.getNodeData().getAttributes();
                row.setValue(clusteringCol, cc);
                avgClusteringCoeff += cc;
            }
            Progress.progress(progress, ++ProgressCount);

            if (isCanceled) {
                graph.readUnlockAll();
                return;
            }
        }
        avgClusteringCoeff /= N;

        graph.readUnlock();
    }

    /**
     * 
     * @param graphModel
     */
    public void bruteForce(GraphModel graphModel, AttributeModel attributeModel) {
        //The atrributes computed by the statistics
        AttributeTable nodeTable = attributeModel.getNodeTable();
        AttributeColumn clusteringCol = nodeTable.getColumn("clustering");
        if (clusteringCol == null) {
            clusteringCol = nodeTable.addColumn("clustering", "Clustering Coefficient", AttributeType.DOUBLE, AttributeOrigin.COMPUTED, new Double(0));
        }

        float totalCC = 0;
        Graph graph = null;
        if (!directed) {
            graph = graphModel.getUndirectedGraphVisible();
        } else {
            graph = graphModel.getDirectedGraphVisible();
        }

        graph.readLock();

        Progress.start(progress, graph.getNodeCount());
        int node_count = 0;
        for (Node node : graph.getNodes()) {
            float nodeCC = 0;
            int neighborhood = 0;
            NodeIterable neighbors1 = graph.getNeighbors(node);
            for (Node neighbor1 : neighbors1) {
                neighborhood++;
                NodeIterable neighbors2 = graph.getNeighbors(node);
                for (Node neighbor2 : neighbors2) {

                    if (neighbor1 == neighbor2) {
                        continue;
                    }
                    if (directed) {
                        if (((DirectedGraph) graph).getEdge(neighbor1, neighbor2) != null) {
                            nodeCC++;
                        }
                        if (((DirectedGraph) graph).getEdge(neighbor2, neighbor1) != null) {
                            nodeCC++;
                        }
                    } else {
                        if (graph.isAdjacent(neighbor1, neighbor2)) {
                            nodeCC++;
                        }
                    }
                }
            }
            nodeCC /= 2.0;



            if (neighborhood > 1) {
                float cc = nodeCC / (.5f * neighborhood * (neighborhood - 1));
                if (directed) {
                    cc = nodeCC / (neighborhood * (neighborhood - 1));
                }

                AttributeRow row = (AttributeRow) node.getNodeData().getAttributes();
                row.setValue(clusteringCol, cc);

                totalCC += cc;
            }

            if (isCanceled) {
                break;
            }

            node_count++;
            Progress.progress(progress, node_count);

        }
        avgClusteringCoeff = totalCC / graph.getNodeCount();

        graph.readUnlockAll();
    }

    /**
     * 
     * @return
     */
    public boolean isParamerizable() {
        return true;
    }

    /**
     * 
     * @return
     */
    public String getReport() {

        return new String("<HTML> <BODY> <h1> Clustering Coefficient Metric Report </h1> "
                + "<hr> <br> <h2>Network Revision Number:</h2>"
                + mGraphRevision
                + "<br>" + "<h2> Parameters: </h2>"
                + "Network Interpretation:  " + (this.directed ? "directed" : "undirected") + "<br>"
                + "Algorithm applied: " + (this.bruteForce ? "Brute Force" : "Fast Triangles") + "<br> <h2> Results: </h2>"
                + "Average Clustering Coefficient: " + avgClusteringCoeff + "</BODY> </HTML>");
    }

    /**
     * 
     * @param pDirected
     */
    public void setDirected(boolean pDirected) {
        directed = pDirected;
    }

    public boolean isDirected() {
        return directed;
    }

    public boolean isBruteForce() {
        return bruteForce;
    }

    /**
     * 
     * @param brute
     */
    public void setBruteForce(boolean brute) {
        bruteForce = brute;
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
     * @param ProgressTicket
     */
    public void setProgressTicket(ProgressTicket ProgressTicket) {
        progress = ProgressTicket;
    }
}
