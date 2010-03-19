package org.gephi.io.exporter.preview;

import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.DefaultFontMapper;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.FileOutputStream;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.gvt.GraphicsNode;
import org.gephi.io.exporter.api.FileType;
import org.gephi.io.exporter.preview.util.LengthUnit;
import org.gephi.io.exporter.preview.util.SupportSize;
import org.gephi.io.exporter.spi.VectorialFileExporter;
import org.gephi.preview.api.Graph;
import org.gephi.preview.api.GraphSheet;
import org.gephi.preview.api.PreviewController;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
public class PDFExporter implements VectorialFileExporter, LongTask {

    private SVGExporter svgExporter;
    private ProgressTicket progress;
    private boolean cancel = false;

    public boolean exportData(File file, Workspace workspace) throws Exception {
        try {
            Progress.start(progress);

            PreviewController controller = Lookup.getDefault().lookup(PreviewController.class);
            GraphSheet graphSheet = controller.getGraphSheet();
            Graph graph = graphSheet.getGraph();
            SupportSize supportSize = new SupportSize(210, 297, LengthUnit.MILLIMETER);

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

            svgExporter = new SVGExporter();
            svgExporter.setProgressTicket(progress);
            org.w3c.dom.Document svgDoc = svgExporter.buildDOM(graphSheet, supportSize);
            svgExporter.clean();
            svgExporter = null;

            Progress.switchToIndeterminate(progress);
            export(svgDoc, file);
            Progress.finish(progress);
        } catch (Exception e) {
            clean();
            throw e;
        }
        boolean c = cancel;
        clean();
        return !c;
    }

    private void clean() {
        progress = null;
        cancel = false;
    }

    private void export(org.w3c.dom.Document svgDoc, File file) throws Exception {
        // Batik initializations
        UserAgent userAgent = new UserAgentAdapter();
        DocumentLoader loader = new DocumentLoader(userAgent);
        BridgeContext ctx = new BridgeContext(userAgent, loader);
        GVTBuilder builder = new GVTBuilder();
        ctx.setDynamicState(BridgeContext.DYNAMIC);

        // SVG rendered as graphics
        GraphicsNode gn = builder.build(ctx, svgDoc);

        // PDF size set
        Rectangle pageSize = PageSize.A4;
        float width = pageSize.getWidth();
        float height = pageSize.getHeight();

        // TODO SVG graphics resized according to the PDF size
        gn.setTransform(AffineTransform.getScaleInstance(0.5, 0.5));

        // PDF file initialization
        Document pdfDoc = new Document();
        PdfWriter writer = PdfWriter.getInstance(pdfDoc, new FileOutputStream(file));
        pdfDoc.open();
        PdfContentByte cb = writer.getDirectContent();
        PdfTemplate tp = cb.createTemplate(width, height);
        Graphics2D g2d = tp.createGraphics(width, height, new DefaultFontMapper());

        // SVG added to PDF
        gn.paint(g2d);
        g2d.dispose();
        cb.addTemplate(tp, 0, 0);

        pdfDoc.close();
    }

    public FileType[] getFileTypes() {
        return new FileType[]{new FileType(".pdf", "PDF files")};
    }

    public String getName() {
        return "PDF Exporter";
    }

    public boolean cancel() {
        cancel = true;
        if (svgExporter != null) {
            svgExporter.cancel();
        }
        return true;
    }

    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progress = progressTicket;
    }
}
