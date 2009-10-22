/*
Copyright 2008 WebAtlas
Authors : Patrick J. McSweeney (pjmcswee@syr.edu)
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.statistics;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Random;
import org.gephi.data.attributes.api.AttributeClass;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.UndirectedGraph;
import org.gephi.statistics.api.Statistics;
import org.gephi.utils.longtask.LongTask;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author pjmcswee
 */
public class Modularity implements Statistics, LongTask {

    private ProgressTicket progress;
    private boolean isCanceled;
    private CommunityStructure mStructure;
    private double modularity;
    private boolean randomize = false;

    /**
     *
     * @param pRandom
     */
    public void setRandom(boolean pRandom) {
        randomize = pRandom;
    }

    /**
     * 
     * @return
     */
    public boolean getRandom() {
        return randomize;
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
     */
    class ModEdge {

        int source;
        int target;
        int weight;

        /**
         * 
         * @param pS
         * @param pT
         * @param pW
         */
        public ModEdge(int pS, int pT, int pW) {
            source = pS;
            target = pT;
            weight = pW;
        }
    }

    class CommunityStructure {

        /**   */
        Hashtable<Community, Integer>[] nodeConnections;
        /** */
        Hashtable<Node, Integer> map;
        /**   */
        Community[] nodeCommunities;
        /** */
        UndirectedGraph mGraph;
        /** */
        double[] weights;
        double graphWeightSum;
        LinkedList<ModEdge>[] topology;
        LinkedList<Community> mCommunities;
        int N;
        Hashtable<Integer, Community> invMap;

        /**
         * 
         * @param graph
         */
        CommunityStructure(UndirectedGraph graph) {
            mGraph = graph;
            N = graph.getNodeCount();
            invMap = new Hashtable<Integer, Community>();
            nodeConnections = new Hashtable[N];
            nodeCommunities = new Community[N];
            map = new Hashtable<Node, Integer>();
            topology = new LinkedList[N];
            mCommunities = new LinkedList<Community>();
            int index = 0;
            weights = new double[N];
            for (Node node : graph.getNodes()) {
                map.put(node, index);
                nodeCommunities[index] = new Community(this);
                nodeConnections[index] = new Hashtable<Community, Integer>();
                weights[index] = graph.getDegree(node);
                nodeCommunities[index].seed(index);
                Community hidden = new Community(mStructure);
                hidden.mNodes.add(index);
                invMap.put(index, hidden);
                mCommunities.add(nodeCommunities[index]);

                index++;
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
            }
            graphWeightSum /= 2.0;
        }

        /**
         *
         * @param node
         * @param pTo
         */
        private void addNodeTo(int node, Community pTo) {
            pTo.add(new Integer(node));
            nodeCommunities[node] = pTo;

            for (ModEdge e : topology[node]) {
                int neighbor = e.target;

                ////////
                //Remove Node Connection to this community
                Integer neighEdgesTo = nodeConnections[neighbor].get(pTo);
                if (neighEdgesTo == null) {
                    nodeConnections[neighbor].put(pTo, e.weight);
                } else {
                    nodeConnections[neighbor].put(pTo, neighEdgesTo + e.weight);
                }




                ///////////////////
                Community adjCom = nodeCommunities[neighbor];
                Integer oEdgesto = adjCom.connections.get(pTo);
                if (oEdgesto == null) {
                    adjCom.connections.put(pTo, e.weight);
                } else {
                    adjCom.connections.put(pTo, oEdgesto + e.weight);
                }

                Integer nodeEdgesTo = nodeConnections[node].get(adjCom);
                if (nodeEdgesTo == null) {
                    nodeConnections[node].put(adjCom, e.weight);
                } else {
                    nodeConnections[node].put(adjCom, nodeEdgesTo + e.weight);
                }

                if (pTo != adjCom) {
                    Integer comEdgesto = pTo.connections.get(adjCom);
                    if (comEdgesto == null) {
                        pTo.connections.put(adjCom, e.weight);
                    } else {
                        pTo.connections.put(adjCom, comEdgesto + e.weight);
                    }
                }
            }
        }

        /**
         * 
         * @param node
         * @param pFrom
         */
        private void removeNodeFrom(int node, Community pFrom) {
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
            pFrom.remove(new Integer(node));
        }

        /**
         *
         * @param node
         * @param pFrom
         * @param pTo
         */
        public void moveNodeTo(int node, Community pTo) {
            Community from = nodeCommunities[node];
            removeNodeFrom(node, from);
            addNodeTo(node, pTo);
        }

        /**
         *
         * @return
         */
        public void zoomOut() {
            int M = mCommunities.size();
            LinkedList<ModEdge>[] newTopology = new LinkedList[M];
            int index = 0;
            nodeCommunities = new Community[M];
            nodeConnections = new Hashtable[M];
            Hashtable<Integer, Community> newInvMap = new Hashtable<Integer, Community>();
            for (int i = 0; i < mCommunities.size(); i++) {//Community com : mCommunities) {
                Community com = mCommunities.get(i);
                nodeConnections[index] = new Hashtable<Community, Integer>();
                newTopology[index] = new LinkedList<ModEdge>();
                nodeCommunities[index] = new Community(com);
                Enumeration<Community> iter = com.connections.keys();
                double weightSum = 0;

                Community hidden = new Community(mStructure);
                for (Integer nodeInt : com.mNodes) {
                    Community oldHidden = invMap.get(nodeInt);
                    hidden.mNodes.addAll(oldHidden.mNodes);
                }
                newInvMap.put(index, hidden);
                while (iter.hasMoreElements()) {
                    Community adjCom = iter.nextElement();
                    int target = mCommunities.indexOf(adjCom);
                    int weight = com.connections.get(adjCom);

                    weightSum += weight;
                    ModEdge e = new ModEdge(index, target, weight);
                    newTopology[index].add(e);
                }
                weights[index] = weightSum;
                nodeCommunities[index].seed(index);

                index++;
            }
            mCommunities.clear();

            for (int i = 0; i < M; i++) {
                Community com = nodeCommunities[i];
                mCommunities.add(com);
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
        CommunityStructure mStructure;
        LinkedList<Integer> mNodes;
        Hashtable<Community, Integer> connections;

        /**
         * 
         * @return
         */
        public int size() {
            return mNodes.size();
        }

        /**
         * 
         * @param pCom
         */
        public Community(Community pCom) {
            mStructure = pCom.mStructure;
            connections = new Hashtable<Community, Integer>();
            mNodes = new LinkedList<Integer>();
        //mHidden = pCom.mHidden;
        }

        /**
         * 
         * @param pStructure
         */
        public Community(CommunityStructure pStructure) {
            mStructure = pStructure;
            connections = new Hashtable<Community, Integer>();
            mNodes = new LinkedList<Integer>();
        }

        /**
         * 
         * @param node
         */
        public void seed(int node) {
            mNodes.add(node);
            weightSum += mStructure.weights[node];
        }

        /**
         *
         * @param node
         * @return
         */
        public boolean add(int node) {
            boolean result = mNodes.add(new Integer(node));
            weightSum += mStructure.weights[node];
            return result;
        }

        /**
         * 
         * @param node
         * @return
         */
        public boolean remove(int node) {
            boolean result = mNodes.remove(new Integer(node));
            weightSum -= mStructure.weights[node];
            if (mNodes.size() == 0) {
                mStructure.mCommunities.remove(this);
            }
            return result;
        }
    }

    /**
     * 
     * @return
     */
    public String getName() {
        return NbBundle.getMessage(PageRank.class, "Modularity_name");
    }

    /**
     *
     * @param graphController
     */
    public void execute(GraphController graphController) {
        progress.start();
        Random rand = new Random();
        UndirectedGraph graph = graphController.getModel().getUndirectedGraphVisible();
        mStructure = new CommunityStructure(graph);
        boolean someChange = true;
        while (someChange) {
            someChange = false;
            boolean localChange = true;


            while (localChange) {
                localChange = false;
                int start = 0;
                if (randomize) {
                    start = Math.abs(rand.nextInt()) % mStructure.N;
                }
                int step = 0;
                for (int i = start; step < mStructure.N; i = (i + 1) % mStructure.N) {
                    step++;
                    double best = 0;
                    double current = q(i, mStructure.nodeCommunities[i]);
                    Community bestCommunity = null;
                    Enumeration<Community> iter = mStructure.nodeConnections[i].keys();
                    while (iter.hasMoreElements()) {
                        Community com = iter.nextElement();
                        double qValue = q(i, com) - current;
                        if (qValue > best) {
                            best = qValue;
                            bestCommunity = com;
                        }
                    }
                    if ((mStructure.nodeCommunities[i] != bestCommunity) && (bestCommunity != null)) {
                        mStructure.moveNodeTo(i, bestCommunity);
                        localChange = true;
                    }
                }
                someChange = localChange || someChange;
            }

            if (someChange) {
                mStructure.zoomOut();
            }
        }

        int[] comStructure = new int[graph.getNodeCount()];
        int count = 0;
        double[] degreeCount = new double[mStructure.mCommunities.size()];
        for (Community com : mStructure.mCommunities) {
            for (Integer node : com.mNodes) {
                Community hidden = mStructure.invMap.get(node);
                for (Integer nodeInt : hidden.mNodes) {
                    comStructure[nodeInt] = count;
                }
            }
            count++;
        }
        for (Node node : graph.getNodes()) {
            int index = mStructure.map.get(node);
            degreeCount[comStructure[index]] += graph.getDegree(node);
        }

        modularity = finalQ(comStructure, degreeCount, graph);
    }

    /**
     * 
     * @param pStruct
     * @param pDegrees
     * @param pGraph
     * @return
     */
    public double finalQ(int[] pStruct, double[] pDegrees, UndirectedGraph pGraph) {

        AttributeController ac = Lookup.getDefault().lookup(AttributeController.class);
        AttributeClass nodeClass = ac.getTemporaryAttributeManager().getNodeClass();
        AttributeColumn modCol = nodeClass.addAttributeColumn("modularity_class", "Modularity Class", AttributeType.INT, AttributeOrigin.COMPUTED, new Integer(0));

        double res = 0;
        double[] internal = new double[pDegrees.length];
        for (Node n : pGraph.getNodes()) {
            int n_index = mStructure.map.get(n);
            AttributeRow row = (AttributeRow) n.getNodeData().getAttributes();
            row.setValue(modCol, pStruct[n_index]);
            for (Node neighbor : pGraph.getNeighbors(n)) {
                if (n == neighbor) {
                    continue;
                }
                int neigh_index = mStructure.map.get(neighbor);
                if (pStruct[neigh_index] == pStruct[n_index]) {
                    internal[pStruct[neigh_index]]++;
                }
            }
        }
        for (int i = 0; i < pDegrees.length; i++) {
            internal[i] /= 2.0;
            res += (internal[i] / pGraph.getEdgeCount()) - Math.pow(pDegrees[i] / (2 * pGraph.getEdgeCount()), 2);
        }
        return res;
    }

    /**
     *
     * @return
     */
    public String getReport() {
        String report = "<html> <body> " + modularity + "<br>";
        report += " </body></html>";
        return report;
    }

    /**
     * 
     * @param node
     * @param pCom
     * @return
     */
    private double q(int node, Community pCommunity) {

        Integer edgesToInt = mStructure.nodeConnections[node].get(pCommunity);
        double edgesTo = 0;
        if (edgesToInt != null) {
            edgesTo = edgesToInt.doubleValue();
        }
        double weightSum = pCommunity.weightSum;
        double nodeWeight = mStructure.weights[node];
        double penalty = (nodeWeight * weightSum) / (2.0 * mStructure.graphWeightSum);
        double qValue = edgesTo - (nodeWeight * weightSum) / (2.0 * mStructure.graphWeightSum);
        if ((mStructure.nodeCommunities[node] == pCommunity) && (mStructure.nodeCommunities[node].size() > 1)) {
            qValue = edgesTo - (nodeWeight * (weightSum - nodeWeight)) / (2.0 * mStructure.graphWeightSum);
        }
        return qValue;
    }
}
