/*
 Copyright 2008-2010 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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
package org.gephi.visualization.screenshot;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLDrawableFactory;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.TileRendererBase;
import org.gephi.visualization.VizController;
import org.gephi.visualization.swing.GLAbstractListener;
import org.gephi.visualization.text.TextManager;

public class OffscreenCanvas extends GLAbstractListener implements TileRendererBase.TileRendererListener {

    private final boolean transparentBackground;

    public OffscreenCanvas(int width, int height, boolean transparentBackground, int antialiasing) {
        super();

        final GLProfile glp = GLProfile.get(GLProfile.GL2);
        final GLCapabilities caps = getCaps();
        caps.setOnscreen(false);
        caps.setDoubleBuffered(false);
        if (antialiasing == 0) {
            caps.setSampleBuffers(false);
        } else {
            caps.setSampleBuffers(true);
            caps.setNumSamples(antialiasing);
        }

        final GLDrawableFactory factory = GLDrawableFactory.getFactory(glp);
        drawable = factory.createOffscreenAutoDrawable(null, caps, null, width, height);

        drawable.addGLEventListener(this);

        cameraLocation = vizController.getDrawable().getCameraLocation();
        cameraTarget = vizController.getDrawable().getCameraTarget();

        engine = VizController.getInstance().getEngine();
        globalScale = VizController.getInstance().getDrawable().getGlobalScale();
        this.transparentBackground = transparentBackground;
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();

        initConfig(gl);
        engine.initDisplayLists(gl, GLU);

        TextManager textManager = VizController.getInstance().getTextManager();
        textManager.reinitRenderers();
    }

    @Override
    public void initConfig(GL2 gl) {
        super.initConfig(gl);

        gl.glClear(GL2.GL_COLOR_BUFFER_BIT);
        float[] backgroundColor = vizController.getVizModel().getBackgroundColorComponents();
        gl.glClearColor(backgroundColor[0], backgroundColor[1], backgroundColor[2], transparentBackground ? 0f : 1f);
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();

        gl.glClear(GL2.GL_COLOR_BUFFER_BIT);
        float[] backgroundColor = vizController.getVizModel().getBackgroundColorComponents();
        gl.glClearColor(backgroundColor[0], backgroundColor[1], backgroundColor[2], transparentBackground ? 0f : 1f);

        engine.display(gl, GLU);
    }

    @Override
    protected void init(GL2 gl) {

    }

    @Override
    protected void render3DScene(GL2 gl, com.jogamp.opengl.glu.GLU glu) {

    }

    @Override
    protected void reshape3DScene(GL2 gl) {

    }

    @Override
    public void addTileRendererNotify(TileRendererBase trb) {

    }

    @Override
    public void removeTileRendererNotify(TileRendererBase trb) {

    }

    @Override
    public void reshapeTile(TileRendererBase tr, int tileX, int tileY, int tileWidth, int tileHeight, int imageWidth, int imageHeight) {
        GL2 gl = tr.getAttachedDrawable().getGL().getGL2();

        double aspectRatio = (double) imageWidth / (double) imageHeight;

        // Compute overall frustum
        float left, right, bottom, top;
        top = (float) (nearDistance * Math.tan(viewField * 3.14159265 / 360.0));
        bottom = -top;
        left = (float) (bottom * aspectRatio);
        right = (float) (top * aspectRatio);

        float w = right - left;
        float h = top - bottom;

        // Compute tiled frustum
        float l = left + tileX * w / imageWidth;
        float r = l + tileWidth * w / imageWidth;

        float b = bottom + tileY * h / imageHeight;
        float t = b + tileHeight * h / imageHeight;

        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();

        gl.glFrustum(l, r, b, t, nearDistance, farDistance);

        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();

        setCameraPosition(gl, GLU);
    }

    @Override
    public void startTileRendering(TileRendererBase trb) {

    }

    @Override
    public void endTileRendering(TileRendererBase trb) {

    }

    @Override
    public void reinitWindow() {
    }
}
