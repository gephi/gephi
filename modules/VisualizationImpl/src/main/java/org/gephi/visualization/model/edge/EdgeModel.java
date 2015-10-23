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

import java.awt.geom.Rectangle2D;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.ElementProperties;
import org.gephi.visualization.VizModel;
import org.gephi.visualization.model.Model;
import org.gephi.visualization.model.TextModel;
import org.gephi.visualization.model.node.NodeModel;

public abstract class EdgeModel implements Model, TextModel {

    protected final Edge edge;
    protected float weight;
    //Flags
    protected boolean selected;
    //Mark
    public int markTime;
    //Id
    protected int octantSourceId;
    protected int octantTargetId;

    public EdgeModel(Edge edge) {
        this.edge = edge;

        //Default
        markTime = 0;
    }

    public abstract NodeModel getSourceModel();

    public abstract NodeModel getTargetModel();

    public abstract boolean isAutoSelected();

    public abstract void displayArrow(GL2 gl, GLU glu, VizModel model);

    public Edge getEdge() {
        return edge;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    @Override
    public boolean hasCustomTextColor() {
        return edge.getTextProperties().getR() > 0;
    }

    @Override
    public void setText(String text) {
        edge.getTextProperties().setText(text);
    }

    @Override
    public float getTextWidth() {
        return edge.getTextProperties().getWidth();
    }

    @Override
    public float getTextHeight() {
        return edge.getTextProperties().getHeight();
    }

    @Override
    public String getText() {
        return edge.getTextProperties().getText();
    }

    @Override
    public float getTextSize() {
        return edge.getTextProperties().getSize();
    }

    @Override
    public float getTextR() {
        return edge.getTextProperties().getR();
    }

    @Override
    public float getTextG() {
        return edge.getTextProperties().getG();
    }

    @Override
    public float getTextB() {
        return edge.getTextProperties().getB();
    }

    @Override
    public float getTextAlpha() {
        return edge.getTextProperties().getAlpha();
    }

    @Override
    public boolean isTextVisible() {
        return edge.getTextProperties().isVisible();
    }

    @Override
    public ElementProperties getElementProperties() {
        return edge;
    }

    public int getOctantSourceId() {
        return octantSourceId;
    }

    public int getOctantTargetId() {
        return octantTargetId;
    }

    public void setOctantSourceId(int octantSourceId) {
        this.octantSourceId = octantSourceId;
    }

    public void setOctantTargetId(int octantTargetId) {
        this.octantTargetId = octantTargetId;
    }
}
