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
package org.gephi.visualization.model.edge;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.gephi.lib.gleem.linalg.Vec3f;
import org.gephi.visualization.GraphLimits;
import org.gephi.visualization.VizController;
import org.gephi.visualization.VizModel;
import org.gephi.visualization.model.node.NodeModel;

/**
 *
 * @author Mathieu Bastian
 */
public class Edge3dModel extends Edge2dModel {

    public Edge3dModel(Edge edge, NodeModel sourceModel, NodeModel targetModel) {
        super(edge, sourceModel, targetModel);
    }

    @Override
    public void display(GL2 gl, GLU glu, VizModel vizModel) {
        float[] cameraLocation = VizController.getInstance().getDrawable().getCameraLocation();

        if (!selected && vizModel.isHideNonSelectedEdges()) {
            return;
        }
        if (selected && vizModel.isAutoSelectNeighbor()) {
            sourceModel.mark = true;
            targetModel.mark = true;
        }

        //Edge weight
        GraphLimits limits = vizModel.getLimits();
        float w;

        float weightRatio;
        if (limits.getMinWeight() == limits.getMaxWeight()) {
            weightRatio = WEIGHT_MINIMUM / limits.getMinWeight();
        } else {
            weightRatio = Math.abs((WEIGHT_MAXIMUM - WEIGHT_MINIMUM) / (limits.getMaxWeight() - limits.getMinWeight()));
        }
        float edgeScale = vizModel.getEdgeScale();
        w = (float) edge.getWeight();
        w = ((w - limits.getMinWeight()) * weightRatio + WEIGHT_MINIMUM) * edgeScale;

        //

        float x1 = edge.getSource().x();
        float x2 = edge.getTarget().x();
        float y1 = edge.getSource().y();
        float y2 = edge.getTarget().y();
        float z1 = edge.getSource().z();
        float z2 = edge.getTarget().z();
        float t1 = w;
        float t2 = w;

        //CameraVector, from camera location to any point on the line
        float cameraVectorX = x1 - cameraLocation[0];
        float cameraVectorY = y1 - cameraLocation[1];
        float cameraVectorZ = z1 - cameraLocation[2];

        //This code has been replaced by followinf more efficient
        //Vec3f edgeVector = new Vec3f(x2 - x1,y2 - y1,z2 - z1);
        //Vec3f cameraVector = new Vec3f(drawable.getCameraLocation()[0] - (x2 - x1)/2f,drawable.getCameraLocation()[1] - (y2 - y1)/2f,drawable.getCameraLocation()[2] - (z2 - z1)/2f);
        //Vec3f sideVector = edgeVector.cross(cameraVector);
        //sideVector.normalize();

        //Vector line
        float edgeVectorX = x2 - x1;
        float edgeVectorY = y2 - y1;
        float edgeVectorZ = z2 - z1;

        //Cross product
        float sideVectorX = edgeVectorY * cameraVectorZ - edgeVectorZ * cameraVectorY;
        float sideVectorY = edgeVectorZ * cameraVectorX - edgeVectorX * cameraVectorZ;
        float sideVectorZ = edgeVectorX * cameraVectorY - edgeVectorY * cameraVectorX;

        //Normalize
        float norm = (float) Math.sqrt(sideVectorX * sideVectorX + sideVectorY * sideVectorY + sideVectorZ * sideVectorZ);
        if (norm > 0f) // Avoid divizion by zero if cameraVector & sideVector colinear
        {
            sideVectorX /= norm;
            sideVectorY /= norm;
            sideVectorZ /= norm;
        } else {
            sideVectorX = 0f;
            sideVectorY = 0f;
            sideVectorZ = 0f;
        }

        float x1Thick = sideVectorX / 2f * t1;
        float x2Thick = sideVectorX / 2f * t2;
        float y1Thick = sideVectorY / 2f * t1;
        float y2Thick = sideVectorY / 2f * t2;
        float z1Thick = sideVectorZ / 2f * t1;
        float z2Thick = sideVectorZ / 2f * t2;

        if (!selected) {
            float r;
            float g;
            float b;
            float a = edge.alpha();
            if (a == 0f) {
                if (vizModel.isEdgeHasUniColor()) {
                    float[] uni = vizModel.getEdgeUniColor();
                    r = uni[0];
                    g = uni[1];
                    b = uni[2];
                    a = uni[3];
                } else {
                    Node source = edge.getSource();
                    r = 0.498f * source.r();
                    g = 0.498f * source.g();
                    b = 0.498f * source.b();
                    a = source.alpha();
                }
            } else {
                g = 0.498f * edge.g();
                b = 0.498f * edge.b();
                r = 0.498f * edge.r();
                a = edge.alpha();
            }
            if (vizModel.getConfig().isLightenNonSelected()) {
                float lightColorFactor = vizModel.getConfig().getLightenNonSelectedFactor();
                a = a - (a - 0.01f) * lightColorFactor;
                gl.glColor4f(r, g, b, a);
            } else {
                gl.glColor4f(r, g, b, a);
            }
        } else {
            float r = 0f;
            float g = 0f;
            float b = 0f;
            if (vizModel.isEdgeSelectionColor()) {
                if (sourceModel.isSelected() && targetModel.isSelected()) {
                    float[] both = vizModel.getEdgeBothSelectionColor();
                    r = both[0];
                    g = both[1];
                    b = both[2];
                } else if (sourceModel.isSelected()) {
                    float[] out = vizModel.getEdgeOutSelectionColor();
                    r = out[0];
                    g = out[1];
                    b = out[2];
                } else if (targetModel.isSelected()) {
                    float[] in = vizModel.getEdgeInSelectionColor();
                    r = in[0];
                    g = in[1];
                    b = in[2];
                }
            } else {
                if (edge.alpha() == 0f) {
                    Node source = edge.getSource();
                    r = source.r();
                    g = source.g();
                    b = source.b();
                } else {
                    r = edge.r();
                    g = edge.g();
                    b = edge.b();
                }
            }
            gl.glColor4f(r, g, b, 1f);
        }

        gl.glVertex3f(x1 + x1Thick, y1 + y1Thick, z1 + z1Thick);
        gl.glVertex3f(x1 - x1Thick, y1 - y1Thick, z1 - z1Thick);
        gl.glVertex3f(x2 - x2Thick, y2 - y2Thick, z2 - z2Thick);
        gl.glVertex3f(x2 - x2Thick, y2 - y2Thick, z2 - z2Thick);
        gl.glVertex3f(x2 + x2Thick, y2 + y2Thick, z2 + z2Thick);
        gl.glVertex3f(x1 + x1Thick, y1 + y1Thick, z1 + z1Thick);

    }

    @Override
    public void displayArrow(GL2 gl, GLU glu, VizModel vizModel) {
        float[] cameraLocation = VizController.getInstance().getDrawable().getCameraLocation();
        if (!selected && vizModel.isHideNonSelectedEdges()) {
            return;
        }

        Node nodeFrom = edge.getSource();
        Node nodeTo = edge.getTarget();

        //Edge weight
        GraphLimits limits = vizModel.getLimits();
        float w;

        float weightRatio;
        if (limits.getMinWeight() == limits.getMaxWeight()) {
            weightRatio = Edge2dModel.WEIGHT_MINIMUM / limits.getMinWeight();
        } else {
            weightRatio = Math.abs((Edge2dModel.WEIGHT_MAXIMUM - Edge2dModel.WEIGHT_MINIMUM) / (limits.getMaxWeight() - limits.getMinWeight()));
        }
        float edgeScale = vizModel.getEdgeScale();
        w = (float) edge.getWeight();
        w = ((w - limits.getMinWeight()) * weightRatio + Edge2dModel.WEIGHT_MINIMUM) * edgeScale;

        //

        //Edge size
        float arrowWidth = ARROW_WIDTH * w * 2f;
        float arrowHeight = ARROW_HEIGHT * w * 2f;

        //Edge vector
        Vec3f edgeVector = new Vec3f(nodeTo.x() - nodeFrom.x(), nodeTo.y() - nodeFrom.y(), nodeTo.z() - nodeFrom.z());
        edgeVector.normalize();

        //Get collision distance between nodeTo and arrow point
        double angle = Math.atan2(nodeTo.y() - nodeFrom.y(), nodeTo.x() - nodeFrom.x());
        float collisionDistance = targetModel.getCollisionDistance(angle);

        float x2 = nodeTo.x();
        float y2 = nodeTo.y();
        float z2 = nodeTo.z();

        //Point of the arrow
        float targetX = x2 - edgeVector.x() * collisionDistance;
        float targetY = y2 - edgeVector.y() * collisionDistance;
        float targetZ = z2 - edgeVector.z() * collisionDistance;

        //Base of the arrow
        float baseX = targetX - edgeVector.x() * arrowHeight * 2f;
        float baseY = targetY - edgeVector.y() * arrowHeight * 2f;
        float baseZ = targetZ - edgeVector.z() * arrowHeight * 2f;

        //Camera vector
        Vec3f cameraVector = new Vec3f(targetX - cameraLocation[0], targetY - cameraLocation[1], targetZ - cameraLocation[2]);

        //Side vector
        Vec3f sideVector = edgeVector.cross(cameraVector);
        sideVector.normalize();

        //Draw the triangle
        if (!selected) {
            float r;
            float g;
            float b;
            float a = edge.alpha();
            if (a == 0f) {
                if (vizModel.isEdgeHasUniColor()) {
                    float[] uni = vizModel.getEdgeUniColor();
                    r = uni[0];
                    g = uni[1];
                    b = uni[2];
                    a = uni[3];
                } else {
                    Node source = edge.getSource();
                    r = 0.498f * source.r();
                    g = 0.498f * source.g();
                    b = 0.498f * source.b();
                    a = source.alpha();
                }
            } else {
                g = 0.498f * edge.g();
                b = 0.498f * edge.b();
                r = 0.498f * edge.r();
                a = edge.alpha();
            }
            if (vizModel.getConfig().isLightenNonSelected()) {
                float lightColorFactor = vizModel.getConfig().getLightenNonSelectedFactor();
                a = a - (a - 0.01f) * lightColorFactor;
                gl.glColor4f(r, g, b, a);
            } else {
                gl.glColor4f(r, g, b, a);
            }
        } else {
            float r = 0f;
            float g = 0f;
            float b = 0f;
            if (vizModel.isEdgeSelectionColor()) {
                if (sourceModel.isSelected() && targetModel.isSelected()) {
                    float[] both = vizModel.getEdgeBothSelectionColor();
                    r = both[0];
                    g = both[1];
                    b = both[2];
                } else if (sourceModel.isSelected()) {
                    float[] out = vizModel.getEdgeOutSelectionColor();
                    r = out[0];
                    g = out[1];
                    b = out[2];
                } else if (targetModel.isSelected()) {
                    float[] in = vizModel.getEdgeInSelectionColor();
                    r = in[0];
                    g = in[1];
                    b = in[2];
                }
            } else {
                if (edge.alpha() == 0f) {
                    Node source = edge.getSource();
                    r = source.r();
                    g = source.g();
                    b = source.b();
                } else {
                    r = edge.r();
                    g = edge.g();
                    b = edge.b();
                }
            }
            gl.glColor4f(r, g, b, 1f);
        }

        gl.glVertex3d(baseX + sideVector.x() * arrowWidth, baseY + sideVector.y() * arrowWidth, baseZ + sideVector.z() * arrowWidth);
        gl.glVertex3d(baseX - sideVector.x() * arrowWidth, baseY - sideVector.y() * arrowWidth, baseZ - sideVector.z() * arrowWidth);
        gl.glVertex3d(targetX, targetY, targetZ);
    }
}
