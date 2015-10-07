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

import com.jogamp.common.nio.Buffers;
import java.nio.FloatBuffer;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.gephi.lib.gleem.linalg.Vec3f;
import org.gephi.visualization.GraphLimits;
import org.gephi.visualization.VizModel;
import org.gephi.visualization.model.node.NodeModel;

/**
 *
 * @author Mathieu Bastian
 */
public class SelfLoopModel extends EdgeModel {

    private static final Vec3f UPVECTOR = new Vec3f(0f, 1f, 0f);
    private static final Vec3f SIDEVECTOR = new Vec3f(1f, 0f, 0f);
    protected static FloatBuffer buffer = Buffers.newDirectFloatBuffer(24);
    protected static int segments = 20;
    //
    protected final NodeModel nodeModel;

    public SelfLoopModel(Edge edge, NodeModel nodeModel) {
        super(edge);
        this.nodeModel = nodeModel;
    }

    @Override
    public void display(GL2 gl, GLU glu, VizModel vizModel) {

        gl.glEnd();

        //Edge weight
        GraphLimits limits = vizModel.getLimits();
        float weightRatio;
        if (limits.getMinWeight() == limits.getMaxWeight()) {
            weightRatio = Edge2dModel.WEIGHT_MINIMUM / limits.getMinWeight();
        } else {
            weightRatio = Math.abs((Edge2dModel.WEIGHT_MAXIMUM - Edge2dModel.WEIGHT_MINIMUM) / (limits.getMaxWeight() - limits.getMinWeight()));
        }
        float w = weight;
        float edgeScale = vizModel.getEdgeScale();
        w = ((w - limits.getMinWeight()) * weightRatio + Edge2dModel.WEIGHT_MINIMUM) * edgeScale;
        //

        //Params
        Node node = edge.getSource();
        float x = node.x();
        float y = node.y();
        float z = node.z();

        //Get thickness points
        float baseRightX = x + SIDEVECTOR.x() * w / 2;
        float baseRightY = y + SIDEVECTOR.y() * w / 2;
        float baseRightZ = z + SIDEVECTOR.z() * w / 2;
        float baseLeftX = x - SIDEVECTOR.x() * w / 2;
        float baseLeftY = y - SIDEVECTOR.y() * w / 2;
        float baseLeftZ = z - SIDEVECTOR.z() * w / 2;
        float baseTopX = x + UPVECTOR.x() * w / 2;
        float baseTopY = y + UPVECTOR.y() * w / 2;
        float baseTopZ = z + UPVECTOR.z() * w / 2;
        float baseBottomX = x - UPVECTOR.x() * w / 2;
        float baseBottomY = y - UPVECTOR.y() * w / 2;
        float baseBottomZ = z - UPVECTOR.z() * w / 2;

        //Calculate control points
        float height = node.size() * 3;
        float controlExterior1X = baseLeftX + UPVECTOR.x() * height;
        float controlExterior1Y = baseLeftY + UPVECTOR.y() * height;
        float controlExterior1Z = baseLeftZ + UPVECTOR.z() * height;
        float controlExterior2X = baseBottomX + SIDEVECTOR.x() * height;
        float controlExterior2Y = baseBottomY + SIDEVECTOR.y() * height;
        float controlExterior2Z = baseBottomZ + SIDEVECTOR.z() * height;
        height /= 1.15f;
        float controlInterior1X = baseRightX + UPVECTOR.x() * height;
        float controlInterior1Y = baseRightY + UPVECTOR.y() * height;
        float controlInterior1Z = baseRightZ + UPVECTOR.z() * height;
        float controlInterior2X = baseTopX + SIDEVECTOR.x() * height;
        float controlInterior2Y = baseTopY + SIDEVECTOR.y() * height;
        float controlInterior2Z = baseTopZ + SIDEVECTOR.z() * height;

        //Fill buffer with interior curve
        buffer.rewind();
        buffer.put(baseRightX);
        buffer.put(baseRightY);
        buffer.put(baseRightZ);
        buffer.put(controlInterior1X);
        buffer.put(controlInterior1Y);
        buffer.put(controlInterior1Z);
        buffer.put(controlInterior2X);
        buffer.put(controlInterior2Y);
        buffer.put(controlInterior2Z);
        buffer.put(baseTopX);
        buffer.put(baseTopY);
        buffer.put(baseTopZ);

        //Fill buffer with exterior curve
        buffer.put(baseLeftX);
        buffer.put(baseLeftY);
        buffer.put(baseLeftZ);
        buffer.put(controlExterior1X);
        buffer.put(controlExterior1Y);
        buffer.put(controlExterior1Z);
        buffer.put(controlExterior2X);
        buffer.put(controlExterior2Y);
        buffer.put(controlExterior2Z);
        buffer.put(baseBottomX);
        buffer.put(baseBottomY);
        buffer.put(baseBottomZ);
        buffer.rewind();                //Rewind

        //Color
        if (!selected) {
            float r;
            float g;
            float b;
            float a = edge.alpha();
            if (vizModel.isEdgeHasUniColor()) {
                float[] uni = vizModel.getEdgeUniColor();
                r = uni[0];
                g = uni[1];
                b = uni[2];
                a = uni[3];
            } else if (a == 0f) {
                Node source = edge.getSource();
                r = 0.498f * source.r();
                g = 0.498f * source.g();
                b = 0.498f * source.b();
                a = source.alpha();
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
                if (nodeModel.isSelected()) {
                    float[] both = vizModel.getEdgeBothSelectionColor();
                    r = both[0];
                    g = both[1];
                    b = both[2];
                }
            } else if (edge.alpha() == 0f) {
                Node source = edge.getSource();
                r = source.r();
                g = source.g();
                b = source.b();
            } else {
                r = edge.r();
                g = edge.g();
                b = edge.b();
            }
            gl.glColor4f(r, g, b, 1f);
        }

        //Display
        gl.glMap2f(GL2.GL_MAP2_VERTEX_3, 0, 1, 3, 4, 0, 1, 12, 2, buffer);       //Map evaluators
        gl.glEnable(GL2.GL_MAP2_VERTEX_3);
        gl.glMapGrid2f(segments, 0, 1, 1, 0, 1);     //Grid
        gl.glEvalMesh2(GL2.GL_FILL, 0, segments, 0, 1);      //Display
        gl.glDisable(GL2.GL_MAP2_VERTEX_3);

        gl.glEnd();

        gl.glBegin(GL2.GL_TRIANGLES);
    }

    @Override
    public void displayArrow(GL2 gl, GLU glu, VizModel model) {
    }

    @Override
    public boolean isAutoSelected() {
        return nodeModel.isSelected();
    }

    @Override
    public NodeModel getSourceModel() {
        return nodeModel;
    }

    @Override
    public NodeModel getTargetModel() {
        return nodeModel;
    }
}
