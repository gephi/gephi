/*
Copyright 2008-2011 Gephi
Authors : Mathieu Bastian
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
