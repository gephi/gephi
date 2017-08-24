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

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUquadric;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.gephi.visualization.VizController;
import org.gephi.visualization.VizModel;
import org.gephi.visualization.apiimpl.Scheduler;
import org.gephi.visualization.model.Model;
import org.gephi.visualization.model.edge.EdgeModel;
import org.gephi.visualization.model.edge.EdgeModeler;
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
    private boolean anySelected = false;
    private List<NodeModel> dragSelected;

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

    public synchronized void updateSelection(GL2 gl, GLU glu) {
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
    public synchronized boolean updateWorld() {
        boolean repositioned = octree.repositionNodes();
        boolean updated = dataBridge.updateWorld();

        return repositioned || updated;
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
            float[] backgroundColor = vizController.getVizModel().getBackgroundColorComponents();
            gl.glClearColor(backgroundColor[0], backgroundColor[1], backgroundColor[2], 1f);
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
    public synchronized void display(GL2 gl, GLU glu) {
        markTime++;

        VizModel vizModel = VizController.getInstance().getVizModel();

        //Edges
        if (edgeModeler.isEnabled()) {
            edgeModeler.beforeDisplay(gl, glu);

            for (Iterator<EdgeModel> itr = octree.getEdgeIterator(); itr.hasNext();) {
                EdgeModel obj = itr.next();

                if (obj.markTime != markTime) {
                    obj.display(gl, glu, vizModel);
                    obj.markTime = markTime;
                }
            }
            edgeModeler.afterDisplay(gl, glu);
        }

        markTime++;

        //Arrows
        if (edgeModeler.isEnabled() && vizConfig.isShowArrows() && dataBridge.isDirected()) {
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
        if (nodeModeler.isEnabled()) {
            nodeModeler.beforeDisplay(gl, glu);
            for (Iterator<NodeModel> itr = octree.getNodeIterator(); itr.hasNext();) {
                NodeModel obj = itr.next();
                if (obj.markTime != markTime) {
                    obj.display(gl, glu, vizModel);
                    obj.markTime = markTime;
                }
            }
            nodeModeler.afterDisplay(gl, glu);
        }

        //Labels
        if (vizModel.getTextModel().isShowNodeLabels() || vizModel.getTextModel().isShowEdgeLabels()) {
            markTime++;
            if (nodeModeler.isEnabled() && vizModel.getTextModel().isShowNodeLabels()) {
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

            //Draw rectangles around text for debug
//            for (Iterator<NodeModel> itr = octree.getNodeIterator(); itr.hasNext();) {
//                NodeModel obj = itr.next();
//
//                float textW = obj.getTextWidth();
//                float textH = obj.getTextHeight();
//                float textX = obj.getX();
//                float textY = obj.getY();
//
//                gl.glColor4f(0.3f, 0.3f, 0.3f, 0.3f);
//                gl.glBegin(GL2.GL_QUADS);
//                gl.glVertex3f(textX-textW/2f, textY+textH/2, 0);
//                gl.glVertex3f(textX+textW/2f, textY+textH/2, 0);
//                gl.glVertex3f(textX+textW/2f, textY-textH/2, 0);
//                gl.glVertex3f(textX-textW/2f, textY-textH/2, 0);
//                gl.glEnd();
//            }
            if (edgeModeler.isEnabled() && vizModel.getTextModel().isShowEdgeLabels()) {
                markTime++;
                textManager.getEdgeRenderer().beginRendering();
                textManager.defaultEdgeColor();
                if (textManager.isSelectedOnly()) {
                    for (Iterator<EdgeModel> itr = octree.getEdgeIterator(); itr.hasNext();) {
                        EdgeModel obj = itr.next();
                        if (obj.markTime != markTime) {
                            if ((obj.isSelected() || obj.isAutoSelected()) && obj.isTextVisible()) {
                                textManager.getEdgeRenderer().drawTextEdge(obj);
                            }
                            obj.markTime = markTime;
                        }
                    }
                } else {
                    for (Iterator<EdgeModel> itr = octree.getEdgeIterator(); itr.hasNext();) {
                        EdgeModel obj = itr.next();
                        if (obj.markTime != markTime) {
                            if (obj.isTextVisible()) {
                                textManager.getEdgeRenderer().drawTextEdge(obj);
                            }
                            obj.markTime = markTime;
                        }
                    }
                }
                textManager.getEdgeRenderer().endRendering();
            }
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
        lifeCycle.setInited();
    }

    @Override
    public synchronized void mouseClick() {
        if (vizConfig.isSelectionEnable() && rectangleSelection && !customSelection) {
            Rectangle rectangle = (Rectangle) currentSelectionArea;
            //rectangle.setBlocking(false);
            resetEdgesSelection();

            //Select with click
            boolean someSelection = false;

            for (Iterator<NodeModel> itr = octree.getSelectableNodeIterator(); itr.hasNext();) {
                NodeModel obj = (NodeModel) itr.next();
                if (isUnderMouse(obj)) {
                    if (!obj.isSelected()) {
                        //New selected
                        obj.setSelected(true);
                    }
                    someSelection = true;
                } else if (obj.isSelected()) {
                    someSelection = true;
                }
            }
            if (!(rectangle.isCtrl() && someSelection)) {
                for (NodeModel nm : getSelectedNodes()) {
                    nm.setSelected(false);
                }
                someSelection = false;
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
            if (dragSelected != null) {
                for (NodeModel obj : dragSelected) {
                    float[] mouseDistance = obj.getDragDistanceFromMouse();
                    obj.getNode().setX(drag[0] + mouseDistance[0]);
                    obj.getNode().setY(drag[1] + mouseDistance[1]);
                }
            }
        }
    }

    @Override
    public synchronized void mouseMove() {
        //Selection
        if (vizConfig.isSelectionEnable() && rectangleSelection) {
            Rectangle rectangle = (Rectangle) currentSelectionArea;
            rectangle.setMousePosition(graphIO.getMousePosition(), graphIO.getMousePosition3d());
            if (rectangle.isStop()) {
                return;
            }
        }

        if (customSelection || currentSelectionArea != null && currentSelectionArea.blockSelection()) {
            return;
        }

        if (!rectangleSelection && graphIO.isDragging()) {
            return;
        }

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
        dragSelected = getSelectedNodes();

        for (NodeModel selected : dragSelected) {
            float[] tab = selected.getDragDistanceFromMouse();
            tab[0] = selected.getNode().x() - x;
            tab[1] = selected.getNode().y() - y;
        }
    }

    @Override
    public void stopDrag() {
        //Selection
        if (vizConfig.isSelectionEnable() && rectangleSelection) {
            Rectangle rectangle = (Rectangle) currentSelectionArea;
            rectangle.stop();
            scheduler.requireUpdateSelection();
        }
        dragSelected = null;
    }

    @Override
    public synchronized void updateLOD() {
        Iterator<NodeModel> iterator = octree.getNodeIterator();
        for (; iterator.hasNext();) {
            NodeModel obj = iterator.next();
            nodeModeler.chooseModel(obj);
        }
    }

    @Override
    public synchronized List<NodeModel> getSelectedNodes() {
        List<NodeModel> selected = new ArrayList<>();
        for (Iterator<NodeModel> itr = octree.getNodeIterator(); itr.hasNext();) {
            NodeModel nodeModel = itr.next();
            if (nodeModel.isSelected()) {
                selected.add(nodeModel);
            }
        }
        return selected;
    }

    @Override
    public synchronized List<EdgeModel> getSelectedEdges() {
        List<EdgeModel> selected = new ArrayList<>();
        for (Iterator<EdgeModel> itr = octree.getEdgeIterator(); itr.hasNext();) {
            EdgeModel edgeModel = itr.next();
            if (edgeModel.isSelected()) {
                selected.add(edgeModel);
            }
        }
        return selected;
    }

    @Override
    public synchronized List<Node> getSelectedUnderlyingNodes() {
        List<Node> selected = new ArrayList<>();
        for (Iterator<NodeModel> itr = octree.getNodeIterator(); itr.hasNext();) {
            NodeModel nodeModel = itr.next();
            if (nodeModel.isSelected()) {
                selected.add(nodeModel.getNode());
            }
        }
        return selected;
    }

    @Override
    public synchronized List<Edge> getSelectedUnderlyingEdges() {
        List<Edge> selected = new ArrayList<>();
        for (Iterator<EdgeModel> itr = octree.getEdgeIterator(); itr.hasNext();) {
            EdgeModel edgeModel = itr.next();
            if (edgeModel.isSelected()) {
                selected.add(edgeModel.getEdge());
            }
        }
        return selected;
    }

    @Override
    public void selectObject(Model modl) {
        selectObject(new Model[]{modl});
    }

    @Override
    public void selectObject(Model[] objs) {
        if (!customSelection) {
            vizConfig.setRectangleSelection(false);
            customSelection = true;
        }
        
        if (objs != null) {
            for (Model mdl : objs) {
                if (mdl != null) {
                    mdl.setSelected(true);
                    anySelected = true;
                }
            }
            
            forceHighlight();
        }
        
        scheduler.requireUpdateSelection();
        configChanged = true;
    }

    private void forceHighlight() {
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

    @Override
    public synchronized void resetSelection() {
        resetNodesSelection();
        resetEdgesSelection();
        
        customSelection = false;
        configChanged = true;
        anySelected = false;
        vizConfig.setLightenNonSelected(false);
        scheduler.requireUpdateSelection();
    }
    
    private void resetNodesSelection(){
        for (NodeModel selectedNode : getSelectedNodes()) {
            selectedNode.setSelected(false);
        }
    }
    
    private void resetEdgesSelection(){
        for (EdgeModel selectedEdge : getSelectedEdges()) {
            selectedEdge.setSelected(false);
        }
    }

    @Override
    public void initDisplayLists(GL2 gl, GLU glu) {

        //Quadric for all the glu models
        GLUquadric quadric = glu.gluNewQuadric();
        int ptr = gl.glGenLists(4);

        nodeModeler.initDisplayLists(gl, glu, quadric, ptr);

        glu.gluDeleteQuadric(quadric);
    }

    @Override
    public void initObject3dClass() {
        nodeModeler = new NodeModeler(this);
        edgeModeler = new EdgeModeler(this);
        nodeModeler.setEnabled(true);
        edgeModeler.setEnabled(vizController.getVizModel().isShowEdges());
    }

    @Override
    public void initSelection() {
        if (vizConfig.isCustomSelection()) {
            rectangleSelection = false;
            currentSelectionArea = null;
            customSelection = true;
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
