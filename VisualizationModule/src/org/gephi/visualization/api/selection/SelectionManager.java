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
package org.gephi.visualization.api.selection;

import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.visualization.VizArchitecture;
import org.gephi.visualization.VizController;
import org.gephi.visualization.api.VizConfig;
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

    //States
    private boolean blocked = false;

    public SelectionManager() {
        listeners = new ArrayList<ChangeListener>();
    }

    public void initArchitecture() {
        this.vizConfig = VizController.getInstance().getVizConfig();
        this.engine = VizController.getInstance().getEngine();
        mouseSelectionDiameter = vizConfig.getMouseSelectionDiameter();
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
        fireChangeEvent();
    }

    public void setRectangleSelection() {
        engine.setRectangleSelection(true);
        vizConfig.setDraggingEnable(false);
        vizConfig.setSelectionEnable(true);
        this.blocked = false;
        fireChangeEvent();
    }

    public void setDirectMouseSelection() {
        engine.setRectangleSelection(false);
        vizConfig.setSelectionEnable(true);
        vizConfig.setDraggingEnable(false);
        this.blocked = false;
        fireChangeEvent();
    }

    public void setDraggingMouseSelection() {
        engine.setRectangleSelection(false);
        vizConfig.setDraggingEnable(true);
        vizConfig.setSelectionEnable(true);
        this.blocked = false;
        fireChangeEvent();
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

    public boolean isBlocked() {
        return blocked;
    }

    public boolean isRectangleSelection() {
        return vizConfig.isSelectionEnable() && vizConfig.isRectangleSelection();
    }

    public boolean isDirectMouseSelection() {
        return vizConfig.isSelectionEnable() && !vizConfig.isRectangleSelection() && !vizConfig.isDraggingEnable();
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
