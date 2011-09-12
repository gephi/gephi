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

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import java.io.IOException;

/**
 * Rendering target to PDF format.
 * <p>
 * This target is used by renderers objects to render a graph to PDF.
 *
 * @author Yudi Xue
 */
public interface PDFTarget extends RenderTarget {

    public static final String SCALE_STROKES = "pdf.scale.strokes";
    public static final String TOP_NODES = "pdf.top.nodes";
    public static final String TOP_EDGES = "pdf.top.edges";
    public static final String TOP_NODE_LABELS = "pdf.top.node.labels";
    public static final String TOP_EDGE_LABELS = "pdf.top.edge.labels";

    /**
     * Returns the PDFContentBype instance of the PDFTarget. PDFContentByte
     * offers a set of drawing functions which can be used by Renderer objects.
     * 
     * @return a PDFContentBype object 
     */
    public PdfContentByte getContentByte();

    /**
     * load a Java Font and return an iText BaseFont object.
     *
     * @param font  the reference font
     * @return      the generated BaseFont
     * @throws      DocumentException
     * @throws      IOException
     */
    public BaseFont loadBaseFont(java.awt.Font font) throws DocumentException, IOException;     

    /**
     * Sets left, right, top and bottom margins.
     * 
     * @param leftMargin    the left margin
     * @param rightMargin   the right margin
     * @param top           the top margin of page
     * @param bottom        the bottom margin of page
     */
    public void setMargins(float leftMargin, float rightMargin, float top, float bottom);

    /**
     * Returns the margin at the bottom of the page.
     * 
     * @return the bottom margin, in pixels
     */
    public float getMarginBottom();

    /**
     * Sets the margin at the bottom of the page.
     *
     * @param marginBottom tbe bottom margin, in pixels
     */
    public void setMarginBottom(float marginBottom);

    /**
     * Returns the margin at the left of the page.
     *
     * @return the left margin, in pixels
     */
    public float getMarginLeft();

    /**
     * Sets the margin at the left of the page.
     *
     * @param marginLeft the left margin, in pixels
     */
    public void setMarginLeft(float marginLeft);

    /**
     * Returns the margin at the right of the page.
     *
     * @return the right margin, in pixels
     */
    public float getMarginRight();

    /**
     * Sets the margin at the right of the page.
     *
     * @param marginRight the right margin, in pixels
     */
    public void setMarginRight(float marginRight);

    /**
     * Returns the margin at the right of the page.
     *
     * @return the top margin, in pixels
     */
    public float getMarginTop();

    /**
     * Sets the margin at the top of the page.
     *
     * @param marginTop the top margin, in pixels
     */
    public void setMarginTop(float marginTop);

    /**
     * Returns whether the orientation is in landscape or portrait.
     *
     * @return <code>true</code> if the orientation is landscape, <code>false</code>
     * if portrait.
     */
    public boolean isLandscape();

    /**
     * Sets the page orientation to landscape. Default is portrait.
     * 
     * @param landscape <code>true<code> for landscape, <code>false</code> for
     * portrait
     */
    public void setLandscape(boolean landscape);

    /**
     * Returns the page's size.
     *
     * @return the page size
     */
    public Rectangle getPageSize();

    /**
     * Set the size of page.
     *
     * @param pageSize the size of the page
     */
    public void setPageSize(Rectangle pageSize);

    /**
     * Returns the scale ratio.
     * 
     * @return the scale ratio
     */
    public float getScaleRatio();
}
