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
