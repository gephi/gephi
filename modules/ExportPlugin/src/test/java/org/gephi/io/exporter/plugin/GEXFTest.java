package org.gephi.io.exporter.plugin;

import java.io.IOException;
import org.gephi.graph.GraphGenerator;
import org.gephi.graph.api.Graph;
import org.gephi.project.api.Workspace;
import org.junit.Test;

public class GEXFTest {

    @Test
    public void testInfinity() throws IOException {
        GraphGenerator graphGenerator = GraphGenerator.build().withWorkspace().generateTinyGraph().addDoubleNodeColumn();
        Graph graph = graphGenerator.getGraph();

        graph.getNode(GraphGenerator.FIRST_NODE).setAttribute(GraphGenerator.DOUBLE_COLUMN, Double.POSITIVE_INFINITY);
        graph.getNode(GraphGenerator.SECOND_NODE).setAttribute(GraphGenerator.DOUBLE_COLUMN, Double.NEGATIVE_INFINITY);

        Utils.assertExporterMatch("infinity.gexf", createExporter(graphGenerator));
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
