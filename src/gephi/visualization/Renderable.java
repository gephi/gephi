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

package gephi.visualization;

import gephi.visualization.opengl.Object3d;

/**
 *
 * @author Mathieu Bastian
 */
public interface Renderable {
    public float getX();
	public float getY();
	public float getZ();
	public void setX(float x);
	public void setY(float y);
	public void setZ(float z);
	public float getRadius();
	public float getSize();
	public float getR();
	public float getG();
	public float getB();

    public Object3d getObject3d();
    public void setObject3d(Object3d obj);
}
