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

import com.jogamp.opengl.util.awt.TextRenderer;
import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.attribute.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.TextProperties;
import org.gephi.visualization.VizArchitecture;
import org.gephi.visualization.VizController;
import org.gephi.visualization.apiimpl.GraphDrawable;
import org.gephi.visualization.apiimpl.VizConfig;
import org.gephi.visualization.model.TextModel;
import org.gephi.visualization.model.edge.EdgeModel;
import org.gephi.visualization.model.node.NodeModel;

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
    private Renderer nodeRenderer;
    private Renderer edgeRenderer;
    //Variables
    private TextModelImpl model;
    private boolean nodeRefresh = true;
    private boolean edgeRefresh = true;
    //Preferences
    private boolean renderer3d;
    private boolean mipmap;
    private boolean fractionalMetrics;
    private boolean antialised;

    public TextManager() {
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

    @Override
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
            @Override
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
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals("init")) {
                    TextManager.this.model = VizController.getInstance().getVizModel().getTextModel();

                    //Initialize columns if needed
                    if (model.getNodeTextColumns() == null || model.getNodeTextColumns().length == 0) {
                        model.setTextColumns(new Column[0], new Column[0]);
                    }
                }
            }
        });

        //Settings
        antialised = vizConfig.isLabelAntialiased();
        mipmap = vizConfig.isLabelMipMap();
        fractionalMetrics = vizConfig.isLabelFractionalMetrics();
        renderer3d = false;
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

    public TextModelImpl getModel() {
        return model;
    }

    public void setModel(TextModelImpl model) {
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

    public String buildText(Element element, TextModel textModel, Column[] selectedColumns) {
        if (selectedColumns != null && selectedColumns.length > 0) {
            String str = "";
            int i = 0;
            for (Column c : selectedColumns) {
                if (i++ > 0) {
                    str += " - ";
                }
                Object val = element.getAttribute(c);
                str += val != null ? val : "";
            }
            textModel.setText(str);
            return str;
        }
        textModel.setText(element.getLabel());
        return element.getLabel();
    }

    //-------------------------------------------------------------------------------------------------
    public static interface Renderer {

        public void initRenderer(Font font);

        public void reinitRenderer();

        public void disposeRenderer();

        public void beginRendering();

        public void endRendering();

        public void drawTextNode(NodeModel model);

        public void drawTextEdge(EdgeModel model);

        public Font getFont();

        public void setFont(Font font);

        public void setColor(float r, float g, float b, float a);

        public TextRenderer getJOGLRenderer();
    }

    private class Renderer3D implements Renderer {

        private TextRenderer renderer;

        @Override
        public void initRenderer(Font font) {
            renderer = new TextRenderer(font, antialised, fractionalMetrics, null, mipmap);
        }

        @Override
        public void reinitRenderer() {
            renderer = new TextRenderer(renderer.getFont(), antialised, fractionalMetrics, null, mipmap);
        }

        @Override
        public void disposeRenderer() {
            renderer.flush();
            renderer.dispose();
        }

        @Override
        public Font getFont() {
            return renderer.getFont();
        }

        @Override
        public void setFont(Font font) {
            initRenderer(font);
        }

        @Override
        public void beginRendering() {
            renderer.begin3DRendering();
        }

        @Override
        public void endRendering() {
            renderer.end3DRendering();
            nodeRefresh = false;
            edgeRefresh = false;
        }

        @Override
        public void drawTextNode(NodeModel objectModel) {
            Node node = objectModel.getNode();
            TextProperties textData = (TextProperties) node.getTextProperties();
            if (textData != null) {
                String txt = textData.getText();
                if (nodeRefresh) {
                    txt = buildText(node, objectModel, model.getNodeTextColumns());
                    if (txt == null || txt.isEmpty()) {
                        return;
                    }
                    Rectangle2D r = renderer.getBounds(txt);
                    objectModel.setTextBounds(r);
                }
                model.colorMode.textNodeColor(this, objectModel);
                float sizeFactor = textData.getSize() * model.sizeMode.getSizeFactor3d(model.nodeSizeFactor, objectModel);

                float width = sizeFactor * objectModel.getTextWidth();
                float height = sizeFactor * objectModel.getTextHeight();
                float posX = node.x() + (float) width / -2 * sizeFactor;
                float posY = node.y() + (float) height / -2 * sizeFactor;
                float posZ = node.size();

                renderer.draw3D(txt, posX, posY, posZ, sizeFactor);
            }
        }

        @Override
        public void drawTextEdge(EdgeModel objectModel) {
            Edge edge = objectModel.getEdge();
            TextProperties textData = (TextProperties) edge.getTextProperties();
            if (textData != null) {
                String txt = textData.getText();
                if (edgeRefresh) {
                    txt = buildText(edge, objectModel, model.getEdgeTextColumns());
                    if (txt == null || txt.isEmpty()) {
                        return;
                    }
                    Rectangle2D r = renderer.getBounds(txt);
                    objectModel.setTextBounds(r);
                }
                model.colorMode.textEdgeColor(this, objectModel);
//                float sizeFactor = textData.getSize() * model.sizeMode.getSizeFactor3d(model.edgeSizeFactor, objectModel);
                float sizeFactor = 1f;
                float width = sizeFactor * objectModel.getTextWidth();
                float height = sizeFactor * objectModel.getTextHeight();
                float x = (objectModel.getSourceModel().getNode().x() + 2 * objectModel.getTargetModel().getNode().x()) / 3f;
                float y = (objectModel.getSourceModel().getNode().y() + 2 * objectModel.getTargetModel().getNode().y()) / 3f;
                float z = (objectModel.getSourceModel().getNode().z() + 2 * objectModel.getTargetModel().getNode().z()) / 3f;

                float posX = x + (float) width / -2 * sizeFactor;
                float posY = y + (float) height / -2 * sizeFactor;
                float posZ = 0;

                renderer.draw3D(txt, posX, posY, posZ, sizeFactor);
            }
        }

        @Override
        public void setColor(float r, float g, float b, float a) {
            renderer.setColor(r, g, b, a);
        }

        @Override
        public TextRenderer getJOGLRenderer() {
            return renderer;
        }
    }

    private class Renderer2D implements Renderer {

        private TextRenderer renderer;
        private static final float PIXEL_LIMIT = 3.5f;

        @Override
        public void initRenderer(Font font) {
            renderer = new TextRenderer(font, antialised, fractionalMetrics, null, mipmap);
        }

        @Override
        public void reinitRenderer() {
            renderer = new TextRenderer(renderer.getFont(), antialised, fractionalMetrics, null, mipmap);
        }

        @Override
        public void disposeRenderer() {
            renderer.flush();
            renderer.dispose();
        }

        @Override
        public Font getFont() {
            return renderer.getFont();
        }

        @Override
        public void setFont(Font font) {
            initRenderer(font);
        }

        @Override
        public void beginRendering() {
            renderer.beginRendering(drawable.getViewportWidth(), drawable.getViewportHeight());
        }

        @Override
        public void endRendering() {
            renderer.endRendering();
            nodeRefresh = false;
            edgeRefresh = false;
        }

        @Override
        public void drawTextNode(NodeModel objectModel) {
            Node node = objectModel.getNode();
            TextProperties textData = (TextProperties) node.getTextProperties();
            if (textData != null) {
                String txt = textData.getText();
                if (nodeRefresh) {
                    txt = buildText(node, objectModel, model.getNodeTextColumns());
                    if (txt == null || txt.isEmpty()) {
                        return;
                    }
                    Rectangle2D r = renderer.getBounds(txt);
                    objectModel.setTextBounds(r);
                }
                model.colorMode.textNodeColor(this, objectModel);
                float sizeFactor = textData.getSize() * model.sizeMode.getSizeFactor2d(model.nodeSizeFactor, objectModel);
                if (sizeFactor * renderer.getCharWidth('a') < PIXEL_LIMIT) {
                    return;
                }
                Rectangle2D r = renderer.getBounds(txt);
                float posX = objectModel.getViewportX() + (float) r.getWidth() / -2 * sizeFactor;
                float posY = objectModel.getViewportY() + (float) r.getHeight() / -2 * sizeFactor;
                r.setRect(0, 0, r.getWidth() / Math.abs(drawable.getDraggingMarkerX()), r.getHeight() / Math.abs(drawable.getDraggingMarkerY()));
                objectModel.setTextBounds(r);

                renderer.draw3D(txt, posX, posY, 0, sizeFactor);
            }
        }

        @Override
        public void drawTextEdge(EdgeModel objectModel) {
            Edge edge = objectModel.getEdge();
            TextProperties textData = (TextProperties) edge.getTextProperties();
            if (textData != null) {
                String txt = textData.getText();
                if (edgeRefresh) {
                    txt = buildText(edge, objectModel, model.getEdgeTextColumns());
                    if (txt == null || txt.isEmpty()) {
                        return;
                    }
                    Rectangle2D r = renderer.getBounds(txt);
                    objectModel.setTextBounds(r);
                }
                model.colorMode.textEdgeColor(this, objectModel);
//                float sizeFactor = textData.getSize() * model.sizeMode.getSizeFactor2d(model.nodeSizeFactor, objectModel);
                float sizeFactor = 1f;
                if (sizeFactor * renderer.getCharWidth('a') < PIXEL_LIMIT) {
                    return;
                }
                Rectangle2D r = renderer.getBounds(txt);
                float viewportX = (objectModel.getSourceModel().getViewportX() + 2 * objectModel.getTargetModel().getViewportX()) / 3f;
                float viewportY = (objectModel.getSourceModel().getViewportY() + 2 * objectModel.getTargetModel().getViewportY()) / 3f;
                float posX = viewportX + (float) r.getWidth() / -2 * sizeFactor;
                float posY = viewportY + (float) r.getHeight() / -2 * sizeFactor;
                r.setRect(0, 0, r.getWidth() / drawable.getDraggingMarkerX(), r.getHeight() / drawable.getDraggingMarkerY());
                objectModel.setTextBounds(r);

                renderer.draw3D(txt, posX, posY, 0, sizeFactor);
            }
        }

        @Override
        public void setColor(float r, float g, float b, float a) {
            renderer.setColor(r, g, b, a);
        }

        @Override
        public TextRenderer getJOGLRenderer() {
            return renderer;
        }
    }
}
