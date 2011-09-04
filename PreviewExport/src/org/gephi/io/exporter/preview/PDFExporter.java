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

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import java.io.IOException;
import java.io.OutputStream;
import org.gephi.io.exporter.spi.ByteExporter;
import org.gephi.io.exporter.spi.VectorExporter;
import org.gephi.preview.api.PDFTarget;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.RenderTarget;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

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
    private static boolean fontRegistered = false;
    private boolean cancel = false;
    private PDFTarget target;
    //Parameters
    private float marginTop = 18f;
    private float marginBottom = 18f;
    private float marginLeft = 18f;
    private float marginRight = 18f;
    private boolean landscape = false;
    private Rectangle pageSize = PageSize.A4;

    public PDFExporter() {
        FontFactory.register("/org/gephi/io/exporter/preview/fonts/LiberationSans.ttf", "ArialMT");
    }

    public boolean execute() {
        PreviewController controller = Lookup.getDefault().lookup(PreviewController.class);
        controller.refreshPreview(workspace);
        target = (PDFTarget) controller.getRenderTarget(RenderTarget.PDF_TARGET, workspace);
        target.setLandscape(landscape);
        target.setMarginBottom(marginBottom);
        target.setMarginLeft(marginLeft);
        target.setMarginRight(marginRight);
        target.setMarginTop(marginTop);
        target.setPageSize(pageSize);
        if (target instanceof LongTask) {
            ((LongTask) target).setProgressTicket(progress);
        }

        try {
            controller.render(target, workspace);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //Write
        
        Progress.finish(progress);
        
        return !cancel;
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
            BaseFont baseFont = null;
            if (!font.getFontName().equals(FontFactory.COURIER)
                    && !font.getFontName().equals(FontFactory.COURIER_BOLD)
                    && !font.getFontName().equals(FontFactory.COURIER_OBLIQUE)
                    && !font.getFontName().equals(FontFactory.COURIER_BOLDOBLIQUE)
                    && !font.getFontName().equals(FontFactory.HELVETICA)
                    && !font.getFontName().equals(FontFactory.HELVETICA_BOLD)
                    && !font.getFontName().equals(FontFactory.HELVETICA_BOLDOBLIQUE)
                    && !font.getFontName().equals(FontFactory.HELVETICA_OBLIQUE)
                    && !font.getFontName().equals(FontFactory.SYMBOL)
                    && !font.getFontName().equals(FontFactory.TIMES_ROMAN)
                    && !font.getFontName().equals(FontFactory.TIMES_BOLD)
                    && !font.getFontName().equals(FontFactory.TIMES_ITALIC)
                    && !font.getFontName().equals(FontFactory.TIMES_BOLDITALIC)
                    && !font.getFontName().equals(FontFactory.ZAPFDINGBATS)
                    && !font.getFontName().equals(FontFactory.COURIER_BOLD)
                    && !font.getFontName().equals(FontFactory.COURIER_BOLD)
                    && !font.getFontName().equals(FontFactory.COURIER_BOLD)) {

                com.itextpdf.text.Font itextFont = FontFactory.getFont(font.getFontName(), BaseFont.IDENTITY_H, font.getSize(), font.getStyle());
                baseFont = itextFont.getBaseFont();
                if (baseFont == null && !PDFExporter.fontRegistered) {

                    String displayName = progress.getDisplayName();
                    Progress.setDisplayName(progress, NbBundle.getMessage(PDFExporter.class, "ExporterPDF.font.registration"));
                    FontFactory.registerDirectories();
                    Progress.setDisplayName(progress, displayName);

                    itextFont = FontFactory.getFont(font.getFontName(), BaseFont.IDENTITY_H, font.getSize(), font.getStyle());
                    baseFont = itextFont.getBaseFont();

                    PDFExporter.fontRegistered = true;
                }
            } else {
                com.itextpdf.text.Font itextFont = FontFactory.getFont(font.getFontName(), BaseFont.IDENTITY_H, font.getSize(), font.getStyle());
                baseFont = itextFont.getBaseFont();
            }

            if (baseFont != null) {
                return baseFont;
            }
            return BaseFont.createFont();
        }
        return BaseFont.createFont();
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
