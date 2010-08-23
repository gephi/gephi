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
package org.gephi.visualization.api.selection;

import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Model;
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
    private List<ChangeListener> listeners;
    //Settings
    private int mouseSelectionDiameter;
    private boolean mouseSelectionZoomProportionnal;
    private boolean selectionUpdateWhileDragging;
    //States
    private boolean blocked = false;

    public SelectionManager() {
        listeners = new ArrayList<ChangeListener>();
    }

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
        if (node.getNodeData().getModel() != null) {
            engine.selectObject(node.getNodeData().getModel());
        }
    }

    public void selectEdge(Edge edge) {
        if (!isCustomSelection()) {
            setCustomSelection();
        }
        if (edge.getEdgeData().getModel() != null) {
            engine.selectObject(edge.getEdgeData().getModel());
        }
    }

    public void selectNodes(Node[] nodes) {
        if (!isCustomSelection()) {
            setCustomSelection();
        }
        Model[] models = new Model[nodes.length];
        for(int i=0;i<nodes.length;i++) {
            models[i] = nodes[i].getNodeData().getModel();
        }
        engine.selectObject(models);
    }

    public void selectEdges(Edge[] edges) {
        if (!isCustomSelection()) {
            setCustomSelection();
        }
        for (Edge e : edges) {
            if (e.getEdgeData().getModel() != null) {
                engine.selectObject(e.getEdgeData().getModel());
            }
        }
    }

    public void centerOnNode(Node node) {
        Model model = node.getNodeData().getModel();
        if (model != null) {
            VizController.getInstance().getGraphIO().centerOnCoordinate(model.getObj().x(), model.getObj().y(), model.getObj().z() + model.getObj().getSize() * 8);
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
