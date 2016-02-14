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
package org.gephi.visualization.api.selection;

import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.gephi.visualization.VizArchitecture;
import org.gephi.visualization.VizController;
import org.gephi.visualization.apiimpl.VizConfig;
import org.gephi.visualization.opengl.AbstractEngine;

/**
 *
 * @author Mathieu Bastian
 */
public class SelectionManager implements VizArchitecture {

    private VizConfig vizConfig;
    private AbstractEngine engine;
    private final List<ChangeListener> listeners;
    //Settings
    private int mouseSelectionDiameter;
    private boolean mouseSelectionZoomProportionnal;
    private boolean selectionUpdateWhileDragging;
    //States
    private boolean blocked = false;

    public SelectionManager() {
        listeners = new ArrayList<>();
    }

    @Override
    public void initArchitecture() {
        this.vizConfig = VizController.getInstance().getVizConfig();
        this.engine = VizController.getInstance().getEngine();
        mouseSelectionDiameter = vizConfig.getMouseSelectionDiameter();
        selectionUpdateWhileDragging = vizConfig.isMouseSelectionUpdateWhileDragging();
    }

    public void blockSelection(boolean block) {
        if (vizConfig.isRectangleSelection()) {
            this.blocked = block;
            vizConfig.setSelectionEnable(!block);
            fireChangeEvent();
        } else {
            setDirectMouseSelection();
        }
    }

    public void disableSelection() {
        vizConfig.setSelectionEnable(false);
        this.blocked = false;
        fireChangeEvent();
    }

    public void setDraggingEnable(boolean dragging) {
        vizConfig.setMouseSelectionUpdateWhileDragging(!dragging);
        fireChangeEvent();
    }

    public void setRectangleSelection() {
        engine.setRectangleSelection(true);
        vizConfig.setDraggingEnable(false);
        vizConfig.setCustomSelection(false);
        vizConfig.setSelectionEnable(true);
        this.blocked = false;
        fireChangeEvent();
    }

    public void setDirectMouseSelection() {
        engine.setRectangleSelection(false);
        vizConfig.setSelectionEnable(true);
        vizConfig.setDraggingEnable(false);
        vizConfig.setCustomSelection(false);
        this.blocked = false;
        fireChangeEvent();
    }

    public void setDraggingMouseSelection() {
        engine.setRectangleSelection(false);
        vizConfig.setDraggingEnable(true);
        vizConfig.setMouseSelectionUpdateWhileDragging(false);
        vizConfig.setSelectionEnable(true);
        vizConfig.setCustomSelection(false);
        this.blocked = false;
        fireChangeEvent();
    }

    public void setCustomSelection() {
        vizConfig.setSelectionEnable(false);
        vizConfig.setDraggingEnable(false);
        vizConfig.setCustomSelection(true);
        //this.blocked = true;
        fireChangeEvent();
    }

    public void resetSelection() {
        if (isCustomSelection()) {
            vizConfig.setCustomSelection(false);
            setDirectMouseSelection();
        }
        engine.resetSelection();
    }

    public void selectNode(Node node) {
        if (!isCustomSelection()) {
            setCustomSelection();
        }
//        if (node.getNodeData().getModel() != null) {
//            engine.selectObject(node.getNodeData().getModel());
//        }
    }

    public void selectEdge(Edge edge) {
        if (!isCustomSelection()) {
            setCustomSelection();
        }
//        if (edge.getEdgeData().getModel() != null) {
//            engine.selectObject(edge.getEdgeData().getModel());
//        }
    }

    public void selectNodes(Node[] nodes) {
        if (nodes == null) {
            resetSelection();
            return;
        }
        if (!isCustomSelection()) {
            setCustomSelection();
        }
//        Model[] models = new Model[nodes.length];
        for (int i = 0; i < nodes.length; i++) {
//            models[i] = nodes[i].getNodeData().getModel();
        }
//        engine.selectObject(models);
    }

    public void selectEdges(Edge[] edges) {
        if (!isCustomSelection()) {
            setCustomSelection();
        }
        for (Edge e : edges) {
//            if (e.getEdgeData().getModel() != null) {
//                engine.selectObject(e.getEdgeData().getModel());
//            }
        }
    }

    public void centerOnNode(Node node) {
        if (node != null) {
            VizController.getInstance().getGraphIO().centerOnCoordinate(node.x(), node.y(), node.z() + node.size()* 8);
            engine.getScheduler().requireUpdateVisible();
        }
    }

    public void setMouseSelectionDiameter(int mouseSelectionDiameter) {
        this.mouseSelectionDiameter = mouseSelectionDiameter;
    }

    public int getMouseSelectionDiameter() {
        return mouseSelectionDiameter;
    }

    public void setMouseSelectionZoomProportionnal(boolean mouseSelectionZoomProportionnal) {
        this.mouseSelectionZoomProportionnal = mouseSelectionZoomProportionnal;
    }

    public boolean isMouseSelectionZoomProportionnal() {
        return mouseSelectionZoomProportionnal;
    }

    public boolean isSelectionUpdateWhileDragging() {
        return selectionUpdateWhileDragging;
    }

    public void setSelectionUpdateWhileDragging(boolean selectionUpdateWhileDragging) {
        this.selectionUpdateWhileDragging = selectionUpdateWhileDragging;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public boolean isRectangleSelection() {
        return vizConfig.isSelectionEnable() && vizConfig.isRectangleSelection();
    }

    public boolean isDirectMouseSelection() {
        return vizConfig.isSelectionEnable() && !vizConfig.isRectangleSelection() && !vizConfig.isDraggingEnable();
    }

    public boolean isCustomSelection() {
        return vizConfig.isCustomSelection();
    }

    public boolean isSelectionEnabled() {
        return vizConfig.isSelectionEnable();
    }

    public boolean isDraggingEnabled() {
        return vizConfig.isDraggingEnable();
    }

    //Event
    public void addChangeListener(ChangeListener changeListener) {
        listeners.add(changeListener);
    }

    public void removeChangeListener(ChangeListener changeListener) {
        listeners.remove(changeListener);
    }

    private void fireChangeEvent() {
        ChangeEvent evt = new ChangeEvent(this);
        for (ChangeListener l : listeners) {
            l.stateChanged(evt);
        }
    }
}
