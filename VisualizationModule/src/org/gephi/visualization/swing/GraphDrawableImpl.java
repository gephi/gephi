/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.visualization.swing;

import com.sun.opengl.util.BufferUtil;
import java.awt.Component;
import java.awt.Cursor;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.glu.GLU;
import org.gephi.visualization.VizArchitecture;
import org.gephi.visualization.VizController;
import org.gephi.visualization.api.GraphDrawable;
import org.gephi.visualization.opengl.AbstractEngine;
import org.gephi.visualization.gleem.linalg.Vec3f;
import org.gephi.visualization.api.Scheduler;

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
    }

    @Override
    protected void init(GL gl) {
        System.out.println("init");
        graphComponent.setCursor(Cursor.getDefaultCursor());
        engine.initEngine(gl, glu);
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
