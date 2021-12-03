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
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.UndirectedGraph;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

/**
 * @author Mathieu Jacomy
 */
public class StatisticalInferenceTest extends TestCase {

    /*@Test
    public void testTwoConnectedNodesStatInf() {
        GraphModel graphModel = GraphGenerator.generateCompleteUndirectedGraph(2);
        UndirectedGraph graph = graphModel.getUndirectedGraph();

        StatisticalInferenceClustering sic = new StatisticalInferenceClustering();

        StatisticalInferenceClustering.CommunityStructure theStructure = sic.new CommunityStructure(graph);
        int[] comStructure = new int[graph.getNodeCount()];

        HashMap<String, Double> sicValues = sic.computePartition(graph, theStructure, comStructure,
                false);

        double descriptionLength = sicValues.get("descriptionLength");
        int class1 = comStructure[0];
        int class2 = comStructure[1];

        System.out.println("################## Description length found:" + descriptionLength);
        //assertEquals(descriptionLength, 0.0);
        assertEquals(class1, class2);
    }*/

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
        assertTrue(descriptionLength_atInit - 36.0896 < 0.0001);

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

    /*@Test
    public void testCyclicWithWeightsGraphModularity() {
        GraphModel graphModel = GraphModel.Factory.newInstance();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();

        Node node1 = graphModel.factory().newNode("0");
        Node node2 = graphModel.factory().newNode("1");
        Node node3 = graphModel.factory().newNode("2");
        Node node4 = graphModel.factory().newNode("3");
        Node node5 = graphModel.factory().newNode("4");
        Node node6 = graphModel.factory().newNode("5");
        Node node7 = graphModel.factory().newNode("6");
        Node node8 = graphModel.factory().newNode("7");

        undirectedGraph.addNode(node1);
        undirectedGraph.addNode(node2);
        undirectedGraph.addNode(node3);
        undirectedGraph.addNode(node4);
        undirectedGraph.addNode(node5);
        undirectedGraph.addNode(node6);
        undirectedGraph.addNode(node7);
        undirectedGraph.addNode(node8);

        //Test 3 parallel edges summing weight = 10
        //Related issues ==> #1419 Getting null pointer error when trying to calculate modularity; #1526 NullPointerException on Modularity Statistics with gexf with kind / parallel nodes
        Edge edge12_1 = graphModel.factory().newEdge(node1, node2, 1, 2.f, false);
        Edge edge12_2 = graphModel.factory().newEdge(node1, node2, 2, 5.f, false);
        Edge edge12_3 = graphModel.factory().newEdge(node1, node2, 2, 3.f, false);

        Edge edge23 = graphModel.factory().newEdge(node2, node3, false);
        Edge edge34 = graphModel.factory().newEdge(node3, node4, 0, 10.f, false);
        Edge edge45 = graphModel.factory().newEdge(node4, node5, false);
        Edge edge56 = graphModel.factory().newEdge(node5, node6, 0, 10.f, false);
        Edge edge67 = graphModel.factory().newEdge(node6, node7, false);

        //Test 2 parallel edges summing weight = 10
        Edge edge78_1 = graphModel.factory().newEdge(node7, node8, 0, 5.f, false);
        Edge edge78_2 = graphModel.factory().newEdge(node7, node8, 0, 5.f, false);
        Edge edge81 = graphModel.factory().newEdge(node8, node1, false);

        undirectedGraph.addEdge(edge12_1);
        undirectedGraph.addEdge(edge12_2);
        undirectedGraph.addEdge(edge12_3);
        undirectedGraph.addEdge(edge23);
        undirectedGraph.addEdge(edge34);
        undirectedGraph.addEdge(edge45);
        undirectedGraph.addEdge(edge56);
        undirectedGraph.addEdge(edge67);
        undirectedGraph.addEdge(edge78_1);
        undirectedGraph.addEdge(edge78_2);
        undirectedGraph.addEdge(edge81);

        UndirectedGraph graph = graphModel.getUndirectedGraph();

        StatisticalInferenceClustering sic = new StatisticalInferenceClustering();

        StatisticalInferenceClustering.CommunityStructure theStructure = sic.new CommunityStructure(graph);
        int[] comStructure = new int[graph.getNodeCount()];

        HashMap<String, Double> sicValues = sic.computePartition(graph, theStructure, comStructure,
                false);

        double descriptionLength = sicValues.get("descriptionLength");


    }*/
}
