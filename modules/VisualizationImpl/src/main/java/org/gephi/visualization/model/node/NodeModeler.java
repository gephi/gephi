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

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUquadric;
import org.gephi.graph.api.Node;
import org.gephi.visualization.model.Model;
import org.gephi.visualization.model.Modeler;
import org.gephi.visualization.opengl.CompatibilityEngine;

/**
 *
 * @author Mathieu Bastian
 */
public class NodeModeler extends Modeler {

    public int SHAPE_DIAMOND;
    public int SHAPE_DISK16;
    public int SHAPE_DISK32;
    public int SHAPE_DISK64;
    public int BORDER16;
    public int BORDER32;
    public int BORDER64;

    public NodeModeler(CompatibilityEngine engine) {
        super(engine);
    }

    public NodeModel initModel(Node n) {
        NodeDiskModel obj = new NodeDiskModel((Node) n);
        obj.modelType = SHAPE_DISK64;
        obj.modelBorderType = BORDER64;

        chooseModel(obj);
        return obj;
    }

    @Override
    public void chooseModel(Model object3d) {
        NodeDiskModel obj = (NodeDiskModel) object3d;
        if (config.isDisableLOD()) {
            obj.modelType = SHAPE_DISK64;
            obj.modelBorderType = BORDER64;
            return;
        }

        float distance = cameraDistance(obj) / (obj.getNode().size() * drawable.getGlobalScale());
        if (distance > 600) {
            obj.modelType = SHAPE_DIAMOND;
            obj.modelBorderType = -1;
        } else if (distance > 50) {
            obj.modelType = SHAPE_DISK16;
            obj.modelBorderType = BORDER16;
        } else {
            obj.modelType = SHAPE_DISK32;
            obj.modelBorderType = BORDER32;
        }
    }

    @Override
    public int initDisplayLists(GL2 gl, GLU glu, GLUquadric quadric, int ptr) {
        // Diamond display list
        SHAPE_DIAMOND = ptr + 1;
        gl.glNewList(SHAPE_DIAMOND, GL2.GL_COMPILE);
        glu.gluDisk(quadric, 0, 0.5, 4, 1);
        gl.glEndList();
        //End

        //Disk16
        SHAPE_DISK16 = SHAPE_DIAMOND + 1;
        gl.glNewList(SHAPE_DISK16, GL2.GL_COMPILE);
        glu.gluDisk(quadric, 0, 0.5, 6, 1);
        gl.glEndList();
        //Fin

        //Disk32
        SHAPE_DISK32 = SHAPE_DISK16 + 1;
        gl.glNewList(SHAPE_DISK32, GL2.GL_COMPILE);
        glu.gluDisk(quadric, 0, 0.5, 12, 2);
        gl.glEndList();

        //Disk64
        SHAPE_DISK64 = SHAPE_DISK32 + 1;
        gl.glNewList(SHAPE_DISK64, GL2.GL_COMPILE);
        glu.gluDisk(quadric, 0, 0.5, 32, 4);
        gl.glEndList();

        //Border16
        BORDER16 = SHAPE_DISK64 + 1;
        gl.glNewList(BORDER16, GL2.GL_COMPILE);
        glu.gluDisk(quadric, 0.42, 0.50, 24, 2);
        gl.glEndList();

        //Border32
        BORDER32 = BORDER16 + 1;
        gl.glNewList(BORDER32, GL2.GL_COMPILE);
        glu.gluDisk(quadric, 0.42, 0.50, 48, 2);
        gl.glEndList();

        //Border32
        BORDER64 = BORDER32 + 1;
        gl.glNewList(BORDER64, GL2.GL_COMPILE);
        glu.gluDisk(quadric, 0.42, 0.50, 96, 4);
        gl.glEndList();

        return BORDER64;
    }

    @Override
    public void beforeDisplay(GL2 gl, GLU glu) {
    }

    @Override
    public void afterDisplay(GL2 gl, GLU glu) {
    }

    protected float cameraDistance(NodeModel object) {
        float[] cameraLocation = drawable.getCameraLocation();
        double distance = Math.sqrt(Math.pow((double) object.getNode().x() - cameraLocation[0], 2d)
                + Math.pow((double) object.getNode().y() - cameraLocation[1], 2d)
                + Math.pow((double) object.getNode().z() - cameraLocation[2], 2d));
        object.setCameraDistance((float) distance);

        return (float) distance;
    }

    public boolean isLod() {
        return true;
    }

    public boolean isSelectable() {
        return true;
    }

    public boolean isClickable() {
        return true;
    }

    public boolean isOnlyAutoSelect() {
        return false;
    }
}
