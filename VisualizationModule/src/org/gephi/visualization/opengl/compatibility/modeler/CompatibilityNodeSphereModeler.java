/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
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
import org.gephi.visualization.api.initializer.CompatibilityNodeModeler;
import org.gephi.visualization.modeler.NodeSphereModeler;
import org.gephi.visualization.opengl.AbstractEngine;
import org.gephi.visualization.apiimpl.ModelImpl;
import org.gephi.visualization.apiimpl.VizConfig;
import org.gephi.visualization.opengl.compatibility.CompatibilityEngine;
import org.gephi.visualization.opengl.compatibility.objects.NodeSphereModel;

/**
 * Default initializer for the nodes. The class draw sphere objects and manage a LOD system.
 *
 * @author Mathieu Bastian
 * @see NodeSphereModel
 */
public class CompatibilityNodeSphereModeler extends NodeSphereModeler implements CompatibilityNodeModeler {

    public int SHAPE_DIAMOND;
    public int SHAPE_SPHERE16;
    public int SHAPE_SPHERE32;
    public int SHAPE_SPHERE64;
    public int SHAPE_BILLBOARD;
    private CompatibilityEngine engine;
    private VizConfig config;

    public CompatibilityNodeSphereModeler(AbstractEngine engine) {
        this.engine = (CompatibilityEngine) engine;
        this.config = VizController.getInstance().getVizConfig();
    }

    @Override
    public ModelImpl initModel(Renderable n) {
        NodeSphereModel obj = new NodeSphereModel();
        obj.setObj((NodeData) n);
        obj.setSelected(false);
        obj.setDragDistanceFromMouse(new float[2]);
        obj.modelType = SHAPE_SPHERE64;
        n.setModel(obj);

        chooseModel(obj);

        return obj;
    }

    @Override
    public void chooseModel(ModelImpl object3d) {
        NodeSphereModel obj = (NodeSphereModel) object3d;
        if (config.isDisableLOD()) {
            obj.modelType = SHAPE_SPHERE64;
            return;
        }

        float distance = engine.cameraDistance(object3d) / object3d.getObj().getRadius();
        if (distance > 600) {
            obj.modelType = SHAPE_DIAMOND;
        } else if (distance > 50) {
            obj.modelType = SHAPE_SPHERE16;
        } else {
            obj.modelType = SHAPE_SPHERE32;
        }
    }

    @Override
    public int initDisplayLists(GL gl, GLU glu, GLUquadric quadric, int ptr) {
        // Diamond display list
        SHAPE_DIAMOND = ptr + 1;
        gl.glNewList(SHAPE_DIAMOND, GL.GL_COMPILE);
        glu.gluSphere(quadric, 0.5f, 4, 2);
        gl.glEndList();
        //End


        // Sphere16 display list
        SHAPE_SPHERE16 = SHAPE_DIAMOND + 1;
        gl.glNewList(SHAPE_SPHERE16, GL.GL_COMPILE);
        gl.glCallList(ptr);
        glu.gluSphere(quadric, 0.5f, 16, 8);
        gl.glEndList();
        //Fin


        // Sphere32 display list
        SHAPE_SPHERE32 = SHAPE_SPHERE16 + 1;
        gl.glNewList(SHAPE_SPHERE32, GL.GL_COMPILE);
        gl.glCallList(ptr);
        glu.gluSphere(quadric, 0.5f, 32, 16);
        gl.glEndList();

        // Sphere32 display list
        SHAPE_SPHERE64 = SHAPE_SPHERE32 + 1;
        gl.glNewList(SHAPE_SPHERE64, GL.GL_COMPILE);
        gl.glCallList(ptr);
        glu.gluSphere(quadric, 0.5f, 64, 32);
        gl.glEndList();

        return SHAPE_SPHERE64;
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

    public boolean is3d() {
        return true;
    }

    @Override
    public String getName() {
        return "Sphere 3d";
    }
}
