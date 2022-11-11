package org.gephi.io.exporter.plugin;

import java.io.IOException;
import org.gephi.graph.GraphGenerator;
import org.gephi.graph.api.Graph;
import org.gephi.project.api.Workspace;
import org.junit.Assert;
import org.junit.Test;

public class GEXFTest {

    @Test
    public void testInfinity() throws IOException {
        GraphGenerator graphGenerator = GraphGenerator.build().withWorkspace().generateTinyGraph().addDoubleNodeColumn();
        Graph graph = graphGenerator.getGraph();

        graph.getNode(GraphGenerator.FIRST_NODE).setAttribute(GraphGenerator.DOUBLE_COLUMN, Double.POSITIVE_INFINITY);
        graph.getNode(GraphGenerator.SECOND_NODE).setAttribute(GraphGenerator.DOUBLE_COLUMN, Double.NEGATIVE_INFINITY);

        Utils.assertExporterMatch("gexf/infinity.gexf", createExporter(graphGenerator));
    }

    @Test
    public void testIncludeAttValues() throws IOException {
        GraphGenerator graphGenerator = GraphGenerator.build().withWorkspace().generateTinyGraph().addDoubleNodeColumn();

        Graph graph = graphGenerator.getGraph();

        graph.getNode(GraphGenerator.FIRST_NODE).setAttribute(GraphGenerator.DOUBLE_COLUMN, null);
        ExporterGEXF exporterGEXF = createExporter(graphGenerator);
        exporterGEXF.setIncludeNullAttValues(true);
        Utils.assertExporterMatch("gexf/includenullattvalues.gexf", exporterGEXF);
    }

    @Test
    public void testMeta() throws IOException {
        GraphGenerator graphGenerator = GraphGenerator.build().withWorkspace();
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
