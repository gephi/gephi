/*
Copyright 2008-2010 Gephi
Authors : Jeremy Subtil <jeremy.subtil@gephi.org>,
Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.io.exporter.preview;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.DefaultFontMapper;
import com.itextpdf.text.pdf.FontMapper;
import com.itextpdf.text.pdf.FontSelector;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import java.awt.Font;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import org.gephi.io.exporter.spi.ByteExporter;
import org.gephi.io.exporter.spi.VectorExporter;
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
import sun.font.FontManager;

/**
 * Class exporting the preview graph as a PDF file.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 * @author Mathieu Bastian
 */
public class PDFExporter implements GraphRenderer, ByteExporter, VectorExporter, LongTask {

    private ProgressTicket progress;
    private Workspace workspace;
    private OutputStream stream;
    private boolean cancel = false;
    private PdfContentByte cb;
    private Document document;
    //Parameters
    private float marginTop = 18f;
    private float marginBottom = 18f;
    private float marginLeft = 18f;
    private float marginRight = 18f;
    private boolean landscape = false;
    private Rectangle pageSize = PageSize.A4;
    private FontMapper fontMapper = new DefaultFontMapper();

    public boolean execute() {
        // fetches the preview graph sheet
        PreviewController controller = Lookup.getDefault().lookup(PreviewController.class);
        GraphSheet graphSheet = controller.getGraphSheet();
        Graph graph = graphSheet.getGraph();
        try {
            exportData(graph);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return !cancel;
    }

    /**
     * Does export the preview graph as an SVG image.
     *
     * @param file         the output SVG file
     * @throws Exception
     */
    private void exportData(Graph graph) throws Exception {
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

        Rectangle size = new Rectangle(pageSize);
        if (landscape) {
            size = new Rectangle(pageSize.rotate());
        }
        size.setBackgroundColor(new BaseColor(Lookup.getDefault().lookup(PreviewController.class).getModel().getBackgroundColor()));
        document = new Document(size);
        PdfWriter pdfWriter = PdfWriter.getInstance(document, stream);
        document.open();
        cb = pdfWriter.getDirectContent();
        cb.saveState();

        //Limits
        float minX = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;
        for (Node n : graph.getNodes()) {
            minX = Math.min(minX, n.getPosition().getX() - n.getRadius() - n.getBorderWidth());
            maxX = Math.max(maxX, n.getPosition().getX() + n.getRadius() + n.getBorderWidth());
            minY = Math.min(minY, -n.getPosition().getY() - n.getRadius() - n.getBorderWidth());
            maxY = Math.max(maxY, -n.getPosition().getY() + n.getRadius() + n.getBorderWidth());
        }

        double graphWidth = maxX - minX;
        double graphHeight = maxY - minY;
        double centerX = minX + graphWidth / 2.;
        double centerY = minY + graphHeight / 2.;

        //Transform
        double pageWidth = size.getWidth() - marginLeft - marginRight;
        double pageHeight = size.getHeight() - marginTop - marginBottom;
        double ratioWidth = pageWidth / graphWidth;
        double ratioHeight = pageHeight / graphHeight;
        double scale = ratioWidth < ratioHeight ? ratioWidth : ratioHeight;
        double translateX = (marginLeft + pageWidth / 2.) / scale;
        double translateY = (marginBottom + pageHeight / 2.) / scale;
        cb.transform(AffineTransform.getTranslateInstance(-centerX * scale, -centerY * scale));
        cb.transform(AffineTransform.getScaleInstance(scale, scale));
        cb.transform(AffineTransform.getTranslateInstance(translateX, translateY));

        renderGraph(graph);
        Progress.switchToIndeterminate(progress);

        cb.restoreState();
        document.close();

        Progress.finish(progress);
    }

    public boolean cancel() {
        cancel = true;
        return true;
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
                if (e.showLabel() && e.hasLabel() && e.getLabel().getFont() != null) {
                    renderEdgeLabel(e.getLabel());
                }

                if (e.showMiniLabels()) {
                    renderEdgeMiniLabels(e);
                }
            }
        }

        for (BidirectionalEdge e : graph.getBidirectionalEdges()) {
            if (!e.isCurved()) {
                if (e.showLabel() && e.hasLabel() && e.getLabel().getFont() != null) {
                    renderEdgeLabel(e.getLabel());
                }

                if (e.showMiniLabels()) {
                    renderEdgeMiniLabels(e);
                }
            }
        }

        for (UndirectedEdge e : graph.getUndirectedEdges()) {
            if (e.showLabel() && !e.isCurved() && e.hasLabel() && e.getLabel().getFont() != null) {
                renderEdgeLabel(e.getLabel());
            }
        }

        for (Node n : graph.getNodes()) {
            if (n.showLabel() && n.hasLabel() && n.getLabel().getFont() != null) {
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
        cb.circle(center.getX(), -center.getY(), node.getRadius());
        cb.fillStroke();
    }

    public void renderNodeLabel(NodeLabel label) {
        Point p = label.getPosition();
        Font font = label.getFont();

        setFillColor(label.getColor());

        try {
            BaseFont bf = genBaseFont(font);
            float ascent = bf.getAscentPoint(label.getValue(), font.getSize());
            float descent = bf.getDescentPoint(label.getValue(), font.getSize());
            float textHeight = (ascent - descent) / 2f;

            cb.beginText();
            cb.setFontAndSize(bf, font.getSize());
            cb.showTextAligned(PdfContentByte.ALIGN_CENTER, label.getValue(), p.getX(), -p.getY() - textHeight, 0);
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

        cb.moveTo(pt1.getX(), -pt1.getY());
        cb.lineTo(pt2.getX(), -pt2.getY());
        cb.lineTo(pt3.getX(), -pt3.getY());
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
            cb.showTextAligned(PdfContentByte.ALIGN_CENTER, label.getValue(), p.getX(), -p.getY(), (float) (Math.toDegrees(-label.getAngle())));
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
            cb.showTextAligned(miniLabel.getHAlign().toIText(), miniLabel.getValue(), p.getX(), -p.getY(), (float) (Math.toDegrees(-miniLabel.getAngle())));
            cb.endText();
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * Generates an iText BaseFont object from a Java Font one.
     *
     * @param font  the reference font
     * @return      the generated BaseFont
     * @throws      DocumentException
     * @throws      IOException
     */
    private BaseFont genBaseFont(java.awt.Font font) throws DocumentException, IOException {
        if (font != null) {
            try {
                if (fontMapper instanceof DefaultFontMapper) {
                    DefaultFontMapper defaultFontMapper = (DefaultFontMapper) fontMapper;
                    String fontName = FontManager.getFileNameForFontName(font.getFontName()).toLowerCase();
                    if (fontName != null && !fontName.isEmpty()) {
                        String fontFilePath = FontManager.getFontPath(true) + "/" + fontName;

                        if (fontName.endsWith(".ttf") || fontName.endsWith(".otf") || fontName.endsWith(".afm")) {
                            Object allNames[] = BaseFont.getAllFontNames(fontFilePath, BaseFont.CP1252, null);
                            defaultFontMapper.insertNames(allNames, fontFilePath);
                        } else if (fontName.endsWith(".ttc")) {
                            String ttcs[] = BaseFont.enumerateTTCNames(fontFilePath);
                            for (int j = 0; j < ttcs.length; ++j) {
                                String nt = fontFilePath + "," + j;
                                Object allNames[] = BaseFont.getAllFontNames(nt, BaseFont.CP1252, null);
                                defaultFontMapper.insertNames(allNames, nt);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return BaseFont.createFont();
            }
            return fontMapper.awtToPdf(font);
        }
        return BaseFont.createFont();
    }

    /**
     * Draws a line.
     *
     * @param start  the start of the line to draw
     * @param end    the end of the line to draw
     */
    private void line(Point start, Point end) {
        cb.moveTo(start.getX(), -start.getY());
        cb.lineTo(end.getX(), -end.getY());
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

        cb.moveTo(pt1.getX(), -pt1.getY());
        cb.curveTo(pt2.getX(), -pt2.getY(), pt3.getX(), -pt3.getY(), pt4.getX(), -pt4.getY());
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

    public float getMarginBottom() {
        return marginBottom;
    }

    public void setMarginBottom(float marginBottom) {
        this.marginBottom = marginBottom;
    }

    public float getMarginLeft() {
        return marginLeft;
    }

    public void setMarginLeft(float marginLeft) {
        this.marginLeft = marginLeft;
    }

    public float getMarginRight() {
        return marginRight;
    }

    public void setMarginRight(float marginRight) {
        this.marginRight = marginRight;
    }

    public float getMarginTop() {
        return marginTop;
    }

    public void setMarginTop(float marginTop) {
        this.marginTop = marginTop;
    }

    public boolean isLandscape() {
        return landscape;
    }

    public void setLandscape(boolean landscape) {
        this.landscape = landscape;
    }

    public Rectangle getPageSize() {
        return pageSize;
    }

    public void setPageSize(Rectangle pageSize) {
        this.pageSize = pageSize;
    }

    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progress = progressTicket;
    }

    public void setOutputStream(OutputStream stream) {
        this.stream = stream;
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    public void setFontMapper(FontMapper fontMapper) {
        this.fontMapper = fontMapper;
    }

    public FontMapper getFontMapper() {
        return fontMapper;
    }
}
