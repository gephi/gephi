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
package org.gephi.visualization.opengl.compatibility.objects;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import org.gephi.graph.api.Node;
import org.gephi.visualization.api.Object3dImpl;
import org.gephi.visualization.gleem.linalg.Vecf;
import org.gephi.visualization.opengl.compatibility.CompatibilityEngine;
import org.gephi.visualization.opengl.octree.Octant;

/**
 * Represent the basic 3d node shape, namely Sphere. Support different model type, which is display list
 * identifier.
 *
 * @author Mathieu Bastian
 * @see CompatibilityEngine
 */
public class NodeSphereObject extends Object3dImpl<Node> {

    public int modelType;

    public NodeSphereObject() {
        octants = new Octant[1];
    }

    @Override
    public int[] octreePosition(float centerX, float centerY, float centerZ, float size) {
        //float radius = obj.getRadius();
        int index = 0;

        if (obj.y() < centerY) {
            index += 4;
        }
        if (obj.z() > centerZ) {
            index += 2;
        }
        if (obj.x() < centerX) {
            index += 1;
        }

        return new int[]{index};
    }

    @Override
    public boolean isInOctreeLeaf(Octant leaf) {
        if (Math.abs(obj.x() - leaf.getPosX()) > (leaf.getSize() / 2 - obj.getRadius()) ||
                Math.abs(obj.y() - leaf.getPosY()) > (leaf.getSize() / 2 - obj.getRadius()) ||
                Math.abs(obj.z() - leaf.getPosZ()) > (leaf.getSize() / 2 - obj.getRadius())) {
            return false;
        }
        return true;
    }

    @Override
    public void display(GL gl, GLU glu) {
        gl.glPushMatrix();
        float size = obj.getSize() * 2;
        gl.glTranslatef(obj.x(), obj.y(), obj.z());
        gl.glScalef(size, size, size);
        if (selected) {
            gl.glColor4f(1f, 1f, 1f, obj.alpha());
        } else {
            gl.glColor4f(obj.r(), obj.g(), obj.b(), obj.alpha());
        }
        gl.glCallList(modelType);
        gl.glPopMatrix();
    }

    @Override
    public boolean selectionTest(Vecf distanceFromMouse, float selectionSize) {
        if (distanceFromMouse.get(2) - selectionSize < getViewportRadius()) {
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
}
