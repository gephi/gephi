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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import org.gephi.visualization.api.initializer.Modeler;
import org.gephi.visualization.api.ModelImpl;
import org.gephi.visualization.api.initializer.CompatibilityModeler;

/**
 *
 * @author Mathieu Bastian
 */
public class CompatibilityModelClass extends ModelClass {

    //Initializer
    private CompatibilityModeler currentModeler;
    private List<CompatibilityModeler> modelers;

    public CompatibilityModelClass(String name, boolean lod, boolean selectable, boolean clickable) {
        super(name, lod, selectable, clickable);
        modelers = new ArrayList<CompatibilityModeler>();
    }

    public void lod(Iterator<ModelImpl> iterator) {
        for (; iterator.hasNext();) {
            ModelImpl obj = iterator.next();
            currentModeler.chooseModel(obj);
        }
    }

    public void beforeDisplay(GL gl, GLU glu) {
        currentModeler.beforeDisplay(gl, glu);
    }

    public void afterDisplay(GL gl, GLU glu) {
        currentModeler.afterDisplay(gl, glu);
    }

    public void addModeler(Modeler modeler) {
        if (modelers.isEmpty()) //Set first one as current
        {
            setCurrentModeler(modeler);
        }

        modelers.add((CompatibilityModeler) modeler);
    }

    @Override
    public void setCurrentModeler(Modeler modeler) {
        currentModeler = (CompatibilityModeler) modeler;
    }

    @Override
    public CompatibilityModeler getCurrentModeler() {
        return currentModeler;
    }

    @Override
    public List<CompatibilityModeler> getModelers() {
        return modelers;
    }
}
