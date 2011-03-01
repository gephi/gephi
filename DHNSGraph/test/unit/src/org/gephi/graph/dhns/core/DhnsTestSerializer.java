/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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
package org.gephi.graph.dhns.core;

import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLReporter;
import java.io.StringReader;
import javax.xml.stream.XMLOutputFactory;
import java.io.StringWriter;
import java.util.HashMap;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.graph.api.Node;
import org.gephi.graph.dhns.DhnsGraphController;
import org.gephi.graph.dhns.edge.AbstractEdge;
import org.gephi.graph.dhns.graph.HierarchicalDirectedGraphImpl;
import org.gephi.graph.dhns.node.AbstractNode;
import org.gephi.graph.dhns.utils.DHNSSerializer;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
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
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        Workspace workspace = pc.getCurrentWorkspace();
        Lookup.getDefault().lookup(AttributeController.class).getModel();

        //Graph 1 - Multilevel sample without edges
        DhnsGraphController controller1 = new DhnsGraphController();
        dhns1 = new Dhns(controller1, workspace);
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
        Workspace workspace2 = pc.newWorkspace(pc.getCurrentProject());
        pc.openWorkspace(workspace2);
        Lookup.getDefault().lookup(AttributeController.class).getModel();
        nodeMap2 = new HashMap<String, Node>();
        DhnsGraphController controller2 = new DhnsGraphController();
        dhns2 = new Dhns(controller2, workspace2);
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
    public void testDhnsSerializer() {
        try {
            DHNSSerializer dHNSSerializer = new DHNSSerializer();
            StringWriter stringWriter = new StringWriter();
            dHNSSerializer.writeDhns(createWriter(stringWriter), dhns2);
            String s1 = stringWriter.toString();
            ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
            Workspace workspace3 = pc.newWorkspace(pc.getCurrentProject());
            pc.openWorkspace(workspace3);
            Lookup.getDefault().lookup(AttributeController.class).getModel();
            Dhns d2 = new Dhns(new DhnsGraphController(), workspace3);
            StringReader stringReader = new StringReader(s1);
            dHNSSerializer.readDhns(createReader(stringReader), d2);
            stringWriter = new StringWriter();
            dHNSSerializer.writeDhns(createWriter(stringWriter), d2);
            String s2 = stringWriter.toString();
            assertEquals(s1, s2);
        } catch (XMLStreamException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private XMLStreamWriter createWriter(StringWriter stringWriter) {
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        outputFactory.setProperty("javax.xml.stream.isRepairingNamespaces", Boolean.FALSE);

        try {
            XMLStreamWriter xmlWriter = outputFactory.createXMLStreamWriter(stringWriter);
            xmlWriter.writeStartDocument("UTF-8", "1.0");
            return xmlWriter;
        } catch (XMLStreamException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    private XMLStreamReader createReader(StringReader stringReader) {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        if (inputFactory.isPropertySupported("javax.xml.stream.isValidating")) {
            inputFactory.setProperty("javax.xml.stream.isValidating", Boolean.FALSE);
        }
        inputFactory.setXMLReporter(new XMLReporter() {

            @Override
            public void report(String message, String errorType, Object relatedInformation, Location location) throws XMLStreamException {
                System.out.println("Error:" + errorType + ", message : " + message);
            }
        });
        try {
            return inputFactory.createXMLStreamReader(stringReader);
        } catch (XMLStreamException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
}
