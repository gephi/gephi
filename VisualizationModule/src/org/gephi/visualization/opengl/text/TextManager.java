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
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import org.gephi.graph.api.EdgeData;
import org.gephi.graph.api.NodeData;
import org.gephi.graph.api.Renderable;
import org.gephi.graph.api.TextData;
import org.gephi.visualization.VizArchitecture;
import org.gephi.visualization.VizController;
import org.gephi.visualization.api.ModelImpl;
import org.gephi.visualization.api.VizConfig;

/**
 *
 * @author Mathieu Bastian
 */
public class TextManager implements VizArchitecture {

    //Architecture
    private VizConfig vizConfig;

    //Processing
    private TextUtils textUtils;
    private TextRenderer renderer;
    private TextDataBuilder builder;

    //Variables
    private ColorMode colorMode;
    private SizeMode sizeMode;
    private boolean mouseMode = false;

    public TextManager() {
        textUtils = new TextUtils(this);
        colorMode = new UniqueColorMode();
        sizeMode = new ScaledSizeMode();
        builder = new TextDataBuilder();
    }

    public void initArchitecture() {
        vizConfig = VizController.getInstance().getVizConfig();
        renderer = new TextRenderer(vizConfig.getDefaultLabelFont(), false, false, null, true);
    }

    public void defaultNodeColor() {
        colorMode.defaultNodeColor(renderer);
    }

    public void defaultEdgeColor() {
        colorMode.defaultEdgeColor(renderer);
    }

    public void beginRendering() {
        renderer.begin3DRendering();
    }

    public void endRendering() {
        renderer.end3DRendering();
    }

    public void drawText(ModelImpl model) {
        Renderable renderable = model.getObj();
        TextDataImpl textData = (TextDataImpl) renderable.getTextData();
        if (textData != null) {
            colorMode.textColor(renderer, textData, model);
            sizeMode.setSizeFactor(textData, model);

            String txt = textData.line.text;
            Rectangle2D r = renderer.getBounds(txt);
            textData.line.setBounds(r);
            int posX = (int) renderable.x() - (int) r.getWidth() / 2;
            int posY = (int) renderable.y() - (int) r.getHeight() / 2;

            renderer.draw3D(txt, posX, posY, 0, textData.sizeFactor);
        }
    }

    public TextRenderer getRenderer() {
        return renderer;
    }

    public void setFont(Font font) {
        renderer = new TextRenderer(font, false, false, null, true);
    }

    public TextData newTextData(NodeData node) {
        return builder.buildTextNode(node);
    }

    public TextData newTextData(EdgeData edge) {
        return builder.buildTextEdge(edge);
    }

    public boolean isMouseMode() {
        return mouseMode;
    }
}
