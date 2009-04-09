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
package org.gephi.visualization.opengl.compatibility.initializer;

import org.gephi.visualization.api.initializer.CompatibilityObject3dInitializer;
import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;
import javax.swing.JPanel;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Renderable;
import org.gephi.visualization.api.Object3dImpl;
import org.gephi.visualization.opengl.compatibility.objects.Edge3dObject;

/**
 *
 * @author Mathieu Bastian
 */
public class CompatibilityEdgeInitializer implements CompatibilityObject3dInitializer<Edge> {

    @Override
    public Object3dImpl initObject(Renderable n) {
        Edge e = (Edge) n;

        Edge3dObject edge = new Edge3dObject();
        edge.setObj(e);
        e.setObject3d(edge);

        return edge;
    }

    public void chooseModel(Object3dImpl obj) {
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
