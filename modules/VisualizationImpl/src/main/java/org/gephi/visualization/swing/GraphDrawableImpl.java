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

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.glu.GLU;
import org.gephi.lib.gleem.linalg.Vec3f;
import org.gephi.visualization.VizArchitecture;
import org.gephi.visualization.VizController;
import org.gephi.visualization.apiimpl.GraphDrawable;
import org.gephi.visualization.apiimpl.Scheduler;
import org.gephi.visualization.opengl.AbstractEngine;

/**
 *
 * @author Mathieu Bastian
 */
public class GraphDrawableImpl extends GLAbstractListener implements VizArchitecture, GraphDrawable {

    protected Component graphComponent;
    protected AbstractEngine engine;
    protected Scheduler scheduler;
    protected float[] cameraLocation;
    protected float[] cameraTarget;
    protected double[] draggingMarker = new double[2];//The drag mesure for a moving of 1 to the viewport
    protected Vec3f cameraVector = new Vec3f();
    protected MouseAdapter graphMouseAdapter;

    public GraphDrawableImpl() {
        super();
        this.vizController = VizController.getInstance();
    }

    @Override
    public void initArchitecture() {
        this.engine = VizController.getInstance().getEngine();
        this.scheduler = VizController.getInstance().getScheduler();
        this.screenshotMaker = VizController.getInstance().getScreenshotMaker();

        cameraLocation = vizController.getVizConfig().getDefaultCameraPosition();
        cameraTarget = vizController.getVizConfig().getDefaultCameraTarget();

        //Mouse events
        if (vizController.getVizConfig().isReduceFpsWhenMouseOut()) {
            final int minVal = vizController.getVizConfig().getReduceFpsWhenMouseOutValue();
            final int maxVal = 30;
            graphMouseAdapter = new MouseAdapter() {
                private float lastTarget = 0.1f;

                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!scheduler.isAnimating()) {
                        engine.startDisplay();
                    }
                    scheduler.setFps(maxVal);
                    resetFpsAverage();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    float fps = getFpsAverage();
                    float target = (float) (fps / (1. / Math.sqrt(getFpsAverage()) * 10.));
                    if (fps == 0f) {
                        target = lastTarget;
                    }
                    if (target <= 0.005f) {
                        engine.stopDisplay();
                    } else if (target > minVal) {
                        target = minVal;
                    }
                    lastTarget = target;
                    scheduler.setFps(target);
                }
            };
            graphComponent.addMouseListener(graphMouseAdapter);
        } else if (vizController.getVizConfig().isPauseLoopWhenMouseOut()) {
            graphMouseAdapter = new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    engine.startDisplay();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    engine.stopDisplay();
                }
            };
            graphComponent.addMouseListener(graphMouseAdapter);
        }
    }

    @Override
    protected void init(GL2 gl) {
        //System.out.println("init");
//        graphComponent.setCursor(Cursor.getDefaultCursor());
        engine.initEngine(gl, glu);
    }

    public void destroy() {
        if (graphMouseAdapter != null) {
            graphComponent.removeMouseListener(graphMouseAdapter);
        }
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
        gl.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, modelMatrix);
        cameraVector.set(cameraTarget[0] - cameraLocation[0], cameraTarget[1] - cameraLocation[1], cameraTarget[2] - cameraLocation[2]);
        refreshDraggingMarker();
    }

    @Override
    protected void reshape3DScene(GL2 gl) {
        setCameraPosition(gl, glu);
    }

    @Override
    protected void render3DScene(GL2 gl, GLU glu) {

        scheduler.display(gl, glu);
        //renderTestCube(gl);
    }

    private void renderTestCube(GL2 gl) {
        float cubeSize = 100f;

        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();
        glu.gluLookAt(cameraLocation[0], cameraLocation[1], cameraLocation[2], cameraTarget[0], cameraTarget[1], cameraTarget[2], 0, 1, 0);

        gl.glColor3f(0f, 0f, 0f);

        gl.glRotatef(15.0f, 0.0f, 1.0f, 0.0f);	// Rotate The cube around the Y axis
        gl.glRotatef(15.0f, 1.0f, 1.0f, 1.0f);

        gl.glBegin(GL2.GL_QUADS);		// Draw The Cube Using quads
        gl.glColor3f(0.0f, 1.0f, 0.0f);	// Color Blue
        gl.glVertex3f(cubeSize, cubeSize, -cubeSize);	// Top Right Of The Quad (Top)
        gl.glVertex3f(-cubeSize, cubeSize, -cubeSize);	// Top Left Of The Quad (Top)
        gl.glVertex3f(-cubeSize, cubeSize, cubeSize);	// Bottom Left Of The Quad (Top)
        gl.glVertex3f(cubeSize, cubeSize, 1.0f);	// Bottom Right Of The Quad (Top)
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

    public void renderScreenshot(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        if (vizController.getVizModel().isUse3d()) {
            gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        } else {
            gl.glClear(GL2.GL_COLOR_BUFFER_BIT);
        }
        setCameraPosition(gl, glu);
        engine.display(gl, glu);
    }

    public void display() {
        drawable.display();
    }

    //Utils
    public double[] myGluProject(float x, float y) {
        return myGluProject(x, y);
    }

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
    public void dispose(GLAutoDrawable glad) {
        /* FIXME: jbilcke: what should it do? is it new in JOGL2? */
    }
}
