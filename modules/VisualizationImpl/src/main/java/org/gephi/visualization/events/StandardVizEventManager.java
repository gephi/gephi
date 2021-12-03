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
import org.gephi.graph.api.Node;
import org.gephi.visualization.VizController;
import org.gephi.visualization.apiimpl.VizEvent;
import org.gephi.visualization.apiimpl.VizEventListener;
import org.gephi.visualization.apiimpl.VizEventManager;
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.lwjgl.pipeline.events.MouseEvent;
import org.gephi.viz.engine.status.GraphSelection;
import org.joml.Vector2f;
import org.joml.Vector2i;

/**
 * @author Mathieu Bastian
 */
public class StandardVizEventManager implements VizEventManager {

    private static final int PRESSING_FREQUENCY = 5;
    private static final int DRAGGING_FREQUENCY = 5;
    // State
    private final Vector2i dragStartMouseScreenPosition = new Vector2i(0, 0);
    private final Vector2f dragStartMouseWorldPosition2d = new Vector2f(0, 0);

    private final Vector2i previousMouseScreenPosition = new Vector2i(0, 0);
    private final Vector2f previousMouseWorldPosition2d = new Vector2f(0, 0);

    private final Vector2i mouseScreenPosition = new Vector2i(0, 0);
    private final Vector2f mouseWorldPosition = new Vector2f(0, 0);

    //Architecture
    private VizEventTypeHandler[] handlers;
    private int pressingTick = 0;
    private int draggingTick = 0;
    private boolean dragging = false;
    private boolean pressing = false;

    public StandardVizEventManager() {
    }

    @Override
    public boolean processMouseEvent(VizEngine engine, MouseEvent mouseEvent) {
        previousMouseScreenPosition.set(mouseScreenPosition);
        previousMouseWorldPosition2d.set(mouseWorldPosition);

        mouseScreenPosition.set(mouseEvent.x, mouseEvent.y);
        engine.screenCoordinatesToWorldCoordinates(
            mouseScreenPosition.x, mouseScreenPosition.y,
            mouseWorldPosition
        );

        switch (mouseEvent.action) {
            case DRAG:
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
            case MOVE:
                return mouseMove(engine);
            case CLICK:
                switch (mouseEvent.button) {
                    case LEFT:
                        return mouseLeftClick(engine);
                    case RIGHT:
                        return mouseRightClick(engine);
                    case MIDDLE:
                        return mouseMiddleClick(engine);
                }
                return false;
            case PRESS:
                final boolean wasPressing = pressing;
                pressing = true;
                switch (mouseEvent.button) {
                    case LEFT:
                        if (wasPressing) {
                            return mouseLeftPressing(engine);
                        } else {
                            return mouseLeftPress(engine);
                        }
                    case RIGHT:
                        return mouseRightPress(engine);
                    case MIDDLE:
                        return mouseMiddlePress(engine);
                }
                return false;
            case SCROLL:
                //NOOP
                return false;
            case RELEASE:
                pressing = false;
                if (dragging) {
                    stopDrag(engine);
                }
                mouseReleased(engine);

                return false;//Never consume release events
            case DOUBLE_CLICK:
                //NOOP
                return false;
            default:
                return false;
        }
    }

    @Override
    public void initArchitecture() {
        //Set handlers
        final ArrayList<VizEventTypeHandler> handlersList = new ArrayList<>();
        handlersList.add(new VizEventTypeHandler(VizEvent.Type.MOUSE_LEFT_CLICK, false));
        handlersList.add(new VizEventTypeHandler(VizEvent.Type.MOUSE_LEFT_PRESS, false));
        handlersList.add(new VizEventTypeHandler(VizEvent.Type.MOUSE_MIDDLE_CLICK, false));
        handlersList.add(new VizEventTypeHandler(VizEvent.Type.MOUSE_MIDDLE_PRESS, false));
        handlersList.add(new VizEventTypeHandler(VizEvent.Type.MOUSE_RIGHT_CLICK, false));
        handlersList.add(new VizEventTypeHandler(VizEvent.Type.MOUSE_RIGHT_PRESS, false));
        handlersList.add(new VizEventTypeHandler(VizEvent.Type.MOUSE_MOVE, true));
        handlersList.add(new VizEventTypeHandler(VizEvent.Type.START_DRAG, false));
        handlersList.add(new VizEventTypeHandler(VizEvent.Type.DRAG, true));
        handlersList.add(new VizEventTypeHandler(VizEvent.Type.STOP_DRAG, false));
        handlersList.add(new VizEventTypeHandler(VizEvent.Type.NODE_LEFT_CLICK, false));
        handlersList.add(new VizEventTypeHandler(VizEvent.Type.MOUSE_LEFT_PRESSING, false));
        handlersList.add(new VizEventTypeHandler(VizEvent.Type.MOUSE_RELEASED, false));
        handlersList.add(new VizEventTypeHandler(VizEvent.Type.NODE_LEFT_PRESS, false));
        handlersList.add(new VizEventTypeHandler(VizEvent.Type.NODE_LEFT_PRESSING, false));
        Collections.sort(handlersList, new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                VizEvent.Type t1 = ((VizEventTypeHandler) o1).type;
                VizEvent.Type t2 = ((VizEventTypeHandler) o2).type;
                return t1.compareTo(t2);
            }
        });
        handlers = handlersList.toArray(new VizEventTypeHandler[0]);
    }

    public boolean mouseLeftClick(VizEngine engine) {
        final GraphSelection index = engine.getLookup().lookup(GraphSelection.class);

        //Node Left click
        final VizEventTypeHandler nodeLeftClickHandler = handlers[VizEvent.Type.NODE_LEFT_CLICK.ordinal()];
        if (nodeLeftClickHandler.hasListeners() && VizController.getInstance().getVizConfig().isSelectionEnable()) {
            //Check if some node are selected
            final Set<Node> selectedNodes = index.getSelectedNodes();
            if (selectedNodes.size() > 0) {
                if (nodeLeftClickHandler.dispatch(toArray(selectedNodes))) {
                    return true;
                }
            }
        }

        //Mouse left click
        final VizEventTypeHandler mouseLeftClickHandler = handlers[VizEvent.Type.MOUSE_LEFT_CLICK.ordinal()];
        if (mouseLeftClickHandler.hasListeners()) {
            final Set<Node> selectedNodes = index.getSelectedNodes();
            if (selectedNodes.isEmpty() || !VizController.getInstance().getVizConfig().isSelectionEnable()) {
                if (mouseLeftClickHandler.dispatch(
                    getScreenAndWorldPositionsArray(mouseScreenPosition, mouseWorldPosition))) {
                    return true;
                }
            }
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
        final GraphSelection index = engine.getLookup().lookup(GraphSelection.class);

        handlers[VizEvent.Type.MOUSE_LEFT_PRESS.ordinal()].dispatch();
        pressingTick = PRESSING_FREQUENCY;
        final VizEventTypeHandler nodeLefPressHandler = handlers[VizEvent.Type.NODE_LEFT_PRESS.ordinal()];
        if (nodeLefPressHandler.hasListeners()) {
            //Check if some node are selected
            final Set<Node> selectedNodes = index.getSelectedNodes();
            if (selectedNodes.size() > 0) {
                return nodeLefPressHandler.dispatch(toArray(selectedNodes));
            }
        }

        return false;
    }

    private Node[] toArray(Collection<Node> selectedNodes) {
        return selectedNodes.toArray(new Node[0]);
    }

    public boolean mouseMiddleClick(VizEngine engine) {
        return handlers[VizEvent.Type.MOUSE_MIDDLE_CLICK.ordinal()].dispatch();
    }

    public boolean mouseMiddlePress(VizEngine engine) {
        return handlers[VizEvent.Type.MOUSE_LEFT_PRESS.ordinal()].dispatch();
    }

    public boolean mouseMove(VizEngine engine) {
        return handlers[VizEvent.Type.MOUSE_MOVE.ordinal()].dispatch();
    }

    public boolean mouseRightClick(VizEngine engine) {
        return handlers[VizEvent.Type.MOUSE_RIGHT_CLICK.ordinal()].dispatch();
    }

    public boolean mouseRightPress(VizEngine engine) {
        return handlers[VizEvent.Type.MOUSE_RIGHT_PRESS.ordinal()].dispatch();
    }

    public boolean mouseLeftPressing(VizEngine engine) {
        final GraphSelection index = engine.getLookup().lookup(GraphSelection.class);

        if (pressingTick++ >= PRESSING_FREQUENCY) {
            pressingTick = 0;
            final VizEventTypeHandler nodeLeftPressingHandler
                = handlers[VizEvent.Type.NODE_LEFT_PRESSING.ordinal()];
            if (nodeLeftPressingHandler.hasListeners()) {
                //Check if some node are selected
                final Set<Node> selectedNodes = index.getSelectedNodes();
                if (selectedNodes.size() > 0) {
                    return nodeLeftPressingHandler.dispatch(toArray(selectedNodes));
                }
            }
        }

        return false;
    }

    public boolean startDrag(VizEngine engine) {
        return handlers[VizEvent.Type.START_DRAG.ordinal()].dispatch();
    }

    public void stopDrag(VizEngine engine) {
        handlers[VizEvent.Type.STOP_DRAG.ordinal()].dispatch();
    }

    public boolean drag(VizEngine engine) {
        if (draggingTick++ >= DRAGGING_FREQUENCY) {
            draggingTick = 0;
            final VizEventTypeHandler handler = handlers[VizEvent.Type.DRAG.ordinal()];
            if (handler.hasListeners()) {
                final Vector2i dragScreenDisplacement = new Vector2i(dragStartMouseScreenPosition);
                dragScreenDisplacement.sub(mouseScreenPosition);
                final Vector2f dragWorldDisplacement = new Vector2f(dragStartMouseWorldPosition2d);
                dragWorldDisplacement.sub(mouseWorldPosition);

                return handler.dispatch(
                    getScreenAndWorldPositionsArray(dragScreenDisplacement, dragWorldDisplacement)
                );
            }
        }

        return false;
    }

    public void mouseReleased(VizEngine engine) {
        handlers[VizEvent.Type.MOUSE_RELEASED.ordinal()].dispatch();
    }

    //Listeners
    @Override
    public boolean hasListeners(VizEvent.Type type) {
        return handlers[type.ordinal()].hasListeners();
    }

    @Override
    public void addListener(VizEventListener listener) {
        handlers[listener.getType().ordinal()].addListener(listener);
    }

    @Override
    public void removeListener(VizEventListener listener) {
        handlers[listener.getType().ordinal()].removeListener(listener);
    }

    @Override
    public void addListener(VizEventListener[] listeners) {
        for (int i = 0; i < listeners.length; i++) {
            handlers[listeners[i].getType().ordinal()].addListener(listeners[i]);
        }
    }

    @Override
    public void removeListener(VizEventListener[] listeners) {
        for (int i = 0; i < listeners.length; i++) {
            handlers[listeners[i].getType().ordinal()].removeListener(listeners[i]);
        }
    }

    private class VizEventTypeHandler {

        protected final VizEvent.Type type;
        //Settings
        private final boolean limitRunning;
        //Data
        protected List<WeakReference<VizEventListener>> listeners;
        protected Runnable runnable;
        //States
        protected boolean running;

        public VizEventTypeHandler(VizEvent.Type type, boolean limitRunning) {
            this.limitRunning = limitRunning;
            this.type = type;
            this.listeners = new ArrayList<>();
        }

        protected synchronized void addListener(VizEventListener listener) {
            if (listener == null) {
                return;
            }

            final WeakReference<VizEventListener> weakListener = new WeakReference<>(listener);
            listeners.add(weakListener);
        }

        protected synchronized void removeListener(VizEventListener listener) {
            for (Iterator<WeakReference<VizEventListener>> itr = listeners.iterator(); itr.hasNext(); ) {
                WeakReference<VizEventListener> li = itr.next();
                if (li.get() == listener) {
                    itr.remove();
                }
            }
        }

        protected boolean dispatch() {
            if (limitRunning && running) {
                return false;
            }

            if (listeners.size() > 0) {
                running = true;

                final boolean consumed = fireVizEvent(null);
                running = false;

                return consumed;
            }

            return false;
        }

        protected boolean dispatch(final Object data) {
            if (limitRunning && running) {
                return false;
            }
            if (listeners.size() > 0) {
                running = true;

                try {
                    final boolean consumed = fireVizEvent(data);
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

        private synchronized boolean fireVizEvent(Object data) {
            final VizEvent event = new VizEvent(this, type, data);
            for (int i = 0; i < listeners.size(); i++) {
                final WeakReference<VizEventListener> weakListener = listeners.get(i);
                final VizEventListener listener = weakListener.get();

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
            return listeners.size() > 0;
        }

        protected int getIndex() {
            return type.ordinal();
        }
    }
}
