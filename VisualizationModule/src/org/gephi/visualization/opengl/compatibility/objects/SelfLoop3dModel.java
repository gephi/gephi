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
import org.gephi.visualization.VizController;
import org.gephi.visualization.VizModel;
import org.gephi.visualization.api.ModelImpl;
import org.gephi.visualization.gleem.linalg.Vec3f;
import org.gephi.visualization.opengl.octree.Octant;

/**
 *
 * @author Mathieu Bastian
 */
public class SelfLoop3dModel extends SelfLoop2dModel {

    protected float[] cameraLocation;
    protected static Vec3f transVector = new Vec3f(1, 0, 0);

    public SelfLoop3dModel() {
        octants = new Octant[1];
        cameraLocation = VizController.getInstance().getDrawable().getCameraLocation();
    }

    @Override
    public void display(GL gl, GLU glu, VizModel vizModel) {
        gl.glEnd();

        //Params
        float weight = obj.getEdge().getWeight();
        NodeData node = obj.getSource();
        float x = node.x();
        float y = node.y();
        float z = node.z();

        //CameraVector, from camera location to any point on the line
        Vec3f cameraVector = new Vec3f(x - cameraLocation[0], y - cameraLocation[1], z - cameraLocation[2]);
        cameraVector.normalize();

        //Get two vectors perpendicular to cameraVector
        Vec3f upVector = transVector.cross(cameraVector);
        upVector.normalize();
        Vec3f sideVector = cameraVector.cross(upVector);
        sideVector.normalize();

        //Get thickness points
        float baseRightX = x + sideVector.x() * weight / 2;
        float baseRightY = y + sideVector.y() * weight / 2;
        float baseRightZ = z + sideVector.z() * weight / 2;
        float baseLeftX = x - sideVector.x() * weight / 2;
        float baseLeftY = y - sideVector.y() * weight / 2;
        float baseLeftZ = z - sideVector.z() * weight / 2;
        float baseTopX = x + upVector.x() * weight / 2;
        float baseTopY = y + upVector.y() * weight / 2;
        float baseTopZ = z + upVector.z() * weight / 2;
        float baseBottomX = x - upVector.x() * weight / 2;
        float baseBottomY = y - upVector.y() * weight / 2;
        float baseBottomZ = z - upVector.z() * weight / 2;

        //Calculate control points
        float height = node.getRadius() * 3;
        float controlExterior1X = baseLeftX + upVector.x() * height;
        float controlExterior1Y = baseLeftY + upVector.y() * height;
        float controlExterior1Z = baseLeftZ + upVector.z() * height;
        float controlExterior2X = baseBottomX + sideVector.x() * height;
        float controlExterior2Y = baseBottomY + sideVector.y() * height;
        float controlExterior2Z = baseBottomZ + sideVector.z() * height;
        height /= 1.15f;
        float controlInterior1X = baseRightX + upVector.x() * height;
        float controlInterior1Y = baseRightY + upVector.y() * height;
        float controlInterior1Z = baseRightZ + upVector.z() * height;
        float controlInterior2X = baseTopX + sideVector.x() * height;
        float controlInterior2Y = baseTopY + sideVector.y() * height;
        float controlInterior2Z = baseTopZ + sideVector.z() * height;

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
            float a;
            if (vizModel.isEdgeHasUniColor()) {
                float[] uni = vizModel.getEdgeUniColor();
                r = uni[0];
                g = uni[1];
                b = uni[2];
                a = uni[3];
            } else {
                r = obj.r();
                if (r == -1f) {
                    NodeData source = obj.getSource();
                    r = 0.498f * source.r();
                    g = 0.498f * source.g();
                    b = 0.498f * source.b();
                    a = obj.alpha();
                } else {
                    g = 0.498f * obj.g();
                    b = 0.498f * obj.b();
                    r *= 0.498f;
                    a = obj.alpha();
                }
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
                ModelImpl m1 = (ModelImpl) obj.getSource().getModel();
                ModelImpl m2 = (ModelImpl) obj.getTarget().getModel();
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
                r = obj.r();
                if (r == -1f) {
                    NodeData source = obj.getSource();
                    r = source.r();
                    g = source.g();
                    b = source.b();
                } else {
                    g = obj.g();
                    b = obj.b();
                }
            }
            gl.glColor4f(r, g, b, 1f);
        }

        //Display
        gl.glMap2f(GL.GL_MAP2_VERTEX_3, 0, 1, 3, 4, 0, 1, 12, 2, buffer);       //Map evaluators
        gl.glEnable(GL.GL_MAP2_VERTEX_3);
        gl.glMapGrid2f(segments, 0, 1, 1, 0, 1);     //Grid
        gl.glEvalMesh2(GL.GL_FILL, 0, segments, 0, 1);      //Display
        gl.glDisable(GL.GL_MAP2_VERTEX_3);

        gl.glEnd();

        gl.glBegin(GL.GL_TRIANGLES);

    }
}
