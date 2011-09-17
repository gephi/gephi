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
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.OutputStream;
import org.gephi.io.exporter.spi.ByteExporter;
import org.gephi.io.exporter.spi.VectorExporter;
import org.gephi.preview.api.PDFTarget;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.api.RenderTarget;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.gephi.project.api.Workspace;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * Class exporting the preview graph as a PDF file.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 * @author Mathieu Bastian
 */
public class PDFExporter implements ByteExporter, VectorExporter, LongTask {

    private ProgressTicket progress;
    private Workspace workspace;
    private OutputStream stream;
    private boolean cancel = false;
    private PDFTarget target;
    //Parameters
    private float marginTop = 18f;
    private float marginBottom = 18f;
    private float marginLeft = 18f;
    private float marginRight = 18f;
    private boolean landscape = false;
    private Rectangle pageSize = PageSize.A4;

    public boolean execute() {
        Progress.start(progress);

        PreviewController controller = Lookup.getDefault().lookup(PreviewController.class);
        controller.refreshPreview(workspace);
        PreviewProperties props = controller.getModel(workspace).getProperties();

        Rectangle size = new Rectangle(pageSize);
        if (landscape) {
            size = new Rectangle(pageSize.rotate());
        }
        size.setBackgroundColor(new BaseColor(props.getColorValue(PreviewProperty.BACKGROUND_COLOR)));

        Document document = new Document(size);
        PdfWriter pdfWriter = null;
        try {
            pdfWriter = PdfWriter.getInstance(document, stream);
            pdfWriter.setPdfVersion(PdfWriter.PDF_VERSION_1_5);
            pdfWriter.setFullCompression();

        } catch (DocumentException ex) {
            Exceptions.printStackTrace(ex);
        }
        document.open();
        PdfContentByte cb = pdfWriter.getDirectContent();
        cb.saveState();

        props.putValue(PDFTarget.LANDSCAPE, landscape);
        props.putValue(PDFTarget.PAGESIZE, size);
        props.putValue(PDFTarget.MARGIN_TOP, new Float((float) marginTop));
        props.putValue(PDFTarget.MARGIN_LEFT, new Float((float) marginLeft));
        props.putValue(PDFTarget.MARGIN_BOTTOM, new Float((float) marginBottom));
        props.putValue(PDFTarget.MARGIN_RIGHT, new Float((float) marginRight));
        props.putValue(PDFTarget.PDF_CONTENT_BYTE, cb);
        target = (PDFTarget) controller.getRenderTarget(RenderTarget.PDF_TARGET, workspace);
        if (target instanceof LongTask) {
            ((LongTask) target).setProgressTicket(progress);
        }

        try {
            controller.render(target, workspace);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        cb.restoreState();
        document.close();

        Progress.finish(progress);

        props.putValue(PDFTarget.PDF_CONTENT_BYTE, null);

        return !cancel;
    }

    public boolean isLandscape() {
        return landscape;
    }

    public float getMarginBottom() {
        return marginBottom;
    }

    public float getMarginLeft() {
        return marginLeft;
    }

    public float getMarginRight() {
        return marginRight;
    }

    public float getMarginTop() {
        return marginTop;
    }

    public Rectangle getPageSize() {
        return pageSize;
    }

    public void setMarginBottom(float marginBottom) {
        this.marginBottom = marginBottom;
    }

    public void setMarginLeft(float marginLeft) {
        this.marginLeft = marginLeft;
    }

    public void setMarginRight(float marginRight) {
        this.marginRight = marginRight;
    }

    public void setMarginTop(float marginTop) {
        this.marginTop = marginTop;
    }

    public void setPageSize(Rectangle pageSize) {
        this.pageSize = pageSize;
    }

    public void setOutputStream(OutputStream stream) {
        this.stream = stream;
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    public void setLandscape(boolean landscape) {
        this.landscape = landscape;
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    public boolean cancel() {
        this.cancel = true;
        if (target instanceof LongTask) {
            ((LongTask) target).cancel();
        }
        return true;
    }

    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progress = progressTicket;
    }
}
