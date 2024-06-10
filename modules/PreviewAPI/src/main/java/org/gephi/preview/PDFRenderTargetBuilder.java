/*
Copyright 2008-2011 Gephi
Authors : Mathieu Bastian
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

package org.gephi.preview;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.FontMappers;
import org.apache.pdfbox.pdmodel.font.FontMapping;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.util.Matrix;
import org.gephi.preview.api.CanvasSize;
import org.gephi.preview.api.PDFTarget;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.api.RenderTarget;
import org.gephi.preview.spi.RenderTargetBuilder;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 * Default implementation to PDFRenderTargetBuilder.
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = RenderTargetBuilder.class)
public class PDFRenderTargetBuilder implements RenderTargetBuilder {

    @Override
    public String getName() {
        return RenderTarget.PDF_TARGET;
    }

    @Override
    public RenderTarget buildRenderTarget(PreviewModel previewModel) {
        CanvasSize cs = previewModel.getGraphicsCanvasSize();
        PreviewProperties properties = previewModel.getProperties();
        float marginBottom = properties.getFloatValue(PDFTarget.MARGIN_BOTTOM);
        float marginLeft = properties.getFloatValue(PDFTarget.MARGIN_LEFT);
        float marginRight = properties.getFloatValue(PDFTarget.MARGIN_RIGHT);
        float marginTop = properties.getFloatValue(PDFTarget.MARGIN_TOP);
        final PDRectangle pageSize = properties.getValue(PDFTarget.PAGESIZE);
        boolean landscape = properties.getBooleanValue(PDFTarget.LANDSCAPE);
        boolean transparentBackground = properties.getBooleanValue(PDFTarget.TRANSPARENT_BACKGROUND);
        Color backgroundColor = transparentBackground ? null : properties.getColorValue(PreviewProperty.BACKGROUND_COLOR);
        PDPageContentStream cb = properties.getValue(PDFTarget.PDF_CONTENT_BYTE);
        PDDocument doc = properties.getValue(PDFTarget.PDF_DOCUMENT);
        PDFRenderTargetImpl renderTarget = new PDFRenderTargetImpl(
            doc,
            cb,
            cs,
            pageSize,
            backgroundColor,
            marginLeft,
            marginRight,
            marginTop,
            marginBottom,
            landscape);
        return renderTarget;
    }

    public static class PDFRenderTargetImpl extends AbstractRenderTarget implements PDFTarget {

        private final PDPageContentStream cb;
        private final PDDocument document;
        //Parameters
        private final float marginTop;
        private final float marginBottom;
        private final float marginLeft;
        private final float marginRight;
        private final boolean landscape;
        private final PDRectangle pageSize;

        private Map<String, PDFont> fontMap;

        public PDFRenderTargetImpl(
            PDDocument doc,
            PDPageContentStream cb,
            CanvasSize cs,
            PDRectangle size,
            Color backgroundColor,
            float marginLeft,
            float marginRight,
            float marginTop,
            float marginBottom,
            boolean landscape) {
            this.document = doc;
            this.cb = cb;
            this.marginTop = marginTop;
            this.marginLeft = marginLeft;
            this.marginBottom = marginBottom;
            this.marginRight = marginRight;
            this.pageSize = size;
            this.landscape = landscape;
            this.fontMap = new HashMap<>();

            double centerX = cs.getX() + cs.getWidth() / 2;
            double centerY = cs.getY() + cs.getHeight() / 2;

            //Transform
            double pageWidth = size.getWidth() - marginLeft - marginRight;
            double pageHeight = size.getHeight() - marginTop - marginBottom;
            double ratioWidth = pageWidth / cs.getWidth();
            double ratioHeight = pageHeight / cs.getHeight();
            double scale = (float) (ratioWidth < ratioHeight ? ratioWidth : ratioHeight);
            double translateX = (marginLeft + pageWidth / 2.) / scale;
            double translateY = (marginBottom + pageHeight / 2.) / scale;
            try {
                // Background
                if (backgroundColor != null) {
                    cb.setNonStrokingColor(backgroundColor);
                    cb.addRect(0, 0, size.getWidth(), size.getHeight());
                    cb.fill();
                }

                // Transformations
                cb.transform(Matrix.getTranslateInstance((float) (-centerX * scale), (float) (centerY * scale)));
                cb.transform(Matrix.getScaleInstance((float) scale, (float) scale));
                cb.transform(Matrix.getTranslateInstance((float) translateX, (float) translateY));
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public PDPageContentStream getContentStream() {
            return this.cb;
        }

        public PDFont getPDFont(java.awt.Font font) {
            final String fontKey = getFontKey(font);
            return fontMap.computeIfAbsent(fontKey, (key) -> {
                FontMapping<TrueTypeFont> mapping = FontMappers.instance().getTrueTypeFont(fontKey, null);
                if (mapping != null) {
                    try {
                        return PDType0Font.load(document, mapping.getFont(), true);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                return new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            });
        }

        private static String getFontKey(Font font) {
            StringBuilder name = new StringBuilder(font.getName().replace(" ", "-"));
            if (font.isBold()) {
                name.append("-Bold");
            }
            if (font.isItalic()) {
                name.append("-Italic");
            }

            return name.toString();
        }

        @Override
        public float getMarginBottom() {
            return marginBottom;
        }

        @Override
        public float getMarginLeft() {
            return marginLeft;
        }

        @Override
        public float getMarginRight() {
            return marginRight;
        }

        @Override
        public float getMarginTop() {
            return marginTop;
        }

        @Override
        public boolean isLandscape() {
            return landscape;
        }

        @Override
        public PDRectangle getPageSize() {
            return pageSize;
        }
    }
}
