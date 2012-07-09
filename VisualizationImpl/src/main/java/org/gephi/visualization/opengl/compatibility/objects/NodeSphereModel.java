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
import org.gephi.graph.api.NodeData;
import org.gephi.visualization.VizModel;
import org.gephi.visualization.apiimpl.ModelImpl;
import org.gephi.lib.gleem.linalg.Vecf;
import org.gephi.visualization.opengl.compatibility.CompatibilityEngine;
import org.gephi.visualization.opengl.octree.Octant;

/**
 * Represent the basic 3d node shape, namely Sphere. Support different model type, which is display list
 * identifier.
 *
 * @author Mathieu Bastian
 * @see CompatibilityEngine
 */
public class NodeSphereModel extends ModelImpl<NodeData> {

    public int modelType;

    public NodeSphereModel() {
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
        if (Math.abs(obj.x() - leaf.getPosX()) > (leaf.getSize() / 2 - obj.getRadius())
                || Math.abs(obj.y() - leaf.getPosY()) > (leaf.getSize() / 2 - obj.getRadius())
                || Math.abs(obj.z() - leaf.getPosZ()) > (leaf.getSize() / 2 - obj.getRadius())) {
            return false;
        }
        return true;
    }

    @Override
    public void display(GL gl, GLU glu, VizModel model) {
        boolean selec = selected;
        boolean neighbor = false;
        highlight = false;
        if (model.isAutoSelectNeighbor() && mark && !selec) {
            selec = true;
            highlight = true;
            neighbor = true;
        }
        mark = false;
        gl.glPushMatrix();
        float size = obj.getSize() * 2;
        gl.glTranslatef(obj.x(), obj.y(), obj.z());
        gl.glScalef(size, size, size);

        if (!selec) {
            if (model.getConfig().isLightenNonSelected()) {
                float[] lightColor = model.getConfig().getLightenNonSelectedColor();
                float lightColorFactor = model.getConfig().getLightenNonSelectedFactor();
                float r = obj.r();
                float g = obj.g();
                float b = obj.b();
                gl.glColor3f(r + (lightColor[0] - r) * lightColorFactor, g + (lightColor[1] - g) * lightColorFactor, b + (lightColor[2] - b) * lightColorFactor);
                gl.glCallList(modelType);
            } else {
                float r = obj.r();
                float g = obj.g();
                float b = obj.b();
                gl.glColor3f(r, g, b);
                gl.glCallList(modelType);
            }
        } else {
            float r;
            float g;
            float b;
            float rborder;
            float gborder;
            float bborder;
            if (model.isUniColorSelected()) {
                if (neighbor) {
                    r = model.getConfig().getUniColorSelectedNeigborColor()[0];
                    g = model.getConfig().getUniColorSelectedNeigborColor()[1];
                    b = model.getConfig().getUniColorSelectedNeigborColor()[2];
                } else {
                    r = model.getConfig().getUniColorSelectedColor()[0];
                    g = model.getConfig().getUniColorSelectedColor()[1];
                    b = model.getConfig().getUniColorSelectedColor()[2];
                }
                rborder = 0.498f * r;
                gborder = 0.498f * g;
                bborder = 0.498f * b;
            } else {
                rborder = obj.r();
                gborder = obj.g();
                bborder = obj.b();
                r = Math.min(1, 0.5f * rborder + 0.5f);
                g = Math.min(1, 0.5f * gborder + 0.5f);
                b = Math.min(1, 0.5f * bborder + 0.5f);
            }
            gl.glColor3f(r, g, b);
            gl.glCallList(modelType);
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
