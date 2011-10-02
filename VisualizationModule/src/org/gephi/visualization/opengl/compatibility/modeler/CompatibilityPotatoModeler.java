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
package org.gephi.visualization.opengl.compatibility.modeler;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;
import javax.swing.JPanel;

import org.gephi.graph.api.NodeData;
import org.gephi.graph.api.Renderable;
import org.gephi.visualization.apiimpl.ModelImpl;
import org.gephi.visualization.api.initializer.CompatibilityModeler;

/**
 *
 * @author Mathieu Bastian
 */
public class CompatibilityPotatoModeler implements CompatibilityModeler<NodeData> {

    public int DISK_LOW;
    public int DISK_HIGH;

    public ModelImpl initModel(Renderable n) {
        /*Potato potato = (Potato)n;

        Potato3dObject obj = new Potato3dObject(potato);
        obj.modelType = DISK_HIGH;
        obj.setObj(potato);
        potato.setObject3d(obj);*/

        return null;
    }

    public void chooseModel(ModelImpl<NodeData> obj) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int initDisplayLists(GL gl, GLU glu, GLUquadric quadric, int ptr) {

        //Low res disk
        DISK_LOW = ptr + 1;
        gl.glNewList(DISK_LOW, GL.GL_COMPILE);
        gl.glDisable(GL.GL_LIGHTING);
        glu.gluDisk(quadric, 0.0, 1.0, 8, 1);
        gl.glEnable(GL.GL_LIGHTING);
        gl.glEndList();
        //End

        //High res disk
        DISK_HIGH = DISK_LOW + 1;
        gl.glNewList(DISK_HIGH, GL.GL_COMPILE);
        gl.glDisable(GL.GL_LIGHTING);
        glu.gluDisk(quadric, 0.0, 1.0, 32, 2);
        gl.glEnable(GL.GL_LIGHTING);
        gl.glEndList();
        //End

        return DISK_HIGH;
    }

    public void beforeDisplay(GL gl, GLU glu) {
    }

    public void afterDisplay(GL gl, GLU glu) {
    }

    public void initFromOpenGLThread() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public JPanel getPanel() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
