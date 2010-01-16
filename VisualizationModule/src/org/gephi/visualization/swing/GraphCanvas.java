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

import com.sun.opengl.util.GLUT;
import java.awt.Component;

import java.awt.Cursor;
import java.awt.Dimension;
import java.text.NumberFormat;
import javax.media.opengl.GL;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.glu.GLU;
import javax.swing.JPopupMenu;
import javax.swing.ToolTipManager;

/**
 *
 * @author Mathieu Bastian
 */
public class GraphCanvas extends GraphDrawableImpl {

    private GLCanvas glCanvas;
    private GLUT glut = new GLUT();
    private NumberFormat formatter;

    public GraphCanvas() {
        super();
        formatter = NumberFormat.getNumberInstance();
        formatter.setMaximumFractionDigits(1);
        glCanvas = new GLCanvas(getCaps());
        super.initDrawable(glCanvas);
        glCanvas.setMinimumSize(new Dimension(0, 0));   //Fix Canvas resize Issue

        //Basic init
        graphComponent = (Component) glCanvas;
        graphComponent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        //False lets the components appear on top of the canvas
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
    }

    @Override
    protected void render3DScene(GL gl, GLU glu) {
        if (vizController.getVizConfig().isShowFPS()) {
            gl.glPushMatrix();
            gl.glLoadIdentity();
            gl.glMatrixMode(gl.GL_PROJECTION);
            gl.glPushMatrix();
            gl.glLoadIdentity();

            gl.glGetIntegerv(gl.GL_VIEWPORT, viewport);
            glu.gluOrtho2D(0, viewport.get(2), viewport.get(3), 0);
            gl.glDepthFunc(gl.GL_ALWAYS);
            gl.glColor3i(192, 192, 192);
            gl.glRasterPos2f(10, 15);
            String fpsRound = formatter.format(fps);
            glut.glutBitmapString(GLUT.BITMAP_HELVETICA_10, fpsRound);

            gl.glDepthFunc(gl.GL_LESS);
            gl.glPopMatrix();
            gl.glMatrixMode(gl.GL_MODELVIEW);
            gl.glPopMatrix();
        }
        super.render3DScene(gl, glu);
    }
}
