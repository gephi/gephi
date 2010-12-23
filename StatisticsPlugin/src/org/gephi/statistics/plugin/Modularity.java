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

import java.util.HashMap;
import java.util.LinkedList;
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

    /**
     *
     * @param isRandomized
     */
    public void setRandom(boolean isRandomized) {
        this.isRandomized = isRandomized;
    }

    /**
     * 
     * @return
     */
    public boolean getRandom() {
        return isRandomized;
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

    /**
     *
     */
    class ModEdge {

        int source;
        int target;
        int weight;

        /**
         * 
         * @param s
         * @param t
         * @param w
         */
        public ModEdge(int s, int t, int w) {
            source = s;
            target = t;
            weight = w;
        }
    }

    class CommunityStructure {

        /**   */
        HashMap<Community, Integer>[] nodeConnections;
        /** */
        HashMap<Node, Integer> map;
        /**   */
        Community[] nodeCommunities;
        /** */
        HierarchicalUndirectedGraph graph;
        /** */
        double[] weights;
        double graphWeightSum;
        LinkedList<ModEdge>[] topology;
        LinkedList<Community> communities;
        int N;
        HashMap<Integer, Community> invMap;

        /**
         * 
         * @param graph
         */
        CommunityStructure(HierarchicalUndirectedGraph graph) {
            this.graph = graph;
            N = graph.getNodeCount();
            invMap = new HashMap<Integer, Community>();
            nodeConnections = new HashMap[N];
            nodeCommunities = new Community[N];
            map = new HashMap<Node, Integer>();
            topology = new LinkedList[N];
            communities = new LinkedList<Community>();
            int index = 0;
            weights = new double[N];
            for (Node node : graph.getNodes()) {
                map.put(node, index);
                nodeCommunities[index] = new Community(this);
                nodeConnections[index] = new HashMap<Community, Integer>();
                weights[index] = graph.getTotalDegree(node);
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

            for (Node node : graph.getNodes()) {
                int node_index = map.get(node);
                topology[node_index] = new LinkedList<ModEdge>();

                for (Node neighbor : graph.getNeighbors(node)) {
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
                    graphWeightSum++;
                }

                if (isCanceled) {
                    return;
                }
            }
            graphWeightSum /= 2.0;
        }

        /**
         *
         * @param node
         * @param to
         */
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

        /**
         * 
         * @param node
         * @param from
         */
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

        /**
         *
         * @param node
         * @param to
         */
        public void moveNodeTo(int node, Community to) {
            Community from = nodeCommunities[node];
            removeNodeFrom(node, from);
            addNodeTo(node, to);
        }

        /**
         *
         * @return
         */
        public void zoomOut() {
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

    /**
     *
     */
    class Community {

        double weightSum;
        CommunityStructure structure;
        LinkedList<Integer> nodes;
        HashMap<Community, Integer> connections;
        Integer min;

        /**
         * 
         * @return
         */
        public int size() {
            return nodes.size();
        }

        /**
         * 
         * @param com
         */
        public Community(Community com) {
            structure = com.structure;
            connections = new HashMap<Community, Integer>();
            nodes = new LinkedList<Integer>();
            min = Integer.MAX_VALUE;
            //mHidden = pCom.mHidden;
        }

        /**
         * 
         * @param structure
         */
        public Community(CommunityStructure structure) {
            this.structure = structure;
            connections = new HashMap<Community, Integer>();
            nodes = new LinkedList<Integer>();
        }

        /**
         * 
         * @param node
         */
        public void seed(int node) {
            nodes.add(node);
            weightSum += structure.weights[node];
            min = node;
        }

        /**
         *
         * @param node
         * @return
         */
        public boolean add(int node) {
            nodes.addLast(new Integer(node));
            weightSum += structure.weights[node];
            if (!isRandomized) {
                min = Math.min(node, min);
            }
            return true;
        }

        /**
         * 
         * @param node
         * @return
         */
        public boolean remove(int node) {
            boolean result = nodes.remove(new Integer(node));
            weightSum -= structure.weights[node];
            if (nodes.size() == 0) {
                structure.communities.remove(this);
            }
            if (!isRandomized) {
                if (node == min.intValue()) {
                    min = Integer.MAX_VALUE;
                    for (Integer other : nodes) {
                        min = Math.min(other, min);
                    }
                }
            }
            return result;
        }

        public int getMin() {
            return min;
        }
    }

    /**
     *
     * @param graphModel
     */
    public void execute(GraphModel graphModel, AttributeModel attributeModel) {
        HierarchicalUndirectedGraph graph = graphModel.getHierarchicalUndirectedGraphVisible();
        execute(graph, attributeModel);
    }

    public void execute(HierarchicalUndirectedGraph graph, AttributeModel attributeModel) {
        isCanceled = false;
        Progress.start(progress);
        Random rand = new Random();

        graph.readLock();

        structure = new CommunityStructure(graph);
        if (isCanceled) {
            graph.readUnlockAll();
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
                    double best = 0;
                    double current = q(i, structure.nodeCommunities[i]);
                    Community bestCommunity = null;
                    int smallest = Integer.MAX_VALUE;
                    Set<Community> iter = structure.nodeConnections[i].keySet();
                    for(Community com : iter) {
                        double qValue = q(i, com) - current;
                        if (qValue > best) {
                            best = qValue;
                            bestCommunity = com;
                            smallest = com.getMin();
                        } else if ((qValue == best) && (com.getMin() < smallest)) {
                            best = qValue;
                            bestCommunity = com;
                            smallest = com.getMin();
                        }
                    }
                    if ((structure.nodeCommunities[i] != bestCommunity) && (bestCommunity != null)) {
                        structure.moveNodeTo(i, bestCommunity);
                        localChange = true;
                    }
                    if (isCanceled) {
                        graph.readUnlockAll();
                        return;
                    }
                }
                someChange = localChange || someChange;
                if (isCanceled) {
                    graph.readUnlockAll();
                    return;
                }
            }

            if (someChange) {
                structure.zoomOut();
            }
        }

        int[] comStructure = new int[graph.getNodeCount()];
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
        for (Node node : graph.getNodes()) {
            int index = structure.map.get(node);
            degreeCount[comStructure[index]] += graph.getTotalDegree(node);
        }

        modularity = finalQ(comStructure, degreeCount, graph, attributeModel);

        graph.readUnlock();
    }

    /**
     * 
     * @param struct
     * @param degrees
     * @param hgraph
     * @return
     */
    public double finalQ(int[] struct, double[] degrees, HierarchicalUndirectedGraph hgraph, AttributeModel attributeModel) {
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

    /**
     * 
     * @return
     */
    public double getModularity() {
        return modularity;
    }

    /**
     *
     * @return
     */
    public String getReport() {

        String report = "<HTML> <BODY> <h1>Modularity Report </h1> "
                + "<hr>"
                + "<h2> Parameters: </h2>"
                + "Randomize:  " + (isRandomized ? "On" : "Off") + "<br>"
                + "<br> <h2> Results: </h2>"
                + "Modularity: " + modularity + "<br>"
                + "Number of Communities: " + structure.communities.size()
                + "</BODY></HTML>";

        return report;
    }

    /**
     * 
     * @param node
     * @param pCom
     * @return
     */
    private double q(int node, Community community) {

        Integer edgesToInt = structure.nodeConnections[node].get(community);
        double edgesTo = 0;
        if (edgesToInt != null) {
            edgesTo = edgesToInt.doubleValue();
        }
        double weightSum = community.weightSum;
        double nodeWeight = structure.weights[node];
        //double penalty = (nodeWeight * weightSum) / (2.0 * mStructure.graphWeightSum);
        double qValue = edgesTo - (nodeWeight * weightSum) / (2.0 * structure.graphWeightSum);
        if ((structure.nodeCommunities[node] == community) && (structure.nodeCommunities[node].size() > 1)) {
            qValue = edgesTo - (nodeWeight * (weightSum - nodeWeight)) / (2.0 * structure.graphWeightSum);
        }
        return qValue;
    }
}
