package org.gephi.viz.engine.jogl.pipeline;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.NEWTEvent;
import java.util.ArrayList;
import java.util.List;
import org.gephi.graph.api.Rect2D;
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.VizEngineModel;
import org.gephi.viz.engine.jogl.JOGLRenderingTarget;
import org.gephi.viz.engine.spi.InputListener;
import org.gephi.viz.engine.status.GraphSelection;
import org.gephi.viz.engine.util.actions.InputActionsProcessor;
import org.joml.Vector2f;

/**
 *
 * @author Eduardo Ramos
 */
public class DefaultJOGLEventListener implements InputListener<JOGLRenderingTarget, NEWTEvent> {

    private final VizEngine<JOGLRenderingTarget, NEWTEvent> engine;
    private final InputActionsProcessor inputActionsProcessor;

    private static final short MOUSE_LEFT_BUTTON = MouseEvent.BUTTON1;
    private static final short MOUSE_WHEEL_BUTTON = MouseEvent.BUTTON2;
    private static final short MOUSE_RIGHT_BUTTON = MouseEvent.BUTTON3;
    // Keyboard navigation: pan distance in screen pixels and zoom in mouse wheel units for each key press
    private static final int KEY_PAN_STEP = 40;
    public static final double KEY_ZOOM_STEP = 3;
    private boolean mouseRightButtonPressed = false;
    private boolean mouseLeftButtonPressed = false;
    // Selections to apply, kept until the graph lock lets them through
    private MouseEvent lastMovedPosition = null;
    private Rect2D pendingRectangle = null;
    private VizEngineModel model;

    public DefaultJOGLEventListener(VizEngine<JOGLRenderingTarget, NEWTEvent> engine) {
        this.engine = engine;
        this.inputActionsProcessor = new InputActionsProcessor(engine);
    }

    @Override
    public void frameStart(VizEngineModel model) {
        this.model = model;
        // Before the other listeners process their events, so a click doesn't act on a selection left behind
        applyPendingSelection(model);
    }

    @Override
    public void frameEnd(VizEngineModel model) {
        applyPendingSelection(model);
        this.model = null;
    }

    private void applyPendingSelection(VizEngineModel model) {
        boolean applied = true;
        if (lastMovedPosition != null) {
            //TODO: move to independent selection input listener
            final Vector2f worldCoords =
                engine.screenCoordinatesToWorldCoordinates(lastMovedPosition.getX(), lastMovedPosition.getY());

            if (model.getGraphSelection().getMode() == GraphSelection.GraphSelectionMode.SINGLE_NODE_SELECTION) {
                applied = inputActionsProcessor.selectNodesAndEdgesUnderPosition(model, worldCoords);
            } else if (
                model.getGraphSelection().getMode() == GraphSelection.GraphSelectionMode.SIMPLE_MOUSE_SELECTION ||
                    model.getGraphSelection().getMode() == GraphSelection.GraphSelectionMode.MULTI_NODE_SELECTION) {
                float diameter = model.getGraphSelection().getMouseSelectionEffectiveDiameter();

                if (diameter <= 1) {
                    // Diameter is disabled
                    applied = inputActionsProcessor.selectNodesAndEdgesUnderPosition(model, worldCoords);
                } else {
                    applied =
                        inputActionsProcessor.selectNodesWithinRadius(model, worldCoords.x, worldCoords.y, diameter);
                }
            }
            if (applied) {
                lastMovedPosition = null;
            }
        }
        if (pendingRectangle != null) {
            if (inputActionsProcessor.selectNodesAndEdgesOnRectangle(model, pendingRectangle)) {
                pendingRectangle = null;
            } else {
                applied = false;
            }
        }
        if (!applied) {
            // A graph writer keeps the lock, clear the selection rather than let a click act on stale nodes
            inputActionsProcessor.clearSelection(model);
        }
    }

    @Override
    public List<NEWTEvent> processEvents(List<NEWTEvent> events) {
        // Compress consecutive MOUSE_MOVED events - keep only the last one
        List<NEWTEvent> compressed = compressMouseMoveEvents(events);

        // Process compressed events and return unconsumed ones
        List<NEWTEvent> remaining = new ArrayList<>();
        for (NEWTEvent event : compressed) {
            boolean consumed = processEvent(event);
            if (!consumed) {
                remaining.add(event);
            }
        }
        return remaining;
    }

    private List<NEWTEvent> compressMouseMoveEvents(List<NEWTEvent> events) {
        if (events.isEmpty()) {
            return events;
        }

        List<NEWTEvent> compressed = new ArrayList<>();
        NEWTEvent lastMouseMove = null;

        for (NEWTEvent event : events) {
            if (event instanceof MouseEvent &&
                event.getEventType() == MouseEvent.EVENT_MOUSE_MOVED) {
                // This is a MOUSE_MOVED event, hold onto it
                lastMouseMove = event;
            } else {
                // Not a MOUSE_MOVED event
                // First, add any pending lastMouseMove
                if (lastMouseMove != null) {
                    compressed.add(lastMouseMove);
                    lastMouseMove = null;
                }
                // Then add this event
                compressed.add(event);
            }
        }

        // Don't forget the last MOUSE_MOVED event if there was one
        if (lastMouseMove != null) {
            compressed.add(lastMouseMove);
        }

        return compressed;
    }

    private boolean processEvent(NEWTEvent event) {
        if (event instanceof KeyEvent keyEvent) {
            return processKeyEvent(keyEvent);
        } else if (event instanceof MouseEvent mouseEvent) {
            switch (event.getEventType()) {
                case MouseEvent.EVENT_MOUSE_CLICKED:
                    return this.mouseClicked(mouseEvent);
                case MouseEvent.EVENT_MOUSE_DRAGGED:
                    return this.mouseDragged(mouseEvent);
                case MouseEvent.EVENT_MOUSE_MOVED:
                    return this.mouseMoved(mouseEvent);
                case MouseEvent.EVENT_MOUSE_PRESSED:
                    return this.mousePressed(mouseEvent);
                case MouseEvent.EVENT_MOUSE_RELEASED:
                    return this.mouseReleased(mouseEvent);
                case MouseEvent.EVENT_MOUSE_WHEEL_MOVED:
                    return this.mouseWheelMoved(mouseEvent);
                case MouseEvent.EVENT_MOUSE_ENTERED:
                case MouseEvent.EVENT_MOUSE_EXITED:
                default:
                    return false;
            }
        }

        return false;
    }

    public boolean mouseClicked(MouseEvent e) {
        boolean leftClick = e.getClickCount() == 1 && e.getButton() == MOUSE_LEFT_BUTTON;
        boolean doubleLeftClick = e.getClickCount() == 2 && e.getButton() == MOUSE_LEFT_BUTTON;
        boolean doubleRightClick = e.getClickCount() == 2 && e.getButton() == MOUSE_RIGHT_BUTTON;
        boolean wheelClick = e.getButton() == MOUSE_WHEEL_BUTTON;

        final int x = e.getX();
        final int y = e.getY();

        if (wheelClick) {
            inputActionsProcessor.processCenterOnGraphEvent();
            return true;
        } else if (doubleLeftClick) {
            //Zoom in:
            inputActionsProcessor.processZoomEvent(10, x, y);
            return true;
        } else if (doubleRightClick) {
            //Zoom out:
            inputActionsProcessor.processZoomEvent(-10, x, y);
            return true;
        } else {
            if (model.getGraphSelection().getMode() == GraphSelection.GraphSelectionMode.SIMPLE_MOUSE_SELECTION &&
                leftClick) {
                //TODO: move to independent selection input listener
                return true;
            } else if (model.getGraphSelection().getMode() == GraphSelection.GraphSelectionMode.SINGLE_NODE_SELECTION &&
                leftClick) {
                return true;
            } else if (model.getGraphSelection().getMode() == GraphSelection.GraphSelectionMode.MULTI_NODE_SELECTION &&
                leftClick) {
                return true;
            } else if (model.getGraphSelection().getMode() == GraphSelection.GraphSelectionMode.RECTANGLE_SELECTION) {
                pendingRectangle = null;
                inputActionsProcessor.clearSelection(model);
                return true;
            }
        }

        return false;
    }

    public boolean mousePressed(MouseEvent e) {
        if (e.getButton() == MOUSE_LEFT_BUTTON) {
            mouseLeftButtonPressed = true;

            if (model.getGraphSelection().getMode() == GraphSelection.GraphSelectionMode.RECTANGLE_SELECTION) {
                pendingRectangle = null;
                inputActionsProcessor.clearSelection(model);
                model.getGraphSelection()
                    .startRectangleSelection(engine.screenCoordinatesToWorldCoordinates(e.getX(), e.getY()));
                return true;
            }
        }

        if (e.getButton() == MOUSE_RIGHT_BUTTON) {
            mouseRightButtonPressed = true;
        }

        lastX = e.getX();
        lastY = e.getY();

        return false;
    }

    public boolean mouseReleased(MouseEvent e) {
        if (e.getButton() == MOUSE_LEFT_BUTTON) {
            mouseLeftButtonPressed = false;
        }

        if (e.getButton() == MOUSE_RIGHT_BUTTON) {
            mouseRightButtonPressed = false;
        }

        if (e.getButton() == MOUSE_LEFT_BUTTON &&
            model.getGraphSelection().getMode() == GraphSelection.GraphSelectionMode.RECTANGLE_SELECTION) {
            model.getGraphSelection()
                .stopRectangleSelection(engine.screenCoordinatesToWorldCoordinates(e.getX(), e.getY()));
        }

        return false;
    }

    public boolean mouseMoved(MouseEvent e) {
        lastMovedPosition = e;
        if ((model.getGraphSelection().getMode() == GraphSelection.GraphSelectionMode.SIMPLE_MOUSE_SELECTION ||
            model.getGraphSelection().getMode() == GraphSelection.GraphSelectionMode.MULTI_NODE_SELECTION) &&
            model.getGraphSelection().getMouseSelectionDiameter() > 1f) {
            model.getGraphSelection()
                .updateMousePosition(engine.screenCoordinatesToWorldCoordinates(e.getX(), e.getY()));
        }
        return true;
    }

    public boolean mouseDragged(MouseEvent e) {
        try {
            if (mouseLeftButtonPressed && mouseRightButtonPressed) {
                //Zoom in/on the screen center with both buttons pressed and vertical movement:
                double zoomQuantity = (lastY - e.getY()) / 7f;//Divide by some number so zoom is not too fast
                inputActionsProcessor.processZoomEvent(zoomQuantity, engine.getWidth() / 2, engine.getHeight() / 2);
                return true;
            } else {
                if (model.getGraphSelection().getMode() == GraphSelection.GraphSelectionMode.MULTI_NODE_SELECTION &&
                    model.getGraphSelection().getMouseSelectionDiameter() > 1f) {
                    model.getGraphSelection()
                        .updateMousePosition(engine.screenCoordinatesToWorldCoordinates(e.getX(), e.getY()));
                } else if (
                    model.getGraphSelection().getMode() != GraphSelection.GraphSelectionMode.RECTANGLE_SELECTION &&
                        (mouseLeftButtonPressed || mouseRightButtonPressed)) {
                    inputActionsProcessor.processCameraMoveEvent(e.getX() - lastX, e.getY() - lastY);
                    return true;
                } else if (
                    model.getGraphSelection().getMode() == GraphSelection.GraphSelectionMode.RECTANGLE_SELECTION &&
                        mouseLeftButtonPressed) {
                    model.getGraphSelection().updateRectangleSelection(
                        engine.screenCoordinatesToWorldCoordinates(e.getX(), e.getY()));

                    final Vector2f initialPosition = model.getGraphSelection().getRectangleInitialPosition();
                    final Vector2f currentPosition = model.getGraphSelection().getRectangleCurrentPosition();

                    if (initialPosition != null && currentPosition != null) {
                        // Selected at the end of the frame
                        pendingRectangle = new Rect2D(
                            Math.min(initialPosition.x, currentPosition.x),
                            Math.min(initialPosition.y, currentPosition.y),
                            Math.max(initialPosition.x, currentPosition.x),
                            Math.max(initialPosition.y, currentPosition.y)
                        );
                    }
                    return true;
                } else if (
                    model.getGraphSelection().getMode() == GraphSelection.GraphSelectionMode.RECTANGLE_SELECTION &&
                        mouseRightButtonPressed) {
                    inputActionsProcessor.processCameraMoveEvent(e.getX() - lastX, e.getY() - lastY);
                    return true;
                }
            }
        } finally {
            lastX = e.getX();
            lastY = e.getY();
        }

        return false;
    }

    private boolean processKeyEvent(KeyEvent e) {
        if (e.getEventType() != KeyEvent.EVENT_KEY_PRESSED || e.isControlDown() || e.isAltDown() || e.isMetaDown()) {
            return false;
        }

        final int screenCenterX = engine.getWidth() / 2;
        final int screenCenterY = engine.getHeight() / 2;
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                inputActionsProcessor.processCameraMoveEvent(KEY_PAN_STEP, 0);
                return true;
            case KeyEvent.VK_RIGHT:
                inputActionsProcessor.processCameraMoveEvent(-KEY_PAN_STEP, 0);
                return true;
            case KeyEvent.VK_UP:
                inputActionsProcessor.processCameraMoveEvent(0, KEY_PAN_STEP);
                return true;
            case KeyEvent.VK_DOWN:
                inputActionsProcessor.processCameraMoveEvent(0, -KEY_PAN_STEP);
                return true;
            case KeyEvent.VK_PLUS:
            case KeyEvent.VK_EQUALS:
            case KeyEvent.VK_ADD:
                inputActionsProcessor.processZoomEvent(KEY_ZOOM_STEP, screenCenterX, screenCenterY);
                return true;
            case KeyEvent.VK_MINUS:
            case KeyEvent.VK_SUBTRACT:
                inputActionsProcessor.processZoomEvent(-KEY_ZOOM_STEP, screenCenterX, screenCenterY);
                return true;
            case KeyEvent.VK_HOME:
                inputActionsProcessor.processCenterOnGraphEvent();
                return true;
            default:
                return false;
        }
    }

    public boolean mouseWheelMoved(MouseEvent e) {
        float[] rotation = e.getRotation();
        float verticalRotation = rotation[1] * e.getRotationScale();
        inputActionsProcessor.processZoomEvent(verticalRotation, e.getX(), e.getY());

        return true;
    }

    private int lastX;
    private int lastY;

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public String getCategory() {
        return "default";
    }

    @Override
    public int getPreferenceInCategory() {
        return 0;
    }

    @Override
    public String getName() {
        return "Default";
    }

    @Override
    public boolean isAvailable(JOGLRenderingTarget target) {
        return true;
    }

    @Override
    public void init(JOGLRenderingTarget target) {
        //NOOP
    }
}
