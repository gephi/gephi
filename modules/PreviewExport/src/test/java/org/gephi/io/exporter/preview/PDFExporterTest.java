package org.gephi.io.exporter.preview;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;
import org.gephi.graph.GraphGenerator;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.types.DependantOriginalColor;
import org.gephi.project.api.Workspace;
import org.junit.Assert;
import org.junit.Test;
import org.openide.util.Lookup;

public class PDFExporterTest {

    @Test
    public void testPDFExporter() throws IOException {
        Workspace workspace = GraphGenerator.build()
            .generateSmallRandomGraph().addRandomPositions().getWorkspace();

        Random random = new Random();
        for (Node node : workspace.getLookup().lookup(GraphModel.class).getGraph().getNodes()) {
            float alpha = Math.max(0.25f, random.nextFloat());
            node.setColor(new Color(random.nextFloat(), random.nextFloat(), random.nextFloat(), alpha));
            node.setLabel("Node" + node.getId());
        }

        PreviewController previewController = Lookup.getDefault().lookup(PreviewController.class);
        PreviewProperties props = previewController.getModel(workspace).getProperties();
        props.putValue(PreviewProperty.BACKGROUND_COLOR, Color.CYAN);
        props.putValue(PreviewProperty.NODE_PER_NODE_OPACITY, Boolean.TRUE);
        props.putValue(PreviewProperty.EDGE_CURVED, Boolean.FALSE);
        props.putValue(PreviewProperty.SHOW_NODE_LABELS, Boolean.TRUE);
        props.putValue(PreviewProperty.NODE_LABEL_COLOR,
            new DependantOriginalColor(DependantOriginalColor.Mode.PARENT));
        props.putValue(PreviewProperty.NODE_LABEL_OUTLINE_SIZE, 4);
        props.putValue(PreviewProperty.NODE_LABEL_SHOW_BOX, Boolean.TRUE);
        props.putValue(PreviewProperty.NODE_LABEL_BOX_OPACITY, 50f);

//        props.putValue(PreviewProperty.NODE_LABEL_FONT, new Font("Arial Unicode MS", Font.PLAIN, 12));

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
