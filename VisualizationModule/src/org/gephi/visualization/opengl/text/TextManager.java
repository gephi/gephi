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
import java.awt.geom.Rectangle2D;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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

    //Configuration
    private SizeMode[] sizeModes;

    //Processing
    private TextUtils textUtils;
    private TextRenderer renderer;
    private TextDataBuilder builder;

    //Variables
    private TextModel model;

    //Preferences
    private boolean mipmap;
    private boolean fractionalMetrics;
    private boolean antialised;


    public TextManager() {
        textUtils = new TextUtils(this);
        builder = new TextDataBuilder();
        model = new TextModel();

        //SizeMode init
        sizeModes = new SizeMode[3];
        sizeModes[0] = new FixedSizeMode(model);
        sizeModes[1] = new ScaledSizeMode(model);
        sizeModes[2] = new ProportionalSizeMode(model);
    }

    public void initArchitecture() {
        vizConfig = VizController.getInstance().getVizConfig();
        model.colorMode = new UniqueColorMode(model);
        model.sizeMode = sizeModes[1];
        model.font = vizConfig.getLabelFont();
        model.nodeColor = vizConfig.getDefaultNodeLabelColor();
        model.edgeColor = vizConfig.getDefaultEdgeLabelColor();
        model.setSelectedOnly(vizConfig.isShowLabelOnSelectedOnly());
        initRenderer();

        //Model listening
        model.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                if (!renderer.getFont().equals(model.getFont())) {
                    initRenderer();
                }
            }
        });
    }

    public void defaultNodeColor() {
        model.colorMode.defaultNodeColor(renderer);
    }

    public void defaultEdgeColor() {
        model.colorMode.defaultEdgeColor(renderer);
    }

    public void initRenderer() {
        renderer = new TextRenderer(model.getFont(), antialised, fractionalMetrics, null, mipmap);
    }

    public void disposeRenderer() {
        renderer.flush();
        renderer.dispose();
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
            this.model.colorMode.textColor(renderer, textData, model);
            this.model.sizeMode.setSizeFactor(textData, model);

            String txt = textData.line.text;
            Rectangle2D r = renderer.getBounds(txt);
            textData.line.setBounds(r);
            float posX = renderable.x() + (float) r.getWidth() / -2 * textData.sizeFactor;
            float posY = renderable.y() + (float) r.getHeight() / -2 * textData.sizeFactor;

            renderer.draw3D(txt, posX, posY, 0, textData.sizeFactor);
        }
    }

    public TextRenderer getRenderer() {
        return renderer;
    }

    public TextData newTextData(NodeData node) {
        return builder.buildTextNode(node);
    }

    public TextData newTextData(EdgeData edge) {
        return builder.buildTextEdge(edge);
    }

    public boolean isSelectedOnly() {
        return model.selectedOnly;
    }

    public TextModel getModel() {
        return model;
    }

    public void setModel(TextModel model) {
        this.model = model;
    }

    public SizeMode[] getSizeModes() {
        return sizeModes;
    }
}
