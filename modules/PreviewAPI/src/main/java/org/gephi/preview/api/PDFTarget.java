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

import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;

/**
 * Rendering target to PDF format.
 * <p>
 * This target is used by renderers objects to render a graph to PDF and uses
 * the <a href=https://pdfbox.apache.org/">PDFBox</a> Java library.
 * <p>
 * The target give access to the <code>PDPageContentStream</code> object from PDFBox to
 * draw items.
 * <p>
 * When this target is instantiated it uses property values defined in the
 * {@link PreviewProperties}. Namely is uses <code>MARGIN_LEFT</code>,
 * <code>MARGIN_TOP</code>, <code>MARGIN_BOTTOM</code>, <code>MARGIN_RIGHT</code>,
 * <code>LANDSCAPE</code> and <code>PAGESIZE</code>.
 *
 * @author Yudi Xue, Mathieu Bastian
 */
public interface PDFTarget extends RenderTarget {

    String PDF_CONTENT_BYTE = "pdf.contentbyte";
    String MARGIN_LEFT = "pdf.margin.left";
    String MARGIN_TOP = "pdf.margin.top";
    String MARGIN_BOTTOM = "pfd.margin.bottom";
    String MARGIN_RIGHT = "pdf.margin.right";
    String LANDSCAPE = "pdf.landscape";
    String PAGESIZE = "pdf.pagesize";
    String PDF_DOCUMENT = "pdf.document";
    String TRANSPARENT_BACKGROUND = "pdf.transparent.background";

    /**
     * Returns the <code>PDPageContentStream</code> instance of the PDFTarget. PDPageContentStream
     * offers a set of drawing functions which can be used by Renderer objects.
     *
     * @return a PDPageContentStream object
     */
    PDPageContentStream getContentStream();

    /**
     * Get the PDFBox equivalent the Java font.
     * <p>
     * If <code>font</code> can't be found it returns the default Helvetica font.
     * <p>
     * Note that each font (regardless of font size) is embedded in the PDF Document. Also note that some fonts might
     * miss Unicode characters, which will prevent the text from being rendered properly.
     *
     * @param font the reference Java font
     * @return the PDFont, or Helvetica is not found
     */
    PDFont getPDFont(java.awt.Font font);

    /**
     * Returns the margin at the bottom of the page.
     *
     * @return the bottom margin, in pixels
     */
    float getMarginBottom();

    /**
     * Returns the margin at the left of the page.
     *
     * @return the left margin, in pixels
     */
    float getMarginLeft();

    /**
     * Returns the margin at the right of the page.
     *
     * @return the right margin, in pixels
     */
    float getMarginRight();

    /**
     * Returns the margin at the top of the page.
     *
     * @return the top margin, in pixels
     */
    float getMarginTop();

    /**
     * Returns whether the orientation is in landscape or portrait.
     *
     * @return <code>true</code> if the orientation is landscape, <code>false</code>
     * if portrait.
     */
    boolean isLandscape();

    /**
     * Returns the page's size.
     *
     * @return the page size
     */
    PDRectangle getPageSize();
}
