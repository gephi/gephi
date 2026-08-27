package org.gephi.io.exporter.plugin;

import java.io.IOException;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilderFactory;
import org.gephi.graph.GraphGenerator;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.project.api.Workspace;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class GEXFTest {

    @Test
    public void testInfinity() throws IOException {
        GraphGenerator graphGenerator = GraphGenerator.build().generateTinyGraph().addDoubleNodeColumn();
        Graph graph = graphGenerator.getGraph();

        graph.getNode(GraphGenerator.FIRST_NODE).setAttribute(GraphGenerator.DOUBLE_COLUMN, Double.POSITIVE_INFINITY);
        graph.getNode(GraphGenerator.SECOND_NODE).setAttribute(GraphGenerator.DOUBLE_COLUMN, Double.NEGATIVE_INFINITY);

        Utils.assertExporterMatch("gexf/infinity.gexf", createExporter(graphGenerator));
    }

    @Test
    public void testIncludeAttValues() throws IOException {
        GraphGenerator graphGenerator = GraphGenerator.build().generateTinyGraph().addDoubleNodeColumn();

        Graph graph = graphGenerator.getGraph();

        graph.getNode(GraphGenerator.FIRST_NODE).setAttribute(GraphGenerator.DOUBLE_COLUMN, null);
        ExporterGEXF exporterGEXF = createExporter(graphGenerator);
        exporterGEXF.setIncludeNullAttValues(true);
        Utils.assertExporterMatch("gexf/includenullattvalues.gexf", exporterGEXF);
    }

    @Test
    public void testMeta() throws IOException {
        GraphGenerator graphGenerator = GraphGenerator.build();
        Workspace workspace = graphGenerator.getWorkspace();
        workspace.getWorkspaceMetadata().setTitle("title");
        workspace.getWorkspaceMetadata().setDescription("desc");

        ExporterGEXF exporterGEXF = createExporter(graphGenerator);
        exporterGEXF.setExportMeta(true);
        String str = Utils.toString(exporterGEXF);
        Assert.assertTrue(str.contains("<meta "));
        Assert.assertTrue(str.contains("<title>title</title>"));
        Assert.assertTrue(str.contains("<description>desc</description>"));
    }

    @Test
    public void testControlCharsInLabelsAndAttValuesArePreserved() throws Exception {
        GraphGenerator graphGenerator = GraphGenerator.build().generateTinyGraph().addStringNodeColumn();
        Graph graph = graphGenerator.getGraph();

        Node node = graph.getNode(GraphGenerator.FIRST_NODE);
        node.setLabel("foo\bbar");
        node.setAttribute(GraphGenerator.STRING_COLUMN, "baz\bqux");
        Edge edge = graph.getEdge(GraphGenerator.FIRST_EDGE);
        edge.setLabel("edge\blabel");

        String str = Utils.toString(createExporter(graphGenerator));
        Document doc = parse(str);

        Assert.assertEquals("foo\bbar", findById(doc, "node", "1").getAttribute("label"));
        Assert.assertEquals("edge\blabel", findById(doc, "edge", "1").getAttribute("label"));
        Element attvalue = (Element) findById(doc, "node", "1").getElementsByTagName("attvalue").item(0);
        Assert.assertEquals("baz\bqux", attvalue.getAttribute("value"));
    }

    @Test
    public void testNullCharacterIsStripped() throws IOException {
        GraphGenerator graphGenerator = GraphGenerator.build().generateTinyGraph();
        Graph graph = graphGenerator.getGraph();
        graph.getNode(GraphGenerator.FIRST_NODE).setLabel("foo\0bar");

        String str = Utils.toString(createExporter(graphGenerator));
        Assert.assertFalse(str.contains("\0"));
        Assert.assertTrue(str.contains("label=\"foobar\""));
    }

    private static Document parse(String xml) throws Exception {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
    }

    private static Element findById(Document doc, String tagName, String id) {
        NodeList elements = doc.getElementsByTagName(tagName);
        for (int i = 0; i < elements.getLength(); i++) {
            Element element = (Element) elements.item(i);
            if (id.equals(element.getAttribute("id"))) {
                return element;
            }
        }
        throw new AssertionError("No <" + tagName + "> element with id=" + id);
    }

    private static ExporterGEXF createExporter(GraphGenerator graphGenerator) {
        Workspace workspace = graphGenerator.getWorkspace();
        ExporterGEXF exporterGEXF = new ExporterGEXF();
        exporterGEXF.setExportSize(false);
        exporterGEXF.setExportColors(false);
        exporterGEXF.setExportPosition(false);
        exporterGEXF.setExportMeta(false);
        exporterGEXF.setWorkspace(workspace);
        return exporterGEXF;
    }
}
