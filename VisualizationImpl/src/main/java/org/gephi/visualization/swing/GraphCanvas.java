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
package org.gephi.visualization.swing;

import com.sun.opengl.util.GLUT;
import java.awt.Component;

import java.awt.Cursor;
import java.awt.Dimension;
import java.text.NumberFormat;
import javax.media.opengl.GL;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.glu.GLU;
import javax.swing.JPopupMenu;
import javax.swing.ToolTipManager;

/**
 *
 * @author Mathieu Bastian
 */
public class GraphCanvas extends GraphDrawableImpl {

    private GLCanvas glCanvas;
    private GLUT glut = new GLUT();
    private NumberFormat formatter;

    public GraphCanvas() {
        super();
        formatter = NumberFormat.getNumberInstance();
        formatter.setMaximumFractionDigits(1);
        glCanvas = new GLCanvas(getCaps());
        super.initDrawable(glCanvas);
        glCanvas.setMinimumSize(new Dimension(0, 0));   //Fix Canvas resize Issue

        //Basic init
        graphComponent = (Component) glCanvas;
        graphComponent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        //False lets the components appear on top of the canvas
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
    }

    @Override
    protected void render3DScene(GL gl, GLU glu) {
        if (vizController.getVizConfig().isShowFPS()) {
            gl.glPushMatrix();
            gl.glLoadIdentity();
            gl.glMatrixMode(gl.GL_PROJECTION);
            gl.glPushMatrix();
            gl.glLoadIdentity();

            gl.glGetIntegerv(gl.GL_VIEWPORT, viewport);
            glu.gluOrtho2D(0, viewport.get(2), viewport.get(3), 0);
            gl.glDepthFunc(gl.GL_ALWAYS);
            gl.glColor3i(192, 192, 192);
            gl.glRasterPos2f(10, 15);
            String fpsRound = formatter.format(fps);
            glut.glutBitmapString(GLUT.BITMAP_HELVETICA_10, fpsRound);

            gl.glDepthFunc(gl.GL_LESS);
            gl.glPopMatrix();
            gl.glMatrixMode(gl.GL_MODELVIEW);
            gl.glPopMatrix();
        }
        super.render3DScene(gl, glu);
    }
}
