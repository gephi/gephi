package org.gephi.io.exporter.plugin;

import java.io.IOException;
import org.gephi.graph.GraphGenerator;
import org.gephi.project.api.Workspace;
import org.junit.Test;

public class JsonTest {

    @Test
    public void testTest() throws IOException {
        GraphGenerator graphGenerator =
            GraphGenerator.build().withWorkspace().generateTinyGraph().addDoubleNodeColumn();

        Utils.print(createExporter(graphGenerator));
    }

    private static ExporterJson createExporter(GraphGenerator graphGenerator) {
        Workspace workspace = graphGenerator.getWorkspace();
        ExporterJson exporterJson = new ExporterJson();
        exporterJson.setWorkspace(workspace);
        exporterJson.setExportMeta(false);
        return exporterJson;
    }
}
