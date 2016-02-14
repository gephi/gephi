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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.graph.api.Table;
import org.gephi.graph.api.UndirectedGraph;
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
public class ConnectedComponents implements Statistics, LongTask {

    public static final String WEAKLY = "componentnumber";
    public static final String STRONG = "strongcompnum";
    private boolean isDirected;
    private ProgressTicket progress;
    private boolean isCanceled;
    private int componentCount;
    private int stronglyCount;
    private int[] componentsSize;
    int count;

    public ConnectedComponents() {
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        if (graphController != null && graphController.getGraphModel() != null) {
            isDirected = graphController.getGraphModel().isDirected();
        }
    }

    @Override
    public void execute(GraphModel graphModel) {
        isDirected = graphModel.isDirected();
        isCanceled = false;

        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraphVisible();

        undirectedGraph.readLock();

        weaklyConnected(undirectedGraph);
        if (isDirected) {
            DirectedGraph directedGraph = graphModel.getDirectedGraphVisible();
            stronglyConnected(directedGraph, graphModel);
        }

        undirectedGraph.readUnlock();
    }

    public void weaklyConnected(UndirectedGraph graph) {
        isCanceled = false;

        Column componentCol = initializeWeeklyConnectedColumn(graph.getModel());

        HashMap<Node, Integer> indicies = createIndiciesMap(graph);

        LinkedList<LinkedList<Node>> components = computeWeeklyConnectedComponents(graph, indicies);

        saveComputedComponents(components, componentCol);

        fillComponentSizeList(components);

        componentCount = components.size();
    }

    public LinkedList<LinkedList<Node>> computeWeeklyConnectedComponents(Graph graph, HashMap<Node, Integer> indicies) {

        int N = graph.getNodeCount();

        //Keep track of which nodes have been seen
        int[] color = new int[N];

        Progress.start(progress, N);

        int seenCount = 0;

        LinkedList<LinkedList<Node>> components = new LinkedList<>();
        while (seenCount < N) {
            //The search Q
            LinkedList<Node> Q = new LinkedList<>();
            //The component-list
            LinkedList<Node> component = new LinkedList<>();

            //Seed the seach Q
            NodeIterable iter = graph.getNodes();
            for (Node first : iter) {
                if (color[indicies.get(first)] == 0) {
                    Q.add(first);
                    iter.doBreak();
                    break;
                }
            }

            //While there are more nodes to search
            while (!Q.isEmpty()) {
                if (isCanceled) {
                    graph.readUnlock();
                    return new LinkedList<>();
                }
                //Get the next Node and add it to the component list
                Node u = Q.removeFirst();
                component.add(u);

                //Iterate over all of u's neighbors
                EdgeIterable edgeIter = graph.getEdges(u);

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

                        Progress.progress(progress, seenCount);
                    }
                }
                color[indicies.get(u)] = 2;
                seenCount++;
            }
            components.add(component);
        }
        return components;
    }

    private Column initializeWeeklyConnectedColumn(GraphModel graphModel) {
        Table nodeTable = graphModel.getNodeTable();
        Column componentCol = nodeTable.getColumn(WEAKLY);
        if (componentCol == null) {
            componentCol = nodeTable.addColumn(WEAKLY, "Component ID", Integer.class, new Integer(0));
        }
        return componentCol;
    }

    public HashMap<Node, Integer> createIndiciesMap(Graph hgraph) {
        HashMap<Node, Integer> indicies = new HashMap<>();
        int index = 0;
        for (Node s : hgraph.getNodes()) {
            indicies.put(s, index);
            index++;
        }
        return indicies;
    }

    private void saveComputedComponents(LinkedList<LinkedList<Node>> components, Column componentCol) {
        int i = 0;
        for (LinkedList<Node> component : components) {
            for (Node s : component) {
                s.setAttribute(componentCol, i);
            }
            i++;
        }
    }

    void fillComponentSizeList(LinkedList<LinkedList<Node>> components) {
        componentsSize = new int[components.size()];
        for (int i = 0; i < components.size(); i++) {
            componentsSize[i] = components.get(i).size();
        }
    }

    private Column initializeStronglyConnectedColumn(GraphModel graphModel) {
        Table nodeTable = graphModel.getNodeTable();
        Column componentCol = nodeTable.getColumn(STRONG);
        if (componentCol == null) {
            componentCol = nodeTable.addColumn(STRONG, "Strongly-Connected ID", Integer.class, new Integer(0));
        }
        return componentCol;
    }

    public void stronglyConnected(DirectedGraph hgraph, GraphModel graphModel) {
        count = 1;
        stronglyCount = 0;

        Column componentCol = initializeStronglyConnectedColumn(graphModel);

        HashMap<Node, Integer> indicies = createIndiciesMap(hgraph);

        LinkedList<LinkedList<Node>> components = top_tarjans(hgraph, indicies);

        saveComputedComponents(components, componentCol);

        stronglyCount = components.size();
    }

    public LinkedList<LinkedList<Node>> top_tarjans(DirectedGraph graph, HashMap<Node, Integer> indicies) {

        LinkedList<LinkedList<Node>> allComponents = new LinkedList<>();

        count = 1;
        stronglyCount = 0;

        int N = graph.getNodeCount();
        int[] index = new int[N];
        int[] low_index = new int[N];

        while (true) {
            //The search Q
            LinkedList<Node> S = new LinkedList<>();
            //The component-list
            //LinkedList<Node> component = new LinkedList<Node>();
            //Seed the seach Q
            Node first = null;
            NodeIterable iter = graph.getNodes();
            for (Node u : iter) {
                if (index[indicies.get(u)] == 0) {
                    first = u;
                    iter.doBreak();
                    break;
                }
            }
            if (first == null) {
                return allComponents;
            }

            LinkedList<LinkedList<Node>> components = new LinkedList<>();
            components = tarjans(components, S, graph, first, index, low_index, indicies);
            for (LinkedList<Node> component : components) {
                allComponents.add(component);
            }
        }
    }

    private LinkedList<LinkedList<Node>> tarjans(LinkedList<LinkedList<Node>> components, LinkedList<Node> S, DirectedGraph graph, Node f, int[] index, int[] low_index, HashMap<Node, Integer> indicies) {
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
                tarjans(components, S, graph, u, index, low_index, indicies);
                low_index[id] = Math.min(low_index[x], low_index[id]);
            } else if (S.contains(u)) {
                low_index[id] = Math.min(low_index[id], index[x]);
            }
        }
        LinkedList<Node> currentComponent = new LinkedList<>();
        if (low_index[id] == index[id]) {
            Node v = null;
            while (v != f) {
                v = S.removeFirst();
                currentComponent.add(v);
            }
            components.add(currentComponent);
        }
        return components;
    }

    public int getConnectedComponentsCount() {
        return componentCount;
    }

    public void setDirected(boolean isDirected) {
        this.isDirected = isDirected;
    }

    public boolean isDirected() {
        return isDirected;
    }

    /**
      * @return an unordered array of component sizes
      */
    public int[] getComponentsSize() {
        return componentsSize;
    }

    /**
      * @return the index of the largest component in the array returned by getComponentSize()
      */
    public int getGiantComponent() {
        int[] sizes = getComponentsSize();
        int max = Integer.MIN_VALUE;
        int maxIndex = -1;
        for (int i = 0; i < sizes.length; i++) {
            if (sizes[i] > max) {
                max = sizes[i];
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    public int getComponentNumber(LinkedList<LinkedList<Node>> components, Node node) {
        int i = 0;
        for (LinkedList<Node> component : components) {
            for (Node currentNode : component) {
                if (currentNode.equals(node)) {
                    return i;
                }
            }
            i++;
        }
        return 0;
    }

    @Override
    public String getReport() {
        Map<Integer, Integer> sizeDist = new HashMap<>();
        for (int v : componentsSize) {
            if (!sizeDist.containsKey(v)) {
                sizeDist.put(v, 0);
            }
            sizeDist.put(v, sizeDist.get(v) + 1);
        }

        //Distribution series
        XYSeries dSeries = ChartUtils.createXYSeries(sizeDist, "Size Distribution");

        XYSeriesCollection dataset1 = new XYSeriesCollection();
        dataset1.addSeries(dSeries);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Size Distribution",
                "Size (number of nodes)",
                "Count",
                dataset1,
                PlotOrientation.VERTICAL,
                true,
                false,
                false);
        chart.removeLegend();
        ChartUtils.decorateChart(chart);
        ChartUtils.scaleChart(chart, dSeries, false);
        String imageFile = ChartUtils.renderChart(chart, "cc-size-distribution.png");

        NumberFormat f = new DecimalFormat("#0.000");

        String report = "<HTML> <BODY> <h1>Connected Components Report </h1> "
                + "<hr>"
                + "<br>"
                + "<h2> Parameters: </h2>"
                + "Network Interpretation:  " + (isDirected ? "directed" : "undirected") + "<br>"
                + "<br> <h2> Results: </h2>"
                + "Number of Weakly Connected Components: " + componentCount + "<br>"
                + (isDirected ? "Number of Stronlgy Connected Components: " + stronglyCount + "<br>" : "")
                + "<br /><br />" + imageFile
                + "<br />" + "<h2> Algorithm: </h2>"
                + "Robert Tarjan, <i>Depth-First Search and Linear Graph Algorithms</i>, in SIAM Journal on Computing 1 (2): 146–160 (1972)<br />"
                + "</BODY> </HTML>";

        return report;
    }

    @Override
    public boolean cancel() {
        isCanceled = true;
        return true;
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        progress = progressTicket;
    }
}
