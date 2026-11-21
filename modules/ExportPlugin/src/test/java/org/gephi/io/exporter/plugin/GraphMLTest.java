package org.gephi.io.exporter.plugin;

import java.io.StringReader;
import javax.xml.parsers.DocumentBuilderFactory;
import org.gephi.graph.GraphGenerator;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.project.api.Workspace;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class GraphMLTest {

    @Test
    public void testInvalidXmlCharsAreStripped() throws Exception {
        GraphGenerator graphGenerator = GraphGenerator.build().generateTinyGraph().addStringNodeColumn();
        GraphModel graphModel = graphGenerator.getGraphModel();
        Graph graph = graphModel.getGraph();

        Node node = graph.getNode(GraphGenerator.FIRST_NODE);
        node.setLabel("foo\u0001bar");
        node.setAttribute(GraphGenerator.STRING_COLUMN, "baz\u0000qux");
        Node otherNode = graphModel.factory().newNode("id\u0001x");
        graph.addNode(otherNode);
        graph.addEdge(graphModel.factory().newEdge(node, otherNode, true));
        Edge edge = graph.getEdge(GraphGenerator.FIRST_EDGE);
        edge.setLabel("edge\u000Blabel");

        Document document = parse(Utils.toString(createExporter(graphGenerator)));

        Element nodeE = findById(document.getElementsByTagName("node"), GraphGenerator.FIRST_NODE);
        Assert.assertEquals("foobar", findData(nodeE, "label"));
        Assert.assertEquals("bazqux", findData(nodeE, GraphGenerator.STRING_COLUMN));
        Element edgeE = findById(document.getElementsByTagName("edge"), GraphGenerator.FIRST_EDGE);
        Assert.assertEquals("edgelabel", findData(edgeE, "edgelabel"));
        Assert.assertNotNull(findById(document.getElementsByTagName("node"), "idx"));
        Assert.assertEquals(1, countByAttribute(document.getElementsByTagName("edge"), "target", "idx"));
    }

    private static Document parse(String xml) throws Exception {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
    }

    private static Element findById(NodeList elements, String id) {
        for (int i = 0; i < elements.getLength(); i++) {
            Element element = (Element) elements.item(i);
            if (id.equals(element.getAttribute("id"))) {
                return element;
            }
        }
        return null;
    }

    private static int countByAttribute(NodeList elements, String attribute, String value) {
        int count = 0;
        for (int i = 0; i < elements.getLength(); i++) {
            if (value.equals(((Element) elements.item(i)).getAttribute(attribute))) {
                count++;
            }
        }
        return count;
    }

    private static String findData(Element parent, String key) {
        NodeList dataElements = parent.getElementsByTagName("data");
        for (int i = 0; i < dataElements.getLength(); i++) {
            Element dataE = (Element) dataElements.item(i);
            if (key.equals(dataE.getAttribute("key"))) {
                return dataE.getTextContent();
            }
        }
        return null;
    }

    private static ExporterGraphML createExporter(GraphGenerator graphGenerator) {
        Workspace workspace = graphGenerator.getWorkspace();
        ExporterGraphML exporterGraphML = new ExporterGraphML();
        exporterGraphML.setExportSize(false);
        exporterGraphML.setExportColors(false);
        exporterGraphML.setExportPosition(false);
        exporterGraphML.setWorkspace(workspace);
        return exporterGraphML;
    }
}
