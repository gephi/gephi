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
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.openide.util.Lookup;
import org.gephi.graph.api.NodeIterable;
/**
 * Ref: Matthieu Latapy, Main-memory Triangle Computations for Very Large (Sparse (Power-Law)) Graphs,
 * in Theoretical Computer Science (TCS) 407 (1-3), pages 458-473, 2008
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
        HierarchicalGraph hgraph = null;
        if (isDirected) {
            hgraph = graphModel.getHierarchicalDirectedGraphVisible();
        } else {
            hgraph = graphModel.getHierarchicalUndirectedGraphVisible();
        }

        execute(hgraph, attributeModel);
    }
    
    public void execute(HierarchicalGraph hgraph, AttributeModel attributeModel) {
        isCanceled = false;
        
        HashMap<String, Double> resultValues = new HashMap<String, Double>();

        if(isDirected) {
            avgClusteringCoeff = bruteForce(hgraph, attributeModel);
        }
           
        else {
            initStartValues(hgraph);
            resultValues = computeTriangles(hgraph, network, triangles, nodeClustering, isDirected);
            totalTriangles = resultValues.get("triangles").intValue();
            avgClusteringCoeff = resultValues.get("clusteringCoefficient");
            
        }
        
        

        //Set results in columns
        AttributeTable nodeTable = attributeModel.getNodeTable();
        AttributeColumn clusteringCol = nodeTable.getColumn(CLUSTERING_COEFF);
        if (clusteringCol == null) {
            clusteringCol = nodeTable.addColumn(CLUSTERING_COEFF, "Clustering Coefficient", AttributeType.DOUBLE, AttributeOrigin.COMPUTED, new Double(0));
        }

        AttributeColumn triCount = null;
        if(!isDirected){
            triCount = nodeTable.getColumn("Triangles");
            if (triCount == null) {
                triCount = nodeTable.addColumn("Triangles", "Number of triangles", AttributeType.INT, AttributeOrigin.COMPUTED, new Integer(0));
            }
        }

        for (int v = 0; v < N; v++) {
            if (network[v].length() > 1) {
                AttributeRow row = (AttributeRow) network[v].node.getNodeData().getAttributes();
                row.setValue(clusteringCol, nodeClustering[v]);
                if(!isDirected)
                    row.setValue(triCount, triangles[v]);
            }
        }
    }
    
    public void triangles(HierarchicalGraph hgraph) {
        initStartValues(hgraph);
        HashMap<String, Double> resultValues = computeTriangles(hgraph, network, triangles,
                nodeClustering, isDirected);
        totalTriangles = resultValues.get("triangles").intValue();
        avgClusteringCoeff = resultValues.get("clusteringCoefficient");
    }
    
    public HashMap<String, Double> computeClusteringCoefficient(HierarchicalGraph hgraph, ArrayWrapper[] currentNetwork, 
            int[] currentTriangles, double[] currentNodeClustering, boolean directed) {
        HashMap<String, Double> resultValues = new HashMap<String, Double>();

        if(isDirected) {
            double avClusteringCoefficient = bruteForce(hgraph, null);
            resultValues.put("clusteringCoefficient", avClusteringCoefficient);
            return resultValues;
        }
           
        else {
            initStartValues(hgraph);
            resultValues = computeTriangles(hgraph, currentNetwork, currentTriangles, currentNodeClustering, directed);
            return resultValues;
            
        }
    }
    
    public void initStartValues(HierarchicalGraph hgraph) {
        N = hgraph.getNodeCount();
        K = (int) Math.sqrt(N);
        nodeClustering = new double[N];
        network = new ArrayWrapper[N];
        triangles = new int[N];
    }
    
     public int createIndiciesMapAndInitNetwork(HierarchicalGraph hgraph, HashMap<Node, Integer> indicies, ArrayWrapper[] networks, int currentProgress) {
        int index = 0;
        for (Node s : hgraph.getNodes()) {
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
   
    
    private HashMap<Node, EdgeWrapper> createNeighbourTable(HierarchicalGraph hgraph, Node node, HashMap<Node, Integer> indicies, 
            ArrayWrapper[] networks,boolean directed) {
        
        HashMap<Node, EdgeWrapper> neighborTable = new HashMap<Node, EdgeWrapper>();
        
        if (!directed) {
                for (Edge edge : hgraph.getEdgesAndMetaEdges(node)) {
                    Node neighbor = hgraph.getOpposite(node, edge);
                    neighborTable.put(neighbor, new EdgeWrapper(1, networks[indicies.get(neighbor)]));
                }
            } else {
                for (Edge in : ((HierarchicalDirectedGraph) hgraph).getInEdgesAndMetaInEdges(node)) {
                    Node neighbor = in.getSource().getNodeData().getNode(hgraph.getView().getViewId());
                    neighborTable.put(neighbor, new EdgeWrapper(1, networks[indicies.get(neighbor)]));
                }

                for (Edge out : ((HierarchicalDirectedGraph) hgraph).getOutEdgesAndMetaOutEdges(node)) {
                    Node neighbor = out.getTarget().getNodeData().getNode(hgraph.getView().getViewId());
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
    
    private int computeRemainingTrianles(HierarchicalGraph hgraph, ArrayWrapper[] currentNetwork, int[] currentTriangles, int currentProgress) {
        int n=hgraph.getNodeCount();
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
                hgraph.readUnlockAll();
                return currentProgress;
            }
        }
        return currentProgress;
    }
    
    private HashMap<String, Double> computeResultValues(HierarchicalGraph hgraph, ArrayWrapper[] currentNetwork, 
            int[] currentTriangles, double[] currentNodeClusterig, boolean directed, int currentProgress) {
        int n = hgraph.getNodeCount();
        HashMap<String, Double> totalValues = new HashMap<String, Double>();
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
                hgraph.readUnlockAll();
                return totalValues;
            }
        }
        trianglesNumber /= 3;
        currentClusteringCoefficient /= numNodesDegreeGreaterThanOne;
        
        totalValues.put("triangles", (double) trianglesNumber);
        totalValues.put("clusteringCoefficient", currentClusteringCoefficient);
        return totalValues;
    }
    
    

    private HashMap<String, Double> computeTriangles(HierarchicalGraph hgraph, ArrayWrapper[] currentNetwork, int[] currentTriangles, 
            double[] nodeClustering, boolean directed) {

        HashMap<String, Double> resultValues = new HashMap<String, Double>();
        int ProgressCount = 0;
        Progress.start(progress, 7 * hgraph.getNodeCount());

        hgraph.readLock();

        int n = hgraph.getNodeCount();

        /** Create network for processing */

        /**  */
        
        HashMap<Node, Integer> indicies = new HashMap<Node, Integer>();
        
        
        ProgressCount = createIndiciesMapAndInitNetwork(hgraph, indicies, currentNetwork, ProgressCount);

        int index = 0;
        for (Node node : hgraph.getNodes()) {
            HashMap<Node, EdgeWrapper> neighborTable = createNeighbourTable(hgraph, node, indicies, currentNetwork, directed);

            EdgeWrapper[] edges = getEdges(neighborTable);
            currentNetwork[index].node = node;
            currentNetwork[index].setArray(edges);
            index++;
            Progress.progress(progress, ++ProgressCount);

            if (isCanceled) {
                hgraph.readUnlockAll();
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
        
        ProgressCount = computeRemainingTrianles(hgraph, currentNetwork, currentTriangles, ProgressCount);
        
        resultValues = computeResultValues(hgraph, currentNetwork, currentTriangles, nodeClustering, directed, ProgressCount);

        

        hgraph.readUnlock();
        return resultValues;
    }
    
     private double bruteForce(HierarchicalGraph hgraph, AttributeModel attributeModel) {
        //The atrributes computed by the statistics
        AttributeColumn clusteringColumn = initializeAttributeColunms(attributeModel);

        float totalCC = 0;

        hgraph.readLock();

        Progress.start(progress, hgraph.getNodeCount());
        int node_count = 0;
        for (Node node : hgraph.getNodes()) {
            float nodeClusteringCoefficient = computeNodeClusteringCoefficient(hgraph, node, isDirected);
            
            if (nodeClusteringCoefficient>-1) {

                saveCalculatedValue(node, clusteringColumn, nodeClusteringCoefficient);

                totalCC += nodeClusteringCoefficient;
            }

            if (isCanceled) {
                break;
            }

            node_count++;
            Progress.progress(progress, node_count);

        }
        double clusteringCoeff = totalCC / hgraph.getNodeCount();

        hgraph.readUnlockAll();
        
        return clusteringCoeff;
    }
    
    private float increaseCCifNesessary(HierarchicalGraph hgraph, Node neighbor1, Node neighbor2, boolean directed, float nodeCC) {
        if (neighbor1 == neighbor2) {
            return nodeCC;
        }
        if (directed) {
            if (((HierarchicalDirectedGraph) hgraph).getEdge(neighbor1, neighbor2) != null) {
                nodeCC++;
            }
            if (((HierarchicalDirectedGraph) hgraph).getEdge(neighbor2, neighbor1) != null) {
                nodeCC++;
            }
        } else {
            if (hgraph.isAdjacent(neighbor1, neighbor2)) {
                nodeCC++;
            }
        }
        return nodeCC;
    }
    
    private float computeNodeClusteringCoefficient(HierarchicalGraph hgraph, Node node, boolean directed) {
        float nodeCC = 0;
        int neighborhood = 0;
        NodeIterable neighbors1 = hgraph.getNeighbors(node);
        for (Node neighbor1 : neighbors1) {
            neighborhood++;
            NodeIterable neighbors2 = hgraph.getNeighbors(node);
            
            for (Node neighbor2 : neighbors2) {
              nodeCC=increaseCCifNesessary(hgraph, neighbor1, neighbor2, directed, nodeCC);
            }
        }
        nodeCC /= 2.0;

        if (neighborhood > 1) {
            float cc = nodeCC / (.5f * neighborhood * (neighborhood - 1));
            if (directed) {
                cc = nodeCC / (neighborhood * (neighborhood - 1));
            }

            return cc;
        } else 
            return -1.f;
    }
    
    private AttributeColumn initializeAttributeColunms(AttributeModel attributeModel) {
        
        if (attributeModel==null) return null;
        AttributeTable nodeTable = attributeModel.getNodeTable();
        AttributeColumn clusteringCol = nodeTable.getColumn("clustering");
        
        if (clusteringCol == null) {
            clusteringCol = nodeTable.addColumn("clustering", "Clustering Coefficient", AttributeType.DOUBLE, AttributeOrigin.COMPUTED, new Double(0));
        }
        
        return clusteringCol;
    }
    
    private void saveCalculatedValue(Node node, AttributeColumn clusteringColumn, 
            float nodeClusteringCoefficient) {  

        if (clusteringColumn==null) return;
        AttributeRow row = (AttributeRow) node.getNodeData().getAttributes();
        row.setValue(clusteringColumn, nodeClusteringCoefficient);
    }

   
    
    
    public String getReport() {
        //distribution of values
        Map<Double, Integer> dist = new HashMap<Double, Integer>();
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

        if(isDirected){
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

    public boolean cancel() {
        isCanceled = true;
        return true;
    }

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
