package org.gephi.io.exporter.plugin;

import java.io.IOException;
import org.gephi.graph.GraphGenerator;
import org.gephi.graph.api.Configuration;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.TimeRepresentation;
import org.gephi.project.api.Workspace;
import org.junit.Assert;
import org.junit.Test;

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
    public void testInvalidXmlCharsAreStripped() throws IOException {
        GraphGenerator graphGenerator = GraphGenerator.build().generateTinyGraph().addStringNodeColumn();
        Graph graph = graphGenerator.getGraph();

        Node node = graph.getNode(GraphGenerator.FIRST_NODE);
        node.setLabel("foo\bbar");
        node.setAttribute(GraphGenerator.STRING_COLUMN, "baz\0qux");
        Edge edge = graph.getEdge(GraphGenerator.FIRST_EDGE);
        edge.setLabel("edge\blabel");

        String str = Utils.toString(createExporter(graphGenerator));
        Assert.assertFalse(str.contains("\b"));
        Assert.assertFalse(str.contains("\0"));
        Assert.assertTrue(str.contains("label=\"foobar\""));
        Assert.assertTrue(str.contains("value=\"bazqux\""));
        Assert.assertTrue(str.contains("label=\"edgelabel\""));
    }

    @Test
    public void testTimestampSetColumn() throws IOException {
        // A regular attribute column of type TimestampSet, as opposed to the 'timeset' property
        GraphGenerator graphGenerator = GraphGenerator.build(timestampConfiguration()).generateTinyGraph().addTimestampSetColumn();

        Utils.assertExporterMatch("gexf/timestampset_column.gexf", createExporter(graphGenerator));
    }

    @Test
    public void testTimestampSetColumnDynamicGraph() throws IOException {
        // Same column next to the 'timeset' property, which is still written as node timestamps
        GraphGenerator graphGenerator =
            GraphGenerator.build(timestampConfiguration()).generateTinyGraph().setTimestampSet()
                .addTimestampSetColumn();

        String str = Utils.toString(createExporter(graphGenerator));
        Assert.assertTrue(str.contains("<node id=\"1\" timestamp=\"3.0\">"));
        Assert.assertTrue(str.contains("<attribute id=\"events\" title=\"events\" type=\"string\""));
        Assert.assertTrue(str.contains("<attvalue for=\"events\" value=\"&lt;[3.0]>\""));
        Assert.assertTrue(str.contains("</gexf>"));
    }

    private static Configuration timestampConfiguration() {
        return Configuration.builder().timeRepresentation(TimeRepresentation.TIMESTAMP).build();
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
