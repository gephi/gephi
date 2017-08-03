/*
 Copyright 2008-2011 Gephi
 Authors : Patick J. McSweeney <pjmcswee@syr.edu>, Sebastien Heymann <seb@gephi.org>
 Website : http://www.gephi.org

 This file is part of Gephi.

 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

 Copyright 2011 Gephi Consortium. All rights reserved.

 The contents of this file are subject to the terms of either the GNU
 General Public License Version 3 only ("GPL") or the Common
 Development and Distribution License("CDDL") (collectively, the
 "License"). You may not use this file except in compliance with the
 License. You can obtain a copy of the License at
 http://gephi.org/about/legal/license-notice/
 or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
 specific language governing permissions and limitations under the
 License.  When distributing the software, include this License Header
 Notice in each file and include the License files at
 /cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
 License Header, with the fields enclosed by brackets [] replaced by
 your own identifying information:
 "Portions Copyrighted [year] [name of copyright owner]"

 If you wish your version of this file to be governed by only the CDDL
 or only the GPL Version 3, indicate your decision by adding
 "[Contributor] elects to include this software in this distribution
 under the [CDDL or GPL Version 3] license." If you do not indicate a
 single choice of license, a recipient has the option to distribute
 your version of this file under either the CDDL, the GPL Version 3 or
 to extend the choice of license to its licensees as provided above.
 However, if you add GPL Version 3 code and therefore, elected the GPL
 Version 3 license, then the option applies only if the new code is
 made subject to such option by the copyright holder.

 Contributor(s):

 Portions Copyrighted 2011 Gephi Consortium.
 */
package org.gephi.statistics.plugin;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.graph.api.Table;
import org.gephi.statistics.spi.Statistics;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.openide.util.Lookup;

/**
 * Ref: Matthieu Latapy, Main-memory Triangle Computations for Very Large
 * (Sparse (Power-Law)) Graphs, in Theoretical Computer Science (TCS) 407 (1-3),
 * pages 458-473, 2008
 *
 * @author pjmcswee
 */
class Renumbering implements Comparator<EdgeWrapper> {

    @Override
    public int compare(EdgeWrapper o1, EdgeWrapper o2) {
        if (o1.wrapper.getID() < o2.wrapper.getID()) {
            return -1;
        } else if (o1.wrapper.getID() > o2.wrapper.getID()) {
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

    public int count;
    public ArrayWrapper wrapper;

    public EdgeWrapper(int count, ArrayWrapper wrapper) {
        this.count = count;
        this.wrapper = wrapper;
    }
}

/**
 *
 * @author pjmcswee
 */
class ArrayWrapper implements Comparable {

    private EdgeWrapper[] array;
    private int ID;
    public Node node;

    /**
     * Empty Constructor/
     */
    ArrayWrapper() {
    }

    /**
     *
     * @return The ID of this array wrapper
     */
    public int getID() {
        return ID;
    }

    /**
     *
     * @return The adjacency array
     */
    public EdgeWrapper[] getArray() {
        return array;
    }

    public void setArray(EdgeWrapper[] array) {
        this.array = array;
    }

    /**
     *
     * @param pArray
     */
    ArrayWrapper(int ID, EdgeWrapper[] array) {
        this.array = array;
        this.ID = ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    /**
     *
     * @param pIndex
     * @return
     */
    public int get(int index) {
        if (index >= array.length) {
            return -1;
        }
        return array[index].wrapper.ID;
    }

    public int getCount(int index) {
        if (index >= array.length) {
            return -1;
        }
        return array[index].count;
    }

    /**
     *
     * @return
     */
    public int length() {
        return array.length;
    }

    /**
     *
     * @param o
     * @return
     */
    @Override
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

    public static final String CLUSTERING_COEFF = "clustering";
    /**
     * The avergage Clustering Coefficient.
     */
    private double avgClusteringCoeff;
    /**
     * Indicates should treat graph as undirected.
     */
    private boolean isDirected;
    /**
     * Indicates statistics should stop processing/
     */
    private boolean isCanceled;
    /**
     * Keeps track of Progress made.
     */
    private ProgressTicket progress;
    private int[] triangles;
    private ArrayWrapper[] network;
    private int K;
    private int N;
    private double[] nodeClustering;
    private int totalTriangles;

    public ClusteringCoefficient() {
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        if (graphController != null && graphController.getGraphModel() != null) {
            isDirected = graphController.getGraphModel().isDirected();
        }
    }

    public double getAverageClusteringCoefficient() {
        return avgClusteringCoeff;
    }

    @Override
    public void execute(GraphModel graphModel) {
        Graph graph;
        if (isDirected) {
            graph = graphModel.getDirectedGraphVisible();
        } else {
            graph = graphModel.getUndirectedGraphVisible();
        }

        execute(graph);
    }

    public void execute(Graph graph) {
        isCanceled = false;

        HashMap<String, Double> resultValues;

        if (isDirected) {
            avgClusteringCoeff = bruteForce(graph);
        } else {
            initStartValues(graph);
            resultValues = computeTriangles(graph, network, triangles, nodeClustering, isDirected);
            totalTriangles = resultValues.get("triangles").intValue();
            avgClusteringCoeff = resultValues.get("clusteringCoefficient");

        }

        //Set results in columns
        Table nodeTable = graph.getModel().getNodeTable();
        Column clusteringCol = nodeTable.getColumn(CLUSTERING_COEFF);
        if (clusteringCol == null) {
            clusteringCol = nodeTable.addColumn(CLUSTERING_COEFF, "Clustering Coefficient", Double.class, 0.0);
        }

        Column triCount = null;
        if (!isDirected) {
            triCount = nodeTable.getColumn("Triangles");
            if (triCount == null) {
                triCount = nodeTable.addColumn("Triangles", "Number of triangles", Integer.class, 0);
            }
        }

        for (int v = 0; v < N; v++) {
            if (network[v].length() > 1) {
                network[v].node.setAttribute(clusteringCol, nodeClustering[v]);
                if (!isDirected) {
                    network[v].node.setAttribute(triCount, triangles[v]);
                }
            }
        }
    }

    public void triangles(Graph graph) {
        initStartValues(graph);
        HashMap<String, Double> resultValues = computeTriangles(graph, network, triangles,
                nodeClustering, isDirected);
        totalTriangles = resultValues.get("triangles").intValue();
        avgClusteringCoeff = resultValues.get("clusteringCoefficient");
    }

    public HashMap<String, Double> computeClusteringCoefficient(Graph graph, ArrayWrapper[] currentNetwork,
            int[] currentTriangles, double[] currentNodeClustering, boolean directed) {
        HashMap<String, Double> resultValues = new HashMap<>();

        if (directed) {
            double avClusteringCoefficient = bruteForce(graph);
            resultValues.put("clusteringCoefficient", avClusteringCoefficient);
            return resultValues;
        } else {
            initStartValues(graph);
            resultValues = computeTriangles(graph, currentNetwork, currentTriangles, currentNodeClustering, directed);
            return resultValues;

        }
    }

    public void initStartValues(Graph graph) {
        N = graph.getNodeCount();
        K = (int) Math.sqrt(N);
        nodeClustering = new double[N];
        network = new ArrayWrapper[N];
        triangles = new int[N];
    }

    public int createIndiciesMapAndInitNetwork(Graph graph, HashMap<Node, Integer> indicies, ArrayWrapper[] networks, int currentProgress) {
        int index = 0;
        for (Node s : graph.getNodes()) {
            indicies.put(s, index);
            networks[index] = new ArrayWrapper();
            index++;
            Progress.progress(progress, ++currentProgress);
        }
        return currentProgress;
    }

    private int closest_in_array(ArrayWrapper[] currentNetwork, int v) {
        int right = currentNetwork[v].length() - 1;

        /* optimization for extreme cases */
        if (right < 0) {
            return (-1);
        }
        if (currentNetwork[v].get(0) >= v) {
            return (-1);
        }
        if (currentNetwork[v].get(right) < v) {
            return (right);
        }
        if (currentNetwork[v].get(right) == v) {
            return (right - 1);
        }

        int left = 0, mid;
        while (right > left) {
            mid = (left + right) / 2;
            if (v < currentNetwork[v].get(mid)) {
                right = mid - 1;
            } else if (v > currentNetwork[v].get(mid)) {
                left = mid + 1;
            } else {
                return (mid - 1);
            }
        }

        if (v > currentNetwork[v].get(right)) {
            return (right);
        } else {

            return right - 1;
        }
    }

    /**
     *
     * @param v - The specific node to count the triangles on.
     */
    private void newVertex(ArrayWrapper[] currentNetwork, int[] currentTrianlgles, int v, int n) {
        int[] A = new int[n];

        for (int i = currentNetwork[v].length() - 1; (i >= 0) && (currentNetwork[v].get(i) > v); i--) {
            int neighbor = currentNetwork[v].get(i);
            A[neighbor] = currentNetwork[v].getCount(i);
        }
        for (int i = currentNetwork[v].length() - 1; i >= 0; i--) {
            int neighbor = currentNetwork[v].get(i);
            for (int j = closest_in_array(currentNetwork, neighbor); j >= 0; j--) {
                int next = currentNetwork[neighbor].get(j);
                if (A[next] > 0) {
                    currentTrianlgles[next] += currentNetwork[v].getCount(i);
                    currentTrianlgles[v] += currentNetwork[v].getCount(i);
                    currentTrianlgles[neighbor] += A[next];
                }
            }
        }
    }

    private void tr_link_nohigh(ArrayWrapper[] currentNetwork, int[] currentTriangles, int u, int v, int count, int k) {
        int iu = 0, iv = 0, w;
        while ((iu < currentNetwork[u].length()) && (iv < currentNetwork[v].length())) {
            if (currentNetwork[u].get(iu) < currentNetwork[v].get(iv)) {
                iu++;
            } else if (currentNetwork[u].get(iu) > currentNetwork[v].get(iv)) {
                iv++;
            } else { /* neighbor in common */

                w = currentNetwork[u].get(iu);
                if (w >= k) {
                    currentTriangles[w] += count;
                }
                iu++;
                iv++;
            }
        }
    }

    private HashMap<Node, EdgeWrapper> createNeighbourTable(Graph graph, Node node, HashMap<Node, Integer> indicies,
            ArrayWrapper[] networks, boolean directed) {

        HashMap<Node, EdgeWrapper> neighborTable = new HashMap<>();

        if (!directed) {
            for (Edge edge : graph.getEdges(node)) {
                Node neighbor = graph.getOpposite(node, edge);
                neighborTable.put(neighbor, new EdgeWrapper(1, networks[indicies.get(neighbor)]));
            }
        } else {
            for (Node neighbor : ((DirectedGraph) graph).getPredecessors(node)) {
                neighborTable.put(neighbor, new EdgeWrapper(1, networks[indicies.get(neighbor)]));
            }

            for (Edge out : ((DirectedGraph) graph).getOutEdges(node)) {
                Node neighbor = out.getTarget();
                EdgeWrapper ew = neighborTable.get(neighbor);
                if (ew == null) {
                    neighborTable.put(neighbor, new EdgeWrapper(1, network[indicies.get(neighbor)]));
                } else {
                    ew.count++;
                }
            }
        }
        return neighborTable;
    }

    private EdgeWrapper[] getEdges(HashMap<Node, EdgeWrapper> neighborTable) {

        int i = 0;
        EdgeWrapper[] edges = new EdgeWrapper[neighborTable.size()];
        for (EdgeWrapper e : neighborTable.values()) {
            edges[i] = e;
            i++;
        }
        return edges;
    }

    private int processNetwork(ArrayWrapper[] currentNetwork, int currentProgress) {
        Arrays.sort(currentNetwork);
        for (int j = 0; j < N; j++) {
            currentNetwork[j].setID(j);
            Progress.progress(progress, ++currentProgress);
        }

        for (int j = 0; j < N; j++) {
            Arrays.sort(currentNetwork[j].getArray(), new Renumbering());
            Progress.progress(progress, ++currentProgress);
        }
        return currentProgress;
    }

    private int computeRemainingTrianles(Graph graph, ArrayWrapper[] currentNetwork, int[] currentTriangles, int currentProgress) {
        int n = graph.getNodeCount();
        int k = (int) Math.sqrt(n);
        for (int v = n - 1; (v >= 0) && (v >= k); v--) {
            for (int i = closest_in_array(currentNetwork, v); i >= 0; i--) {
                int u = currentNetwork[v].get(i);
                if (u >= k) {
                    tr_link_nohigh(currentNetwork, currentTriangles, u, v, currentNetwork[v].getCount(i), k);
                }
            }
            Progress.progress(progress, ++currentProgress);

            if (isCanceled) {
                return currentProgress;
            }
        }
        return currentProgress;
    }

    private HashMap<String, Double> computeResultValues(Graph graph, ArrayWrapper[] currentNetwork,
            int[] currentTriangles, double[] currentNodeClusterig, boolean directed, int currentProgress) {
        int n = graph.getNodeCount();
        HashMap<String, Double> totalValues = new HashMap<>();
        int numNodesDegreeGreaterThanOne = 0;
        int trianglesNumber = 0;
        double currentClusteringCoefficient = 0;
        for (int v = 0; v < n; v++) {
            if (currentNetwork[v].length() > 1) {
                numNodesDegreeGreaterThanOne++;
                double cc = currentTriangles[v];
                trianglesNumber += currentTriangles[v];
                cc /= (currentNetwork[v].length() * (currentNetwork[v].length() - 1));
                if (!directed) {
                    cc *= 2.0f;
                }
                currentNodeClusterig[v] = cc;
                currentClusteringCoefficient += cc;
            }
            Progress.progress(progress, ++currentProgress);

            if (isCanceled) {
                return totalValues;
            }
        }
        trianglesNumber /= 3;
        currentClusteringCoefficient /= numNodesDegreeGreaterThanOne;

        totalValues.put("triangles", (double) trianglesNumber);
        totalValues.put("clusteringCoefficient", currentClusteringCoefficient);
        return totalValues;
    }

    private HashMap<String, Double> computeTriangles(Graph graph, ArrayWrapper[] currentNetwork, int[] currentTriangles,
            double[] nodeClustering, boolean directed) {

        HashMap<String, Double> resultValues = new HashMap<>();
        int ProgressCount = 0;
        Progress.start(progress, 7 * graph.getNodeCount());

        graph.readLock();
        try {
            int n = graph.getNodeCount();

            /**
             * Create network for processing
             */
            /**
             *         */
            HashMap<Node, Integer> indicies = new HashMap<>();

            ProgressCount = createIndiciesMapAndInitNetwork(graph, indicies, currentNetwork, ProgressCount);

            int index = 0;
            NodeIterable nodesIterable = graph.getNodes();
            for (Node node : nodesIterable) {
                HashMap<Node, EdgeWrapper> neighborTable = createNeighbourTable(graph, node, indicies, currentNetwork, directed);

                EdgeWrapper[] edges = getEdges(neighborTable);
                currentNetwork[index].node = node;
                currentNetwork[index].setArray(edges);
                index++;
                Progress.progress(progress, ++ProgressCount);

                if (isCanceled) {
                    nodesIterable.doBreak();
                    return resultValues;
                }
            }

            ProgressCount = processNetwork(currentNetwork, ProgressCount);

            int k = (int) Math.sqrt(n);

            for (int v = 0; v < k && v < n; v++) {
                newVertex(currentNetwork, currentTriangles, v, n);
                Progress.progress(progress, ++ProgressCount);
            }

            /* remaining links */
            ProgressCount = computeRemainingTrianles(graph, currentNetwork, currentTriangles, ProgressCount);

            resultValues = computeResultValues(graph, currentNetwork, currentTriangles, nodeClustering, directed, ProgressCount);
        } finally {
            graph.readUnlock();
        }

        return resultValues;
    }

    private double bruteForce(Graph graph) {
        //The atrributes computed by the statistics
        Column clusteringColumn = initializeAttributeColunms(graph.getModel());

        float totalCC = 0;

        graph.readLock();
        
        try {
            Progress.start(progress, graph.getNodeCount());
            int node_count = 0;
            NodeIterable nodesIterable = graph.getNodes();
            for (Node node : nodesIterable) {
                float nodeClusteringCoefficient = computeNodeClusteringCoefficient(graph, node, isDirected);

                if (nodeClusteringCoefficient > -1) {

                    saveCalculatedValue(node, clusteringColumn, nodeClusteringCoefficient);

                    totalCC += nodeClusteringCoefficient;
                }

                if (isCanceled) {
                    nodesIterable.doBreak();
                    break;
                }

                node_count++;
                Progress.progress(progress, node_count);

            }
            double clusteringCoeff = totalCC / graph.getNodeCount();
            
            return clusteringCoeff;
        } finally {
            graph.readUnlockAll();
        }
    }

    private float increaseCCifNesessary(Graph graph, Node neighbor1, Node neighbor2, boolean directed, float nodeCC) {
        if (neighbor1 == neighbor2) {
            return nodeCC;
        }
        if (directed) {
            if (graph.isAdjacent(neighbor1, neighbor2)) {
                nodeCC++;
            }
            if (graph.isAdjacent(neighbor2, neighbor1)) {
                nodeCC++;
            }
        } else {
            if (graph.isAdjacent(neighbor1, neighbor2)) {
                nodeCC++;
            }
        }
        return nodeCC;
    }

    private float computeNodeClusteringCoefficient(Graph graph, Node node, boolean directed) {
        float nodeCC = 0;
        int neighborhood = 0;
        NodeIterable neighbors1 = graph.getNeighbors(node);
        for (Node neighbor1 : neighbors1) {
            neighborhood++;
            NodeIterable neighbors2 = graph.getNeighbors(node);

            for (Node neighbor2 : neighbors2) {
                nodeCC = increaseCCifNesessary(graph, neighbor1, neighbor2, directed, nodeCC);
            }
        }
        nodeCC /= 2.0;

        if (neighborhood > 1) {
            float cc = nodeCC / (.5f * neighborhood * (neighborhood - 1));
            if (directed) {
                cc = nodeCC / (neighborhood * (neighborhood - 1));
            }

            return cc;
        } else {
            return -1.f;
        }
    }

    private Column initializeAttributeColunms(GraphModel graphModel) {

        if (graphModel == null) {
            return null;
        }
        Table nodeTable = graphModel.getNodeTable();
        Column clusteringCol = nodeTable.getColumn("clustering");

        if (clusteringCol == null) {
            clusteringCol = nodeTable.addColumn("clustering", "Clustering Coefficient", Double.class, new Double(0));
        }

        return clusteringCol;
    }

    private void saveCalculatedValue(Node node, Column clusteringColumn,
            double nodeClusteringCoefficient) {

        if (clusteringColumn == null) {
            return;
        }
        node.setAttribute(clusteringColumn, nodeClusteringCoefficient);
    }

    @Override
    public String getReport() {
        //distribution of values
        Map<Double, Integer> dist = new HashMap<>();
        for (int i = 0; i < N; i++) {
            Double d = nodeClustering[i];
            if (dist.containsKey(d)) {
                Integer v = dist.get(d);
                dist.put(d, v + 1);
            } else {
                dist.put(d, 1);
            }
        }

        //Distribution series
        XYSeries dSeries = ChartUtils.createXYSeries(dist, "Clustering Coefficient");
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(dSeries);

        JFreeChart chart = ChartFactory.createScatterPlot(
                "Clustering Coefficient Distribution",
                "Value",
                "Count",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                false,
                false);
        chart.removeLegend();
        ChartUtils.decorateChart(chart);
        ChartUtils.scaleChart(chart, dSeries, false);
        String imageFile = ChartUtils.renderChart(chart, "clustering-coefficient.png");

        NumberFormat f = new DecimalFormat("#0.000");

        if (isDirected) {
            return "<HTML> <BODY> <h1> Clustering Coefficient Metric Report </h1> "
                    + "<hr>"
                    + "<br />" + "<h2> Parameters: </h2>"
                    + "Network Interpretation:  " + (isDirected ? "directed" : "undirected") + "<br />"
                    + "<br>" + "<h2> Results: </h2>"
                    + "Average Clustering Coefficient: " + f.format(avgClusteringCoeff) + "<br />"
                    + "The Average Clustering Coefficient is the mean value of individual coefficients.<br /><br />"
                    + imageFile
                    + "<br /><br />" + "<h2> Algorithm: </h2>"
                    + "Simple and slow brute force.<br />"
                    + "</BODY> </HTML>";
        } else {

            return "<HTML> <BODY> <h1> Clustering Coefficient Metric Report </h1> "
                    + "<hr>"
                    + "<br />" + "<h2> Parameters: </h2>"
                    + "Network Interpretation:  " + (isDirected ? "directed" : "undirected") + "<br />"
                    + "<br>" + "<h2> Results: </h2>"
                    + "Average Clustering Coefficient: " + f.format(avgClusteringCoeff) + "<br />"
                    + "Total triangles: " + totalTriangles + "<br />"
                    + "The Average Clustering Coefficient is the mean value of individual coefficients.<br /><br />"
                    + imageFile
                    + "<br /><br />" + "<h2> Algorithm: </h2>"
                    + "Matthieu Latapy, <i>Main-memory Triangle Computations for Very Large (Sparse (Power-Law)) Graphs</i>, in Theoretical Computer Science (TCS) 407 (1-3), pages 458-473, 2008<br />"
                    + "</BODY> </HTML>";
        }
    }

    public void setDirected(boolean isDirected) {
        this.isDirected = isDirected;
    }

    public boolean isDirected() {
        return isDirected;
    }

    @Override
    public boolean cancel() {
        isCanceled = true;
        return true;
    }

    @Override
    public void setProgressTicket(ProgressTicket ProgressTicket) {
        this.progress = ProgressTicket;
    }

    public double[] getCoefficientReuslts() {
        double[] res = new double[N];
        for (int v = 0; v < N; v++) {
            if (network[v].length() > 1) {
                res[v] = nodeClustering[v];
            }
        }
        return res;
    }

    public double[] getTriangesReuslts() {
        double[] res = new double[N];
        for (int v = 0; v < N; v++) {
            if (network[v].length() > 1) {
                res[v] = triangles[v];
            }
        }
        return res;
    }
}
