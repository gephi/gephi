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

import org.gephi.visualization.api.initializer.CompatibilityModeler;
import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;
import javax.swing.JPanel;
import org.gephi.graph.api.EdgeData;
import org.gephi.graph.api.Renderable;
import org.gephi.visualization.api.ModelImpl;
import org.gephi.visualization.opengl.compatibility.objects.Edge3dModel;

/**
 *
 * @author Mathieu Bastian
 */
public class CompatibilityEdgeModeler implements CompatibilityModeler<EdgeData> {

    @Override
    public ModelImpl initModel(Renderable n) {
        EdgeData e = (EdgeData) n;

        Edge3dModel edge = new Edge3dModel();
        edge.setObj(e);
        e.setObject3d(edge);

        return edge;
    }

    public void chooseModel(ModelImpl obj) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int initDisplayLists(GL gl, GLU glu, GLUquadric quadric, int ptr) {
        throw new UnsupportedOperationException("Not supported yet.");
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
