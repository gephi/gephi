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

import org.gephi.visualization.api.initializer.CompatibilityModeler;
import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;
import javax.swing.JPanel;
import org.gephi.graph.api.EdgeData;
import org.gephi.graph.api.NodeData;
import org.gephi.graph.api.Renderable;
import org.gephi.visualization.VizController;
import org.gephi.visualization.opengl.AbstractEngine;
import org.gephi.visualization.apiimpl.ModelImpl;
import org.gephi.visualization.apiimpl.VizConfig;
import org.gephi.visualization.opengl.compatibility.CompatibilityEngine;
import org.gephi.visualization.opengl.compatibility.objects.Arrow2dModel;
import org.gephi.visualization.opengl.compatibility.objects.Arrow3dModel;

/**
 *
 * @author Mathieu Bastian
 */
public class CompatibilityArrowModeler implements CompatibilityModeler<NodeData> {

    private CompatibilityEngine engine;
    private VizController controller;

    public CompatibilityArrowModeler(AbstractEngine engine) {
        this.engine = (CompatibilityEngine) engine;
        this.controller = VizController.getInstance();
    }

    @Override
    public ModelImpl initModel(Renderable n) {
        EdgeData e = (EdgeData) n;
        Arrow2dModel arrow;
        if (controller.getVizModel().isUse3d()) {
            arrow = new Arrow3dModel(e);
        } else {
            arrow = new Arrow2dModel(e);
        }
        arrow.setObj(e.getTarget());

        return arrow;
    }

    public void chooseModel(ModelImpl obj) {
        float distance = engine.cameraDistance(obj) + obj.getObj().getRadius();	//Radius is added to cancel the cameraDistance diff
        if (distance < 100) {
            obj.mark = false;
        } else {
            obj.mark = true;
        }
    }

    public void beforeDisplay(GL gl, GLU glu) {
        gl.glBegin(GL.GL_TRIANGLES);
    }

    public void afterDisplay(GL gl, GLU glu) {
        gl.glEnd();
    }

    public int initDisplayLists(GL gl, GLU glu, GLUquadric quadric, int ptr) {
        return ptr;
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
