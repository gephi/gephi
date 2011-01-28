/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.gephi.visualization.opengl.compatibility.objects;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import org.gephi.utils.collection.avl.AVLItemAccessor;
import org.gephi.utils.collection.avl.ParamAVLTree;

import org.gephi.graph.api.NodeData;
import org.gephi.visualization.VizModel;
import org.gephi.visualization.apiimpl.ModelImpl;
import org.gephi.lib.gleem.linalg.Vecf;
import org.gephi.visualization.opengl.octree.Octant;

/**
 *
 * @author Mathieu Bastian
 */
public class Potato3dModel extends ModelImpl<NodeData> {

    public int modelType;
    private ParamAVLTree<Octant> octantsTree;
    protected boolean underMouse = false;
    //protected PotatoDisplay display = null;

    public Potato3dModel() {

        octantsTree = new ParamAVLTree<Octant>(new AVLItemAccessor<Octant>() {

            public int getNumber(Octant item) {
                return item.getNumber();
            }
        });

    //potato.updatePotato();
    }

    @Override
    public int[] octreePosition(float centerX, float centerY, float centerZ, float size) {
        throw new UnsupportedOperationException("Not needed");
    }

    @Override
    public boolean isInOctreeLeaf(Octant leaf) {
        /* for (Node node : obj.getContent()) {
        ModelImpl objImpl = (ModelImpl) node.getObject3d();
        Octant o = objImpl.getOctants()[0];
        if (o == leaf) {
        return true;
        }
        }*/
        return false;
    }

    @Override
    public void display(GL gl, GLU glu, VizModel model) {
        /*if (mark) {
        this.display = obj.getDisplay();
        }

        if (display != null) {
        //Disks
        if (mark) {
        if (selected) {
        gl.glColor3f(0.9f, 0.9f, 0.9f);
        } else {
        gl.glColor3f(obj.r(), obj.g(), obj.b());
        }
        for (float[] disk : display.getDisks()) {
        gl.glPushMatrix();
        float size = disk[2];
        gl.glTranslatef(disk[0], disk[1], obj.z());
        gl.glScalef(size, size, size);
        gl.glCallList(modelType);
        gl.glPopMatrix();
        }
        }

        //Triangles
        if (!mark) {
        if (selected) {
        gl.glColor3f(0.9f, 0.9f, 0.9f);
        } else {
        gl.glColor3f(obj.r(), obj.g(), obj.b());
        }
        for (float[] triangle : display.getTriangles()) {
        gl.glVertex3f(triangle[0], triangle[1], obj.z());
        gl.glVertex3f(triangle[2], triangle[3], obj.z());
        gl.glVertex3f(triangle[4], triangle[5], obj.z());
        }
        }
        }*/
    }

    @Override
    public boolean selectionTest(Vecf distanceFromMouse, float selectionSize) {
        /*if (underMouse) {
        for (Node n : obj.getContent()) {
        if (n.getObject3d().isSelected()) {
        return false;
        }
        }

        for (Potato p : obj.getInnerPotatoes()) {
        Potato3dModel po = (Potato3dModel) p.getObject3d();
        if (po.underMouse) {
        return false;
        }
        }
        return true;
        }*/
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
       /* for (Node node : obj.getContent()) {
        ModelImpl objImpl = (ModelImpl) node.getObject3d();
        Octant o = objImpl.getOctants()[0];
        octantsTree.add(o);
        }
         */
        octants = octantsTree.toArray(new Octant[0]);
    }

    public void setUnderMouse(boolean underMouse) {
        this.underMouse = underMouse;
    }

    public boolean isParentUnderMouse() {
        /* if (obj.getParent() != null && ((Potato3dModel) obj.getParent().getObject3d()).underMouse) {
        return true;
        }*/
        return false;
    }

    public boolean isDisplayReady() {
        //return display != null;
        return false;
    }
}
