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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;
import org.gephi.lib.gleem.linalg.Vecf;
import org.gephi.visualization.VizArchitecture;
import org.gephi.visualization.VizController;
import org.gephi.visualization.api.selection.SelectionArea;
import org.gephi.visualization.apiimpl.Engine;
import org.gephi.visualization.apiimpl.GraphIO;
import org.gephi.visualization.apiimpl.Scheduler;
import org.gephi.visualization.apiimpl.VizConfig;
import org.gephi.visualization.apiimpl.VizEventManager;
import org.gephi.visualization.bridge.DataBridge;
import org.gephi.visualization.model.ModelClass;
import org.gephi.visualization.model.ModelClassLibrary;
import org.gephi.visualization.model.node.NodeModel;
import org.gephi.visualization.octree.Octree;
import org.gephi.visualization.swing.GraphDrawableImpl;
import org.gephi.visualization.text.TextManager;

/**
 * Abstract graphic engine. Real graphic engines inherit from this class and can
 * use the common functionalities.
 *
 * @author Mathieu Bastian
 */
public abstract class AbstractEngine implements Engine, VizArchitecture {

    //Enums
    public enum Limits {

        MIN_X, MAX_X, MIN_Y, MAX_Y, MIN_Z, MAX_Z
    };
    //Architecture
    protected GraphDrawableImpl graphDrawable;
    protected GraphIO graphIO;
    protected VizEventManager vizEventManager;
    protected SelectionArea currentSelectionArea;
    protected ModelClassLibrary modelClassLibrary;
    protected DataBridge dataBridge;
    protected VizController vizController;
    protected VizConfig vizConfig;
    protected TextManager textManager;
    //States
    protected boolean rectangleSelection;
    protected boolean customSelection;
    protected EngineLifeCycle lifeCycle = new EngineLifeCycle();
    protected boolean configChanged = false;
    protected boolean backgroundChanged = false;
    protected boolean reinit = false;
    protected float lightenAnimationDelta = 0f;
    //Octree
    protected Octree octree;
    //User config
    protected ModelClass nodeClass;
    protected ModelClass edgeClass;

    @Override
    public void initArchitecture() {
        this.graphDrawable = VizController.getInstance().getDrawable();
        this.graphIO = VizController.getInstance().getGraphIO();
        this.modelClassLibrary = VizController.getInstance().getModelClassLibrary();
        this.dataBridge = VizController.getInstance().getDataBridge();
        this.vizController = VizController.getInstance();
        this.vizConfig = VizController.getInstance().getVizConfig();
        this.textManager = VizController.getInstance().getTextManager();
        initObject3dClass();
        initSelection();

        //Vizconfig events
        vizController.getVizModel().addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                configChanged = true;
                if (evt.getPropertyName().equals("backgroundColor")) {
                    backgroundChanged = true;
                } else if (evt.getPropertyName().equals("use3d")) {
                    reinit = true;
                }

                edgeClass.setEnabled(vizController.getVizModel().isShowEdges());
            }
        });
    }

    public abstract void beforeDisplay(GL2 gl, GLU glu);

    public abstract void display(GL2 gl, GLU glu);

    public abstract void afterDisplay(GL2 gl, GLU glu);

    public abstract void initEngine(GL2 gl, GLU glu);

    public abstract void initScreenshot(GL2 gl, GLU glu);

    public abstract void cameraHasBeenMoved(GL2 gl, GLU glu);

    public abstract void mouseMove();

    public abstract void mouseDrag();

    public abstract void startDrag();

    public abstract void stopDrag();

    public abstract void mouseClick();

    public abstract Scheduler getScheduler();

//    public abstract void addObject(int classID, Model obj);
//    public abstract void removeObject(int classID, Model obj);
    public abstract void updateObjectsPosition();

    public abstract boolean updateWorld();

    public abstract void refreshGraphLimits();

    public abstract void initObject3dClass();

    public abstract void initSelection();

    protected abstract void startAnimating();

    protected abstract void stopAnimating();

//    public abstract Model[] getSelectedObjects(int modelClass);
//    public abstract void selectNodes(NodeModel obj);
//    public abstract void selectObject(NodeModel[] objs);
    public abstract void resetSelection();

    /**
     * Reset contents of octree for the given class
     */
    public abstract void resetObjectClass(ModelClass object3dClass);

    public void reinit() {
        reinit = true;
    }

    protected boolean isUnderMouse(NodeModel obj) {
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

    public boolean isRectangleSelection() {
        return rectangleSelection;
    }

    public void setRectangleSelection(boolean rectangleSelection) {
        vizConfig.setRectangleSelection(rectangleSelection);
        configChanged = true;
        lightenAnimationDelta = 0;
        vizConfig.setLightenNonSelected(false);
    }

    public void setConfigChanged(boolean configChanged) {
        this.configChanged = configChanged;
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

    public ModelClass getNodeClass() {
        return nodeClass;
    }

    public ModelClass getEdgeClass() {
        return edgeClass;
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
                    //graphDrawable.display();
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
