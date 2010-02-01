/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.io.exporter.preview;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.fop.svg.PDFTranscoder;
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
import org.openide.util.lookup.ServiceProvider;
import org.w3c.dom.Document;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = VectorialFileExporter.class)
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
            Document doc = svgExporter.buildDOM(graphSheet, supportSize);
            svgExporter.clean();
            svgExporter = null;
            
            Progress.switchToIndeterminate(progress);
            export(doc, file);
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

    private void export(Document doc, File file) throws Exception {        
        OutputStream ostream = null;
        PDFTranscoder t = new PDFTranscoder();
        TranscoderInput input = new TranscoderInput(doc);

        // performs transcoding
        try {
            ostream = new BufferedOutputStream(new FileOutputStream(file));
            TranscoderOutput output = new TranscoderOutput(ostream);
            t.transcode(input, output);
        } finally {
            ostream.close();
        }
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
