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
import com.jogamp.newt.event.MouseAdapter;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.glu.GLU;
import java.awt.Component;
import java.awt.Point;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gephi.lib.gleem.linalg.Vec3f;
import org.gephi.visualization.VizArchitecture;
import org.gephi.visualization.VizController;
import org.gephi.visualization.VizModel;
import org.gephi.visualization.apiimpl.GraphDrawable;
import org.gephi.visualization.apiimpl.GraphIO;
import org.gephi.visualization.apiimpl.Scheduler;
import org.gephi.visualization.opengl.AbstractEngine;
import org.gephi.visualization.opengl.GraphicalConfiguration;
import org.openide.util.Exceptions;

/**
 *
 * @author Mathieu Bastian
 */
public abstract class GLAbstractListener implements GLEventListener, VizArchitecture, GraphDrawable {

    //GLU
    protected static final GLU GLU = new GLU();
    //Architecture
    protected GLAutoDrawable drawable;
    protected VizController vizController;
    protected VizModel vizModel;
    protected GraphIO graphIO;

    private long startTime = 0;
    protected float fps;
    protected float fpsAvg = 0;
    protected float fpsCount = 0;
    private boolean showGLLog = true;
    private volatile boolean resizing = false;
    public final float viewField = 30.0f;
    public final float nearDistance = 1.0f;
    public final float farDistance = 150000f;
    private double aspectRatio = 0;
    protected float globalScale = 1f;
    protected FloatBuffer projMatrix = Buffers.newDirectFloatBuffer(16);
    protected FloatBuffer modelMatrix = Buffers.newDirectFloatBuffer(16);
    protected IntBuffer viewport = Buffers.newDirectIntBuffer(4);
    protected GraphicalConfiguration graphicalConfiguration;
    protected GLWindow window;
    public Component graphComponent;
    protected AbstractEngine engine;
    protected Scheduler scheduler;
    protected float[] cameraLocation;
    protected float[] cameraTarget;
    protected double[] draggingMarker = new double[2];//The drag mesure for a moving of 1 to the viewport
    protected Vec3f cameraVector = new Vec3f();
    protected MouseAdapter graphMouseAdapterNewt;
    protected java.awt.event.MouseAdapter graphMouseAdapterCanvas;
    protected GraphMouseAdapter graphMouseAdapter;

    public GLAbstractListener() {
        this.vizController = VizController.getInstance();
    }

    protected void initDrawable(GLAutoDrawable drawable) {
        this.drawable = drawable;
        drawable.addGLEventListener(this);
    }

    @Override
    public void initArchitecture() {
        this.engine = VizController.getInstance().getEngine();
        this.scheduler = VizController.getInstance().getScheduler();
        this.graphIO = VizController.getInstance().getGraphIO();

        cameraLocation = vizController.getVizConfig().getDefaultCameraPosition();
        cameraTarget = vizController.getVizConfig().getDefaultCameraTarget();

        //Mouse events
        if (vizController.getVizConfig().isReduceFpsWhenMouseOut() || vizController.getVizConfig().isPauseLoopWhenMouseOut()) {
            graphMouseAdapter = new GraphMouseAdapter();
            if (window != null) {
                graphMouseAdapterNewt = new MouseAdapter() {

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        graphMouseAdapter.mouseEntered();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        graphMouseAdapter.mouseExited();
                    }
                };
                window.addMouseListener(graphMouseAdapterNewt);
            } else {
                graphMouseAdapterCanvas = new java.awt.event.MouseAdapter() {

                    @Override
                    public void mouseEntered(java.awt.event.MouseEvent e) {
                        graphMouseAdapter.mouseEntered();
                    }

                    @Override
                    public void mouseExited(java.awt.event.MouseEvent e) {
                        graphMouseAdapter.mouseExited();
                    }
                };
                graphComponent.addMouseListener(graphMouseAdapterCanvas);
            }
        }
    }

    protected abstract void init(GL2 gl);

    protected abstract void render3DScene(GL2 gl, GLU glu);

    protected abstract void reshape3DScene(GL2 gl);

    protected GLCapabilities getCaps() {
        GLProfile profile = GLProfile.get(GLProfile.GL2);
        GLCapabilities caps = new GLCapabilities(profile);

        try {
            caps.setAlphaBits(8);		//if NOT opaque
            caps.setDoubleBuffered(true);
            caps.setHardwareAccelerated(true);

            //FSAA
            int antialisaing = vizController.getVizConfig().getAntialiasing();
            switch (antialisaing) {
                case 0:
                    caps.setSampleBuffers(false);
                    break;
                case 2:
                    caps.setSampleBuffers(true);
                    caps.setNumSamples(2);
                    break;
                case 4:
                    caps.setSampleBuffers(true);
                    caps.setNumSamples(4);
                    break;
                case 8:
                    caps.setSampleBuffers(true);
                    caps.setNumSamples(8);
                    break;
                case 16:
                    caps.setSampleBuffers(true);
                    caps.setNumSamples(16);
                    break;
                default:
            }
        } catch (com.jogamp.opengl.GLException ex) {
            Exceptions.printStackTrace(ex);
        }

        return caps;
    }

    @Override
    public void initConfig(GL2 gl) {
        //Disable Vertical synchro
        gl.setSwapInterval(0);

        //Config
        gl.glDisable(GL2.GL_DEPTH_TEST);     //Z is set by the order of drawing
        gl.glDisable(GL2.GL_POINT_SMOOTH);
        gl.glDisable(GL2.GL_LINE_SMOOTH);

        gl.glClearDepth(1.0f);

        //Background
        float[] backgroundColor = vizController.getVizModel().getBackgroundColorComponents();
        gl.glClearColor(backgroundColor[0], backgroundColor[1], backgroundColor[2], 1f);

        //Blending
        if (vizController.getVizConfig().isBlending()) {
            gl.glEnable(GL2.GL_BLEND);
            gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);             //Use alpha values correctly
        }

        //Lighting
        gl.glDisable(GL2.GL_LIGHTING);
        gl.glShadeModel(GL2.GL_FLAT);

        //Mesh view
        if (vizController.getVizConfig().isWireFrame()) {
            gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
        }

        // Bug: Black faces when enabled
//        gl.glEnable(GL2.GL_TEXTURE_2D);
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();

        graphicalConfiguration = new GraphicalConfiguration();
        graphicalConfiguration.checkGeneralCompatibility(gl);

        vizController.getTextManager().reinitRenderers();
        
        //Reinit viewport, to ensure reshape to perform
        viewport = Buffers.newDirectIntBuffer(4);

        resizing = false;
        initConfig(gl);

//        graphComponent.setCursor(Cursor.getDefaultCursor());
        engine.initEngine(gl, GLU);

        init(gl);
    }

    public void refreshDraggingMarker() {
        //Refresh dragging marker
        /*DoubleBuffer objPos = BufferUtil.newDoubleBuffer(3);
         glu.gluProject(0, 0, 0, modelMatrix, projMatrix, viewport, objPos);
         double dxx = objPos.get(0);
         double dyy = objPos.get(1);
         glu.gluProject(1, 1, 0, modelMatrix, projMatrix, viewport, objPos);
         draggingMarker[0] = dxx - objPos.get(0);
         draggingMarker[1] = dyy - objPos.get(1);
         System.out.print(draggingMarker[0]);*/

        float[] d = myGluProject(0, 0, 0);
        float[] d2 = myGluProject(1, 1, 0);

        draggingMarker[0] = d[0] - d2[0];
        draggingMarker[1] = d[1] - d2[1];

    }

    @Override
    public void setCameraPosition(GL2 gl, GLU glu) {
        //Refresh rotation angle
        gl.glLoadIdentity();
        glu.gluLookAt(cameraLocation[0], cameraLocation[1], cameraLocation[2], cameraTarget[0], cameraTarget[1], cameraTarget[2], 0, 1, 0);
        gl.glScalef(globalScale, globalScale, 1f);
        gl.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, modelMatrix);
        cameraVector.set(cameraTarget[0] - cameraLocation[0], cameraTarget[1] - cameraLocation[1], cameraTarget[2] - cameraLocation[2]);
        refreshDraggingMarker();
    }

    @Override
    public void display(GLAutoDrawable drawable) {
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

        gl.glClear(GL2.GL_COLOR_BUFFER_BIT);

        render3DScene(gl, GLU);
        scheduler.display(gl, GLU);
//        renderTestCube(gl);
    }

    @Override
    public void display() {
        drawable.display();
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        if (!resizing) {
            if (viewport.get(2) == width && viewport.get(3) == height) {
                return;
            }
            resizing = true;

            if (height == 0) {
                height = 1;
            }
            if (width == 0) {
                width = 1;
            }

            int viewportW, viewportH, viewportX, viewportY;

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
            GLU.gluPerspective(viewField, aspectRatio, nearDistance, farDistance);
            gl.glGetFloatv(GL2.GL_PROJECTION_MATRIX, projMatrix);//Update projection buffer

            gl.glMatrixMode(GL2.GL_MODELVIEW);
            gl.glLoadIdentity();

            setCameraPosition(gl, GLU);
            reshape3DScene(drawable.getGL().getGL2());

            if (showGLLog) {
                showGLLog = false;
                Logger logger = Logger.getLogger("");
                logger.log(Level.INFO, "GL_VENDOR: {0}", gl.glGetString(GL2.GL_VENDOR));
                logger.log(Level.INFO, "GL_RENDERER: {0}", gl.glGetString(GL2.GL_RENDERER));
                logger.log(Level.INFO, "GL_VERSION: {0}", gl.glGetString(GL2.GL_VERSION));
                logger.log(Level.INFO, "GL_SURFACE_SCALE: {0}", globalScale);
            }

            resizing = false;
            
        }
    }

    @Override
    public void destroy() {
        if (graphMouseAdapterNewt != null) {
            window.removeMouseListener(graphMouseAdapterNewt);
        } else if (graphMouseAdapterCanvas != null) {
            graphComponent.removeMouseListener(graphMouseAdapterCanvas);
        }
        graphMouseAdapter = null;
        drawable.destroy();
    }

    // TEST CUBE CODE BEGIN
    private static float rotateFactor = 15f;

    public void renderTestCube(GL2 gl) {
        float cubeSize = 1f;

        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
        GLU.gluLookAt(cameraLocation[0], cameraLocation[1], cameraLocation[2], cameraTarget[0], cameraTarget[1], cameraTarget[2], 0, 1, 0);

        gl.glColor3f(0f, 0f, 0f);

        gl.glRotatef(rotateFactor++ % 360f, 0.0f, 1.0f, 0.0f);	// Rotate The cube around the Y axis
        gl.glRotatef(15.0f, 1.0f, 1.0f, 1.0f);

        gl.glBegin(GL2.GL_QUADS);		// Draw The Cube Using quads
        gl.glColor3f(0.0f, 1.0f, 0.0f);	// Color Green
        gl.glVertex3f(cubeSize, cubeSize, -cubeSize);	// Top Right Of The Quad (Top)
        gl.glVertex3f(-cubeSize, cubeSize, -cubeSize);	// Top Left Of The Quad (Top)
        gl.glVertex3f(-cubeSize, cubeSize, cubeSize);	// Bottom Left Of The Quad (Top)
        gl.glVertex3f(cubeSize, cubeSize, cubeSize);	// Bottom Right Of The Quad (Top)
        gl.glColor3f(1.0f, 0.5f, 0.0f);	// Color Orange
        gl.glVertex3f(cubeSize, -cubeSize, cubeSize);	// Top Right Of The Quad (Bottom)
        gl.glVertex3f(-cubeSize, -cubeSize, cubeSize);	// Top Left Of The Quad (Bottom)
        gl.glVertex3f(-cubeSize, -cubeSize, -cubeSize);	// Bottom Left Of The Quad (Bottom)
        gl.glVertex3f(cubeSize, -cubeSize, -cubeSize);	// Bottom Right Of The Quad (Bottom)
        gl.glColor3f(1.0f, 0.0f, 0.0f);	// Color Red
        gl.glVertex3f(cubeSize, cubeSize, cubeSize);	// Top Right Of The Quad (Front)
        gl.glVertex3f(-cubeSize, cubeSize, cubeSize);	// Top Left Of The Quad (Front)
        gl.glVertex3f(-cubeSize, -cubeSize, cubeSize);	// Bottom Left Of The Quad (Front)
        gl.glVertex3f(cubeSize, -cubeSize, cubeSize);	// Bottom Right Of The Quad (Front)
        gl.glColor3f(1.0f, 1.0f, 0.0f);	// Color Yellow
        gl.glVertex3f(cubeSize, -cubeSize, -cubeSize);	// Top Right Of The Quad (Back)
        gl.glVertex3f(-cubeSize, -cubeSize, -cubeSize);	// Top Left Of The Quad (Back)
        gl.glVertex3f(-cubeSize, cubeSize, -cubeSize);	// Bottom Left Of The Quad (Back)
        gl.glVertex3f(cubeSize, cubeSize, -cubeSize);	// Bottom Right Of The Quad (Back)
        gl.glColor3f(0.0f, 0.0f, 1.0f);	// Color Blue
        gl.glVertex3f(-cubeSize, cubeSize, cubeSize);	// Top Right Of The Quad (Left)
        gl.glVertex3f(-cubeSize, cubeSize, -cubeSize);	// Top Left Of The Quad (Left)
        gl.glVertex3f(-cubeSize, -cubeSize, -cubeSize);	// Bottom Left Of The Quad (Left)
        gl.glVertex3f(-cubeSize, -cubeSize, cubeSize);	// Bottom Right Of The Quad (Left)
        gl.glColor3f(1.0f, 0.0f, 1.0f);	// Color Violet
        gl.glVertex3f(cubeSize, cubeSize, -cubeSize);	// Top Right Of The Quad (Right)
        gl.glVertex3f(cubeSize, cubeSize, cubeSize);	// Top Left Of The Quad (Right)
        gl.glVertex3f(cubeSize, -cubeSize, cubeSize);	// Bottom Left Of The Quad (Right)
        gl.glVertex3f(cubeSize, -cubeSize, -cubeSize);	// Bottom Right Of The Quad (Right)
        gl.glEnd();			// End Drawing The Cube
    }

    // TEST CUBE CODE END
    //Utils
    @Override
    public double[] myGluProject(float x, float y) {
        return myGluProject(x, y);
    }

    @Override
    public float[] myGluProject(float x, float y, float z) {
        float[] res = new float[2];

        float o0 = modelMatrix.get(0) * x + modelMatrix.get(4) * y + modelMatrix.get(8) * z + modelMatrix.get(12) * 1f;
        float o1 = modelMatrix.get(1) * x + modelMatrix.get(5) * y + modelMatrix.get(9) * z + modelMatrix.get(13) * 1f;
        float o2 = modelMatrix.get(2) * x + modelMatrix.get(6) * y + modelMatrix.get(10) * z + modelMatrix.get(14) * 1f;
        float o3 = modelMatrix.get(3) * x + modelMatrix.get(7) * y + modelMatrix.get(11) * z + modelMatrix.get(15) * 1f;

        float p0 = projMatrix.get(0) * o0 + projMatrix.get(4) * o1 + projMatrix.get(8) * o2 + projMatrix.get(12) * o3;
        float p1 = projMatrix.get(1) * o0 + projMatrix.get(5) * o1 + projMatrix.get(9) * o2 + projMatrix.get(13) * o3;
        float p2 = projMatrix.get(2) * o0 + projMatrix.get(6) * o1 + projMatrix.get(10) * o2 + projMatrix.get(14) * o3;
        float p3 = projMatrix.get(3) * o0 + projMatrix.get(7) * o1 + projMatrix.get(11) * o2 + projMatrix.get(15) * o3;
        p0 /= p3;
        p1 /= p3;
        p2 /= p3;

        res[0] = viewport.get(0) + (p0 + 1) * viewport.get(2) / 2;
        res[1] = viewport.get(1) + viewport.get(3) * (p1 + 1) / 2;

        return res;
    }

    private double[] transformVect(double[] in, DoubleBuffer m) {
        double[] out = new double[4];

        out[0] = m.get(0) * in[0] + m.get(4) * in[1] + m.get(8) * in[2] + m.get(12) * in[3];
        out[1] = m.get(1) * in[0] + m.get(5) * in[1] + m.get(9) * in[2] + m.get(13) * in[3];
        out[2] = m.get(2) * in[0] + m.get(6) * in[1] + m.get(10) * in[2] + m.get(14) * in[3];
        out[3] = m.get(3) * in[0] + m.get(7) * in[1] + m.get(11) * in[2] + m.get(15) * in[3];

        return out;
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

    @Override
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

    @Override
    public float[] getCameraLocation() {
        return cameraLocation;
    }

    @Override
    public void setCameraLocation(float[] cameraLocation) {
        this.cameraLocation = cameraLocation;
    }

    @Override
    public float[] getCameraTarget() {
        return cameraTarget;
    }

    @Override
    public void setCameraTarget(float[] cameraTarget) {
        this.cameraTarget = cameraTarget;
    }

    @Override
    public Component getGraphComponent() {
        return graphComponent;
    }

    @Override
    public Vec3f getCameraVector() {
        return cameraVector;
    }

    @Override
    public int getViewportHeight() {
        return viewport.get(3);
    }

    @Override
    public int getViewportWidth() {
        return viewport.get(2);
    }

    @Override
    public double getDraggingMarkerX() {
        return draggingMarker[0];
    }

    @Override
    public double getDraggingMarkerY() {
        return draggingMarker[1];
    }

    @Override
    public FloatBuffer getProjectionMatrix() {
        return projMatrix;
    }

    public FloatBuffer getModelMatrix() {
        return modelMatrix;
    }

    @Override
    public IntBuffer getViewport() {
        return viewport;
    }

    @Override
    public float getGlobalScale() {
        return globalScale;
    }

    @Override
    public void dispose(GLAutoDrawable glad) {
        engine.stopDisplay();
        VizController.getInstance().getDataBridge().reset();
    }

    @Override
    public Point getLocationOnScreen() {
        return graphComponent.getLocationOnScreen();
    }

    private class GraphMouseAdapter {

        final boolean pause = vizController.getVizConfig().isPauseLoopWhenMouseOut();
        final int minVal = vizController.getVizConfig().getReduceFpsWhenMouseOutValue();
        final int maxVal = 30;
        private float lastTarget = 0.1f;

        private void mouseEntered() {
            if (pause) {
                engine.resumeDisplay();
            } else {
                if (!scheduler.isAnimating()) {
                    engine.resumeDisplay();
                }
                scheduler.setFps(maxVal);
                resetFpsAverage();
            }
        }

        private void mouseExited() {
            if (pause) {
                engine.pauseDisplay();
            } else {
                float fps = getFpsAverage();
                float target = (float) (fps / (1. / Math.sqrt(getFpsAverage()) * 10.));
                if (fps == 0f) {
                    target = lastTarget;
                }
                if (target <= 0.005f) {
                    engine.pauseDisplay();
                } else if (target > minVal) {
                    target = minVal;
                }
                lastTarget = target;
                scheduler.setFps(target);
            }
        }
    }
}
