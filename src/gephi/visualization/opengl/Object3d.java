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
package gephi.visualization.opengl;

import gephi.data.network.avl.simple.AVLItem;
import gephi.visualization.Renderable;
import gephi.visualization.opengl.gleem.linalg.Vec3f;
import gephi.visualization.opengl.octree.Octant;
import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

/**
 *
 * @author Mathieu
 */
public abstract class Object3d<ObjectType extends Renderable> implements AVLItem {

    private int ID;

    //Architecture
    protected Octant octant;
    protected ObjectType obj;

    //Graphical data
    protected float viewportX;
    protected float viewportY;
    protected float viewportZ;
    protected float viewportRadius;
    protected float[] dragDistance;

    //Flags
    protected boolean selected;
    public long markTime=0;
    public boolean mark=false;

    public abstract int[] octreePosition(float centerX, float centerY, float centerZ, float size);
	public abstract boolean isInOctreeLeaf(Octant leaf);
	public abstract void display(GL gl, GLU glu);
	public abstract boolean selectionTest(Vec3f distanceFromMouse, float selectionSize);
	public abstract float getCollisionDistance(double angle);
	public abstract String toSVG();

    public int getNumber() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public Octant getOctant() {
        return octant;
    }

    public void setOctant(Octant octant) {
        this.octant = octant;
    }

    public ObjectType getObj() {
        return obj;
    }

    public void setObj(ObjectType obj) {
        this.obj = obj;
    }

    public float getViewportRadius() {
        return viewportRadius;
    }

    public void setViewportRadius(float viewportRadius) {
        this.viewportRadius = viewportRadius;
    }

    public float getViewportX() {
        return viewportX;
    }

    public void setViewportX(float viewportX) {
        this.viewportX = viewportX;
    }

    public float getViewportY() {
        return viewportY;
    }

    public void setViewportY(float viewportY) {
        this.viewportY = viewportY;
    }

    public float getViewportZ() {
        return viewportZ;
    }

    public void setViewportZ(float viewportZ) {
        this.viewportZ = viewportZ;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public float[] getDragDistanceFromMouse()
    {
        return dragDistance;
    }

    public void setDragDistanceFromMouse(float[] dragDistance)
    {
        this.dragDistance = dragDistance;
    }
}
