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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.HierarchicalDirectedGraph;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.HierarchicalUndirectedGraph;
import org.gephi.graph.api.Node;
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
 * Ref: Jon M. Kleinberg, Authoritative Sources in a Hyperlinked Environment,
 * in Journal of the ACM 46 (5): 604–632 (1999)
 *
 * @author pjmcswee
 */
public class Hits implements Statistics, LongTask {

    public static final String AUTHORITY = "authority";
    public static final String HUB = "hub";
    private boolean isCanceled;
    private ProgressTicket progress;
    private double[] authority;
    private double[] hubs;
    private boolean useUndirected;
    private double epsilon = 0.0001;
    private LinkedList<Node> hub_list;
    private LinkedList<Node> auth_list;
    private HashMap<Node, Integer> indicies;

    public Hits() {
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        if (graphController != null && graphController.getModel() != null) {
            useUndirected = graphController.getModel().isUndirected();
        }
    }

    public void setUndirected(boolean pUndirected) {
        useUndirected = pUndirected;
    }

    /**
     * 
     * @return
     */
    public boolean getUndirected() {
        return useUndirected;
    }

    public void execute(GraphModel graphModel, AttributeModel attributeModel) {
        HierarchicalGraph graph = null;
        if (useUndirected) {
            graph = graphModel.getHierarchicalUndirectedGraphVisible();
        } else {
            graph = graphModel.getHierarchicalDirectedGraphVisible();
        }
        execute(graph, attributeModel);
    }
    
   

    public void execute(HierarchicalGraph hgraph, AttributeModel attributeModel) {
        
        Map<String, AttributeColumn> attributeColumns = initializeAttributeColunms(attributeModel); 
        
        hgraph.readLock();       
        
        initializeStartValues(hgraph);
        
        calculateHits(hgraph, hubs, authority, hub_list, auth_list, indicies, !useUndirected, epsilon);
        
        saveCalculatedValues(hgraph, attributeColumns, indicies, authority, hubs);

        hgraph.readUnlockAll();
    }
    
    public void calculateHits(HierarchicalGraph hgraph, double[] hubValues, double[] authorityValues, 
            LinkedList<Node> hubList, LinkedList<Node> authList, HashMap<Node, Integer> indicies, 
            boolean isDirected, double eps) {
        
        int N = hgraph.getNodeCount();

        double[] temp_authority = new double[N];
        double[] temp_hubs = new double[N];

        Progress.start(progress);
       
        formHubsAndAuthoritiesLists(hgraph, hubList, authList, indicies, isDirected); 
        setInitialHubsAndAuthoritiesValues(hubList, authList, hubValues, authorityValues, indicies);

        while (true) {

            boolean done = true;
            
            double auth_sum = updateValues(hgraph, authList, authorityValues, temp_authority, hubValues, indicies, isDirected);            
            double hub_sum = updateValues(hgraph, hubList, hubValues, temp_hubs, authorityValues, indicies, isDirected);           
            
            normalizeValues(temp_authority, auth_sum);
            normalizeValues(temp_hubs, hub_sum);
            
            done = checkDiff(authorityValues, temp_authority, eps) && checkDiff(hubValues, temp_hubs, eps);
            
            System.arraycopy(temp_authority, 0, authorityValues, 0, N);
            System.arraycopy(temp_hubs, 0, hubValues, 0, N);
            
            temp_authority = new double[N];
            temp_hubs = new double[N];

            if ((done) || (isCanceled)) {
                break;
            }
        }
    }
    
    private Map<String, AttributeColumn> initializeAttributeColunms(AttributeModel attributeModel) {
         Map<String, AttributeColumn> attributeColumns = new HashMap<String, AttributeColumn>();
        
        AttributeTable nodeTable = attributeModel.getNodeTable();
        AttributeColumn authorityCol = nodeTable.getColumn(AUTHORITY);
        AttributeColumn hubsCol = nodeTable.getColumn(HUB);
        
        if (authorityCol == null) {
            authorityCol = nodeTable.addColumn(AUTHORITY, "Authority", AttributeType.FLOAT, AttributeOrigin.COMPUTED, new Float(0));
        }
        if (hubsCol == null) {
            hubsCol = nodeTable.addColumn(HUB, "Hub", AttributeType.FLOAT, AttributeOrigin.COMPUTED, new Float(0));
        }
        
        attributeColumns.put(AUTHORITY, authorityCol);
        attributeColumns.put(HUB, hubsCol);
        
        return attributeColumns;
    }
    
     private void initializeStartValues(HierarchicalGraph hgraph) {
        int N = hgraph.getNodeCount();
        authority = new double[N];
        hubs = new double[N];
        
        indicies = createIndiciesMap(hgraph);
        
        hub_list = new LinkedList<Node>();
        auth_list = new LinkedList<Node>();
    }
    
    private void formHubsAndAuthoritiesLists(HierarchicalGraph hgraph, LinkedList<Node> hubsList, LinkedList<Node> authoritiesList, 
            HashMap<Node, Integer> indicies, boolean isDirected) {
        
        for (Node node : hgraph.getNodes()) {

            if (isDirected) {
                if (((HierarchicalDirectedGraph) hgraph).getTotalOutDegree(node) > 0) {
                    hubsList.add(node);
                }
                if (((HierarchicalDirectedGraph) hgraph).getTotalInDegree(node) > 0) {
                    authoritiesList.add(node);
                }
            } else {
                if (((HierarchicalUndirectedGraph) hgraph).getTotalDegree(node) > 0) {
                    hubsList.add(node);
                    authoritiesList.add(node);
                }
            }
        }
    }
    
    private void setInitialHubsAndAuthoritiesValues(LinkedList<Node> hubsList, LinkedList<Node> authoritiesList, 
            double[] hubValues, double[] authValues,
            HashMap<Node, Integer> indicies) {
         for (Node node : hubsList) {
            int n_index = indicies.get(node);
            hubValues[n_index] = 1.0f;
         }
         for (Node node : authoritiesList) {
            int n_index = indicies.get(node);
            authValues[n_index] = 1.0f;
        }
        
    }
    
    double updateValues(HierarchicalGraph hgraph, LinkedList<Node> list, double[] oldValues, double[] newValues, 
            double[] otherValues, HashMap<Node, Integer> indicies, boolean isDirected) {
        
         double sum = 0;
         for (Node node : list) {

                int n_index = indicies.get(node);
                newValues[n_index] = oldValues[n_index];
                EdgeIterable edge_iter;
                if (isDirected) {
                    edge_iter = ((HierarchicalDirectedGraph) hgraph).getInEdgesAndMetaInEdges(node);
                } else {
                    edge_iter = ((HierarchicalUndirectedGraph) hgraph).getEdgesAndMetaEdges(node);
                }
                for (Edge edge : edge_iter) {
                    Node target = hgraph.getOpposite(node, edge);
                    int target_index = indicies.get(target);
                    newValues[n_index] += otherValues[target_index];
                }

                sum += newValues[n_index];
                if (isCanceled) {
                    break;
                }
            }
         
         return sum;
    }
    
    private void normalizeValues(double[] newValues, double sum) {
        if (sum == 0) return;
        
        for (int i=0; i<newValues.length; i++) {
            newValues[i] /= sum;
        }
       
    }
    
    private boolean checkDiff(double[] oldValues, double[] newValues, double epsilon) {
        
        for (int i=0; i<oldValues.length; i++) {
            if (oldValues[i]>0 && ((newValues[i] - oldValues[i]) / oldValues[i]) >= epsilon) {
                return false;
            }
        }
        return true;
    }
    
    public  HashMap<Node, Integer> createIndiciesMap(HierarchicalGraph hgraph) {
       HashMap<Node, Integer> newIndicies = new HashMap<Node, Integer>();
        int index = 0;
        for (Node s : hgraph.getNodes()) {
            newIndicies.put(s, index);
            index++;
        } 
        return newIndicies;
    }
    
    private void saveCalculatedValues(HierarchicalGraph hgraph, Map<String, AttributeColumn> attributeColumns, HashMap<Node, Integer> indicies,
            double[] nodeAuthority, double[] nodeHubs) {
        
        for (Node s : hgraph.getNodes()) {
            int s_index = indicies.get(s);
            AttributeRow row = (AttributeRow) s.getNodeData().getAttributes();
            
            AttributeColumn authorityCol=attributeColumns.get(AUTHORITY);
            AttributeColumn hubsCol=attributeColumns.get(HUB);
            
            row.setValue(authorityCol, (float) nodeAuthority[s_index]);
            row.setValue(hubsCol, (float) nodeHubs[s_index]);
        }
        
    }

    /**
     *
     * @return
     */
    public String getReport() {
        //distribution of hub values
        Map<Double, Integer> distHubs = new HashMap<Double, Integer>();
        for (Node node : hub_list) {
            int n_index = indicies.get(node);
            Double d = hubs[n_index];
            if (distHubs.containsKey(d)) {
                Integer v = distHubs.get(d);
                distHubs.put(d, v + 1);
            } else {
                distHubs.put(d, 1);
            }
        }

        //distribution of authority values
        Map<Double, Integer> distAuthorities = new HashMap<Double, Integer>();
        for (Node node : auth_list) {
            int n_index = indicies.get(node);
            Double d = authority[n_index];
            if (distAuthorities.containsKey(d)) {
                Integer v = distAuthorities.get(d);
                distAuthorities.put(d, v + 1);
            } else {
                distAuthorities.put(d, 1);
            }
        }

        //Distribution of hub series
        XYSeries dHubsSeries = ChartUtils.createXYSeries(distHubs, "Hubs");

        //Distribution of authority series
        XYSeries dAuthsSeries = ChartUtils.createXYSeries(distAuthorities, "Authority");

        XYSeriesCollection datasetHubs = new XYSeriesCollection();
        datasetHubs.addSeries(dHubsSeries);

        XYSeriesCollection datasetAuths = new XYSeriesCollection();
        datasetAuths.addSeries(dAuthsSeries);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Hubs Distribution",
                "Score",
                "Count",
                datasetHubs,
                PlotOrientation.VERTICAL,
                true,
                false,
                false);
        chart.removeLegend();
        ChartUtils.decorateChart(chart);
        ChartUtils.scaleChart(chart, dHubsSeries, true);
        String imageFile1 = ChartUtils.renderChart(chart, "hubs.png");

        JFreeChart chart2 = ChartFactory.createXYLineChart(
                "Authority Distribution",
                "Score",
                "Count",
                datasetAuths,
                PlotOrientation.VERTICAL,
                true,
                false,
                false);
        chart2.removeLegend();
        ChartUtils.decorateChart(chart2);
        ChartUtils.scaleChart(chart2, dAuthsSeries, true);
        String imageFile2 = ChartUtils.renderChart(chart2, "authorities.png");


        String report = "<HTML> <BODY> <h1> HITS Metric Report </h1>"
                + "<hr>"
                + "<br />"
                + "<h2> Parameters: </h2>  &#917; = " + this.epsilon
                + "<br /> <h2> Results: </h2><br />"
                + imageFile1 + "<br />" + imageFile2
                + "<br /><br />" + "<h2> Algorithm: </h2>"
                + "Jon M. Kleinberg, <i>Authoritative Sources in a Hyperlinked Environment</i>, in Journal of the ACM 46 (5): 604–632 (1999)<br />"
                + "</BODY> </HTML>";

        return report;
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

    /**
     *
     * @param eps
     */
    public void setEpsilon(double eps) {
        epsilon = eps;
    }

    /**
     *
     * @return
     */
    public double getEpsilon() {
        return epsilon;
    }
}
