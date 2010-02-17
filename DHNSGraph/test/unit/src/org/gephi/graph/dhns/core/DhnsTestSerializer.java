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
package org.gephi.graph.dhns.core;

import java.io.StringWriter;
import java.util.HashMap;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.gephi.graph.api.Node;
import org.gephi.graph.dhns.DhnsGraphController;
import org.gephi.graph.dhns.edge.AbstractEdge;
import org.gephi.graph.dhns.graph.HierarchicalDirectedGraphImpl;
import org.gephi.graph.dhns.node.AbstractNode;
import org.gephi.graph.dhns.utils.DHNSSerializer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openide.util.Exceptions;
import org.w3c.dom.Element;
import static org.junit.Assert.*;

/**
 *
 * @author Mathieu Bastian
 */
public class DhnsTestSerializer {

    private Dhns dhns1;
    private HierarchicalDirectedGraphImpl graph1;
    private Dhns dhns2;
    private HierarchicalDirectedGraphImpl graph2;
    private HashMap<String, Node> nodeMap2;

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        //Graph 1 - Multilevel sample without edges
        DhnsGraphController controller1 = new DhnsGraphController();
        dhns1 = new Dhns(controller1, null);
        graph1 = new HierarchicalDirectedGraphImpl(dhns1, dhns1.getGraphStructure().getMainView());
        GraphFactoryImpl factory1 = dhns1.factory();

        AbstractNode nodeA = factory1.newNode();
        AbstractNode nodeB = factory1.newNode();
        AbstractNode nodeC = factory1.newNode();
        AbstractNode nodeD = factory1.newNode();
        AbstractNode nodeE = factory1.newNode();
        graph1.addNode(nodeA);
        graph1.addNode(nodeB);
        graph1.addNode(nodeC, nodeA);
        graph1.addNode(nodeE, nodeB);
        graph1.addNode(nodeD, nodeA);
        graph1.addNode(nodeD, nodeB);

        //Graph2 - Directed sample with edges
        nodeMap2 = new HashMap<String, Node>();
        DhnsGraphController controller2 = new DhnsGraphController();
        dhns2 = new Dhns(controller2, null);
        graph2 = new HierarchicalDirectedGraphImpl(dhns2, dhns2.getGraphStructure().getMainView());
        GraphFactoryImpl factory2 = dhns2.factory();

        for (int i = 0; i < 10; i++) {
            Node node = factory2.newNode();
            node.getNodeData().setLabel("Node " + i);
            graph2.addNode(node);
            nodeMap2.put(node.getNodeData().getLabel(), node);
        }
        Node node4 = nodeMap2.get("Node 4");
        Node node5 = nodeMap2.get("Node 5");
        Node node6 = nodeMap2.get("Node 6");
        Node node7 = nodeMap2.get("Node 7");

        AbstractEdge edge1 = factory2.newEdge(node4, node5);
        AbstractEdge edge2 = factory2.newEdge(node5, node6);
        AbstractEdge edge3 = factory2.newEdge(node6, node5);
        AbstractEdge edge4 = factory2.newEdge(node7, node7);
        AbstractEdge edge5 = factory2.newEdge(node4, node4);
        graph2.addEdge(edge1);
        graph2.addEdge(edge2);
        graph2.addEdge(edge3);
        graph2.addEdge(edge4);
        graph2.addEdge(edge5);
    }

    @After
    public void tearDown() {
        dhns1 = null;
        graph1 = null;
    }

    @Test
    public void testTreeStructureSerializer() {
        DHNSSerializer dHNSSerializer = new DHNSSerializer();
        Element e1 = dHNSSerializer.writeTreeStructure(dHNSSerializer.createDocument(), dhns1.getGraphStructure().getMainView());
        String s1 = printXML(e1);
        graph1.clear();
        dHNSSerializer.readTreeStructure(e1, dhns1.getGraphStructure(), dhns1.factory());
        Element e2 = dHNSSerializer.writeTreeStructure(dHNSSerializer.createDocument(), dhns1.getGraphStructure().getMainView());
        String s2 = printXML(e2);
        assertEquals(s1, s2);
    }

    @Test
    public void testEdgesSerializer() {
        DHNSSerializer dHNSSerializer = new DHNSSerializer();
        Element e1 = dHNSSerializer.writeEdges(dHNSSerializer.createDocument(), dhns2.getGraphStructure().getMainView().getStructure());
        String s1 = printXML(e1);
        graph2.clearEdges();
        dHNSSerializer.readEdges(e1, dhns2.getGraphStructure(), dhns2.factory());
        Element e2 = dHNSSerializer.writeEdges(dHNSSerializer.createDocument(), dhns2.getGraphStructure().getMainView().getStructure());
        String s2 = printXML(e2);
        assertEquals(s1, s2);
    }

    @Test
    public void testDhnsSerializer() {
        DHNSSerializer dHNSSerializer = new DHNSSerializer();
        Element e1 = dHNSSerializer.writeDhns(dHNSSerializer.createDocument(), dhns2);
        String s1 = printXML(e1);
        Dhns d2 = new Dhns(new DhnsGraphController(), null);
        dHNSSerializer.readDhns(e1, d2);
        Element e2 = dHNSSerializer.writeDhns(dHNSSerializer.createDocument(), d2);
        String s2 = printXML(e2);
        assertEquals(s1, s2);
    }

    private String printXML(org.w3c.dom.Node node) {
        try {
            Source source = new DOMSource(node);
            StringWriter writer = new StringWriter();
            Result result = new StreamResult(writer);
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.transform(source, result);
            return writer.toString();
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
}
