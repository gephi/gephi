/*
Copyright 2008-2011 Gephi
Authors : Yudi Xue <yudi.xue@usask.ca>, Mathieu Bastian
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
 * This target is used by renderers objects to render a graph to PDF and uses
 * the <a href="http://itextpdf.com">iText</a> Java library.
 * <p>
 * The target give access to the <code>PDFContentBype</code> object from itext to
 * draw items.
 * <p>
 * When this target is instanciated it uses property values defined in the
 * {@link PreviewProperties}. Namely is uses <code>MARGIN_LEFT</code>, 
 * <code>MARGIN_TOP</code>, <code>MARGIN_BOTTOM</code>, <code>MARGIN_RIGHT</code>, 
 * <code>LANDCAPE</code> and <code>PAGESIZE</code>. 
 * @author Yudi Xue, Mathieu Bastian
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
     * Returns the <code>PDFContentBype</code> instance of the PDFTarget. PDFContentByte
     * offers a set of drawing functions which can be used by Renderer objects.
     * 
     * @return a PDFContentBype object 
     */
    public PdfContentByte getContentByte();

    /**
     * Get a the equivalent in iText of the Java font. Base fonts are either
     * Type 1 fonts (PDF default's font) or valid system fonts. The first time
     * a base font which is not a Type 1 is requested the system will
     * register the system fonts in order to find the right font. This might
     * take some time up to a minute.
     * <p>
     * If <code>font</code> can't be found in iText's default fonts or registered
     * fonts it returns the default Helvetica font.
     *
     * @param font  the reference Java font
     * @return      the iText BaseFont, or Helvetica is not found
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
     * Returns the margin at the top of the page.
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
