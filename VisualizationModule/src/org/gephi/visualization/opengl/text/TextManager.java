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
package org.gephi.visualization.opengl.text;

import org.gephi.visualization.impl.TextDataImpl;
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
import org.gephi.dynamic.api.DynamicModel;
import org.gephi.dynamic.api.DynamicModelEvent;
import org.gephi.dynamic.api.DynamicModelListener;
import org.gephi.graph.api.EdgeData;
import org.gephi.graph.api.NodeData;
import org.gephi.graph.api.Renderable;
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
    private DynamicController dynamicController;
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
        builder = new TextDataBuilderImpl();

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

                    DynamicModel dynamicModel = dynamicController.getModel();
                    if(dynamicModel!=null) {
                        currentTimeInterval = dynamicModel.getVisibleInterval();
                        builder.setDefaultEstimator(dynamicModel.getEstimator());
                        builder.setNumberEstimator(dynamicModel.getNumberEstimator());
                    } else {
                        currentTimeInterval = null;
                    }
                }
            }
        });

        //Settings
        antialised = vizConfig.isLabelAntialiased();
        mipmap = vizConfig.isLabelMipMap();
        fractionalMetrics = vizConfig.isLabelFractionalMetrics();
        renderer3d = false;

        //Dynamic change
        dynamicController = Lookup.getDefault().lookup(DynamicController.class);
        dynamicController.addModelListener(new DynamicModelListener() {

            public void dynamicModelChanged(DynamicModelEvent event) {
                if(event.getEventType().equals(DynamicModelEvent.EventType.VISIBLE_INTERVAL)) {
                    currentTimeInterval = (TimeInterval) event.getData();
                }
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
                String txt = textData.getLine().getText();
                Rectangle2D r = renderer.getBounds(txt);
                textData.getLine().setBounds(r);
                float posX = renderable.x() + (float) r.getWidth() / -2 * textData.getSizeFactor();
                float posY = renderable.y() + (float) r.getHeight() / -2 * textData.getSizeFactor();
                float posZ = renderable.getRadius();

                renderer.draw3D(txt, posX, posY, posZ, textData.getSizeFactor());
            }
        }

        public void drawTextEdge(ModelImpl objectModel) {
            Renderable renderable = objectModel.getObj();
            TextDataImpl textData = (TextDataImpl) renderable.getTextData();
            if (textData != null) {
                model.colorMode.textColor(this, textData, objectModel);
                model.sizeMode.setSizeFactor3d(model.edgeSizeFactor, textData, objectModel);
                if (edgeRefresh) {
                    builder.buildEdgeText((EdgeData) renderable, textData, model, currentTimeInterval);
                }

                String txt = textData.getLine().getText();
                Rectangle2D r = renderer.getBounds(txt);
                textData.getLine().setBounds(r);
                float posX = renderable.x() + (float) r.getWidth() / -2 * textData.getSizeFactor();
                float posY = renderable.y() + (float) r.getHeight() / -2 * textData.getSizeFactor();
                float posZ = renderable.getRadius();

                renderer.draw3D(txt, posX, posY, posZ, textData.getSizeFactor());
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
                if (textData.getSizeFactor() * renderer.getCharWidth('a') < PIXEL_LIMIT) {
                    return;
                }
                String txt = textData.getLine().getText();
                Rectangle2D r = renderer.getBounds(txt);
                float posX = renderable.getModel().getViewportX() + (float) r.getWidth() / -2 * textData.getSizeFactor();
                float posY = renderable.getModel().getViewportY() + (float) r.getHeight() / -2 * textData.getSizeFactor();
                r.setRect(0, 0, r.getWidth() / Math.abs(drawable.getDraggingMarkerX()), r.getHeight() / Math.abs(drawable.getDraggingMarkerY()));
                textData.getLine().setBounds(r);

                renderer.draw3D(txt, posX, posY, 0, textData.getSizeFactor());
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
                if (textData.getSizeFactor() * renderer.getCharWidth('a') < PIXEL_LIMIT) {
                    return;
                }
                String txt = textData.getLine().getText();
                Rectangle2D r = renderer.getBounds(txt);
                float posX = renderable.getModel().getViewportX() + (float) r.getWidth() / -2 * textData.getSizeFactor();
                float posY = renderable.getModel().getViewportY() + (float) r.getHeight() / -2 * textData.getSizeFactor();
                r.setRect(0, 0, r.getWidth() / drawable.getDraggingMarkerX(), r.getHeight() / drawable.getDraggingMarkerY());
                textData.getLine().setBounds(r);

                renderer.draw3D(txt, posX, posY, 0, textData.getSizeFactor());
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
