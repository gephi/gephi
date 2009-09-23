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

import java.util.Arrays;
import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import org.gephi.graph.api.Renderable;
import org.gephi.visualization.VizController;
import org.gephi.visualization.api.GraphDrawable;
import org.gephi.visualization.api.ModelImpl;
import org.gephi.visualization.api.VizConfig;
import org.gephi.visualization.api.selection.SelectionArea;
import org.gephi.visualization.gleem.linalg.Vecf;

/**
 *
 * @author Mathieu Bastian
 */
public class Rectangle implements SelectionArea {

    private static float[] pointRect = {1, 1};
    private float[] startPosition;
    private float[] rectangle = new float[2];
    private float[] center = new float[2];
    private float[] rectangleSize = new float[2];
    private GraphDrawable drawable;
    private boolean stop = true;
    private VizConfig config;
    private boolean blocking = true;
    private boolean ctrl = false;

    public Rectangle() {
        drawable = VizController.getInstance().getDrawable();
        config = VizController.getInstance().getVizConfig();
    }

    public float[] getSelectionAreaRectancle() {
        if (stop) {
            return pointRect;
        }
        rectangleSize[0] = Math.abs(rectangle[0] - startPosition[0]);
        rectangleSize[1] = Math.abs(rectangle[1] - startPosition[1]);
        if (rectangleSize[0] < 1f) {
            rectangleSize[0] = 1f;
        }
        if (rectangleSize[1] < 1f) {
            rectangleSize[1] = 1f;
        }
        return rectangleSize;
    }

    public float[] getSelectionAreaCenter() {
        if (stop) {
            return null;
        }
        center[0] = -(rectangle[0] - startPosition[0]) / 2f;
        center[1] = -(rectangle[1] - startPosition[1]) / 2f;
        return center;
    }

    public boolean mouseTest(Vecf distanceFromMouse, ModelImpl object) {
        if (stop) {
            return object.selectionTest(distanceFromMouse, 0);
        }
        float x = object.getViewportX();
        float y = object.getViewportY();
        float rad = object.getViewportRadius();
        //System.out.println(rectangle[0]+"   "+rectangle[1]);
        boolean res = true;
        if (startPosition[0] > rectangle[0]) {
            if (x - rad > startPosition[0] || x + rad < rectangle[0]) {
                res = false;
            }
        } else {
            if (x + rad < startPosition[0] || x - rad > rectangle[0]) {
                res = false;
            }
        }
        if (startPosition[1] < rectangle[1]) {
            if (y + rad < startPosition[1] || y - rad > rectangle[1]) {
                res = false;
            }
        } else {
            if (y - rad > startPosition[1] || y + rad < rectangle[1]) {
                res = false;
            }
        }
        return res;
    }

    public boolean select(Renderable object) {
        return true;
    }

    public boolean unselect(Renderable object) {
        return true;
    }

    public void start(float[] mousePosition) {
        this.startPosition = Arrays.copyOf(mousePosition, 2);
        this.rectangle[0] = startPosition[0];
        this.rectangle[1] = startPosition[1];
        stop = false;
        blocking = false;
    }

    public void stop() {
        stop = true;
        blocking = true;
    }

    public void setMousePosition(float[] mousePosition) {
        if (!stop) {
            rectangle[0] = mousePosition[0];
            rectangle[1] = mousePosition[1];
        }
    }

    public boolean isEnabled() {
        return true;
    }

    public boolean blockSelection() {
        return blocking;
    }

    public void setBlocking(boolean blocking) {
        this.blocking = blocking;
    }

    public void drawArea(GL gl, GLU glu) {
        if (!stop) {
            float x = startPosition[0];
            float y = startPosition[1];
            float w = rectangle[0] - startPosition[0];
            float h = rectangle[1] - startPosition[1];
            //System.out.println("x:"+x+"  y:"+y+"   w:"+w+"   h:"+h);

            gl.glMatrixMode(GL.GL_PROJECTION);
            gl.glPushMatrix();
            gl.glLoadIdentity();
            glu.gluOrtho2D(0, drawable.getViewportWidth(), 0, drawable.getViewportHeight());
            gl.glMatrixMode(GL.GL_MODELVIEW);
            gl.glPushMatrix();
            gl.glLoadIdentity();

            float[] color = config.getRectangleSelectionColor();
            gl.glColor4f(color[0], color[1], color[2], color[3]);

            gl.glBegin(GL.GL_QUADS);
            gl.glVertex3f(x + w, y, 0);
            gl.glVertex3f(x, y, 0);
            gl.glVertex3f(x, y + h, 0);
            gl.glVertex3f(x + w, y + h, 0);
            gl.glEnd();

            gl.glColor4f(color[0], color[1], color[2], 1f);
            gl.glBegin(GL.GL_LINE_LOOP);
            gl.glVertex3f(x + w, y, 0);
            gl.glVertex3f(x, y, 0);
            gl.glVertex3f(x, y + h, 0);
            gl.glVertex3f(x + w, y + h, 0);
            gl.glEnd();

            gl.glPopMatrix();
            gl.glMatrixMode(GL.GL_PROJECTION);
            gl.glPopMatrix();
            gl.glMatrixMode(GL.GL_MODELVIEW);
        } else {
            startPosition = null;
        }
    }

    public boolean isStop() {
        return stop;
    }

    public void setCtrl(boolean ctrl) {
        this.ctrl = ctrl;
    }

    public boolean isCtrl() {
        return ctrl;
    }
}
