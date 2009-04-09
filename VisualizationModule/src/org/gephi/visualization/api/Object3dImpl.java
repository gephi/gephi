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
package org.gephi.visualization.api;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import org.gephi.datastructure.avl.simple.AVLItem;
import org.gephi.graph.api.Object3d;
import org.gephi.graph.api.Renderable;
import org.gephi.visualization.opengl.gleem.linalg.Vec3f;
import org.gephi.visualization.opengl.octree.Octant;

/**
 *
 * @author Mathieu Bastian
 */
public abstract class Object3dImpl<ObjectType extends Renderable> implements Object3d, AVLItem {

    private int ID = -1;
    private int cacheMarker;

    //Architecture
    protected Octant[] octants;
    protected ObjectType obj;

    //Graphical data
    protected float viewportX;
    protected float viewportY;
    protected float viewportZ;
    protected float viewportRadius;
    protected float[] dragDistance;

    //Flags
    protected boolean selected;
    public long markTime = 0;
    public boolean mark = false;

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
        if (this.ID == -1) {
            this.ID = ID;
        }
    }

    public Octant[] getOctants() {
        return octants;
    }

    public void setOctant(Octant octant) {
        this.octants[0] = octant;
    }

    public void resetOctant(Octant octant) {
        if (this.octants != null) {
            this.octants[0] = null;
        }
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

    public float[] getDragDistanceFromMouse() {
        return dragDistance;
    }

    public void setDragDistanceFromMouse(float[] dragDistance) {
        this.dragDistance = dragDistance;
    }

    public void setCacheMarker(int cacheMarker) {
        this.cacheMarker = cacheMarker;
    }

    public boolean isCacheMatching(int cacheMarker) {
        return cacheMarker == this.cacheMarker;
    }
}
