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
import org.gephi.datastructure.avl.param.AVLItemAccessor;
import org.gephi.datastructure.avl.param.ParamAVLTree;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Potato;
import org.gephi.visualization.api.Object3dImpl;
import org.gephi.visualization.gleem.linalg.Vecf;
import org.gephi.visualization.opengl.octree.Octant;

/**
 *
 * @author Mathieu Bastian
 */
public class Potato3dObject extends Object3dImpl<Potato> {

    public int modelType;
    private ParamAVLTree<Octant> octantsTree;
    protected boolean underMouse = false;

    public Potato3dObject(Potato potato) {

        octantsTree = new ParamAVLTree<Octant>(new AVLItemAccessor<Octant>() {

            public int getNumber(Octant item) {
                return item.getNumber();
            }
        });

        potato.updatePotato();
    }

    @Override
    public int[] octreePosition(float centerX, float centerY, float centerZ, float size) {
        throw new UnsupportedOperationException("Not needed");
    }

    @Override
    public boolean isInOctreeLeaf(Octant leaf) {
        for (Node node : obj.getContent()) {
            Object3dImpl objImpl = (Object3dImpl) node.getObject3d();
            Octant o = objImpl.getOctants()[0];
            if (o == leaf) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void display(GL gl, GLU glu) {

        //Disks
        if (mark && obj.getDisks() != null) {
            if (selected) {
                gl.glColor3f(0.9f, 0.9f, 0.9f);
            } else {
                gl.glColor3f(obj.r(), obj.g(), obj.b());
            }
            for (float[] disk : obj.getDisks()) {
                gl.glPushMatrix();
                float size = disk[2];
                gl.glTranslatef(disk[0], disk[1], obj.z());
                gl.glScalef(size, size, size);
                gl.glCallList(modelType);
                gl.glPopMatrix();
            }
        }

        //Triangles
        if (!mark && obj.getTriangles() != null) {
            if (selected) {
                gl.glColor3f(0.9f, 0.9f, 0.9f);
            } else {
                gl.glColor3f(obj.r(), obj.g(), obj.b());
            }
            for (float[] triangle : obj.getTriangles()) {
                gl.glVertex3f(triangle[0], triangle[1], obj.z());
                gl.glVertex3f(triangle[2], triangle[3], obj.z());
                gl.glVertex3f(triangle[4], triangle[5], obj.z());
            }
        }
    }

    @Override
    public boolean selectionTest(Vecf distanceFromMouse, float selectionSize) {
        if (underMouse) {
            for (Node n : obj.getContent()) {
                if (n.getObject3d().isSelected()) {
                    return false;
                }
            }

            for(Potato p : obj.getInnerPotatoes())
            {
                Potato3dObject po = (Potato3dObject)p.getObject3d();
                if(po.underMouse)
                    return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public float getCollisionDistance(double angle) {
        return 0;
    }

    @Override
    public String toSVG() {
        return null;
    }

    @Override
    public Octant[] getOctants() {
        if (octants == null) {
            //Recompute octant tree
            computeOctantsTree();
        }
        return octants;
    }

    @Override
    public void resetOctant() {
        octants = null;
    }

    private void computeOctantsTree() {
        octantsTree.clear();

        //Compute
        for (Node node : obj.getContent()) {
            Object3dImpl objImpl = (Object3dImpl) node.getObject3d();
            Octant o = objImpl.getOctants()[0];
            octantsTree.add(o);
        }

        octants = octantsTree.toArray(new Octant[0]);
    }

    public void setUnderMouse(boolean underMouse) {
        this.underMouse = underMouse;
    }

    public boolean isParentUnderMouse() {
        if (obj.getParent() != null && ((Potato3dObject) obj.getParent().getObject3d()).underMouse) {
            return true;
        }
        return false;
    }
}
