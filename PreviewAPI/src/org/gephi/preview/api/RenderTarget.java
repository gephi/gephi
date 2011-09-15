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

import org.gephi.preview.spi.Renderer;

/**
 * RenderTarget is the graphic container the renderers draw into.
 * <p>
 * There are three types of targets: <b>Processing</b>, <b>PDF</b> or <b>SVG</b>.
 * When the target is Processing, renderers obtain the 
 * <a href="http://processing.googlecode.com/svn/trunk/processing/build/javadoc/core/index.html?processing/core/PGraphicsJava2D.html">PGraphicsJava2D</a> object.
 * For the SVG target, renderers obtain Batik's <a href="http://xmlgraphics.apache.org/batik/using/dom-api.html">Document</a> instance. 
 * As the PDF target rely on the iText library renderers obtain the <a href="http://api.itextpdf.com/itext/index.html?com/itextpdf/text/pdf/PdfContentByte.html">PdfContentByte</a>
 * object.
 * <p>
 * Render targets are not drawing anything. They just make accessible the canvas
 * renderers can draw into.
 * <p>
 * When render targets have specific properties, values should be set in the
 * {@link PreviewProperties}.
 * 
 * @author Yudi Xue, Mathieu Bastian
 * @see Renderer
 */
public interface RenderTarget {

    public static final String PROCESSING_TARGET = "processing";
    public static final String SVG_TARGET = "svg";
    public static final String PDF_TARGET = "pdf";
}
