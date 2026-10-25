package org.gephi.integration;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.gephi.graph.api.GraphModel;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.EdgeDirectionDefault;
import org.gephi.io.importer.plugin.file.ImporterGEXF;
import org.gephi.io.importer.plugin.file.ImporterGML;
import org.gephi.io.importer.spi.FileImporter;
import org.gephi.project.api.Workspace;
import org.junit.Assert;
import org.junit.Test;

public class ImportTest extends AbstractIntegrationTest {

    @Test
    public void testImporterSelectedByExtension() throws IOException {
        Map<String, Class<? extends FileImporter>> fixtures = Map.of(
            "lesmiserables.gml", ImporterGML.class,
            "LesMiserables.gexf", ImporterGEXF.class);
        for (Map.Entry<String, Class<? extends FileImporter>> entry : fixtures.entrySet()) {
            File file = fixture(entry.getKey());
            Assert.assertEquals(entry.getKey(), entry.getValue(), importController.getFileImporter(file).getClass());

            Container container = importController.importFile(file);
            container.getLoader().setEdgeDefault(EdgeDirectionDefault.DIRECTED);
            container.getLoader().setAllowAutoNode(false);
            Workspace workspace = importController.process(container);

            GraphModel graphModel = graphController.getGraphModel(workspace);
            Assert.assertEquals(entry.getKey(), 77, graphModel.getGraph().getNodeCount());
            Assert.assertEquals(entry.getKey(), 254, graphModel.getGraph().getEdgeCount());
        }
    }
}
