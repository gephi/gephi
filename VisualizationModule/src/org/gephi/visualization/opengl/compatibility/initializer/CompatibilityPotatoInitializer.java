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

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;
import javax.swing.JPanel;
import org.gephi.graph.api.Potato;
import org.gephi.graph.api.Renderable;
import org.gephi.visualization.api.Object3dImpl;
import org.gephi.visualization.api.initializer.CompatibilityObject3dInitializer;
import org.gephi.visualization.opengl.compatibility.objects.Potato3dObject;

/**
 *
 * @author Mathieu Bastian
 */
public class CompatibilityPotatoInitializer implements CompatibilityObject3dInitializer<Potato> {

    public Object3dImpl initObject(Renderable n) {
        Potato potato = (Potato)n;

        Potato3dObject obj = new Potato3dObject(potato);
        obj.setObj(potato);

        return obj;
    }

    public void chooseModel(Object3dImpl<Potato> obj) {
        throw new UnsupportedOperationException("Not supported yet.");
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
