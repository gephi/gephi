package org.gephi.io.exporter.preview;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.gephi.graph.GraphGenerator;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.spi.Renderer;
import org.gephi.project.api.Workspace;
import org.junit.Assert;
import org.junit.Test;
import org.openide.util.Lookup;

public class PDFExporterTest {

    @Test
    public void testPDFExporter() throws IOException  {
        Workspace workspace = GraphGenerator.build().withWorkspace()
            .generateSmallRandomGraph().addRandomPositions().getWorkspace();

        PreviewController previewController = Lookup.getDefault().lookup(PreviewController.class);
        PreviewProperties props = previewController.getModel(workspace).getProperties();
        props.putValue(PreviewProperty.BACKGROUND_COLOR, Color.CYAN);

        PDFExporter pdfExporter = new PDFExporter();
        pdfExporter.setWorkspace(workspace);
        File tempFile = new File("testPDFExporter.pdf");
        tempFile.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(tempFile);
        pdfExporter.setOutputStream(fos);
        pdfExporter.execute();
        fos.close();

        Assert.assertTrue(tempFile.length() > 0);
    }
}
