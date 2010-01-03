package org.gephi.io.exporter.preview;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Locale;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.svg2svg.SVGTranscoder;
import org.gephi.io.exporter.FileType;
import org.gephi.io.exporter.VectorialFileExporter;
import org.gephi.io.exporter.preview.util.LengthUnit;
import org.gephi.io.exporter.preview.util.SupportSize;
import org.gephi.preview.api.BidirectionalEdge;
import org.gephi.preview.api.CubicBezierCurve;
import org.gephi.preview.api.DirectedEdge;
import org.gephi.preview.api.Edge;
import org.gephi.preview.api.EdgeArrow;
import org.gephi.preview.api.EdgeLabel;
import org.gephi.preview.api.EdgeMiniLabel;
import org.gephi.preview.api.Graph;
import org.gephi.preview.api.GraphRenderer;
import org.gephi.preview.api.GraphSheet;
import org.gephi.preview.api.Node;
import org.gephi.preview.api.NodeLabel;
import org.gephi.preview.api.NodeLabelBorder;
import org.gephi.preview.api.Point;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.SelfLoop;
import org.gephi.preview.api.UndirectedEdge;
import org.gephi.preview.api.UnidirectionalEdge;
import org.gephi.utils.longtask.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.gephi.workspace.api.Workspace;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.w3c.dom.svg.SVGLocatable;
import org.w3c.dom.svg.SVGRect;

/**
 * Class exporting the preview graph as an SVG image.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
@ServiceProvider(service = VectorialFileExporter.class)
public class SVGExporter implements GraphRenderer, VectorialFileExporter, LongTask {

    private final static float MARGIN = 25f;
    private final String namespaceURI = SVGDOMImplementation.SVG_NAMESPACE_URI;
    private final HashMap<NodeLabel, SVGLocatable> nodeLabelMap = new HashMap<NodeLabel, SVGLocatable>();
    private Document doc;
    private ProgressTicket progress;
    private boolean cancel = false;
    private Element svgRoot;
    private Element nodeGroupElem;
    private Element edgeGroupElem;
    private Element labelGroupElem;
    private Element labelBorderGroupElem;

    public boolean exportData(File file, Workspace workspace) throws Exception {
        try {
            SupportSize supportSize = new SupportSize(210, 297, LengthUnit.MILLIMETER);
            exportData(file, supportSize);
        } catch (Exception e) {
            clean();
            throw e;
        }
        boolean c = cancel;
        clean();
        return !c;
    }

    public FileType[] getFileTypes() {
        return new FileType[]{new FileType(".svg", "SVG files")};
    }

    public String getName() {
        return "SVG Exporter";
    }

    public boolean cancel() {
        cancel = true;
        return true;
    }

    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progress = progressTicket;
    }

    public void renderGraph(Graph graph) {
        if (graph.showEdges()) {
            renderGraphEdges(graph);
        }

        if (graph.showNodes()) {
            renderGraphNodes(graph);
        }

        renderGraphLabels(graph);

        renderGraphLabelBorders(graph);
    }

    public void renderGraphEdges(Graph graph) {
        edgeGroupElem = createGroupElem("edges");
        svgRoot.appendChild(edgeGroupElem);

        renderGraphUnidirectionalEdges(graph);
        renderGraphBidirectionalEdges(graph);
        renderGraphUndirectedEdges(graph);

        if (graph.showSelfLoops()) {
            renderGraphSelfLoops(graph);
        }
    }

    public void renderGraphSelfLoops(Graph graph) {
        for (SelfLoop sl : graph.getSelfLoops()) {
            renderSelfLoop(sl);
        }
    }

    public void renderGraphUnidirectionalEdges(Graph graph) {
        for (UnidirectionalEdge edge : graph.getUnidirectionalEdges()) {
            renderDirectedEdge(edge);
        }
    }

    public void renderGraphBidirectionalEdges(Graph graph) {
        for (BidirectionalEdge edge : graph.getBidirectionalEdges()) {
            renderDirectedEdge(edge);
        }
    }

    public void renderGraphUndirectedEdges(Graph graph) {
        for (UndirectedEdge e : graph.getUndirectedEdges()) {
            renderEdge(e);
        }
    }

    public void renderGraphNodes(Graph graph) {
        nodeGroupElem = createGroupElem("nodes");
        svgRoot.appendChild(nodeGroupElem);

        for (Node n : graph.getNodes()) {
            renderNode(n);
        }
    }

    public void renderGraphLabels(Graph graph) {
        labelGroupElem = createGroupElem("labels");
        svgRoot.appendChild(labelGroupElem);

        for (UnidirectionalEdge e : graph.getUnidirectionalEdges()) {
            if (!e.isCurved()) {
                if (e.showLabel() && e.hasLabel()) {
                    renderEdgeLabel(e.getLabel());
                }

                if (e.showMiniLabels()) {
                    renderEdgeMiniLabels(e);
                }
            }
        }

        for (BidirectionalEdge e : graph.getBidirectionalEdges()) {
            if (!e.isCurved()) {
                if (e.showLabel() && e.hasLabel()) {
                    renderEdgeLabel(e.getLabel());
                }

                if (e.showMiniLabels()) {
                    renderEdgeMiniLabels(e);
                }
            }
        }

        for (UndirectedEdge e : graph.getUndirectedEdges()) {
            if (e.showLabel() && !e.isCurved() && e.hasLabel()) {
                renderEdgeLabel(e.getLabel());
            }
        }

        for (Node n : graph.getNodes()) {
            if (n.showLabel() && n.hasLabel()) {
                renderNodeLabel(n.getLabel());
            }
        }
    }

    public void renderGraphLabelBorders(Graph graph) {
        labelBorderGroupElem = createGroupElem("label borders");
        svgRoot.insertBefore(labelBorderGroupElem, labelGroupElem);

        for (Node n : graph.getNodes()) {
            if (n.showLabel() && n.hasLabel() && n.showLabelBorders()) {
                renderNodeLabelBorder(n.getLabelBorder());
            }
        }
    }

    public void renderNode(Node node) {
        Element nodeElem = createElement("circle");
        nodeElem.setAttribute("cx", node.getPosition().getX().toString());
        nodeElem.setAttribute("cy", node.getPosition().getY().toString());
        nodeElem.setAttribute("r", node.getRadius().toString());
        nodeElem.setAttribute("fill", node.getColor().toHexString());
        nodeElem.setAttribute("stroke", node.getBorderColor().toHexString());
        nodeElem.setAttribute("stroke-width", node.getBorderWidth().toString());
        nodeGroupElem.appendChild(nodeElem);

        Progress.progress(progress);
    }

    public void renderNodeLabel(NodeLabel label) {
        Text labelText = createTextNode(label.getValue());

        Element labelElem = createElement("text");
        labelElem.setAttribute("x", label.getPosition().getX().toString());
        labelElem.setAttribute("y", label.getPosition().getY().toString());
        labelElem.setAttribute("style", "text-anchor: middle");
        labelElem.setAttribute("fill", label.getColor().toHexString());
        labelElem.setAttribute("font-family", label.getFont().getFamily());
        labelElem.setAttribute("font-size", Integer.toString(label.getFont().getSize()));
        labelElem.appendChild(labelText);
        labelGroupElem.appendChild(labelElem);

        // need to save the label element in order to eventually draw its border
        nodeLabelMap.put(label, (SVGLocatable) labelElem);
    }

    public void renderNodeLabelBorder(NodeLabelBorder border) {
        // retrieve label's bounding box
        SVGRect rect = nodeLabelMap.get(border.getLabel()).getBBox();

        Element borderElem = createElement("rect");
        borderElem.setAttribute("x", Float.toString(rect.getX()));
        borderElem.setAttribute("y", Float.toString(rect.getY()));
        borderElem.setAttribute("width", Float.toString(rect.getWidth()));
        borderElem.setAttribute("height", Float.toString(rect.getHeight()));
        borderElem.setAttribute("fill", border.getColor().toHexString());
        labelBorderGroupElem.appendChild(borderElem);
    }

    public void renderSelfLoop(SelfLoop selfLoop) {
        CubicBezierCurve curve = selfLoop.getCurve();

        Element selfLoopElem = createElement("path");
        selfLoopElem.setAttribute("d", String.format(Locale.ENGLISH, "M %f,%f C %f,%f %f,%f %f,%f",
                curve.getPt1().getX(), curve.getPt1().getY(),
                curve.getPt2().getX(), curve.getPt2().getY(),
                curve.getPt3().getX(), curve.getPt3().getY(),
                curve.getPt4().getX(), curve.getPt4().getY()));
        selfLoopElem.setAttribute("stroke", selfLoop.getColor().toHexString());
        selfLoopElem.setAttribute("stroke-width", Float.toString(selfLoop.getThickness()));
        selfLoopElem.setAttribute("fill", "none");
        edgeGroupElem.appendChild(selfLoopElem);
    }

    public void renderDirectedEdge(DirectedEdge edge) {
        renderEdge(edge);

        if (!edge.isCurved() && edge.showArrows()) {
            renderEdgeArrows(edge);
        }
    }

    public void renderEdge(Edge edge) {
        if (edge.isCurved()) {
            renderCurvedEdge(edge);
        } else {
            renderStraightEdge(edge);
        }

        Progress.progress(progress);
    }

    public void renderStraightEdge(Edge edge) {
        Point boundary1 = edge.getNode1().getPosition();
        Point boundary2 = edge.getNode2().getPosition();

        Element edgeElem = createElement("path");
        edgeElem.setAttribute("d", String.format(Locale.ENGLISH, "M %f,%f L %f,%f",
                boundary1.getX(), boundary1.getY(),
                boundary2.getX(), boundary2.getY()));
        edgeElem.setAttribute("stroke", edge.getColor().toHexString());
        edgeElem.setAttribute("stroke-width", Float.toString(edge.getThickness()));
        edgeGroupElem.appendChild(edgeElem);
    }

    public void renderCurvedEdge(Edge edge) {
        for (CubicBezierCurve curve : edge.getCurves()) {
            Element curveElem = createElement("path");
            curveElem.setAttribute("d", String.format(Locale.ENGLISH, "M %f,%f C %f,%f %f,%f %f,%f",
                    curve.getPt1().getX(), curve.getPt1().getY(),
                    curve.getPt2().getX(), curve.getPt2().getY(),
                    curve.getPt3().getX(), curve.getPt3().getY(),
                    curve.getPt4().getX(), curve.getPt4().getY()));
            curveElem.setAttribute("stroke", edge.getColor().toHexString());
            curveElem.setAttribute("stroke-width", Float.toString(edge.getThickness()));
            curveElem.setAttribute("fill", "none");
            edgeGroupElem.appendChild(curveElem);
        }
    }

    public void renderEdgeArrows(DirectedEdge edge) {
        for (EdgeArrow a : edge.getArrows()) {
            renderEdgeArrow(a);
        }
    }

    public void renderEdgeMiniLabels(DirectedEdge edge) {
        for (EdgeMiniLabel ml : edge.getMiniLabels()) {
            renderEdgeMiniLabel(ml);
        }
    }

    public void renderEdgeArrow(EdgeArrow arrow) {
        Element arrowElem = createElement("polyline");
        arrowElem.setAttribute("points", String.format(Locale.ENGLISH, "%f,%f %f,%f %f,%f",
                arrow.getPt1().getX(), arrow.getPt1().getY(),
                arrow.getPt2().getX(), arrow.getPt2().getY(),
                arrow.getPt3().getX(), arrow.getPt3().getY()));
        arrowElem.setAttribute("fill", arrow.getColor().toHexString());
        arrowElem.setAttribute("stroke", "none");
        edgeGroupElem.appendChild(arrowElem);
    }

    public void renderEdgeLabel(EdgeLabel label) {
        Text text = createTextNode(label.getValue());

        Element labelElem = createElement("text");
        labelElem.setAttribute("x", "0");
        labelElem.setAttribute("y", "0");
        labelElem.setAttribute("style", "text-anchor: middle");
        labelElem.setAttribute("fill", label.getColor().toHexString());
        labelElem.setAttribute("font-family", label.getFont().getFamily());
        labelElem.setAttribute("font-size", Integer.toString(label.getFont().getSize()));
        labelElem.setAttribute("transform", String.format(Locale.ENGLISH, "translate(%f,%f) rotate(%f)",
                label.getPosition().getX(), label.getPosition().getY(),
                Math.toDegrees(label.getAngle())));
        labelElem.appendChild(text);
        labelGroupElem.appendChild(labelElem);
    }

    public void renderEdgeMiniLabel(EdgeMiniLabel miniLabel) {
        Text text = createTextNode(miniLabel.getValue());

        Element miniLabelElem = createElement("text");
        miniLabelElem.setAttribute("x", "0");
        miniLabelElem.setAttribute("y", "0");
        miniLabelElem.setAttribute("style", miniLabel.getHAlign().toCSS());
        miniLabelElem.setAttribute("fill", miniLabel.getColor().toHexString());
        miniLabelElem.setAttribute("font-family", miniLabel.getFont().getFamily());
        miniLabelElem.setAttribute("font-size", Integer.toString(miniLabel.getFont().getSize()));
        miniLabelElem.setAttribute("transform", String.format(Locale.ENGLISH, "translate(%f,%f) rotate(%f)",
                miniLabel.getPosition().getX(), miniLabel.getPosition().getY(),
                Math.toDegrees(miniLabel.getAngle())));
        miniLabelElem.appendChild(text);
        labelGroupElem.appendChild(miniLabelElem);
    }

    /**
     * Cleans all fields.
     */
    private void clean() {
        progress = null;
        cancel = false;
        doc = null;
        nodeLabelMap.clear();
        svgRoot = null;
        nodeGroupElem = null;
        edgeGroupElem = null;
        labelGroupElem = null;
        labelBorderGroupElem = null;
    }

    /**
     * Does export the preview graph as an SVG image.
     *
     * @param file         the output SVG file
     * @param supportSize  the support size of the exported image
     * @throws Exception
     */
    private void exportData(File file, SupportSize supportSize) throws Exception {
        // fetches the preview graph sheet
        PreviewController controller = Lookup.getDefault().lookup(PreviewController.class);
        GraphSheet graphSheet = controller.getGraphSheet();
        Graph graph = graphSheet.getGraph();

        Progress.start(progress);

        // calculates progress units count
        int max = 0;
        if (graph.showNodes()) {
            max += graph.countNodes();
        }
        if (graph.showEdges()) {
            max += graph.countUnidirectionalEdges() + graph.countBidirectionalEdges();
            if (graph.showSelfLoops()) {
                max += graph.countSelfLoops();
            }
        }
        Progress.switchToDeterminate(progress, max);

        // export tasks
        buildDOM(graphSheet, supportSize);
        saveDOM(file);

        Progress.finish(progress);
    }

    /**
     * Builds the DOM from the preview graph.
     *
     * @param graphSheet   the preview graph sheet
     * @param supportSize  the support size of the exported image
     */
    private void buildDOM(GraphSheet graphSheet, SupportSize supportSize) {
        // creates SVG document
        DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
        DocumentType doctype = impl.createDocumentType(
                "-//W3C//DTD SVG 1.1//EN",
                "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd",
                "");
        doc = impl.createDocument(namespaceURI, "svg", doctype);

        // initializes CSS and SVG specific DOM interfaces
        UserAgent userAgent = new UserAgentAdapter();
        DocumentLoader loader = new DocumentLoader(userAgent);
        BridgeContext ctx = new BridgeContext(userAgent, loader);
        ctx.setDynamicState(BridgeContext.DYNAMIC);
        GVTBuilder builder = new GVTBuilder();
        builder.build(ctx, doc);

        // image margin
        graphSheet.setMargin(MARGIN);

        // root element
        svgRoot = doc.getDocumentElement();
        svgRoot.setAttributeNS(null, "width", supportSize.getWidth());
        svgRoot.setAttributeNS(null, "height", supportSize.getHeight());
        svgRoot.setAttributeNS(null, "version", "1.1");
        svgRoot.setAttributeNS(null, "viewBox", String.format(Locale.ENGLISH, "%d %d %d %d",
                graphSheet.getTopLeftPosition().getX().intValue(),
                graphSheet.getTopLeftPosition().getY().intValue(),
                graphSheet.getWidth().intValue(),
                graphSheet.getHeight().intValue()));

        // draws the graph exporting it into the DOM
        renderGraph(graphSheet.getGraph());
    }

    /**
     * Saves the current DOM to a file.
     *
     * @param file  the file to write the DOM to
     */
    private void saveDOM(File file) throws Exception {
        OutputStream ostream = null;
        Writer w = null;

        // creates SVG-to-SVG transcoder
        SVGTranscoder t = new SVGTranscoder();
        t.addTranscodingHint(SVGTranscoder.KEY_XML_DECLARATION,
                new String("<?xml version=\"1.0\" encoding=\"utf-8\"?>"));

        // sets transcoder input and output
        TranscoderInput input = new TranscoderInput(doc);

        // performs transcoding
        try {
            ostream = new BufferedOutputStream(new FileOutputStream(file));
            try {
                w = new OutputStreamWriter(ostream, "utf8");
                TranscoderOutput output = new TranscoderOutput(w);
                t.transcode(input, output);
            } finally {
                w.close();
            }
        } finally {
            ostream.close();
        }
    }

    /**
     * Creates a new element from the current document.
     *
     * @param qualifiedName  the qualified name of the element type to
     *   instantiate
     * @return               a new <code>Element</code> object
     */
    private Element createElement(String qualifiedName) {
        return doc.createElementNS(namespaceURI, qualifiedName);
    }

    /**
     * Creates a text node from the current document.
     *
     * @param data  the data for the node
     * @return      a new <code>Text</code> object
     */
    private Text createTextNode(String data) {
        return doc.createTextNode(data);
    }

    /**
     * Creates the group element corresponding to the given type.
     *
     * @param type  the name of the group element to create
     * @return      the created group element
     */
    private Element createGroupElem(String name) {
        Element group = createElement("g");
        group.setAttribute("id", name);

        return group;
    }
}
