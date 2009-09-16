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
import org.gephi.visualization.opengl.compatibility.objects.NodeDiskModel;
import org.gephi.visualization.opengl.text.TextManager;

/**
 *
 * @author Mathieu Bastian
 */
public class CompatibilityNodeDiskModeler implements CompatibilityNodeModeler {

    public int SHAPE_DIAMOND;
    public int SHAPE_DISK16;
    public int SHAPE_DISK32;
    public int BORDER16;
    public int BORDER32;
    private CompatibilityEngine engine;
    protected VizConfig config;
    protected TextManager textManager;

    public CompatibilityNodeDiskModeler(AbstractEngine engine) {
        this.engine = (CompatibilityEngine) engine;
        this.config = VizController.getInstance().getVizConfig();
        this.textManager = VizController.getInstance().getTextManager();
    }

    @Override
    public ModelImpl initModel(Renderable n) {
        NodeData nd = (NodeData) n;
        NodeDiskModel obj = new NodeDiskModel();
        obj.setObj(nd);
        obj.setSelected(false);
        obj.setConfig(config);
        obj.setDragDistanceFromMouse(new float[2]);
        n.setModel(obj);

        if(n.getTextData()==null) {
            n.setTextData(textManager.newTextData(nd));
        }

        chooseModel(obj);
        return obj;
    }

    @Override
    public void chooseModel(ModelImpl object3d) {
        NodeDiskModel obj = (NodeDiskModel) object3d;

        float distance = engine.cameraDistance(object3d) / object3d.getObj().getRadius();
        if (distance > 600) {
            obj.modelType = SHAPE_DIAMOND;
            obj.modelBorderType = -1;
        } else if (distance > 50) {
            obj.modelType = SHAPE_DISK16;
            obj.modelBorderType = BORDER16;
        } else {
            obj.modelType = SHAPE_DISK32;
            obj.modelBorderType = BORDER32;
        }
    }

    @Override
    public int initDisplayLists(GL gl, GLU glu, GLUquadric quadric, int ptr) {
        // Diamond display list
        SHAPE_DIAMOND = ptr + 1;
        gl.glNewList(SHAPE_DIAMOND, GL.GL_COMPILE);
        glu.gluDisk(quadric, 0, 0.5, 4, 1);
        gl.glEndList();
        //End

        //Disk16
        SHAPE_DISK16 = SHAPE_DIAMOND + 1;
        gl.glNewList(SHAPE_DISK16, GL.GL_COMPILE);
        glu.gluDisk(quadric, 0, 0.5, 6, 1);
        gl.glEndList();
        //Fin

        //Disk32
        SHAPE_DISK32 = SHAPE_DISK16 + 1;
        gl.glNewList(SHAPE_DISK32, GL.GL_COMPILE);
        glu.gluDisk(quadric, 0, 0.5, 12, 2);
        gl.glEndList();


        //Border16
        BORDER16 = SHAPE_DISK32 + 1;
        gl.glNewList(BORDER16, GL.GL_COMPILE);
        glu.gluDisk(quadric, 0.42, 0.50, 24, 2);
        gl.glEndList();

        //Border32
        BORDER32 = BORDER16 + 1;
        gl.glNewList(BORDER32, GL.GL_COMPILE);
        glu.gluDisk(quadric, 0.42, 0.50, 48, 2);
        gl.glEndList();

        return BORDER32;
    }

    public void beforeDisplay(GL gl, GLU glu) {
    }

    public void afterDisplay(GL gl, GLU glu) {
    }

    @Override
    public void initFromOpenGLThread() {
    }

    @Override
    public JPanel getPanel() {
        return null;
    }

    public String getName() {
        return "Disk 2d";
    }

    @Override
    public String toString() {
        return getName();
    }

    public boolean is3d() {
        return false;
    }
}
