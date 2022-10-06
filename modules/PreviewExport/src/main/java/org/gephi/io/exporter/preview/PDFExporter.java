/*
Copyright 2008-2010 Gephi
Authors : Jeremy Subtil <jeremy.subtil@gephi.org>,
Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
 */

package org.gephi.io.exporter.preview;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.OutputStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.gephi.io.exporter.spi.ByteExporter;
import org.gephi.io.exporter.spi.VectorExporter;
import org.gephi.preview.api.PDFTarget;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.api.RenderTarget;
import org.gephi.project.api.Workspace;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * Class exporting the preview graph as a PDF file.
 *
 * @author Jérémy Subtil
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
    private PDRectangle pageSize = PDRectangle.A4;

    @Override
    public boolean execute() {
        Progress.start(progress);

        PreviewController controller = Lookup.getDefault().lookup(PreviewController.class);
        controller.getModel(workspace).getProperties().putValue(PreviewProperty.VISIBILITY_RATIO, 1.0);
        controller.refreshPreview(workspace);
        PreviewProperties props = controller.getModel(workspace).getProperties();

        try (PDDocument doc = new PDDocument()) {
            doc.setVersion(1.5f);

            PDRectangle size = landscape ? new PDRectangle(pageSize.getHeight(), pageSize.getWidth()) : pageSize;
            PDPage page = new PDPage(size);
            doc.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(doc, page)) {
                props.putValue(PDFTarget.LANDSCAPE, landscape);
                props.putValue(PDFTarget.PAGESIZE, size);
                props.putValue(PDFTarget.MARGIN_TOP, marginTop);
                props.putValue(PDFTarget.MARGIN_LEFT, marginLeft);
                props.putValue(PDFTarget.MARGIN_BOTTOM, marginBottom);
                props.putValue(PDFTarget.MARGIN_RIGHT, marginRight);
                props.putValue(PDFTarget.PDF_CONTENT_BYTE, contentStream);
                props.putValue(PDFTarget.PDF_DOCUMENT, doc);
                target = (PDFTarget) controller.getRenderTarget(RenderTarget.PDF_TARGET, workspace);
                if (target instanceof LongTask) {
                    ((LongTask) target).setProgressTicket(progress);
                }

                controller.render(target, workspace);
            }
            doc.save(stream);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            Progress.finish(progress);

            props.putValue(PDFTarget.PDF_CONTENT_BYTE, null);
            props.putValue(PDFTarget.PAGESIZE, null);
            props.putValue(PDFTarget.PDF_DOCUMENT, null);
        }
        return !cancel;
    }

    public boolean isLandscape() {
        return landscape;
    }

    public void setLandscape(boolean landscape) {
        this.landscape = landscape;
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

    public PDRectangle getPageSize() {
        return pageSize;
    }

    public void setPageSize(PDRectangle pageSize) {
        this.pageSize = pageSize;
    }

    @Override
    public void setOutputStream(OutputStream stream) {
        this.stream = stream;
    }

    @Override
    public Workspace getWorkspace() {
        return workspace;
    }

    @Override
    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    @Override
    public boolean cancel() {
        this.cancel = true;
        if (target instanceof LongTask) {
            ((LongTask) target).cancel();
        }
        return true;
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progress = progressTicket;
    }
}
