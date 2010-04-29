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
import org.gephi.visualization.apiimpl.ModelImpl;
import org.gephi.visualization.api.initializer.CompatibilityModeler;

/**
 *
 * @author Mathieu Bastian
 */
public class CompatibilityPotatoModeler implements CompatibilityModeler<NodeData> {

    public int DISK_LOW;
    public int DISK_HIGH;

    public ModelImpl initModel(Renderable n) {
        /*Potato potato = (Potato)n;

        Potato3dObject obj = new Potato3dObject(potato);
        obj.modelType = DISK_HIGH;
        obj.setObj(potato);
        potato.setObject3d(obj);*/

        return null;
    }

    public void chooseModel(ModelImpl<NodeData> obj) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int initDisplayLists(GL gl, GLU glu, GLUquadric quadric, int ptr) {

        //Low res disk
        DISK_LOW = ptr + 1;
        gl.glNewList(DISK_LOW, GL.GL_COMPILE);
        gl.glDisable(GL.GL_LIGHTING);
        glu.gluDisk(quadric, 0.0, 1.0, 8, 1);
        gl.glEnable(GL.GL_LIGHTING);
        gl.glEndList();
        //End

        //High res disk
        DISK_HIGH = DISK_LOW + 1;
        gl.glNewList(DISK_HIGH, GL.GL_COMPILE);
        gl.glDisable(GL.GL_LIGHTING);
        glu.gluDisk(quadric, 0.0, 1.0, 32, 2);
        gl.glEnable(GL.GL_LIGHTING);
        gl.glEndList();
        //End

        return DISK_HIGH;
    }

    public void beforeDisplay(GL gl, GLU glu) {
    }

    public void afterDisplay(GL gl, GLU glu) {
    }

    public void initFromOpenGLThread() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public JPanel getPanel() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
