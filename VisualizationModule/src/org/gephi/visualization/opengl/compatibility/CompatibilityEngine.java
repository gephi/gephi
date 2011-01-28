/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.visualization.opengl.compatibility;

import com.sun.opengl.util.BufferUtil;
import java.awt.Color;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;
import org.gephi.graph.api.EdgeData;
import org.gephi.graph.api.Model;
import org.gephi.graph.api.NodeData;
import org.gephi.visualization.VizController;
import org.gephi.visualization.VizModel;
import org.gephi.visualization.api.objects.ModelClass;
import org.gephi.visualization.opengl.AbstractEngine;
import org.gephi.visualization.apiimpl.ModelImpl;
import org.gephi.visualization.api.initializer.CompatibilityModeler;
import org.gephi.visualization.opengl.octree.Octree;
import org.gephi.visualization.apiimpl.Scheduler;
import org.gephi.visualization.api.objects.CompatibilityModelClass;
import org.gephi.visualization.selection.Cylinder;
import org.gephi.visualization.selection.Rectangle;

/**
 *
 * @author Mathieu Bastian
 */
public class CompatibilityEngine extends AbstractEngine {

    private CompatibilityScheduler scheduler;
    private long markTime = 0;
    private long markTime2 = 0;
    //User config
    protected CompatibilityModelClass[] modelClasses;
    protected CompatibilityModelClass[] lodClasses;
    protected CompatibilityModelClass[] selectableClasses;
    protected CompatibilityModelClass[] clickableClasses;
    //Selection
    private ConcurrentLinkedQueue<ModelImpl>[] selectedObjects;
    private boolean anySelected = false;

    public CompatibilityEngine() {
        super();
    }

    @Override
    public void initArchitecture() {
        super.initArchitecture();
        scheduler = (CompatibilityScheduler) VizController.getInstance().getScheduler();
        vizEventManager = VizController.getInstance().getVizEventManager();

        //Init
        octree = new Octree(vizConfig.getOctreeDepth(), vizConfig.getOctreeWidth(), modelClasses.length);
        octree.initArchitecture();
    }

    public void updateSelection(GL gl, GLU glu) {
        if (vizConfig.isSelectionEnable() && currentSelectionArea != null && currentSelectionArea.isEnabled()) {
            VizModel vizModel = VizController.getInstance().getVizModel();
            float[] mp = Arrays.copyOf(graphIO.getMousePosition(), 2);
            float[] cent = currentSelectionArea.getSelectionAreaCenter();
            if (cent != null) {
                mp[0] += cent[0];
                mp[1] += cent[1];
            }
            octree.updateSelectedOctant(gl, glu, mp, currentSelectionArea.getSelectionAreaRectancle());

            for (int i = 0; i < selectableClasses.length; i++) {
                CompatibilityModelClass modelClass = selectableClasses[i];

                if (modelClass.isEnabled() && modelClass.isGlSelection()) {
                    int objectCount = octree.countSelectedObjects(modelClass.getClassId());

                    float[] mousePosition = Arrays.copyOf(graphIO.getMousePosition(), 2);
                    float[] pickRectangle = currentSelectionArea.getSelectionAreaRectancle();
                    float[] center = currentSelectionArea.getSelectionAreaCenter();
                    if (center != null) {
                        mousePosition[0] += center[0];
                        mousePosition[1] += center[1];
                    }
                    int capacity = 1 * 4 * objectCount;      //Each object take in maximium : 4 * name stack depth
                    IntBuffer hitsBuffer = BufferUtil.newIntBuffer(capacity);

                    gl.glSelectBuffer(hitsBuffer.capacity(), hitsBuffer);
                    gl.glRenderMode(GL.GL_SELECT);

                    gl.glInitNames();
                    gl.glPushName(0);

                    gl.glMatrixMode(GL.GL_PROJECTION);
                    gl.glPushMatrix();
                    gl.glLoadIdentity();

                    glu.gluPickMatrix(mousePosition[0], mousePosition[1], pickRectangle[0], pickRectangle[1], graphDrawable.getViewport());
                    gl.glMultMatrixd(graphDrawable.getProjectionMatrix());

                    gl.glMatrixMode(GL.GL_MODELVIEW);

                    int hitName = 1;
                    ModelImpl[] array = new ModelImpl[objectCount];
                    for (Iterator<ModelImpl> itr = octree.getSelectedObjectIterator(modelClass.getClassId()); itr.hasNext();) {
                        ModelImpl obj = itr.next();
                        obj.setAutoSelect(false);

                        array[hitName - 1] = obj;
                        gl.glLoadName(hitName);
                        obj.display(gl, glu, vizModel);
                        hitName++;

                    }

                    //Restoring the original projection matrix
                    gl.glMatrixMode(GL.GL_PROJECTION);
                    gl.glPopMatrix();
                    gl.glMatrixMode(GL.GL_MODELVIEW);
                    gl.glFlush();

                    //Returning to normal rendering mode
                    int nbRecords = gl.glRenderMode(GL.GL_RENDER);

                    //Get the hits and put the node under selection in the selectionArray
                    for (int j = 0; j < nbRecords; j++) {
                        int hit = hitsBuffer.get(j * 4 + 3) - 1; 		//-1 Because of the glPushName(0)
                        ModelImpl obj = array[hit];
                        obj.setAutoSelect(true);
                    }
                }
            }
        }
    }

    @Override
    public boolean updateWorld() {
        boolean res = false;
        boolean changeMode = modeManager.requireModeChange();
        boolean newConfig = configChanged;
        if (changeMode) {
            modeManager.unload();
        }
        if (newConfig) {
            dataBridge.reset();
            if (!vizConfig.isCustomSelection()) {
                //Reset model classes
                for (ModelClass objClass : getModelClasses()) {
                    if (objClass.isEnabled()) {
                        objClass.swapModelers();
                        resetObjectClass(objClass);
                    }
                }
            }

            initSelection();

        }
        if (dataBridge.requireUpdate() || changeMode || newConfig) {
            dataBridge.updateWorld();
            res = true;
        }
        if (changeMode) {
            modeManager.changeMode();
        }
        if (newConfig) {

            configChanged = false;
        }
        return res;
    }

    @Override
    public void worldUpdated(int cacheMarker) {
        octree.setCacheMarker(cacheMarker);
        for (ModelClass objClass : modelClasses) {
            if (objClass.getCacheMarker() == cacheMarker) {
                octree.cleanDeletedObjects(objClass.getClassId());
            }
        }
    }

    @Override
    public void beforeDisplay(GL gl, GLU glu) {
        //Lighten delta
        if (lightenAnimationDelta != 0) {
            float factor = vizConfig.getLightenNonSelectedFactor();
            factor += lightenAnimationDelta;
            if (factor >= 0.5f && factor <= 0.98f) {
                vizConfig.setLightenNonSelectedFactor(factor);
            } else {
                lightenAnimationDelta = 0;
                vizConfig.setLightenNonSelected(anySelected);
            }
        }

        if (backgroundChanged) {
            Color backgroundColor = vizController.getVizModel().getBackgroundColor();
            gl.glClearColor(backgroundColor.getRed() / 255f, backgroundColor.getGreen() / 255f, backgroundColor.getBlue() / 255f, 1f);
            gl.glClear(GL.GL_COLOR_BUFFER_BIT);
            backgroundChanged = false;
        }

        if (reinit) {
            VizController.getInstance().refreshWorkspace();
            dataBridge.reset();
            graphDrawable.initConfig(gl);
            graphDrawable.setCameraLocation(vizController.getVizModel().getCameraPosition());
            graphDrawable.setCameraTarget(vizController.getVizModel().getCameraTarget());
            vizConfig.setCustomSelection(false);
            reinit = false;
        }
    }

    @Override
    public void display(GL gl, GLU glu) {
        for (Iterator<ModelImpl> itr = octree.getObjectIterator(AbstractEngine.CLASS_NODE); itr.hasNext();) {       //TODO Move this
            ModelImpl obj = itr.next();
            modelClasses[AbstractEngine.CLASS_NODE].getCurrentModeler().chooseModel(obj);
            setViewportPosition(obj);
        }

        markTime++;

        CompatibilityModelClass edgeClass = modelClasses[AbstractEngine.CLASS_EDGE];
        CompatibilityModelClass nodeClass = modelClasses[AbstractEngine.CLASS_NODE];
        CompatibilityModelClass arrowClass = modelClasses[AbstractEngine.CLASS_ARROW];
        CompatibilityModelClass potatoClass = modelClasses[AbstractEngine.CLASS_POTATO];

        VizModel vizModel = VizController.getInstance().getVizModel();

        //Potato
        if (potatoClass.isEnabled()) {
            potatoClass.beforeDisplay(gl, glu);
            for (Iterator<ModelImpl> itr = octree.getObjectIterator(AbstractEngine.CLASS_POTATO); itr.hasNext();) {
                ModelImpl obj = itr.next();

                if (obj.markTime != markTime) {
                    obj.display(gl, glu, vizModel);
                    obj.markTime = markTime;
                }

            }
            potatoClass.afterDisplay(gl, glu);
        }

        //Edges
        if (edgeClass.isEnabled()) {
            edgeClass.beforeDisplay(gl, glu);
            for (Iterator<ModelImpl> itr = octree.getObjectIterator(AbstractEngine.CLASS_EDGE); itr.hasNext();) {
                ModelImpl obj = itr.next();
                //Renderable renderable = obj.getObj();

                if (obj.markTime != markTime) {
                    obj.display(gl, glu, vizModel);
                    obj.markTime = markTime;
                }

            }
            edgeClass.afterDisplay(gl, glu);
        }

        //Arrows
        if (arrowClass.isEnabled()) {
            arrowClass.beforeDisplay(gl, glu);
            for (Iterator<ModelImpl> itr = octree.getObjectIterator(AbstractEngine.CLASS_ARROW); itr.hasNext();) {
                ModelImpl obj = itr.next();
                if (obj.markTime != markTime) {
                    obj.display(gl, glu, vizModel);
                    obj.markTime = markTime;
                }
            }
            arrowClass.afterDisplay(gl, glu);
        }

        //Nodes
        if (nodeClass.isEnabled()) {
            nodeClass.beforeDisplay(gl, glu);
            for (Iterator<ModelImpl> itr = octree.getObjectIterator(AbstractEngine.CLASS_NODE); itr.hasNext();) {
                ModelImpl obj = itr.next();
                if (obj.markTime != markTime) {
                    obj.display(gl, glu, vizModel);
                    obj.markTime = markTime;
                }
            }
            nodeClass.afterDisplay(gl, glu);
        }

        //Labels
        if (vizModel.getTextModel().isShowNodeLabels() || vizModel.getTextModel().isShowEdgeLabels()) {
            markTime++;
            if (nodeClass.isEnabled() && vizModel.getTextModel().isShowNodeLabels()) {
                textManager.getNodeRenderer().beginRendering();
                textManager.defaultNodeColor();
                if (textManager.isSelectedOnly()) {
                    for (Iterator<ModelImpl> itr = octree.getObjectIterator(AbstractEngine.CLASS_NODE); itr.hasNext();) {
                        ModelImpl obj = itr.next();
                        if (obj.markTime != markTime) {
                            if ((obj.isSelected() || obj.isHighlight()) && obj.getObj().getTextData().isVisible()) {
                                textManager.getNodeRenderer().drawTextNode(obj);
                            }
                            obj.markTime = markTime;
                        }
                    }
                } else {
                    for (Iterator<ModelImpl> itr = octree.getObjectIterator(AbstractEngine.CLASS_NODE); itr.hasNext();) {
                        ModelImpl obj = itr.next();
                        if (obj.markTime != markTime) {
                            if (obj.getObj().getTextData().isVisible()) {
                                textManager.getNodeRenderer().drawTextNode(obj);
                            }
                            obj.markTime = markTime;
                        }
                    }
                }
                textManager.getNodeRenderer().endRendering();
            }
            if (edgeClass.isEnabled() && vizModel.getTextModel().isShowEdgeLabels()) {
                textManager.getEdgeRenderer().beginRendering();
                textManager.defaultEdgeColor();
                if (textManager.isSelectedOnly()) {
                    for (Iterator<ModelImpl> itr = octree.getObjectIterator(AbstractEngine.CLASS_EDGE); itr.hasNext();) {
                        ModelImpl obj = itr.next();
                        if (obj.markTime != markTime) {
                            if ((obj.isSelected() || obj.isHighlight()) && obj.getObj().getTextData().isVisible()) {
                                textManager.getEdgeRenderer().drawTextEdge(obj);
                            }
                            obj.markTime = markTime;
                        }
                    }
                } else {
                    for (Iterator<ModelImpl> itr = octree.getObjectIterator(AbstractEngine.CLASS_EDGE); itr.hasNext();) {
                        ModelImpl obj = itr.next();
                        if (obj.markTime != markTime) {
                            if (obj.getObj().getTextData().isVisible()) {
                                textManager.getEdgeRenderer().drawTextEdge(obj);
                            }
                            obj.markTime = markTime;
                        }
                    }
                }
                textManager.getEdgeRenderer().endRendering();
            }

        }


        //octree.displayOctree(gl, glu);
    }

    @Override
    public void afterDisplay(GL gl, GLU glu) {
        if (vizConfig.isSelectionEnable() && currentSelectionArea != null) {
            gl.glMatrixMode(GL.GL_PROJECTION);
            gl.glPushMatrix();
            gl.glLoadIdentity();
            gl.glOrtho(0, graphDrawable.getViewportWidth(), 0, graphDrawable.getViewportHeight(), -1, 1);
            gl.glMatrixMode(GL.GL_MODELVIEW);
            gl.glPushMatrix();
            gl.glLoadIdentity();
            currentSelectionArea.drawArea(gl, glu);
            gl.glMatrixMode(GL.GL_PROJECTION);
            gl.glPopMatrix();
            gl.glMatrixMode(GL.GL_MODELVIEW);
            gl.glPopMatrix();
        }
        graphIO.trigger();
    }

    @Override
    public void cameraHasBeenMoved(GL gl, GLU glu) {
    }

    @Override
    public void initEngine(final GL gl, final GLU glu) {
        initDisplayLists(gl, glu);
        scheduler.cameraMoved.set(true);
        scheduler.mouseMoved.set(true);
        lifeCycle.setInited();
    }

    @Override
    public void initScreenshot(GL gl, GLU glu) {
        initDisplayLists(gl, glu);
        textManager.getNodeRenderer().reinitRenderer();
        textManager.getEdgeRenderer().reinitRenderer();
        scheduler.cameraMoved.set(true);
    }

    @Override
    public void addObject(int classID, ModelImpl obj) {
        octree.addObject(classID, obj);
    }

    @Override
    public void removeObject(int classID, ModelImpl obj) {
        octree.removeObject(classID, obj);
    }

    @Override
    public void resetObjectClass(ModelClass object3dClass) {
        octree.resetObjectClass(object3dClass.getClassId());
    }

    @Override
    public void mouseClick() {
        for (ModelClass objClass : clickableClasses) {
            ModelImpl[] objArray = selectedObjects[objClass.getSelectionId()].toArray(new ModelImpl[0]);
            if (objArray.length > 0) {
                eventBridge.mouseClick(objClass, objArray);
            }
        }

        if (vizConfig.isSelectionEnable() && rectangleSelection && !customSelection) {
            Rectangle rectangle = (Rectangle) currentSelectionArea;
            //rectangle.setBlocking(false);
            //Clean opengl picking
            for (ModelClass objClass : selectableClasses) {
                if (objClass.isEnabled() && objClass.isGlSelection()) {
                    for (ModelImpl obj : selectedObjects[objClass.getSelectionId()]) {
                        obj.setAutoSelect(false);
                    }
                }
            }

            //Select with click
            int i = 0;
            boolean someSelection = false;
            for (ModelClass objClass : selectableClasses) {
                markTime2++;
                for (Iterator<ModelImpl> itr = octree.getSelectedObjectIterator(objClass.getClassId()); itr.hasNext();) {
                    ModelImpl obj = itr.next();
                    if (isUnderMouse(obj) && currentSelectionArea.select(obj.getObj())) {
                        if (!obj.isSelected()) {
                            //New selected
                            obj.setSelected(true);
                            /*if (vizEventManager.hasSelectionListeners()) {
                            newSelectedObjects.add(obj);
                            }*/
                            selectedObjects[i].add(obj);
                        }
                        someSelection = true;
                        obj.selectionMark = markTime2;
                    }
                }
                if (!(rectangle.isCtrl() && someSelection)) {
                    for (Iterator<ModelImpl> itr = selectedObjects[i].iterator(); itr.hasNext();) {
                        ModelImpl o = itr.next();
                        if (o.selectionMark != markTime2) {
                            itr.remove();
                            o.setSelected(false);
                        }
                    }
                }

                i++;
            }
            rectangle.setBlocking(someSelection);

            if (vizController.getVizModel().isLightenNonSelectedAuto()) {

                if (vizConfig.isLightenNonSelectedAnimation()) {
                    if (!anySelected && someSelection) {
                        //Start animation
                        lightenAnimationDelta = 0.07f;
                    } else if (anySelected && !someSelection) {
                        //Stop animation
                        lightenAnimationDelta = -0.07f;
                    }

                    vizConfig.setLightenNonSelected(someSelection || lightenAnimationDelta != 0);
                } else {
                    vizConfig.setLightenNonSelected(someSelection);
                }
            }

            anySelected = someSelection;

            scheduler.requireUpdateSelection();
        }
    }

    @Override
    public void mouseDrag() {
        if (vizConfig.isMouseSelectionUpdateWhileDragging()) {
            mouseMove();
        } else {
            float[] drag = graphIO.getMouseDrag3d();
            for (ModelImpl obj : selectedObjects[0]) {
                float[] mouseDistance = obj.getDragDistanceFromMouse();
                obj.getObj().setX(drag[0] + mouseDistance[0]);
                obj.getObj().setY(drag[1] + mouseDistance[1]);
            }
        }
    }

    @Override
    public void mouseMove() {

        //Selection
        if (vizConfig.isSelectionEnable() && rectangleSelection) {
            Rectangle rectangle = (Rectangle) currentSelectionArea;
            rectangle.setMousePosition(graphIO.getMousePosition());
            if (rectangle.isStop()) {
                return;
            }
        }

        if (customSelection || currentSelectionArea.blockSelection()) {
            return;
        }


        /*List<ModelImpl> newSelectedObjects = null;
        List<ModelImpl> unSelectedObjects = null;

        if (vizEventManager.hasSelectionListeners()) {
        newSelectedObjects = new ArrayList<ModelImpl>();
        unSelectedObjects = new ArrayList<ModelImpl>();
        }*/

        markTime2++;
        int i = 0;
        boolean someSelection = false;
        boolean forceUnselect = false;
        for (ModelClass objClass : selectableClasses) {
            forceUnselect = objClass.isAloneSelection() && someSelection;
            for (Iterator<ModelImpl> itr = octree.getSelectedObjectIterator(objClass.getClassId()); itr.hasNext();) {
                ModelImpl obj = itr.next();
                if (!forceUnselect && isUnderMouse(obj) && currentSelectionArea.select(obj.getObj())) {
                    if (!objClass.isAloneSelection()) {  //avoid potatoes to select
                        someSelection = true;
                    }
                    if (!obj.isSelected()) {
                        //New selected
                        obj.setSelected(true);
                        /*if (vizEventManager.hasSelectionListeners()) {
                        newSelectedObjects.add(obj);
                        }*/
                        selectedObjects[i].add(obj);
                    }
                    obj.selectionMark = markTime2;
                } else if (currentSelectionArea.unselect(obj.getObj())) {
                    if (forceUnselect) {
                        obj.setAutoSelect(false);
                    } /*else if (vizEventManager.hasSelectionListeners() && obj.isSelected()) {
                    unSelectedObjects.add(obj);
                    }*/
                }
            }

            for (Iterator<ModelImpl> itr = selectedObjects[i].iterator(); itr.hasNext();) {
                ModelImpl o = itr.next();
                if (o.selectionMark != markTime2) {
                    itr.remove();
                    o.setSelected(false);
                }
            }
            i++;
        }

        if (vizController.getVizModel().isLightenNonSelectedAuto()) {

            if (vizConfig.isLightenNonSelectedAnimation()) {
                if (!anySelected && someSelection) {
                    //Start animation
                    lightenAnimationDelta = 0.07f;
                } else if (anySelected && !someSelection) {
                    //Stop animation
                    lightenAnimationDelta = -0.07f;
                }

                vizConfig.setLightenNonSelected(someSelection || lightenAnimationDelta != 0);
            } else {
                vizConfig.setLightenNonSelected(someSelection);
            }
        }

        anySelected = someSelection;
    }

    @Override
    public void refreshGraphLimits() {
    }

    @Override
    public void startDrag() {
        float x = graphIO.getMouseDrag3d()[0];
        float y = graphIO.getMouseDrag3d()[1];

        for (Iterator<ModelImpl> itr = selectedObjects[0].iterator(); itr.hasNext();) {
            ModelImpl o = itr.next();
            float[] tab = o.getDragDistanceFromMouse();
            tab[0] = o.getObj().x() - x;
            tab[1] = o.getObj().y() - y;
        }
    }

    @Override
    public void stopDrag() {
        scheduler.requireUpdatePosition();

        //Selection
        if (vizConfig.isSelectionEnable() && rectangleSelection) {
            Rectangle rectangle = (Rectangle) currentSelectionArea;
            rectangle.stop();
            scheduler.requireUpdateSelection();
        }
    }

    @Override
    public void updateObjectsPosition() {
        for (ModelClass objClass : modelClasses) {
            if (objClass.isEnabled()) {
                octree.updateObjectsPosition(objClass.getClassId());
            }
        }
    }

    @Override
    public ModelImpl[] getSelectedObjects(int modelClass) {
        return selectedObjects[modelClasses[modelClass].getSelectionId()].toArray(new ModelImpl[0]);
    }

    @Override
    public void selectObject(Model obj) {
        ModelImpl modl = (ModelImpl) obj;
        if (!customSelection) {
            vizConfig.setRectangleSelection(false);
            customSelection = true;
            configChanged = true;
            //Reset
            for (ModelClass objClass : selectableClasses) {
                for (Iterator<ModelImpl> itr = selectedObjects[objClass.getSelectionId()].iterator(); itr.hasNext();) {
                    ModelImpl o = itr.next();
                    itr.remove();
                    o.setSelected(false);
                }
            }
            anySelected = true;
            //Force highlight
            if (vizController.getVizModel().isLightenNonSelectedAuto()) {

                if (vizConfig.isLightenNonSelectedAnimation()) {
                    //Start animation
                    lightenAnimationDelta = 0.07f;
                    vizConfig.setLightenNonSelected(true);
                } else {
                    vizConfig.setLightenNonSelected(true);
                }
            }
        }
        modl.setSelected(true);
        if (modl.getObj() instanceof NodeData) {
            selectedObjects[modelClasses[AbstractEngine.CLASS_NODE].getSelectionId()].add(modl);
        }

        forceSelectRefresh(modelClasses[AbstractEngine.CLASS_EDGE].getClassId());
    }

    @Override
    public void selectObject(Model[] objs) {
        if (!customSelection) {
            vizConfig.setRectangleSelection(false);
            customSelection = true;
            configChanged = true;
            //Reset
            for (ModelClass objClass : selectableClasses) {
                for (Iterator<ModelImpl> itr = selectedObjects[objClass.getSelectionId()].iterator(); itr.hasNext();) {
                    ModelImpl o = itr.next();
                    itr.remove();
                    o.setSelected(false);
                }
            }
            anySelected = true;
            //Force highlight
            if (vizController.getVizModel().isLightenNonSelectedAuto()) {

                if (vizConfig.isLightenNonSelectedAnimation()) {
                    //Start animation
                    lightenAnimationDelta = 0.07f;
                    vizConfig.setLightenNonSelected(true);
                } else {
                    vizConfig.setLightenNonSelected(true);
                }
            }
        } else {
            //Reset
            for (ModelClass objClass : selectableClasses) {
                for (Iterator<ModelImpl> itr = selectedObjects[objClass.getSelectionId()].iterator(); itr.hasNext();) {
                    ModelImpl o = itr.next();
                    itr.remove();
                    o.setSelected(false);
                }
            }

            for (Iterator<ModelImpl> itr = octree.getSelectedObjectIterator(modelClasses[AbstractEngine.CLASS_EDGE].getClassId()); itr.hasNext();) {
                ModelImpl obj = itr.next();
                obj.setSelected(false);
            }
        }
        for (Model r : objs) {
            if (r != null) {
                ModelImpl mdl = (ModelImpl) r;
                mdl.setSelected(true);
                if (mdl.getObj() instanceof NodeData) {
                    selectedObjects[modelClasses[AbstractEngine.CLASS_NODE].getSelectionId()].add(mdl);
                } else if (mdl.getObj() instanceof EdgeData) {
                    selectedObjects[modelClasses[AbstractEngine.CLASS_EDGE].getSelectionId()].add(mdl);
                }
            }
        }

        //forceSelectRefresh(modelClasses[AbstractEngine.CLASS_EDGE].getClassId());
    }

    public void forceSelectRefresh(int selectedClass) {
        for (Iterator<ModelImpl> itr = octree.getSelectedObjectIterator(selectedClass); itr.hasNext();) {
            ModelImpl obj = itr.next();
            if (isUnderMouse(obj)) {
                if (!obj.isSelected()) {
                    //New selected
                    obj.setSelected(true);
                    /*if (vizEventManager.hasSelectionListeners()) {
                    newSelectedObjects.add(obj);
                    }*/
                    selectedObjects[selectedClass].add(obj);
                }
            }
        }
    }

    @Override
    public void resetSelection() {
        customSelection = false;
        configChanged = true;
        anySelected = false;
        for (ModelClass objClass : selectableClasses) {
            selectedObjects[objClass.getSelectionId()].clear();
        }
    }

    private void initDisplayLists(GL gl, GLU glu) {
        //Constants
        float blancCasse[] = {(float) 213 / 255, (float) 208 / 255, (float) 188 / 255, 1.0f};
        float noirCasse[] = {(float) 39 / 255, (float) 25 / 255, (float) 99 / 255, 1.0f};
        float noir[] = {(float) 0 / 255, (float) 0 / 255, (float) 0 / 255, 0.0f};
        float[] shine_low = {10.0f, 0.0f, 0.0f, 0.0f};
        FloatBuffer ambient_metal = FloatBuffer.wrap(noir);
        FloatBuffer diffuse_metal = FloatBuffer.wrap(noirCasse);
        FloatBuffer specular_metal = FloatBuffer.wrap(blancCasse);
        FloatBuffer shininess_metal = FloatBuffer.wrap(shine_low);

        //End

        //Quadric for all the glu models
        GLUquadric quadric = glu.gluNewQuadric();
        int ptr = gl.glGenLists(4);

        // Metal material display list
        int MATTER_METAL = ptr;
        gl.glNewList(MATTER_METAL, GL.GL_COMPILE);
        gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT, ambient_metal);
        gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_DIFFUSE, diffuse_metal);
        gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_SPECULAR, specular_metal);
        gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_SHININESS, shininess_metal);
        gl.glEndList();
        //Fin

        for (CompatibilityModeler cis : modelClasses[CLASS_NODE].getModelers()) {
            int newPtr = cis.initDisplayLists(gl, glu, quadric, ptr);
            ptr = newPtr;
        }

        //modelClasses[CLASS_POTATO].getCurrentModeler().initDisplayLists(gl, glu, quadric, ptr);

        //Fin

        // Sphere with a texture
        //SHAPE_BILLBOARD = SHAPE_SPHERE32 + 1;
		/*gl.glNewList(SHAPE_BILLBOARD,GL.GL_COMPILE);
        textures[0].bind();
        gl.glBegin(GL.GL_TRIANGLE_STRIP);
        // Map the texture and create the vertices for the particle.
        gl.glTexCoord2d(1, 1);
        gl.glVertex3f(0.5f, 0.5f, 0);
        gl.glTexCoord2d(0, 1);
        gl.glVertex3f(-0.5f, 0.5f,0);
        gl.glTexCoord2d(1, 0);
        gl.glVertex3f(0.5f, -0.5f, 0);
        gl.glTexCoord2d(0, 0);
        gl.glVertex3f(-0.5f,-0.5f, 0);
        gl.glEnd();

        gl.glBindTexture(GL.GL_TEXTURE_2D,0);
        gl.glEndList();*/
        //Fin

        glu.gluDeleteQuadric(quadric);
    }

    public void initObject3dClass() {
        modelClasses = modelClassLibrary.createModelClassesCompatibility(this);
        lodClasses = new CompatibilityModelClass[0];
        selectableClasses = new CompatibilityModelClass[0];
        clickableClasses = new CompatibilityModelClass[0];


        modelClasses[CLASS_NODE].setEnabled(true);
        modelClasses[CLASS_EDGE].setEnabled(vizController.getVizModel().isShowEdges());
        modelClasses[CLASS_ARROW].setEnabled(vizConfig.isShowArrows());
        modelClasses[CLASS_POTATO].setEnabled(vizController.getVizModel().isShowHulls());

        //LOD
        ArrayList<ModelClass> classList = new ArrayList<ModelClass>();
        for (ModelClass objClass : modelClasses) {
            if (objClass.isLod()) {
                classList.add(objClass);
            }
        }
        lodClasses = classList.toArray(lodClasses);

        //Selectable
        classList.clear();
        for (ModelClass objClass : modelClasses) {
            if (objClass.isSelectable()) {
                classList.add(objClass);
            }
        }
        selectableClasses = classList.toArray(selectableClasses);

        //Clickable
        classList.clear();
        for (ModelClass objClass : modelClasses) {
            if (objClass.isClickable()) {
                classList.add(objClass);
            }
        }
        clickableClasses = classList.toArray(clickableClasses);

        //Init selection lists
        selectedObjects = new ConcurrentLinkedQueue[selectableClasses.length];
        int i = 0;
        for (ModelClass objClass : selectableClasses) {
            objClass.setSelectionId(i);
            selectedObjects[i] = new ConcurrentLinkedQueue<ModelImpl>();
            i++;
        }
    }

    @Override
    public void initSelection() {
        if (vizConfig.isCustomSelection()) {
            //System.out.println("CustomSelection");
            rectangleSelection = false;
            currentSelectionArea = null;
        } else if (vizConfig.isRectangleSelection()) {
            currentSelectionArea = new Rectangle();
            rectangleSelection = true;
            customSelection = false;
        } else {
            currentSelectionArea = new Cylinder();
            rectangleSelection = false;
            customSelection = false;
        }
    }

    @Override
    public void startAnimating() {
        if (!scheduler.isAnimating()) {
            //System.out.println("start animating");
            scheduler.start();
            graphIO.startMouseListening();
        }
    }

    @Override
    public void stopAnimating() {
        if (scheduler.isAnimating()) {
            //System.out.println("stop animating");
            scheduler.stop();
            graphIO.stopMouseListening();
        }

    }

    public CompatibilityModelClass[] getModelClasses() {
        return modelClasses;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }
}
