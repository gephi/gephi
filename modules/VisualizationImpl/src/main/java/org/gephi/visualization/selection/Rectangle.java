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
package org.gephi.visualization.selection;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;
import java.util.Arrays;
import org.gephi.lib.gleem.linalg.Vecf;
import org.gephi.visualization.VizController;
import org.gephi.visualization.api.selection.SelectionArea;
import org.gephi.visualization.apiimpl.GraphDrawable;
import org.gephi.visualization.apiimpl.VizConfig;
import org.gephi.visualization.model.node.NodeModel;

/**
 *
 * @author Mathieu Bastian
 */
public class Rectangle implements SelectionArea {

    private static final float[] POINT_RECT = {1, 1};
    private final GraphDrawable drawable;
    private final VizConfig config;
    private final float[] color;
    //Variables
    private float[] startPosition;
    private float[] startPosition3d;
    private final float[] rectangle = new float[2];
    private final float[] rectangle3d = new float[2];
    private final float[] center = new float[2];
    private final float[] rectangleSize = new float[2];
    private boolean stop = true;
    private boolean blocking = true;
    private boolean ctrl = false;

    public Rectangle() {
        drawable = VizController.getInstance().getDrawable();
        config = VizController.getInstance().getVizConfig();
        color = config.getRectangleSelectionColor().getRGBComponents(null);
    }

    @Override
    public float[] getSelectionAreaRectancle() {
        if (stop) {
            return POINT_RECT;
        }
        rectangleSize[0] = Math.abs(rectangle[0] - startPosition[0]);
        rectangleSize[1] = Math.abs(rectangle[1] - startPosition[1]);
        if (rectangleSize[0] < 1f) {
            rectangleSize[0] = 1f;
        }
        if (rectangleSize[1] < 1f) {
            rectangleSize[1] = 1f;
        }
        return rectangleSize;
    }

    @Override
    public float[] getSelectionAreaCenter() {
        if (stop) {
            return null;
        }
        center[0] = -(rectangle[0] - startPosition[0]) / 2f;
        center[1] = -(rectangle[1] - startPosition[1]) / 2f;
        return center;
    }

    @Override
    public boolean mouseTest(Vecf distanceFromMouse, NodeModel nodeModel) {
        if (stop) {
            return nodeModel.selectionTest(distanceFromMouse, 0);
        }
        
        float x = nodeModel.getX();
        float y = nodeModel.getY();
        float rad = nodeModel.getNode().size();

        boolean res = true;
        if (startPosition3d[0] > rectangle3d[0]) {
            if (x - rad > startPosition3d[0] || x + rad < rectangle3d[0]) {
                res = false;
            }
        } else if (x + rad < startPosition3d[0] || x - rad > rectangle3d[0]) {
            res = false;
        }
        if (startPosition3d[1] < rectangle3d[1]) {
            if (y + rad < startPosition3d[1] || y - rad > rectangle3d[1]) {
                res = false;
            }
        } else if (y - rad > startPosition3d[1] || y + rad < rectangle3d[1]) {
            res = false;
        }
        return res;
    }

    public void start(float[] mousePosition, float[] mousePosition3d) {
        this.startPosition = Arrays.copyOf(mousePosition, 2);
        this.startPosition3d = Arrays.copyOf(mousePosition3d, 2);
        this.rectangle[0] = startPosition[0];
        this.rectangle[1] = startPosition[1];
        this.rectangle3d[0] = startPosition3d[0];
        this.rectangle3d[1] = startPosition3d[1];
        stop = false;
        blocking = false;
    }

    public void stop() {
        stop = true;
        blocking = true;
    }

    public void setMousePosition(float[] mousePosition, float[] mousePosition3d) {
        if (!stop) {
            rectangle[0] = mousePosition[0];
            rectangle[1] = mousePosition[1];
            rectangle3d[0] = mousePosition3d[0];
            rectangle3d[1] = mousePosition3d[1];
        }
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean blockSelection() {
        return blocking;
    }

    public void setBlocking(boolean blocking) {
        this.blocking = blocking;
    }

    @Override
    public void drawArea(GL2 gl, GLU glu) {
        if (!stop) {
            float x = startPosition[0];
            float y = startPosition[1];
            float w = rectangle[0] - startPosition[0];
            float h = rectangle[1] - startPosition[1];

            gl.glMatrixMode(GL2.GL_PROJECTION);
            gl.glPushMatrix();
            gl.glLoadIdentity();
            glu.gluOrtho2D(0, drawable.getViewportWidth(), 0, drawable.getViewportHeight());
            gl.glMatrixMode(GL2.GL_MODELVIEW);
            gl.glPushMatrix();
            gl.glLoadIdentity();

            gl.glColor4f(color[0], color[1], color[2], color[3]);

            gl.glBegin(GL2.GL_QUADS);
            gl.glVertex3f(x + w, y, 0);
            gl.glVertex3f(x, y, 0);
            gl.glVertex3f(x, y + h, 0);
            gl.glVertex3f(x + w, y + h, 0);
            gl.glEnd();

            gl.glColor4f(color[0], color[1], color[2], 1f);
            gl.glBegin(GL2.GL_LINE_LOOP);
            gl.glVertex3f(x + w, y, 0);
            gl.glVertex3f(x, y, 0);
            gl.glVertex3f(x, y + h, 0);
            gl.glVertex3f(x + w, y + h, 0);
            gl.glEnd();

            gl.glPopMatrix();
            gl.glMatrixMode(GL2.GL_PROJECTION);
            gl.glPopMatrix();
            gl.glMatrixMode(GL2.GL_MODELVIEW);
        } else {
            startPosition = null;
        }
    }

    public boolean isStop() {
        return stop;
    }

    public void setCtrl(boolean ctrl) {
        this.ctrl = ctrl;
    }

    public boolean isCtrl() {
        return ctrl;
    }
}
