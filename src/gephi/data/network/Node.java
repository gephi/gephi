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

package gephi.data.network;

import gephi.visualization.Renderable;
import gephi.visualization.opengl.Object3d;

/**
 *
 * @author Mathieu
 */
public class Node implements Renderable
{
    public float x;
	public float y;
	public float z;
    public float a = 0.5f;
	public float r = 0f;
	public float g = 0f;
	public float b = 0f;
	public float size = 1f;

    private Object3d object3d;

    public Node()
	{
		x = ((float)Math.random())*2000-1000.0f;
		y = ((float)Math.random())*2000-1000.0f;
        //z = ((float)Math.random())*2000-2000.0f;
	}

    public float getB() {
       return b;
    }

    public float[] getDragDistanceFromMouse() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public float getG() {
       return g;
    }

    public Object3d getObject3d() {
        return object3d;
    }

    public float getR() {
       return r;
    }

    public float getA() {
        return a;
    }

    public float getRadius() {
        return size;
    }

    public float getSize() {
       return size;
    }

    public float getX() {
       return x;
    }

    public float getY() {
       return y;
    }

    public float getZ() {
       return z;
    }

    public void setObject3d(Object3d obj) {
        this.object3d = obj;
    }

    public void setX(float x) {
       this.x = x;
       updatePositionFlag();
    }

    public void setY(float y) {
       this.y = y;
       updatePositionFlag();
    }

    public void setZ(float z) {
       this.z = z;
       updatePositionFlag();
    }

    private void updatePositionFlag()
    {
        if(object3d!=null && object3d.getOctant()!=null)
        {
            object3d.getOctant().requireUpdatePosition();
        }
    }

}
