/*
Copyright 2008-2010 Gephi
Authors : Patick J. McSweeney <pjmcswee@syr.edu>
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

import java.util.Enumeration;
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
import org.gephi.graph.api.Node;
import org.gephi.graph.api.UndirectedGraph;
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
    private ProgressTicket mProgress;
    private boolean mIsCanceled;
    private CommunityStructure mStructure;
    private double mModularity;
    private boolean mRandomize = false;

    /**
     *
     * @param pRandom
     */
    public void setRandom(boolean pRandom) {
        mRandomize = pRandom;
    }

    /**
     * 
     * @return
     */
    public boolean getRandom() {
        return mRandomize;
    }

    /**
     * 
     * @return
     */
    public boolean cancel() {
        mIsCanceled = true;
        return true;
    }

    /**
     *
     * @param progressTicket
     */
    public void setProgressTicket(ProgressTicket progressTicket) {
        mProgress = progressTicket;
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
        HashMap<Community, Integer>[] nodeConnections;
        /** */
        HashMap<Node, Integer> map;
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
        HashMap<Integer, Community> invMap;

        /**
         * 
         * @param graph
         */
        CommunityStructure(UndirectedGraph graph) {
            mGraph = graph;
            N = graph.getNodeCount();
            invMap = new HashMap<Integer, Community>();
            nodeConnections = new HashMap[N];
            nodeCommunities = new Community[N];
            map = new HashMap<Node, Integer>();
            topology = new LinkedList[N];
            mCommunities = new LinkedList<Community>();
            int index = 0;
            weights = new double[N];
            for (Node node : graph.getNodes()) {
                map.put(node, index);
                nodeCommunities[index] = new Community(this);
                nodeConnections[index] = new HashMap<Community, Integer>();
                weights[index] = graph.getDegree(node);
                nodeCommunities[index].seed(index);
                Community hidden = new Community(mStructure);
                hidden.mNodes.add(index);
                invMap.put(index, hidden);
                mCommunities.add(nodeCommunities[index]);
                index++;

                if (mIsCanceled) {
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

                if (mIsCanceled) {
                    return;
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
            nodeConnections = new HashMap[M];
            HashMap<Integer, Community> newInvMap = new HashMap<Integer, Community>();
            for (int i = 0; i < mCommunities.size(); i++) {//Community com : mCommunities) {
                Community com = mCommunities.get(i);
                nodeConnections[index] = new HashMap<Community, Integer>();
                newTopology[index] = new LinkedList<ModEdge>();
                nodeCommunities[index] = new Community(com);
                Set<Community> iter = com.connections.keySet();
                double weightSum = 0;

                Community hidden = new Community(mStructure);
                for (Integer nodeInt : com.mNodes) {
                    Community oldHidden = invMap.get(nodeInt);
                    hidden.mNodes.addAll(oldHidden.mNodes);
                }
                newInvMap.put(index, hidden);
                for(Community adjCom : iter) {
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
        HashMap<Community, Integer> connections;
        Integer min;

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
            connections = new HashMap<Community, Integer>();
            mNodes = new LinkedList<Integer>();
            min = Integer.MAX_VALUE;
            //mHidden = pCom.mHidden;
        }

        /**
         * 
         * @param pStructure
         */
        public Community(CommunityStructure pStructure) {
            mStructure = pStructure;
            connections = new HashMap<Community, Integer>();
            mNodes = new LinkedList<Integer>();
        }

        /**
         * 
         * @param node
         */
        public void seed(int node) {
            mNodes.add(node);
            weightSum += mStructure.weights[node];
            min = node;
        }

        /**
         *
         * @param node
         * @return
         */
        public boolean add(int node) {
            mNodes.addLast(new Integer(node));
            weightSum += mStructure.weights[node];
            if (!mRandomize) {
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
            boolean result = mNodes.remove(new Integer(node));
            weightSum -= mStructure.weights[node];
            if (mNodes.size() == 0) {
                mStructure.mCommunities.remove(this);
            }
            if (!mRandomize) {
                if (node == min.intValue()) {
                    min = Integer.MAX_VALUE;
                    for (Integer other : mNodes) {
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
        UndirectedGraph graph = graphModel.getUndirectedGraphVisible();
        execute(graph, attributeModel);
    }

    public void execute(UndirectedGraph graph, AttributeModel attributeModel) {
        mIsCanceled = false;
        Progress.start(mProgress);
        Random rand = new Random();

        graph.readLock();

        mStructure = new CommunityStructure(graph);
        if (mIsCanceled) {
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
                if (mRandomize) {
                    start = Math.abs(rand.nextInt()) % mStructure.N;
                }
                int step = 0;
                for (int i = start; step < mStructure.N; i = (i + 1) % mStructure.N) {
                    step++;
                    double best = 0;
                    double current = q(i, mStructure.nodeCommunities[i]);
                    Community bestCommunity = null;
                    int smallest = Integer.MAX_VALUE;
                    Set<Community> iter = mStructure.nodeConnections[i].keySet();
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
                    if ((mStructure.nodeCommunities[i] != bestCommunity) && (bestCommunity != null)) {
                        mStructure.moveNodeTo(i, bestCommunity);
                        localChange = true;
                    }
                    if (mIsCanceled) {
                        graph.readUnlockAll();
                        return;
                    }
                }
                someChange = localChange || someChange;
                if (mIsCanceled) {
                    graph.readUnlockAll();
                    return;
                }
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

        mModularity = finalQ(comStructure, degreeCount, graph, attributeModel);

        graph.readUnlock();
    }

    /**
     * 
     * @param pStruct
     * @param pDegrees
     * @param pGraph
     * @return
     */
    public double finalQ(int[] pStruct, double[] pDegrees, UndirectedGraph pGraph, AttributeModel attributeModel) {
        AttributeTable nodeTable = attributeModel.getNodeTable();
        AttributeColumn modCol = nodeTable.getColumn(MODULARITY_CLASS);
        if (modCol == null) {
            modCol = nodeTable.addColumn(MODULARITY_CLASS, "Modularity Class", AttributeType.INT, AttributeOrigin.COMPUTED, new Integer(0));
        }

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
    public double getModularity() {
        return mModularity;
    }

    /**
     *
     * @return
     */
    public String getReport() {

        String report = "<HTML> <BODY> <h1>Modularity Report </h1> "
                + "<hr>"
                + "<h2> Parameters: </h2>"
                + "Randomize:  " + (this.mRandomize ? "On" : "Off") + "<br>"
                + "<br> <h2> Results: </h2>"
                + "Modularity: " + mModularity + "<br>"
                + "Number of Communities: " + mStructure.mCommunities.size()
                + "</BODY></HTML>";

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
