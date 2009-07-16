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
import org.gephi.visualization.api.ModelImpl;
import org.gephi.visualization.gleem.linalg.Vecf;
import org.gephi.visualization.hull.ConvexHull;
import org.gephi.visualization.opengl.octree.Octant;

/**
 *
 * @author Mathieu Bastian
 */
public class ConvexHullModel extends ModelImpl<ConvexHull> {

    protected boolean autoSelect = false;
    protected boolean requestUpdate = true;

    @Override
    public int[] octreePosition(float centerX, float centerY, float centerZ, float size) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isInOctreeLeaf(Octant leaf) {
        ModelImpl[] nodes = obj.getNodes();
        if (nodes.length != octants.length) {
            return false;
        }
        for (int i = 0; i < nodes.length; i++) {
            ModelImpl model = nodes[i];
            if (model.getOctants()[0] != octants[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void display(GL gl, GLU glu) {
        if (requestUpdate) {
            requestUpdate = false;
            obj.recompute();
        }
        float r = obj.r();
        float g = obj.g();
        float b = obj.b();
        float rlight = Math.min(1, 0.5f * r + 0.5f);
        float glight = Math.min(1, 0.5f * g + 0.5f);
        float blight = Math.min(1, 0.5f * b + 0.5f);

        //Fill
        if (selected) {
            gl.glColor3f(r, b, b);
        } else {
            gl.glColor3f(rlight, glight, blight);
        }
        gl.glBegin(GL.GL_POLYGON);
        ModelImpl[] nodes = obj.getNodes();
        for (int i = 0; i < nodes.length; i++) {
            ModelImpl node = nodes[i];
            gl.glVertex3f(node.getObj().x(), node.getObj().y(), node.getObj().z());
        }
        gl.glEnd();

        //Line
        gl.glColor3f(r, g, b);
        gl.glBegin(GL.GL_LINE_LOOP);
        for (int i = 0; i < nodes.length; i++) {
            ModelImpl node = nodes[i];
            gl.glVertex3f(node.getObj().x(), node.getObj().y(), node.getObj().z());
        }
        gl.glEnd();
    }

    @Override
    public boolean selectionTest(Vecf distanceFromMouse, float selectionSize) {
        return false;
    }

    @Override
    public float getCollisionDistance(double angle) {
        return 0f;
    }

    @Override
    public String toSVG() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setOctant(Octant octant) {
    }

    @Override
    public Octant[] getOctants() {
        ModelImpl[] nodes = obj.getNodes();
        octants = new Octant[nodes.length];
        for (int i = 0; i < octants.length; i++) {
            octants[i] = nodes[i].getOctants()[0];
        }
        return octants;
    }

    @Override
    public void resetOctant() {
        octants = null;
    }

    @Override
    public boolean isAutoSelected() {
        return autoSelect;
    }

    @Override
    public boolean onlyAutoSelect() {
        return true;
    }

    @Override
    public void setAutoSelect(boolean autoSelect) {
        this.autoSelect = autoSelect;
    }

    @Override
    public void updatePositionFlag() {
        requestUpdate = true;
    }
}
