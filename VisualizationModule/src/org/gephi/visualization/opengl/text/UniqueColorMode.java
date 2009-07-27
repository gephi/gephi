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

import com.sun.opengl.util.j2d.TextRenderer;
import org.gephi.visualization.VizController;
import org.gephi.visualization.api.ModelImpl;
import org.gephi.visualization.api.VizConfig;

/**
 *
 * @author Mathieu Bastian
 */
public class UniqueColorMode implements ColorMode {

    private VizConfig config;
    private float[] color;

    public UniqueColorMode() {
        config = VizController.getInstance().getVizConfig();
    }

    public float[] getColor() {
        return color;
    }

    public void setColor(float[] color) {
        this.color = color;
    }

    public void defaultNodeColor(TextRenderer renderer) {
        float[] defaultNodeColor = config.getDefaultNodeLabelColor();
        renderer.setColor(defaultNodeColor[0], defaultNodeColor[1], defaultNodeColor[2], defaultNodeColor[3]);
    }

    public void defaultEdgeColor(TextRenderer renderer) {
        float[] defaultEdgeColor = config.getDefaultEdgeLabelColor();
        renderer.setColor(defaultEdgeColor[0], defaultEdgeColor[1], defaultEdgeColor[2], defaultEdgeColor[3]);
    }

    public void textColor(TextRenderer renderer, TextDataImpl text, ModelImpl model) {
        if (text.hasCustomColor()) {
            renderer.setColor(text.r, text.g, text.b, text.a);
        }
    }
}
