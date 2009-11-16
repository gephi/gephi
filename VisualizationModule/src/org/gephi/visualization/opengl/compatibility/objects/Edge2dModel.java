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
import org.gephi.visualization.gleem.linalg.Vecf;
import org.gephi.visualization.opengl.octree.Octant;

/**
 *
 * @author Mathieu Bastian
 */
public class Edge2dModel extends ModelImpl<EdgeData> {

    protected static float WEIGHT_DIV = 1f;  //Set the size of edges according to cardinal

    //An edge is set in both source node and target node octant. Hence edges are not drawn when none of
    //these octants are visible.
    protected ModelImpl arrow;

    public Edge2dModel() {
        octants = new Octant[2];
    }

    @Override
    public int[] octreePosition(float centerX, float centerY, float centerZ, float size) {
        NodeData nodeFrom = obj.getSource();
        NodeData nodeTo = obj.getTarget();

        int index1 = -1, index2 = -1;

        size /= 2;

        //True if the point is in the big cube.
        if (!(Math.abs(nodeFrom.x() - centerX) > size || Math.abs(nodeFrom.y() - centerY) > size || Math.abs(nodeFrom.z() - centerZ) > size)) {
            index1 = 0;

            //Point1
            if (nodeFrom.y() < centerY) {
                index1 += 4;
            }
            if (nodeFrom.z() > centerZ) {
                index1 += 2;
            }
            if (nodeFrom.x() < centerX) {
                index1 += 1;
            }
        }

        if (!(Math.abs(nodeTo.x() - centerX) > size || Math.abs(nodeTo.y() - centerY) > size || Math.abs(nodeTo.z() - centerZ) > size)) {
            index2 = 0;

            //Point2
            if (nodeTo.y() < centerY) {
                index2 += 4;
            }
            if (nodeTo.z() > centerZ) {
                index2 += 2;
            }
            if (nodeTo.x() < centerX) {
                index2 += 1;
            }
        }

        if (index1 >= 0 && index2 >= 0) {
            if (index1 != index2) {
                return new int[]{index1, index2};
            } else {
                return new int[]{index1};
            }
        } else if (index1 >= 0) {
            return new int[]{index1};
        } else if (index2 >= 0) {
            return new int[]{index2};
        } else {
            return new int[]{};
        }
    }

    @Override
    public boolean isInOctreeLeaf(Octant leaf) {
        NodeData nodeFrom = obj.getSource();
        NodeData nodeTo = obj.getTarget();

        if (octants[0] == leaf) {
            if (octants[0] != ((ModelImpl) nodeFrom.getModel()).getOctants()[0]) //0 = nodeFrom
            {
                return false;
            }
        } else {
            if (octants[1] != ((ModelImpl) nodeTo.getModel()).getOctants()[0]) //1 = nodeTo
            {
                return false;
            }
        }

        return true;
    }

    @Override
    public void display(GL gl, GLU glu, VizModel vizModel) {
        if (this.arrow != null) {
            this.arrow.setSelected(selected);
        }
        if (!selected && vizModel.isHideNonSelectedEdges()) {
            return;
        }
        if (selected && vizModel.isAutoSelectNeighbor()) {
            ModelImpl m1 = (ModelImpl) obj.getSource().getModel();
            ModelImpl m2 = (ModelImpl) obj.getTarget().getModel();
            m1.mark = true;
            m2.mark = true;
        }
        float x1 = obj.getSource().x();
        float x2 = obj.getTarget().x();
        float y1 = obj.getSource().y();
        float y2 = obj.getTarget().y();
        float t1 = obj.getEdge().getWeight() / WEIGHT_DIV;
        float t2 = obj.getEdge().getWeight() / WEIGHT_DIV;

        float sideVectorX = y1 - y2;
        float sideVectorY = x2 - x1;
        float norm = (float) Math.sqrt(sideVectorX * sideVectorX + sideVectorY * sideVectorY);
        sideVectorX /= norm;
        sideVectorY /= norm;

        float x1Thick = sideVectorX / 2f * t1;
        float x2Thick = sideVectorX / 2f * t2;
        float y1Thick = sideVectorY / 2f * t1;
        float y2Thick = sideVectorY / 2f * t2;

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
                    g = 0.498f *obj.g();
                    b = 0.498f *obj.b();
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

        gl.glVertex2f(x1 + x1Thick, y1 + y1Thick);
        gl.glVertex2f(x1 - x1Thick, y1 - y1Thick);
        gl.glVertex2f(x2 - x2Thick, y2 - y2Thick);
        gl.glVertex2f(x2 - x2Thick, y2 - y2Thick);
        gl.glVertex2f(x2 + x2Thick, y2 + y2Thick);
        gl.glVertex2f(x1 + x1Thick, y1 + y1Thick);
    }

    @Override
    public boolean selectionTest(Vecf distanceFromMouse, float selectionSize) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public float getCollisionDistance(double angle) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float getCameraDistance() {
        return (obj.getSource().getModel().getCameraDistance() + obj.getTarget().getModel().getCameraDistance()) / 2f;
    }

    @Override
    public boolean isAutoSelected() {
        return obj.getSource().getModel().isSelected() || obj.getTarget().getModel().isSelected();
    }

    @Override
    public boolean onlyAutoSelect() {
        return true;
    }

    @Override
    public boolean isSelected() {
        return selected;
    }

    @Override
    public String toSVG() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setOctant(Octant octant) {
        if (((ModelImpl) obj.getSource().getModel()).getOctants()[0] == octant) {
            octants[0] = octant;
        } else {
            octants[1] = octant;
        }
    }

    @Override
    public void resetOctant() {
        /*if(octants[0]==octant)
        {
        octants[0] = null;
        }
        if(octants[1]==octant)
        {
        octants[1] = null;
        }*/
        octants[0] = null;
        octants[1] = null;
    }

    @Override
    public boolean isValid() {
        return octants[0] != null || octants[1] != null;
    }

    public ModelImpl getArrow() {
        return arrow;
    }

    public void setArrow(ModelImpl arrow) {
        this.arrow = arrow;
    }

    @Override
    public float getViewportX() {
        return (obj.getSource().getModel().getViewportX() + 2 * obj.getTarget().getModel().getViewportX()) / 3f;
    }

    @Override
    public float getViewportY() {
        return (obj.getSource().getModel().getViewportY() + 2 * obj.getTarget().getModel().getViewportY()) / 3f;
    }
}
