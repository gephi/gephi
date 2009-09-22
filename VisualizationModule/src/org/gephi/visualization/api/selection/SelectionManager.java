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

    //States
    private boolean blocked = false;

    public SelectionManager() {
        listeners = new ArrayList<ChangeListener>();
    }

    public void initArchitecture() {
        this.vizConfig = VizController.getInstance().getVizConfig();
        this.engine = VizController.getInstance().getEngine();
    }

    public void blockSelection(boolean block) {
        if (vizConfig.isRectangleSelection()) {
            this.blocked = block;
            vizConfig.setSelectionEnable(block);
            fireChangeEvent();
        }
    }

    public void setRectangleSelection(boolean rectangleSelection) {
        engine.setRectangleSelection(rectangleSelection);
        vizConfig.setDraggingEnable(false);
        fireChangeEvent();
    }

    public void setDirectMouseSelection(boolean directMouseSelection) {
        engine.setRectangleSelection(directMouseSelection);
        vizConfig.setDraggingEnable(false);
        fireChangeEvent();
    }

    public void setDraggingMouseSelection(boolean draggingMouseSelection) {
        engine.setRectangleSelection(draggingMouseSelection);
        vizConfig.setDraggingEnable(true);
        fireChangeEvent();
    }

    public boolean isBlocked() {
        return blocked;
    }

    public boolean isRectangleSelection() {
        return vizConfig.isSelectionEnable() && vizConfig.isRectangleSelection();
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
