package org.gephi.io.exporter.preview;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayDeque;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.svg2svg.SVGTranscoder;
import org.gephi.io.exporter.Exporter;
import org.gephi.io.exporter.FileExporter;
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
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.SelfLoop;
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
import processing.core.PVector;

/**
 * Class exporting the preview graph as an SVG image.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
@ServiceProvider(service = VectorialFileExporter.class)
public class SVGExporter implements GraphRenderer, VectorialFileExporter, LongTask {

    private final ArrayDeque<Element> parentStack = new ArrayDeque<Element>();
    private final String namespaceURI = SVGDOMImplementation.SVG_NAMESPACE_URI;
    private Document doc;
    private ProgressTicket progress;
    private boolean cancel = false;
    private Element lastLabel;

    /**
     * @see VectorialFileExporter#exportData(java.io.File, org.gephi.workspace.api.Workspace)
     */
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

    /**
     * @see FileExporter#getFileTypes()
     */
    public FileType[] getFileTypes() {
        return new FileType[]{new FileType(".svg", "SVG files")};
    }

    /**
     * @see Exporter#getName()
     */
    public String getName() {
        return "SVG Exporter";
    }

    /**
     * @see LongTask#cancel()
     */
    public boolean cancel() {
        cancel = true;
        return true;
    }

    /**
     * @see LongTask#setProgressTicket(org.gephi.utils.progress.ProgressTicket)
     */
    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progress = progressTicket;
    }

    /**
     * @see GraphRenderer#renderGraph(org.gephi.preview.api.Graph)
     */
    public void renderGraph(Graph graph) {
        Element groupElem = createElement("g");
        groupElem.setAttribute("id", "graph");
        appendToParentElement(groupElem);
        pushParentElement(groupElem);

        if (graph.showEdges()) {
            renderGraphEdges(graph);
        }

        // nodes are above edges and self-loops
        if (graph.showNodes()) {
            renderGraphNodes(graph);
        }

        popParentElement();
    }

    /**
     * @see GraphRenderer#renderGraph(org.gephi.preview.api.Graph)
     */
    public void renderGraphEdges(Graph graph) {
        Element edgeGroupElem = createElement("g");
        edgeGroupElem.setAttribute("id", "edges");
        appendToParentElement(edgeGroupElem);
        pushParentElement(edgeGroupElem);

        renderGraphUnidirectionalEdges(graph);
        renderGraphBidirectionalEdges(graph);

        if (graph.showSelfLoops()) {
            renderGraphSelfLoops(graph);
        }

        popParentElement();
    }

    public void renderGraphUnidirectionalEdges(Graph graph) {
        for (UnidirectionalEdge edge : graph.getUnidirectionalEdges()) {
            renderEdge(edge);

            if (edge.showArrows()) {
                renderEdgeArrows(edge);
            }

            if (edge.showMiniLabels()) {
                renderEdgeMiniLabels(edge);
            }
        }
    }

    public void renderGraphBidirectionalEdges(Graph graph) {
        for (BidirectionalEdge edge : graph.getBidirectionalEdges()) {
            renderEdge(edge);

            if (edge.showArrows()) {
                renderEdgeArrows(edge);
            }

            if (edge.showMiniLabels()) {
                renderEdgeMiniLabels(edge);
            }
        }
    }

    public void renderGraphSelfLoops(Graph graph) {
        for (SelfLoop sl : graph.getSelfLoops()) {
            renderSelfLoop(sl);
        }
    }

    public void renderGraphNodes(Graph graph) {
        Element nodeGroupElem = createElement("g");
        nodeGroupElem.setAttribute("id", "nodes");
        appendToParentElement(nodeGroupElem);
        pushParentElement(nodeGroupElem);

        for (Node n : graph.getNodes()) {
            renderNode(n);
        }

        popParentElement();
    }

    public void renderNode(Node node) {
        Element groupElem = createElement("g");
        appendToParentElement(groupElem);
        pushParentElement(groupElem);

        Element nodeElem = createElement("circle");
        nodeElem.setAttribute("cx", Float.toString(node.getPosition().x));
        nodeElem.setAttribute("cy", Float.toString(node.getPosition().y));
        nodeElem.setAttribute("r", node.getRadius().toString());
        nodeElem.setAttribute("fill", node.getColor().toHexString());
        nodeElem.setAttribute("stroke", node.getBorderColor().toHexString());
        nodeElem.setAttribute("stroke-width", node.getBorderWidth().toString());
        appendToParentElement(nodeElem);

        if (node.showLabel() && node.hasLabel()) {
            renderNodeLabel(node.getLabel());

            if (node.showLabelBorders()) {
                renderNodeLabelBorder(node.getLabelBorder());
            }
        }

        popParentElement();

        Progress.progress(progress);
    }

    public void renderNodeLabel(NodeLabel label) {
        Text labelText = createTextNode(label.getValue());

        Element labelElem = createElement("text");
        labelElem.setAttribute("x", Float.toString(label.getPosition().x));
        labelElem.setAttribute("y", Float.toString(label.getPosition().y));
        labelElem.setAttribute("style", "text-anchor: middle");
        labelElem.setAttribute("fill", label.getColor().toHexString());
        labelElem.setAttribute("font-family", label.getFont().getFamily());
        labelElem.setAttribute("font-size", Integer.toString(label.getFont().getSize()));
        labelElem.appendChild(labelText);
        appendToParentElement(labelElem);

        // need to save label in order to eventually draw its border
        lastLabel = labelElem;
    }

    public void renderNodeLabelBorder(NodeLabelBorder border) {
        // retrieve label's bounding box
        SVGRect rect = ((SVGLocatable) lastLabel).getBBox();

        Element borderElem = createElement("rect");
        borderElem.setAttribute("x", Float.toString(rect.getX()));
        borderElem.setAttribute("y", Float.toString(rect.getY()));
        borderElem.setAttribute("width", Float.toString(rect.getWidth()));
        borderElem.setAttribute("height", Float.toString(rect.getHeight()));
        borderElem.setAttribute("fill", border.getColor().toHexString());
        insertBeforeElement(borderElem, lastLabel);
    }

    public void renderSelfLoop(SelfLoop selfLoop) {
        CubicBezierCurve curve = selfLoop.getCurve();

        Element groupElem = createElement("g");
        appendToParentElement(groupElem);
        pushParentElement(groupElem);

        Element selfLoopElem = createElement("path");
        selfLoopElem.setAttribute("d",
                "M " + curve.getPt1().x + "," + curve.getPt1().y
                + " C " + curve.getPt2().x + "," + curve.getPt2().y
                + " " + curve.getPt3().x + "," + curve.getPt3().y
                + " " + curve.getPt4().x + "," + curve.getPt4().y);
        selfLoopElem.setAttribute("stroke", selfLoop.getColor().toHexString());
        selfLoopElem.setAttribute("stroke-width", Float.toString(selfLoop.getThickness()));
        selfLoopElem.setAttribute("fill", "none");
        appendToParentElement(selfLoopElem);

        popParentElement();
    }

    public void renderEdge(Edge edge) {
        Element groupElem = createElement("g");
        appendToParentElement(groupElem);
        pushParentElement(groupElem);

        if (edge.isCurved()) {
            renderCurvedEdge(edge);
        } else {
            renderStraightEdge(edge);
        }

        popParentElement();

        Progress.progress(progress);
    }

    public void renderStraightEdge(Edge edge) {
        PVector boundary1 = edge.getNode1().getPosition();
        PVector boundary2 = edge.getNode2().getPosition();

        // attach straight edge
        Element edgeElem = createElement("path");
        edgeElem.setAttribute("d",
                "M " + boundary1.x + "," + boundary1.y
                + " L " + boundary2.x + "," + boundary2.y);
        edgeElem.setAttribute("stroke", edge.getColor().toHexString());
        edgeElem.setAttribute("stroke-width", Float.toString(edge.getThickness()));
        appendToParentElement(edgeElem);

        if (edge.showLabel() && edge.hasLabel()) {
            renderEdgeLabel(edge.getLabel());
        }
    }

    public void renderCurvedEdge(Edge edge) {
        for (CubicBezierCurve curve : edge.getCurves()) {
            Element curveElem = createElement("path");
            curveElem.setAttribute("d",
                    "M " + curve.getPt1().x + "," + curve.getPt1().y
                    + " C " + curve.getPt2().x + "," + curve.getPt2().y
                    + " " + curve.getPt3().x + "," + curve.getPt3().y
                    + " " + curve.getPt4().x + "," + curve.getPt4().y);
            curveElem.setAttribute("stroke", edge.getColor().toHexString());
            curveElem.setAttribute("stroke-width", Float.toString(edge.getThickness()));
            curveElem.setAttribute("fill", "none");
            appendToParentElement(curveElem);
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
        arrowElem.setAttribute("points",
                arrow.getPt1().x + "," + arrow.getPt1().y
                + " " + arrow.getPt2().x + "," + arrow.getPt2().y
                + " " + arrow.getPt3().x + "," + arrow.getPt3().y);
        arrowElem.setAttribute("fill", arrow.getColor().toHexString());
        arrowElem.setAttribute("stroke", "none");
        appendToParentElement(arrowElem);
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
        labelElem.setAttribute("transform",
                "translate(" + label.getPosition().x + "," + label.getPosition().y + ")"
                + " rotate(" + Math.toDegrees(label.getAngle()) + ")");
        labelElem.appendChild(text);
        appendToParentElement(labelElem);
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
        miniLabelElem.setAttribute("transform",
                "translate(" + miniLabel.getPosition().x + "," + miniLabel.getPosition().y + ")"
                + " rotate(" + Math.toDegrees(miniLabel.getAngle()) + ")");
        miniLabelElem.appendChild(text);
        appendToParentElement(miniLabelElem);
    }

    /**
     * Cleans all fields.
     */
    private void clean() {
        progress = null;
        cancel = false;
        doc = null;
        lastLabel = null;
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

        // graph positioning
        PVector topLeft = graphSheet.getTopLeftPosition().get();
        PVector bottomRight = graphSheet.getBottomRightPosition().get();
        PVector box = PVector.sub(bottomRight, topLeft);

        // root element
        Element svgRoot = doc.getDocumentElement();
        svgRoot.setAttributeNS(null, "width", supportSize.getWidth());
        svgRoot.setAttributeNS(null, "height", supportSize.getHeight());
        svgRoot.setAttributeNS(null, "version", "1.1");
        svgRoot.setAttributeNS(null, "viewBox",
                Integer.toString((int) topLeft.x) + " "
                + Integer.toString((int) topLeft.y) + " "
                + Integer.toString((int) box.x) + " "
                + Integer.toString((int) box.y));
        pushParentElement(svgRoot);

        // draws the graph exporting it into the DOM
        renderGraph(graphSheet.getGraph());

        // empties parent element stack
        popParentElement();
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
     * Appends the given element to the current parent element.
     *
     * @param element  the element to append
     */
    private void appendToParentElement(Element element) {
        parentStack.peek().appendChild(element);
    }

    /**
     * Inserts a new element just before a given child of the current parent
     * element.
     * 
     * @param newChild  the new element to insert
     * @param refChild  the reference child element
     */
    private void insertBeforeElement(Element newChild, Element refChild) {
        parentStack.peek().insertBefore(newChild, refChild);
    }

    /**
     * Pushes the given element onto the parent element stack.
     *
     * @param element  the element to push
     */
    private void pushParentElement(Element element) {
        parentStack.push(element);
    }

    /**
     * Pops an element from the parent element stack.
     *
     * @return the element at the top of the parent element stack
     */
    private Element popParentElement() {
        return parentStack.pop();
    }
}
