package org.gephi.integration;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.gephi.graph.GraphGenerator;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.io.exporter.preview.PDFExporter;
import org.gephi.io.exporter.spi.CharacterExporter;
import org.gephi.io.importer.api.Container;
import org.gephi.project.api.Project;
import org.gephi.project.api.Workspace;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ExportTest extends AbstractIntegrationTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void testExportGexfRoundTrip() throws IOException {
        Project project = projectController.newProject();
        Graph graph = GraphGenerator.build(project.getCurrentWorkspace()).generateTinyGraphWithPosition()
            .addStringNodeColumn().getGraph();

        File file = tempFolder.newFile("test.gexf");
        exportController.exportFile(file, project.getCurrentWorkspace());

        Workspace reimported = importFile(file);
        assertSameGraph(graph, graphController.getGraphModel(reimported).getGraph());
    }

    @Test
    public void testExportGraphMLToWriterRoundTrip() {
        Project project = projectController.newProject();
        Graph graph = GraphGenerator.build(project.getCurrentWorkspace()).generateTinyGraphWithPosition()
            .addStringNodeColumn().getGraph();

        StringWriter stringWriter = new StringWriter();
        exportController.exportWriter(stringWriter, (CharacterExporter) exportController.getExporter("graphml"));

        Container container = importController.importFile(new StringReader(stringWriter.toString()),
            importController.getFileImporter("graphml"));
        Workspace reimported = process(container);
        assertSameGraph(graph, graphController.getGraphModel(reimported).getGraph());
    }

    @Test
    public void testExportPdf() throws IOException {
        Project project = projectController.newProject();
        GraphGenerator.build(project.getCurrentWorkspace()).generateTinyGraph().addRandomPositions();

        PDFExporter pdfExporter = (PDFExporter) exportController.getExporter("pdf");
        pdfExporter.setPageSize(PDRectangle.A0);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        exportController.exportStream(baos, pdfExporter);

        try (PDDocument document = Loader.loadPDF(baos.toByteArray())) {
            Assert.assertEquals(1, document.getNumberOfPages());
            PDPage page = document.getPage(0);
            Assert.assertEquals(PDRectangle.A0.getWidth(), page.getMediaBox().getWidth(), 0.01);
            Assert.assertEquals(PDRectangle.A0.getHeight(), page.getMediaBox().getHeight(), 0.01);
            try (InputStream contents = page.getContents()) {
                Assert.assertTrue("The page should have drawing instructions", contents.readAllBytes().length > 0);
            }
        }
    }

    private static void assertSameGraph(Graph expected, Graph actual) {
        Assert.assertEquals(expected.getNodeCount(), actual.getNodeCount());
        Assert.assertEquals(expected.getEdgeCount(), actual.getEdgeCount());
        for (Node node : expected.getNodes()) {
            Node copy = actual.getNode(node.getId());
            Assert.assertNotNull("Missing node " + node.getId(), copy);
            Assert.assertEquals(node.x(), copy.x(), 0.001);
            Assert.assertEquals(node.y(), copy.y(), 0.001);
            Assert.assertEquals(node.getAttribute(GraphGenerator.STRING_COLUMN),
                copy.getAttribute(GraphGenerator.STRING_COLUMN));
        }
        for (Edge edge : expected.getEdges()) {
            Edge copy = actual.getEdge(actual.getNode(edge.getSource().getId()),
                actual.getNode(edge.getTarget().getId()));
            Assert.assertNotNull("Missing edge " + edge.getId(), copy);
            Assert.assertEquals(edge.isDirected(), copy.isDirected());
            Assert.assertEquals(edge.getWeight(), copy.getWeight(), 0);
        }
    }
}
