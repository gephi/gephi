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
