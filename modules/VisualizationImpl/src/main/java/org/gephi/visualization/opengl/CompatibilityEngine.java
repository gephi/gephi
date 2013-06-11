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
package org.gephi.visualization.opengl;

import java.awt.Color;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.Iterator;
import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;
import org.gephi.visualization.VizController;
import org.gephi.visualization.VizModel;
import org.gephi.visualization.api.initializer.Modeler;
import org.gephi.visualization.apiimpl.Scheduler;
import org.gephi.visualization.model.ModelClass;
import org.gephi.visualization.model.edge.EdgeModel;
import org.gephi.visualization.model.node.NodeModel;
import org.gephi.visualization.model.node.NodeModeler;
import org.gephi.visualization.octree.Octree;
import org.gephi.visualization.scheduler.CompatibilityScheduler;
import org.gephi.visualization.selection.Cylinder;
import org.gephi.visualization.selection.Rectangle;

/**
 *
 * @author Mathieu Bastian
 */
public class CompatibilityEngine extends AbstractEngine {

    private CompatibilityScheduler scheduler;
    private int markTime = 0;
    //Selection
//    private ConcurrentLinkedQueue<ModelImpl>[] selectedObjects;
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
        octree = new Octree(vizConfig.getOctreeDepth(), vizConfig.getOctreeWidth());
        octree.initArchitecture();
    }

    public void updateSelection(GL2 gl, GLU glu) {
        if (vizConfig.isSelectionEnable() && currentSelectionArea != null && currentSelectionArea.isEnabled()) {
            float[] mp = Arrays.copyOf(graphIO.getMousePosition(), 2);
            float[] cent = currentSelectionArea.getSelectionAreaCenter();
            if (cent != null) {
                mp[0] += cent[0];
                mp[1] += cent[1];
            }
            octree.updateSelectedOctant(gl, glu, mp, currentSelectionArea.getSelectionAreaRectancle());
        }
    }

    @Override
    public boolean updateWorld() {
        boolean repositioned = octree.repositionNodes();
        boolean updated = dataBridge.updateWorld();

        return repositioned || updated;
//        boolean res = false;
//        boolean newConfig = configChanged;
//        if (newConfig) {
//            dataBridge.reset();
//            if (!vizConfig.isCustomSelection()) {
//                //Reset model classes
////                for (ModelClass objClass : getModelClasses()) {
////                    if (objClass.isEnabled()) {
////                        objClass.swapModelers();
////                        resetObjectClass(objClass);
////                    }
////                }
//            }
//
//            initSelection();
//
//        }
//        if (dataBridge.requireUpdate() || newConfig) {
//            dataBridge.updateWorld();
//            res = true;
//        }
//        if (newConfig) {
//
//            configChanged = false;
//        }
//        return res;
    }

    @Override
    public void beforeDisplay(GL2 gl, GLU glu) {
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
            gl.glClear(GL2.GL_COLOR_BUFFER_BIT);
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
    public void display(GL2 gl, GLU glu) {
        //Update viewport
        NodeModeler nodeModeler = (NodeModeler) nodeClass.getCurrentModeler();
        for (Iterator<NodeModel> itr = octree.getNodeIterator(); itr.hasNext();) {       //TODO Move this
            NodeModel obj = itr.next();
            nodeModeler.setViewportPosition(obj);
        }

        markTime++;

        VizModel vizModel = VizController.getInstance().getVizModel();

        //Edges
        if (edgeClass.isEnabled()) {
            edgeClass.beforeDisplay(gl, glu);

            for (Iterator<EdgeModel> itr = octree.getEdgeIterator(); itr.hasNext();) {
                EdgeModel obj = itr.next();

                if (obj.markTime != markTime) {
                    obj.display(gl, glu, vizModel);
                    obj.markTime = markTime;
                }
            }
            edgeClass.afterDisplay(gl, glu);
        }

        markTime++;

        //Arrows
        if (edgeClass.isEnabled() && vizConfig.isShowArrows() && dataBridge.isDirected()) {
            gl.glBegin(GL2.GL_TRIANGLES);
            for (Iterator<EdgeModel> itr = octree.getEdgeIterator(); itr.hasNext();) {
                EdgeModel obj = itr.next();
                if (obj.getEdge().isDirected() && obj.markTime != markTime) {
                    obj.displayArrow(gl, glu, vizModel);
                    obj.markTime = markTime;
                }
            }
            gl.glEnd();
        }

        //Nodes
        if (nodeClass.isEnabled()) {
            nodeClass.beforeDisplay(gl, glu);
            for (Iterator<NodeModel> itr = octree.getNodeIterator(); itr.hasNext();) {
                NodeModel obj = itr.next();
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
                    for (Iterator<NodeModel> itr = octree.getNodeIterator(); itr.hasNext();) {
                        NodeModel obj = itr.next();
                        if (obj.markTime != markTime) {
                            if (obj.isSelected() && obj.isTextVisible()) {
                                textManager.getNodeRenderer().drawTextNode(obj);
                            }
                            obj.markTime = markTime;
                        }
                    }
                } else {
                    for (Iterator<NodeModel> itr = octree.getNodeIterator(); itr.hasNext();) {
                        NodeModel obj = itr.next();
                        if (obj.markTime != markTime) {
                            if (obj.isTextVisible()) {
                                textManager.getNodeRenderer().drawTextNode(obj);
                            }
                            obj.markTime = markTime;
                        }
                    }
                }
                textManager.getNodeRenderer().endRendering();
            }
//            if (edgeClass.isEnabled() && vizModel.getTextModel().isShowEdgeLabels()) {
//                textManager.getEdgeRenderer().beginRendering();
//                textManager.defaultEdgeColor();
//                if (textManager.isSelectedOnly()) {
//                    for (Iterator<ModelImpl> itr = octree.getObjectIterator(AbstractEngine.CLASS_EDGE); itr.hasNext();) {
//                        ModelImpl obj = itr.next();
//                        if (obj.markTime != markTime) {
//                            if ((obj.isSelected() || obj.isHighlight()) && obj.getObj().getTextData().isVisible()) {
//                                textManager.getEdgeRenderer().drawTextEdge(obj);
//                            }
//                            obj.markTime = markTime;
//                        }
//                    }
//                } else {
//                    for (Iterator<ModelImpl> itr = octree.getObjectIterator(AbstractEngine.CLASS_EDGE); itr.hasNext();) {
//                        ModelImpl obj = itr.next();
//                        if (obj.markTime != markTime) {
//                            if (obj.getObj().getTextData().isVisible()) {
//                                textManager.getEdgeRenderer().drawTextEdge(obj);
//                            }
//                            obj.markTime = markTime;
//                        }
//                    }
//                }
//                textManager.getEdgeRenderer().endRendering();
//            }
        }


//        octree.displayOctree(gl, glu);
    }

    @Override
    public void afterDisplay(GL2 gl, GLU glu) {
        if (vizConfig.isSelectionEnable() && currentSelectionArea != null) {
            gl.glMatrixMode(GL2.GL_PROJECTION);
            gl.glPushMatrix();
            gl.glLoadIdentity();
            gl.glOrtho(0, graphDrawable.getViewportWidth(), 0, graphDrawable.getViewportHeight(), -1, 1);
            gl.glMatrixMode(GL2.GL_MODELVIEW);
            gl.glPushMatrix();
            gl.glLoadIdentity();
            currentSelectionArea.drawArea(gl, glu);
            gl.glMatrixMode(GL2.GL_PROJECTION);
            gl.glPopMatrix();
            gl.glMatrixMode(GL2.GL_MODELVIEW);
            gl.glPopMatrix();
        }
        graphIO.trigger();
    }

    @Override
    public void cameraHasBeenMoved(GL2 gl, GLU glu) {
    }

    @Override
    public void initEngine(final GL2 gl, final GLU glu) {
        initDisplayLists(gl, glu);
//        scheduler.cameraMoved.set(true);
//        scheduler.mouseMoved.set(true);
        lifeCycle.setInited();
    }

    @Override
    public void initScreenshot(GL2 gl, GLU glu) {
        initDisplayLists(gl, glu);
        textManager.getNodeRenderer().reinitRenderer();
        textManager.getEdgeRenderer().reinitRenderer();
//        scheduler.cameraMoved.set(true);
    }

    @Override
    public void resetObjectClass(ModelClass object3dClass) {
//        octree.resetObjectClass(object3dClass.getClassId());
    }

    @Override
    public void mouseClick() {
        if (vizConfig.isSelectionEnable() && rectangleSelection && !customSelection) {
            Rectangle rectangle = (Rectangle) currentSelectionArea;
            //rectangle.setBlocking(false);

            //Select with click
            int i = 0;
            boolean someSelection = false;

//            for (Iterator<ModelImpl> itr = octree.getSelectedObjectIterator(objClass.getClassId()); itr.hasNext();) {
//                NodeModel obj = (NodeModel) itr.next();
//                if (isUnderMouse(obj)) {
//                    if (!obj.isSelected()) {
//                        //New selected
//                        obj.setSelected(true);
//                        /*if (vizEventManager.hasSelectionListeners()) {
//                         newSelectedObjects.add(obj);
//                         }*/
//                        selectedObjects[i].add(obj);
//                    }
//                    someSelection = true;
//                    obj.selectionMark = markTime2;
//                }
//            }
//            if (!(rectangle.isCtrl() && someSelection)) {
//                for (Iterator<ModelImpl> itr = selectedObjects[i].iterator(); itr.hasNext();) {
//                    ModelImpl o = itr.next();
//                    if (o.selectionMark != markTime2) {
//                        itr.remove();
//                        o.setSelected(false);
//                    }
//                }
//
//
//                i++;
//            }
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
//            float[] drag = graphIO.getMouseDrag3d();
//            for (ModelImpl obj : selectedObjects[0]) {
//                float[] mouseDistance = obj.getDragDistanceFromMouse();
//                obj.getObj().setX(drag[0] + mouseDistance[0]);
//                obj.getObj().setY(drag[1] + mouseDistance[1]);
//            }
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
//
//
//        /*List<ModelImpl> newSelectedObjects = null;
//         List<ModelImpl> unSelectedObjects = null;
//
//         if (vizEventManager.hasSelectionListeners()) {
//         newSelectedObjects = new ArrayList<ModelImpl>();
//         unSelectedObjects = new ArrayList<ModelImpl>();
//         }*/
//
        boolean someSelection = false;
        for (Iterator<NodeModel> itr = octree.getSelectableNodeIterator(); itr.hasNext();) {
            NodeModel obj = itr.next();
            if (isUnderMouse(obj)) {
                if (!obj.isSelected()) {
                    //New selected
                    obj.setSelected(true);
                }
                someSelection = true;
            } else if (obj.isSelected()) {
                obj.setSelected(false);
            }
        }
//
//        for (ModelClass objClass : selectableClasses) {
//            forceUnselect = objClass.isAloneSelection() && someSelection;
//            for (Iterator<ModelImpl> itr = octree.getSelectedObjectIterator(objClass.getClassId()); itr.hasNext();) {
//                ModelImpl obj = itr.next();
//                if (!forceUnselect && isUnderMouse(obj) && currentSelectionArea.select(obj.getObj())) {
//                    if (!objClass.isAloneSelection()) {  //avoid potatoes to select
//                        someSelection = true;
//                    }
//                    if (!obj.isSelected()) {
//                        //New selected
//                        obj.setSelected(true);
//                        /*if (vizEventManager.hasSelectionListeners()) {
//                         newSelectedObjects.add(obj);
//                         }*/
//                        selectedObjects[i].add(obj);
//                    }
//                    obj.selectionMark = markTime2;
//                } else if (currentSelectionArea.unselect(obj.getObj())) {
//                    if (forceUnselect) {
//                        obj.setAutoSelect(false);
//                    } /*else if (vizEventManager.hasSelectionListeners() && obj.isSelected()) {
//                     unSelectedObjects.add(obj);
//                     }*/
//                }
//            }
//
//            for (Iterator<ModelImpl> itr = selectedObjects[i].iterator(); itr.hasNext();) {
//                ModelImpl o = itr.next();
//                if (o.selectionMark != markTime2) {
//                    itr.remove();
//                    o.setSelected(false);
//                }
//            }
//        }
//
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

//        for (Iterator<ModelImpl> itr = selectedObjects[0].iterator(); itr.hasNext();) {
//            ModelImpl o = itr.next();
//            float[] tab = o.getDragDistanceFromMouse();
//            tab[0] = o.getObj().x() - x;
//            tab[1] = o.getObj().y() - y;
//        }
    }

    @Override
    public void stopDrag() {

        //Selection
        if (vizConfig.isSelectionEnable() && rectangleSelection) {
            Rectangle rectangle = (Rectangle) currentSelectionArea;
            rectangle.stop();
            scheduler.requireUpdateSelection();
        }
    }

    @Override
    public void updateObjectsPosition() {
//        for (ModelClass objClass : modelClasses) {
//            if (objClass.isEnabled()) {
//                octree.updateObjectsPosition(objClass.getClassId());
//            }
//        }
    }

//    @Override
//    public ModelImpl[] getSelectedObjects(int modelClass) {
//        return selectedObjects[modelClasses[modelClass].getSelectionId()].toArray(new ModelImpl[0]);
//    }
//    @Override
//    public void selectObject(Model obj) {
//        ModelImpl modl = (ModelImpl) obj;
//        if (!customSelection) {
//            vizConfig.setRectangleSelection(false);
//            customSelection = true;
//            configChanged = true;
//            //Reset
//            for (ModelClass objClass : selectableClasses) {
//                for (Iterator<ModelImpl> itr = selectedObjects[objClass.getSelectionId()].iterator(); itr.hasNext();) {
//                    ModelImpl o = itr.next();
//                    itr.remove();
//                    o.setSelected(false);
//                }
//            }
//            anySelected = true;
//            //Force highlight
//            if (vizController.getVizModel().isLightenNonSelectedAuto()) {
//
//                if (vizConfig.isLightenNonSelectedAnimation()) {
//                    //Start animation
//                    lightenAnimationDelta = 0.07f;
//                    vizConfig.setLightenNonSelected(true);
//                } else {
//                    vizConfig.setLightenNonSelected(true);
//                }
//            }
//        }
//        modl.setSelected(true);
//        if (modl.getObj() instanceof NodeData) {
//            selectedObjects[modelClasses[AbstractEngine.CLASS_NODE].getSelectionId()].add(modl);
//        }
//
//        forceSelectRefresh(modelClasses[AbstractEngine.CLASS_EDGE].getClassId());
//    }
//    @Override
//    public void selectObject(Model[] objs) {
//        if (!customSelection) {
//            vizConfig.setRectangleSelection(false);
//            customSelection = true;
//            configChanged = true;
//            //Reset
//            for (ModelClass objClass : selectableClasses) {
//                for (Iterator<ModelImpl> itr = selectedObjects[objClass.getSelectionId()].iterator(); itr.hasNext();) {
//                    ModelImpl o = itr.next();
//                    itr.remove();
//                    o.setSelected(false);
//                }
//            }
//            anySelected = true;
//            //Force highlight
//            if (vizController.getVizModel().isLightenNonSelectedAuto()) {
//
//                if (vizConfig.isLightenNonSelectedAnimation()) {
//                    //Start animation
//                    lightenAnimationDelta = 0.07f;
//                    vizConfig.setLightenNonSelected(true);
//                } else {
//                    vizConfig.setLightenNonSelected(true);
//                }
//            }
//        } else {
//            //Reset
//            for (ModelClass objClass : selectableClasses) {
//                for (Iterator<ModelImpl> itr = selectedObjects[objClass.getSelectionId()].iterator(); itr.hasNext();) {
//                    ModelImpl o = itr.next();
//                    itr.remove();
//                    o.setSelected(false);
//                }
//            }
//
//            for (Iterator<ModelImpl> itr = octree.getSelectedObjectIterator(modelClasses[AbstractEngine.CLASS_EDGE].getClassId()); itr.hasNext();) {
//                ModelImpl obj = itr.next();
//                obj.setSelected(false);
//            }
//        }
//        for (Model r : objs) {
//            if (r != null) {
//                ModelImpl mdl = (ModelImpl) r;
//                mdl.setSelected(true);
//                if (mdl.getObj() instanceof NodeData) {
//                    selectedObjects[modelClasses[AbstractEngine.CLASS_NODE].getSelectionId()].add(mdl);
//                } else if (mdl.getObj() instanceof EdgeData) {
//                    selectedObjects[modelClasses[AbstractEngine.CLASS_EDGE].getSelectionId()].add(mdl);
//                }
//            }
//        }
//
//    }
    public void forceSelectRefresh(int selectedClass) {
//        for (Iterator<ModelImpl> itr = octree.getSelectedObjectIterator(selectedClass); itr.hasNext();) {
//            ModelImpl obj = itr.next();
//            if (isUnderMouse(obj)) {
//                if (!obj.isSelected()) {
//                    //New selected
//                    obj.setSelected(true);
//                    selectedObjects[selectedClass].add(obj);
//                }
//            }
//        }
    }

    @Override
    public void resetSelection() {
        customSelection = false;
        configChanged = true;
        anySelected = false;
//        for (ModelClass objClass : selectableClasses) {
//            selectedObjects[objClass.getSelectionId()].clear();
//        }
    }

    private void initDisplayLists(GL2 gl, GLU glu) {
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
        gl.glNewList(MATTER_METAL, GL2.GL_COMPILE);
        gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT, ambient_metal);
        gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_DIFFUSE, diffuse_metal);
        gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, specular_metal);
        gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_SHININESS, shininess_metal);
        gl.glEndList();
        //Fin

        //Display lists
        for (Modeler cis : nodeClass.getModelers()) {
            int newPtr = cis.initDisplayLists(gl, glu, quadric, ptr);
            ptr = newPtr;
        }
        //Fin

        // Sphere with a texture
        //SHAPE_BILLBOARD = SHAPE_SPHERE32 + 1;
		/*gl.glNewList(SHAPE_BILLBOARD,GL2.GL_COMPILE);
         textures[0].bind();
         gl.glBegin(GL2.GL_TRIANGLE_STRIP);
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

         gl.glBindTexture(GL2.GL_TEXTURE_2D,0);
         gl.glEndList();*/
        //Fin

        glu.gluDeleteQuadric(quadric);
    }

    @Override
    public void initObject3dClass() {
        modelClassLibrary.createModelClassesCompatibility(this);
        nodeClass = modelClassLibrary.getNodeClass();
        edgeClass = modelClassLibrary.getEdgeClass();

        nodeClass.setEnabled(true);
        edgeClass.setEnabled(vizController.getVizModel().isShowEdges());
    }

    @Override
    public void initSelection() {
        if (vizConfig.isCustomSelection()) {
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
            scheduler.start();
            graphIO.startMouseListening();
        }
    }

    @Override
    public void stopAnimating() {
        if (scheduler.isAnimating()) {
            scheduler.stop();
            graphIO.stopMouseListening();
        }

    }

    @Override
    public Scheduler getScheduler() {
        return scheduler;
    }
}
