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
import org.gephi.visualization.api.GraphDrawable;
import org.gephi.visualization.api.ModelImpl;
import org.gephi.visualization.gleem.linalg.Vecf;
import org.gephi.visualization.opengl.octree.Octant;

/**
 *
 * @author Mathieu Bastian
 */
public class NodeRectangeModel extends ModelImpl<NodeData> {

    public boolean border = true;
    protected float width = 20f;
    protected float height = 20f;

    public NodeRectangeModel() {
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
    public void display(GL gl, GLU glu, VizModel vizModel) {
        boolean selec = selected;
        boolean neighbor = false;
        highlight = false;
        if (vizModel.isAutoSelectNeighbor() && mark && !selec) {
            selec = true;
            highlight = true;
            neighbor = true;
        }
        mark = false;
        if (vizModel.isAdjustByText()) {
            width = obj.getTextData().getWidth();
            height = obj.getTextData().getHeight();
        } else {
            float size = obj.getSize();
            width = size;
            height = size;
        }

        float w = width / 2f;
        float h = height / 2f;
        float x = obj.x();
        float y = obj.y();

        float borderSize = 1f;

        if (!selec) {
            if (vizModel.getConfig().isLightenNonSelected()) {
                float[] lightColor = vizModel.getConfig().getLightenNonSelectedColor();
                float lightColorFactor = vizModel.getConfig().getLightenNonSelectedFactor();
                float r = obj.r();
                float g = obj.g();
                float b = obj.b();
                if (border) {
                    float rborder = 0.498f * r;
                    float gborder = 0.498f * g;
                    float bborder = 0.498f * b;
                    gl.glColor3f(rborder + (lightColor[0] - rborder) * lightColorFactor, gborder + (lightColor[1] - gborder) * lightColorFactor, bborder + (lightColor[2] - bborder) * lightColorFactor);
                    gl.glVertex3f(x + w, y + h, 0);
                    gl.glVertex3f(x - w, y + h, 0);
                    gl.glVertex3f(x - w, y - h, 0);
                    gl.glVertex3f(x + w, y - h, 0);
                    w -= borderSize;
                    h -= borderSize;
                }
                gl.glColor3f(r + (lightColor[0] - r) * lightColorFactor, g + (lightColor[1] - g) * lightColorFactor, b + (lightColor[2] - b) * lightColorFactor);
            } else {
                float r = obj.r();
                float g = obj.g();
                float b = obj.b();
                if (border) {
                    float rborder = 0.498f * r;
                    float gborder = 0.498f * g;
                    float bborder = 0.498f * b;
                    gl.glColor3f(rborder, gborder, bborder);
                    gl.glVertex3f(x + w, y + h, 0);
                    gl.glVertex3f(x - w, y + h, 0);
                    gl.glVertex3f(x - w, y - h, 0);
                    gl.glVertex3f(x + w, y - h, 0);
                    w -= borderSize;
                    h -= borderSize;
                }
                gl.glColor3f(r, g, b);
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
                rborder = obj.r();
                gborder = obj.g();
                bborder = obj.b();
                r = Math.min(1, 0.5f * rborder + 0.5f);
                g = Math.min(1, 0.5f * gborder + 0.5f);
                b = Math.min(1, 0.5f * bborder + 0.5f);
            }
            if (border) {
                gl.glColor3f(rborder, gborder, bborder);
                gl.glVertex3f(x + w, y + h, 0);
                gl.glVertex3f(x - w, y + h, 0);
                gl.glVertex3f(x - w, y - h, 0);
                gl.glVertex3f(x + w, y - h, 0);
                w -= borderSize;
                h -= borderSize;
            }
            gl.glColor3f(r, g, b);
        }

        gl.glVertex3f(x + w, y + h, 0);
        gl.glVertex3f(x - w, y + h, 0);
        gl.glVertex3f(x - w, y - h, 0);
        gl.glVertex3f(x + w, y - h, 0);
    }

    @Override
    public boolean selectionTest(Vecf distanceFromMouse, float selectionSize) {
        GraphDrawable drawable = VizController.getInstance().getDrawable();
        if (distanceFromMouse.get(0) < width / 2 * Math.abs(drawable.getDraggingMarkerX()) && distanceFromMouse.get(1) < height / 2 * Math.abs(drawable.getDraggingMarkerY())) {
            return true;
        }
        return false;
    }

    @Override
    public float getCollisionDistance(double angle) {
        double angleSinus = Math.sin(angle);
        double angleCosinus = Math.cos(angle);
        angle %= Math.PI * 2;
        while (angle < 0) {
            angle += Math.PI * 2;
        }

        if (angle < Math.atan2(height / 2, width / 2)
                || (angle > Math.PI - Math.atan2(height / 2, width / 2) && angle < Math.PI + Math.atan2(height / 2, width / 2))
                || angle > 2 * Math.PI - Math.atan2(height / 2, width / 2)) {
            return (float) Math.sqrt((width * width / 4) / (1 - angleSinus * angleSinus));
        } else {
            return (float) Math.sqrt((height * height / 4) / (1 - angleCosinus * angleCosinus));
        }
    }

    @Override
    public String toSVG() {
        return null;
    }
}
