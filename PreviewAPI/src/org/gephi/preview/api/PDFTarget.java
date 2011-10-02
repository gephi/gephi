/*
Copyright 2008-2011 Gephi
Authors : Yudi Xue <yudi.xue@usask.ca>, Mathieu Bastian
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
