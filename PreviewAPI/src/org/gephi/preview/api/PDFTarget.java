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
package org.gephi.preview.api;

import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;

/**
 * Rendering target to PDF format.
 * <p>
 * This target is used by renderers objects to render a graph to PDF.
 *
 * @author Yudi Xue
 */
public interface PDFTarget extends RenderTarget {

    public static final String PDF_CONTENT_BYTE = "pdf.contentbyte";
    public static final String MARGIN_LEFT = "pdf.margin.left";
    public static final String MARGIN_TOP = "pdf.margin.top";
    public static final String MARGIN_BOTTOM = "pfd.margin.bottom";
    public static final String MARGIN_RIGHT = "pdf.margin.right";
    public static final String LANDSCAPE = "pdf.landscape";
    public static final String PAGESIZE = "pdf.pagesize";

    /**
     * Returns the PDFContentBype instance of the PDFTarget. PDFContentByte
     * offers a set of drawing functions which can be used by Renderer objects.
     * 
     * @return a PDFContentBype object 
     */
    public PdfContentByte getContentByte();

    /**
     * Get a the equivalent in iText of the Java font
     *
     * @param font  the reference Java font
     * @return      the iText BaseFont
     */
    public BaseFont getBaseFont(java.awt.Font font);

    /**
     * Returns the margin at the bottom of the page.
     * 
     * @return the bottom margin, in pixels
     */
    public float getMarginBottom();

    /**
     * Returns the margin at the left of the page.
     *
     * @return the left margin, in pixels
     */
    public float getMarginLeft();

    /**
     * Returns the margin at the right of the page.
     *
     * @return the right margin, in pixels
     */
    public float getMarginRight();

    /**
     * Returns the margin at the right of the page.
     *
     * @return the top margin, in pixels
     */
    public float getMarginTop();

    /**
     * Returns whether the orientation is in landscape or portrait.
     *
     * @return <code>true</code> if the orientation is landscape, <code>false</code>
     * if portrait.
     */
    public boolean isLandscape();

    /**
     * Returns the page's size.
     *
     * @return the page size
     */
    public Rectangle getPageSize();
}
