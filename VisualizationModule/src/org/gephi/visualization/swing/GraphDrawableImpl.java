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

import com.sun.opengl.util.BufferUtil;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.glu.GLU;
import org.gephi.visualization.VizArchitecture;
import org.gephi.visualization.VizController;
import org.gephi.visualization.apiimpl.GraphDrawable;
import org.gephi.visualization.opengl.AbstractEngine;
import org.gephi.lib.gleem.linalg.Vec3f;
import org.gephi.visualization.apiimpl.Scheduler;

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
    protected void init(GL gl) {
        //System.out.println("init");
        graphComponent.setCursor(Cursor.getDefaultCursor());
        engine.initEngine(gl, glu);
    }
    
    public void destroy() {
        if(graphMouseAdapter != null) {
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

        double[] v = {0, 0, 0, 1.0};
        double[] v2 = {1.0, 1.0, 0, 1.0};

        double[] d = myGluProject(v);
        double[] d2 = myGluProject(v2);

        draggingMarker[0] = d[0] - d2[0];
        draggingMarker[1] = d[1] - d2[1];

    }

    @Override
    public void setCameraPosition(GL gl, GLU glu) {

        //Refresh rotation angle
        gl.glLoadIdentity();
        glu.gluLookAt(cameraLocation[0], cameraLocation[1], cameraLocation[2], cameraTarget[0], cameraTarget[1], cameraTarget[2], 0, 1, 0);
        gl.glGetDoublev(GL.GL_MODELVIEW_MATRIX, modelMatrix);
        cameraVector.set(cameraTarget[0] - cameraLocation[0], cameraTarget[1] - cameraLocation[1], cameraTarget[2] - cameraLocation[2]);
        refreshDraggingMarker();
    }

    @Override
    protected void reshape3DScene(GL gl) {
        setCameraPosition(gl, glu);
        graphComponent.invalidate();    //Force canvas to be laid out with the proper size
    }

    @Override
    protected void render3DScene(GL gl, GLU glu) {

        scheduler.display(gl, glu);
        //renderTestCube(gl);
    }

    private void renderTestCube(GL gl) {
        float cubeSize = 100f;

        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();
        glu.gluLookAt(cameraLocation[0], cameraLocation[1], cameraLocation[2], cameraTarget[0], cameraTarget[1], cameraTarget[2], 0, 1, 0);

        gl.glColor3f(0f, 0f, 0f);

        gl.glRotatef(15.0f, 0.0f, 1.0f, 0.0f);	// Rotate The cube around the Y axis
        gl.glRotatef(15.0f, 1.0f, 1.0f, 1.0f);

        gl.glBegin(GL.GL_QUADS);		// Draw The Cube Using quads
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
        GL gl = drawable.getGL();
        if (vizController.getVizModel().isUse3d()) {
            gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        } else {
            gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        }
        setCameraPosition(gl, glu);
        engine.display(gl, glu);
    }

    public void display() {
        drawable.display();
    }

    //Utils
    public double[] myGluProject(float x, float y) {
        return myGluProject(new double[]{x, y, 0, 1.0});
    }

    public double[] myGluProject(float x, float y, float z) {
        return myGluProject(new double[]{x, y, z, 1.0});
    }

    public double[] myGluProject(double[] in) {
        double[] res = new double[2];

        double[] out = transformVect(in, modelMatrix);
        double[] out2 = transformVect(out, projMatrix);
        out2[0] /= out2[3];
        out2[1] /= out2[3];
        out2[2] /= out2[3];
        res[0] = viewport.get(0) + (out2[0] + 1) * viewport.get(2) / 2;
        res[1] = viewport.get(1) + viewport.get(3) * (out2[1] + 1) / 2;

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

    public double[] gluUnProject(float x, float y, float z) {
        DoubleBuffer buffer = BufferUtil.newDoubleBuffer(3);
        glu.gluUnProject(x, y, z, modelMatrix, projMatrix, viewport, buffer);
        return new double[]{buffer.get(0), buffer.get(1), buffer.get(2)};
    }

    public float[] getCameraLocation() {
        return cameraLocation;
    }

    public void setCameraLocation(float[] cameraLocation) {
        this.cameraLocation = cameraLocation;
    }

    public float[] getCameraTarget() {
        return cameraTarget;
    }

    public void setCameraTarget(float[] cameraTarget) {
        this.cameraTarget = cameraTarget;
    }

    public Component getGraphComponent() {
        return graphComponent;
    }

    public Vec3f getCameraVector() {
        return cameraVector;
    }

    public int getViewportHeight() {
        return viewport.get(3);
    }

    public int getViewportWidth() {
        return viewport.get(2);
    }

    public double getDraggingMarkerX() {
        return draggingMarker[0];
    }

    public double getDraggingMarkerY() {
        return draggingMarker[1];
    }

    public DoubleBuffer getProjectionMatrix() {
        return projMatrix;
    }

    public DoubleBuffer getModelMatrix() {
        return modelMatrix;
    }

    public IntBuffer getViewport() {
        return viewport;
    }
}
