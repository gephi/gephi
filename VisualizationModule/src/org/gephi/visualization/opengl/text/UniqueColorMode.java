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
package org.gephi.visualization.opengl.text;

import org.gephi.visualization.VizController;
import org.gephi.visualization.api.ModelImpl;
import org.gephi.visualization.api.VizConfig;
import org.gephi.visualization.opengl.text.TextManager.Renderer;

/**
 *
 * @author Mathieu Bastian
 */
public class UniqueColorMode implements ColorMode {

    private TextModel textModel;
    private VizConfig vizConfig;
    private float[] color;

    public UniqueColorMode(TextModel model) {
        this.textModel = model;
        this.vizConfig = VizController.getInstance().getVizConfig();
    }

    public void defaultNodeColor(Renderer renderer) {
        color = textModel.nodeColor;
        renderer.setColor(color[0], color[1], color[2], color[3]);
    }

    public void defaultEdgeColor(Renderer renderer) {
        color = textModel.edgeColor;
        renderer.setColor(color[0], color[1], color[2], color[3]);
    }

    public void textColor(Renderer renderer, TextDataImpl text, ModelImpl model) {
        if (text.hasCustomColor()) {
            if (vizConfig.isLightenNonSelected()) {
                if (!model.isSelected() && !model.isHighlight()) {
                    float lightColorFactor = 1 - vizConfig.getLightenNonSelectedFactor();
                    renderer.setColor(text.r, text.g, text.b, lightColorFactor);
                } else {
                    renderer.setColor(text.r, text.g, text.b, 1);
                }
            } else {
                renderer.setColor(text.r, text.g, text.b, text.a);
            }
        } else {
            if (vizConfig.isLightenNonSelected()) {
                if (!model.isSelected() && !model.isHighlight()) {
                    float lightColorFactor = 1 - vizConfig.getLightenNonSelectedFactor();
                    renderer.setColor(color[0], color[1], color[2], lightColorFactor);
                } else {
                    renderer.setColor(color[0], color[1], color[2], 1);
                }
            }
        }
    }
}
