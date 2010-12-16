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
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import org.gephi.statistics.spi.Statistics;
import org.gephi.graph.api.Node;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.HierarchicalDirectedGraph;
import org.gephi.graph.api.HierarchicalGraph;
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
import org.openide.util.Lookup;

/**
 *
 * @author pjmcswee
 */
class Renumbering implements Comparator<EdgeWrapper> {

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

    /** Empty Constructor/ */
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
    /** The avergage Clustering Coefficient.*/
    private double avgClusteringCoeff;
    /**Indicates should treat graph as undirected.*/
    private boolean isDirected;
    /** Indicates statistics should stop processing/*/
    private boolean isCanceled;
    /** Keeps track of Progress made. */
    private ProgressTicket progress;
    private int[] triangles;
    private ArrayWrapper[] network;
    private int K;
    private int N;
    private double[] nodeClustering;
    private int totalTriangles;

    public ClusteringCoefficient() {
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        if (graphController != null && graphController.getModel() != null) {
            isDirected = graphController.getModel().isDirected();
        }
    }
    
    public double getAverageClusteringCoefficient() {
        return avgClusteringCoeff;
    }

    public void execute(GraphModel graphModel, AttributeModel attributeModel) {
        HierarchicalGraph graph = null;
        if (!isDirected) {
            graph = graphModel.getHierarchicalUndirectedGraphVisible();
        } else {
            graph = graphModel.getHierarchicalDirectedGraphVisible();
        }

        execute(graph, attributeModel);
    }

    public void execute(HierarchicalGraph graph, AttributeModel attributeModel) {
        isCanceled = false;

        triangles(graph, attributeModel);
    }

    /**
     *
     * @param v
     * @return
     */
    public int closest_in_array(int v) {
        int right = network[v].length() - 1;

        /* optimization for extreme cases */
        if (right < 0) {
            return (-1);
        }
        if (network[v].get(0) >= v) {
            return (-1);
        }
        if (network[v].get(right) < v) {
            return (right);
        }
        if (network[v].get(right) == v) {
            return (right - 1);
        }

        int left = 0, mid;
        while (right > left) {
            mid = (left + right) / 2;
            if (v < network[v].get(mid)) {
                right = mid - 1;
            } else if (v > network[v].get(mid)) {
                left = mid + 1;
            } else {
                return (mid - 1);
            }
        }


        if (v > network[v].get(right)) {
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

        for (int i = network[v].length() - 1; (i >= 0) && (network[v].get(i) > v); i--) {
            int neighbor = network[v].get(i);
            A[neighbor] = network[v].getCount(i);
        }
        for (int i = network[v].length() - 1; i >= 0; i--) {
            int neighbor = network[v].get(i);
            for (int j = closest_in_array(neighbor); j >= 0; j--) {
                int next = network[neighbor].get(j);
                if (A[next] > 0) {
                    triangles[next] += network[v].getCount(i);
                    triangles[v] += network[v].getCount(i);
                    triangles[neighbor] += A[next];
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
        while ((iu < network[u].length()) && (iv < network[v].length())) {
            if (network[u].get(iu) < network[v].get(iv)) {
                iu++;
            } else if (network[u].get(iu) > network[v].get(iv)) {
                iv++;
            } else { /* neighbor in common */
                w = network[u].get(iu);
                if (w >= K) {
                    triangles[w] += count;
                }
                iu++;
                iv++;
            }
        }
    }

    public void triangles(HierarchicalGraph graph, AttributeModel attributeModel) {

        int ProgressCount = 0;
        Progress.start(progress, 7 * graph.getNodeCount());

        graph.readLock();

        N = graph.getNodeCount();
        nodeClustering = new double[N];

        /** Create network for processing */
        network = new ArrayWrapper[N];

        /**  */
        HashMap<Node, Integer> indicies = new HashMap<Node, Integer>();
        int index = 0;
        for (Node s : graph.getNodes()) {
            indicies.put(s, index);
            network[index] = new ArrayWrapper();
            index++;
            Progress.progress(progress, ++ProgressCount);
        }

        index = 0;
        for (Node node : graph.getNodes()) {
            HashMap<Node, EdgeWrapper> neighborTable = new HashMap<Node, EdgeWrapper>();

            if (!isDirected) {
                for (Node neighbor : graph.getNeighbors(node)) {
                    neighborTable.put(neighbor, new EdgeWrapper(1, network[indicies.get(neighbor)]));
                }
            } else {
                for (Edge in : ((HierarchicalDirectedGraph) graph).getInEdges(node)) {
                    Node neighbor = in.getSource();
                    neighborTable.put(neighbor, new EdgeWrapper(1, network[indicies.get(neighbor)]));
                }

                for (Edge out : ((HierarchicalDirectedGraph) graph).getOutEdges(node)) {
                    Node neighbor = out.getTarget();
                    EdgeWrapper ew = neighborTable.get(neighbor);
                    if (ew == null) {
                        neighborTable.put(neighbor, new EdgeWrapper(1, network[indicies.get(neighbor)]));
                    } else {
                        ew.count++;
                    }
                }
            }

            EdgeWrapper[] edges = new EdgeWrapper[neighborTable.size()];
            int i = 0;
            for (EdgeWrapper e : neighborTable.values()) {
                edges[i] = e;
                i++;
            }
            network[index].node = node;
            network[index].setArray(edges);
            index++;
            Progress.progress(progress, ++ProgressCount);

            if (isCanceled) {
                graph.readUnlockAll();
                return;
            }
        }

        Arrays.sort(network);
        for (int j = 0; j < N; j++) {
            network[j].setID(j);
            Progress.progress(progress, ++ProgressCount);
        }

        for (int j = 0; j < N; j++) {
            Arrays.sort(network[j].getArray(), new Renumbering());
            Progress.progress(progress, ++ProgressCount);
        }

        triangles = new int[N];
        K = (int) Math.sqrt(N);


        for (int v = 0; v < K && v < N; v++) {
            newVertex(v);
            Progress.progress(progress, ++ProgressCount);
        }

        /* remaining links */
        for (int v = N - 1; (v >= 0) && (v >= K); v--) {
            for (int i = closest_in_array(v); i >= 0; i--) {
                int u = network[v].get(i);
                if (u >= K) {
                    tr_link_nohigh(u, v, network[v].getCount(i));
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
        AttributeColumn clusteringCol = nodeTable.getColumn(CLUSTERING_COEFF);
        if (clusteringCol == null) {
            clusteringCol = nodeTable.addColumn(CLUSTERING_COEFF, "Clustering Coefficient", AttributeType.DOUBLE, AttributeOrigin.COMPUTED, new Double(0));
        }

        for (Node s : graph.getNodes()) {
            int v = indicies.get(s);
            if (network[v].length() > 1) {
                double cc = triangles[v];
                totalTriangles += triangles[v];
                cc /= (network[v].length() * (network[v].length() - 1));
                if (!isDirected) {
                    cc *= 2.0f;
                }
                nodeClustering[v] = cc;
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
        totalTriangles /= 3;
        avgClusteringCoeff /= N;

        graph.readUnlock();
    }


    /*public void bruteForce(GraphModel graphModel, AttributeModel attributeModel) {
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
    }*/

    public String getReport() {

        //double max = 0;
        XYSeries series1 = new XYSeries("Clustering Coefficient");
        for (int i = 0; i < N; i++) {
            series1.add(i, nodeClustering[i]);
            //max = Math.max(nodeClustering[i], max);
        }
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series1);


        JFreeChart chart = ChartFactory.createXYLineChart(
                "Clustering Coefficient Distribution",
                "Node",
                "Clustering Ceofficient",
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
            final String fileName = "coefficients.png";
            final File file1 = tempDir.createFile(fileName);
            imageFile = "<IMG SRC=\"file:" + file1.getAbsolutePath() + "\" " + "WIDTH=\"600\" HEIGHT=\"400\" BORDER=\"0\" USEMAP=\"#chart\"></IMG>";
            ChartUtilities.saveChartAsPNG(file1, chart, 600, 400, info);

        } catch (IOException e) {
            System.out.println(e.toString());
        }



        return "<HTML> <BODY> <h1> Clustering Coefficient Metric Report </h1> "
                + "<hr>"
                + "<br>" + "<h2> Parameters: </h2>"
                + "Network Interpretation:  " + (isDirected ? "directed" : "undirected") + "<br>"
                + "Average Clustering Coefficient: " + avgClusteringCoeff + "<br>"
                + "Total triangles: " + totalTriangles + "<br>"
                + imageFile + "<br>" + "</BODY> </HTML>";
    }

    public void setDirected(boolean isDirected) {
        this.isDirected = isDirected;
    }

    public boolean isDirected() {
        return isDirected;
    }

    public boolean cancel() {
        isCanceled = true;
        return true;
    }

    public void setProgressTicket(ProgressTicket ProgressTicket) {
        this.progress = ProgressTicket;
    }
}
