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
package org.gephi.visualization.api.initializer;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;
import org.gephi.visualization.VizController;
import org.gephi.visualization.apiimpl.VizConfig;
import org.gephi.visualization.model.Model;
import org.gephi.visualization.model.node.NodeModel;
import org.gephi.visualization.opengl.CompatibilityEngine;

/**
 *
 * @author Mathieu Bastian
 */
public abstract class Modeler {

    protected final CompatibilityEngine engine;
    protected final VizController controller;
    protected final VizConfig config;

    public Modeler(CompatibilityEngine engine) {
        this.engine = engine;
        this.controller = VizController.getInstance();
        this.config = VizController.getInstance().getVizConfig();
    }

    public abstract int initDisplayLists(GL2 gl, GLU glu, GLUquadric quadric, int ptr);

    public abstract void chooseModel(Model obj);

    public abstract void beforeDisplay(GL2 gl, GLU glu);

    public abstract void afterDisplay(GL2 gl, GLU glu);

    protected float cameraDistance(NodeModel object) {
        float[] cameraLocation = controller.getDrawable().getCameraLocation();
        double distance = Math.sqrt(Math.pow((double) object.getNode().x() - cameraLocation[0], 2d)
                + Math.pow((double) object.getNode().y() - cameraLocation[1], 2d)
                + Math.pow((double) object.getNode().z() - cameraLocation[2], 2d));
        object.setCameraDistance((float) distance);

        return (float) distance - object.getNode().size();
    }
}
