/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
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
package org.gephi.graph.dhns.filter;

import java.util.HashMap;
import java.util.Map;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.gephi.graph.dhns.DhnsGraphController;
import org.gephi.graph.dhns.core.Dhns;
import org.gephi.graph.dhns.core.GraphFactoryImpl;
import org.gephi.graph.dhns.core.TreeStructure;
import org.gephi.graph.dhns.edge.AbstractEdge;
import org.gephi.graph.dhns.graph.ClusteredDirectedGraphImpl;
import org.gephi.graph.dhns.views.ViewImpl;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class DhnsTestFiltering {

    private Dhns dhnsGlobal;
    private ClusteredDirectedGraphImpl graphGlobal;
    private Map<String, Node> nodeMap;
    private Map<String, Edge> edgeMap;

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        DhnsGraphController controller = new DhnsGraphController();
        dhnsGlobal = new Dhns(controller);
        graphGlobal = new ClusteredDirectedGraphImpl(dhnsGlobal, dhnsGlobal.getGraphStructure(), new ViewImpl(dhnsGlobal));
        nodeMap = new HashMap<String, Node>();
        edgeMap = new HashMap<String, Edge>();

        TreeStructure treeStructure = dhnsGlobal.getGraphStructure().getStructure();
        GraphFactoryImpl factory = dhnsGlobal.factory();

        //Nodes
        //System.out.println("-----Global-----");
        for (int i = 0; i < 10; i++) {
            Node node = factory.newNode();
            node.getNodeData().setLabel("Node " + i);
            graphGlobal.addNode(node);
            nodeMap.put(node.getNodeData().getLabel(), node);
        //System.out.println("Node " + i + " added. Id = " + node.getId());
        }
        //System.out.println("---End Global---");

        //Edges
        Node node0 = nodeMap.get("Node 0");
        Node node1 = nodeMap.get("Node 1");
        Node node2 = nodeMap.get("Node 2");
        Node node3 = nodeMap.get("Node 3");
        Node node4 = nodeMap.get("Node 4");
        Node node5 = nodeMap.get("Node 5");
        Node node6 = nodeMap.get("Node 6");
        Node node7 = nodeMap.get("Node 7");
        Node node8 = nodeMap.get("Node 8");

        AbstractEdge edge1 = factory.newEdge(node4, node5, 1f, true);
        AbstractEdge edge2 = factory.newEdge(node5, node6, 4f, true);
        AbstractEdge edge3 = factory.newEdge(node6, node5, 3f, true);
        AbstractEdge edge4 = factory.newEdge(node7, node7, 5f, true);
        AbstractEdge edge5 = factory.newEdge(node4, node4, 2f, true);
        AbstractEdge edge6 = factory.newEdge(node2, node1, 1f, true);
        AbstractEdge edge7 = factory.newEdge(node2, node3, 10f, true);
        AbstractEdge edge8 = factory.newEdge(node2, node5, 12f, true);

        graphGlobal.addEdge(edge1);
        graphGlobal.addEdge(edge2);
        graphGlobal.addEdge(edge3);
        graphGlobal.addEdge(edge4);
        graphGlobal.addEdge(edge5);
        graphGlobal.addEdge(edge6);
        graphGlobal.addEdge(edge7);
        graphGlobal.addEdge(edge8);

        edgeMap.put("4-5", edge1);
        edgeMap.put("5-6", edge2);
        edgeMap.put("6-5", edge3);
        edgeMap.put("7-7", edge4);
        edgeMap.put("4-4", edge5);
        edgeMap.put("2-1", edge6);
        edgeMap.put("2-3", edge7);
        edgeMap.put("2-5", edge8);
    }

    @Test
    public void testSetUp() {
        Node[] expected = new Node[10];
        for (int i = 0; i < nodeMap.size(); i++) {
            expected[i] = nodeMap.get("Node " + i);
        }
        Node[] actual = graphGlobal.getNodes().toArray();
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testFiltering() {
        graphGlobal.getView().addPredicate(new DegreePredicate(3, 5));
        Node[] actual = graphGlobal.getNodes().toArray();
        for(int i=0;i<actual.length;i++) {
            System.out.println(actual[i].getId());
        }
    }
}
