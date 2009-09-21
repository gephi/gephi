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
package org.gephi.visualization.opengl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.gephi.visualization.api.ModelImpl;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import org.gephi.visualization.VizArchitecture;
import org.gephi.visualization.VizController;
import org.gephi.visualization.api.Engine;
import org.gephi.visualization.api.GraphIO;
import org.gephi.visualization.api.VizEventManager;
import org.gephi.visualization.api.objects.ModelClass;
import org.gephi.visualization.api.objects.ModelClassLibrary;
import org.gephi.visualization.api.Scheduler;
import org.gephi.visualization.api.VizConfig;
import org.gephi.visualization.api.selection.SelectionArea;
import org.gephi.visualization.bridge.DataBridge;
import org.gephi.visualization.bridge.EventBridge;
import org.gephi.visualization.gleem.linalg.Vecf;
import org.gephi.visualization.mode.ModeManager;
import org.gephi.visualization.opengl.octree.Octree;
import org.gephi.visualization.opengl.text.TextManager;
import org.gephi.visualization.swing.GraphDrawableImpl;

/**
 * Abstract graphic engine. Real graphic engines inherit from this class and can use the common functionalities.
 *
 * @author Mathieu Bastian
 */
public abstract class AbstractEngine implements Engine, VizArchitecture {

    //Enums
    public enum Limits {

        MIN_X, MAX_X, MIN_Y, MAX_Y, MIN_Z, MAX_Z
    };
    public static final int CLASS_NODE = 0;
    public static final int CLASS_EDGE = 1;
    public static final int CLASS_ARROW = 2;
    public static final int CLASS_POTATO = 3;

    //Architecture
    protected GraphDrawableImpl graphDrawable;
    protected GraphIO graphIO;
    protected VizEventManager vizEventManager;
    protected SelectionArea currentSelectionArea;
    protected ModelClassLibrary modelClassLibrary;
    protected DataBridge dataBridge;
    protected EventBridge eventBridge;
    protected VizController vizController;
    protected VizConfig vizConfig;
    protected ModeManager modeManager;
    protected TextManager textManager;

    //States
    protected boolean rectangleSelection;
    protected EngineLifeCycle lifeCycle = new EngineLifeCycle();
    protected boolean configChanged = false;
    protected boolean backgroundChanged = false;
    protected boolean reinit = false;

    //Octree
    protected Octree octree;

    public void initArchitecture() {
        this.graphDrawable = VizController.getInstance().getDrawable();
        this.graphIO = VizController.getInstance().getGraphIO();
        this.modelClassLibrary = VizController.getInstance().getModelClassLibrary();
        this.dataBridge = VizController.getInstance().getDataBridge();
        this.eventBridge = VizController.getInstance().getEventBridge();
        this.vizController = VizController.getInstance();
        this.vizConfig = VizController.getInstance().getVizConfig();
        this.modeManager = VizController.getInstance().getModeManager();
        this.textManager = VizController.getInstance().getTextManager();
        initObject3dClass();
        initSelection();

        //Vizconfig events
        vizController.getVizModel().addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                configChanged = true;
                if (evt.getPropertyName().equals("backgroundColor")) {
                    backgroundChanged = true;
                } else if (evt.getPropertyName().equals("use3d")) {
                    reinit = true;
                }

                getModelClasses()[AbstractEngine.CLASS_EDGE].setEnabled(vizController.getVizModel().isShowEdges());
                getModelClasses()[AbstractEngine.CLASS_ARROW].setEnabled(vizController.getVizModel().isShowEdges() && vizConfig.isShowArrows());
            }
        });
    }

    public abstract void beforeDisplay(GL gl, GLU glu);

    public abstract void display(GL gl, GLU glu);

    public abstract void afterDisplay(GL gl, GLU glu);

    public abstract void initEngine(GL gl, GLU glu);

    public abstract void initScreenshot(GL gl, GLU glu);

    public abstract void cameraHasBeenMoved(GL gl, GLU glu);

    public abstract void mouseMove();

    public abstract void mouseDrag();

    public abstract void startDrag();

    public abstract void stopDrag();

    public abstract void mouseClick();

    public abstract Scheduler getScheduler();

    public abstract void addObject(int classID, ModelImpl obj);

    public abstract void removeObject(int classID, ModelImpl obj);

    public abstract void worldUpdated(int cacheMarker);

    public abstract void updateObjectsPosition();

    public abstract boolean updateWorld();

    public abstract void refreshGraphLimits();

    public abstract void initObject3dClass();

    public abstract void initSelection();

    public abstract ModelClass[] getModelClasses();

    protected abstract void startAnimating();

    protected abstract void stopAnimating();

    public abstract ModelImpl[] getSelectedObjects(int modelClass);

    /**
     * Reset contents of octree for the given class
     */
    public abstract void resetObjectClass(ModelClass object3dClass);

    public float cameraDistance(ModelImpl object) {
        float[] cameraLocation = graphDrawable.getCameraLocation();
        double distance = Math.sqrt(Math.pow((double) object.getObj().x() - cameraLocation[0], 2d) +
                Math.pow((double) object.getObj().y() - cameraLocation[1], 2d) +
                Math.pow((double) object.getObj().z() - cameraLocation[2], 2d));
        object.setCameraDistance((float) distance);

        return (float) distance - object.getObj().getRadius();
    }

    protected void setViewportPosition(ModelImpl object) {
        double[] res = graphDrawable.myGluProject(object.getObj().x(), object.getObj().y(), object.getObj().z());
        object.setViewportX((float) res[0]);
        object.setViewportY((float) res[1]);

        res = graphDrawable.myGluProject(object.getObj().x() + object.getObj().getRadius(), object.getObj().y(), object.getObj().z());
        float rad = Math.abs((float) res[0] - object.getViewportX());
        object.setViewportRadius(rad);
    }

    public void reinit() {
        reinit = true;
    }

    protected boolean isUnderMouse(ModelImpl obj) {
        if (obj.isAutoSelected()) {
            return true;
        }
        if (obj.onlyAutoSelect()) {
            return false;
        }
        if (!currentSelectionArea.isEnabled()) {
            return false;
        }
        float x1 = graphIO.getMousePosition()[0];
        float y1 = graphIO.getMousePosition()[1];

        float x2 = obj.getViewportX();
        float y2 = obj.getViewportY();

        float xDist = Math.abs(x2 - x1);
        float yDist = Math.abs(y2 - y1);

        float distance = (float) Math.sqrt(xDist * xDist + yDist * yDist);

        Vecf d = new Vecf(5);
        d.set(0, xDist);
        d.set(1, yDist);
        d.set(2, distance);

        return currentSelectionArea.mouseTest(d, obj);
    }

    public SelectionArea getCurrentSelectionArea() {
        return currentSelectionArea;
    }

    public void setRectangleSelection(boolean rectangleSelection) {
        vizConfig.setRectangleSelection(rectangleSelection);
        configChanged = true;
    }

    public void startDisplay() {
        lifeCycle.requestStartAnimating();
    }

    public void stopDisplay() {
        lifeCycle.requestStopAnimating();
    }

    public Octree getOctree() {
        return octree;
    }

    protected class EngineLifeCycle {

        private boolean inited;
        private boolean requestAnimation;

        public void requestStartAnimating() {
            if (inited) {
                startAnimating();
            } else {
                requestAnimation = true;
            }
        }

        public void requestStopAnimating() {
            if (inited) {
                stopAnimating();
            }
        }

        public void initEngine() {
        }

        public boolean isInited() {
            return inited;
        }

        public void setInited() {
            if (!inited) {
                inited = true;
                if (requestAnimation) {
                    graphDrawable.display();
                    startAnimating();
                    requestAnimation = false;
                }
            } else {
                dataBridge.reset();
                textManager.initArchitecture();
            }
        }
    }
}
