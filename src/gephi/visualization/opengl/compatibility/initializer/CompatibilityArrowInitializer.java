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

package gephi.visualization.opengl.compatibility.initializer;

import gephi.data.network.Edge;
import gephi.data.network.Node;
import gephi.visualization.Renderable;
import gephi.visualization.opengl.AbstractEngine;
import gephi.visualization.opengl.Object3d;
import gephi.visualization.opengl.compatibility.CompatibilityEngine;
import gephi.visualization.opengl.compatibility.nodeobjects.Arrow3dObject;
import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;
import javax.swing.JPanel;

/**
 *
 * @author Mathieu
 */
public class CompatibilityArrowInitializer implements CompatibilityObject3dInitializer<Node> {

    private CompatibilityEngine engine;

	public CompatibilityArrowInitializer(AbstractEngine engine)
	{
		this.engine = (CompatibilityEngine)engine;
	}

    @Override
    public Object3d initObject(Renderable n) {
        Edge e = (Edge)n;

        Arrow3dObject arrow = new Arrow3dObject(e);
		arrow.setObj(e.getSource());
        arrow.setOctant(e.getSource().getObject3d().getOctant());

        return arrow;
    }

    public void chooseModel(Object3d<Node> obj) {
       float distance = engine.cameraDistance(obj) + obj.getObj().getRadius();	//Radius is added to cancel the cameraDistance diff
       if(distance < 100)
            obj.mark = false;
       else
           obj.mark = true;
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
