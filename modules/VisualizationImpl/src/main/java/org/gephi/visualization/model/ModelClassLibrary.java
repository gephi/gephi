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
package org.gephi.visualization.model;

import org.gephi.visualization.VizController;
import org.gephi.visualization.model.edge.ArrowModeler;
import org.gephi.visualization.model.edge.EdgeModeler;
import org.gephi.visualization.model.node.NodeDiskModeler;
import org.gephi.visualization.model.node.NodeRectangleModeler;
import org.gephi.visualization.model.node.NodeSphereModeler;
import org.gephi.visualization.opengl.CompatibilityEngine;

/**
 *
 * @author Mathieu Bastian
 */
public class ModelClassLibrary {

    private ModelClass[] compatibilityModelClasses;

    public void createModelClassesCompatibility(CompatibilityEngine engine) {
        compatibilityModelClasses = new ModelClass[3];
        int classIds = 0;

        //NODE
        ModelClass nodeClass = new ModelClass(classIds++, "NODE", true, true, true, false);
        NodeSphereModeler modeler3d = new NodeSphereModeler(engine);
        NodeDiskModeler modeler2d = new NodeDiskModeler(engine);
        NodeRectangleModeler modelerRect = new NodeRectangleModeler(engine);
        nodeClass.addModeler(modeler3d);
        nodeClass.addModeler(modeler2d);
        nodeClass.addModeler(modelerRect);
        if (VizController.getInstance().getVizModel().isUse3d()) {
            nodeClass.setCurrentModeler(modeler3d);
        } else {
            nodeClass.setCurrentModeler(modeler2d);
        }
        compatibilityModelClasses[0] = nodeClass;

        //EDGE
        ModelClass edgeClass = new ModelClass(classIds++, "EDGE", false, true, false, true);
        EdgeModeler edgeModeler = new EdgeModeler(engine);
        edgeClass.addModeler(edgeModeler);
        edgeClass.setCurrentModeler(edgeModeler);
        compatibilityModelClasses[1] = edgeClass;

        //ARROW
        ModelClass arrowClass = new ModelClass(classIds++, "ARROW", true, false, false, false);
        ArrowModeler arrowModeler = new ArrowModeler(engine);
        arrowClass.addModeler(arrowModeler);
        arrowClass.setCurrentModeler(arrowModeler);
        compatibilityModelClasses[2] = arrowClass;
    }

    public ModelClass getNodeClass() {
        return compatibilityModelClasses[0];
    }

    public ModelClass getEdgeClass() {
        return compatibilityModelClasses[1];
    }

    public ModelClass getArrowClass() {
        return compatibilityModelClasses[2];
    }
}
