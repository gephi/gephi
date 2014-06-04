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
import org.gephi.attribute.api.AttributeModel;
import org.gephi.attribute.api.Column;
import org.gephi.attribute.api.Table;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
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
 * Ref: Jon M. Kleinberg, Authoritative Sources in a Hyperlinked Environment, in
 * Journal of the ACM 46 (5): 604–632 (1999)
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

    public Hits() {
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        if (graphController != null && graphController.getGraphModel() != null) {
            useUndirected = graphController.getGraphModel().isUndirected();
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

    @Override
    public void execute(GraphModel graphModel, AttributeModel attributeModel) {
        Graph graph = null;
        if (useUndirected) {
            graph = graphModel.getUndirectedGraphVisible();
        } else {
            graph = graphModel.getDirectedGraphVisible();
        }
        execute(graph, attributeModel);
    }

    public void execute(Graph hgraph, AttributeModel attributeModel) {

        initializeAttributeColunms(attributeModel);

        hgraph.readLock();

        int N = hgraph.getNodeCount();
        authority = new double[N];
        hubs = new double[N];

        Map<Node, Integer> indicies = createIndiciesMap(hgraph);

        calculateHits(hgraph, hubs, authority, indicies, !useUndirected, epsilon);

        saveCalculatedValues(hgraph, authority, hubs);

        hgraph.readUnlockAll();
    }

    public void calculateHits(Graph hgraph, double[] hubValues, double[] authorityValues, Map<Node, Integer> indicies, boolean isDirected, double eps) {

        int N = hgraph.getNodeCount();

        double[] temp_authority = new double[N];
        double[] temp_hubs = new double[N];

        initializeStartValues(hubValues, authorityValues);
        
        Progress.start(progress);

        while (true) {

            boolean done = true;

            updateAutorithy(hgraph, temp_authority, hubValues, isDirected, indicies);
            updateHub(hgraph, temp_hubs, temp_authority, isDirected, indicies);

            done = checkDiff(authorityValues, temp_authority, eps) && checkDiff(hubValues, temp_hubs, eps);

            System.arraycopy(temp_authority, 0, authorityValues, 0, N);
            System.arraycopy(temp_hubs, 0, hubValues, 0, N);

//            temp_authority = new double[N];
//            temp_hubs = new double[N]

            if ((done) || (isCanceled)) {
                break;
            }
        }
    }

    private void initializeAttributeColunms(AttributeModel attributeModel) {
        Table nodeTable = attributeModel.getNodeTable();

        if (!nodeTable.hasColumn(AUTHORITY)) {
            nodeTable.addColumn(AUTHORITY, "Authority", Float.class, new Float(0));
        }
        if (!nodeTable.hasColumn(HUB)) {
            nodeTable.addColumn(HUB, "Hub", Float.class, new Float(0));
        }
    }

    private void initializeStartValues(double[] hubValues, double[] authorityValues) {
        for (int i = 0; i < authorityValues.length; i++) {
            authorityValues[i] = 1.0;
            hubValues[i] = 1.0;
        }
    }

    void updateAutorithy(Graph hgraph, double[] newValues, double[] hubValues, boolean isDirected, Map<Node, Integer> indicies) {
        double norm = 0;
        int j = 0;
        for (Node node : hgraph.getNodes()) {
            double auth = 0;
            EdgeIterable edge_iter;
            if (isDirected) {
                edge_iter = ((DirectedGraph) hgraph).getInEdges(node);
            } else {
                edge_iter = hgraph.getEdges(node);
            }
            for (Edge edge : edge_iter) {
                Node target = hgraph.getOpposite(node, edge);
                auth += hubValues[indicies.get(target)];
            }
            if (auth > 0) {
                newValues[j] = auth;
            }
            norm += newValues[j++];
            if (isCanceled) {
                return;
            }
        }
//        norm = Math.sqrt(norm);
        if (norm > 0) {
            for (int i = 0; i < newValues.length; i++) {
                newValues[i] = newValues[i] / norm;
            }
        }
    }

    void updateHub(Graph hgraph, double[] newValues, double[] authValues, boolean isDirected, Map<Node, Integer> indicies) {
        double norm = 0;
        int j = 0;
        for (Node node : hgraph.getNodes()) {
            double hub = 0;
            EdgeIterable edge_iter;
            if (isDirected) {
                edge_iter = ((DirectedGraph) hgraph).getOutEdges(node);
            } else {
                edge_iter = hgraph.getEdges(node);
            }
            for (Edge edge : edge_iter) {
                Node target = hgraph.getOpposite(node, edge);
                hub += authValues[indicies.get(target)];
            }
            if(hub > 0) {
                newValues[j] = hub;
            }
            norm += newValues[j++];
            if (isCanceled) {
                return;
            }
        }
        if (norm > 0) {
            for (int i = 0; i < newValues.length; i++) {
                newValues[i] = newValues[i] / norm;
            }
        }
    }

    private boolean checkDiff(double[] oldValues, double[] newValues, double epsilon) {

        for (int i = 0; i < oldValues.length; i++) {
            if (oldValues[i] > 0 && ((newValues[i] - oldValues[i]) / oldValues[i]) >= epsilon) {
                return false;
            }
        }
        return true;
    }

    private void saveCalculatedValues(Graph hgraph, double[] nodeAuthority, double[] nodeHubs) {
        int i = 0;
        for (Node s : hgraph.getNodes()) {
            int s_index = i++;

            s.setAttribute(AUTHORITY, (float) nodeAuthority[s_index]);
            s.setAttribute(HUB, (float) nodeHubs[s_index]);
        }
    }

    public HashMap<Node, Integer> createIndiciesMap(Graph hgraph) {
        HashMap<Node, Integer> newIndicies = new HashMap<Node, Integer>();
        int index = 0;
        for (Node s : hgraph.getNodes()) {
            newIndicies.put(s, index);
            index++;
        }
        return newIndicies;
    }

    /**
     *
     * @return
     */
    @Override
    public String getReport() {
        //distribution of hub values
        Map<Double, Integer> distHubs = new HashMap<Double, Integer>();
        for (int i = 0; i < hubs.length; i++) {
            Double d = hubs[i];
            if (distHubs.containsKey(d)) {
                Integer v = distHubs.get(d);
                distHubs.put(d, v + 1);
            } else {
                distHubs.put(d, 1);
            }
        }

        //distribution of authority values
        Map<Double, Integer> distAuthorities = new HashMap<Double, Integer>();
        for (int i = 0; i < authority.length; i++) {
            Double d = authority[i];
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
    @Override
    public boolean cancel() {
        isCanceled = true;
        return true;
    }

    /**
     *
     * @param progressTicket
     */
    @Override
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
