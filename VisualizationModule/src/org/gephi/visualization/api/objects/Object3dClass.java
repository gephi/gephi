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
import org.gephi.visualization.api.initializer.Object3dInitializer;

/**
 *
 * @author Mathieu Bastian
 */
public abstract class Object3dClass {

    private static int IDS = 0;
    private final String name;
    private int classId;
    private boolean enabled;
    private int cacheMarker;

    //Config
    private boolean lod;
    private boolean selectable;

    public Object3dClass(String name, boolean lod, boolean selectable) {
        this.classId = Object3dClass.getNumber();
        this.name = name;
        this.lod = lod;
        this.selectable = selectable;
    }

    public Object3dClass() {
        this.name = "";
    }

    public abstract void addObjectInitializer(Object3dInitializer object3dInitializer);

    public abstract Object3dInitializer getCurrentObject3dInitializer();

    public abstract void setCurrentObject3dInitializer(Object3dInitializer object3dInitializer);

    public abstract List<? extends Object3dInitializer> getObject3dInitializers();

    public int getClassId() {
        return classId;
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

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getCacheMarker() {
        return cacheMarker;
    }

    public void setCacheMarker(int cacheMarker) {
        this.cacheMarker = cacheMarker;
    }

    
    //STATIC
    private synchronized static int getNumber() {
        return IDS++;
    }
}
