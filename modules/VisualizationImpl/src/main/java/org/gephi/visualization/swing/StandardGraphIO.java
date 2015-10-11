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

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
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
    protected GLAbstractListener graphDrawable;
    protected Component graphComponent;
    protected AbstractEngine engine;
    protected VizEventManager vizEventManager;
    protected VizController vizController;
    protected GraphLimits limits;
    //Listeners data
    protected float[] rightButtonMoving = {-1f, 0f, 0f};
    protected float[] leftButtonMoving = {-1f, 0f, 0f};
    protected float[] middleButtonMoving = {-1f, 0f, 0f};
    protected float[] mousePosition = new float[2];
    protected float[] mousePosition3d = new float[2];
    protected float[] mouseDrag3d = new float[2];
    protected float[] mouseDrag = new float[2];
    protected float[] startDrag2d = new float[2];
    //Flags
    protected boolean draggingEnable = true;
    protected boolean dragging = false;
    protected boolean pressing = false;

    @Override
    public void initArchitecture() {
        this.graphDrawable = (GLAbstractListener) VizController.getInstance().getDrawable();
        this.graphComponent = graphDrawable.graphComponent;
        this.engine = VizController.getInstance().getEngine();
        this.vizEventManager = VizController.getInstance().getVizEventManager();
        this.vizController = VizController.getInstance();
        this.limits = VizController.getInstance().getLimits();
    }

    @Override
    public void startMouseListening() {
        stopMouseListening();

        if (vizController.getVizConfig().isCameraControlEnable()) {
            graphComponent.addMouseListener(this);
            graphComponent.addMouseWheelListener(this);
        }

        if (vizController.getVizConfig().isSelectionEnable()) {
            graphComponent.addMouseMotionListener(this);
        }
    }

    @Override
    public void stopMouseListening() {
        graphComponent.removeMouseListener(this);
        graphComponent.removeMouseMotionListener(this);
        graphComponent.removeMouseWheelListener(this);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        float globalScale = graphDrawable.getGlobalScale();
        Point componentLocation = graphDrawable.getLocationOnScreen();
        Point location = e.getLocationOnScreen();
        float x = globalScale * (location.x - componentLocation.x);
        float y = globalScale * (location.y - componentLocation.y);

        if (SwingUtilities.isRightMouseButton(e)) {
            //Save the coordinate of the start
            rightButtonMoving[0] = x;
            rightButtonMoving[1] = y;

            //Change cursor
            graphComponent.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));

            //Dispatch event
            vizEventManager.mouseRightPress();
        } else if (SwingUtilities.isLeftMouseButton(e)) {
            //Save the coordinate of the start
            leftButtonMoving[0] = x;
            leftButtonMoving[1] = y;

            //Dispatch event
            pressing = true;
            vizEventManager.mouseLeftPress();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        float globalScale = graphDrawable.getGlobalScale();
        Point componentLocation = graphDrawable.getLocationOnScreen();
        Point location = e.getLocationOnScreen();
        float x = globalScale * (location.x - componentLocation.x);
        float y = globalScale * (location.y - componentLocation.y);

        //Disable the right button moving
        rightButtonMoving[0] = -1;
        leftButtonMoving[0] = -1;
        middleButtonMoving[0] = -1;

        //Update mouse position
        mousePosition[0] = (int) x;
        mousePosition[1] = graphDrawable.viewport.get(3) - (int) y - 1;

        //Update 3d position
        double[] marker = graphDrawable.draggingMarker;
        mousePosition3d[0] = (float) ((x - graphDrawable.viewport.get(2) / 2.0) / -marker[0]) + graphDrawable.cameraTarget[0] / globalScale;       //Set to centric coordinates
        mousePosition3d[1] = (float) ((y - graphDrawable.viewport.get(3) / 2.0) / -marker[1]) + graphDrawable.cameraTarget[1] / globalScale;

        if (dragging) {
            dragging = false;
            engine.getScheduler().requireStopDrag();

            //Dispatch event
            vizEventManager.stopDrag();
        } else {
            //Set default cursor
            graphComponent.setCursor(Cursor.getDefaultCursor());
        }

        //Dispatch event
        pressing = false;
        vizEventManager.mouseReleased();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        dragging = false;
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        float globalScale = graphDrawable.getGlobalScale();
        Point componentLocation = graphDrawable.getLocationOnScreen();
        Point location = e.getLocationOnScreen();
        float x = globalScale * (location.x - componentLocation.x);
        float y = globalScale * (location.y - componentLocation.y);

        x = (int) x;
        y = graphDrawable.viewport.get(3) - (int) y - 1;

        // Only update if position has changed
        if (mousePosition[0] != x || mousePosition[1] != y) {
            mousePosition[0] = x;
            mousePosition[1] = y;

            //Upate 3d position
            double[] marker = graphDrawable.draggingMarker;
            mousePosition3d[0] = (float) ((x - graphDrawable.viewport.get(2) / 2.0) / -marker[0]) + graphDrawable.cameraTarget[0] / globalScale;       //Set to centric coordinates
            mousePosition3d[1] = (float) ((y - graphDrawable.viewport.get(3) / 2.0) / -marker[1]) + graphDrawable.cameraTarget[1] / globalScale;

            engine.getScheduler().requireUpdateSelection();
            vizEventManager.mouseMove();
        }
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
                float globalScale = graphDrawable.getGlobalScale();
                int x = (int) (mousePosition[0] / globalScale);
                int y = (int) ((graphDrawable.viewport.get(3) - mousePosition[1]) / globalScale);
                popupMenu.getMenu().show(graphDrawable.getGraphComponent(), x, y);
            }
            vizEventManager.mouseRightClick();
        } else if (SwingUtilities.isMiddleMouseButton(e)) {
            vizEventManager.mouseMiddleClick();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        float globalScale = graphDrawable.getGlobalScale();
        Point componentLocation = graphDrawable.getLocationOnScreen();
        Point location = e.getLocationOnScreen();
        float x = globalScale * (location.x - componentLocation.x);
        float y = globalScale * (location.y - componentLocation.y);

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

        if (leftButtonMoving[0] != -1) {

            //Remet à jour aussi la mousePosition pendant le drag, notamment pour coller quand drag released
            mousePosition[0] = (int) x;
            mousePosition[1] = graphDrawable.viewport.get(3) - (int) y - 1;
            mouseDrag3d[0] = (float) ((graphDrawable.viewport.get(2) / 2 - x) / graphDrawable.draggingMarker[0] + graphDrawable.cameraTarget[0]);
            mouseDrag3d[1] = (float) ((y - graphDrawable.viewport.get(3) / 2) / graphDrawable.draggingMarker[1] + graphDrawable.cameraTarget[1]);
            mousePosition3d[0] = mouseDrag3d[0];
            mousePosition3d[1] = mouseDrag3d[1];

            if (vizController.getVizConfig().isSelectionEnable() && engine.isRectangleSelection()) {
                if (!dragging) {
                    //Start drag
                    dragging = true;
                    Rectangle rectangle = (Rectangle) engine.getCurrentSelectionArea();
                    //TODO fix that
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
        float globalScale = graphDrawable.getGlobalScale();
        float way = -e.getUnitsToScroll() / Math.abs(e.getUnitsToScroll());
        float cameraLocation[] = graphDrawable.getCameraLocation();
        float cameraTarget[] = graphDrawable.getCameraTarget();
        float markerX = (float) graphDrawable.getDraggingMarkerX();
        float markerY = (float) graphDrawable.getDraggingMarkerY();

        //Get mouse position within the clipping plane
        float mouseX = MathUtil.clamp(mousePosition[0], limits.getMinXviewport(), limits.getMaxXviewport());
        float mouseY = MathUtil.clamp(mousePosition[1], limits.getMinYviewport(), limits.getMaxYviewport());

        //Transform in 3d coordinates
        mouseX = (float) ((mouseX - graphDrawable.viewport.get(2) / 2.0) / -markerX) + cameraTarget[0] / globalScale;       //Set to centric coordinates
        mouseY = (float) ((mouseY - graphDrawable.viewport.get(3) / 2.0) / -markerY) + cameraTarget[1] / globalScale;
        mouseX *= globalScale;
        mouseY *= globalScale;

        //Camera location and target vectors
        Vec3f targetVector = new Vec3f(mouseX - cameraTarget[0], mouseY - cameraTarget[1], 0f);
        Vec3f locationVector = new Vec3f(mouseX - cameraLocation[0], mouseY - cameraLocation[1], -cameraLocation[2]);

        //Distance from location to mouse
        float distance = (float) Math.sqrt(locationVector.x() * locationVector.x()
                + locationVector.y() * locationVector.y() + locationVector.z() * locationVector.z());
        float distanceRatio = MathUtil.clamp(2 * distance / 10000f, 0f, 2f);
        float coeff = (float) (Math.exp(distanceRatio - 2) * 2.2 - 0.295);      //exp(x-2)*2.2-0.3
        float step = way * (10f + 1000 * coeff);
        if (way == -1) {
            step *= 3;
        }
        float stepRatio = step / distance;

        //Multiply vectors
        targetVector.scale(stepRatio);
        locationVector.scale(stepRatio);

        if (cameraLocation[2] + locationVector.z() >= 1f) {
            cameraLocation[0] += locationVector.x();
            cameraLocation[1] += locationVector.y();
            cameraLocation[2] += locationVector.z();

            cameraTarget[0] += targetVector.x();
            cameraTarget[1] += targetVector.y();
        }

        //Refresh
        engine.getScheduler().requireUpdateVisible();
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
        return mousePosition3d;
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
        graphDrawable.cameraLocation[1] = limits.getMinYoctree() + graphHeight / 2;
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

    @Override
    public boolean isDragging() {
        return dragging;
    }
}
