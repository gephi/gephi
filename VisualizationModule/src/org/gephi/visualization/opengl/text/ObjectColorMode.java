/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.gephi.visualization.opengl.text;

import javax.swing.ImageIcon;
import org.gephi.graph.api.Renderable;
import org.gephi.visualization.VizController;
import org.gephi.visualization.apiimpl.ModelImpl;
import org.gephi.visualization.apiimpl.VizConfig;
import org.gephi.visualization.opengl.text.TextManager.Renderer;

/**
 *
 * @author Mathieu Bastian
 */
public class ObjectColorMode implements ColorMode {

    private VizConfig vizConfig;

    public ObjectColorMode() {
        this.vizConfig = VizController.getInstance().getVizConfig();
    }

    public void defaultEdgeColor(Renderer renderer) {
    }

    public void defaultNodeColor(Renderer renderer) {
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
            Renderable renderable = model.getObj();
            if (vizConfig.isLightenNonSelected()) {
                if (!model.isSelected() && !model.isHighlight()) {
                    float lightColorFactor = 1 - vizConfig.getLightenNonSelectedFactor();
                    renderer.setColor(renderable.r(), renderable.g(), renderable.b(), lightColorFactor);
                } else {
                    renderer.setColor(renderable.r(), renderable.g(), renderable.b(), 1);
                }
            } else {
                renderer.setColor(renderable.r(), renderable.g(), renderable.b(), renderable.alpha());
            }
        }
    }

    public String getName() {
        return "Object";
    }

    public ImageIcon getIcon() {
        return new ImageIcon(getClass().getResource("/org/gephi/visualization/opengl/text/ObjectColorMode.png"));
    }

    @Override
    public String toString() {
        return getName();
    }
}
