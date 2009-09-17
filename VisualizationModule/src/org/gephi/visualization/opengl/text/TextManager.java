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
import java.awt.geom.Rectangle2D;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.graph.api.EdgeData;
import org.gephi.graph.api.NodeData;
import org.gephi.graph.api.Renderable;
import org.gephi.graph.api.TextData;
import org.gephi.visualization.VizArchitecture;
import org.gephi.visualization.VizController;
import org.gephi.visualization.api.GraphDrawable;
import org.gephi.visualization.api.ModelImpl;
import org.gephi.visualization.api.VizConfig;

/**
 *
 * @author Mathieu Bastian
 */
public class TextManager implements VizArchitecture {

    //Architecture
    private VizConfig vizConfig;
    private GraphDrawable drawable;

    //Configuration
    private SizeMode[] sizeModes;
    private ColorMode[] colorModes;
    

    //Processing
    private TextUtils textUtils;
    private Renderer nodeRenderer;
    private Renderer edgeRenderer;
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
        
        //SizeMode init
        sizeModes = new SizeMode[3];
        sizeModes[0] = new FixedSizeMode();
        sizeModes[1] = new ScaledSizeMode();
        sizeModes[2] = new ProportionalSizeMode();

        //ColorMode init
        colorModes = new ColorMode[2];
        colorModes[0] = new UniqueColorMode();
        colorModes[1] = new ObjectColorMode();
    }

    public void initArchitecture() {
        model = VizController.getInstance().getVizModel().getTextModel();
        vizConfig = VizController.getInstance().getVizConfig();
        drawable = VizController.getInstance().getDrawable();
        nodeRenderer = new Renderer2D();
        nodeRenderer.initRenderer(model.getNodeFont());
        edgeRenderer = new Renderer2D();
        edgeRenderer.initRenderer(model.getEdgeFont());

        //Model listening
        model.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                if (!nodeRenderer.getFont().equals(model.getNodeFont())) {
                    nodeRenderer.setFont(model.getNodeFont());
                }
                if (!edgeRenderer.getFont().equals(model.getEdgeFont())) {
                    edgeRenderer.setFont(model.getEdgeFont());
                }
            }
        });
    }

    public void defaultNodeColor() {
        model.colorMode.defaultNodeColor(nodeRenderer);
    }

    public void defaultEdgeColor() {
        model.colorMode.defaultEdgeColor(edgeRenderer);
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

    public ColorMode[] getColorModes() {
        return colorModes;
    }

    public Renderer getNodeRenderer() {
        return nodeRenderer;
    }

    public Renderer getEdgeRenderer() {
        return edgeRenderer;
    }

    public static interface Renderer {

        public void initRenderer(Font font);

        public void reinitRenderer();

        public void disposeRenderer();

        public void beginRendering();

        public void endRendering();

        public void drawTextNode(ModelImpl model);

        public void drawTextEdge(ModelImpl model);

        public Font getFont();

        public void setFont(Font font);

        public void setColor(float r, float g, float b, float a);

        public TextRenderer getJOGLRenderer();
    }

    private class Renderer3D implements Renderer {

        private TextRenderer renderer;

        public void initRenderer(Font font) {
            renderer = new TextRenderer(font, antialised, fractionalMetrics, null, mipmap);
        }

        public void reinitRenderer() {
            renderer = new TextRenderer(renderer.getFont(), antialised, fractionalMetrics, null, mipmap);
        }

        public void disposeRenderer() {
            renderer.flush();
            renderer.dispose();
        }

        public Font getFont() {
            return renderer.getFont();
        }

        public void setFont(Font font) {
            initRenderer(font);
        }

        public void beginRendering() {
            renderer.begin3DRendering();
        }

        public void endRendering() {
            renderer.end3DRendering();
        }

        public void drawTextNode(ModelImpl objectModel) {
            Renderable renderable = objectModel.getObj();
            TextDataImpl textData = (TextDataImpl) renderable.getTextData();
            if (textData != null) {
                model.colorMode.textColor(this, textData, objectModel);
                model.sizeMode.setSizeFactor(model.nodeSizeFactor, textData, objectModel);

                String txt = textData.line.text;
                Rectangle2D r = renderer.getBounds(txt);
                textData.line.setBounds(r);
                float posX = renderable.x() + (float) r.getWidth() / -2 * textData.sizeFactor;
                float posY = renderable.y() + (float) r.getHeight() / -2 * textData.sizeFactor;
                float posZ = renderable.getRadius();

                renderer.draw3D(txt, posX, posY, posZ, textData.sizeFactor);
            }
        }

        public void drawTextEdge(ModelImpl objectModel) {
            Renderable renderable = objectModel.getObj();
            TextDataImpl textData = (TextDataImpl) renderable.getTextData();
            if (textData != null) {
                model.colorMode.textColor(this, textData, objectModel);
                model.sizeMode.setSizeFactor(model.edgeSizeFactor, textData, objectModel);

                String txt = textData.line.text;
                Rectangle2D r = renderer.getBounds(txt);
                textData.line.setBounds(r);
                float posX = renderable.x() + (float) r.getWidth() / -2 * textData.sizeFactor;
                float posY = renderable.y() + (float) r.getHeight() / -2 * textData.sizeFactor;
                float posZ = renderable.getRadius();

                renderer.draw3D(txt, posX, posY, posZ, textData.sizeFactor);
            }
        }

        public void setColor(float r, float g, float b, float a) {
            renderer.setColor(r, g, b, a);
        }

        public TextRenderer getJOGLRenderer() {
            return renderer;
        }
    }

    private class Renderer2D implements Renderer {

        private TextRenderer renderer;
        private static final float PIXEL_LIMIT = 3.5f;

        public void initRenderer(Font font) {
            renderer = new TextRenderer(font, antialised, fractionalMetrics, null, mipmap);
        }

        public void reinitRenderer() {
            renderer = new TextRenderer(renderer.getFont(), antialised, fractionalMetrics, null, mipmap);
        }

        public void disposeRenderer() {
            renderer.flush();
            renderer.dispose();
        }

        public Font getFont() {
            return renderer.getFont();
        }

        public void setFont(Font font) {
            initRenderer(font);
        }

        public void beginRendering() {
            renderer.beginRendering(drawable.getViewportWidth(), drawable.getViewportHeight());
        }

        public void endRendering() {
            renderer.endRendering();
        }

        public void drawTextNode(ModelImpl objectModel) {
            Renderable renderable = objectModel.getObj();
            TextDataImpl textData = (TextDataImpl) renderable.getTextData();
            if (textData != null) {
                model.colorMode.textColor(this, textData, objectModel);
                model.sizeMode.setSizeFactor(model.nodeSizeFactor, textData, objectModel);
                if (textData.sizeFactor * renderer.getCharWidth('a') < PIXEL_LIMIT) {
                    return;
                }
                String txt = textData.line.text;
                Rectangle2D r = renderer.getBounds(txt);
                textData.line.setBounds(r);
                float posX = renderable.getModel().getViewportX() + (float) r.getWidth() / -2 * textData.sizeFactor;
                float posY = renderable.getModel().getViewportY() + (float) r.getHeight() / -2 * textData.sizeFactor;

                renderer.draw3D(txt, posX, posY, 0, textData.sizeFactor);
            }
        }

        public void drawTextEdge(ModelImpl objectModel) {
            Renderable renderable = objectModel.getObj();
            TextDataImpl textData = (TextDataImpl) renderable.getTextData();
            if (textData != null) {
                model.colorMode.textColor(this, textData, objectModel);
                model.sizeMode.setSizeFactor(model.edgeSizeFactor, textData, objectModel);
                if (textData.sizeFactor * renderer.getCharWidth('a') < PIXEL_LIMIT) {
                    return;
                }
                String txt = textData.line.text;
                Rectangle2D r = renderer.getBounds(txt);
                textData.line.setBounds(r);
                float posX = renderable.getModel().getViewportX() + (float) r.getWidth() / -2 * textData.sizeFactor;
                float posY = renderable.getModel().getViewportY() + (float) r.getHeight() / -2 * textData.sizeFactor;

                renderer.draw3D(txt, posX, posY, 0, textData.sizeFactor);
            }
        }

        public void setColor(float r, float g, float b, float a) {
            renderer.setColor(r, g, b, a);
        }

        public TextRenderer getJOGLRenderer() {
            return renderer;
        }
    }
}
