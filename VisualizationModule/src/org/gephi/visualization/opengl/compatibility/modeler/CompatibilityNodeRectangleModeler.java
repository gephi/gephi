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
package org.gephi.visualization.opengl.compatibility.modeler;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;
import javax.swing.JPanel;
import org.gephi.graph.api.NodeData;
import org.gephi.graph.api.Renderable;
import org.gephi.visualization.VizController;
import org.gephi.visualization.api.ModelImpl;
import org.gephi.visualization.api.VizConfig;
import org.gephi.visualization.api.initializer.CompatibilityNodeModeler;
import org.gephi.visualization.opengl.AbstractEngine;
import org.gephi.visualization.opengl.compatibility.CompatibilityEngine;
import org.gephi.visualization.opengl.compatibility.objects.NodeRectangeModel;

/**
 *
 * @author Mathieu Bastian
 */
public class CompatibilityNodeRectangleModeler implements CompatibilityNodeModeler {

    private CompatibilityEngine engine;
    protected VizConfig config;
    public int SHAPE_RECTANGLE;
    public int SHAPE_RECTANGLE_BORDER;
    public int SHAPE_RECTANGLE_WITH_BORDER;

    public CompatibilityNodeRectangleModeler(AbstractEngine engine) {
        this.engine = (CompatibilityEngine) engine;
        this.config = VizController.getInstance().getVizConfig();
    }

    @Override
    public ModelImpl initModel(Renderable n) {
        NodeRectangeModel obj = new NodeRectangeModel();
        obj.setObj((NodeData) n);
        obj.setSelected(false);
        obj.setConfig(config);
        obj.setDragDistanceFromMouse(new float[2]);
        n.setModel(obj);

        chooseModel(obj);
        return obj;
    }

    @Override
    public void chooseModel(ModelImpl object3d) {
        NodeRectangeModel obj = (NodeRectangeModel) object3d;

        float distance = engine.cameraDistance(object3d) / object3d.getObj().getRadius();
        if (distance > 600) {
            obj.modelType = SHAPE_RECTANGLE;
            obj.modelBorderType = 0;
        } else {
            obj.modelType = SHAPE_RECTANGLE_WITH_BORDER;
            obj.modelBorderType = SHAPE_RECTANGLE_BORDER;
        }
    }

    @Override
    public int initDisplayLists(GL gl, GLU glu, GLUquadric quadric, int ptr) {

        SHAPE_RECTANGLE = ptr + 1;
        gl.glNewList(SHAPE_RECTANGLE, GL.GL_COMPILE);
        gl.glBegin(GL.GL_TRIANGLE_STRIP);
        gl.glVertex3f(0.5f, 0.5f, 0);
        gl.glVertex3f(-0.5f, 0.5f, 0);
        gl.glVertex3f(0.5f, -0.5f, 0);
        gl.glVertex3f(-0.5f, -0.5f, 0);
        gl.glEnd();
        gl.glEndList();

        SHAPE_RECTANGLE_WITH_BORDER = SHAPE_RECTANGLE + 1;
        gl.glNewList(SHAPE_RECTANGLE_WITH_BORDER, GL.GL_COMPILE);
        gl.glBegin(GL.GL_TRIANGLE_STRIP);
        gl.glVertex3f(0.4f, 0.4f, 0);
        gl.glVertex3f(-0.4f, 0.4f, 0);
        gl.glVertex3f(0.4f, -0.4f, 0);
        gl.glVertex3f(-0.4f, -0.4f, 0);
        gl.glEnd();
        gl.glEndList();

        SHAPE_RECTANGLE_BORDER = SHAPE_RECTANGLE_WITH_BORDER + 1;
        gl.glNewList(SHAPE_RECTANGLE_BORDER, GL.GL_COMPILE);
        gl.glBegin(GL.GL_TRIANGLE_STRIP);
        gl.glVertex3f(0.5f, 0.5f, 0);
        gl.glVertex3f(-0.5f, 0.5f, 0);
        gl.glVertex3f(0.5f, -0.5f, 0);
        gl.glVertex3f(-0.5f, -0.5f, 0);
        gl.glEnd();
        gl.glEndList();


        return SHAPE_RECTANGLE_BORDER;
    }

    @Override
    public void initFromOpenGLThread() {
    }

    @Override
    public JPanel getPanel() {
        return null;
    }

    public String getName() {
        return "nodeModeler_Rectangle_name";
    }
}
