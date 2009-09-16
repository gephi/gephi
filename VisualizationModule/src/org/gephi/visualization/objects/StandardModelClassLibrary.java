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
package org.gephi.visualization.objects;

import org.gephi.visualization.VizController;
import org.gephi.visualization.api.initializer.CompatibilityModeler;
import org.gephi.visualization.api.objects.ModelClass;
import org.gephi.visualization.api.objects.ModelClassLibrary;
import org.gephi.visualization.api.objects.CompatibilityModelClass;
import org.gephi.visualization.opengl.AbstractEngine;
import org.gephi.visualization.opengl.compatibility.modeler.CompatibilityArrowModeler;
import org.gephi.visualization.opengl.compatibility.modeler.CompatibilityEdgeModeler;
import org.gephi.visualization.opengl.compatibility.modeler.CompatibilityHullModeler;
import org.gephi.visualization.opengl.compatibility.modeler.CompatibilityNodeDiskModeler;
import org.gephi.visualization.opengl.compatibility.modeler.CompatibilityNodeRectangleModeler;
import org.gephi.visualization.opengl.compatibility.modeler.CompatibilityNodeSphereModeler;

/**
 *
 * @author Mathieu Bastian
 */
public class StandardModelClassLibrary implements ModelClassLibrary {

    private CompatibilityModelClass[] compatibilityModelClasses;

    public CompatibilityModelClass[] createModelClassesCompatibility(AbstractEngine engine) {
        compatibilityModelClasses = new CompatibilityModelClass[4];
        int classIds = 0;

        //NODE
        compatibilityModelClasses[0] = new CompatibilityModelClass("NODE", true, true, true, false, false);
        compatibilityModelClasses[0].setClassId(classIds++);
        CompatibilityNodeSphereModeler modeler3d = new CompatibilityNodeSphereModeler(engine);
        CompatibilityNodeDiskModeler modeler2d = new CompatibilityNodeDiskModeler(engine);
        CompatibilityNodeRectangleModeler modelerRect = new CompatibilityNodeRectangleModeler(engine);
        compatibilityModelClasses[0].addModeler(modeler3d);
        compatibilityModelClasses[0].addModeler(modeler2d);
        compatibilityModelClasses[0].addModeler(modelerRect);
        if (VizController.getInstance().getVizConfig().use3d()) {
            compatibilityModelClasses[0].setCurrentModeler(modeler3d);
        } else {
            compatibilityModelClasses[0].setCurrentModeler(modeler2d);
        }

        //EDGE
        compatibilityModelClasses[1] = new CompatibilityModelClass("EDGE", false, true, false, false, false);
        compatibilityModelClasses[1].setClassId(classIds++);
        CompatibilityEdgeModeler edgeModeler = new CompatibilityEdgeModeler();
        compatibilityModelClasses[1].addModeler(edgeModeler);
        compatibilityModelClasses[1].setCurrentModeler(edgeModeler);

        //ARROW
        compatibilityModelClasses[2] = new CompatibilityModelClass("ARROW", true, false, false, false, false);
        compatibilityModelClasses[2].setClassId(classIds++);
        CompatibilityArrowModeler arrowModeler = new CompatibilityArrowModeler(engine);
        compatibilityModelClasses[2].addModeler(arrowModeler);
        compatibilityModelClasses[2].setCurrentModeler(arrowModeler);

        //POTATO
        compatibilityModelClasses[3] = new CompatibilityModelClass("POTATO", false, true, true, true, true);
        compatibilityModelClasses[3].setClassId(classIds++);
        CompatibilityHullModeler hullModeler = new CompatibilityHullModeler();
        compatibilityModelClasses[3].addModeler(hullModeler);
        compatibilityModelClasses[3].setCurrentModeler(hullModeler);
        //modelClasses[3] = new CompatibilityModelClass("POTATO", false, true, true);
        //modelClasses[3].addModeler(new CompatibilityPotatoModeler());
        return compatibilityModelClasses;
    }

    public ModelClass getNodeClass() {
        return compatibilityModelClasses[0];
    }
}
