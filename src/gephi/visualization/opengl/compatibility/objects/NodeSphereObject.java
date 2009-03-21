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
package gephi.visualization.opengl.compatibility.objects;

import gephi.data.network.Node;
import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

import gephi.visualization.Renderable;
import gephi.visualization.opengl.Object3d;
import gephi.visualization.opengl.gleem.linalg.Vec3f;
import gephi.visualization.opengl.octree.Octant;

/**
 * Represent the basic 3d node shape, namely Sphere. Support different model type, which is display list
 * identifier.
 *
 * @author Mathieu Bastian
 * @see CompatibilityEngine
 */
public class NodeSphereObject extends Object3d<Node> {

    public int modelType;
    private Octant octant;

    @Override
    public int[] octreePosition(float centerX, float centerY, float centerZ, float size) {
        //float radius = obj.getRadius();
        int index = 0;

        if (obj.getY() < centerY) {
            index += 4;
        }
        if (obj.getZ() > centerZ) {
            index += 2;
        }
        if (obj.getX() < centerX) {
            index += 1;
        }

        return new int[]{index};
    }

    @Override
    public boolean isInOctreeLeaf(Octant leaf) {
        if (Math.abs(obj.getX() - leaf.getPosX()) > (leaf.getSize() / 2 - obj.getRadius()) ||
                Math.abs(obj.getY() - leaf.getPosY()) > (leaf.getSize() / 2 - obj.getRadius()) ||
                Math.abs(obj.getZ() - leaf.getPosZ()) > (leaf.getSize() / 2 - obj.getRadius())) {
            return true;
        }
        return false;
    }

    @Override
    public void display(GL gl, GLU glu) {
        gl.glPushMatrix();
        float size = obj.getSize() * 2;
        gl.glTranslatef(obj.getX(), obj.getY(), obj.getZ());
        gl.glScalef(size, size, size);
        if (selected) {
            gl.glColor4f(1f, 1f, 1f, obj.a);
        } else {
            gl.glColor4f(obj.b, obj.g, obj.b, obj.a);
        }
        gl.glCallList(modelType);
        gl.glPopMatrix();
    }

    @Override
    public boolean selectionTest(Vec3f distanceFromMouse, float selectionSize) {
        if (distanceFromMouse.z() - selectionSize < getViewportRadius()) {
            return true;
        }
        return false;
    }

    @Override
    public float getCollisionDistance(double angle) {
        return obj.getRadius();
    }

    @Override
    public String toSVG() {
        return null;
    }

    @Override
    public void setOctant(Octant octant) {
        this.octant = octant;
    }

    @Override
    public Octant getOctant() {
        return octant;
    }
}
