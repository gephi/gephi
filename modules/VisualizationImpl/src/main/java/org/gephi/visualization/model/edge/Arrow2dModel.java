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
import org.gephi.lib.gleem.linalg.Vec2f;
import org.gephi.visualization.GraphLimits;
import org.gephi.visualization.VizModel;
import org.gephi.visualization.model.node.NodeModel;

/**
 *
 * @author Mathieu Bastian
 */
public class Arrow2dModel extends ArrowModel {

    protected static float ARROW_WIDTH = 1f;
    protected static float ARROW_HEIGHT = 1.1f;

    public Arrow2dModel(EdgeModel edgeModel) {
        super(edgeModel);
    }

    @Override
    public void display(GL2 gl, GLU glu, VizModel vizModel) {
        if (!edgeModel.isSelected() && vizModel.isHideNonSelectedEdges()) {
            return;
        }

        Edge edge = edgeModel.getEdge();
        Node nodeFrom = edge.getSource();
        Node nodeTo = edge.getTarget();
        NodeModel sourceModel = edgeModel.getSourceModel();
        NodeModel targetModel = edgeModel.getTargetModel();

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

        float x2 = nodeTo.x();
        float y2 = nodeTo.y();
        float x1 = nodeFrom.x();
        float y1 = nodeFrom.y();

        //Edge vector
        Vec2f edgeVector = new Vec2f(x2 - x1, y2 - y1);
        edgeVector.normalize();

        //Get collision distance between nodeTo and arrow point
        double angle = Math.atan2(y2 - y1, x2 - x1);
        float collisionDistance = targetModel.getCollisionDistance(angle);

        //Point of the arrow
        float targetX = x2 - edgeVector.x() * collisionDistance;
        float targetY = y2 - edgeVector.y() * collisionDistance;

        //Base of the arrow
        float baseX = targetX - edgeVector.x() * arrowHeight * 2f;
        float baseY = targetY - edgeVector.y() * arrowHeight * 2f;

        //Side vector
        float sideVectorX = y1 - y2;
        float sideVectorY = x2 - x1;
        float norm = (float) Math.sqrt(sideVectorX * sideVectorX + sideVectorY * sideVectorY);
        sideVectorX /= norm;
        sideVectorY /= norm;

        //Color
        if (!edgeModel.isSelected()) {
            float r;
            float g;
            float b;
            float a;
            r = edge.r();
            if (r == -1f) {
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
                    a = edge.alpha();
                }
            } else {
                g = 0.498f * edge.g();
                b = 0.498f * edge.b();
                r *= 0.498f;
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
                r = edge.r();
                if (r == -1f) {
                    Node source = edge.getSource();
                    r = source.r();
                    g = source.g();
                    b = source.b();
                } else {
                    g = edge.g();
                    b = edge.b();
                }
            }
            gl.glColor4f(r, g, b, 1f);
        }

        //Draw the triangle
        gl.glVertex2d(baseX + sideVectorX * arrowWidth, baseY + sideVectorY * arrowWidth);
        gl.glVertex2d(baseX - sideVectorX * arrowWidth, baseY - sideVectorY * arrowWidth);
        gl.glVertex2d(targetX, targetY);
    }

    public boolean isSelected() {
        return edgeModel.isSelected();
    }
}
