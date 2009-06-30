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
import org.gephi.visualization.VizController;
import org.gephi.visualization.api.ModelImpl;

/**
 *
 * @author Mathieu Bastian
 */
public class Edge3dModel extends Edge2dModel {

    private float[] cameraLocation;

    public Edge3dModel() {
        super();
        cameraLocation = VizController.getInstance().getDrawable().getCameraLocation();
    }

    @Override
    public void display(GL gl, GLU glu) {
        if (this.arrow != null) {
            this.arrow.setSelected(selected);
        }
        if (!selected && config.isHideNonSelectedEdges()) {
            return;
        }
        if (selected && config.isAutoSelectNeighbor()) {
            ModelImpl m1 = (ModelImpl) obj.getSource().getModel();
            ModelImpl m2 = (ModelImpl) obj.getTarget().getModel();
            m1.mark = true;
            m2.mark = true;
        }

        float x1 = obj.getSource().x();
        float x2 = obj.getTarget().x();
        float y1 = obj.getSource().y();
        float y2 = obj.getTarget().y();
        float z1 = obj.getSource().z();
        float z2 = obj.getTarget().z();
        float t1 = obj.getEdge().getWeight() / CARDINAL_DIV;
        float t2 = obj.getEdge().getWeight() / CARDINAL_DIV;

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
            float a;
            if (config.isEdgeUniColor()) {
                float[] uni = config.getEdgeUniColorValue();
                r = uni[0];
                g = uni[1];
                b = uni[2];
                a = uni[3];
            } else {
                r = obj.r();
                g = obj.g();
                b = obj.b();
                a = obj.alpha();
            }
            if (config.isLightenNonSelected()) {
                float lightColorFactor = config.getLightenNonSelectedFactor();
                a = a - (a - 0.1f) * lightColorFactor;
                gl.glColor4f(r, g, b, a);
            } else {
                gl.glColor4f(r, g, b, a);
            }
        } else {
            float rdark = 0.498f * obj.r();
            float gdark = 0.498f * obj.g();
            float bdark = 0.498f * obj.b();
            gl.glColor4f(rdark, gdark, bdark, 1f);
        }

        gl.glVertex3f(x1 + x1Thick, y1 + y1Thick, z1 + z1Thick);
        gl.glVertex3f(x1 - x1Thick, y1 - y1Thick, z1 - z1Thick);
        gl.glVertex3f(x2 - x2Thick, y2 - y2Thick, z2 - z2Thick);
        gl.glVertex3f(x2 - x2Thick, y2 - y2Thick, z2 - z2Thick);
        gl.glVertex3f(x2 + x2Thick, y2 + y2Thick, z2 + z2Thick);
        gl.glVertex3f(x1 + x1Thick, y1 + y1Thick, z1 + z1Thick);

    }
}
