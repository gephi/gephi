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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gephi.graph.api.Node;
import org.gephi.visualization.VizController;
import org.gephi.visualization.apiimpl.GraphIO;
import org.gephi.visualization.apiimpl.VizEvent;
import org.gephi.visualization.apiimpl.VizEventListener;
import org.gephi.visualization.apiimpl.VizEventManager;
import org.gephi.visualization.model.node.NodeModel;
import org.gephi.visualization.opengl.AbstractEngine;

/**
 *
 * @author Mathieu Bastian
 */
public class StandardVizEventManager implements VizEventManager {

    //Architecture
    private AbstractEngine engine;
    private GraphIO graphIO;
    //
    private final ThreadPoolExecutor pool;
    private VizEventTypeHandler[] handlers;

    public StandardVizEventManager() {
        pool = new ThreadPoolExecutor(0, 1, 60L, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(10));
    }

    @Override
    public void initArchitecture() {
        engine = VizController.getInstance().getEngine();
        graphIO = VizController.getInstance().getGraphIO();

        //Set handlers
        ArrayList<VizEventTypeHandler> handlersList = new ArrayList<>();
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

    @Override
    public void mouseLeftClick() {
        //Node Left click
        VizEventTypeHandler nodeLeftHandler = handlers[VizEvent.Type.NODE_LEFT_CLICK.ordinal()];
        if (nodeLeftHandler.hasListeners() && VizController.getInstance().getVizConfig().isSelectionEnable()) {
            //Check if some node are selected
            List<NodeModel> modelArray = engine.getSelectedNodes();
            if (modelArray.size() > 0) {
                Node[] nodeArray = new Node[modelArray.size()];
                for (int i = 0; i < modelArray.size(); i++) {
                    nodeArray[i] = ((NodeModel) modelArray.get(i)).getNode();
                }
                nodeLeftHandler.dispatch(nodeArray);
            }
        }

        //Mouse left click
        VizEventTypeHandler mouseLeftHandler = handlers[VizEvent.Type.MOUSE_LEFT_CLICK.ordinal()];
        if (mouseLeftHandler.hasListeners()) {
            List<NodeModel> modelArray = engine.getSelectedNodes();
            if (modelArray.isEmpty() || !VizController.getInstance().getVizConfig().isSelectionEnable()) {
                float[] mousePositionViewport = graphIO.getMousePosition();
                float[] mousePosition3d = graphIO.getMousePosition3d();
                float[] mousePos = new float[]{mousePositionViewport[0], mousePositionViewport[1], mousePosition3d[0], mousePosition3d[1]};
                handlers[VizEvent.Type.MOUSE_LEFT_CLICK.ordinal()].dispatch(mousePos);
            }
        }
    }

    @Override
    public void mouseLeftPress() {
        handlers[VizEvent.Type.MOUSE_LEFT_PRESS.ordinal()].dispatch();
        pressingTick = PRESSING_FREQUENCY;
        VizEventTypeHandler pressHandler = handlers[VizEvent.Type.NODE_LEFT_PRESS.ordinal()];
        if (pressHandler.hasListeners()) {
            //Check if some node are selected
            List<NodeModel> modelArray = engine.getSelectedNodes();
            if (!modelArray.isEmpty()) {
                Node[] nodeArray = new Node[modelArray.size()];
                for (int i = 0; i < modelArray.size(); i++) {
                    nodeArray[i] = ((NodeModel) modelArray.get(i)).getNode();
                }
                pressHandler.dispatch(nodeArray);
            }
        }
    }

    @Override
    public void mouseMiddleClick() {
        handlers[VizEvent.Type.MOUSE_MIDDLE_CLICK.ordinal()].dispatch();
    }

    @Override
    public void mouseMiddlePress() {
        handlers[VizEvent.Type.MOUSE_LEFT_PRESS.ordinal()].dispatch();
    }

    @Override
    public void mouseMove() {
        handlers[VizEvent.Type.MOUSE_MOVE.ordinal()].dispatch();
    }

    @Override
    public void mouseRightClick() {
        handlers[VizEvent.Type.MOUSE_RIGHT_CLICK.ordinal()].dispatch();
    }

    @Override
    public void mouseRightPress() {
        handlers[VizEvent.Type.MOUSE_RIGHT_PRESS.ordinal()].dispatch();
    }
    private static final int PRESSING_FREQUENCY = 5;
    private int pressingTick = 0;

    @Override
    public void mouseLeftPressing() {
        if (pressingTick++ >= PRESSING_FREQUENCY) {
            pressingTick = 0;
            VizEventTypeHandler nodeHandler = handlers[VizEvent.Type.NODE_LEFT_PRESSING.ordinal()];
            if (nodeHandler.hasListeners()) {
                //Check if some node are selected
                List<NodeModel> modelArray = engine.getSelectedNodes();
                if (!modelArray.isEmpty()) {
                    Node[] nodeArray = new Node[modelArray.size()];
                    for (int i = 0; i < modelArray.size(); i++) {
                        nodeArray[i] = ((NodeModel) modelArray.get(i)).getNode();
                    }
                    nodeHandler.dispatch(nodeArray);
                }
            }
        }
    }

    @Override
    public void startDrag() {
        handlers[VizEvent.Type.START_DRAG.ordinal()].dispatch();
    }

    @Override
    public void stopDrag() {
        handlers[VizEvent.Type.STOP_DRAG.ordinal()].dispatch();
    }
    private static final int DRAGGING_FREQUENCY = 5;
    private int draggingTick = 0;

    @Override
    public void drag() {
        if (draggingTick++ >= DRAGGING_FREQUENCY) {
            draggingTick = 0;
            VizEventTypeHandler handler = handlers[VizEvent.Type.DRAG.ordinal()];
            if (handler.hasListeners()) {
                float[] mouseDrag = Arrays.copyOf(graphIO.getMouseDrag(), 4);
                mouseDrag[2] = graphIO.getMouseDrag3d()[0];
                mouseDrag[3] = graphIO.getMouseDrag3d()[1];
                handler.dispatch(mouseDrag);
            }
        }
    }

    @Override
    public void mouseReleased() {
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

        //Settings
        private final boolean limitRunning;
        //Data
        protected List<WeakReference<VizEventListener>> listeners;
        protected final VizEvent.Type type;
        protected Runnable runnable;
        //States
        protected boolean running;

        public VizEventTypeHandler(VizEvent.Type type, boolean limitRunning) {
            this.limitRunning = limitRunning;
            this.type = type;
            this.listeners = new ArrayList<>();
            runnable = new Runnable() {
                @Override
                public void run() {
                    fireVizEvent(null);
                    running = false;
                }
            };
        }

        protected synchronized void addListener(VizEventListener listener) {
            WeakReference<VizEventListener> weakListener = new WeakReference<>(listener);
            listeners.add(weakListener);
        }

        protected synchronized void removeListener(VizEventListener listener) {
            for (Iterator<WeakReference<VizEventListener>> itr = listeners.iterator(); itr.hasNext();) {
                WeakReference<VizEventListener> li = itr.next();
                if (li.get() == listener) {
                    itr.remove();
                }
            }
        }

        protected void dispatch() {
            if (limitRunning && running) {
                return;
            }
            if (listeners.size() > 0) {
                running = true;
                pool.submit(runnable);
            }
        }

        protected void dispatch(final Object data) {
            if (limitRunning && running) {
                return;
            }
            if (listeners.size() > 0) {
                running = true;
                pool.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            fireVizEvent(data);
                            running = false;
                        } catch (Exception e) {
                            Logger.getLogger("").log(Level.SEVERE, null, e);
                        }
                    }
                });
            }
        }

        protected boolean isRunning() {
            return running;
        }

        private synchronized void fireVizEvent(Object data) {
            VizEvent event = new VizEvent(this, type, data);
            for (int i = 0; i < listeners.size(); i++) {
                WeakReference<VizEventListener> weakListener = listeners.get(i);
                VizEventListener v = weakListener.get();
                v.handleEvent(event);
            }
        }

        public boolean hasListeners() {
            return listeners.size() > 0;
        }

        protected int getIndex() {
            return type.ordinal();
        }
    }
}
