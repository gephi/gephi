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
package org.gephi.visualization.api.objects;

import java.util.List;
import org.gephi.visualization.api.initializer.Modeler;

/**
 *
 * @author Mathieu Bastian
 */
public abstract class ModelClass {

    private final String name;
    private int classId;
    private boolean enabled;
    private int cacheMarker;
    private int selectionId = 0;

    //Config
    private boolean lod;
    private boolean selectable;
    private boolean clickable;
    private boolean glSelection;
    private boolean aloneSelection;

    public ModelClass(String name, boolean lod, boolean selectable, boolean clickable, boolean glSelection, boolean aloneSelection) {
        this.name = name;
        this.lod = lod;
        this.selectable = selectable;
        this.clickable = clickable;
        this.glSelection = glSelection;
        this.aloneSelection = aloneSelection;
    }

    public ModelClass() {
        this.name = "";
    }

    public abstract void addModeler(Modeler modeler);

    public abstract Modeler getCurrentModeler();

    public abstract void setCurrentModeler(Modeler modeler);

    public abstract void setCurrentModeler(String className);

    public abstract List<? extends Modeler> getModelers();

    public abstract void swapModelers();

    public int getClassId() {
        return classId;
    }

    public void setClassId(int classId) {
        this.classId = classId;
    }

    public boolean isLod() {
        return lod;
    }

    public boolean isSelectable() {
        return selectable;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isClickable() {
        return clickable;
    }

    public boolean isGlSelection() {
        return glSelection;
    }

    public boolean isAloneSelection() {
        return aloneSelection;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getCacheMarker() {
        return cacheMarker;
    }

    public void setCacheMarker(int cacheMarker) {
        this.cacheMarker = cacheMarker;
    }

    public int getSelectionId() {
        return selectionId;
    }

    public void setSelectionId(int selectionId) {
        this.selectionId = selectionId;
    }
}
