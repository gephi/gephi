package org.gephi.io.exporter.preview;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Random;
import org.gephi.graph.GraphGenerator;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.types.DependantOriginalColor;
import org.gephi.preview.types.EdgeColor;
import org.gephi.project.api.Workspace;
import org.junit.Assert;
import org.junit.Test;
import org.openide.util.Lookup;

public class ExporterTest {

    private Workspace createRandomGraph() {
        // Create a random graph to test that exporters work
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
        return workspace;
    }

    private Workspace createCondensedNodeGraph() {
        GraphGenerator generator = GraphGenerator.build();
        GraphModel graphModel = generator.getGraphModel();
        DirectedGraph directedGraph = graphModel.getDirectedGraph();

        final Color nodeColor = new Color(0.8f, 0.8f, 0.8f);
        final Color edgeColor = new Color(0.8f, 0.1f, 0.1f);

        // Insert pairs of connected nodes with various degrees of closeness and node sizes
        final float[] dist = {
            0f, 5f, 5f, 5f, 5f,
            5f, 5f, 6f, 6f, 6f,
            6f, 5f, 7f, 7f, 7f,
            7f, 10f, 14f, 16f, 20f
            };
        final float[] r1 = {
            2f, 20f, 10f, 10f, 9f,
            20f, 6f, 20f, 12f, .5f,
            13f, 8f, 8f, 7f, 6f,
            7f, 7f, 7f, 7f, 7f
            };
        final float[] r2 = {
            5f, 6f, 4f, 5f, 4f,
            4f, 1f, 4f, 1f, 1f,
            1f, 6f, 4f, 4f, 4f,
            7f, 7f, 7f, 7f, 7f
            };

        float lowest = 0;
        final float margin = 10;
        for (int i = 0; i < dist.length; i++) {
            // Calculate the position of a pair of nodes
            // Where the nodes have sizes from r1 and r2, and their centers are dist apart
            // Pairs of nodes share the same y-value
            final float dy = Math.max(r1[i], r2[i]);
            final float x1 = r1[i];
            final float y1 = lowest - dy;
            final float x2 = r1[i] + dist[i];
            final float y2 = y1;

            lowest -= dy * 2 + margin;

            // Create the nodes
            Node node1 = graphModel.factory().newNode();
            node1.setX(x1);
            node1.setY(y1);
            node1.setSize(r1[i]);
            node1.setColor(nodeColor);
            directedGraph.addNode(node1);
            Node node2 = graphModel.factory().newNode();
            node2.setX(x2);
            node2.setY(y2);
            node2.setSize(r2[i]);
            node2.setColor(nodeColor);
            directedGraph.addNode(node2);
            Edge edge = graphModel.factory().newEdge(node1, node2);
            directedGraph.addEdge(edge);
        }

        generator.addNodeLabels();
        generator.addEdgeLabels();

        Workspace workspace = generator.getWorkspace();
        PreviewController previewController = Lookup.getDefault().lookup(PreviewController.class);
        PreviewProperties props = previewController.getModel(workspace).getProperties();
        props.putValue(PreviewProperty.BACKGROUND_COLOR, Color.DARK_GRAY);
        props.putValue(PreviewProperty.NODE_OPACITY, 20f);
        props.putValue(PreviewProperty.EDGE_CURVED, Boolean.TRUE);
        props.putValue(PreviewProperty.SHOW_EDGE_LABELS, Boolean.TRUE);
        props.putValue(PreviewProperty.SHOW_NODE_LABELS, Boolean.TRUE);
        props.putValue(PreviewProperty.NODE_LABEL_COLOR,
            new DependantOriginalColor(DependantOriginalColor.Mode.PARENT));
        props.putValue(PreviewProperty.NODE_LABEL_FONT, new Font("Arial", Font.PLAIN, 3));
        props.putValue(PreviewProperty.ARROW_SIZE, 2);
        props.putValue(PreviewProperty.EDGE_THICKNESS, 1);
        props.putValue(PreviewProperty.EDGE_OPACITY, 80);
        props.putValue(PreviewProperty.EDGE_COLOR, new EdgeColor(edgeColor));

        return workspace;
    }

    @Test
    public void testPDFExporter() throws IOException {
        Workspace workspace = createRandomGraph();

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

    @Test
    public void testSVGExporter() throws IOException {
        Workspace workspace = createCondensedNodeGraph();
        
        SVGExporter svgExporter = new SVGExporter();
        svgExporter.setWorkspace(workspace);
        File tempFile = new File("testSVGExporter.svg");
        Writer writer = new OutputStreamWriter(new FileOutputStream(tempFile), StandardCharsets.UTF_8);
        tempFile.deleteOnExit();
        svgExporter.setWriter(writer);
        svgExporter.execute();

        Assert.assertTrue(tempFile.length() > 0);

        List<String> contents = Files.readAllLines(tempFile.toPath());
        for (String line : contents) {
            // NaN is not a valid number, should not be found in path d, or text x/y
            Assert.assertFalse(line.contains("NaN"));
        }
    }
}
