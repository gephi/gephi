package org.gephi.io.exporter.preview;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import java.awt.Font;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.gephi.io.exporter.api.FileType;
import org.gephi.io.exporter.preview.util.LengthUnit;
import org.gephi.io.exporter.preview.util.SupportSize;
import org.gephi.io.exporter.spi.VectorialFileExporter;
import org.gephi.preview.api.BidirectionalEdge;
import org.gephi.preview.api.Color;
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
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.gephi.project.api.Workspace;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * Class exporting the preview graph as an SVG image.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
@ServiceProvider(service = VectorialFileExporter.class)
public class PDFExporter implements GraphRenderer, VectorialFileExporter, LongTask {

    private ProgressTicket progress;
    private boolean cancel = false;
    private PdfContentByte cb;
    private Document document;

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
        return new FileType[]{new FileType(".pdf", "PDF files")};
    }

    public String getName() {
        return "PDF Exporter";
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
        for (Node n : graph.getNodes()) {
            renderNode(n);
        }
    }

    public void renderGraphLabels(Graph graph) {
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
    }

    public void renderNode(Node node) {
        Point center = node.getPosition();
        Color c = node.getColor();
        Color bc = node.getBorderColor();

        cb.setRGBColorStroke(bc.getRed(), bc.getGreen(), bc.getBlue());
        cb.setLineWidth(node.getBorderWidth());
        cb.setRGBColorFill(c.getRed(), c.getGreen(), c.getBlue());
        cb.circle(center.getX(), center.getY(), node.getRadius());
        cb.fillStroke();
    }

    public void renderNodeLabel(NodeLabel label) {
        Point p = label.getPosition();
        Font font = label.getFont();

        setFillColor(label.getColor());

        try {
            BaseFont bf = genBaseFont(font);
            float ascent = bf.getAscentPoint(label.getValue(), font.getSize());

            cb.beginText();
            cb.setFontAndSize(bf, font.getSize());
            cb.showTextAligned(PdfContentByte.ALIGN_CENTER, label.getValue(), p.getX() - ascent / 2, p.getY(), -90);
            cb.endText();
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void renderNodeLabelBorder(NodeLabelBorder border) {
    }

    public void renderSelfLoop(SelfLoop selfLoop) {
        cubicBezierCurve(selfLoop.getCurve());
        setStrokeColor(selfLoop.getColor());
        cb.setLineWidth(selfLoop.getThickness() * selfLoop.getScale());
        cb.stroke();
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
        line(edge.getNode1().getPosition(), edge.getNode2().getPosition());
        setStrokeColor(edge.getColor());
        cb.setLineWidth(edge.getThickness() * edge.getScale());
        cb.stroke();
    }

    public void renderCurvedEdge(Edge edge) {
        for (CubicBezierCurve c : edge.getCurves()) {
            cubicBezierCurve(c);
            setStrokeColor(edge.getColor());
            cb.setLineWidth(edge.getThickness() * edge.getScale());
            cb.stroke();
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
        Point pt1 = arrow.getPt1();
        Point pt2 = arrow.getPt2();
        Point pt3 = arrow.getPt3();
        
        cb.moveTo(pt1.getX(), pt1.getY());
        cb.lineTo(pt2.getX(), pt2.getY());
        cb.lineTo(pt3.getX(), pt3.getY());
        cb.closePath();

        setFillColor(arrow.getColor());
        cb.fill();
    }

    public void renderEdgeLabel(EdgeLabel label) {
        Point p = label.getPosition();
        Font font = label.getFont();

        setFillColor(label.getColor());

        try {
            BaseFont bf = genBaseFont(font);

            cb.beginText();
            cb.setFontAndSize(bf, font.getSize());
            cb.showTextAligned(PdfContentByte.ALIGN_CENTER, label.getValue(), p.getX(), p.getY(), (float) (Math.toDegrees(label.getAngle()) + 180));
            cb.endText();
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void renderEdgeMiniLabel(EdgeMiniLabel miniLabel) {
        Point p = miniLabel.getPosition();
        Font font = miniLabel.getFont();

        setFillColor(miniLabel.getColor());

        try {
            BaseFont bf = genBaseFont(font);

            cb.beginText();
            cb.setFontAndSize(bf, font.getSize());
            cb.showTextAligned(miniLabel.getHAlign().toIText(), miniLabel.getValue(), p.getX(), p.getY(), (float) (Math.toDegrees(miniLabel.getAngle()) + 180));
            cb.endText();
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * Cleans all fields.
     */
    public void clean() {
        progress = null;
        cancel = false;
        cb = null;
        document = null;
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

        // export task
        document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();
        cb = writer.getDirectContent();
        cb.saveState();
        cb.concatCTM(0f, 1f, -1f, 0f, 0f, 0f);
        renderGraph(graphSheet.getGraph());
        cb.restoreState();
        document.close();

        Progress.finish(progress);
    }

    private BaseFont genBaseFont(java.awt.Font font) throws DocumentException, IOException {
        return BaseFont.createFont();
    }

    /**
     * Draws a line.
     * 
     * @param start  the start of the line to draw
     * @param end    the end of the line to draw
     */
    private void line(Point start, Point end) {
        cb.moveTo(start.getX(), start.getY());
        cb.lineTo(end.getX(), end.getY());
    }

    /**
     * Draws a cubic bezier curve.
     *
     * @param curve  the curve to draw
     */
    private void cubicBezierCurve(CubicBezierCurve curve) {
        Point pt1 = curve.getPt1();
        Point pt2 = curve.getPt2();
        Point pt3 = curve.getPt3();
        Point pt4 = curve.getPt4();

        cb.moveTo(pt1.getX(), pt1.getY());
        cb.curveTo(pt2.getX(), pt2.getY(), pt3.getX(), pt3.getY(), pt4.getX(), pt4.getY());
    }

    /**
     * Defines the stroke color.
     *
     * @param color  the stroke color to set
     */
    private void setStrokeColor(Color color) {
        cb.setRGBColorStroke(color.getRed(), color.getGreen(), color.getBlue());
    }

    
    /**
     * Defines the filling color.
     *
     * @param color  the filling color to set
     */
    private void setFillColor(Color color) {
        cb.setRGBColorFill(color.getRed(), color.getGreen(), color.getBlue());
    }
}
