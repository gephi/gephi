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
package org.gephi.visualization.text;

import javax.swing.ImageIcon;
import org.gephi.visualization.VizController;
import org.gephi.visualization.apiimpl.VizConfig;
import org.gephi.visualization.model.TextModel;
import org.gephi.visualization.model.edge.EdgeModel;
import org.gephi.visualization.model.node.NodeModel;
import org.gephi.visualization.text.TextManager.Renderer;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class TextColorMode implements ColorMode {

    private final VizConfig vizConfig;
    private float[] color;

    public TextColorMode() {
        this.vizConfig = VizController.getInstance().getVizConfig();
    }

    @Override
    public void defaultNodeColor(Renderer renderer) {
        color = VizController.getInstance().getVizModel().getTextModel().nodeColor;
    }

    @Override
    public void defaultEdgeColor(Renderer renderer) {
        color = VizController.getInstance().getVizModel().getTextModel().edgeColor;
    }

    @Override
    public void textNodeColor(Renderer renderer, NodeModel nodeModel) {
        textColor(renderer, nodeModel, nodeModel.isSelected() || nodeModel.isHighlight());
    }

    @Override
    public void textEdgeColor(Renderer renderer, EdgeModel edgeModel) {
        textColor(renderer, edgeModel, edgeModel.isSelected() || edgeModel.isAutoSelected());
    }

    public void textColor(Renderer renderer, TextModel text, boolean selected) {
        if (text.hasCustomTextColor()) {
            if (vizConfig.isLightenNonSelected()) {
                if (!selected) {
                    float lightColorFactor = 1 - vizConfig.getLightenNonSelectedFactor();
                    renderer.setColor(text.getTextR(), text.getTextG(), text.getTextB(), lightColorFactor);
                } else {
                    renderer.setColor(text.getTextR(), text.getTextG(), text.getTextB(), 1);
                }
            } else {
                renderer.setColor(text.getTextR(), text.getTextG(), text.getTextB(), text.getTextAlpha());
            }
        } else if (vizConfig.isLightenNonSelected()) {
            if (!selected) {
                float lightColorFactor = 1 - vizConfig.getLightenNonSelectedFactor();
                renderer.setColor(color[0], color[1], color[2], lightColorFactor);
            } else {
                renderer.setColor(color[0], color[1], color[2], 1);
            }
        } else {
            renderer.setColor(color[0], color[1], color[2], 1);
        }
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(TextColorMode.class, "TextColorMode.name");
    }

    @Override
    public ImageIcon getIcon() {
        return new ImageIcon(getClass().getResource("/org/gephi/visualization/opengl/text/TextColorMode.png"));
    }

    @Override
    public String toString() {
        return getName();
    }
}
