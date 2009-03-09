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

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;

import gephi.visualization.NodeInitializer;
import gephi.visualization.Renderable;
import gephi.visualization.opengl.Object3d;

/**
 * Specialized initilizer interface adapted to the {@link CompatibilityEngine} processes.
 *
 * @author Mathieu Bastian
 */
public interface CompatibilityNodeInitializer extends NodeInitializer
{
	public void chooseModel(Object3d<Renderable> obj);
	public int initDisplayLists(GL gl, GLU glu, GLUquadric quadric, int ptr);
	public void initFromOpenGLThread();
}
