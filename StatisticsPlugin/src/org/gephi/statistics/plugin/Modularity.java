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
import java.util.Random;
import java.util.Set;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.graph.api.GraphModel;
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

/**
 *
 * @author pjmcswee
 */
public class Modularity implements Statistics, LongTask {

    public static final String MODULARITY_CLASS = "modularity_class";
    private ProgressTicket progress;
    private boolean isCanceled;
    private CommunityStructure structure;
    private double modularity;
    private boolean isRandomized = false;

    public void setRandom(boolean isRandomized) {
        this.isRandomized = isRandomized;
    }

    public boolean getRandom() {
        return isRandomized;
    }

    public boolean cancel() {
        this.isCanceled = true;
        return true;
    }

    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progress = progressTicket;
    }

    class ModEdge {

        int source;
        int target;
        int weight;

        public ModEdge(int s, int t, int w) {
            source = s;
            target = t;
            weight = w;
        }
    }

    class CommunityStructure {

        HashMap<Community, Integer>[] nodeConnections;
        HashMap<Node, Integer> map;
        Community[] nodeCommunities;
        HierarchicalUndirectedGraph graph;
        double[] weights;
        double graphWeightSum;
        LinkedList<ModEdge>[] topology;
        LinkedList<Community> communities;
        int N;
        HashMap<Integer, Community> invMap;

        
        CommunityStructure(HierarchicalUndirectedGraph hgraph) {
            this.graph = hgraph;
            N = hgraph.getNodeCount();
            invMap = new HashMap<Integer, Community>();
            nodeConnections = new HashMap[N];
            nodeCommunities = new Community[N];
            map = new HashMap<Node, Integer>();
            topology = new LinkedList[N];
            communities = new LinkedList<Community>();
            int index = 0;
            weights = new double[N];
            for (Node node : hgraph.getNodes()) {
                map.put(node, index);
                nodeCommunities[index] = new Community(this);
                nodeConnections[index] = new HashMap<Community, Integer>();
                weights[index] = hgraph.getTotalDegree(node);
                nodeCommunities[index].seed(index);
                Community hidden = new Community(structure);
                hidden.nodes.add(index);
                invMap.put(index, hidden);
                communities.add(nodeCommunities[index]);               
                index++;
                if (isCanceled) {
                    return;
                }
            }

            for (Node node : hgraph.getNodes()) {
                int node_index = map.get(node);
                topology[node_index] = new LinkedList<ModEdge>();

                for (Node neighbor : hgraph.getNeighbors(node)) {
                    if (node == neighbor) {
                        continue;
                    }
                    int neighbor_index = map.get(neighbor);
                    ModEdge me = new ModEdge(node_index, neighbor_index, 1);
                    topology[node_index].add(me);
                    Community adjCom = nodeCommunities[neighbor_index];
                    nodeConnections[node_index].put(adjCom, 1);
                    nodeCommunities[node_index].connections.put(adjCom, 1);
                    nodeConnections[neighbor_index].put(nodeCommunities[node_index], 1);
                    nodeCommunities[neighbor_index].connections.put(nodeCommunities[node_index], 1);
                    graphWeightSum++;//WARNING : may be an issue with self_loop
                }

                if (isCanceled) {
                    return;
                }
            }
            graphWeightSum /= 2.0;
        }

        private void addNodeTo(int node, Community to) {
            to.add(new Integer(node));
            nodeCommunities[node] = to;

            for (ModEdge e : topology[node]) {
                int neighbor = e.target;

                ////////
                //Remove Node Connection to this community
                Integer neighEdgesTo = nodeConnections[neighbor].get(to);
                if (neighEdgesTo == null) {
                    nodeConnections[neighbor].put(to, e.weight);
                } else {
                    nodeConnections[neighbor].put(to, neighEdgesTo + e.weight);
                }




                ///////////////////
                Community adjCom = nodeCommunities[neighbor];
                Integer oEdgesto = adjCom.connections.get(to);
                if (oEdgesto == null) {
                    adjCom.connections.put(to, e.weight);
                } else {
                    adjCom.connections.put(to, oEdgesto + e.weight);
                }

                Integer nodeEdgesTo = nodeConnections[node].get(adjCom);
                if (nodeEdgesTo == null) {
                    nodeConnections[node].put(adjCom, e.weight);
                } else {
                    nodeConnections[node].put(adjCom, nodeEdgesTo + e.weight);
                }

                if (to != adjCom) {
                    Integer comEdgesto = to.connections.get(adjCom);
                    if (comEdgesto == null) {
                        to.connections.put(adjCom, e.weight);
                    } else {
                        to.connections.put(adjCom, comEdgesto + e.weight);
                    }
                }
            }
        }

        private void removeNodeFrom(int node, Community from) {
            Community community = nodeCommunities[node];
            for (ModEdge e : topology[node]) {
                int neighbor = e.target;

                ////////
                //Remove Node Connection to this community
                Integer edgesTo = nodeConnections[neighbor].get(community);
                if (edgesTo - e.weight == 0) {
                    nodeConnections[neighbor].remove(community);
                } else {
                    nodeConnections[neighbor].put(community, edgesTo - e.weight);
                }

                ///////////////////
                //Remove Adjacency Community's connetion to this community
                Community adjCom = nodeCommunities[neighbor];
                Integer oEdgesto = adjCom.connections.get(community);
                if (oEdgesto - e.weight == 0) {
                    adjCom.connections.remove(community);
                } else {
                    adjCom.connections.put(community, oEdgesto - e.weight);
                }

                if (node == neighbor) {
                    continue;
                }

                if (adjCom != community) {
                    Integer comEdgesto = community.connections.get(adjCom);
                    if (comEdgesto - e.weight == 0) {
                        community.connections.remove(adjCom);
                    } else {
                        community.connections.put(adjCom, comEdgesto - e.weight);
                    }
                }

                Integer nodeEgesTo = nodeConnections[node].get(adjCom);
                if (nodeEgesTo - e.weight == 0) {
                    nodeConnections[node].remove(adjCom);
                } else {
                    nodeConnections[node].put(adjCom, nodeEgesTo - e.weight);
                }


            }
            from.remove(new Integer(node));
        }

        private void moveNodeTo(int node, Community to) {
            Community from = nodeCommunities[node];
            removeNodeFrom(node, from);
            addNodeTo(node, to);
        }

        private void zoomOut() {
            int M = communities.size();
            LinkedList<ModEdge>[] newTopology = new LinkedList[M];
            int index = 0;
            nodeCommunities = new Community[M];
            nodeConnections = new HashMap[M];
            HashMap<Integer, Community> newInvMap = new HashMap<Integer, Community>();
            for (int i = 0; i < communities.size(); i++) {//Community com : mCommunities) {
                Community com = communities.get(i);
                nodeConnections[index] = new HashMap<Community, Integer>();
                newTopology[index] = new LinkedList<ModEdge>();
                nodeCommunities[index] = new Community(com);
                Set<Community> iter = com.connections.keySet();
                double weightSum = 0;

                Community hidden = new Community(structure);
                for (Integer nodeInt : com.nodes) {
                    Community oldHidden = invMap.get(nodeInt);
                    hidden.nodes.addAll(oldHidden.nodes);
                }
                newInvMap.put(index, hidden);
                for(Community adjCom : iter) {
                    int target = communities.indexOf(adjCom);
                    int weight = com.connections.get(adjCom);
                    if(target == index)
                        weightSum += 2*weight;
                    else
                        weightSum += weight;
                    ModEdge e = new ModEdge(index, target, weight);
                    newTopology[index].add(e);
                }
                weights[index] = weightSum;
                nodeCommunities[index].seed(index);

                index++;
            }
            communities.clear();

            for (int i = 0; i < M; i++) {
                Community com = nodeCommunities[i];
                communities.add(com);
                for (ModEdge e : newTopology[i]) {
                    nodeConnections[i].put(nodeCommunities[e.target], e.weight);
                    com.connections.put(nodeCommunities[e.target], e.weight);
                }

            }

            N = M;
            topology = newTopology;
            invMap = newInvMap;
        }
    }

    class Community {
        double weightSum;
        CommunityStructure structure;
        LinkedList<Integer> nodes;
        HashMap<Community, Integer> connections;

        public int size() {
            return nodes.size();
        }

        public Community(Community com) {
            structure = com.structure;
            connections = new HashMap<Community, Integer>();
            nodes = new LinkedList<Integer>();
            //mHidden = pCom.mHidden;
        }

        public Community(CommunityStructure structure) {
            this.structure = structure;
            connections = new HashMap<Community, Integer>();
            nodes = new LinkedList<Integer>();
        }

        public void seed(int node) {
            nodes.add(node);
            weightSum += structure.weights[node];
        }

        public boolean add(int node) {
            nodes.addLast(new Integer(node));
            weightSum += structure.weights[node];
            return true;
        }

        public boolean remove(int node) {
            boolean result = nodes.remove(new Integer(node));
            weightSum -= structure.weights[node];
            if (nodes.size() == 0) {
                structure.communities.remove(this);
            }
            return result;
        }
    }

    public void execute(GraphModel graphModel, AttributeModel attributeModel) {
        HierarchicalUndirectedGraph hgraph = graphModel.getHierarchicalUndirectedGraphVisible();
        execute(hgraph, attributeModel);
    }

    public void execute(HierarchicalUndirectedGraph hgraph, AttributeModel attributeModel) {
        isCanceled = false;
        Progress.start(progress);
        Random rand = new Random();
        hgraph.readLock();
        structure = new CommunityStructure(hgraph);
        if (isCanceled) {
            hgraph.readUnlockAll();
            return;
        }
        boolean someChange = true;
        while (someChange) {
            someChange = false;
            boolean localChange = true;
            while (localChange) {
                localChange = false;
                int start = 0;
                if (isRandomized) {
                    start = Math.abs(rand.nextInt()) % structure.N;
                }
                int step = 0;
                for (int i = start; step < structure.N; i = (i + 1) % structure.N) {
                    step++;
                    double best = 0.;
                    Community bestCommunity = null;
                    Community nodecom = structure.nodeCommunities[i];
                    Set<Community> iter = structure.nodeConnections[i].keySet();
                    for(Community com : iter) {
                        double qValue = q(i, com);
                        if (qValue > best) {
                            best = qValue;
                            bestCommunity = com;
                        } 
                    }
                    if ((structure.nodeCommunities[i] != bestCommunity) && (bestCommunity != null)) {
                        structure.moveNodeTo(i, bestCommunity);
                        localChange = true;
                    }
                    if (isCanceled) {
                        hgraph.readUnlockAll();
                        return;
                    }
                }
                someChange = localChange || someChange;
                if (isCanceled) {
                    hgraph.readUnlockAll();
                    return;
                }
            }

            if (someChange) {
                structure.zoomOut();
            }
        }

        int[] comStructure = new int[hgraph.getNodeCount()];
        int count = 0;
        double[] degreeCount = new double[structure.communities.size()];
        for (Community com : structure.communities) {
            for (Integer node : com.nodes) {
                Community hidden = structure.invMap.get(node);
                for (Integer nodeInt : hidden.nodes) {
                    comStructure[nodeInt] = count;
                }
            }
            count++;
        }
        for (Node node : hgraph.getNodes()) {
            int index = structure.map.get(node);
            degreeCount[comStructure[index]] += hgraph.getTotalDegree(node);
        }

        modularity = finalQ(comStructure, degreeCount, hgraph, attributeModel);

        hgraph.readUnlock();
    }

    private double finalQ(int[] struct, double[] degrees, HierarchicalUndirectedGraph hgraph, AttributeModel attributeModel) {
        AttributeTable nodeTable = attributeModel.getNodeTable();
        AttributeColumn modCol = nodeTable.getColumn(MODULARITY_CLASS);
        if (modCol == null) {
            modCol = nodeTable.addColumn(MODULARITY_CLASS, "Modularity Class", AttributeType.INT, AttributeOrigin.COMPUTED, new Integer(0));
        }

        double res = 0;
        double[] internal = new double[degrees.length];
        for (Node n : hgraph.getNodes()) {
            int n_index = structure.map.get(n);
            AttributeRow row = (AttributeRow) n.getNodeData().getAttributes();
            row.setValue(modCol, struct[n_index]);
            for (Node neighbor : hgraph.getNeighbors(n)) {
                if (n == neighbor) {
                    continue;
                }
                int neigh_index = structure.map.get(neighbor);
                if (struct[neigh_index] == struct[n_index]) {
                    internal[struct[neigh_index]]++;
                }
            }
        }
        for (int i = 0; i < degrees.length; i++) {
            internal[i] /= 2.0;
            res += (internal[i] / hgraph.getTotalEdgeCount()) - Math.pow(degrees[i] / (2 * hgraph.getTotalEdgeCount()), 2);
        }
        return res;
    }

    public double getModularity() {
        return modularity;
    }

    public String getReport() {
        //Distribution series
        Map<Integer, Integer> sizeDist = new HashMap<Integer, Integer>();
        for(Node n : structure.graph.getNodes()) {
            Integer v = (Integer) n.getNodeData().getAttributes().getValue(MODULARITY_CLASS);
            if(!sizeDist.containsKey(v)) {
                sizeDist.put(v, 0);
            }
            sizeDist.put(v, sizeDist.get(v) + 1);
        }
        
        XYSeries dSeries = ChartUtils.createXYSeries(sizeDist, "Size Distribution");

        XYSeriesCollection dataset1 = new XYSeriesCollection();
        dataset1.addSeries(dSeries);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Size Distribution",
                "Modularity Class",
                "Size (number of nodes)",
                dataset1,
                PlotOrientation.VERTICAL,
                true,
                false,
                false);
        chart.removeLegend();
        ChartUtils.decorateChart(chart);
        ChartUtils.scaleChart(chart, dSeries, false);
        String imageFile = ChartUtils.renderChart(chart, "communities-size-distribution.png");

        
        NumberFormat f = new DecimalFormat("#0.000");

        String report = "<HTML> <BODY> <h1>Modularity Report </h1> "
                + "<hr>"
                + "<h2> Parameters: </h2>"
                + "Randomize:  " + (isRandomized ? "On" : "Off") + "<br>"
                + "<br> <h2> Results: </h2>"
                + "Modularity: " + f.format(modularity) + "<br>"
                + "Number of Communities: " + structure.communities.size()
                + "<br /><br />"+imageFile
                + "<br /><br />" + "<h2> Algorithm: </h2>"
                + "Vincent D Blondel, Jean-Loup Guillaume, Renaud Lambiotte, Etienne Lefebvre, <i>Fast unfolding of communities in large networks</i>, in Journal of Statistical Mechanics: Theory and Experiment 2008 (10), P1000<br />"
                + "</BODY> </HTML>";

        return report;
    }

    private double q(int node, Community community) {

        Integer edgesToInt = structure.nodeConnections[node].get(community);
        double edgesTo = 0;
        if (edgesToInt != null) {
            edgesTo = edgesToInt.doubleValue();
        }
        double weightSum = community.weightSum;
        double nodeWeight = structure.weights[node];
        double qValue = edgesTo - (nodeWeight * weightSum) / (2.0 * structure.graphWeightSum);
        if ((structure.nodeCommunities[node] == community) && (structure.nodeCommunities[node].size() > 1)) {
            qValue = edgesTo - (nodeWeight * (weightSum - nodeWeight)) / (2.0 * structure.graphWeightSum);
        }
        if ((structure.nodeCommunities[node] == community) && (structure.nodeCommunities[node].size() == 1)) {
            qValue = 0.;
        }
        return qValue;
    }
}
