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
package org.gephi.visualization.model.node;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;
import org.gephi.graph.api.Node;
import org.gephi.visualization.model.Model;
import org.gephi.visualization.opengl.CompatibilityEngine;
import org.openide.util.NbBundle;

/**
 * Default initializer for the nodes. The class draw sphere objects and manage a
 * LOD system.
 *
 * @author Mathieu Bastian
 * @see NodeSphereModel
 */
public class NodeSphereModeler extends NodeModeler {

    public int SHAPE_DIAMOND;
    public int SHAPE_SPHERE16;
    public int SHAPE_SPHERE32;
    public int SHAPE_SPHERE64;
    public int SHAPE_BILLBOARD;

    public NodeSphereModeler(CompatibilityEngine engine) {
        super(engine);
    }

    @Override
    public NodeModel initModel(Node n) {
        NodeSphereModel obj = new NodeSphereModel(n);
        obj.modelType = SHAPE_SPHERE64;

        chooseModel(obj);

        return obj;
    }

    @Override
    public void chooseModel(Model object3d) {
        NodeSphereModel obj = (NodeSphereModel) object3d;
        if (config.isDisableLOD()) {
            obj.modelType = SHAPE_SPHERE64;
            return;
        }

        float distance = cameraDistance(obj) / obj.getNode().size();
        if (distance > 600) {
            obj.modelType = SHAPE_DIAMOND;
        } else if (distance > 50) {
            obj.modelType = SHAPE_SPHERE16;
        } else {
            obj.modelType = SHAPE_SPHERE32;
        }
    }

    @Override
    public int initDisplayLists(GL2 gl, GLU glu, GLUquadric quadric, int ptr) {
        // Diamond display list
        SHAPE_DIAMOND = ptr + 1;
        gl.glNewList(SHAPE_DIAMOND, GL2.GL_COMPILE);
        glu.gluSphere(quadric, 0.5f, 4, 2);
        gl.glEndList();
        //End

        // Sphere16 display list
        SHAPE_SPHERE16 = SHAPE_DIAMOND + 1;
        gl.glNewList(SHAPE_SPHERE16, GL2.GL_COMPILE);
        gl.glCallList(ptr);
        glu.gluSphere(quadric, 0.5f, 16, 8);
        gl.glEndList();
        //Fin


        // Sphere32 display list
        SHAPE_SPHERE32 = SHAPE_SPHERE16 + 1;
        gl.glNewList(SHAPE_SPHERE32, GL2.GL_COMPILE);
        gl.glCallList(ptr);
        glu.gluSphere(quadric, 0.5f, 32, 16);
        gl.glEndList();

        // Sphere32 display list
        SHAPE_SPHERE64 = SHAPE_SPHERE32 + 1;
        gl.glNewList(SHAPE_SPHERE64, GL2.GL_COMPILE);
        gl.glCallList(ptr);
        glu.gluSphere(quadric, 0.5f, 64, 32);
        gl.glEndList();

        return SHAPE_SPHERE64;
    }

    @Override
    public void beforeDisplay(GL2 gl, GLU glu) {
    }

    @Override
    public void afterDisplay(GL2 gl, GLU glu) {
    }

    @Override
    public boolean is3d() {
        return true;
    }

    @Override
    public String toString() {
        return NbBundle.getMessage(NodeSphereModeler.class, "nodeModeler_sphere");
    }
}
