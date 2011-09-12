/*
Copyright 2008-2011 Gephi
Authors : Yudi Xue <yudi.xue@usask.ca>
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
package org.gephi.preview;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;

import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.Map;
import org.gephi.preview.api.PDFTarget;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.RenderTarget;
import org.gephi.preview.spi.RenderTargetBuilder;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 * Default implementation to PDFRenderTargetBuilder.
 * 
 * @author Yudi Xue
 */
@ServiceProvider(service = RenderTargetBuilder.class)
public class PDFRenderTargetBuilder implements RenderTargetBuilder {

    @Override
    public String getName() {
        return RenderTarget.PDF_TARGET;
    }

    @Override
    public RenderTarget buildRenderTarget(PreviewModel previewModel) {
        double width = previewModel.getDimensions().getWidth();
        double height = previewModel.getDimensions().getHeight();
        width = Math.max(1, width);
        height = Math.max(1, height);
        int topLeftX = previewModel.getTopLeftPosition().x;
        int topLeftY = previewModel.getTopLeftPosition().y;
        PDFRenderTargetImpl renderTarget = new PDFRenderTargetImpl(width, height, topLeftX, topLeftY);
        return renderTarget;
    }

    public static class PDFRenderTargetImpl extends AbstractRenderTarget implements PDFTarget {

        private PdfContentByte cb;
        private Document document;
        private static boolean fontRegistered = false;
        //Parameters
        private float marginTop = 18f;
        private float marginBottom = 18f;
        private float marginLeft = 18f;
        private float marginRight = 18f;
        private boolean landscape = false;
        private Rectangle pageSize = PageSize.A4;
        private float scale;
        private Map<String, Element> topElements = new HashMap<String, Element>();

        public PDFRenderTargetImpl(double width, double height, double topLeftX, double topLeftY) {
            Rectangle size = new Rectangle(pageSize);
            if (landscape) {
                size = new Rectangle(pageSize.rotate());
            }
            document = new Document(size);
            PdfWriter pdfWriter = null;
            try {
                PdfWriter.getInstance(document, null);
            } catch (DocumentException ex) {
                Exceptions.printStackTrace(ex);
            }
            document.open();
            cb = pdfWriter.getDirectContent();
            cb.saveState();

            double centerX = topLeftX + width / 2;
            double centerY = topLeftY + height / 2;

            //Transform
            double pageWidth = size.getWidth() - marginLeft - marginRight;
            double pageHeight = size.getHeight() - marginTop - marginBottom;
            double ratioWidth = pageWidth / width;
            double ratioHeight = pageHeight / height;
            scale = (float) (ratioWidth < ratioHeight ? ratioWidth : ratioHeight);
            double translateX = (marginLeft + pageWidth / 2.) / scale;
            double translateY = (marginBottom + pageHeight / 2.) / scale;
            cb.transform(AffineTransform.getTranslateInstance(-centerX * scale, -centerY * scale));
            cb.transform(AffineTransform.getScaleInstance(scale, scale));
            cb.transform(AffineTransform.getTranslateInstance(translateX, translateY));
            cb.restoreState();
            document.close();
        }

        @Override
        public float getScaleRatio() {
            return this.scale;
        }

        @Override
        public PdfContentByte getContentByte() {
            return this.cb;
        }
        
        @Override
        public void setMargins(float leftMargin, float rightMargin, float top, float bottom) {
            this.marginTop = top;
            this.marginBottom = bottom;
            this.marginLeft = leftMargin;
            this.marginRight = rightMargin;
        }

        @Override
        public float getMarginBottom() {
            return marginBottom;
        }

        @Override
        public void setMarginBottom(float marginBottom) {
            this.marginBottom = marginBottom;
        }

        @Override
        public float getMarginLeft() {
            return marginLeft;
        }

        @Override
        public void setMarginLeft(float marginLeft) {
            this.marginLeft = marginLeft;
        }

        @Override
        public float getMarginRight() {
            return marginRight;
        }

        @Override
        public void setMarginRight(float marginRight) {
            this.marginRight = marginRight;
        }

        @Override
        public float getMarginTop() {
            return marginTop;
        }

        @Override
        public void setMarginTop(float marginTop) {
            this.marginTop = marginTop;
        }

        @Override
        public boolean isLandscape() {
            return landscape;
        }

        @Override
        public void setLandscape(boolean landscape) {
            this.landscape = landscape;
        }

        @Override
        public Rectangle getPageSize() {
            return pageSize;
        }

        @Override
        public void setPageSize(Rectangle pageSize) {
            this.pageSize = pageSize;
        }
    }
}
