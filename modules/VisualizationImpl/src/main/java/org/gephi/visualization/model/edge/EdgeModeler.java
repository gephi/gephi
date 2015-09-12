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
package org.gephi.visualization.model.edge;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUquadric;
import org.gephi.graph.api.Edge;
import org.gephi.visualization.model.Modeler;
import org.gephi.visualization.model.Model;
import org.gephi.visualization.model.node.NodeModel;
import org.gephi.visualization.opengl.CompatibilityEngine;

/**
 *
 * @author Mathieu Bastian
 */
public class EdgeModeler extends Modeler {

    public EdgeModeler(CompatibilityEngine engine) {
        super(engine);
    }

    public EdgeModel initModel(Edge edge, NodeModel sourceModel, NodeModel targetModelImpl) {

        EdgeModel edgeModel;
        if (edge.isSelfLoop()) {
            edgeModel = new SelfLoopModel(edge, sourceModel);
        } else {
            edgeModel = new Edge2dModel(edge, sourceModel, targetModelImpl);
        }

        return edgeModel;
    }

    @Override
    public void beforeDisplay(GL2 gl, GLU glu) {
        gl.glBegin(GL2.GL_TRIANGLES);
    }

    @Override
    public void afterDisplay(GL2 gl, GLU glu) {
        gl.glEnd();
    }

    @Override
    public void chooseModel(Model obj) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public int initDisplayLists(GL2 gl, GLU glu, GLUquadric quadric, int ptr) {
        return ptr;
    }

    public boolean isLod() {
        return false;
    }

    public boolean isSelectable() {
        return true;
    }

    public boolean isClickable() {
        return false;
    }

    public boolean isOnlyAutoSelect() {
        return true;
    }
}
