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

package org.gephi.visualization.events;

import java.awt.Component;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jogamp.newt.event.MouseEvent;
import javax.swing.JComponent;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.visualization.VizModel;
import org.gephi.visualization.api.VisualizationEvent;
import org.gephi.visualization.api.VisualizationEventListener;
import org.gephi.visualization.apiimpl.GraphContextMenu;
import org.gephi.visualization.apiimpl.VizConfig;
import org.gephi.visualization.apiimpl.VizEvent;
import org.gephi.visualization.component.VizEngineGraphCanvasManager;
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.status.GraphSelection;
import org.gephi.viz.engine.structure.GraphIndex;
import org.joml.Vector2f;
import org.joml.Vector2i;

/**
 * @author Mathieu Bastian
 */
public class StandardVizEventManager {

    private static final short MOUSE_LEFT_BUTTON = MouseEvent.BUTTON1;
    private static final short MOUSE_WHEEL_BUTTON = MouseEvent.BUTTON2;
    private static final short MOUSE_RIGHT_BUTTON = MouseEvent.BUTTON3;

    // State
    private final Vector2i dragStartMouseScreenPosition = new Vector2i(0, 0);
    private final Vector2f dragStartMouseWorldPosition2d = new Vector2f(0, 0);

    private final Vector2i previousMouseScreenPosition = new Vector2i(0, 0);
    private final Vector2f previousMouseWorldPosition2d = new Vector2f(0, 0);

    private final Vector2i mouseScreenPosition = new Vector2i(0, 0);
    private final Vector2f mouseWorldPosition = new Vector2f(0, 0);

    // Pressing thread
    private static final int PRESSING_FREQUENCY = 7;
    private Thread pressingThread;
    private volatile boolean shouldStopPressing = false;
    private final Object pressingLock = new Object();

    //Architecture
    private final VisualizationEventTypeHandler[] handlers;
    private boolean dragging = false;

    public StandardVizEventManager() {
        //Set handlers
        final ArrayList<VisualizationEventTypeHandler> handlersList = new ArrayList<>();
        handlersList.add(new VisualizationEventTypeHandler(VisualizationEvent.Type.MOUSE_LEFT_CLICK, false));
        handlersList.add(new VisualizationEventTypeHandler(VisualizationEvent.Type.MOUSE_LEFT_PRESS, false));
        handlersList.add(new VisualizationEventTypeHandler(VisualizationEvent.Type.MOUSE_MIDDLE_CLICK, false));
        handlersList.add(new VisualizationEventTypeHandler(VisualizationEvent.Type.MOUSE_MIDDLE_PRESS, false));
        handlersList.add(new VisualizationEventTypeHandler(VisualizationEvent.Type.MOUSE_RIGHT_CLICK, false));
        handlersList.add(new VisualizationEventTypeHandler(VisualizationEvent.Type.MOUSE_RIGHT_PRESS, false));
        handlersList.add(new VisualizationEventTypeHandler(VisualizationEvent.Type.MOUSE_MOVE, true));
        handlersList.add(new VisualizationEventTypeHandler(VisualizationEvent.Type.START_DRAG, false));
        handlersList.add(new VisualizationEventTypeHandler(VisualizationEvent.Type.DRAG, true));
        handlersList.add(new VisualizationEventTypeHandler(VisualizationEvent.Type.STOP_DRAG, false));
        handlersList.add(new VisualizationEventTypeHandler(VisualizationEvent.Type.NODE_LEFT_CLICK, false));
        handlersList.add(new VisualizationEventTypeHandler(VisualizationEvent.Type.MOUSE_LEFT_PRESSING, false));
        handlersList.add(new VisualizationEventTypeHandler(VisualizationEvent.Type.MOUSE_RELEASED, false));
        handlersList.add(new VisualizationEventTypeHandler(VisualizationEvent.Type.NODE_LEFT_PRESS, false));
        handlersList.add(new VisualizationEventTypeHandler(VisualizationEvent.Type.NODE_LEFT_PRESSING, false));
        handlersList.sort((o1, o2) -> {
            VisualizationEvent.Type t1 = o1.type;
            VisualizationEvent.Type t2 = o2.type;
            return t1.compareTo(t2);
        });
        handlers = handlersList.toArray(new VisualizationEventTypeHandler[0]);
    }

    public boolean processMouseEvent(Component parentComponent, VizEngineGraphCanvasManager canvasManager, VizEngine engine, MouseEvent mouseEvent) {
        previousMouseScreenPosition.set(mouseScreenPosition);
        previousMouseWorldPosition2d.set(mouseWorldPosition);

        mouseScreenPosition.set(mouseEvent.getX(), mouseEvent.getY());
        engine.screenCoordinatesToWorldCoordinates(
                mouseScreenPosition.x, mouseScreenPosition.y,
                mouseWorldPosition
        );

        switch (mouseEvent.getEventType()) {
            case MouseEvent.EVENT_MOUSE_DRAGGED:
                if (mouseEvent.getButton() != MOUSE_LEFT_BUTTON) {
                    return false;
                }
                final boolean startDragConsumed;
                if (!dragging) {
                    dragStartMouseScreenPosition.set(mouseScreenPosition);
                    dragStartMouseWorldPosition2d.set(mouseWorldPosition);
                    dragging = true;

                    startDragConsumed = startDrag(engine);
                } else {
                    startDragConsumed = false;
                }

                return drag(engine) || startDragConsumed;
            case MouseEvent.EVENT_MOUSE_MOVED:
                return mouseMove(engine);
            case MouseEvent.EVENT_MOUSE_CLICKED:
                switch (mouseEvent.getButton()) {
                    case MOUSE_LEFT_BUTTON:
                        return mouseLeftClick(engine);
                    case MOUSE_RIGHT_BUTTON:
                        return mouseRightClick(parentComponent, canvasManager, engine);
                    case MOUSE_WHEEL_BUTTON:
                        return mouseMiddleClick(engine);
                }
                return false;
            case MouseEvent.EVENT_MOUSE_PRESSED:
                switch (mouseEvent.getButton()) {
                    case MOUSE_LEFT_BUTTON:
                        return mouseLeftPress(engine);
                    case MOUSE_RIGHT_BUTTON:
                        return mouseRightPress(engine);
                    case MOUSE_WHEEL_BUTTON:
                        return mouseMiddlePress(engine);
                }
                return false;
            case MouseEvent.EVENT_MOUSE_WHEEL_MOVED:
                //NOOP
                return false;
            case MouseEvent.EVENT_MOUSE_RELEASED:
                if (dragging) {
                    dragging = false;
                    stopDrag(engine);
                }
                mouseReleased(engine);

                // Stop pressing thread if it was running
                if(mouseEvent.getButton() == MOUSE_LEFT_BUTTON) {
                    stopPressingThread();
                }

                return false;//Never consume release events
            case MouseEvent.EVENT_MOUSE_EXITED:
                // Stop the thread if exit mouse event is received
                stopPressingThread();
            default:
                //NOOP
                return false;
        }
    }

    public boolean mouseLeftClick(VizEngine engine) {
        final GraphIndex graphIndex = engine.getLookup().lookup(GraphIndex.class);
        final GraphSelection graphSelection = engine.getLookup().lookup(GraphSelection.class);

        Node[] clickedNodes = null;
        if (!graphSelection.getMode().equals(GraphSelection.GraphSelectionMode.CUSTOM_SELECTION)) {
            clickedNodes = graphSelection.getSelectedNodes().toArray(new Node[0]);
        } else {
            clickedNodes = graphIndex.getNodesUnderPosition(mouseWorldPosition.x, mouseWorldPosition.y).toArray();
        }

        //Node Left click
        final VisualizationEventTypeHandler nodeLeftClickHandler = handlers[VisualizationEvent.Type.NODE_LEFT_CLICK.ordinal()];
        if (nodeLeftClickHandler.hasListeners() && clickedNodes.length > 0) {
            if (nodeLeftClickHandler.dispatch(clickedNodes)) {
                return true;
            }
        }

        //Mouse left click
        final VisualizationEventTypeHandler mouseLeftClickHandler = handlers[VisualizationEvent.Type.MOUSE_LEFT_CLICK.ordinal()];
        if (mouseLeftClickHandler.hasListeners() && clickedNodes.length == 0) {
            return mouseLeftClickHandler.dispatch(
                    getScreenAndWorldPositionsArray(mouseScreenPosition, mouseWorldPosition)
            );
        }

        return false;
    }

    private float[] getScreenAndWorldPositionsArray(Vector2i screenPosition, Vector2f worldPosition) {
        return new float[] {
            screenPosition.x(), screenPosition.y(),
            worldPosition.x(), worldPosition.y()
        };
    }

    public boolean mouseLeftPress(VizEngine engine) {
        final GraphSelection selectionIndex = engine.getLookup().lookup(GraphSelection.class);

        final VisualizationEventTypeHandler nodeLefPressingHandler = handlers[VisualizationEvent.Type.NODE_LEFT_PRESSING.ordinal()];
        if (nodeLefPressingHandler.hasListeners()) {
            //Check if some node are selected
            final Set<Node> selectedNodes = selectionIndex.getSelectedNodes();
            if (!selectedNodes.isEmpty()) {
                startPressingThread(engine);
                return nodeLefPressingHandler.dispatch(toArray(selectedNodes));
            }
        }

        final VisualizationEventTypeHandler nodeLefPressHandler = handlers[VisualizationEvent.Type.NODE_LEFT_PRESS.ordinal()];
        if (nodeLefPressHandler.hasListeners()) {
            //Check if some node are selected
            final Set<Node> selectedNodes = selectionIndex.getSelectedNodes();
            if (!selectedNodes.isEmpty()) {
                return nodeLefPressHandler.dispatch(toArray(selectedNodes));
            }
        }

        return handlers[VisualizationEvent.Type.MOUSE_LEFT_PRESS.ordinal()].dispatch();
    }

    private void startPressingThread(final VizEngine engine) {
        final GraphSelection selectionIndex = engine.getLookup().lookup(GraphSelection.class);
        synchronized (pressingLock) {
            // Stop any existing pressing thread
            stopPressingThread();

            shouldStopPressing = false;
            pressingThread = new Thread(() -> {
                final long intervalMs = 1000 / PRESSING_FREQUENCY;

                while (!shouldStopPressing && !Thread.currentThread().isInterrupted()) {
                    try {
                        Thread.sleep(intervalMs);

                        if (!shouldStopPressing) {
                            final Set<Node> selectedNodes = selectionIndex.getSelectedNodes();
                            if (!selectedNodes.isEmpty()) {
                                final Node[] nodesArray = toArray(selectedNodes);
                                handlers[VisualizationEvent.Type.NODE_LEFT_PRESSING.ordinal()].dispatch(nodesArray);
                            }
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });

            pressingThread.setDaemon(true);
            pressingThread.start();
        }
    }

    private void stopPressingThread() {
        synchronized (pressingLock) {
            shouldStopPressing = true;
            if (pressingThread != null && pressingThread.isAlive()) {
                pressingThread.interrupt();
                try {
                    pressingThread.join(100); // Wait up to 100ms for thread to finish
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            pressingThread = null;
        }
    }

    private Node[] toArray(Collection<Node> selectedNodes) {
        return selectedNodes.toArray(new Node[0]);
    }

    public boolean mouseMiddleClick(VizEngine engine) {
        return handlers[VisualizationEvent.Type.MOUSE_MIDDLE_CLICK.ordinal()].dispatch();
    }

    public boolean mouseMiddlePress(VizEngine engine) {
        return handlers[VisualizationEvent.Type.MOUSE_MIDDLE_PRESS.ordinal()].dispatch();
    }

    public boolean mouseMove(VizEngine engine) {
        return handlers[VisualizationEvent.Type.MOUSE_MOVE.ordinal()].dispatch();
    }

    public boolean mouseRightClick(Component parentComponent, VizEngineGraphCanvasManager canvasManager, VizEngine engine) {
        GraphContextMenu popupMenu = new GraphContextMenu();
        float globalScale = canvasManager.getSurfaceScale().orElse(1.0f);
        int x = (int) (mouseScreenPosition.x / globalScale);
        int y = (int)(mouseScreenPosition.y / globalScale);
        popupMenu.getMenu(engine).show(parentComponent, x, y);

        return handlers[VisualizationEvent.Type.MOUSE_RIGHT_CLICK.ordinal()].dispatch();
    }

    public boolean mouseRightPress(VizEngine engine) {
        return handlers[VisualizationEvent.Type.MOUSE_RIGHT_PRESS.ordinal()].dispatch();
    }

    public boolean startDrag(VizEngine engine) {
        return handlers[VisualizationEvent.Type.START_DRAG.ordinal()].dispatch();
    }

    public void stopDrag(VizEngine engine) {
        handlers[VisualizationEvent.Type.STOP_DRAG.ordinal()].dispatch();
    }

    public boolean drag(VizEngine engine) {
        final VisualizationEventTypeHandler handler = handlers[VisualizationEvent.Type.DRAG.ordinal()];
        if (handler.hasListeners()) {
            final Vector2i dragScreenDisplacement = new Vector2i(mouseScreenPosition);
            dragScreenDisplacement.sub(dragStartMouseScreenPosition);
            final Vector2f dragWorldDisplacement = new Vector2f(mouseWorldPosition);
            dragWorldDisplacement.sub(dragStartMouseWorldPosition2d);

            return handler.dispatch(
                getScreenAndWorldPositionsArray(dragScreenDisplacement, dragWorldDisplacement)
            );
        }

        return false;
    }

    public void mouseReleased(VizEngine engine) {
        handlers[VisualizationEvent.Type.MOUSE_RELEASED.ordinal()].dispatch();
    }

    //Listeners
    public boolean hasListeners(VisualizationEvent.Type type) {
        return handlers[type.ordinal()].hasListeners();
    }

    public void addListener(VisualizationEventListener listener) {
        handlers[listener.getType().ordinal()].addListener(listener);
    }

    public void removeListener(VisualizationEventListener listener) {
        handlers[listener.getType().ordinal()].removeListener(listener);
    }

    private static class VisualizationEventTypeHandler {

        protected final VisualizationEvent.Type type;
        //Settings
        private final boolean limitRunning;
        //Data
        protected List<WeakReference<VisualizationEventListener>> listeners;
        protected Runnable runnable;
        //States
        protected boolean running;

        public VisualizationEventTypeHandler(VisualizationEvent.Type type, boolean limitRunning) {
            this.limitRunning = limitRunning;
            this.type = type;
            this.listeners = new ArrayList<>();
        }

        protected synchronized void addListener(VisualizationEventListener listener) {
            if (listener == null) {
                return;
            }

            final WeakReference<VisualizationEventListener> weakListener = new WeakReference<>(listener);
            listeners.add(weakListener);
        }

        protected synchronized void removeListener(VisualizationEventListener listener) {
            for (Iterator<WeakReference<VisualizationEventListener>> itr = listeners.iterator(); itr.hasNext(); ) {
                WeakReference<VisualizationEventListener> li = itr.next();
                if (li.get() == listener) {
                    itr.remove();
                }
            }
        }

        protected boolean dispatch() {
            if (limitRunning && running) {
                return false;
            }

            if (!listeners.isEmpty()) {
                running = true;

                final boolean consumed = fireVisualizationEvent(null);
                running = false;

                return consumed;
            }

            return false;
        }

        protected boolean dispatch(final Object data) {
            if (limitRunning && running) {
                return false;
            }
            if (!listeners.isEmpty()) {
                running = true;

                try {
                    final boolean consumed = fireVisualizationEvent(data);
                    running = false;

                    return consumed;
                } catch (Exception e) {
                    Logger.getLogger("").log(Level.SEVERE, null, e);
                }
            }

            return false;
        }

        protected boolean isRunning() {
            return running;
        }

        private synchronized boolean fireVisualizationEvent(Object data) {
            final VisualizationEvent event = new VizEvent(this, type, data);
            for (int i = 0; i < listeners.size(); i++) {
                final WeakReference<VisualizationEventListener> weakListener = listeners.get(i);
                final VisualizationEventListener listener = weakListener.get();

                if (listener != null) {
                    final boolean consumed = listener.handleEvent(event);

                    if (consumed) {
                        return true;
                    }
                }
            }

            return false;
        }

        public boolean hasListeners() {
            return !listeners.isEmpty();
        }

        protected int getIndex() {
            return type.ordinal();
        }
    }
}
