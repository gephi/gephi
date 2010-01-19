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
import org.gephi.visualization.VizModel;
import org.gephi.visualization.api.GraphDrawable;
import org.gephi.visualization.api.GraphIO;
import org.gephi.visualization.api.ModelImpl;
import org.gephi.visualization.api.selection.SelectionArea;
import org.gephi.visualization.api.selection.SelectionManager;
import org.gephi.lib.gleem.linalg.Vecf;

/**
 *
 * @author Mathieu Bastian
 */
public class Cylinder implements SelectionArea {

    //Architecture
    private GraphIO graphIO;
    private GraphDrawable drawable;
    private SelectionManager selectionManager;
    private VizModel vizModel;

    //Variables
    private static final float[] rectPoint = {1, 1};
    private float[] rectangle = new float[2];

    public Cylinder() {
        graphIO = VizController.getInstance().getGraphIO();
        drawable = VizController.getInstance().getDrawable();
        selectionManager = VizController.getInstance().getSelectionManager();
        vizModel = VizController.getInstance().getVizModel();
    }

    public float[] getSelectionAreaRectancle() {
        float diameter = selectionManager.getMouseSelectionDiameter();
        if (diameter == 1) {
            //Point
            return rectPoint;
        } else {
            float size;
            if (selectionManager.isMouseSelectionZoomProportionnal()) {
                size = diameter * (float) Math.abs(drawable.getDraggingMarkerX());
            } else {
                size = diameter;
            }
            rectangle[0] = size;
            rectangle[1] = size;
            return rectangle;
        }
    }

    public float[] getSelectionAreaCenter() {
        return null;
    }

    public boolean mouseTest(Vecf distanceFromMouse, ModelImpl object) {
        float diameter = selectionManager.getMouseSelectionDiameter();
        if (diameter == 1) {
            //Point
            return object.selectionTest(distanceFromMouse, 0);
        } else {
            if (selectionManager.isMouseSelectionZoomProportionnal()) {
                return object.selectionTest(distanceFromMouse, diameter * (float) Math.abs(drawable.getDraggingMarkerX()));
            } else {
                return object.selectionTest(distanceFromMouse, diameter);
            }
        }
    }

    public boolean select(Renderable object) {
        return true;
    }

    public boolean unselect(Renderable object) {
        return true;
    }

    public void drawArea(GL gl, GLU glu) {
        float diameter = selectionManager.getMouseSelectionDiameter();
        if (diameter == 1) {
            //Point
        } else {
            //Cylinder
            float radius;
            boolean lighting = vizModel.isLighting();
            if (selectionManager.isMouseSelectionZoomProportionnal()) {
                radius = (float) (diameter * Math.abs(drawable.getDraggingMarkerX()));      //Proportionnal
            } else {
                radius = diameter;      //Constant
            }
            float[] mousePosition = graphIO.getMousePosition();
            float vectorX, vectorY, vectorX1 = mousePosition[0], vectorY1 = mousePosition[1];
            double angle;

            if (lighting) {
                gl.glDisable(GL.GL_LIGHTING);
            }
            gl.glColor4f(0f, 0f, 0f, 0.2f);
            gl.glBegin(GL.GL_TRIANGLES);
            for (int i = 0; i <= 360; i++) {
                angle = i / 57.29577957795135f;
                vectorX = mousePosition[0] + (radius * (float) Math.sin(angle));
                vectorY = mousePosition[1] + (radius * (float) Math.cos(angle));
                gl.glVertex2f(mousePosition[0], mousePosition[1]);
                gl.glVertex2f(vectorX1, vectorY1);
                gl.glVertex2f(vectorX, vectorY);
                vectorY1 = vectorY;
                vectorX1 = vectorX;
            }
            gl.glEnd();
            if (lighting) {
                gl.glEnable(GL.GL_LIGHTING);
            }
        }
    }

    public boolean isEnabled() {
        return selectionManager.isSelectionEnabled();
    }

    public boolean blockSelection() {
        return false;
    }
}
