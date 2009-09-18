/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.visualization.events;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeData;
import org.gephi.visualization.VizController;
import org.gephi.visualization.api.ModelImpl;
import org.gephi.visualization.api.VizEvent;
import org.gephi.visualization.api.VizEventListener;
import org.gephi.visualization.api.VizEventManager;
import org.gephi.visualization.opengl.AbstractEngine;

/**
 *
 * @author Mathieu Bastian
 */
public class StandardVizEventManager implements VizEventManager {

    private ThreadPoolExecutor pool;
    private VizEventTypeHandler[] handlers;
    private AbstractEngine engine;

    public StandardVizEventManager() {
        pool = new ThreadPoolExecutor(0, 1, 60L, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(10));
    }

    public void initArchitecture() {
        engine = VizController.getInstance().getEngine();

        //Set handlers
        ArrayList<VizEventTypeHandler> handlersList = new ArrayList<VizEventTypeHandler>();
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
        handlersList.add(new VizEventTypeHandler(VizEvent.Type.NODE_LEFT_PRESS, false));
        Collections.sort(handlersList, new Comparator() {

            public int compare(Object o1, Object o2) {
                VizEvent.Type t1 = ((VizEventTypeHandler) o1).type;
                VizEvent.Type t2 = ((VizEventTypeHandler) o2).type;
                return t1.compareTo(t2);
            }
        });
        handlers = handlersList.toArray(new VizEventTypeHandler[0]);
    }

    public void mouseLeftClick() {
        handlers[VizEvent.Type.MOUSE_LEFT_CLICK.ordinal()].dispatch();
        VizEventTypeHandler nodeHandler = handlers[VizEvent.Type.NODE_LEFT_CLICK.ordinal()];
        if (nodeHandler.hasListeners()) {
            //Check if some node are selected
            ModelImpl[] modelArray = engine.getSelectedObjects(AbstractEngine.CLASS_NODE);
            Node[] nodeArray = new Node[modelArray.length];
            for (int i = 0; i < modelArray.length; i++) {
                nodeArray[i] = ((NodeData) modelArray[i].getObj()).getNode();
            }
            nodeHandler.dispatch(nodeArray);
        }
    }

    public void mouseLeftPress() {
        handlers[VizEvent.Type.MOUSE_LEFT_PRESS.ordinal()].dispatch();
    }

    public void mouseMiddleClick() {
        handlers[VizEvent.Type.MOUSE_MIDDLE_CLICK.ordinal()].dispatch();
    }

    public void mouseMiddlePress() {
        handlers[VizEvent.Type.MOUSE_LEFT_PRESS.ordinal()].dispatch();
    }

    public void mouseMove() {
        handlers[VizEvent.Type.MOUSE_MOVE.ordinal()].dispatch();
    }

    public void mouseRightClick() {
        handlers[VizEvent.Type.MOUSE_RIGHT_CLICK.ordinal()].dispatch();
    }

    public void mouseRightPress() {
        handlers[VizEvent.Type.MOUSE_RIGHT_PRESS.ordinal()].dispatch();
    }

    public void startDrag() {
        handlers[VizEvent.Type.START_DRAG.ordinal()].dispatch();
    }

    public void stopDrag() {
        handlers[VizEvent.Type.STOP_DRAG.ordinal()].dispatch();
    }

    public void drag() {
        handlers[VizEvent.Type.DRAG.ordinal()].dispatch();
    }

    public boolean hasListeners(VizEvent.Type type) {
        return handlers[type.ordinal()].hasListeners();
    }

    public void addListener(VizEventListener listener) {
        handlers[listener.getType().ordinal()].addListener(listener);
    }

    public void removeListener(VizEventListener listener) {
        handlers[listener.getType().ordinal()].removeListener(listener);
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
            this.listeners = new ArrayList<WeakReference<VizEventListener>>();
            runnable = new Runnable() {

                public void run() {
                    fireVizEvent(null);
                    running = false;
                }
            };
        }

        protected synchronized void addListener(VizEventListener listener) {
            WeakReference<VizEventListener> weakListener = new WeakReference<VizEventListener>(listener);
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

                    public void run() {
                        fireVizEvent(data);
                        running = false;
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
