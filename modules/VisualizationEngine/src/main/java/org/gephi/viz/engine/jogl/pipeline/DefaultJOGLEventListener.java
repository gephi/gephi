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
    private boolean mouseRightButtonPressed = false;
    private boolean mouseLeftButtonPressed = false;
    private MouseEvent lastMovedPosition = null;
    private VizEngineModel model;

    public DefaultJOGLEventListener(VizEngine<JOGLRenderingTarget, NEWTEvent> engine) {
        this.engine = engine;
        this.inputActionsProcessor = new InputActionsProcessor(engine);
    }

    @Override
    public void frameStart(VizEngineModel model) {
        lastMovedPosition = null;
        this.model = model;
    }

    @Override
    public void frameEnd(VizEngineModel model) {
        if (lastMovedPosition != null) {
            //TODO: move to independent selection input listener
            final Vector2f worldCoords =
                engine.screenCoordinatesToWorldCoordinates(lastMovedPosition.getX(), lastMovedPosition.getY());

            if (model.getGraphSelection().getMode() == GraphSelection.GraphSelectionMode.SINGLE_NODE_SELECTION) {
                inputActionsProcessor.selectNodesAndEdgesUnderPosition(model, worldCoords);
            } else if (
                model.getGraphSelection().getMode() == GraphSelection.GraphSelectionMode.SIMPLE_MOUSE_SELECTION ||
                    model.getGraphSelection().getMode() == GraphSelection.GraphSelectionMode.MULTI_NODE_SELECTION) {
                float diameter = model.getGraphSelection().getMouseSelectionEffectiveDiameter();

                if (diameter <= 1) {
                    // Diameter is disabled
                    inputActionsProcessor.selectNodesAndEdgesUnderPosition(model, worldCoords);
                } else {
                    inputActionsProcessor.selectNodesWithinRadius(model, worldCoords.x, worldCoords.y, diameter);
                }
            }
        }
        this.model = null;
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
        if (event instanceof KeyEvent) {
            return false;
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

        if (model.getGraphSelection().getMode() == GraphSelection.GraphSelectionMode.RECTANGLE_SELECTION) {
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
                        final Rect2D rectangle = new Rect2D(
                            Math.min(initialPosition.x, currentPosition.x),
                            Math.min(initialPosition.y, currentPosition.y),
                            Math.max(initialPosition.x, currentPosition.x),
                            Math.max(initialPosition.y, currentPosition.y)
                        );
                        inputActionsProcessor.selectNodesAndEdgesOnRectangle(model, rectangle);
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
