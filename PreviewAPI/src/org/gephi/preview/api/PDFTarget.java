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

/**
 * The PDFTarget is a subtype of RenderTarge which used to specify a PDF render
 * target. The PDFTarget will be used by Renderer objects to render a graph
 * to PDF.
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
     * Set left,right,top and bottom margins all at once.
     * @param leftMargin - the left margin of page
     * @param rightMargin - the left margin of page
     * @param top - the top margin of page
     * @param bottom - the left margin of page
     */
    public void setMargins(float leftMargin, float rightMargin, float top, float bottom);

    /**
     * return the margin at the bottom of page
     * @return float
     */
    public float getMarginBottom();

    /**
     * set the margin at the bottom of page.
     *
     * @param marginBottom float
     */
    public void setMarginBottom(float marginBottom);

    /**
     * return the margin at the left of page.
     *
     * @return float
     */
    public float getMarginLeft();

    /**
     * set the margin at the left of page.
     *
     * @param marginBottom float
     */
    public void setMarginLeft(float marginLeft);

    /**
     * return the margin at the right of page
     *
     * @return float
     */
    public float getMarginRight();

    /**
     * set the margin at the right of page.
     *
     * @param marginBottom float
     */
    public void setMarginRight(float marginRight);

    /**
     * return the margin at the right of page.
     *
     * @return float
     */
    public float getMarginTop();

    /**
     * set the margin at the top of page.
     *
     * @param marginBottom float
     */
    public void setMarginTop(float marginTop);

    /**
     * return whether the RenderTarget is in landscape.
     *
     * @return true the RenderTarget is in landscape; false the
     * RenderTarget is in portrait
     */
    public boolean isLandscape();

    /**
     * set the margin at the bottom of page
     * @param marginBottom float
     */
    public void setLandscape(boolean landscape);

    /**
     * return the size of page.
     *
     * @return a Rectangle object containing such information
     */
    public Rectangle getPageSize();

    /**
     * set the size of page.
     *
     * @param a Rectangle object
     */
    public void setPageSize(Rectangle pageSize);
    
    /**
     * return the scale ratio.
     * 
     * @return float
     */
    public float getScaleRatio();
}
