package org.gephi.io.exporter.plugin;

import java.io.IOException;
import org.gephi.graph.GraphGenerator;
import org.gephi.graph.api.Graph;
import org.gephi.project.api.Workspace;
import org.junit.Test;

public class VNATest {

    @Test
    public void testBasic() throws IOException {
        GraphGenerator graphGenerator = GraphGenerator.build().withWorkspace().generateTinyGraph().addDoubleNodeColumn();

        Utils.assertExporterMatch("basic.vna", createExporter(graphGenerator));
    }

    private static ExporterVNA createExporter(GraphGenerator graphGenerator) {
        Workspace workspace = graphGenerator.getWorkspace();
        ExporterVNA exporterVNA = new ExporterVNA();
        exporterVNA.setWorkspace(workspace);
        return exporterVNA;
    }
}
