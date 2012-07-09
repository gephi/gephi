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
package org.gephi.visualization.objects;

import org.gephi.visualization.VizController;
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
        if (VizController.getInstance().getVizModel().isUse3d()) {
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
