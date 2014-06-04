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

import java.awt.Cursor;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import javax.swing.SwingUtilities;
import org.gephi.lib.gleem.linalg.MathUtil;
import org.gephi.lib.gleem.linalg.Vec3f;
import org.gephi.visualization.GraphLimits;
import org.gephi.visualization.VizArchitecture;
import org.gephi.visualization.VizController;
import org.gephi.visualization.api.selection.SelectionManager;
import org.gephi.visualization.apiimpl.GraphContextMenu;
import org.gephi.visualization.apiimpl.GraphIO;
import org.gephi.visualization.apiimpl.VizEventManager;
import org.gephi.visualization.opengl.AbstractEngine;
import org.gephi.visualization.selection.Rectangle;

/**
 *
 * @author Mathieu Bastian
 */
public class StandardGraphIO implements GraphIO, VizArchitecture {

    //Architecture
    protected GraphDrawableImpl graphDrawable;
    protected AbstractEngine engine;
    protected VizEventManager vizEventManager;
    protected VizController vizController;
    protected GraphLimits limits;
    //Listeners data
    protected float[] rightButtonMoving = {-1f, 0f, 0f};
    protected float[] leftButtonMoving = {-1f, 0f, 0f};
    protected float[] middleButtonMoving = {-1f, 0f, 0f};
    protected float[] mousePosition = new float[2];
    protected float[] mouseDrag3d = new float[2];
    protected float[] mouseDrag = new float[2];
    protected float[] startDrag2d = new float[2];
    //Flags
    protected boolean draggingEnable = true;
    protected boolean dragging = false;
    protected boolean pressing = false;

    @Override
    public void initArchitecture() {
        this.graphDrawable = VizController.getInstance().getDrawable();
        this.engine = VizController.getInstance().getEngine();
        this.vizEventManager = VizController.getInstance().getVizEventManager();
        this.vizController = VizController.getInstance();
        this.limits = VizController.getInstance().getLimits();
    }

    @Override
    public void startMouseListening() {
        stopMouseListening();
        if (vizController.getVizConfig().isCameraControlEnable()) {
            graphDrawable.graphComponent.addMouseListener(this);
            graphDrawable.graphComponent.addMouseWheelListener(this);
        }

        if (vizController.getVizConfig().isSelectionEnable()) {
            graphDrawable.graphComponent.addMouseMotionListener(this);
        }

    }

    @Override
    public void stopMouseListening() {
        graphDrawable.graphComponent.removeMouseListener(this);
        graphDrawable.graphComponent.removeMouseMotionListener(this);
        graphDrawable.graphComponent.removeMouseWheelListener(this);
    }

    @Override
    public void mousePressed(MouseEvent e) {

        if (!graphDrawable.getGraphComponent().isShowing()) {
            return;
        }

        float x = e.getLocationOnScreen().x - graphDrawable.graphComponent.getLocationOnScreen().x;
        float y = e.getLocationOnScreen().y - graphDrawable.graphComponent.getLocationOnScreen().y;

        if (SwingUtilities.isRightMouseButton(e)) {
            //Save the coordinate of the start
            rightButtonMoving[0] = x;
            rightButtonMoving[1] = y;
            graphDrawable.graphComponent.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            vizEventManager.mouseRightPress();
        } else if (vizController.getVizModel().isRotatingEnable() && SwingUtilities.isMiddleMouseButton(e)) {
            middleButtonMoving[0] = x;
            middleButtonMoving[1] = y;
            graphDrawable.graphComponent.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            vizEventManager.mouseMiddlePress();
        } else if (SwingUtilities.isLeftMouseButton(e)) {
            leftButtonMoving[0] = x;
            leftButtonMoving[1] = y;
            pressing = true;
            vizEventManager.mouseLeftPress();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        //Disable the right button moving
        rightButtonMoving[0] = -1;
        leftButtonMoving[0] = -1;
        middleButtonMoving[0] = -1;

        //Update mouse position because the movement during dragging
        if (graphDrawable.getGraphComponent().isShowing()) {

            float x = e.getLocationOnScreen().x - graphDrawable.graphComponent.getLocationOnScreen().x;
            float y = e.getLocationOnScreen().y - graphDrawable.graphComponent.getLocationOnScreen().y;
            mousePosition[0] = x;
            mousePosition[1] = graphDrawable.viewport.get(3) - y;
        }

        if (dragging) {
            dragging = false;
            engine.getScheduler().requireStopDrag();
            vizEventManager.stopDrag();
        } else {
            graphDrawable.graphComponent.setCursor(Cursor.getDefaultCursor());
        }

        vizEventManager.mouseReleased();
        if (pressing) {
            pressing = false;
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        dragging = false;
        /*if (!engine.getScheduler().isAnimating()) {
         engine.getScheduler().start();
         }*/
    }

    @Override
    public void mouseExited(MouseEvent e) {
        if (!dragging) {
            //engine.getScheduler().stop();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {

        if (!graphDrawable.getGraphComponent().isShowing()) {
            return;
        }

        float x = e.getLocationOnScreen().x - graphDrawable.graphComponent.getLocationOnScreen().x;
        float y = e.getLocationOnScreen().y - graphDrawable.graphComponent.getLocationOnScreen().y;
        mousePosition[0] = x;
        mousePosition[1] = graphDrawable.viewport.get(3) - y;

        engine.getScheduler().requireUpdateSelection();
        vizEventManager.mouseMove();
    }

    /**
     * Mouse clicked event.
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            if (vizController.getVizConfig().isSelectionEnable() && engine.isRectangleSelection()) {
                Rectangle r = (Rectangle) engine.getCurrentSelectionArea();
                boolean ctrl = (e.getModifiers() & InputEvent.CTRL_DOWN_MASK) != 0 || (e.getModifiers() & InputEvent.CTRL_MASK) != 0;
                r.setCtrl(ctrl);
            }
            engine.getScheduler().requireMouseClick();
            vizEventManager.mouseLeftClick();
        } else if (SwingUtilities.isRightMouseButton(e)) {
            if (vizController.getVizConfig().isContextMenu()) {
                GraphContextMenu popupMenu = new GraphContextMenu();
//                popupMenu.getMenu().show(graphDrawable.getGraphComponent(), (int) mousePosition[0], (int) (graphDrawable.viewport.get(3) - mousePosition[1]));
            }
            vizEventManager.mouseRightClick();
        } else if (SwingUtilities.isMiddleMouseButton(e)) {
            vizEventManager.mouseMiddleClick();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {

        if (!graphDrawable.getGraphComponent().isShowing()) {
            return;
        }

        float x = e.getLocationOnScreen().x - graphDrawable.graphComponent.getLocationOnScreen().x;//TODO Pourqoui ce osnt des float et pas des int
        float y = e.getLocationOnScreen().y - graphDrawable.graphComponent.getLocationOnScreen().y;

        if (rightButtonMoving[0] != -1) {
            //The right button is pressed
            float proche = graphDrawable.cameraTarget[2] - graphDrawable.cameraLocation[2];
            proche = proche / 300;

            graphDrawable.cameraTarget[0] += (x - rightButtonMoving[0]) * proche;
            graphDrawable.cameraTarget[1] += (rightButtonMoving[1] - y) * proche;
            graphDrawable.cameraLocation[0] += (x - rightButtonMoving[0]) * proche;
            graphDrawable.cameraLocation[1] += (rightButtonMoving[1] - y) * proche;

            rightButtonMoving[0] = x;
            rightButtonMoving[1] = y;
            engine.getScheduler().requireUpdateVisible();
        }

        if (middleButtonMoving[0] != -1) {
            //The middle button is pressed
            float angleY = y - middleButtonMoving[1];
            if (angleY > 0 || (graphDrawable.cameraTarget[1] - graphDrawable.cameraLocation[1] > 0)) {
                middleButtonMoving[1] = y;

                graphDrawable.cameraLocation[1] = graphDrawable.cameraLocation[1] - Math.abs(graphDrawable.cameraLocation[2] - graphDrawable.cameraTarget[2]) * (float) Math.sin(Math.toRadians(angleY));
                engine.getScheduler().requireUpdateVisible();
            }
        }

        if (leftButtonMoving[0] != -1) {

            //Remet à jour aussi la mousePosition pendant le drag, notamment pour coller quand drag released
            mousePosition[0] = x;
            mousePosition[1] = graphDrawable.viewport.get(3) - y;

            mouseDrag3d[0] = (float) ((graphDrawable.viewport.get(2) / 2 - x) / graphDrawable.draggingMarker[0] + graphDrawable.cameraTarget[0]);
            mouseDrag3d[1] = (float) ((y - graphDrawable.viewport.get(3) / 2) / graphDrawable.draggingMarker[1] + graphDrawable.cameraTarget[1]);

            if (vizController.getVizConfig().isSelectionEnable() && engine.isRectangleSelection()) {
                if (!dragging) {
                    //Start drag
                    dragging = true;
                    Rectangle rectangle = (Rectangle) engine.getCurrentSelectionArea();
                    rectangle.start(mousePosition);
                }
                engine.getScheduler().requireUpdateSelection();
            } else if (vizController.getVizConfig().isDraggingEnable()) {

                if (!dragging) {
                    //Start drag
                    dragging = true;
                    engine.getScheduler().requireStartDrag();
                }
                engine.getScheduler().requireDrag();
            } else if (vizController.getVizConfig().isMouseSelectionUpdateWhileDragging()) {
                engine.getScheduler().requireDrag();
            } else {
                if (!dragging) {
                    //Start drag
                    dragging = true;
                    startDrag2d[0] = x;
                    startDrag2d[1] = y;
                    vizEventManager.startDrag();
                }
                mouseDrag[0] = x - startDrag2d[0];
                mouseDrag[1] = startDrag2d[1] - y;
                vizEventManager.drag();
            }

            leftButtonMoving[0] = x;
            leftButtonMoving[1] = y;
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (e.getUnitsToScroll() == 0) {
            return;
        }

        boolean ctrl = (e.getModifiers() & InputEvent.CTRL_DOWN_MASK) != 0 || (e.getModifiers() & InputEvent.CTRL_MASK) != 0;
        if (ctrl) {
            SelectionManager manager = VizController.getInstance().getSelectionManager();
            if (!manager.isRectangleSelection()) {
                int s = manager.getMouseSelectionDiameter();
                s += -e.getUnitsToScroll() * 2;
                s = Math.min(1000, s);
                s = Math.max(1, s);
                manager.setMouseSelectionDiameter(s);
            }
            return;
        }

        //Attributes
        float way = -e.getUnitsToScroll() / Math.abs(e.getUnitsToScroll());
        Vec3f cameraVector = graphDrawable.getCameraVector().copy();
        float cameraLocation[] = graphDrawable.getCameraLocation();
        float cameraTarget[] = graphDrawable.getCameraTarget();

        //Distance
        float distance = limits.getDistanceFromPoint(cameraLocation[0], cameraLocation[1], cameraLocation[2]);
        float distanceRatio = MathUtil.clamp(2 * distance / 10000f, 0f, 2f);
        float coeff = (float) (Math.exp(distanceRatio - 2) * 2.2 - 0.295);      //exp(x-2)*2.2-0.3
        float step = way * (10f + 1000 * coeff);
        if (way == -1) {
            step *= 3;
        }
        float stepRatio = step / distance;

        //Get mouse position within the clipping plane
        float mouseX = MathUtil.clamp(mousePosition[0], limits.getMinXviewport(), limits.getMaxXviewport());
        float mouseY = MathUtil.clamp(mousePosition[1], limits.getMinYviewport(), limits.getMaxYviewport());
        mouseX = mouseX - graphDrawable.viewport.get(2) / 2f;       //Set to centric coordinates
        mouseY = mouseY - graphDrawable.viewport.get(3) / 2f;

        //Transform in 3d coordinates
        mouseX /= -graphDrawable.draggingMarker[0];
        mouseY /= -graphDrawable.draggingMarker[1];

        //Set stepVector for zooming, direction of camera and norm of step
        cameraVector.normalize();
        Vec3f stepVec = cameraVector.times(step);

        cameraLocation[0] += stepVec.x();
        cameraLocation[1] += stepVec.y();
        cameraLocation[2] += stepVec.z();
        cameraLocation[2] = MathUtil.clamp(cameraLocation[2], 1f, Float.POSITIVE_INFINITY);
        //System.out.println("camera: "+graphDrawable.cameraLocation[2]);

        //Displacement of camera according to mouse position. Clamped to graph limits
        Vec3f disVec = new Vec3f(mouseX, mouseY, 0);
        disVec.scale(stepRatio);
        //System.out.println(disVec.x()+"    "+disVec.y()+"     "+disVec.z());

        cameraLocation[0] += disVec.x();
        cameraLocation[1] += disVec.y();
        cameraLocation[2] += disVec.z();

        cameraTarget[0] += disVec.x();
        cameraTarget[1] += disVec.y();
        cameraTarget[2] += disVec.z();

        //Refresh
        engine.getScheduler().requireUpdateVisible();

        //Too slow as it triggers many events later
        //vizController.getVizModel().setCameraDistance(graphDrawable.getCameraVector().length());

        /* float[] graphLimits = engine.getGraphLimits();
         float graphWidth = Math.abs(graphLimits[1]-graphLimits[0]);
         float graphHeight = Math.abs(graphLimits[3]-graphLimits[2]);

         //On reduit l'hypothenuse et on calcule les depl z et y correpsondant
         double hypotenuse = Math.sqrt(Math.pow(graphDrawable.cameraTarget[1] - graphDrawable.cameraLocation[1],2d) +
         Math.pow(graphDrawable.cameraTarget[2] - graphDrawable.cameraLocation[2],2d));
         float move = e.getUnitsToScroll()*((float)hypotenuse*0.05f);

         float widthRatio = graphWidth/(float)hypotenuse;
         float heightRatio = graphHeight/(float)hypotenuse;
         float distanceRatio = Math.max(widthRatio, heightRatio);

         if(e.getUnitsToScroll() > 0 && distanceRatio < 0.03f)
         return;

         if(hypotenuse + move > 2 ) {
         hypotenuse = hypotenuse + move;

         double disY = hypotenuse*Math.sin(graphDrawable.rotationX);
         double disZ = hypotenuse*Math.cos(graphDrawable.rotationX);
         float moveY = e.getUnitsToScroll()*(float)(disY*1/(8+distanceRatio));
         float moveZ = e.getUnitsToScroll()*(float)(disZ*1/(8+distanceRatio));
         //float moveY = e.getUnitsToScroll()*(float)(disY*0.05f);
         //float moveZ = e.getUnitsToScroll()*(float)(disZ*0.05f);

         graphDrawable.cameraLocation[1] += moveY;
         graphDrawable.cameraLocation[2] += moveZ;
         graphDrawable.rotationX = (float)Math.atan(((graphDrawable.cameraLocation[1]-graphDrawable.cameraTarget[1])/(graphDrawable.cameraLocation[2]-graphDrawable.cameraTarget[2])));
         engine.getScheduler().requireUpdateVisible();
         }*/
    }

    @Override
    public void setCameraDistance(float distance) {
        float cameraLocation[] = graphDrawable.getCameraLocation();
        float cameraTarget[] = graphDrawable.getCameraTarget();
        Vec3f camVect = new Vec3f(cameraTarget[0] - cameraLocation[0], cameraTarget[1] - cameraLocation[1], cameraTarget[2] - cameraLocation[2]);

        float diff = camVect.length() - distance;
        if (Math.abs(diff) > 1f) {
            camVect.normalize();
            cameraLocation[0] += camVect.x() * diff;
            cameraLocation[1] += camVect.y() * diff;
            cameraLocation[2] += camVect.z() * diff;
            cameraLocation[2] = Math.max(0.5f, cameraLocation[2]);

            engine.getScheduler().requireUpdateVisible();
        }
    }

    @Override
    public float[] getMousePosition3d() {
        float[] m = new float[2];
        m[0] = mousePosition[0] - graphDrawable.viewport.get(2) / 2f;       //Set to centric coordinates
        m[1] = mousePosition[1] - graphDrawable.viewport.get(3) / 2f;
        m[0] /= -graphDrawable.draggingMarker[0];        //Transform in 3d coordinates
        m[1] /= -graphDrawable.draggingMarker[1];
        m[0] += graphDrawable.cameraTarget[0];
        m[1] += graphDrawable.cameraTarget[1];

        return m;
    }

    @Override
    public void trigger() {
        if (pressing) {
            vizEventManager.mouseLeftPressing();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public float[] getMousePosition() {
        return mousePosition;
    }

    @Override
    public float[] getMouseDrag() {
        return mouseDrag;
    }

    @Override
    public float[] getMouseDrag3d() {
        return mouseDrag3d;
    }

    @Override
    public void centerOnZero() {
        graphDrawable.cameraLocation[0] = 0;
        graphDrawable.cameraLocation[1] = 0;
        graphDrawable.cameraLocation[2] = 100;

        graphDrawable.cameraTarget[0] = 0;
        graphDrawable.cameraTarget[1] = 0;
        graphDrawable.cameraTarget[2] = 0;

        //Refresh
        engine.getScheduler().requireUpdateVisible();
    }

    @Override
    public void centerOnGraph() {
        float graphWidth = Math.abs(limits.getMaxXoctree() - limits.getMinXoctree());
        float graphHeight = Math.abs(limits.getMaxYoctree() - limits.getMinYoctree());

        float currentDistanceGraphRatioX = Math.abs(graphDrawable.viewport.get(2) / (float) graphDrawable.getDraggingMarkerX()) / graphDrawable.cameraLocation[2];
        float currentDistanceGraphRatioY = Math.abs(graphDrawable.viewport.get(3) / (float) graphDrawable.getDraggingMarkerY()) / graphDrawable.cameraLocation[2];
        float newCameraLocationX = graphWidth / currentDistanceGraphRatioX;
        float newCameraLocationY = graphHeight / currentDistanceGraphRatioY;
        float newCameraLocation = Math.max(newCameraLocationX, newCameraLocationY);

        graphDrawable.cameraLocation[0] = limits.getMinXoctree() + graphWidth / 2;
        graphDrawable.cameraLocation[1] = limits.getMinYoctree() + graphWidth / 2;
        graphDrawable.cameraLocation[2] = newCameraLocation;

        graphDrawable.cameraTarget[0] = graphDrawable.cameraLocation[0];
        graphDrawable.cameraTarget[1] = graphDrawable.cameraLocation[1];
        graphDrawable.cameraTarget[2] = 0;

        //Refresh
        engine.getScheduler().requireUpdateVisible();
    }

    @Override
    public void centerOnCoordinate(float x, float y, float z) {
        graphDrawable.cameraTarget[0] = x;
        graphDrawable.cameraTarget[1] = y;
        graphDrawable.cameraTarget[2] = z;

        graphDrawable.cameraLocation[0] = x;
        graphDrawable.cameraLocation[1] = y;
        graphDrawable.cameraLocation[2] = z + 100;
    }
}
