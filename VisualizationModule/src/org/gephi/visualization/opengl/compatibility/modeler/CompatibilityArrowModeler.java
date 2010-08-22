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

import org.gephi.visualization.api.initializer.CompatibilityModeler;
import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;
import javax.swing.JPanel;
import org.gephi.graph.api.EdgeData;
import org.gephi.graph.api.NodeData;
import org.gephi.graph.api.Renderable;
import org.gephi.visualization.VizController;
import org.gephi.visualization.opengl.AbstractEngine;
import org.gephi.visualization.apiimpl.ModelImpl;
import org.gephi.visualization.apiimpl.VizConfig;
import org.gephi.visualization.opengl.compatibility.CompatibilityEngine;
import org.gephi.visualization.opengl.compatibility.objects.Arrow2dModel;
import org.gephi.visualization.opengl.compatibility.objects.Arrow3dModel;

/**
 *
 * @author Mathieu Bastian
 */
public class CompatibilityArrowModeler implements CompatibilityModeler<NodeData> {

    private CompatibilityEngine engine;
    private VizController controller;

    public CompatibilityArrowModeler(AbstractEngine engine) {
        this.engine = (CompatibilityEngine) engine;
        this.controller = VizController.getInstance();
    }

    @Override
    public ModelImpl initModel(Renderable n) {
        EdgeData e = (EdgeData) n;
        Arrow2dModel arrow;
        if (controller.getVizModel().isUse3d()) {
            arrow = new Arrow3dModel(e);
        } else {
            arrow = new Arrow2dModel(e);
        }
        arrow.setObj(e.getTarget());

        return arrow;
    }

    public void chooseModel(ModelImpl obj) {
        float distance = engine.cameraDistance(obj) + obj.getObj().getRadius();	//Radius is added to cancel the cameraDistance diff
        if (distance < 100) {
            obj.mark = false;
        } else {
            obj.mark = true;
        }
    }

    public void beforeDisplay(GL gl, GLU glu) {
        gl.glBegin(GL.GL_TRIANGLES);
    }

    public void afterDisplay(GL gl, GLU glu) {
        gl.glEnd();
    }

    public int initDisplayLists(GL gl, GLU glu, GLUquadric quadric, int ptr) {
        return ptr;
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
