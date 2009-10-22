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

import java.util.Arrays;
import java.util.Comparator;
import java.util.Hashtable;
import org.gephi.statistics.api.Statistics;
import org.gephi.graph.api.Node;
import java.util.LinkedList;
import org.gephi.data.attributes.api.AttributeClass;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.NodeIterable;
import org.gephi.statistics.ui.ClusteringCoefficientPanel;
import org.gephi.statistics.ui.api.StatisticsUI;
import org.gephi.utils.longtask.LongTask;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author pjmcswee
 */
class Renumbering implements Comparator<ArrayWrapper> {

    public int compare(ArrayWrapper o1, ArrayWrapper o2) {
        if (o1.getID() < o2.getID()) {
            return -1;
        } else {
            return 1;
        }
    }
}

/**
 * 
 * @author pjmcswee
 */
class ArrayWrapper implements Comparable {

    private ArrayWrapper[] mArray;
    private int mID;

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
    public ArrayWrapper[] getArray() {
        return mArray;
    }

    public void setArray(ArrayWrapper[] pArray) {
        mArray = pArray;
    }

    /**
     *
     * @param pArray
     */
    ArrayWrapper(int pID, ArrayWrapper[] pArray) {
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

        return mArray[pIndex].mID;
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
    /** Keeps track of progress made. */
    private ProgressTicket progress;
    private int[] mTriangles;
    private ArrayWrapper[] mNetwork;
    private int mK;
    private int N;

    /**
     * 
     * @return
     */
    public String toString() {

        return new String("Clustering Coefficient");
    }

    /**
     * 
     * @return
     */
    public String getName() {
        return NbBundle.getMessage(ClusteringCoefficient.class, "ClusteringCoefficent_name");
    }

    /**
     *
     * @param synchReader
     */
    public void execute(GraphController graphController) {
        isCanceled = false;

        DirectedGraph digraph = graphController.getModel().getDirectedGraphVisible();

        if (bruteForce) {
            bruteForce(graphController);
            return;
        } else {
            triangles(graphController);
            return;
        }
    }

    /**
     *
     * @param v
     * @return
     */
    public int closest_in_array(int v) {
        //int right = g->degrees[v]-1;
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
            return (right - 1);
        }
    }

    /**
     *
     * @param v - The specific node to count the triangles on.
     */
    public void newVertex(int v) {
        boolean[] A = new boolean[N];


        for (int i = mNetwork[v].length() - 1; (i >= 0) && (mNetwork[v].get(i) > v); i--) {
            int neighbor = mNetwork[v].get(i);
            A[neighbor] = true;
        }


        for (int i = mNetwork[v].length() - 1; i >= 0; i--) {

            int neighbor = mNetwork[v].get(i);

            for (int j = closest_in_array(neighbor); j >= 0; j--) {
                int next = mNetwork[neighbor].get(j);
                if (A[next]) {
                    mTriangles[next]++;
                    mTriangles[v]++;
                    mTriangles[neighbor]++;
                }
            }
        }
    }

    /**
     *
     * @param u
     * @param v
     */
    public void tr_link_nohigh(int u, int v) {
        int iu = 0, iv = 0, w;

        while ((iu < mNetwork[u].length()) && (iv < mNetwork[v].length())) {
            if (mNetwork[u].get(iu) < mNetwork[v].get(iv)) {
                iu++;
            } else if (mNetwork[u].get(iu) > mNetwork[v].get(iv)) {
                iv++;
            } else { /* neighbor in common */
                w = mNetwork[u].get(iu);
                if (w >= mK) {
                    mTriangles[w]++;
                }
                iu++;
                iv++;
            }
        }
    }

    /**
     * 
     * @param graphController
     */
    public void triangles(GraphController graphController) {


        Graph graph = graphController.getModel().getDirectedGraphVisible();
        int progressCount = 0;
        progress.start(7 * graph.getNodeCount());

        if (!directed) {
            graph = graphController.getModel().getUndirectedGraphVisible();
        }


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
            progress.progress(++progressCount);
        }

        index = 0;
        for (Node node : graph.getNodes()) {
            LinkedList<ArrayWrapper> neighbors = new LinkedList<ArrayWrapper>();
            int j = 0;

            for (Node neighbor : graph.getNeighbors(node)) {
                neighbors.add(mNetwork[indicies.get(neighbor)]);
                System.out.println(node.getId() + ": " + neighbor.getId());
                j++;
            }

            ArrayWrapper[] edges = neighbors.toArray(new ArrayWrapper[1]);

            mNetwork[index].setArray(edges);
            index++;
            progress.progress(++progressCount);
        }

        Arrays.sort(mNetwork);
        for (int j = 0; j < N; j++) {
            mNetwork[j].setID(j);
            progress.progress(++progressCount);

        }

        for (int j = 0; j < N; j++) {
            Arrays.sort(mNetwork[j].getArray(), new Renumbering());
            progress.progress(++progressCount);
        }


        mTriangles = new int[N];
        mK = (int) Math.sqrt(N);


        // ClusteringThread.init(network);
        for (int v = 0; v < mK && v < N; v++) {
            newVertex(v);
            progress.progress(++progressCount);

        }

        /* remaining links */
        for (int v = N - 1; (v >= 0) && (v >= mK); v--) {
            for (int i = closest_in_array(v); i >= 0; i--) {
                int u = mNetwork[v].get(i);
                if (u >= mK) {
                    tr_link_nohigh(u, v);
                }
            }
            progress.progress(++progressCount);
        }

        avgClusteringCoeff = 0;
        AttributeController ac = Lookup.getDefault().lookup(AttributeController.class);
        AttributeClass nodeClass = ac.getTemporaryAttributeManager().getNodeClass();
        AttributeColumn clusteringCol = nodeClass.addAttributeColumn("clustering", "Clustering Coefficient", AttributeType.DOUBLE, AttributeOrigin.COMPUTED, new Double(0));


        for (Node s : graph.getNodes()) {
            int v = indicies.get(s);
            if (mNetwork[v].length() > 1) {
                double cc = mTriangles[v] /
                        (mNetwork[v].length() * (mNetwork[v].length() - 1) * .5f);
                AttributeRow row = (AttributeRow) s.getNodeData().getAttributes();
                row.setValue(clusteringCol, cc);

                avgClusteringCoeff += cc;
            }
            progress.progress(++progressCount);

        }

        avgClusteringCoeff /= N;

    }

    /**
     * 
     * @param graphController
     */
    public void bruteForce(GraphController graphController) {
        //The atrributes computed by the statistics
        AttributeController ac = Lookup.getDefault().lookup(AttributeController.class);
        AttributeClass nodeClass = ac.getTemporaryAttributeManager().getNodeClass();
        AttributeColumn clusteringCol = nodeClass.addAttributeColumn("clustering", "Clustering Coefficient", AttributeType.DOUBLE, AttributeOrigin.COMPUTED, new Double(0));


        float totalCC = 0;
        Graph graph = null;
        if (!directed) {
            graph = graphController.getModel().getUndirectedGraphVisible();
        } else {
            graph = graphController.getModel().getDirectedGraphVisible();
        }

        progress.start(graph.getNodeCount());
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
            progress.progress(node_count);

        }
        avgClusteringCoeff = totalCC / graph.getNodeCount();

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
        return new String("Average Clustering Coefficient: " + avgClusteringCoeff);
    }

    /**
     * 
     * @param pDirected
     */
    public void setDirected(boolean pDirected) {
        directed = pDirected;
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
     * @param progressTicket
     */
    public void setProgressTicket(ProgressTicket progressTicket) {
        progress = progressTicket;
    }

    public StatisticsUI getUI() {
        return new ClusteringCoefficientPanel.ClusteringCoefficientUI();
    }
}
       
    
