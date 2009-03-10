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
package gephi.visualization.events;

import java.lang.ref.WeakReference;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Mathieu Bastian
 */
public class StandardVizEventManager implements VizEventManager {

    private ThreadPoolExecutor pool;

    //Cached Runnable
    protected Runnable dragRunnable;
    protected Runnable mouseMoveRunnable;

    public StandardVizEventManager()
    {
        pool = new ThreadPoolExecutor(0, 1,  60L, TimeUnit.SECONDS,new LinkedBlockingDeque<Runnable>(1));

        //Init Cached Runnable
        dragRunnable = new Runnable() {
            public void run() {
               fireVizEvent(VizEvent.Type.DRAG);
            }
        };

        mouseMoveRunnable = new Runnable() {
            public void run() {
                fireVizEvent(VizEvent.Type.MOUSE_MOVE);
            }
        };
    }

    public void mouseLeftClick() {
        pool.submit(new Runnable() {
            public void run() {
                 fireVizEvent(VizEvent.Type.MOUSE_LEFT_CLICK);
            }
        });
    }

    public void mouseLeftPress() {
        pool.submit(new Runnable() {
            public void run() {
                 fireVizEvent(VizEvent.Type.MOUSE_LEFT_PRESS);
            }
        });
    }

    public void mouseMiddleClick() {
         pool.submit(new Runnable() {
            public void run() {
                 fireVizEvent(VizEvent.Type.MOUSE_MIDDLE_CLICK);
            }
        });
    }

    public void mouseMiddlePress() {
         pool.submit(new Runnable() {
            public void run() {
                 fireVizEvent(VizEvent.Type.MOUSE_MIDDLE_PRESS);
            }
        });
    }

    public void mouseMove() {
         pool.submit(mouseMoveRunnable);
    }

    public void mouseRightClick() {
        pool.submit(new Runnable() {
            public void run() {
                 fireVizEvent(VizEvent.Type.MOUSE_RIGHT_CLICK);
            }
        });
    }

    public void mouseRightPress() {
        pool.submit(new Runnable() {
            public void run() {
                 fireVizEvent(VizEvent.Type.MOUSE_RIGHT_PRESS);
            }
        });
    }

    public void startDrag() {
         pool.submit(new Runnable() {
            public void run() {
                 fireVizEvent(VizEvent.Type.START_DRAG);
            }
        });
    }

    public void stopDrag() {
         pool.submit(new Runnable() {
            public void run() {
                 fireVizEvent(VizEvent.Type.STOP_DRAG);
            }
        });
    }

    public void drag() {
         pool.submit(dragRunnable);
    }

    protected WeakReference<VizEventListener>[] startDragArray              = new WeakReference[0];
    protected WeakReference<VizEventListener>[] dragArray                   = new WeakReference[0];
    protected WeakReference<VizEventListener>[] stopDragArray               = new WeakReference[0];
    protected WeakReference<VizEventListener>[] mouseLeftPressArray         = new WeakReference[0];
    protected WeakReference<VizEventListener>[] mouseMiddlePressArray       = new WeakReference[0];
    protected WeakReference<VizEventListener>[] mouseRightPressArray        = new WeakReference[0];
    protected WeakReference<VizEventListener>[] mouseLeftClickArray         = new WeakReference[0];
    protected WeakReference<VizEventListener>[] mouseMiddleClickArray       = new WeakReference[0];
    protected WeakReference<VizEventListener>[] mouseRightClickArray        = new WeakReference[0];
    protected WeakReference<VizEventListener>[] mouseMoveArray              = new WeakReference[0];

    private void fireVizEvent(VizEvent event, WeakReference<VizEventListener>[] array) {
        for (int i = 0; i < array.length; i++) {
            WeakReference<VizEventListener> weakListener = array[i];
            if (weakListener != null) {
                weakListener.get().vizEvent(event);
            }
        }
    }

    public synchronized void fireVizEvent(VizEvent.Type type) {
        VizEvent event = new VizEvent(this, type);
        switch (type) {
            case START_DRAG:
                fireVizEvent(event, startDragArray);
                break;
            case DRAG:
                fireVizEvent(event, dragArray);
                break;
            case STOP_DRAG:
                fireVizEvent(event, stopDragArray);
                break;
            case MOUSE_LEFT_CLICK:
                fireVizEvent(event, mouseLeftClickArray);
                break;
            case MOUSE_LEFT_PRESS:
                fireVizEvent(event, mouseLeftPressArray);
                break;
            case MOUSE_MIDDLE_CLICK:
                fireVizEvent(event, mouseMiddleClickArray);
                break;
            case MOUSE_MIDDLE_PRESS:
                fireVizEvent(event, mouseMiddlePressArray);
                break;
            case MOUSE_MOVE:
                fireVizEvent(event, mouseMoveArray);
                break;
            case MOUSE_RIGHT_CLICK:
                fireVizEvent(event, mouseRightClickArray);
                break;
            case MOUSE_RIGHT_PRESS:
                fireVizEvent(event, mouseRightPressArray);
                break;
        }
    }

    public synchronized void addListener(VizEventListener listener, VizEvent.Type[] types) {
        WeakReference<VizEventListener> weakListener = new WeakReference<VizEventListener>(listener);
        for (VizEvent.Type eventType : types) {
            try {
                switch (eventType) {
                    case START_DRAG:
                        startDragArray = addToArray(startDragArray, weakListener);
                        break;
                    case DRAG:
                        dragArray = addToArray(dragArray, weakListener);
                        break;
                    case STOP_DRAG:
                        stopDragArray = addToArray(stopDragArray, weakListener);
                        break;
                    case MOUSE_LEFT_CLICK:
                        mouseLeftClickArray = addToArray(mouseLeftClickArray, weakListener);
                        break;
                    case MOUSE_LEFT_PRESS:
                        mouseLeftPressArray = addToArray(mouseLeftPressArray, weakListener);
                        break;
                    case MOUSE_MIDDLE_CLICK:
                        mouseMiddleClickArray = addToArray(mouseMiddleClickArray, weakListener);
                        break;
                    case MOUSE_MIDDLE_PRESS:
                        mouseMiddlePressArray = addToArray(mouseMiddlePressArray, weakListener);
                        break;
                    case MOUSE_MOVE:
                        mouseMoveArray = addToArray(mouseMoveArray, weakListener);
                        break;
                    case MOUSE_RIGHT_CLICK:
                        mouseRightClickArray = addToArray(mouseRightClickArray, weakListener);
                        break;
                    case MOUSE_RIGHT_PRESS:
                        mouseRightPressArray = addToArray(mouseRightPressArray, weakListener);
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void removeListener(VizEventListener listener, VizEvent.Type[] types) {
        for (VizEvent.Type eventType : types) {
            try {
                switch (eventType) {
                    case START_DRAG:
                        startDragArray = removeFromArray(startDragArray, listener);
                        break;
                    case DRAG:
                        dragArray = removeFromArray(dragArray, listener);
                        break;
                    case STOP_DRAG:
                        stopDragArray = removeFromArray(stopDragArray, listener);
                        break;
                    case MOUSE_LEFT_CLICK:
                        mouseLeftClickArray = removeFromArray(mouseLeftClickArray, listener);
                        break;
                    case MOUSE_LEFT_PRESS:
                        mouseLeftPressArray = removeFromArray(mouseLeftPressArray, listener);
                        break;
                    case MOUSE_MIDDLE_CLICK:
                        mouseMiddleClickArray = removeFromArray(mouseMiddleClickArray, listener);
                        break;
                    case MOUSE_MIDDLE_PRESS:
                        mouseMiddlePressArray = removeFromArray(mouseMiddlePressArray, listener);
                        break;
                    case MOUSE_MOVE:
                        mouseMoveArray = removeFromArray(mouseMoveArray, listener);
                        break;
                    case MOUSE_RIGHT_CLICK:
                        mouseRightClickArray = removeFromArray(mouseRightClickArray, listener);
                        break;
                    case MOUSE_RIGHT_PRESS:
                        mouseRightPressArray = removeFromArray(mouseRightPressArray, listener);
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public WeakReference<VizEventListener>[] addToArray(WeakReference<VizEventListener>[] array, WeakReference<VizEventListener> listener) throws Exception {
        int newLenght = 1;
        for (int i = 0; i < array.length; i++) {
            if (array[i] != null) {
                newLenght++;
            }
        }

        int j = 0;
        WeakReference<VizEventListener>[] newArray = new WeakReference[newLenght];
        for (int i = 0; i < array.length; i++) {
            if (array[i] != null) {
                newArray[j] = array[i];
                j++;
            }
        }
        newArray[j] = listener;
        
        return newArray;
    }

    public WeakReference<VizEventListener>[] removeFromArray(WeakReference<VizEventListener>[] array, VizEventListener listener) throws Exception {
        int newLenght = 0;
        for (int i = 0; i < array.length; i++) {
            if (array[i] != null) {
                newLenght++;
            }

            if (array[i] != null) {
                WeakReference<VizEventListener> weakListener = array[i];
                if (weakListener.get() == listener) {
                    newLenght--;
                }
            }
        }

        int j = 0;
        WeakReference<VizEventListener>[] newArray = new WeakReference[newLenght];
        if (newLenght > 0) {
            for (int i = 0; i < array.length; i++) {
                if (array[i] != null) {
                    WeakReference<VizEventListener> weakListener = array[i];
                    if (weakListener.get() != listener) {
                        newArray[j] = array[i];
                        j++;
                    }
                }
            }
        }
        return newArray;
    }
}
