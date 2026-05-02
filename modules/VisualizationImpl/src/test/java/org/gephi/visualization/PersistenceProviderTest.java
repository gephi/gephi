package org.gephi.visualization;

import java.awt.Color;
import java.awt.Font;
import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.gephi.graph.GraphGenerator;
import org.gephi.project.api.Workspace;
import org.gephi.project.io.utils.GephiFormat;
import org.gephi.visualization.api.EdgeColorMode;
import org.gephi.visualization.api.LabelColorMode;
import org.gephi.visualization.api.LabelSizeMode;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Spy;

public class PersistenceProviderTest {

    private final VizController vizController = new VizController();
    private final VizModelPersistenceProvider provider = new VizModelPersistenceProvider();

    @Test
    public void testEmpty() throws Exception {
        GraphGenerator generator = GraphGenerator.build().generateTinyGraph();

        VizModel model = vizController.getModel(generator.getWorkspace());
        roundTrip(provider, model.getWorkspace());
    }

    @Test
    public void testBackgroundColor() throws Exception {
        VizModel model = vizController.getModel(GraphGenerator.build().generateTinyGraph().getWorkspace());
        model.setBackgroundColor(Color.CYAN);

        VizModel read = roundTrip(provider, model.getWorkspace());
        Assert.assertEquals(Color.CYAN, read.getBackgroundColor());
    }

    @Test
    public void testZoomAndPan() throws Exception {
         VizModel model = vizController.getModel(GraphGenerator.build().generateTinyGraph().getWorkspace());
        model.setZoom(1.5f);
        model.setPan(new org.joml.Vector2f(123.4f, -56.7f));

        VizModel read = roundTrip(provider, model.getWorkspace());
        Assert.assertEquals(1.5f, read.getZoom(), 0.0001f);
        Assert.assertEquals(123.4f, read.getPan().x(), 0.0001f);
        Assert.assertEquals(-56.7f, read.getPan().y(), 0.0001f);
    }

    @Test
    public void testEdgeSettings() throws Exception {
         VizModel model = vizController.getModel(GraphGenerator.build().generateTinyGraph().getWorkspace());
        model.setShowEdges(false);
        model.setEdgeScale(3.5f);
        model.setNodeScale(2.0f);
        model.setEdgeColorMode(EdgeColorMode.MIXED);
        model.setUseEdgeWeight(false);
        model.setEdgeRescaleWeightEnabled(false);

        VizModel read = roundTrip(provider, model.getWorkspace());
        Assert.assertFalse(read.isShowEdges());
        Assert.assertEquals(3.5f, read.getEdgeScale(), 0.0001f);
        Assert.assertEquals(2.0f, read.getNodeScale(), 0.0001f);
        Assert.assertEquals(EdgeColorMode.MIXED, read.getEdgeColorMode());
        Assert.assertFalse(read.isUseEdgeWeight());
        Assert.assertFalse(read.isRescaleEdgeWeight());
    }

    @Test
    public void testSelectionSettings() throws Exception {
         VizModel model = vizController.getModel(GraphGenerator.build().generateTinyGraph().getWorkspace());
        model.setAutoSelectNeighbors(false);
        model.setHideNonSelectedEdges(true);
        model.setLightenNonSelectedAuto(false);
        model.setLightenNonSelectedFactor(0.5f);
        model.setEdgeSelectionColor(true);
        model.setEdgeInSelectionColor(Color.RED);
        model.setEdgeOutSelectionColor(Color.GREEN);
        model.setEdgeBothSelectionColor(Color.BLUE);

        VizModel read = roundTrip(provider, model.getWorkspace());
        Assert.assertFalse(read.isAutoSelectNeighbors());
        Assert.assertTrue(read.isHideNonSelectedEdges());
        Assert.assertFalse(read.isLightenNonSelectedAuto());
        Assert.assertEquals(0.5f, read.getLightenNonSelectedFactor(), 0.0001f);
        Assert.assertTrue(read.isEdgeSelectionColor());
        Assert.assertEquals(Color.RED, read.getEdgeInSelectionColor());
        Assert.assertEquals(Color.GREEN, read.getEdgeOutSelectionColor());
        Assert.assertEquals(Color.BLUE, read.getEdgeBothSelectionColor());
    }

    @Test
    public void testNodeLabelSettings() throws Exception {
         VizModel model = vizController.getModel(GraphGenerator.build().generateTinyGraph().getWorkspace());
        model.setShowNodeLabels(true);
        model.setNodeLabelFont(new Font("SansSerif", Font.ITALIC, 16));
        model.setNodeLabelScale(0.8f);
        model.setNodeLabelColorMode(LabelColorMode.OBJECT);
        model.setNodeLabelSizeMode(LabelSizeMode.SCREEN);
        model.setHideNonSelectedNodeLabels(true);
        model.setNodeLabelFitToNodeSize(true);
        model.setAvoidNodeLabelOverlap(false);

        VizModel read = roundTrip(provider, model.getWorkspace());
        Assert.assertTrue(read.isShowNodeLabels());
        Assert.assertEquals(0.8f, read.getNodeLabelScale(), 0.0001f);
        Assert.assertEquals(LabelColorMode.OBJECT, read.getNodeLabelColorMode());
        Assert.assertEquals(LabelSizeMode.SCREEN, read.getNodeLabelSizeMode());
        Assert.assertTrue(read.isHideNonSelectedNodeLabels());
        Assert.assertTrue(read.isNodeLabelFitToNodeSize());
        Assert.assertFalse(read.isAvoidNodeLabelOverlap());
    }

    @Test
    public void testEdgeLabelSettings() throws Exception {
         VizModel model = vizController.getModel(GraphGenerator.build().generateTinyGraph().getWorkspace());
        model.setShowEdgeLabels(true);
        model.setEdgeLabelFont(new Font("Monospaced", Font.BOLD, 14));
        model.setEdgeLabelScale(0.6f);
        model.setEdgeLabelColorMode(LabelColorMode.OBJECT);
        model.setEdgeLabelSizeMode(LabelSizeMode.SCREEN);
        model.setHideNonSelectedEdgeLabels(true);

        VizModel read = roundTrip(provider, model.getWorkspace());
        Assert.assertTrue(read.isShowEdgeLabels());
        Assert.assertEquals(0.6f, read.getEdgeLabelScale(), 0.0001f);
        Assert.assertEquals(LabelColorMode.OBJECT, read.getEdgeLabelColorMode());
        Assert.assertEquals(LabelSizeMode.SCREEN, read.getEdgeLabelSizeMode());
        Assert.assertTrue(read.isHideNonSelectedEdgeLabels());
    }

    @Test
    public void testScreenshotModel() throws Exception {
         VizModel model = vizController.getModel(GraphGenerator.build().generateTinyGraph().getWorkspace());
        model.getScreenshotModel().setScaleFactor(4);
        model.getScreenshotModel().setTransparentBackground(true);
        model.getScreenshotModel().setAutoSave(true);

        VizModel read = roundTrip(provider, model.getWorkspace());
        Assert.assertEquals(4, read.getScreenshotModel().getScaleFactor());
        Assert.assertTrue(read.getScreenshotModel().isTransparentBackground());
        Assert.assertTrue(read.getScreenshotModel().isAutoSave());
    }

    @Test
    public void testSelectionModel() throws Exception {
         VizModel model = vizController.getModel(GraphGenerator.build().generateTinyGraph().getWorkspace());
        model.getSelectionModel().setMouseSelectionDiameter(5);
        model.getSelectionModel().setMouseSelectionZoomProportional(true);
        model.getSelectionModel().setRectangleSelection(true);
        model.getSelectionModel().setSelectionEnable(true);

        VizModel read = roundTrip(provider, model.getWorkspace());
        Assert.assertEquals(5, read.getMouseSelectionDiameter());
        Assert.assertTrue(read.isMouseSelectionZoomProportional());
        Assert.assertTrue(read.isRectangleSelection());
        Assert.assertTrue(read.isSelectionEnabled());
    }

    @Test
    public void testSelectionModelNodeSelection() throws Exception {
        VizModel model = vizController.getModel(GraphGenerator.build().generateTinyGraph().getWorkspace());
        model.getSelectionModel().setSelectionEnable(true);
        model.getSelectionModel().setNodeSelection(true);
        model.getSelectionModel().setSingleNodeSelection(true);

        VizModel read = roundTrip(provider, model.getWorkspace());
        Assert.assertTrue(read.getSelectionModel().isNodeSelection());
        Assert.assertTrue(read.getSelectionModel().isSingleNodeSelection());
    }

    @Test
    public void testLegacyScreenshotMakerBackwardCompatibility() throws Exception {
        // Simulate a <vizmodel> from Gephi 0.10 with the old self-closing <screenshotMaker> element.
        // width/height/antialiasing have no equivalent in the new model and must be gracefully ignored.
        String legacyXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<vizmodel>"
            + "<screenshotMaker width=\"1920\" height=\"1080\" antialiasing=\"4\""
            + " transparent=\"true\" autosave=\"true\"/>"
            + "</vizmodel>";

         VizModel model = vizController.getModel(GraphGenerator.build().generateTinyGraph().getWorkspace());
        StringReader stringReader = new StringReader(legacyXml);
        XMLStreamReader xmlReader = GephiFormat.newXMLReader(stringReader);
        new VizModelPersistenceProvider().readXML(xmlReader, model.getWorkspace());
        xmlReader.close();

        Assert.assertTrue(model.getScreenshotModel().isTransparentBackground());
        Assert.assertTrue(model.getScreenshotModel().isAutoSave());
    }

    @Test
    public void testLegacyTextModelBackwardCompatibility() throws Exception {
        // Simulate a <vizmodel> block from Gephi 0.10 containing the old <textmodel> element.
        String legacyXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<vizmodel>"
            + "<textmodel>"
            + "<shownodelabels enable=\"true\"/>"
            + "<showedgelabels enable=\"true\"/>"
            + "<selectedOnly value=\"true\"/>"
            + "<nodefont name=\"SansSerif\" size=\"24\" style=\"1\"/>"
            + "<edgefont name=\"Monospaced\" size=\"18\" style=\"2\"/>"
            + "<nodesizefactor>0.75</nodesizefactor>"
            + "<edgesizefactor>0.4</edgesizefactor>"
            + "<colormode class=\"ObjectColorMode\"/>"
            + "<sizemode class=\"FixedSizeMode\"/>"
            + "<nodecolumns><column id=\"label\"/></nodecolumns>"
            + "<edgecolumns><column id=\"label\"/></edgecolumns>"
            + "</textmodel>"
            + "</vizmodel>";

         VizModel model = vizController.getModel(GraphGenerator.build().generateTinyGraph().getWorkspace());
        StringReader stringReader = new StringReader(legacyXml);
        XMLStreamReader xmlReader = GephiFormat.newXMLReader(stringReader);
        new VizModelPersistenceProvider().readXML(xmlReader, model.getWorkspace());
        xmlReader.close();

        Assert.assertTrue(model.isShowNodeLabels());
        Assert.assertTrue(model.isShowEdgeLabels());
        Assert.assertTrue(model.isHideNonSelectedNodeLabels());
        Assert.assertTrue(model.isHideNonSelectedEdgeLabels());
        Assert.assertEquals("SansSerif", model.getNodeLabelFont().getFamily());
        Assert.assertEquals(24, model.getNodeLabelFont().getSize());
        Assert.assertEquals(Font.BOLD, model.getNodeLabelFont().getStyle());
        Assert.assertEquals("Monospaced", model.getEdgeLabelFont().getFamily());
        Assert.assertEquals(18, model.getEdgeLabelFont().getSize());
        Assert.assertEquals(0.75f, model.getNodeLabelScale(), 0.0001f);
        Assert.assertEquals(0.4f, model.getEdgeLabelScale(), 0.0001f);
        Assert.assertEquals(LabelColorMode.OBJECT, model.getNodeLabelColorMode());
        Assert.assertEquals(LabelColorMode.OBJECT, model.getEdgeLabelColorMode());
        Assert.assertEquals(LabelSizeMode.SCREEN, model.getNodeLabelSizeMode());
        Assert.assertEquals(LabelSizeMode.SCREEN, model.getEdgeLabelSizeMode());
        Assert.assertEquals(1, model.getNodeLabelColumns().length);
        Assert.assertEquals("label", model.getNodeLabelColumns()[0].getId());
    }

    @Test
    public void testDefaultLabelColumnsRoundTrip() throws Exception {
         VizModel model = vizController.getModel(GraphGenerator.build().generateTinyGraph().getWorkspace());
        // Default label column ("label") must survive the round-trip.
        Assert.assertEquals(1, model.getNodeLabelColumns().length);
        Assert.assertEquals("label", model.getNodeLabelColumns()[0].getId());

        VizModel read = roundTrip(provider, model.getWorkspace());
        Assert.assertEquals(1, read.getNodeLabelColumns().length);
        Assert.assertEquals("label", read.getNodeLabelColumns()[0].getId());
    }
    
    // Utils
    /**
     * Performs a full persistence round-trip: serializes the source workspace to XML, reads it
     * into a freshly created destination workspace, serializes the destination again, and asserts
     * both XML representations are identical (idempotency check).
     *
     * <p>The destination workspace is pre-populated with its own {@link VizModel} so that the
     * provider's read path can resolve the model without depending on the global Lookup.
     */
    public VizModel roundTrip(VizModelPersistenceProvider provider, Workspace sourceWorkspace)
        throws Exception {
        String xmlString = toXMLString(provider, sourceWorkspace);

        VizModel destVizModel = vizController.getModel(GraphGenerator.build().getWorkspace());
        Workspace destWorkspace = destVizModel.getWorkspace();

        StringReader stringReader = new StringReader(xmlString);
        XMLStreamReader xmlReader = GephiFormat.newXMLReader(stringReader);
        provider.readXML(xmlReader, destWorkspace);
        xmlReader.close();
        stringReader.close();

        String xmlStringAgain = toXMLString(provider, destWorkspace);
        Assert.assertEquals(xmlString, xmlStringAgain);

        return vizController.getModel(destWorkspace);
    }

    public static String toXMLString(VizModelPersistenceProvider provider, Workspace workspace)
        throws Exception {
        StringWriter stringWriter = new StringWriter();
        XMLStreamWriter writer = GephiFormat.newXMLWriter(stringWriter);
        writer.writeStartDocument("UTF-8", "1.0");
        writer.writeStartElement(provider.getIdentifier());
        provider.writeXML(writer, workspace);
        writer.writeEndElement();
        writer.writeEndDocument();
        writer.close();
        stringWriter.close();
        return stringWriter.toString();
    }
}
