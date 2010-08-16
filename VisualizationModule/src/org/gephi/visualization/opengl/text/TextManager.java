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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.type.TimeInterval;
import org.gephi.data.properties.PropertiesColumn;
import org.gephi.dynamic.api.DynamicController;
import org.gephi.dynamic.api.DynamicModelEvent;
import org.gephi.dynamic.api.DynamicModelListener;
import org.gephi.graph.api.EdgeData;
import org.gephi.graph.api.NodeData;
import org.gephi.graph.api.Renderable;
import org.gephi.graph.spi.TextDataFactory;
import org.gephi.visualization.VizArchitecture;
import org.gephi.visualization.VizController;
import org.gephi.visualization.apiimpl.GraphDrawable;
import org.gephi.visualization.apiimpl.ModelImpl;
import org.gephi.visualization.apiimpl.VizConfig;
import org.openide.util.Lookup;

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
    private TextDataBuilderImpl builder;
    //Variables
    private TextModel model;
    private boolean nodeRefresh = true;
    private boolean edgeRefresh = true;
    private TimeInterval currentTimeInterval;
    //Preferences
    private boolean renderer3d;
    private boolean mipmap;
    private boolean fractionalMetrics;
    private boolean antialised;

    public TextManager() {
        textUtils = new TextUtils(this);
        builder = (TextDataBuilderImpl) Lookup.getDefault().lookup(TextDataFactory.class);

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
        initRenderer();

        //Init sizemodes
        for (SizeMode s : sizeModes) {
            s.init();
        }

        //Model listening
        model.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                if (!nodeRenderer.getFont().equals(model.getNodeFont())) {
                    nodeRenderer.setFont(model.getNodeFont());
                }
                if (!edgeRenderer.getFont().equals(model.getEdgeFont())) {
                    edgeRenderer.setFont(model.getEdgeFont());
                }
                nodeRefresh = true;
                edgeRefresh = true;
            }
        });

        //Model change
        VizController.getInstance().getVizModel().addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals("init")) {
                    TextManager.this.model = VizController.getInstance().getVizModel().getTextModel();

                    //Initialize columns if needed
                    if (model.getNodeTextColumns() == null || model.getNodeTextColumns().length == 0) {
                        AttributeController attributeController = Lookup.getDefault().lookup(AttributeController.class);
                        if (attributeController != null && attributeController.getModel() != null) {
                            AttributeModel attributeModel = attributeController.getModel();
                            AttributeColumn[] nodeCols = new AttributeColumn[]{attributeModel.getNodeTable().getColumn(PropertiesColumn.NODE_LABEL.getIndex())};
                            AttributeColumn[] edgeCols = new AttributeColumn[]{attributeModel.getEdgeTable().getColumn(PropertiesColumn.EDGE_LABEL.getIndex())};
                            model.setTextColumns(nodeCols, edgeCols);
                        }
                    }

                    DynamicController dynamicController = Lookup.getDefault().lookup(DynamicController.class);
                    currentTimeInterval = dynamicController.getModel().getVisibleInterval();
                }
            }
        });

        //Settings
        antialised = vizConfig.isLabelAntialiased();
        mipmap = vizConfig.isLabelMipMap();
        fractionalMetrics = vizConfig.isLabelFractionalMetrics();
        renderer3d = false;

        //Dynamic change
        DynamicController dynamicController = Lookup.getDefault().lookup(DynamicController.class);
        dynamicController.addModelListener(new DynamicModelListener() {

            public void dynamicModelChanged(DynamicModelEvent event) {
                currentTimeInterval = (TimeInterval) event.getData();
            }
        });
    }

    private void initRenderer() {
        if (renderer3d) {
            nodeRenderer = new Renderer3D();
            edgeRenderer = new Renderer3D();
        } else {
            nodeRenderer = new Renderer2D();
            edgeRenderer = new Renderer2D();
        }
        nodeRenderer.initRenderer(model.getNodeFont());
        edgeRenderer.initRenderer(model.getEdgeFont());
    }

    public void defaultNodeColor() {
        model.colorMode.defaultNodeColor(nodeRenderer);
    }

    public void defaultEdgeColor() {
        model.colorMode.defaultEdgeColor(edgeRenderer);
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

    public void setRenderer3d(boolean renderer3d) {
        this.renderer3d = renderer3d;
        initRenderer();
    }

    //-------------------------------------------------------------------------------------------------
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
                model.sizeMode.setSizeFactor3d(model.nodeSizeFactor, textData, objectModel);
                if (nodeRefresh) {
                    builder.buildNodeText((NodeData) renderable, textData, model, currentTimeInterval);
                }
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
                model.sizeMode.setSizeFactor3d(model.edgeSizeFactor, textData, objectModel);
                if (edgeRefresh) {
                    builder.buildNodeText((NodeData) renderable, textData, model, currentTimeInterval);
                }

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
                model.sizeMode.setSizeFactor2d(model.nodeSizeFactor, textData, objectModel);
                if (nodeRefresh) {
                    builder.buildNodeText((NodeData) renderable, textData, model, currentTimeInterval);
                }
                if (textData.sizeFactor * renderer.getCharWidth('a') < PIXEL_LIMIT) {
                    return;
                }
                String txt = textData.line.text;
                Rectangle2D r = renderer.getBounds(txt);
                float posX = renderable.getModel().getViewportX() + (float) r.getWidth() / -2 * textData.sizeFactor;
                float posY = renderable.getModel().getViewportY() + (float) r.getHeight() / -2 * textData.sizeFactor;
                r.setRect(0, 0, r.getWidth() / Math.abs(drawable.getDraggingMarkerX()), r.getHeight() / Math.abs(drawable.getDraggingMarkerY()));
                textData.line.setBounds(r);

                renderer.draw3D(txt, posX, posY, 0, textData.sizeFactor);
            }
        }

        public void drawTextEdge(ModelImpl objectModel) {
            Renderable renderable = objectModel.getObj();
            TextDataImpl textData = (TextDataImpl) renderable.getTextData();
            if (textData != null) {
                model.colorMode.textColor(this, textData, objectModel);
                model.sizeMode.setSizeFactor2d(model.edgeSizeFactor, textData, objectModel);
                if (edgeRefresh) {
                    builder.buildEdgeText((EdgeData) renderable, textData, model, currentTimeInterval);
                }
                if (textData.sizeFactor * renderer.getCharWidth('a') < PIXEL_LIMIT) {
                    return;
                }
                String txt = textData.line.text;
                Rectangle2D r = renderer.getBounds(txt);
                float posX = renderable.getModel().getViewportX() + (float) r.getWidth() / -2 * textData.sizeFactor;
                float posY = renderable.getModel().getViewportY() + (float) r.getHeight() / -2 * textData.sizeFactor;
                r.setRect(0, 0, r.getWidth() / drawable.getDraggingMarkerX(), r.getHeight() / drawable.getDraggingMarkerY());
                textData.line.setBounds(r);

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
