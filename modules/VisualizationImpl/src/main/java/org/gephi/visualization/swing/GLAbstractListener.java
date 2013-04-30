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
package org.gephi.visualization.swing;

import com.jogamp.common.nio.Buffers;
import java.awt.Color;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.glu.GLU;
import org.gephi.visualization.VizController;
import org.gephi.visualization.config.GraphicalConfiguration;
import org.gephi.visualization.opengl.Lighting;
import org.gephi.visualization.screenshot.ScreenshotMaker;
import org.openide.util.Exceptions;

/**
 *
 * @author Mathieu Bastian
 */
public abstract class GLAbstractListener implements GLEventListener {

    protected GLAutoDrawable drawable;
    protected VizController vizController;
    public static final GLU glu = new GLU();
    private static boolean DEBUG = true;
    private long startTime = 0;
    protected float fps;
    protected float fpsAvg = 0;
    protected float fpsCount = 0;
    private volatile boolean resizing = false;
    public final float viewField = 30.0f;
    public final float nearDistance = 1.0f;
    public final float farDistance = 100000f;
    private double aspectRatio = 0;
    protected FloatBuffer projMatrix = Buffers.newDirectFloatBuffer(16);
    protected FloatBuffer modelMatrix = Buffers.newDirectFloatBuffer(16);
    protected IntBuffer viewport = Buffers.newDirectIntBuffer(4);
    protected GraphicalConfiguration graphicalConfiguration;
    protected Lighting lighting = new Lighting();
    protected ScreenshotMaker screenshotMaker;

    protected void initDrawable(GLAutoDrawable drawable) {
        this.drawable = drawable;
        drawable.addGLEventListener(this);
    }

    protected abstract void init(GL2 gl);

    protected abstract void render3DScene(GL2 gl, GLU glu);

    protected abstract void reshape3DScene(GL2 gl);

    protected abstract void setCameraPosition(GL2 gl, GLU glu);

    protected GLCapabilities getCaps() {
        GLProfile profile = GLProfile.get(GLProfile.GL2);
        GLCapabilities caps = new GLCapabilities(profile);

        try {
            caps.setAlphaBits(8);		//if NOT opaque
            caps.setDoubleBuffered(true);
            caps.setHardwareAccelerated(true);

            //FSAA
            int antialisaing = vizController.getVizConfig().getAntialiasing();
            if (antialisaing == 0) {
                caps.setSampleBuffers(false);
            } else if (antialisaing == 2) {
                caps.setSampleBuffers(true);
                caps.setNumSamples(2);
            } else if (antialisaing == 4) {
                caps.setSampleBuffers(true);
                caps.setNumSamples(4);
            } else if (antialisaing == 8) {
                caps.setSampleBuffers(true);
                caps.setNumSamples(8);
            } else if (antialisaing == 16) {
                caps.setSampleBuffers(true);
                caps.setNumSamples(16);
            }
        } catch (javax.media.opengl.GLException ex) {
            Exceptions.printStackTrace(ex);
        }

        return caps;
    }

    public void initConfig(GL2 gl) {
        //Disable Vertical synchro
        gl.setSwapInterval(0);

        //Depth
        if (vizController.getVizModel().isUse3d()) {
            gl.glEnable(GL2.GL_DEPTH_TEST);      //Enable Z-Ordering
            gl.glDepthFunc(GL2.GL_LEQUAL);
            gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);	//Correct texture & colors perspective calculations
        } else {
            gl.glDisable(GL2.GL_DEPTH_TEST);     //Z is set by the order of drawing
        }

        //Cull face
        if (vizController.getVizModel().isCulling()) {        //When enabled, increases performance but polygons must be drawn counterclockwise
            gl.glEnable(GL2.GL_CULL_FACE);
            gl.glCullFace(GL2.GL_BACK);      //Hide back face of polygons
        }

        //Point Smooth
        if (vizController.getVizConfig().isPointSmooth()) {        //Only for GL_POINTS
            gl.glEnable(GL2.GL_POINT_SMOOTH);
            gl.glHint(GL2.GL_POINT_SMOOTH_HINT, GL2.GL_NICEST); //Point smoothing
        } else {
            gl.glDisable(GL2.GL_POINT_SMOOTH);
        }

        //Light Smooth
        if (vizController.getVizConfig().isLineSmooth()) {         //Only for GL_LINES
            gl.glEnable(GL2.GL_LINE_SMOOTH);
            if (vizController.getVizConfig().isLineSmoothNicest()) {
                gl.glHint(GL2.GL_LINE_SMOOTH_HINT, GL2.GL_NICEST);
            } else {
                gl.glHint(GL2.GL_LINE_SMOOTH_HINT, GL2.GL_FASTEST);
            }
        } else {
            gl.glDisable(GL2.GL_LINE_SMOOTH);
        }

        gl.glClearDepth(1.0f);

        //Background
        Color backgroundColor = vizController.getVizModel().getBackgroundColor();
        gl.glClearColor(backgroundColor.getRed() / 255f, backgroundColor.getGreen() / 255f, backgroundColor.getBlue() / 255f, 1f);

        //Lighting
        if (vizController.getVizModel().isLighting()) {
            gl.glEnable(GL2.GL_LIGHTING);
            setLighting(gl);
            gl.glEnable(GL2.GL_NORMALIZE);       //Normalise colors when glScale used
            gl.glShadeModel(GL2.GL_SMOOTH);
        } else {
            gl.glDisable(GL2.GL_LIGHTING);
            gl.glShadeModel(GL2.GL_FLAT);
        }

        //Blending
        if (vizController.getVizConfig().isBlending()) {
            gl.glEnable(GL2.GL_BLEND);
            if (vizController.getVizConfig().isBlendCinema()) {
                gl.glBlendFunc(GL2.GL_CONSTANT_COLOR, GL2.GL_ONE_MINUS_SRC_ALPHA);        //Black display
            } else {
                gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);             //Use alpha values correctly
            }
        }


        //Material
        if (vizController.getVizModel().isMaterial()) {
            gl.glColorMaterial(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE);
            gl.glEnable(GL2.GL_COLOR_MATERIAL);                                      //Use color and avoid using glMaterial
        }
        //Mesh view
        if (vizController.getVizConfig().isWireFrame()) {
            gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
        }

        gl.glEnable(GL2.GL_TEXTURE_2D);

    }

    protected void setLighting(GL2 gl) {
        lighting = new Lighting();
        lighting.glInit(gl);
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();

        graphicalConfiguration = new GraphicalConfiguration();
        graphicalConfiguration.checkGeneralCompatibility(gl);

        //Reinit viewport, to ensure reshape to perform
        viewport = Buffers.newDirectIntBuffer(4);

        resizing = false;
        initConfig(gl);
        init(gl);
    }

    @Override
    public void display(GLAutoDrawable drawable) {

        //Screenshot
        screenshotMaker.openglSignal(drawable);

        //FPS
        if (startTime == 0) {
            startTime = System.currentTimeMillis() - 1;
        }
        long endTime = System.currentTimeMillis();
        long delta = endTime - startTime;
        startTime = endTime;
        fps = 1000.0f / delta;
        if (fps < 100) {
            fpsAvg = (fpsAvg * fpsCount + fps) / ++fpsCount;
        }

        GL2 gl = drawable.getGL().getGL2();

        if (vizController.getVizModel().isUse3d()) {
            gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        } else {
            gl.glClear(GL2.GL_COLOR_BUFFER_BIT);
        }

        render3DScene(gl, glu);
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        if (!resizing) {
            if (viewport.get(2) == width && viewport.get(3) == height)//NO need
            {
                return;
            }
            resizing = true;

            if (height == 0) {
                height = 1;
            }
            if (width == 0) {
                width = 1;
            }

            int viewportW = 0, viewportH = 0, viewportX = width, viewportY = height;

            aspectRatio = (double) width / (double) height;
            viewportH = height;
            viewportW = (int) (height * aspectRatio);
            if (viewportW > width) {
                viewportW = width;
                viewportH = (int) (width * (1 / aspectRatio));
            }
            viewportX = ((width - viewportW) / 2);
            viewportY = ((height - viewportH) / 2);

            GL2 gl = drawable.getGL().getGL2();

            gl.glViewport(viewportX, viewportY, viewportW, viewportH);
            gl.glGetIntegerv(GL2.GL_VIEWPORT, viewport);//Update viewport buffer

            gl.glMatrixMode(GL2.GL_PROJECTION);
            gl.glLoadIdentity();
            glu.gluPerspective(viewField, aspectRatio, nearDistance, farDistance);
            gl.glGetFloatv(GL2.GL_PROJECTION_MATRIX, projMatrix);//Update projection buffer


            gl.glMatrixMode(GL2.GL_MODELVIEW);
            gl.glLoadIdentity();

            reshape3DScene(drawable.getGL().getGL2());

            if (DEBUG) {
                DEBUG = false;
                System.err.println("GL_VENDOR: " + gl.glGetString(GL2.GL_VENDOR));
                System.err.println("GL_RENDERER: " + gl.glGetString(GL2.GL_RENDERER));
                System.err.println("GL_VERSION: " + gl.glGetString(GL2.GL_VERSION));
            }

            resizing = false;
        }
    }

    public GL2 getGL() {
        return drawable.getGL().getGL2();
    }

    public void setVizController(VizController vizController) {
        this.vizController = vizController;
    }

    public GLAutoDrawable getGLAutoDrawable() {
        return drawable;
    }

    public Lighting getLighting() {
        return lighting;
    }

    public GraphicalConfiguration getGraphicalConfiguration() {
        return graphicalConfiguration;
    }

    protected void resetFpsAverage() {
        fpsAvg = 0;
        fpsCount = 0;
    }

    protected float getFpsAverage() {
        return fpsAvg;
    }
}
