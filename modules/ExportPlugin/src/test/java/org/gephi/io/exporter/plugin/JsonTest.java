package org.gephi.io.exporter.plugin;

import java.io.IOException;
import org.gephi.graph.GraphGenerator;
import org.gephi.project.api.Workspace;
import org.junit.Test;

public class JsonTest {

    @Test
    public void testEmpty() throws IOException {
        GraphGenerator graphGenerator =
            GraphGenerator.build().withWorkspace();

        Utils.assertExporterMatch("json/empty.json", createExporter(graphGenerator));
    }

    @Test
    public void testTinyGraph() throws IOException {
        GraphGenerator graphGenerator =
            GraphGenerator.build().withWorkspace().generateTinyGraph();

        Utils.assertExporterMatch("json/tiny.json", createExporter(graphGenerator));
    }

    @Test
    public void testNodeColumn() throws IOException {
        GraphGenerator graphGenerator =
            GraphGenerator.build().withWorkspace().generateTinyGraph().addDoubleNodeColumn();

        Utils.assertExporterMatch("json/column.json", createExporter(graphGenerator));
    }

    @Test
    public void testLabels() throws IOException {
        GraphGenerator graphGenerator =
            GraphGenerator.build().withWorkspace().generateTinyGraph().addNodeLabels().addEdgeLabels();

        Utils.assertExporterMatch("json/labels.json", createExporter(graphGenerator));
    }

    @Test
    public void testMulti() throws IOException {
        GraphGenerator graphGenerator =
            GraphGenerator.build().withWorkspace().generateTinyMultiGraph();

        Utils.assertExporterMatch("json/multi.json", createExporter(graphGenerator));
    }

    @Test
    public void testUndirected() throws IOException {
        GraphGenerator graphGenerator =
            GraphGenerator.build().withWorkspace().generateTinyUndirectedGraph();

        Utils.assertExporterMatch("json/undirected.json", createExporter(graphGenerator));
    }

    @Test
    public void testMixed() throws IOException {
        GraphGenerator graphGenerator =
            GraphGenerator.build().withWorkspace().generateTinyMixedGraph();

        Utils.assertExporterMatch("json/mixed.json", createExporter(graphGenerator));
    }

    private static ExporterJson createExporter(GraphGenerator graphGenerator) {
        Workspace workspace = graphGenerator.getWorkspace();
        ExporterJson exporterJson = new ExporterJson();
        exporterJson.setWorkspace(workspace);
        exporterJson.setExportMeta(false);
        exporterJson.setExportColors(false);
        exporterJson.setExportPosition(false);
        exporterJson.setExportSize(false);
        return exporterJson;
    }
}
