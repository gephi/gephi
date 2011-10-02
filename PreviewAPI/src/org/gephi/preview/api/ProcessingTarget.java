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
package org.gephi.preview.api;

import processing.core.PApplet;
import processing.core.PGraphics;

/**
 * Rendering target to the <a href="http://processing.org">Processing</a> library.
 * <p>
 * Processing is using a <b>Java2D</b> mode so the underlying Processing object
 * used is a <a href="http://processing.googlecode.com/svn/trunk/processing/build/javadoc/core/index.html?processing/core/PGraphicsJava2D.html">PGraphicsJava2D</a>
 * object. 
 * <p>
 * This render target supports two modes: <b>applet</b> or <b>headless</b>. The 
 * applet mode is what is used in Gephi GUI and is backed by a <code>PApplet</code>
 * with zoom and pan control. The headless mode is tuned to work without GUI and 
 * is typically used in exports. Either way users should use <code>getGraphics()</code>
 * method for drawing.
 * <h4>How to create a headless Processing canvas?</h4>
 * Before creating a processing target with the {@link PreviewController#getRenderTarget(java.lang.String)}
 * method users should define a <b>width</b> and <b>height</b> property in the
 * {@link PreviewProperties}:
 * <pre>
 * PreviewController previewController = Lookup.getDefault().lookup(PreviewController.class);
 * PreviewModel model = previewController.getModel();
 * PreviewProperties props = model.getProperties();
 * props.putValue("width", 800);
 * props.putValue("height", 600);
 * ProcessingTarget target = (ProcessingTarget)previewController.getRenderTarget(RenderTarget.PROCESSING_TARGET);
 * </pre>
 * @author Mathieu Bastian
 */
public interface ProcessingTarget extends RenderTarget {

    /**
     * Returns the current graphics object. Use this method to draw to Processing.
     * The <code>PGRraphics</code> object can be cast to <code>PGraphicsJava2D</code>.
     * @return the current graphics to draw to
     */
    public PGraphics getGraphics();

    /**
     * Returns the current applet if the mode is set to applet. If in headless
     * mode returns <code>null</code>. Always use <code>getGraphics()</code>
     * for drawing.
     * @return the current applet or <code>null</code> if in headless mode
     */
    public PApplet getApplet();

    /**
     * Resets the zoom level to default
     */
    public void resetZoom();

    /**
     * Zooms in
     */
    public void zoomPlus();

    /**
     * Zooms out
     */
    public void zoomMinus();

    /**
     * Redraw the Processing canvas
     */
    public void refresh();

    /**
     * Returns <code>true</code> if the applet is finished redrawing
     * @return <code>true</code> if the applet is redrawn, <code>false</code> otherwise
     */
    public boolean isRedrawn();
}
