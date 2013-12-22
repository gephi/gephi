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

import java.io.IOException;
import java.util.HashMap;
import org.gephi.statistics.spi.Statistics;
import org.gephi.graph.api.*;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
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
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Ref: Ulrik Brandes, A Faster Algorithm for Betweenness Centrality,
 * in Journal of Mathematical Sociology 25(2):163-177, (2001)
 *
 * @author pjmcswee
 */
public class GraphDistance implements Statistics, LongTask {

    public static final String BETWEENNESS = "betweenesscentrality";
    public static final String CLOSENESS = "closnesscentrality";
    public static final String ECCENTRICITY = "eccentricity";
    /** */
    private double[] betweenness;
    /** */
    private double[] closeness;
    /** */
    private double[] eccentricity;
    /** */
    private int diameter;
    private int radius;
    /** */
    private double avgDist;
    /** */
    private int N;
    /** */
    private boolean isDirected;
    /** */
    private ProgressTicket progress;
    /** */
    private boolean isCanceled;
    private int shortestPaths;
    private boolean isNormalized;

    public GraphDistance() {
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        if (graphController != null && graphController.getModel() != null) {
            isDirected = graphController.getModel().isDirected();
        }
    }

    public double getPathLength() {
        return avgDist;
    }

    /**
     * 
     * @return
     */
    public double getDiameter() {
        return diameter;
    }
    
    public double getRadius() {
        return radius;
    }

    /**
     *
     * @param graphModel
     */
    public void execute(GraphModel graphModel, AttributeModel attributeModel) {
        HierarchicalGraph graph = null;
        if (isDirected) {
            graph = graphModel.getHierarchicalDirectedGraphVisible();
        } else {
            graph = graphModel.getHierarchicalUndirectedGraphVisible();
        }
        execute(graph, attributeModel);
    }

    public void execute(HierarchicalGraph hgraph, AttributeModel attributeModel) {
        isCanceled = false;
        
        Map<String, AttributeColumn> attributeColumns = initializeAttributeColunms(attributeModel); 

        hgraph.readLock();

        N = hgraph.getNodeCount();
        
        initializeStartValues();
        
        HierarchicalUndirectedGraph undirectedGraph = hgraph.getGraphModel().getHierarchicalUndirectedGraph();
        HashMap<Node, Integer> indicies = createIndiciesMap(undirectedGraph);

        Map<String, double[]> metrics = calculateDistanceMetrics(hgraph, indicies, isDirected, isNormalized);
        
        eccentricity = metrics.get(ECCENTRICITY);
        closeness = metrics.get(CLOSENESS);
        betweenness = metrics.get(BETWEENNESS);
        
        saveCalculatedValues(hgraph, attributeColumns, indicies, eccentricity, betweenness, closeness);
                
        hgraph.readUnlock();
    }
    
    public Map<String, double[]> calculateDistanceMetrics(HierarchicalGraph hgraph, HashMap<Node, Integer> indicies, boolean directed, boolean normalized) {
        int n = hgraph.getNodeCount();
        
        HashMap<String, double[]> metrics = new HashMap<String, double[]>();
        
        double[] nodeEccentricity = new double[n];
        double[] nodeBetweenness = new double[n];
        double[] nodeCloseness = new double[n];
        
        metrics.put(ECCENTRICITY, nodeEccentricity);
        metrics.put(CLOSENESS, nodeCloseness);
        metrics.put(BETWEENNESS, nodeBetweenness);
        
        Progress.start(progress, hgraph.getNodeCount());
        int count = 0;
        
        
        for (Node s : hgraph.getNodes()) {
            Stack<Node> S = new Stack<Node>();

            LinkedList<Node>[] P = new LinkedList[n];
            double[] theta = new double[n];
            int[] d = new int[n];
            
            int s_index = indicies.get(s);
            
            setInitParametetrsForNode(s, P, theta, d, s_index, n);

            LinkedList<Node> Q = new LinkedList<Node>();
            Q.addLast(s);
            while (!Q.isEmpty()) {
                Node v = Q.removeFirst();
                S.push(v);
                int v_index = indicies.get(v);

                EdgeIterable edgeIter = getEdgeIter(hgraph, v, directed);

                for (Edge edge : edgeIter) {
                    Node reachable = hgraph.getOpposite(v, edge);

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
            for (int i = 0; i < n; i++) {
                if (d[i] > 0) {
                    avgDist += d[i];
                    nodeEccentricity[s_index] = (int) Math.max(nodeEccentricity[s_index], d[i]);
                    nodeCloseness[s_index] += d[i];
                    diameter = Math.max(diameter, d[i]);
                    reachable++;
                }
            }

            radius = (int) Math.min(nodeEccentricity[s_index], radius);

            if (reachable != 0) {
                nodeCloseness[s_index] /= reachable;
            }

            shortestPaths += reachable;

            double[] delta = new double[n];
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
                    nodeBetweenness[w_index] += delta[w_index];
                }
            }
            count++;
            if (isCanceled) {
                hgraph.readUnlockAll();
                return metrics;
            }
            Progress.progress(progress, count);
        }

        avgDist /= shortestPaths;//mN * (mN - 1.0f);

        calculateCorrection(hgraph, indicies, nodeBetweenness, nodeCloseness, directed, normalized);
        
        return metrics;
    }
    
    private void setInitParametetrsForNode(Node s, LinkedList<Node>[] P, double[] theta, int[] d, int index, int n) {           
            for (int j = 0; j < n; j++) {
                P[j] = new LinkedList<Node>();
                theta[j] = 0;
                d[j] = -1;
            }
            theta[index] = 1;
            d[index] = 0;
    }
    
    private EdgeIterable getEdgeIter(HierarchicalGraph hgraph, Node v, boolean directed) {
            EdgeIterable edgeIter = null;
            if (directed) {
                edgeIter = ((HierarchicalDirectedGraph) hgraph).getOutEdgesAndMetaOutEdges(v);
            } else {
                edgeIter = hgraph.getEdgesAndMetaEdges(v);
             }
            return edgeIter;
    }
    
    private Map<String, AttributeColumn> initializeAttributeColunms(AttributeModel attributeModel) {
        Map<String, AttributeColumn> attributeColumns = new HashMap<String, AttributeColumn>();
        
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
        
        attributeColumns.put(ECCENTRICITY, eccentricityCol);
        attributeColumns.put(CLOSENESS, closenessCol);
        attributeColumns.put(BETWEENNESS, betweenessCol);
        
        return attributeColumns;
    }
    
     public  HashMap<Node, Integer> createIndiciesMap(HierarchicalUndirectedGraph hgraph) {
       HashMap<Node, Integer> indicies = new HashMap<Node, Integer>();
        int index = 0;
        for (Node s : hgraph.getNodes()) {
            indicies.put(s, index);
            index++;
        } 
        return indicies;
    }
     
     public void initializeStartValues() {
        betweenness = new double[N];
        eccentricity = new double[N];
        closeness = new double[N];
        diameter = 0;
        avgDist = 0;
        shortestPaths = 0;
        radius = Integer.MAX_VALUE;
     }
     
     private void calculateCorrection(HierarchicalGraph hgraph, HashMap<Node, Integer> indicies,
             double[] nodeBetweenness, double[] nodeCloseness, boolean directed, boolean normalized) {
         
         int n = hgraph.getNodeCount();
         
         for (Node s : hgraph.getNodes()) {
            
             int s_index = indicies.get(s);

            if (!directed) {
                nodeBetweenness[s_index] /= 2;
            }
            if (normalized) {
                nodeCloseness[s_index] = (nodeCloseness[s_index] == 0) ? 0 : 1.0 / nodeCloseness[s_index];
                nodeBetweenness[s_index] /= directed ? (n - 1) * (n - 2) : (n - 1) * (n - 2) / 2;
            }     
         }
     }
     
     private void saveCalculatedValues(HierarchicalGraph hgraph, Map<String, AttributeColumn> attributeColumns, HashMap<Node, Integer> indicies,
            double[] nodeEccentricity, double[] nodeBetweenness, double[] nodeCloseness) {
        for (Node s : hgraph.getNodes()) {
            AttributeRow row = (AttributeRow) s.getNodeData().getAttributes();
            int s_index = indicies.get(s);


            AttributeColumn eccentricityCol=attributeColumns.get(ECCENTRICITY);
            AttributeColumn closenessCol=attributeColumns.get(CLOSENESS);
            AttributeColumn betweenessCol=attributeColumns.get(BETWEENNESS);
            row.setValue(eccentricityCol, nodeEccentricity[s_index]);
            row.setValue(closenessCol, nodeCloseness[s_index]);
            row.setValue(betweenessCol, nodeBetweenness[s_index]);
        }
    }

    public void setNormalized(boolean isNormalized) {
        this.isNormalized = isNormalized;
    }

    public boolean isNormalized() {
        return isNormalized;
    }

    public void setDirected(boolean isDirected) {
        this.isDirected = isDirected;
    }

    public boolean isDirected() {
        return isDirected;
    }

    private String createImageFile(TempDir tempDir, double[] pVals, String pName, String pX, String pY) {
        //distribution of values
        Map<Double, Integer> dist = new HashMap<Double, Integer>();
        for (int i = 0; i < N; i++) {
            Double d = pVals[i];
            if (dist.containsKey(d)) {
                Integer v = dist.get(d);
                dist.put(d, v + 1);
            } else {
                dist.put(d, 1);
            }
        }

        //Distribution series
        XYSeries dSeries = ChartUtils.createXYSeries(dist, pName);

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(dSeries);

        JFreeChart chart = ChartFactory.createXYLineChart(
                pName,
                pX,
                pY,
                dataset,
                PlotOrientation.VERTICAL,
                true,
                false,
                false);
        chart.removeLegend();
        ChartUtils.decorateChart(chart);
        ChartUtils.scaleChart(chart, dSeries, isNormalized);
        return ChartUtils.renderChart(chart, pName + ".png");
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
            htmlIMG1 = createImageFile(tempDir, betweenness, "Betweenness Centrality Distribution", "Value", "Count");
            htmlIMG2 = createImageFile(tempDir, closeness, "Closeness Centrality Distribution", "Value", "Count");
            htmlIMG3 = createImageFile(tempDir, eccentricity, "Eccentricity Distribution", "Value", "Count");
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        String report = "<HTML> <BODY> <h1>Graph Distance  Report </h1> "
                + "<hr>"
                + "<br>"
                + "<h2> Parameters: </h2>"
                + "Network Interpretation:  " + (isDirected ? "directed" : "undirected") + "<br />"
                + "<br /> <h2> Results: </h2>"
                + "Diameter: " + diameter + "<br />"
                + "Radius: " + radius + "<br />"
                + "Average Path length: " + avgDist + "<br />"
                + "Number of shortest paths: " + shortestPaths + "<br /><br />"
                + htmlIMG1 + "<br /><br />"
                + htmlIMG2 + "<br /><br />"
                + htmlIMG3
                + "<br /><br />" + "<h2> Algorithm: </h2>"
                + "Ulrik Brandes, <i>A Faster Algorithm for Betweenness Centrality</i>, in Journal of Mathematical Sociology 25(2):163-177, (2001)<br />"
                + "</BODY> </HTML>";

        return report;
    }

    /**
     * 
     * @return
     */
    public boolean cancel() {
        this.isCanceled = true;
        return true;
    }

    /**
     *
     * @param progressTicket
     */
    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progress = progressTicket;
    }
}
