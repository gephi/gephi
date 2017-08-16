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
import org.gephi.graph.api.AttributeUtils;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.TextProperties;
import org.gephi.visualization.VizArchitecture;
import org.gephi.visualization.VizController;
import org.gephi.visualization.apiimpl.GraphDrawable;
import org.gephi.visualization.apiimpl.VizConfig;
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
    private final SizeMode[] sizeModes;
    private final ColorMode[] colorModes;
    //Processing
    private Renderer nodeRenderer;
    private Renderer edgeRenderer;
    //Variables
    private TextModelImpl model;
    private boolean nodeRefresh = true;
    private boolean edgeRefresh = true;
    private float cachedCameraLocationZ;
    //Preferences
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
        colorModes = new ColorMode[3];
        colorModes[0] = new UniqueColorMode();
        colorModes[1] = new ObjectColorMode();
        colorModes[2] = new TextColorMode();
    }

    @Override
    public void initArchitecture() {
        model = VizController.getInstance().getVizModel().getTextModel();
        vizConfig = VizController.getInstance().getVizConfig();
        drawable = VizController.getInstance().getDrawable();

        //Settings
        antialised = vizConfig.isLabelAntialiased();
        mipmap = vizConfig.isLabelMipMap();
        fractionalMetrics = vizConfig.isLabelFractionalMetrics();

        //Init
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

    }

    public void reinitRenderers() {
        nodeRenderer.reinitRenderer();
        edgeRenderer.reinitRenderer();
        nodeRefresh = true;
        edgeRefresh = true;
    }

    private void initRenderer() {
        nodeRenderer = new Renderer3D();
        edgeRenderer = new Renderer3D();
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

    public boolean refreshNode(Graph graph, NodeModel node, TextModelImpl modelImpl) {
        TextProperties textData = node.getNode().getTextProperties();
        if (textData != null) {
            String txt = textData.getText();
            String newTxt = buildText(graph, node.getNode(), modelImpl.getNodeTextColumns());
            if ((txt == null && newTxt != null) || (txt != null && newTxt == null)
                    || (txt != null && newTxt != null && !txt.equals(newTxt))) {
                node.setText(newTxt);
                return true;
            }
        }
        nodeRefresh = true;
        return false;
    }

    public boolean refreshEdge(Graph graph, EdgeModel edge, TextModelImpl modelImpl) {
        TextProperties textData = edge.getEdge().getTextProperties();
        if (textData != null) {
            String txt = textData.getText();
            String newTxt = buildText(graph, edge.getEdge(), modelImpl.getEdgeTextColumns());
            if ((txt == null && newTxt != null) || (txt != null && newTxt == null)
                    || (txt != null && newTxt != null && !txt.equals(newTxt))) {
                edge.setText(newTxt);
                return true;
            }
        }
        edgeRefresh = true;
        return false;
    }

    private String buildText(Graph graph, Element element, Column[] selectedColumns) {
        String txt;
        if (selectedColumns == null || selectedColumns.length == 0) {
            txt = element.getLabel();
        } else if (selectedColumns.length == 1) {
            return buildText(graph, element, selectedColumns[0]);
        } else {
            StringBuilder sb = new StringBuilder();
            int i = 0;
            for (Column c : selectedColumns) {
                if (i++ > 0) {
                    sb.append(" - ");
                }
                sb.append(buildText(graph, element, c));
            }
            txt = sb.toString();
        }
        return (txt != null && !txt.isEmpty()) ? txt : null;
    }

    private String buildText(Graph graph, Element element, Column column) {
        Object val = element.getAttribute(column, graph.getView());
        if (val == null) {
            return "";
        }
        if (column.isArray()) {
            return AttributeUtils.printArray(val);
        } else {
            return val.toString();
        }
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
            renderer = new TextRenderer(font, antialised, fractionalMetrics, null, shouldUseMipmapGeneration());
        }

        @Override
        public void reinitRenderer() {
            renderer = new TextRenderer(renderer.getFont(), antialised, fractionalMetrics, null, shouldUseMipmapGeneration());
        }

        private boolean shouldUseMipmapGeneration() {
            boolean doMipmap = mipmap;
            if (drawable.getGraphicalConfiguration() != null && drawable.getGraphicalConfiguration().isIntelVendor()) {
                //Disable mipmap generation in intel GPUs. See https://github.com/gephi/gephi/issues/1494 (Some label characters fade away when zooming out)
                doMipmap = false;
            }

            return doMipmap;
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
            float cameraLocation = drawable.getCameraLocation()[2];
            if (cachedCameraLocationZ != cameraLocation && model.sizeMode == sizeModes[0]) {
                //Force refresh if camera location changed in fixed mode
                nodeRefresh = true;
                edgeRefresh = true;
            }
            cachedCameraLocationZ = cameraLocation;
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
                float width, height, posX, posY;

                if (txt == null || txt.isEmpty()) {
                    return;
                }

                float sizeFactor = drawable.getGlobalScale() * textData.getSize() * model.sizeMode.getSizeFactor3d(model.nodeSizeFactor, objectModel);
                if (nodeRefresh || (objectModel.getTextWidth() == 0f && objectModel.getTextHeight() == 0f)) {
                    Rectangle2D r = renderer.getBounds(txt);

                    width = (float) (sizeFactor * r.getWidth());
                    height = (float) (sizeFactor * r.getHeight());
                    posX = node.x() + (float) width / -2f;
                    posY = node.y() + (float) height / -2f;

                    textData.setDimensions(width, height);
                } else {
                    width = textData.getWidth();
                    height = textData.getHeight();
                    posX = node.x() + (float) width / -2f;
                    posY = node.y() + (float) height / -2f;
                }
                model.colorMode.textNodeColor(this, objectModel);

                renderer.draw3D(txt, posX, posY, (float) node.z(), sizeFactor);
            }
        }

        @Override
        public void drawTextEdge(EdgeModel objectModel) {
            Edge edge = objectModel.getEdge();
            TextProperties textData = (TextProperties) edge.getTextProperties();
            if (textData != null) {
                String txt = textData.getText();
                float width, height, posX, posY;

                if (txt == null || txt.isEmpty()) {
                    return;
                }

                float sizeFactor = drawable.getGlobalScale() * textData.getSize() * model.edgeSizeFactor;
                if (edgeRefresh || (objectModel.getTextWidth() == 0f && objectModel.getTextHeight() == 0f)) {
                    Rectangle2D r = renderer.getBounds(txt);

                    width = (float) (sizeFactor * r.getWidth());
                    height = (float) (sizeFactor * r.getHeight());
                    textData.setDimensions(width, height);
                } else {
                    width = textData.getWidth();
                    height = textData.getHeight();
                }

                model.colorMode.textEdgeColor(this, objectModel);

                float x, y;
                if (edge.isDirected()) {
                    x = (objectModel.getSourceModel().getNode().x() + 2 * objectModel.getTargetModel().getNode().x()) / 3f;
                    y = (objectModel.getSourceModel().getNode().y() + 2 * objectModel.getTargetModel().getNode().y()) / 3f;
                } else {
                    x = (objectModel.getSourceModel().getNode().x() + objectModel.getTargetModel().getNode().x()) / 2f;
                    y = (objectModel.getSourceModel().getNode().y() + objectModel.getTargetModel().getNode().y()) / 2f;
                }

                posX = x + (float) width / -2;
                posY = y + (float) height / -2;
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
}
