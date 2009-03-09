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
package gephi.visualization.opengl.compatibility.nodeinit;

import gephi.visualization.Renderable;
import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;
import javax.swing.JPanel;

import gephi.visualization.nodeinit.NodeSphereInitializer;
import gephi.visualization.opengl.AbstractEngine;
import gephi.visualization.opengl.Object3d;
import gephi.visualization.opengl.compatibility.CompatibilityEngine;
import gephi.visualization.opengl.compatibility.nodeobjects.NodeSphereObject;

/**
 * Default initializer for the nodes. The class draw sphere objects and manage a LOD system.
 *
 * @author Mathieu Bastian
 * @see NodeSphereObject
 */
public class CompatibilityNodeSphereInitializer extends NodeSphereInitializer implements CompatibilityNodeInitializer {

	public int SHAPE_DIAMOND;
	public int SHAPE_SPHERE16;
	public int SHAPE_SPHERE32;
	public int SHAPE_BILLBOARD;

	private CompatibilityEngine engine;

	public CompatibilityNodeSphereInitializer(AbstractEngine engine)
	{
		this.engine = (CompatibilityEngine)engine;
	}

	@Override
	public Object3d<Renderable> initNodeObject(Renderable n)
	{
		NodeSphereObject obj = new NodeSphereObject();
		obj.setObj(n);
		obj.setSelected(false);
		
		chooseModel(obj);
		return obj;
	}

	@Override
	public void chooseModel(Object3d<Renderable> object3d)
	{
		NodeSphereObject obj = (NodeSphereObject)object3d;

		float distance = engine.cameraDistance(object3d)/object3d.getObj().getRadius();
		if(distance > 600)
			obj.modelType = SHAPE_DIAMOND;
		else if(distance > 50)
			obj.modelType = SHAPE_SPHERE16;
		else
			obj.modelType = SHAPE_SPHERE32;
	}

	@Override
	public int initDisplayLists(GL gl, GLU glu, GLUquadric quadric, int ptr)
	{
		// Diamond display list
		SHAPE_DIAMOND = ptr + 1;
		gl.glNewList(SHAPE_DIAMOND,GL.GL_COMPILE);
		gl.glCallList(ptr);
		glu.gluSphere(quadric, 0.5f, 4, 2);
		gl.glEndList();
		//End


		// Sphere16 display list
		SHAPE_SPHERE16 = SHAPE_DIAMOND + 1;
		gl.glNewList(SHAPE_SPHERE16,GL.GL_COMPILE);
		gl.glCallList(ptr);
		glu.gluSphere(quadric, 0.5f, 16, 8);
		gl.glEndList();
		//Fin


		// Sphere32 display list
		SHAPE_SPHERE32 = SHAPE_SPHERE16 + 1;
		gl.glNewList(SHAPE_SPHERE32,GL.GL_COMPILE);
		gl.glCallList(ptr);
		glu.gluSphere(quadric, 0.5f, 32, 16);
		gl.glEndList();

		return SHAPE_SPHERE32;
	}

	@Override
	public void initFromOpenGLThread() {
	}

	@Override
	public JPanel getPanel() {
		return null;
	}
}
