package org.gephi.io.exporter.plugin;

import org.gephi.graph.GraphGenerator;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.project.api.Workspace;
import org.junit.Test;

import java.awt.*;
import java.io.IOException;

public class JsonTest {

    @Test
    public void testEmpty() throws IOException {
        GraphGenerator graphGenerator =
                GraphGenerator.build();

        Utils.assertExporterMatch("json/empty.json", createExporter(graphGenerator));
    }

    @Test
    public void testTinyGraph() throws IOException {
        GraphGenerator graphGenerator =
                GraphGenerator.build().generateTinyGraph();

        Utils.assertExporterMatch("json/tiny.json", createExporter(graphGenerator));
    }

    @Test
    public void testNodeColumn() throws IOException {
        GraphGenerator graphGenerator =
                GraphGenerator.build().generateTinyGraph().addDoubleNodeColumn();

        Utils.assertExporterMatch("json/column.json", createExporter(graphGenerator));
    }

    @Test
    public void testLabels() throws IOException {
        GraphGenerator graphGenerator =
                GraphGenerator.build().generateTinyGraph().addNodeLabels().addEdgeLabels();

        Utils.assertExporterMatch("json/labels.json", createExporter(graphGenerator));
    }

    @Test
    public void testMulti() throws IOException {
        GraphGenerator graphGenerator =
                GraphGenerator.build().generateTinyMultiGraph();

        Utils.assertExporterMatch("json/multi.json", createExporter(graphGenerator));
    }

    @Test
    public void testUndirected() throws IOException {
        GraphGenerator graphGenerator =
                GraphGenerator.build().generateTinyUndirectedGraph();

        Utils.assertExporterMatch("json/undirected.json", createExporter(graphGenerator));
    }

    @Test
    public void testMixed() throws IOException {
        GraphGenerator graphGenerator =
                GraphGenerator.build().generateTinyMixedGraph();

        Utils.assertExporterMatch("json/mixed.json", createExporter(graphGenerator));
    }

    @Test
    public void testColors() throws IOException {
        GraphGenerator graphGenerator =
                GraphGenerator.build().generateTinyGraph();
        Graph graph = graphGenerator.getGraph();
        Node n1 = graph.getNode(GraphGenerator.FIRST_NODE);
        Node n2 = graph.getNode(GraphGenerator.SECOND_NODE);
        n1.setColor(Color.CYAN);
        n2.setColor(new Color(255, 100, 120, 254));

        ExporterJson exporterJson = createExporter(graphGenerator);
        exporterJson.setExportColors(true);
        Utils.assertExporterMatch("json/colors.json", exporterJson);
    }
    
    @Test
    public void testPosition() throws IOException {
                GraphGenerator graphGenerator =
                GraphGenerator.build().generateTinyGraphWithPosition();
        Graph graph = graphGenerator.getGraph();
        Node n1 = graph.getNode(GraphGenerator.FIRST_NODE);
        Node n2 = graph.getNode(GraphGenerator.SECOND_NODE);
        n1.setColor(Color.CYAN);
        n2.setColor(new Color(255, 100, 120, 254));

        ExporterJson exporterJson = createExporter(graphGenerator);
        exporterJson.setExportPosition(true);
        exporterJson.setNormalize(false);

        Utils.assertExporterMatch("json/position.json", exporterJson);
    }
    
    @Test
    public void testPositionNormalized() throws IOException {
                GraphGenerator graphGenerator =
                GraphGenerator.build().generateTinyGraphWithPosition();
        Graph graph = graphGenerator.getGraph();
        Node n1 = graph.getNode(GraphGenerator.FIRST_NODE);
        Node n2 = graph.getNode(GraphGenerator.SECOND_NODE);
        n1.setColor(Color.CYAN);
        n2.setColor(new Color(255, 100, 120, 254));

        ExporterJson exporterJson = createExporter(graphGenerator);
        exporterJson.setExportPosition(true);
        exporterJson.setNormalize(true);

        Utils.assertExporterMatch("json/position_normalized.json", exporterJson);
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
