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

package org.gephi.visualization;

import java.awt.Color;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.jogamp.newt.event.NEWTEvent;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.project.api.Workspace;
import org.gephi.ui.utils.ColorUtils;
import org.gephi.visualization.api.LabelColorMode;
import org.gephi.visualization.api.LabelSizeMode;
import org.gephi.visualization.api.VisualisationModel;
import org.gephi.visualization.api.VisualizationPropertyChangeListener;
import org.gephi.visualization.component.VizEngineGraphCanvasManager;
import org.gephi.visualization.screenshot.ScreenshotModelImpl;
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.jogl.JOGLRenderingTarget;
import org.gephi.viz.engine.status.GraphRenderingOptions;
import org.joml.Vector2fc;
import org.gephi.ui.utils.UIUtils;
import org.gephi.visualization.apiimpl.VizConfig;
import org.openide.util.Lookup;

/**
 * @author Mathieu Bastian
 */
public class VizModel implements VisualisationModel {

    private final VizController vizController;
    private final Workspace workspace;
    private final VizEngineGraphCanvasManager canvasManager;

    protected final VizConfig config;

    //Listener
    private GraphRenderingOptions renderingOptions;

    // TODO: Delete after moved to the viz-engine
    private boolean showNodeLabels = true;
    private boolean showEdgeLabels = true;
    private Font nodeLabelFont = new Font("Arial", Font.PLAIN, 12);
    private Font edgeLabelFont = new Font("Arial", Font.PLAIN, 12);
    private Color nodeLabelColor = Color.WHITE;
    private Color edgeLabelColor = Color.WHITE;
    private float nodeLabelSize = 12f;
    private float edgeLabelSize = 12f;
    private LabelColorMode nodeLabelColorMode = LabelColorMode.OBJECT;
    private LabelSizeMode nodeLabelSizeMode = LabelSizeMode.FIXED;
    private boolean hideNonSelectedLabels = false;
    private Column[] nodeLabelColumns = new Column[0];
    private Column[] edgeLabelColumns = new Column[0];
    // TODO End

    // Selection
    private final SelectionModelImpl selectionModel;


    public VizModel(VizController controller, Workspace workspace) {
        this.vizController = controller;
        this.workspace = workspace;
        this.config = new VizConfig();
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel(workspace);
        this.canvasManager = new VizEngineGraphCanvasManager(workspace, graphModel);
        this.selectionModel = new SelectionModelImpl(this);

        //TODO: Remove once this is moved to the viz-engine
        this.nodeLabelColumns = new Column[] {graphModel.getNodeTable().getColumn("label")};
        this.edgeLabelColumns = new Column[] {graphModel.getEdgeTable().getColumn("label")};
    }

    public void destroy(JComponent component) {
        canvasManager.destroy(component);
    }

    public SelectionModelImpl getSelectionModel() {
        return selectionModel;
    }

    public ScreenshotModelImpl getScreenshotModel() {
        return null;
    }

    @Override
    public Workspace getWorkspace() {
        return workspace;
    }

    public Optional<VizEngine<JOGLRenderingTarget, NEWTEvent>> getEngine() {
        return canvasManager.getEngine();
    }

    private boolean loadEngine() {
        VizEngine<JOGLRenderingTarget, NEWTEvent> engine = canvasManager.getEngine().orElse(null);
        if (engine == null) {
            return false; // Engine still not ready in the workspace
        }
        this.renderingOptions = engine.getLookup().lookup(GraphRenderingOptions.class);

        defaultValues();

        return true;
    }

    /**
     * Getters and setters should only be called if the model is ready (has a backing viz-engine setup).
     */
    public boolean isReady() {
        if (canvasManager.getEngine().isPresent()) {
           return true;
        }

        return loadEngine();
    }

    private boolean initialized = false;

    public synchronized boolean init(JComponent component) {
        if (initialized) {
            return true;
        }

        // Todo no idea if this should be here, and when to do reinit instead
        canvasManager.init(component);

        if (!loadEngine()) {
            return false;
        }

        initialized = true;

        return initialized;
    }

    private void defaultValues() {
        if (!isReady()) {
            return;
        }

        //textModel = new TextModelImpl();
        //TODO
        if (UIUtils.isDarkLookAndFeel()) {
            setBackgroundColor(config.getDefaultDarkBackgroundColor());
        } else {
            setBackgroundColor(config.getDefaultBackgroundColor());
        }

        setShowEdges(config.isDefaultShowEdges());
        setLightenNonSelectedAuto(config.isDefaultLightenNonSelectedAuto());
        setAutoSelectNeighbors(config.isDefaultAutoSelectNeighbor());
        setHideNonSelectedEdges(config.isDefaultHideNonSelectedEdges());
        setEdgeHasUniColor(config.isDefaultEdgeHasUniColor());
        setEdgeUniColor(config.getDefaultEdgeUniColor().getRGBComponents(null));
        setAdjustByText(config.isDefaultAdjustByText());
        setEdgeSelectionColor(config.isDefaultEdgeSelectionColor());
        setEdgeInSelectionColor(config.getDefaultEdgeInSelectedColor().getRGBComponents(null));
        setEdgeOutSelectionColor(config.getDefaultEdgeOutSelectedColor().getRGBComponents(null));
        setEdgeBothSelectionColor(config.getDefaultEdgeBothSelectedColor().getRGBComponents(null));
        setEdgeScale(config.getDefaultEdgeScale());

        // Text
        setShowNodeLabels(config.isDefaultShowNodeLabels());
        setShowEdgeLabels(config.isDefaultShowEdgeLabels());
        setNodeLabelColor(config.getDefaultNodeLabelColor());
        setEdgeLabelColor(config.getDefaultEdgeLabelColor());
        setNodeLabelColorMode(config.getDefaultNodeLabelColorMode());
        setNodeLabelSizeMode(config.getDefaultNodeLabelSizeMode());
        setNodeLabelFont(config.getDefaultNodeLabelFont());
        setEdgeLabelFont(config.getDefaultEdgeLabelFont());
        setNodeLabelSize(config.getDefaultNodeSizeFactor());
        setEdgeLabelSize(config.getDefaultEdgeSizeFactor());
        setHideNonSelectedLabels(config.isDefaultShowLabelOnSelectedOnly());
    }

    @Override
    public float getZoom() {
        return getEngine().map(VizEngine::getZoom)
            .orElse(1.0f); // Default zoom if engine is not ready
    }

    public void setZoom(float zoom) {
        // TODO : set zoom in the engine
    }

    public boolean isAdjustByText() {
        // TODO: Needs to be added to the engine?
        return false;
    }

    public void setAdjustByText(boolean adjustByText) {
        // TODO: Needs to be added to the engine?
//        firePropertyChange("adjustByText", null, adjustByText);
    }

    @Override
    public boolean isAutoSelectNeighbors() {
        return renderingOptions.isAutoSelectNeighbours();
    }

    public void setAutoSelectNeighbors(boolean autoSelectNeighbor) {
        boolean oldValue = renderingOptions.isAutoSelectNeighbours();
        renderingOptions.setAutoSelectNeighbours(autoSelectNeighbor);
        firePropertyChange("autoSelectNeighbor", oldValue, autoSelectNeighbor);
    }

    @Override
    public Color getBackgroundColor() {
        float[] engineBackgroundColor = getEngine().map(VizEngine::getBackgroundColor)
            .orElse(config.getDefaultBackgroundColor().getRGBComponents(null));
        return new Color(engineBackgroundColor[0], engineBackgroundColor[1],
            engineBackgroundColor[2], engineBackgroundColor[3]);
    }

    public void setBackgroundColor(Color backgroundColor) {
        Color oldValue = getBackgroundColor();
        getEngine().ifPresent(vizEngine -> vizEngine.setBackgroundColor(backgroundColor));

        firePropertyChange("backgroundColor", oldValue, backgroundColor);
    }

    @Override
    public boolean isShowEdges() {
        return renderingOptions.isShowEdges();
    }

    public void setShowEdges(boolean showEdges) {
        boolean oldValue = renderingOptions.isShowEdges();
        renderingOptions.setShowEdges(showEdges);
        firePropertyChange("showEdges", oldValue, showEdges);
    }

    @Override
    public boolean isEdgeHasUniColor() {
        //TODO: Needs to be added to the engine?
        return false;
    }

    public void setEdgeHasUniColor(boolean edgeHasUniColor) {
        //TODO: Needs to be added to the engine?
//        firePropertyChange("edgeHasUniColor", null, edgeHasUniColor);
    }

    @Override
    public float[] getEdgeUniColor() {
        //TODO: Needs to be added to the engine?
        return new float[] {0f, 0f, 0f, 1f}; // Default black color
    }

    public void setEdgeUniColor(float[] edgeUniColor) {
        //TODO: Needs to be added to the engine?
//        firePropertyChange("edgeUniColor", null, edgeUniColor);
    }

    @Override
    public boolean isHideNonSelectedEdges() {
        return renderingOptions.isHideNonSelected();
    }

    public void setHideNonSelectedEdges(boolean hideNonSelectedEdges) {
        boolean oldValue = renderingOptions.isHideNonSelected();
        renderingOptions.setHideNonSelected(hideNonSelectedEdges);
        firePropertyChange("hideNonSelectedEdges", oldValue, hideNonSelectedEdges);
    }

    @Override
    public boolean isLightenNonSelectedAuto() {
        return renderingOptions.isLightenNonSelected();
    }

    public void setLightenNonSelectedAuto(boolean lightenNonSelectedAuto) {
        boolean oldValue = renderingOptions.isLightenNonSelected();
        renderingOptions.setLightenNonSelected(lightenNonSelectedAuto);
        firePropertyChange("lightenNonSelectedAuto", oldValue, lightenNonSelectedAuto);
    }

    @Override
    public boolean isEdgeSelectionColor() {
        return renderingOptions.isEdgeSelectionColor();
    }

    public void setEdgeSelectionColor(boolean edgeSelectionColor) {
        boolean oldValue = renderingOptions.isEdgeSelectionColor();
        renderingOptions.setEdgeSelectionColor(edgeSelectionColor);

        firePropertyChange("edgeSelectionColor", oldValue, edgeSelectionColor);
    }

    @Override
    public float[] getEdgeInSelectionColor() {
        return renderingOptions.getEdgeInSelectionColor().getRGBComponents(null);
    }

    public void setEdgeInSelectionColor(float[] edgeInSelectionColor) {
        Color oldValue = renderingOptions.getEdgeInSelectionColor();
        Color color = new Color(
                edgeInSelectionColor[0], edgeInSelectionColor[1],
                edgeInSelectionColor[2], edgeInSelectionColor[3]);
        renderingOptions.setEdgeInSelectionColor(color);

        firePropertyChange("edgeInSelectionColor", oldValue, edgeInSelectionColor);
    }

    @Override
    public float[] getEdgeOutSelectionColor() {
        return renderingOptions.getEdgeOutSelectionColor().getRGBComponents(null);
    }

    public void setEdgeOutSelectionColor(float[] edgeOutSelectionColor) {
        Color oldValue = renderingOptions.getEdgeOutSelectionColor();
        Color color = new Color(
                edgeOutSelectionColor[0], edgeOutSelectionColor[1],
                edgeOutSelectionColor[2], edgeOutSelectionColor[3]);
        renderingOptions.setEdgeOutSelectionColor(color);

        firePropertyChange("edgeOutSelectionColor", oldValue, edgeOutSelectionColor);
    }

    @Override
    public float[] getEdgeBothSelectionColor() {
        return renderingOptions.getEdgeBothSelectionColor().getRGBComponents(null);
    }

    public void setEdgeBothSelectionColor(float[] edgeBothSelectionColor) {
        Color oldValue = renderingOptions.getEdgeBothSelectionColor();
        Color color = new Color(
                edgeBothSelectionColor[0], edgeBothSelectionColor[1],
                edgeBothSelectionColor[2], edgeBothSelectionColor[3]);
        renderingOptions.setEdgeBothSelectionColor(color);
        firePropertyChange("edgeBothSelectionColor", oldValue, edgeBothSelectionColor);
    }

    @Override
    public float getEdgeScale() {
        return renderingOptions.getEdgeScale();
    }

    public void setEdgeScale(float edgeScale) {
        float oldValue = renderingOptions.getEdgeScale();
        renderingOptions.setEdgeScale(edgeScale);
        firePropertyChange("edgeScale", oldValue, edgeScale);
    }

    // TEXT

    @Override
    public boolean isShowNodeLabels() {
        return showNodeLabels;
    }

    public void setShowNodeLabels(boolean showNodeLabels) {
        boolean oldValue = this.showNodeLabels;
        this.showNodeLabels = showNodeLabels;
        firePropertyChange("showNodeLabels", oldValue, showNodeLabels);
    }

    @Override
    public boolean isShowEdgeLabels(){
        return showEdgeLabels;
    }

    public void setShowEdgeLabels(boolean showEdgeLabels) {
        boolean oldValue = this.showEdgeLabels;
        this.showEdgeLabels = showEdgeLabels;
        firePropertyChange("showEdgeLabels", oldValue, showEdgeLabels);
    }

    @Override
    public Color getNodeLabelColor() {
        return nodeLabelColor;
    }

    public void setNodeLabelColor(Color nodeLabelColor) {
        Color oldValue = this.nodeLabelColor;
        this.nodeLabelColor = nodeLabelColor;
        firePropertyChange("nodeLabelColor", oldValue, nodeLabelColor);
    }

    @Override
    public Color getEdgeLabelColor() {
        return edgeLabelColor;
    }

    public void setEdgeLabelColor(Color edgeLabelColor) {
        Color oldValue = this.edgeLabelColor;
        this.edgeLabelColor = edgeLabelColor;
        firePropertyChange("edgeLabelColor", oldValue, edgeLabelColor);
    }

    @Override
    public LabelColorMode getNodeLabelColorMode() {
        return nodeLabelColorMode;
    }

    public void setNodeLabelColorMode(LabelColorMode nodeLabelColorMode) {
        LabelColorMode oldValue = this.nodeLabelColorMode;
        this.nodeLabelColorMode = nodeLabelColorMode;
        firePropertyChange("nodeLabelColorMode", oldValue, nodeLabelColorMode);
    }

    @Override
    public LabelSizeMode getNodeLabelSizeMode() {
        return nodeLabelSizeMode;
    }

    public void setNodeLabelSizeMode(LabelSizeMode nodeLabelSizeMode) {
        LabelSizeMode oldValue = this.nodeLabelSizeMode;
        this.nodeLabelSizeMode = nodeLabelSizeMode;
        firePropertyChange("nodeLabelSizeMode", oldValue, nodeLabelSizeMode);
    }

    @Override
    public Font getNodeLabelFont() {
        return nodeLabelFont;
    }

    public void setNodeLabelFont(Font nodeLabelFont) {
        Font oldValue = this.nodeLabelFont;
        this.nodeLabelFont = nodeLabelFont;
        firePropertyChange("nodeLabelFont", oldValue, nodeLabelFont);
    }

    @Override
    public Font getEdgeLabelFont() {
        return edgeLabelFont;
    }

    public void setEdgeLabelFont(Font edgeLabelFont) {
        Font oldValue = this.edgeLabelFont;
        this.edgeLabelFont = edgeLabelFont;
        firePropertyChange("edgeLabelFont", oldValue, edgeLabelFont);
    }

    @Override
    public float getNodeLabelSize() {
        return nodeLabelSize;
    }

    public void setNodeLabelSize(float nodeLabelSize) {
        float oldValue = this.nodeLabelSize;
        this.nodeLabelSize = nodeLabelSize;
        firePropertyChange("nodeLabelSize", oldValue, nodeLabelSize);
    }

    @Override
    public float getEdgeLabelSize() {
        return edgeLabelSize;
    }

    public void setEdgeLabelSize(float edgeLabelSize) {
        float oldValue = this.edgeLabelSize;
        this.edgeLabelSize = edgeLabelSize;
        firePropertyChange("edgeLabelSize", oldValue, edgeLabelSize);
    }

    @Override
    public boolean isHideNonSelectedLabels() {
        return hideNonSelectedLabels;
    }

    public void setHideNonSelectedLabels(boolean hideNonSelectedLabels) {
        boolean oldValue = this.hideNonSelectedLabels;
        this.hideNonSelectedLabels = hideNonSelectedLabels;
        firePropertyChange("hideNonSelectedLabels", oldValue, hideNonSelectedLabels);
    }

    @Override
    public Column[] getNodeLabelColumns() {
        return nodeLabelColumns;
    }

    public void setNodeLabelColumns(Column[] nodeLabelColumns) {
        this.nodeLabelColumns = nodeLabelColumns;
        firePropertyChange("nodeLabelColumns", null, nodeLabelColumns);
    }

    @Override
    public Column[] getEdgeLabelColumns() {
        return edgeLabelColumns;
    }

    public void setEdgeLabelColumns(Column[] edgeLabelColumns) {
        this.edgeLabelColumns = edgeLabelColumns;
        firePropertyChange("edgeLabelColumns", null, edgeLabelColumns);
    }

    //EVENTS

    public void fireSelectionChange() {
        //Copy to avoid possible concurrent modification:
        final VisualizationPropertyChangeListener[] listenersCopy = vizController.listeners.toArray(new VisualizationPropertyChangeListener[0]);

        final PropertyChangeEvent evt = new PropertyChangeEvent(this, "selection", null, null);
        for (VisualizationPropertyChangeListener l : listenersCopy) {
            l.propertyChange(this, evt);
        }
    }

    public void firePropertyChange(String propertyName, Object oldvalue, Object newValue) {
        // Do not fire if nothing has changed, supporting null values
        if (oldvalue == null && newValue == null) {
            return;
        }
        if (oldvalue != null && oldvalue.equals(newValue)) {
            return;
        }
        if (newValue != null && newValue.equals(oldvalue)) {
            return;
        }

        //Copy to avoid possible concurrent modification:
        final VisualizationPropertyChangeListener[] listenersCopy = vizController.listeners.toArray(new VisualizationPropertyChangeListener[0]);

        final PropertyChangeEvent evt = new PropertyChangeEvent(this, propertyName, oldvalue, newValue);
        for (VisualizationPropertyChangeListener l : listenersCopy) {
            l.propertyChange(this, evt);
        }
    }

    @Override
    public int getMouseSelectionDiameter() {
        return selectionModel.getMouseSelectionDiameter();
    }

    @Override
    public boolean isMouseSelectionZoomProportional() {
        return selectionModel.isMouseSelectionZoomProportional();
    }

    @Override
    public boolean isBlocked() {
        return selectionModel.isBlocked();
    }

    @Override
    public boolean isRectangleSelection() {
        return selectionModel.isRectangleSelection();
    }

    @Override
    public boolean isDirectMouseSelection() {
        return selectionModel.isDirectMouseSelection();
    }

    @Override
    public boolean isCustomSelection() {
        return selectionModel.isCustomSelection();
    }

    @Override
    public boolean isSelectionEnabled() {
        return selectionModel.isSelectionEnabled();
    }

    @Override
    public List<Node> getSelectedNodes() {
        return Collections.emptyList();
        // TODO : fix
//        return currentEngineSelectionModel()
//            .map(selection -> (List<Node>) new ArrayList<>(selection.getSelectedNodes()))
//            .orElse(Collections.emptyList());
    }

    @Override
    public List<Edge> getSelectedEdges() {
        return Collections.emptyList();
        // TODO : fix
//        return currentEngineSelectionModel()
//            .map(selection -> (List<Edge>) new ArrayList<>(selection.getSelectedEdges()))
//            .orElse(Collections.emptyList());
    }

    //XML
    public void readXML(XMLStreamReader reader, Workspace workspace) throws XMLStreamException {
        boolean end = false;
        while (reader.hasNext() && !end) {
            int type = reader.next();

            switch (type) {
                case XMLStreamReader.START_ELEMENT:
                    String name = reader.getLocalName();
                    if ("textmodel".equalsIgnoreCase(name)) {
//                        textModel.readXML(reader, workspace);
                        //TODO
                    } else if ("cameraposition".equalsIgnoreCase(name)) {
                        // TODO FIx
//                        engine.setTranslate(
//                                Float.parseFloat(reader.getAttributeValue(null, "x")),
//                                Float.parseFloat(reader.getAttributeValue(null, "y"))
//                        );
                    } else if ("cameratarget".equalsIgnoreCase(name)) {
                        // No longer necessary
                    } else if ("showedges".equalsIgnoreCase(name)) {
                        setShowEdges(Boolean.parseBoolean(reader.getAttributeValue(null, "value")));
                    } else if ("lightennonselectedauto".equalsIgnoreCase(name)) {
                        setLightenNonSelectedAuto(Boolean.parseBoolean(reader.getAttributeValue(null, "value")));
                    } else if ("autoselectneighbor".equalsIgnoreCase(name)) {
                        setAutoSelectNeighbors(Boolean.parseBoolean(reader.getAttributeValue(null, "value")));
                    } else if ("hidenonselectededges".equalsIgnoreCase(name)) {
                        setHideNonSelectedEdges(Boolean.parseBoolean(reader.getAttributeValue(null, "value")));
                    } else if ("edgehasunicolor".equalsIgnoreCase(name)) {
                        setEdgeHasUniColor(Boolean.parseBoolean(reader.getAttributeValue(null, "value")));
                    } else if ("adjustbytext".equalsIgnoreCase(name)) {
                        setAdjustByText(Boolean.parseBoolean(reader.getAttributeValue(null, "value")));
                    } else if ("edgeSelectionColor".equalsIgnoreCase(name)) {
                        setEdgeSelectionColor(Boolean.parseBoolean(reader.getAttributeValue(null, "value")));

                    } else if ("backgroundcolor".equalsIgnoreCase(name)) {
                        setBackgroundColor(ColorUtils.decode(reader.getAttributeValue(null, "value")));
                    } else if ("edgeunicolor".equalsIgnoreCase(name)) {
                        setEdgeUniColor(
                            ColorUtils.decode(reader.getAttributeValue(null, "value")).getRGBComponents(null));
                    } else if ("edgeInSelectionColor".equalsIgnoreCase(name)) {
                        setEdgeInSelectionColor(
                            ColorUtils.decode(reader.getAttributeValue(null, "value")).getRGBComponents(null));
                    } else if ("edgeOutSelectionColor".equalsIgnoreCase(name)) {
                        setEdgeOutSelectionColor(
                            ColorUtils.decode(reader.getAttributeValue(null, "value")).getRGBComponents(null));
                    } else if ("edgeBothSelectionColor".equalsIgnoreCase(name)) {
                        setEdgeBothSelectionColor(
                            ColorUtils.decode(reader.getAttributeValue(null, "value")).getRGBComponents(null));

                    } else if ("edgeScale".equalsIgnoreCase(name)) {
                        setEdgeScale(Float.parseFloat(reader.getAttributeValue(null, "value")));
                    } else if("screenshotMaker".equalsIgnoreCase(name)) {
                        // TODO FIx
//                        ScreenshotControllerImpl screenshotMaker = VizController.getInstance().getScreenshotMaker();
//                        if (screenshotMaker != null) {
//                            screenshotMaker.setWidth(Integer.parseInt(reader.getAttributeValue(null, "width")));
//                            screenshotMaker.setHeight(Integer.parseInt(reader.getAttributeValue(null, "height")));
//                            screenshotMaker.setTransparentBackground(Boolean.parseBoolean(reader.getAttributeValue(null, "transparent")));
//                            screenshotMaker.setAutoSave(Boolean.parseBoolean(reader.getAttributeValue(null, "autosave")));
//                            screenshotMaker.setAntiAliasing(Integer.parseInt(reader.getAttributeValue(null, "antialiasing")));
//                            String path = reader.getAttributeValue(null, "path");
//                            if (path != null && !path.isEmpty()) {
//                                File file = new File(reader.getAttributeValue(null, "path"));
//                                if (file.exists()) {
//                                    screenshotMaker.setDefaultDirectory(file);
//                                }
//                            }
//                        }
                    }
                    break;
                case XMLStreamReader.END_ELEMENT:
                    if ("vizmodel".equalsIgnoreCase(reader.getLocalName())) {
                        end = true;
                    }
                    break;
            }
        }
    }

    public void writeXML(XMLStreamWriter writer) throws XMLStreamException {
        //Fast refresh
        // TODO: Fix
        final Vector2fc cameraPosition = null;

        //TextModel
        //        textModel.writeXML(writer);
        //TODO

        //Camera
        writer.writeStartElement("cameraposition");
        writer.writeAttribute("x", Float.toString(cameraPosition.x()));
        writer.writeAttribute("y", Float.toString(cameraPosition.y()));
        writer.writeAttribute("z", Float.toString(0));
        writer.writeEndElement();

        writer.writeStartElement("showedges");
        writer.writeAttribute("value", String.valueOf(isShowEdges()));
        writer.writeEndElement();

        writer.writeStartElement("lightennonselectedauto");
        writer.writeAttribute("value", String.valueOf(isLightenNonSelectedAuto()));
        writer.writeEndElement();

        writer.writeStartElement("autoselectneighbor");
        writer.writeAttribute("value", String.valueOf(isAutoSelectNeighbors()));
        writer.writeEndElement();

        writer.writeStartElement("hidenonselectededges");
        writer.writeAttribute("value", String.valueOf(isHideNonSelectedEdges()));
        writer.writeEndElement();

        writer.writeStartElement("edgehasunicolor");
        writer.writeAttribute("value", String.valueOf(isEdgeHasUniColor()));
        writer.writeEndElement();

        writer.writeStartElement("adjustbytext");
        writer.writeAttribute("value", String.valueOf(isAdjustByText()));
        writer.writeEndElement();

        writer.writeStartElement("edgeSelectionColor");
        writer.writeAttribute("value", String.valueOf(isEdgeSelectionColor()));
        writer.writeEndElement();

        //Colors
        writer.writeStartElement("backgroundcolor");
        writer.writeAttribute("value", ColorUtils.encode(getBackgroundColor()));
        writer.writeEndElement();

        writer.writeStartElement("edgeunicolor");
        writer.writeAttribute("value", ColorUtils.encode(ColorUtils.decode(getEdgeUniColor())));
        writer.writeEndElement();

        writer.writeStartElement("edgeInSelectionColor");
        writer.writeAttribute("value", ColorUtils.encode(ColorUtils.decode(getEdgeInSelectionColor())));
        writer.writeEndElement();

        writer.writeStartElement("edgeOutSelectionColor");
        writer.writeAttribute("value", ColorUtils.encode(ColorUtils.decode(getEdgeOutSelectionColor())));
        writer.writeEndElement();

        writer.writeStartElement("edgeBothSelectionColor");
        writer.writeAttribute("value", ColorUtils.encode(ColorUtils.decode(getEdgeBothSelectionColor())));
        writer.writeEndElement();

        //Float
        writer.writeStartElement("edgeScale");
        writer.writeAttribute("value", String.valueOf(getEdgeScale()));
        writer.writeEndElement();

        //Screenshot settings
        // TODO Fix
//        ScreenshotControllerImpl screenshotMaker = VizController.getInstance().getScreenshotMaker();
//        if (screenshotMaker != null) {
//            writer.writeStartElement("screenshotMaker");
//            writer.writeAttribute("width", String.valueOf(screenshotMaker.getWidth()));
//            writer.writeAttribute("height", String.valueOf(screenshotMaker.getHeight()));
//            writer.writeAttribute("antialiasing", String.valueOf(screenshotMaker.getAntiAliasing()));
//            writer.writeAttribute("transparent", String.valueOf(screenshotMaker.isTransparentBackground()));
//            writer.writeAttribute("autosave", String.valueOf(screenshotMaker.isAutoSave()));
//            if (screenshotMaker.getDefaultDirectory() != null) {
//                writer.writeAttribute("path", screenshotMaker.getDefaultDirectory());
//            }
//            writer.writeEndElement();
//        }
    }
}
