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
import org.gephi.graph.api.NodeData;
import org.gephi.visualization.api.ModelImpl;
import org.gephi.visualization.gleem.linalg.Vecf;
import org.gephi.visualization.opengl.octree.Octant;

/**
 *
 * @author Mathieu Bastian
 */
public class NodeDiskModel extends ModelImpl<NodeData> {

    public int modelType;
    public int modelBorderType;

    public NodeDiskModel() {
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
        boolean selec = selected;
        if (config.isAutoSelectNeighbor() && mark) {
            selec = true;
            mark = false;
        }
        gl.glPushMatrix();
        float size = obj.getSize() * 2;
        gl.glTranslatef(obj.x(), obj.y(), obj.z());
        gl.glScalef(size, size, size);

        if (!selec) {
            if (config.isLightenNonSelected()) {
                float[] lightColor = config.getLightenNonSelectedColor();
                float lightColorFactor = config.getLightenNonSelectedFactor();
                float r = obj.r();
                float g = obj.g();
                float b = obj.b();
                float rlight = Math.min(1, 0.5f * r + 0.5f);
                float glight = Math.min(1, 0.5f * g + 0.5f);
                float blight = Math.min(1, 0.5f * b + 0.5f);
                gl.glColor3f(rlight + (lightColor[0] - rlight) * lightColorFactor, glight + (lightColor[1] - glight) * lightColorFactor,  blight + (lightColor[2] - blight) * lightColorFactor);
                gl.glCallList(modelType);
                if (modelBorderType != 0) {
                    gl.glColor3f(r + (lightColor[0] - r) * lightColorFactor, g + (lightColor[1] - g) * lightColorFactor,  b + (lightColor[2] - b) * lightColorFactor);
                    gl.glCallList(modelBorderType);
                }
            } else {
                float r = obj.r();
                float g = obj.g();
                float b = obj.b();
                float rlight = Math.min(1, 0.5f * r + 0.5f);
                float glight = Math.min(1, 0.5f * g + 0.5f);
                float blight = Math.min(1, 0.5f * b + 0.5f);
                gl.glColor3f(rlight, glight, blight);
                gl.glCallList(modelType);
                if (modelBorderType != 0) {
                    gl.glColor3f(r, g, b);
                    gl.glCallList(modelBorderType);
                }
            }
        } else {
            float r;
            float g;
            float b;
            if (config.isUniColorSelected()) {
                r = config.getUniColorSelectedColor()[0];
                g = config.getUniColorSelectedColor()[1];
                b = config.getUniColorSelectedColor()[2];
            } else {
                r = obj.r();
                g = obj.g();
                b = obj.b();
            }

            gl.glColor3f(r, g, b);
            gl.glCallList(modelType);
            if (modelBorderType != 0) {
                float rdark = 0.498f * r;
                float gdark = 0.498f * g;
                float bdark = 0.498f * b;
                gl.glColor3f(rdark, gdark, bdark);
                gl.glCallList(modelBorderType);
            }
        }

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
