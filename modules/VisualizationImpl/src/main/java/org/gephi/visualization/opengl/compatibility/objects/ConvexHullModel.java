/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
 */
package org.gephi.visualization.opengl.compatibility.objects;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import org.gephi.graph.api.Node;
import org.gephi.visualization.VizModel;
import org.gephi.visualization.apiimpl.ModelImpl;
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
        return new int[0];
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
        if (octants == null) {
            return new Octant[0];
        }
        return octants;
    }

    @Override
    public void resetOctant() {
        if (this.octants != null) {
            this.octants[0] = null;
        }
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
