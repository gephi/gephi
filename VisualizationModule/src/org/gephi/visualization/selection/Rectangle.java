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
package org.gephi.visualization.selection;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import org.gephi.graph.api.Renderable;
import org.gephi.visualization.VizController;
import org.gephi.visualization.api.GraphDrawable;
import org.gephi.visualization.api.ModelImpl;
import org.gephi.visualization.api.selection.SelectionArea;
import org.gephi.visualization.gleem.linalg.Vecf;

/**
 *
 * @author Mathieu Bastian
 */
public class Rectangle implements SelectionArea {

    private float[] startPosition;
    private float[] rectangle = new float[2];
    private GraphDrawable drawable;

    public Rectangle() {
        drawable = VizController.getInstance().getDrawable();
    }

    public float[] getSelectionAreaRectancle() {
        return rectangle;
    }

    public boolean mouseTest(Vecf distanceFromMouse, ModelImpl object) {
        float rectangleWidth = rectangle[0];
        float rectangleHeight = rectangle[1];
        if (distanceFromMouse.get(0) < 0 && distanceFromMouse.get(0) > -rectangleWidth &&
                distanceFromMouse.get(1) > 0 && distanceFromMouse.get(1) < rectangleHeight) {
            return true;
        }
        return false;
    }

    public boolean select(Renderable object) {
        return true;
    }

    public boolean unselect(Renderable object) {
        return true;
    }

    public void start(float[] mousePosition) {
        this.startPosition = mousePosition;
        this.rectangle[0] = 0f;
        this.rectangle[1] = 0f;
    }

    public void stop() {
        this.startPosition = null;
    }

    public void setMousePosition(float[] mousePosition) {
        rectangle[0] = mousePosition[0] - startPosition[0];
        rectangle[1] = mousePosition[1] - startPosition[1];
    }

    public void drawArea(GL gl, GLU glu) {
        if (startPosition != null) {
            float x = startPosition[0];
            float y = startPosition[1];
            float w = rectangle[0];
            float h = rectangle[1];

            gl.glMatrixMode(GL.GL_PROJECTION);
            gl.glPushMatrix();
            gl.glLoadIdentity();
            glu.gluOrtho2D(0, drawable.getViewportWidth(), 0, drawable.getViewportHeight());
            gl.glScalef(1, -1, 1);
            gl.glTranslatef(0, -drawable.getViewportHeight(), 0);
            gl.glMatrixMode(GL.GL_MODELVIEW);

            gl.glColor3f(0, 0, 1);
            gl.glVertex3f(x + w, y, 0);
            gl.glVertex3f(x, y, 0);
            gl.glVertex3f(x, y + h, 0);
            gl.glVertex3f(x + w, y + h, 0);

            gl.glMatrixMode(GL.GL_PROJECTION);
            gl.glPopMatrix();
            gl.glMatrixMode(GL.GL_MODELVIEW);
        }
    }
}
