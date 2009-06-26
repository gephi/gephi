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
import org.gephi.visualization.api.objects.ModelClassLibrary;
import org.gephi.visualization.api.objects.CompatibilityModelClass;
import org.gephi.visualization.opengl.AbstractEngine;
import org.gephi.visualization.opengl.compatibility.modeler.CompatibilityArrowModeler;
import org.gephi.visualization.opengl.compatibility.modeler.CompatibilityEdgeModeler;
import org.gephi.visualization.opengl.compatibility.modeler.CompatibilityNodeDiskModeler;
import org.gephi.visualization.opengl.compatibility.modeler.CompatibilityNodeRectangleModeler;
import org.gephi.visualization.opengl.compatibility.modeler.CompatibilityNodeSphereModeler;
import org.gephi.visualization.opengl.compatibility.modeler.CompatibilityPotatoModeler;

/**
 *
 * @author Mathieu Bastian
 */
public class StandardModelClassLibrary implements ModelClassLibrary {

    public CompatibilityModelClass[] createModelClassesCompatibility(AbstractEngine engine) {
        CompatibilityModelClass[] modelClasses = new CompatibilityModelClass[4];

        //NODE
        modelClasses[0] = new CompatibilityModelClass("NODE", true, true, true);
        CompatibilityNodeSphereModeler modeler3d = new CompatibilityNodeSphereModeler(engine);
        CompatibilityNodeDiskModeler modeler2d = new CompatibilityNodeDiskModeler(engine);
        CompatibilityNodeRectangleModeler modelerRect = new CompatibilityNodeRectangleModeler(engine);
        /*if(VizController.getInstance().getVizConfig().use3d()) {
        modelClasses[0].addModeler(modeler3d);
        } else {
        modelClasses[0].addModeler(modeler2d);
        }*/
        modelClasses[0].addModeler(modelerRect);

        //EDGE
        modelClasses[1] = new CompatibilityModelClass("EDGE", false, true, false);
        modelClasses[1].addModeler(new CompatibilityEdgeModeler());

        //ARROW
        modelClasses[2] = new CompatibilityModelClass("ARROW", true, false, false);
        modelClasses[2].addModeler(new CompatibilityArrowModeler(engine));

        //POTATO
        modelClasses[3] = new CompatibilityModelClass("POTATO", false, true, true);
        modelClasses[3].addModeler(new CompatibilityPotatoModeler());
        return modelClasses;
    }
}
