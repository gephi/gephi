/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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

import junit.framework.TestCase;
import org.gephi.graph.api.*;
import org.gephi.io.importer.GraphImporter;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Mathieu Jacomy
 */

public class StatisticalInferenceTest extends TestCase {

    private UndirectedGraph getCliquesBridgeGraph() {
        GraphModel graphModel = GraphModel.Factory.newInstance();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();

        Node node0 = graphModel.factory().newNode("0");
        Node node1 = graphModel.factory().newNode("1");
        Node node2 = graphModel.factory().newNode("2");
        Node node3 = graphModel.factory().newNode("3");
        Node node4 = graphModel.factory().newNode("4");
        Node node5 = graphModel.factory().newNode("5");
        Node node6 = graphModel.factory().newNode("6");
        Node node7 = graphModel.factory().newNode("7");

        undirectedGraph.addNode(node0);
        undirectedGraph.addNode(node1);
        undirectedGraph.addNode(node2);
        undirectedGraph.addNode(node3);
        undirectedGraph.addNode(node4);
        undirectedGraph.addNode(node5);
        undirectedGraph.addNode(node6);
        undirectedGraph.addNode(node7);

        // Clique A
        Edge edge01 = graphModel.factory().newEdge(node0, node1, false);
        Edge edge12 = graphModel.factory().newEdge(node1, node2, false);
        Edge edge23 = graphModel.factory().newEdge(node2, node3, false);
        Edge edge30 = graphModel.factory().newEdge(node3, node0, false);
        Edge edge02 = graphModel.factory().newEdge(node0, node2, false);
        Edge edge13 = graphModel.factory().newEdge(node1, node3, false);
        // Bridge
        Edge edge04 = graphModel.factory().newEdge(node0, node4, false);
        // Clique B
        Edge edge45 = graphModel.factory().newEdge(node4, node5, false);
        Edge edge56 = graphModel.factory().newEdge(node5, node6, false);
        Edge edge67 = graphModel.factory().newEdge(node6, node7, false);
        Edge edge74 = graphModel.factory().newEdge(node7, node4, false);
        Edge edge46 = graphModel.factory().newEdge(node4, node6, false);
        Edge edge57 = graphModel.factory().newEdge(node5, node7, false);

        undirectedGraph.addEdge(edge01);
        undirectedGraph.addEdge(edge12);
        undirectedGraph.addEdge(edge23);
        undirectedGraph.addEdge(edge30);
        undirectedGraph.addEdge(edge02);
        undirectedGraph.addEdge(edge13);
        undirectedGraph.addEdge(edge04);
        undirectedGraph.addEdge(edge45);
        undirectedGraph.addEdge(edge56);
        undirectedGraph.addEdge(edge67);
        undirectedGraph.addEdge(edge74);
        undirectedGraph.addEdge(edge46);
        undirectedGraph.addEdge(edge57);

        UndirectedGraph graph = graphModel.getUndirectedGraph();
        return graph;
    }

    @Test
    public void testCliquesBridgeGraph_descriptionLength() {
        UndirectedGraph graph = getCliquesBridgeGraph();
        StatisticalInferenceClustering sic = new StatisticalInferenceClustering();
        StatisticalInferenceClustering.CommunityStructure theStructure = sic.new CommunityStructure(graph);

        // At initialization, each node is in its own community.
        // Here we just test the description length at init.
        // We test for the know value (from GraphTools)

        double descriptionLength_atInit = sic.computeDescriptionLength(graph, theStructure);
        assertEquals(36.0896, descriptionLength_atInit, 0.0001);

        // Now we move the nodes so that one community remains for each clique
        StatisticalInferenceClustering.Community cA = theStructure.nodeCommunities[0];
        StatisticalInferenceClustering.Community cB = theStructure.nodeCommunities[4];
        theStructure._moveNodeTo(1, cA);
        theStructure._moveNodeTo(2, cA);
        theStructure._moveNodeTo(3, cA);
        theStructure._moveNodeTo(5, cB);
        theStructure._moveNodeTo(6, cB);
        theStructure._moveNodeTo(7, cB);

        // Now we test that the description length is shorter when the communities
        // match the expectations (one community per clique)

        double descriptionLength_atIdealPartition = sic.computeDescriptionLength(graph, theStructure);
        assertTrue(descriptionLength_atIdealPartition < descriptionLength_atInit);
    }

    @Test
    public void testDescriptionLengthOneCommunity() {
        UndirectedGraph graph = getCliquesBridgeGraph();
        StatisticalInferenceClustering sic = new StatisticalInferenceClustering();
        StatisticalInferenceClustering.CommunityStructure theStructure = sic.new CommunityStructure(graph);

        // Now we move the nodes in the same community
        StatisticalInferenceClustering.Community com = theStructure.nodeCommunities[0];
        theStructure._moveNodeTo(1, com);
        theStructure._moveNodeTo(2, com);
        theStructure._moveNodeTo(3, com);
        theStructure._moveNodeTo(4, com);
        theStructure._moveNodeTo(5, com);
        theStructure._moveNodeTo(6, com);
        theStructure._moveNodeTo(7, com);

        // Now we test that the description length is shorter when the communities
        // match the expectations (one community per clique)

        double descriptionLength = sic.computeDescriptionLength(graph, theStructure);
        assertEquals(29.93900552172898, descriptionLength, 0.0001);

        // Zoom out
        theStructure._zoomOut();

        descriptionLength = sic.computeDescriptionLength(graph, theStructure);
        assertEquals(29.93900552172898, descriptionLength, 0.0001);
    }

    @Test
    public void testCommunityWeightsBookkeeping() {
        UndirectedGraph graph = getCliquesBridgeGraph();
        StatisticalInferenceClustering sic = new StatisticalInferenceClustering();
        StatisticalInferenceClustering.CommunityStructure theStructure = sic.new CommunityStructure(graph);
        // Note: at initialization, each node is in its own community.

        for (int node = 0; node < 8; node++) {
            // The community for each node should have a weight equal to the degree of that node.
            assertEquals(theStructure.weights[node], theStructure.nodeCommunities[node].weightSum);
            // The community for each node should have an inner weight equal to zero.
            assertEquals(0., theStructure.nodeCommunities[node].internalWeightSum);
        }

        // Move node 1 to the same community as node 0: it now contains nodes 0 and 1 (degrees 4 and 3).
        theStructure._moveNodeTo(1, theStructure.nodeCommunities[0]);
        assertEquals(7., theStructure.nodeCommunities[0].weightSum);
        // There is 1 internal link
        assertEquals(1., theStructure.nodeCommunities[0].internalWeightSum);

        // Move node 1 to the same community as node 2: now, the community of node 0 contains just nodes 0 (degree 4).
        theStructure._moveNodeTo(1, theStructure.nodeCommunities[2]);
        assertEquals(4., theStructure.nodeCommunities[0].weightSum);
        // There is 0 internal link
        assertEquals(0., theStructure.nodeCommunities[0].internalWeightSum);
    }

    @Test
    public void testMiscMetricsBookkeeping() {
        UndirectedGraph graph = getCliquesBridgeGraph();
        StatisticalInferenceClustering sic = new StatisticalInferenceClustering();
        StatisticalInferenceClustering.CommunityStructure theStructure = sic.new CommunityStructure(graph);

        // Note: at initialization, each node is in its own community.
        // We move the nodes to just two communities, one for each clique.
        StatisticalInferenceClustering.Community cA = theStructure.nodeCommunities[0];
        StatisticalInferenceClustering.Community cB = theStructure.nodeCommunities[4];
        theStructure._moveNodeTo(1, cA);
        theStructure._moveNodeTo(2, cA);
        theStructure._moveNodeTo(3, cA);
        theStructure._moveNodeTo(5, cB);
        theStructure._moveNodeTo(6, cB);
        theStructure._moveNodeTo(7, cB);

        // Total number of edges (graph size)
        Double E = theStructure.graphWeightSum;
        assertEquals(13., E);

        // Total number of edges from one community to the same one
        Double e_in = theStructure.communities.stream().mapToDouble(c -> c.internalWeightSum).sum();
        assertEquals(12., e_in); // 6 inner links per clique

        // Total number of edges from one community to another
        Double e_out = E - e_in;
        assertEquals(1., e_out); // 1 bridge

        // Total number of communities
        Double B = Double.valueOf(theStructure.communities.size());
        assertEquals(2., B);

        // Total number of nodes (not metanodes!!!)
        Double N = Double.valueOf(theStructure.graph.getNodeCount());
        assertEquals(8., N);
    }

    @Test
    public void testSimpleDescriptionLengthDelta() {
        UndirectedGraph graph = getCliquesBridgeGraph();
        StatisticalInferenceClustering sic = new StatisticalInferenceClustering();
        StatisticalInferenceClustering.CommunityStructure theStructure = sic.new CommunityStructure(graph);
        // Note: at initialization, each node is in its own community.

        // Compute description length
        double descriptionLength_before = sic.computeDescriptionLength(graph, theStructure);

        // Test moving node 1 to the same community as node 0
        int node = 1;
        StatisticalInferenceClustering.Community community = theStructure.nodeCommunities[0]; // Node 0's community

        // Benchmark the delta
        Double E = theStructure.graphWeightSum;
        Double e_in = theStructure.communities.stream().mapToDouble(c -> c.internalWeightSum).sum();
        Double e_out = E - e_in;
        Double B = Double.valueOf(theStructure.communities.size());
        Double N = Double.valueOf(theStructure.graph.getNodeCount());
        double descriptionLength_delta = sic.delta(node, community, theStructure, e_in, e_out, E, B, N);

        // Actually move the node
        theStructure._moveNodeTo(node, community);

        // Compute description length again
        double descriptionLength_after = sic.computeDescriptionLength(graph, theStructure);

        // Delta should be (approximately) equal to the difference
        assertEquals(descriptionLength_after - descriptionLength_before, descriptionLength_delta, 0.0001);
    }

    @Test
    public void testDescriptionLengthDeltaWithZoomOut() {
        UndirectedGraph graph = getCliquesBridgeGraph();
        StatisticalInferenceClustering sic = new StatisticalInferenceClustering();
        StatisticalInferenceClustering.CommunityStructure theStructure = sic.new CommunityStructure(graph);

        // Make some groups and zoom out.
        theStructure._moveNodeTo(1, theStructure.nodeCommunities[0]);
        theStructure._moveNodeTo(2, theStructure.nodeCommunities[0]);
        theStructure._zoomOut();
        theStructure._moveNodeTo(2, theStructure.nodeCommunities[0]);
        theStructure._moveNodeTo(3, theStructure.nodeCommunities[1]);

        // Compute description length
        double descriptionLength_before = sic.computeDescriptionLength(graph, theStructure);

        int node = 1;
        StatisticalInferenceClustering.Community community = theStructure.nodeCommunities[0]; // Node 0's community

        // Benchmark the delta
        Double E = theStructure.graphWeightSum;
        Double e_in = theStructure.communities.stream().mapToDouble(c -> c.internalWeightSum).sum();
        Double e_out = E - e_in;
        Double B = Double.valueOf(theStructure.communities.size());
        Double N = Double.valueOf(theStructure.graph.getNodeCount());
        double descriptionLength_delta = sic.delta(node, community, theStructure, e_in, e_out, E, B, N);

        // Actually move the node
        theStructure._moveNodeTo(node, community);

        // Compute description length again
        double descriptionLength_after = sic.computeDescriptionLength(graph, theStructure);

        // Delta should be (approximately) equal to the difference
        assertEquals(descriptionLength_after - descriptionLength_before, descriptionLength_delta, 0.0001);
    }

    @Test
    public void testDescriptionLengthDeltaWithZoomOut_x2() {
        UndirectedGraph graph = getCliquesBridgeGraph();
        StatisticalInferenceClustering sic = new StatisticalInferenceClustering();
        StatisticalInferenceClustering.CommunityStructure theStructure = sic.new CommunityStructure(graph);

        // Make some groups and shuffle around to stress bookkeeping
        theStructure._moveNodeTo(4, theStructure.nodeCommunities[0]);
        theStructure._moveNodeTo(5, theStructure.nodeCommunities[1]);
        theStructure._moveNodeTo(6, theStructure.nodeCommunities[2]);
        theStructure._moveNodeTo(1, theStructure.nodeCommunities[0]);
        theStructure._moveNodeTo(4, theStructure.nodeCommunities[5]);
        theStructure._moveNodeTo(2, theStructure.nodeCommunities[3]);
        theStructure._moveNodeTo(6, theStructure.nodeCommunities[7]);
        //System.out.println(theStructure.getMonitoring());
        // > com0[n0(0) n1(1)]  com2[n5(5) n4(4)]  com6[n3(3) n2(2)]  com14[n7(7) n6(6)]

        // Zoom out
        theStructure._zoomOut();
        //System.out.println(theStructure.getMonitoring());
        // > com16[n0(0 1)]  com18[n1(5 4)]  com20[n2(3 2)]  com22[n3(7 6)]

        // Shuffle around to stress bookkeeping
        theStructure._moveNodeTo(2, theStructure.nodeCommunities[0]);
        theStructure._moveNodeTo(0, theStructure.nodeCommunities[1]);
        theStructure._moveNodeTo(1, theStructure.nodeCommunities[3]);
        //System.out.println(theStructure.getMonitoring());
        // > com16[n2(3 2)]  com18[n0(0 1)]  com22[n3(7 6) n1(5 4)]

        // Zoom out again
        theStructure._zoomOut();
        //System.out.println(theStructure.getMonitoring());
        // > com24[n0(3 2)]  com26[n1(0 1)]  com28[n2(7 6 5 4)]

        // Test

        // Compute description length
        double descriptionLength_before = sic.computeDescriptionLength(graph, theStructure);

        int node = 1;
        StatisticalInferenceClustering.Community community = theStructure.nodeCommunities[0]; // Node 0's community

        // Benchmark the delta
        Double E = theStructure.graphWeightSum;
        Double e_in = theStructure.communities.stream().mapToDouble(c -> c.internalWeightSum).sum();
        Double e_out = E - e_in;
        Double B = Double.valueOf(theStructure.communities.size());
        Double N = Double.valueOf(theStructure.graph.getNodeCount());
        double descriptionLength_delta = sic.delta(node, community, theStructure, e_in, e_out, E, B, N);

        // Actually move the node
        theStructure._moveNodeTo(node, community);

        // Compute description length again
        double descriptionLength_after = sic.computeDescriptionLength(graph, theStructure);

        // Delta should be (approximately) equal to the difference
        assertEquals(descriptionLength_after - descriptionLength_before, descriptionLength_delta, 0.0001);
    }

    // The four next tests are networks from Tiago Peixoto, with a reference partition and description length.
    @Test
    public void testDescriptionLength_football() {
        GraphModel graphModel = GraphImporter.importGraph(DummyTest.class, "football.graphml");
        UndirectedGraph graph = graphModel.getUndirectedGraph();
        StatisticalInferenceClustering sic = new StatisticalInferenceClustering();
        StatisticalInferenceClustering.CommunityStructure theStructure = sic.new CommunityStructure(graph);

        HashMap<Integer, StatisticalInferenceClustering.Community> knownCommunities = new HashMap<>();
        NodeIterable nodesIterable = graph.getNodes();
        for (Node node : nodesIterable) {
            Integer targetCom = (Integer) node.getAttribute("key1");
            int nodeIndex = theStructure.map.get(node);
            StatisticalInferenceClustering.Community initCom = theStructure.nodeCommunities[nodeIndex];
            if (knownCommunities.containsKey(targetCom)) {
                theStructure._moveNodeTo(nodeIndex, knownCommunities.get(targetCom));
            } else {
                knownCommunities.put(targetCom, initCom);
            }
        }

        double descriptionLength = sic.computeDescriptionLength(graph, theStructure);
        assertEquals(1850.2102335828238, descriptionLength, 0.0001);
    }

    @Test
    public void testDescriptionLength_moviegalaxies() {
        GraphModel graphModel = GraphImporter.importGraph(DummyTest.class, "moviegalaxies.graphml");
        UndirectedGraph graph = graphModel.getUndirectedGraph();
        StatisticalInferenceClustering sic = new StatisticalInferenceClustering();
        StatisticalInferenceClustering.CommunityStructure theStructure = sic.new CommunityStructure(graph);

        HashMap<Integer, StatisticalInferenceClustering.Community> knownCommunities = new HashMap<>();
        NodeIterable nodesIterable = graph.getNodes();
        for (Node node : nodesIterable) {
            Integer targetCom = (Integer) node.getAttribute("key1");
            int nodeIndex = theStructure.map.get(node);
            StatisticalInferenceClustering.Community initCom = theStructure.nodeCommunities[nodeIndex];
            if (knownCommunities.containsKey(targetCom)) {
                theStructure._moveNodeTo(nodeIndex, knownCommunities.get(targetCom));
            } else {
                knownCommunities.put(targetCom, initCom);
            }
        }

        double descriptionLength = sic.computeDescriptionLength(graph, theStructure);
        assertEquals(229.04187438186472, descriptionLength, 0.0001);
    }

    @Test
    public void testDescriptionLength_5cliques() {
        GraphModel graphModel = GraphImporter.importGraph(DummyTest.class, "5-cliques.graphml");
        UndirectedGraph graph = graphModel.getUndirectedGraph();
        StatisticalInferenceClustering sic = new StatisticalInferenceClustering();
        StatisticalInferenceClustering.CommunityStructure theStructure = sic.new CommunityStructure(graph);

        HashMap<Integer, StatisticalInferenceClustering.Community> knownCommunities = new HashMap<>();
        NodeIterable nodesIterable = graph.getNodes();
        for (Node node : nodesIterable) {
            Integer targetCom = (Integer) node.getAttribute("key1");
            int nodeIndex = theStructure.map.get(node);
            StatisticalInferenceClustering.Community initCom = theStructure.nodeCommunities[nodeIndex];
            if (knownCommunities.containsKey(targetCom)) {
                theStructure._moveNodeTo(nodeIndex, knownCommunities.get(targetCom));
            } else {
                knownCommunities.put(targetCom, initCom);
            }
        }

        double descriptionLength = sic.computeDescriptionLength(graph, theStructure);
        assertEquals(150.10880360418344, descriptionLength, 0.0001);
    }

    @Test
    public void testDescriptionLength_2cliques() {
        GraphModel graphModel = GraphImporter.importGraph(DummyTest.class, "two-cliques.graphml");
        UndirectedGraph graph = graphModel.getUndirectedGraph();
        StatisticalInferenceClustering sic = new StatisticalInferenceClustering();
        StatisticalInferenceClustering.CommunityStructure theStructure = sic.new CommunityStructure(graph);

        HashMap<Integer, StatisticalInferenceClustering.Community> knownCommunities = new HashMap<>();
        NodeIterable nodesIterable = graph.getNodes();
        for (Node node : nodesIterable) {
            Integer targetCom = (Integer) node.getAttribute("key1");
            int nodeIndex = theStructure.map.get(node);
            StatisticalInferenceClustering.Community initCom = theStructure.nodeCommunities[nodeIndex];
            if (knownCommunities.containsKey(targetCom)) {
                theStructure._moveNodeTo(nodeIndex, knownCommunities.get(targetCom));
            } else {
                knownCommunities.put(targetCom, initCom);
            }
        }

        double descriptionLength = sic.computeDescriptionLength(graph, theStructure);
        assertEquals(43.479327707987835, descriptionLength, 0.0001);
    }

    @Test
    public void testMiscMetricsConsistentThroughZoomOut() {
        UndirectedGraph graph = getCliquesBridgeGraph();
        StatisticalInferenceClustering sic = new StatisticalInferenceClustering();
        StatisticalInferenceClustering.CommunityStructure theStructure = sic.new CommunityStructure(graph);

        StatisticalInferenceClustering.Community cA1 = theStructure.nodeCommunities[0];
        StatisticalInferenceClustering.Community cA2 = theStructure.nodeCommunities[3];
        StatisticalInferenceClustering.Community cB = theStructure.nodeCommunities[4];
        theStructure._moveNodeTo(1, cA1);
        theStructure._moveNodeTo(2, cA1);
        theStructure._moveNodeTo(5, cB);
        theStructure._moveNodeTo(6, cB);
        theStructure._moveNodeTo(7, cB);

        // Total number of edges (graph size)
        double E_before = theStructure.graphWeightSum;
        // Total number of edges from one community to the same one
        double e_in_before = theStructure.communities.stream().mapToDouble(c -> c.internalWeightSum).sum();
        // Total number of communities
        double B_before = Double.valueOf(theStructure.communities.size());
        // Total number of nodes (not metanodes!!!)
        double N_before = Double.valueOf(theStructure.graph.getNodeCount());

        ArrayList<Double> e_r_before = new ArrayList<>();
        ArrayList<Double> e_rr_before = new ArrayList<>();
        ArrayList<Integer> n_r_before = new ArrayList<>();
        for (StatisticalInferenceClustering.Community community : theStructure.communities) {
            // Number of edges of community (with itself or another one)
            double e_r = community.weightSum;
            // Number of edges within community
            double e_rr = community.internalWeightSum;
            // Number of nodes in the  community
            int n_r = community.graphNodeCount;

            e_r_before.add(e_r);
            e_rr_before.add(e_rr);
            n_r_before.add(n_r);
        }

        theStructure._zoomOut();

        // Total number of edges (graph size)
        double E_after = theStructure.graphWeightSum;
        // Total number of edges from one community to the same one
        double e_in_after = theStructure.communities.stream().mapToDouble(c -> c.internalWeightSum).sum();
        // Total number of communities
        double B_after = Double.valueOf(theStructure.communities.size());
        // Total number of nodes (not metanodes!!!)
        double N_after = Double.valueOf(theStructure.graph.getNodeCount());

        ArrayList<Double> e_r_after = new ArrayList<>();
        ArrayList<Double> e_rr_after = new ArrayList<>();
        ArrayList<Integer> n_r_after = new ArrayList<>();
        for (StatisticalInferenceClustering.Community community : theStructure.communities) {
            // Number of edges of community (with itself or another one)
            double e_r = community.weightSum;
            // Number of edges within community
            double e_rr = community.internalWeightSum;
            // Number of nodes in the  community
            int n_r = community.graphNodeCount;

            e_r_after.add(e_r);
            e_rr_after.add(e_rr);
            n_r_after.add(n_r);
        }

        assertEquals(E_before, E_after);
        assertEquals(e_in_before, e_in_after);
        assertEquals(B_before, B_after);
        assertEquals(N_before, N_after);

        assertEquals(e_r_before, e_r_after);
        assertEquals(e_rr_before, e_rr_after);
        assertEquals(n_r_before, n_r_after);
    }

    @Test
    public void testDescriptionLengthConsistentThroughZoomOut_simple() {
        UndirectedGraph graph = getCliquesBridgeGraph();
        StatisticalInferenceClustering sic = new StatisticalInferenceClustering();
        StatisticalInferenceClustering.CommunityStructure theStructure = sic.new CommunityStructure(graph);

        StatisticalInferenceClustering.Community cA1 = theStructure.nodeCommunities[0];
        StatisticalInferenceClustering.Community cA2 = theStructure.nodeCommunities[3];
        StatisticalInferenceClustering.Community cB = theStructure.nodeCommunities[4];
        theStructure._moveNodeTo(1, cA1);
        theStructure._moveNodeTo(2, cA1);
        theStructure._moveNodeTo(5, cB);
        theStructure._moveNodeTo(6, cB);
        theStructure._moveNodeTo(7, cB);

        double descriptionLength_before = sic.computeDescriptionLength(graph, theStructure);

        theStructure._zoomOut();

        double descriptionLength_after = sic.computeDescriptionLength(graph, theStructure);

        assertEquals(descriptionLength_before, descriptionLength_after, 0.00001);
    }

    @Test
    public void testDescriptionLengthConsistentThroughZoomOut_complicated() {
        UndirectedGraph graph = getCliquesBridgeGraph();
        StatisticalInferenceClustering sic = new StatisticalInferenceClustering();
        StatisticalInferenceClustering.CommunityStructure theStructure = sic.new CommunityStructure(graph);

        double descriptionLength_before;
        double descriptionLength_after;

        //System.out.println("\n# Initial");
        //System.out.println("  Structure: "+theStructure.getMonitoring());
        //System.out.println("  DL: "+sic.computeDescriptionLength(graph, theStructure));

        // Move the nodes in categories
        //System.out.println("\n# 1st round of group rearranging");
        theStructure._moveNodeTo(1, theStructure.nodeCommunities[0]);
        theStructure._moveNodeTo(3, theStructure.nodeCommunities[2]);
        //System.out.println("  Structure: "+theStructure.getMonitoring());
        descriptionLength_before = sic.computeDescriptionLength(graph, theStructure);
        //System.out.println("  DL: "+descriptionLength_before);

        // Zoom out
        //System.out.println("\n# Zoom out");
        theStructure._zoomOut();
        //System.out.println("  Structure: "+theStructure.getMonitoring());
        descriptionLength_after = sic.computeDescriptionLength(graph, theStructure);
        //System.out.println("  DL: "+descriptionLength_after);

        assertEquals(descriptionLength_before, descriptionLength_after, 0.00001);

        // Move the nodes in categories
        //System.out.println("\n# 2nd round of group rearranging");
        theStructure._moveNodeTo(1, theStructure.nodeCommunities[2]);
        //System.out.println("  Structure: "+theStructure.getMonitoring());
        descriptionLength_before = sic.computeDescriptionLength(graph, theStructure);
        //System.out.println("  DL: "+descriptionLength_before);

        // Zoom out
        //System.out.println("\n# Zoom out");
        theStructure._zoomOut();
        //System.out.println("  Structure: "+theStructure.getMonitoring());
        descriptionLength_after = sic.computeDescriptionLength(graph, theStructure);
        //System.out.println("  DL: "+descriptionLength_after);

        assertEquals(descriptionLength_before, descriptionLength_after, 0.00001);

        // Move the nodes in categories
        //System.out.println("\n# 3rd round of group rearranging");
        theStructure._moveNodeTo(2, theStructure.nodeCommunities[4]);
        theStructure._moveNodeTo(3, theStructure.nodeCommunities[4]);
        //System.out.println("  Structure: "+theStructure.getMonitoring());
        descriptionLength_before = sic.computeDescriptionLength(graph, theStructure);
        //System.out.println("  DL: "+descriptionLength_before);

        // Zoom out
        //System.out.println("\n# Zoom out");
        theStructure._zoomOut();
        //System.out.println("  Structure: "+theStructure.getMonitoring());
        descriptionLength_after = sic.computeDescriptionLength(graph, theStructure);
        //System.out.println("  DL: "+descriptionLength_after);

        assertEquals(descriptionLength_before, descriptionLength_after, 0.00001);
    }

    /*
    // This test is not unitary enough, it may randomly fail by design. Useful for debugging though.
    @Test
    public void testMinimizationHeuristic_football() {
        GraphModel graphModel = GraphImporter.importGraph(DummyTest.class, "football.graphml");
        UndirectedGraph graph = graphModel.getUndirectedGraph();
        StatisticalInferenceClustering sic = new StatisticalInferenceClustering();

        sic.execute(graph);
        double descriptionLength = sic.getDescriptionLength();

        double targetDescLength = 1850.2102335828238;
        double errorMargin = 0.1;
        assertEquals(targetDescLength, descriptionLength, errorMargin * targetDescLength);
    }
     */

    @Test
    public void testMinimizationHeuristic_5cliques() {
        GraphModel graphModel = GraphImporter.importGraph(DummyTest.class, "5-cliques.graphml");
        UndirectedGraph graph = graphModel.getUndirectedGraph();
        StatisticalInferenceClustering sic = new StatisticalInferenceClustering();

        sic.execute(graph);
        double descriptionLength = sic.getDescriptionLength();

        double targetDescLength = 150.10880360418344;
        double errorMargin = 0.01;
        assertEquals(targetDescLength, descriptionLength, errorMargin * targetDescLength);
    }

    @Test
    public void testMinimizationHeuristic_moviegalaxies() {
        GraphModel graphModel = GraphImporter.importGraph(DummyTest.class, "moviegalaxies.graphml");
        UndirectedGraph graph = graphModel.getUndirectedGraph();
        StatisticalInferenceClustering sic = new StatisticalInferenceClustering();

        sic.execute(graph);
        double descriptionLength = sic.getDescriptionLength();

        double targetDescLength = 229.04187438186472;
        double errorMargin = 0.1;
        assertEquals(targetDescLength, descriptionLength, errorMargin * targetDescLength);
    }

    @Test
    public void testDescriptionLengthZoomOut_football() {
        GraphModel graphModel = GraphImporter.importGraph(DummyTest.class, "football.graphml");
        UndirectedGraph graph = graphModel.getUndirectedGraph();
        StatisticalInferenceClustering sic = new StatisticalInferenceClustering();
        StatisticalInferenceClustering.CommunityStructure theStructure = sic.new CommunityStructure(graph);

        // We reproduce a given setting
        StatisticalInferenceClustering.Community c0 = theStructure.nodeCommunities[74];
        theStructure._moveNodeTo(102, c0);
        theStructure._moveNodeTo(107, c0);
        theStructure._moveNodeTo(49, c0);
        theStructure._moveNodeTo(84, c0);
        theStructure._moveNodeTo(82, c0);
        theStructure._moveNodeTo(77, c0);
        theStructure._moveNodeTo(72, c0);
        theStructure._moveNodeTo(2, c0);
        theStructure._moveNodeTo(98, c0);
        theStructure._moveNodeTo(10, c0);
        StatisticalInferenceClustering.Community c1 = theStructure.nodeCommunities[30];
        theStructure._moveNodeTo(19, c1);
        theStructure._moveNodeTo(60, c1);
        theStructure._moveNodeTo(71, c1);
        theStructure._moveNodeTo(18, c1);
        theStructure._moveNodeTo(99, c1);
        theStructure._moveNodeTo(35, c1);
        theStructure._moveNodeTo(79, c1);
        theStructure._moveNodeTo(38, c1);
        theStructure._moveNodeTo(85, c1);
        theStructure._moveNodeTo(28, c1);
        theStructure._moveNodeTo(55, c1);
        theStructure._moveNodeTo(6, c1);
        theStructure._moveNodeTo(31, c1);
        theStructure._moveNodeTo(54, c1);
        StatisticalInferenceClustering.Community c2 = theStructure.nodeCommunities[20];
        theStructure._moveNodeTo(36, c2);
        theStructure._moveNodeTo(75, c2);
        theStructure._moveNodeTo(48, c2);
        theStructure._moveNodeTo(92, c2);
        theStructure._moveNodeTo(58, c2);
        theStructure._moveNodeTo(59, c2);
        theStructure._moveNodeTo(113, c2);
        StatisticalInferenceClustering.Community c3 = theStructure.nodeCommunities[68];
        theStructure._moveNodeTo(8, c3);
        theStructure._moveNodeTo(22, c3);
        theStructure._moveNodeTo(78, c3);
        theStructure._moveNodeTo(51, c3);
        theStructure._moveNodeTo(111, c3);
        theStructure._moveNodeTo(40, c3);
        theStructure._moveNodeTo(7, c3);
        theStructure._moveNodeTo(21, c3);
        theStructure._moveNodeTo(108, c3);
        StatisticalInferenceClustering.Community c4 = theStructure.nodeCommunities[70];
        theStructure._moveNodeTo(87, c4);
        theStructure._moveNodeTo(64, c4);
        theStructure._moveNodeTo(63, c4);
        theStructure._moveNodeTo(97, c4);
        theStructure._moveNodeTo(24, c4);
        theStructure._moveNodeTo(66, c4);
        theStructure._moveNodeTo(56, c4);
        theStructure._moveNodeTo(65, c4);
        theStructure._moveNodeTo(27, c4);
        theStructure._moveNodeTo(95, c4);
        theStructure._moveNodeTo(76, c4);
        theStructure._moveNodeTo(96, c4);
        theStructure._moveNodeTo(57, c4);
        theStructure._moveNodeTo(91, c4);
        theStructure._moveNodeTo(86, c4);
        theStructure._moveNodeTo(53, c4);
        theStructure._moveNodeTo(17, c4);
        theStructure._moveNodeTo(12, c4);
        theStructure._moveNodeTo(44, c4);
        theStructure._moveNodeTo(112, c4);
        StatisticalInferenceClustering.Community c5 = theStructure.nodeCommunities[103];
        theStructure._moveNodeTo(109, c5);
        theStructure._moveNodeTo(37, c5);
        theStructure._moveNodeTo(89, c5);
        theStructure._moveNodeTo(33, c5);
        theStructure._moveNodeTo(105, c5);
        theStructure._moveNodeTo(25, c5);
        theStructure._moveNodeTo(106, c5);
        theStructure._moveNodeTo(62, c5);
        theStructure._moveNodeTo(45, c5);
        theStructure._moveNodeTo(1, c5);
        theStructure._moveNodeTo(101, c5);
        StatisticalInferenceClustering.Community c6 = theStructure.nodeCommunities[23];
        theStructure._moveNodeTo(0, c6);
        theStructure._moveNodeTo(93, c6);
        theStructure._moveNodeTo(9, c6);
        theStructure._moveNodeTo(16, c6);
        theStructure._moveNodeTo(81, c6);
        theStructure._moveNodeTo(41, c6);
        theStructure._moveNodeTo(50, c6);
        theStructure._moveNodeTo(90, c6);
        theStructure._moveNodeTo(5, c6);
        theStructure._moveNodeTo(4, c6);
        StatisticalInferenceClustering.Community c7 = theStructure.nodeCommunities[100];
        theStructure._moveNodeTo(39, c7);
        theStructure._moveNodeTo(43, c7);
        theStructure._moveNodeTo(14, c7);
        theStructure._moveNodeTo(32, c7);
        theStructure._moveNodeTo(47, c7);
        theStructure._moveNodeTo(42, c7);
        theStructure._moveNodeTo(34, c7);
        theStructure._moveNodeTo(94, c7);
        theStructure._moveNodeTo(13, c7);
        theStructure._moveNodeTo(15, c7);
        theStructure._moveNodeTo(26, c7);
        theStructure._moveNodeTo(61, c7);
        theStructure._moveNodeTo(29, c7);
        theStructure._moveNodeTo(80, c7);
        StatisticalInferenceClustering.Community c8 = theStructure.nodeCommunities[67];
        theStructure._moveNodeTo(88, c8);
        theStructure._moveNodeTo(69, c8);
        theStructure._moveNodeTo(83, c8);
        theStructure._moveNodeTo(73, c8);
        theStructure._moveNodeTo(114, c8);
        theStructure._moveNodeTo(104, c8);
        theStructure._moveNodeTo(11, c8);
        theStructure._moveNodeTo(52, c8);
        theStructure._moveNodeTo(3, c8);
        theStructure._moveNodeTo(46, c8);
        theStructure._moveNodeTo(110, c8);

        double descriptionLength_before = sic.computeDescriptionLength(graph, theStructure);

        theStructure._zoomOut();

        double descriptionLength_after = sic.computeDescriptionLength(graph, theStructure);

        assertEquals(descriptionLength_before, descriptionLength_after, 0.00001);
    }

}

