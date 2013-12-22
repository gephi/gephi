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
import java.util.Map;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeTable;
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
 *
 * @author pjmcswee
 */
public class EigenvectorCentrality implements Statistics, LongTask {

    public static final String EIGENVECTOR = "eigencentrality";
    private int numRuns = 100;
    private double[] centralities;
    private double sumChange;
    private ProgressTicket progress;
    /** */
    private boolean isCanceled;
    private boolean isDirected;

    public EigenvectorCentrality() {
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        if (graphController != null && graphController.getModel() != null) {
            isDirected = graphController.getModel().isDirected();
        }
    }

    public void setNumRuns(int numRuns) {
        this.numRuns = numRuns;
    }

    /**
     * 
     * @return
     */
    public int getNumRuns() {
        return numRuns;
    }

    /**
     * 
     * @return
     */
    public boolean isDirected() {
        return isDirected;
    }

    /**
     * 
     * @param isDirected
     */
    public void setDirected(boolean isDirected) {
        this.isDirected = isDirected;
    }

    /**
     *
     * @param graphModel
     * @param attributeModel
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

        Map<String, AttributeColumn> attributeColumns = initializeAttributeColunms(attributeModel); 

        int N = hgraph.getNodeCount();
        hgraph.readLock();
        
        centralities = new double[N];

        Progress.start(progress, numRuns);
        
        HashMap<Integer, Node> indicies = new HashMap<Integer, Node>();
        HashMap<Node, Integer> invIndicies = new HashMap<Node, Integer>();
        fillIndiciesMaps(hgraph, centralities, indicies, invIndicies);
        
        sumChange = calculateEigenvectorCentrality(hgraph, centralities, indicies, invIndicies, isDirected, numRuns);

        saveCalculatedValues(hgraph, attributeColumns, indicies, centralities);

        hgraph.readUnlock();

        Progress.finish(progress);
    }
    
    private Map<String, AttributeColumn> initializeAttributeColunms(AttributeModel attributeModel) {
         Map<String, AttributeColumn> attributeColumns = new HashMap<String, AttributeColumn>();
        
        AttributeTable nodeTable = attributeModel.getNodeTable();
        AttributeColumn eigenCol = nodeTable.getColumn(EIGENVECTOR);
        
        if (eigenCol == null) {
            eigenCol = nodeTable.addColumn(EIGENVECTOR, "Eigenvector Centrality", AttributeType.DOUBLE, AttributeOrigin.COMPUTED, new Double(0));
        }
        
        attributeColumns.put(EIGENVECTOR, eigenCol);
        
        return attributeColumns;
    }
    
    private void saveCalculatedValues(HierarchicalGraph hgraph, Map<String, AttributeColumn> attributeColumns, HashMap<Integer, Node> indicies,
            double[] eigCenrtalities) {   
        
         int N = hgraph.getNodeCount();
         
         for (int i = 0; i < N; i++) {
            Node s = indicies.get(i);
            AttributeRow row = (AttributeRow) s.getNodeData().getAttributes();
            
            AttributeColumn eigenCol=attributeColumns.get(EIGENVECTOR);
            
            row.setValue(eigenCol, eigCenrtalities[i]);
        }
    }
    
    public void fillIndiciesMaps(HierarchicalGraph hgraph, double[] eigCentralities, HashMap<Integer, Node> indicies, HashMap<Node, Integer> invIndicies) {
        if (indicies==null || invIndicies==null) return;
        
        int count = 0;
        for (Node u : hgraph.getNodes()) {
            indicies.put(count, u);
            invIndicies.put(u, count);
            eigCentralities[count] = 1;
            count++;
        }
    }
    
    private double computeMaxValueAndTempValues(HierarchicalGraph hgraph, HashMap<Integer, Node> indicies, HashMap<Node, Integer> invIndicies,
            double[] tempValues, double[] centralityValues, boolean directed) {
        
        double max=0.;
        int N = hgraph.getNodeCount();
        
        for (int i = 0; i < N; i++) {
                Node u = indicies.get(i);
                EdgeIterable iter = null;
                if (directed) {
                    iter = ((HierarchicalDirectedGraph) hgraph).getInEdgesAndMetaInEdges(u);
                } else {
                    iter = ((HierarchicalUndirectedGraph) hgraph).getEdgesAndMetaEdges(u);
                }

                for (Edge e : iter) {
                    Node v = hgraph.getOpposite(u, e);
                    Integer id = invIndicies.get(v);
                    tempValues[i] += centralityValues[id];
                }
                max = Math.max(max, tempValues[i]);
                if (isCanceled) {
                    return max;
                }
            }
        
        return max;
    }
    
    private double updateValues(HierarchicalGraph hgraph, double[] tempValues, double[] centralityValues, double max) {
        double sumChanged = 0.;
        int N = hgraph.getNodeCount();
        
        for (int k = 0; k < N; k++) {
                if (max != 0) {
                    sumChanged += Math.abs(centralityValues[k] - (tempValues[k] / max));
                    centralityValues[k] = tempValues[k] / max;
                }
                if (isCanceled) {
                    return sumChanged;
                }
            }
        
        return sumChanged;
    }
    
    public double calculateEigenvectorCentrality(HierarchicalGraph hgraph, double[] eigCentralities, 
            HashMap<Integer, Node> indicies, HashMap<Node, Integer> invIndicies,
            boolean directed, int numIterations) {
        
        int N = hgraph.getNodeCount();
        double sumChanged = 0.;
        double[] tmp = new double[N];
        
        for (int s = 0; s < numIterations; s++) {
            double max = computeMaxValueAndTempValues(hgraph, indicies, invIndicies, tmp, eigCentralities, directed);
            sumChanged = updateValues(hgraph, tmp, eigCentralities, max);
            if (isCanceled) {
                return sumChanged;
            }

            Progress.progress(progress);
        } 
        
        return sumChanged;
    }

    /**
     * 
     * @return
     */
    public String getReport() {
        //distribution of values
        Map<Double, Integer> dist = new HashMap<Double, Integer>();
        for (int i = 0; i < centralities.length; i++) {
            Double d = centralities[i];
            if (dist.containsKey(d)) {
                Integer v = dist.get(d);
                dist.put(d, v + 1);
            } else {
                dist.put(d, 1);
            }
        }

        //Distribution series
        XYSeries dSeries = ChartUtils.createXYSeries(dist, "Eigenvector Centralities");

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(dSeries);

        JFreeChart chart = ChartFactory.createScatterPlot(
                "Eigenvector Centrality Distribution",
                "Score",
                "Count",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                false,
                false);
        chart.removeLegend();
        ChartUtils.decorateChart(chart);
        ChartUtils.scaleChart(chart, dSeries, true);
        String imageFile = ChartUtils.renderChart(chart, "eigenvector-centralities.png");
        
        String report = "<HTML> <BODY> <h1>Eigenvector Centrality Report</h1> "
                + "<hr>"
                + "<h2> Parameters: </h2>"
                + "Network Interpretation:  " + (isDirected ? "directed" : "undirected") + "<br>"
                + "Number of iterations: " + numRuns + "<br>"
                + "Sum change: " + sumChange
                + "<br> <h2> Results: </h2>"
                + imageFile
                + "</BODY></HTML>";

        return report;

    }

    public boolean cancel() {
        this.isCanceled = true;
        return true;
    }

    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progress = progressTicket;

    }
}
