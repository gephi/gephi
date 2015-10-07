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
package org.gephi.visualization.model.node;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;
import org.gephi.graph.api.Node;
import org.gephi.lib.gleem.linalg.Vecf;
import org.gephi.visualization.VizModel;

public class NodeDiskModel extends NodeModel {

    public int modelType;
    public int modelBorderType;

    public NodeDiskModel(Node node) {
        super(node);
    }

    @Override
    public void display(GL2 gl, GLU glu, VizModel vizModel) {
        boolean selec = selected;
        boolean neighbor = false;
        highlight = false;
        if (vizModel.isAutoSelectNeighbor() && mark && !selec) {
            selec = true;
            highlight = true;
            neighbor = true;
        }
        mark = false;
        gl.glPushMatrix();
        float size = node.size() * 2;
        gl.glTranslatef(node.x(), node.y(), node.z());
        gl.glScalef(size, size, 1f);

        if (!selec) {
            if (vizModel.getConfig().isLightenNonSelected()) {
                float[] lightColor = vizModel.getConfig().getLightenNonSelectedColor();
                float lightColorFactor = vizModel.getConfig().getLightenNonSelectedFactor();
                float r = node.r();
                float g = node.g();
                float b = node.b();
                gl.glColor3f(r + (lightColor[0] - r) * lightColorFactor, g + (lightColor[1] - g) * lightColorFactor, b + (lightColor[2] - b) * lightColorFactor);
                gl.glCallList(modelType);
                if (modelBorderType != 0) {
                    float rborder = 0.498f * r;
                    float gborder = 0.498f * g;
                    float bborder = 0.498f * b;
                    gl.glColor3f(rborder + (lightColor[0] - rborder) * lightColorFactor, gborder + (lightColor[1] - gborder) * lightColorFactor, bborder + (lightColor[2] - bborder) * lightColorFactor);
                    gl.glCallList(modelBorderType);
                }
            } else {
                float r = node.r();
                float g = node.g();
                float b = node.b();
                gl.glColor3f(r, g, b);
                gl.glCallList(modelType);
                if (modelBorderType != 0) {
                    float rborder = 0.498f * r;
                    float gborder = 0.498f * g;
                    float bborder = 0.498f * b;
                    gl.glColor3f(rborder, gborder, bborder);
                    gl.glCallList(modelBorderType);
                }
            }
        } else {
            float r;
            float g;
            float b;
            float rborder;
            float gborder;
            float bborder;
            if (vizModel.isUniColorSelected()) {
                if (neighbor) {
                    r = vizModel.getConfig().getUniColorSelectedNeigborColor()[0];
                    g = vizModel.getConfig().getUniColorSelectedNeigborColor()[1];
                    b = vizModel.getConfig().getUniColorSelectedNeigborColor()[2];
                } else {
                    r = vizModel.getConfig().getUniColorSelectedColor()[0];
                    g = vizModel.getConfig().getUniColorSelectedColor()[1];
                    b = vizModel.getConfig().getUniColorSelectedColor()[2];
                }
                rborder = 0.498f * r;
                gborder = 0.498f * g;
                bborder = 0.498f * b;
            } else {
                rborder = node.r();
                gborder = node.g();
                bborder = node.b();
                r = Math.min(1, 0.5f * rborder + 0.5f);
                g = Math.min(1, 0.5f * gborder + 0.5f);
                b = Math.min(1, 0.5f * bborder + 0.5f);
            }
            gl.glColor3f(r, g, b);
            gl.glCallList(modelType);
            if (modelBorderType != 0) {
                gl.glColor3f(rborder, gborder, bborder);
                gl.glCallList(modelBorderType);
            }
        }

        gl.glPopMatrix();
    }

    @Override
    public boolean selectionTest(Vecf distanceFromMouse, float selectionSize) {
        return distanceFromMouse.get(2) - selectionSize < node.size();
    }

    @Override
    public float getCollisionDistance(double angle) {
        return node.size();
    }
}
