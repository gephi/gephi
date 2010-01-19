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
import org.gephi.graph.api.EdgeData;
import org.gephi.graph.api.NodeData;
import org.gephi.visualization.VizModel;
import org.gephi.visualization.api.ModelImpl;
import org.gephi.lib.gleem.linalg.Vec3f;

/**
 *
 * @author Mathieu Bastian
 */
public class Arrow3dModel extends Arrow2dModel {

    public Arrow3dModel(EdgeData edge) {
        super(edge);
    }

    @Override
    public void display(GL gl, GLU glu, VizModel vizModel) {
        if (!selected && vizModel.isHideNonSelectedEdges()) {
            return;
        }
        NodeData nodeFrom = edge.getSource();
        NodeData nodeTo = edge.getTarget();

        //Edge size
        float weight = edge.getEdge().getWeight();
        float arrowWidth = ARROW_WIDTH * weight * 2f;
        float arrowHeight = ARROW_HEIGHT * weight * 2f;

        //Edge vector
        Vec3f edgeVector = new Vec3f(nodeTo.x() - nodeFrom.x(), nodeTo.y() - nodeFrom.y(), nodeTo.z() - nodeFrom.z());
        edgeVector.normalize();

        //Get collision distance between nodeTo and arrow point
        double angle = Math.atan2(nodeTo.y() - nodeFrom.y(), nodeTo.x() - nodeFrom.x());
        float collisionDistance = ((ModelImpl) nodeTo.getModel()).getCollisionDistance(angle);

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
                    NodeData source = edge.getSource();
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
                ModelImpl m1 = (ModelImpl) edge.getSource().getModel();
                ModelImpl m2 = (ModelImpl) edge.getTarget().getModel();
                if (m1.isSelected() && m2.isSelected()) {
                    float[] both = vizModel.getEdgeBothSelectionColor();
                    r = both[0];
                    g = both[1];
                    b = both[2];
                } else if (m1.isSelected()) {
                    float[] out = vizModel.getEdgeOutSelectionColor();
                    r = out[0];
                    g = out[1];
                    b = out[2];
                } else if (m2.isSelected()) {
                    float[] in = vizModel.getEdgeInSelectionColor();
                    r = in[0];
                    g = in[1];
                    b = in[2];
                }
            } else {
                r = edge.r();
                if (r == -1f) {
                    NodeData source = edge.getSource();
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

        gl.glVertex3d(baseX + sideVector.x() * arrowWidth, baseY + sideVector.y() * arrowWidth, baseZ + sideVector.z() * arrowWidth);
        gl.glVertex3d(baseX - sideVector.x() * arrowWidth, baseY - sideVector.y() * arrowWidth, baseZ - sideVector.z() * arrowWidth);
        gl.glVertex3d(targetX, targetY, targetZ);
    }
}
