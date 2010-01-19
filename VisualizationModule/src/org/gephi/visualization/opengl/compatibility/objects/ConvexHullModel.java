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
import org.gephi.visualization.VizModel;
import org.gephi.visualization.api.ModelImpl;
import org.gephi.lib.gleem.linalg.Vecf;
import org.gephi.visualization.hull.ConvexHull;
import org.gephi.visualization.opengl.octree.Octant;

/**
 *
 * @author Mathieu Bastian
 */
public class ConvexHullModel extends ModelImpl<ConvexHull> {

    protected boolean autoSelect = false;
    protected boolean requestUpdate = true;
    protected float scale = 0.1f;
    protected float scaleQuantum = 0.1f;

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
    public void display(GL gl, GLU glu, VizModel model) {
        if (requestUpdate) {
            requestUpdate = false;
            obj.recompute();
        }
        float r = obj.r();
        float g = obj.g();
        float b = obj.b();
        /*float rlight = Math.min(1, 0.5f * r + 0.5f);
        float glight = Math.min(1, 0.5f * g + 0.5f);
        float blight = Math.min(1, 0.5f * b + 0.5f);*/

        //Fill
        if (selected) {
            gl.glColor4f(r, g, b, 0.8f);
        } else {
            gl.glColor4f(r, g, b, 0.15f);
        }

        //Centroid
        float centroidX = 0f;
        float centroidY = 0f;

        //Scale factor
        if (scale < 1f && scale > 0f) {
            centroidX = obj.x();
            centroidY = obj.y();
            gl.glPushMatrix();
            gl.glTranslatef(centroidX, centroidY, 0f);
            gl.glScalef(scale, scale, 1f);
        }


        gl.glBegin(GL.GL_POLYGON);
        ModelImpl[] nodes = obj.getNodes();
        for (int i = 0; i < nodes.length; i++) {
            ModelImpl node = nodes[i];
            gl.glVertex3f(node.getObj().x() - centroidX, node.getObj().y() - centroidY, node.getObj().z());
        }
        gl.glEnd();

        //Line
        gl.glColor4f(r, g, b, 0.8f);
        gl.glBegin(GL.GL_LINE_LOOP);
        for (int i = 0; i < nodes.length; i++) {
            ModelImpl node = nodes[i];
            gl.glVertex3f(node.getObj().x() - centroidX, node.getObj().y() - centroidY, node.getObj().z());
        }
        gl.glEnd();

        if (scale < 1f && scale > 0f) {
            scale += scaleQuantum;
            gl.glPopMatrix();
        }
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
        Octant[] newOctants = new Octant[nodes.length];
        boolean allNull = true;
        for (int i = 0; i < newOctants.length; i++) {
            Octant oc = nodes[i].getOctants()[0];
            newOctants[i] = oc;
            if (oc != null) {
                allNull = false;
            }
        }
        if (!allNull) {
            octants = newOctants;
        }
        return octants;
    }

    @Override
    public void resetOctant() {
        octants = null;
    }

    @Override
    public void destroy() {
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

    public void setScale(float scale) {
        this.scale = scale;
    }

    public void setScaleQuantum(float scaleQuantum) {
        this.scaleQuantum = scaleQuantum;
    }
}
