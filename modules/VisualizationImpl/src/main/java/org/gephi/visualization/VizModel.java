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

import com.jogamp.newt.event.NEWTEvent;
import java.awt.Color;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.Optional;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.project.api.Workspace;
import org.gephi.ui.utils.ColorUtils;
import org.gephi.ui.utils.UIUtils;
import org.gephi.visualization.api.EdgeColorMode;
import org.gephi.visualization.api.LabelColorMode;
import org.gephi.visualization.api.LabelSizeMode;
import org.gephi.visualization.api.VisualisationModel;
import org.gephi.visualization.api.VisualizationPropertyChangeListener;
import org.gephi.visualization.apiimpl.VizConfig;
import org.gephi.visualization.screenshot.ScreenshotModelImpl;
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.jogl.JOGLRenderingTarget;
import org.gephi.viz.engine.status.GraphRenderingOptions;
import org.gephi.viz.engine.status.GraphRenderingOptionsImpl;
import org.joml.Vector2fc;
import org.openide.util.Lookup;

/**
 * @author Mathieu Bastian
 */
public class VizModel implements VisualisationModel {

    private final VizController vizController;
    private final Workspace workspace;
    private final GraphModel graphModel;

    protected final VizConfig config;

    //Global
    private float zoom;
    private Color backgroundColor;

    //Edges
    private boolean showEdges;
    private float edgeScale;
    private boolean edgeSelectionColor;
    private Color edgeBothSelectionColor;
    private Color edgeInSelectionColor;
    private Color edgeOutSelectionColor;
    private EdgeColorMode edgeColorMode;
    private boolean edgeWeightEnabled;

    //Nodes
    private float nodeScale;

    //Selection:
    private boolean autoSelectNeighbours;
    private boolean hideNonSelectedEdges;
    private boolean lightenNonSelected;
    private float lightenNonSelectedFactor;

    //Node Labels
    private boolean showNodeLabels;
    private Font nodeLabelFont;
    private float nodeLabelScale;
    private LabelColorMode nodeLabelColorMode;
    private LabelSizeMode nodeLabelSizeMode;
    private boolean hideNonSelectedLabels;
    private Column[] nodeLabelColumns = new Column[0];

    //Edge Labels
    private boolean showEdgeLabels;
    private Font edgeLabelFont;
    private float edgeLabelSize;
    private Column[] edgeLabelColumns = new Column[0];

    // Selection
    private final SelectionModelImpl selectionModel;

    public VizModel(VizController controller, Workspace workspace) {
        this.vizController = controller;
        this.workspace = workspace;
        this.config = new VizConfig();
        this.graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel(workspace);
        this.selectionModel = new SelectionModelImpl(this, config);

        // Initialize default values
        defaultValues();
    }

    private void defaultValues() {
        //Global
        if (UIUtils.isDarkLookAndFeel()) {
            setBackgroundColor(config.getDefaultDarkBackgroundColor());
        } else {
            setBackgroundColor(config.getDefaultBackgroundColor());
        }
        setZoom(config.getDefaultZoom());

        //Edges
        setShowEdges(config.isDefaultShowEdges());
        setEdgeScale(config.getDefaultEdgeScale());
        setEdgeSelectionColor(config.isDefaultEdgeSelectionColor());
        setEdgeInSelectionColor(config.getDefaultEdgeInSelectedColor());
        setEdgeOutSelectionColor(config.getDefaultEdgeOutSelectedColor());
        setEdgeBothSelectionColor(config.getDefaultEdgeBothSelectedColor());
        setEdgeColorMode(config.getDefaultEdgeColorMode());
        setUseEdgeWeight(config.isDefaultUseEdgeWeight());

        //Nodes
        setNodeScale(config.getDefaultNodeScale());

        //Selection
        setAutoSelectNeighbors(config.isDefaultAutoSelectNeighbor());
        setHideNonSelectedEdges(config.isDefaultHideNonSelectedEdges());
        setLightenNonSelectedAuto(config.isDefaultLightenNonSelectedAuto());
        setLightenNonSelectedFactor(config.getDefaultLightenNonSelectedFactor());

        //Node Labels
        setShowNodeLabels(config.isDefaultShowNodeLabels());
        setNodeLabelColorMode(config.getDefaultNodeLabelColorMode());
        setNodeLabelSizeMode(config.getDefaultNodeLabelSizeMode());
        setNodeLabelFont(config.getDefaultNodeLabelFont());
        setNodeLabelScale(config.getDefaultNodeLabelScale());
        setHideNonSelectedLabels(config.isDefaultHideNonSelectedNodeLabels());
        setNodeLabelColumns(new Column[] {this.graphModel.getNodeTable().getColumn("label")});

        //Edge Labels
        setShowEdgeLabels(config.isDefaultShowEdgeLabels());
        setEdgeLabelFont(config.getDefaultEdgeLabelFont());
        setEdgeLabelScale(config.getDefaultEdgeLabelScale());
        setEdgeLabelColumns(new Column[] {this.graphModel.getEdgeTable().getColumn("label")});
    }

    public GraphRenderingOptions toGraphRenderingOptions() {
        GraphRenderingOptionsImpl options = new GraphRenderingOptionsImpl();
        options.setAutoSelectNeighbours(isAutoSelectNeighbors());
        options.setBackgroundColor(getBackgroundColor());
        options.setEdgeBothSelectionColor(getEdgeBothSelectionColor());
        options.setEdgeInSelectionColor(getEdgeInSelectionColor());
        options.setEdgeOutSelectionColor(getEdgeOutSelectionColor());
        options.setEdgeColorMode(GraphRenderingOptions.EdgeColorMode.valueOf(getEdgeColorMode().name()));
        options.setEdgeScale(getEdgeScale());
        options.setEdgeSelectionColor(isEdgeSelectionColor());
        options.setEdgeWeightEnabled(isUseEdgeWeight());
        options.setHideNonSelectedEdges(isHideNonSelectedEdges());
        options.setLightenNonSelected(isLightenNonSelectedAuto());
        options.setLightenNonSelectedFactor(getLightenNonSelectedFactor());
        options.setNodeScale(getNodeScale());
        options.setShowEdges(isShowEdges());
        options.setShowEdgeLabels(isShowEdgeLabels());
        options.setShowNodeLabels(isShowNodeLabels());
        return options;
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
        return vizController.getCanvasManager().getEngine();
    }

    private Optional<GraphRenderingOptions> getRenderingOptions() {
        return vizController.getCanvasManager().getEngine().map(VizEngine::getRenderingOptions);
    }

//    private boolean loadEngine() {
//        VizEngine<JOGLRenderingTarget, NEWTEvent> engine = canvasManager.getEngine().orElse(null);
//        if (engine == null) {
//            return false; // Engine still not ready in the workspace
//        }
//        this.renderingOptions = engine.getRenderingOptions();
//
//        defaultValues();
//
//        return true;
//    }
//
//    public synchronized void init(JComponent component) {
//        if (canvasManager.isInitialized()) {
//            return;
//        }
//
//        this.renderingOptions = canvasManager.init(component).getRenderingOptions();
//        defaultValues();
//    }


    @Override
    public float getZoom() {
        return zoom;
    }

    public void setZoom(float zoom) {
        float oldValue = this.zoom;
        if (oldValue != zoom) {
            this.zoom = zoom;
            getEngine().ifPresent(vizEngine -> vizEngine.setZoom(zoom));
            firePropertyChange("zoom", oldValue, zoom);
        }
    }

    @Override
    public int getFps() {
        return getEngine().map(VizEngine::getFps)
            .orElse(0);
    }

    @Override
    public boolean isAutoSelectNeighbors() {
        return autoSelectNeighbours;
    }

    public void setAutoSelectNeighbors(boolean autoSelectNeighbor) {
        boolean oldValue = this.autoSelectNeighbours;
        if (oldValue != autoSelectNeighbor) {
            this.autoSelectNeighbours = autoSelectNeighbor;
            getRenderingOptions().ifPresent(options -> options.setAutoSelectNeighbours(autoSelectNeighbor));
            firePropertyChange("autoSelectNeighbor", oldValue, autoSelectNeighbor);
        }
    }

    @Override
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        Color oldValue = this.backgroundColor;
        if (oldValue != null && oldValue.equals(backgroundColor)) {
            return;
        }
        this.backgroundColor = backgroundColor;
        getEngine().ifPresent(vizEngine -> vizEngine.setBackgroundColor(backgroundColor));
        firePropertyChange("backgroundColor", oldValue, backgroundColor);
    }

    @Override
    public boolean isShowEdges() {
        return showEdges;
    }

    public void setShowEdges(boolean showEdges) {
        boolean oldValue = this.showEdges;
        if (oldValue != showEdges) {
            this.showEdges = showEdges;
            getRenderingOptions().ifPresent(options -> options.setShowEdges(showEdges));
            firePropertyChange("showEdges", oldValue, showEdges);
        }
    }

    @Override
    public EdgeColorMode getEdgeColorMode() {
        return edgeColorMode;
    }

    public void setEdgeColorMode(EdgeColorMode edgeColorMode) {
        EdgeColorMode oldValue = getEdgeColorMode();
        if (oldValue != edgeColorMode) {
            this.edgeColorMode = edgeColorMode;
            getRenderingOptions().ifPresent(options -> {
                switch (edgeColorMode) {
                    case SELF:
                        options.setEdgeColorMode(GraphRenderingOptions.EdgeColorMode.SELF);
                        break;
                    case SOURCE:
                        options.setEdgeColorMode(GraphRenderingOptions.EdgeColorMode.SOURCE);
                        break;
                    case TARGET:
                        options.setEdgeColorMode(GraphRenderingOptions.EdgeColorMode.TARGET);
                        break;
                    case MIXED:
                        options.setEdgeColorMode(GraphRenderingOptions.EdgeColorMode.MIXED);
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown EdgeColorMode: " + edgeColorMode);
                }
            });
            firePropertyChange("edgeColorMode", oldValue, edgeColorMode);
        }
    }

    @Override
    public boolean isHideNonSelectedEdges() {
        return hideNonSelectedEdges;
    }

    public void setHideNonSelectedEdges(boolean hideNonSelectedEdges) {
        boolean oldValue = this.hideNonSelectedEdges;
        if (oldValue != hideNonSelectedEdges) {
            this.hideNonSelectedEdges = hideNonSelectedEdges;
            getRenderingOptions().ifPresent(options -> options.setHideNonSelectedEdges(hideNonSelectedEdges));
            firePropertyChange("hideNonSelectedEdges", oldValue, hideNonSelectedEdges);
        }
    }

    @Override
    public boolean isLightenNonSelectedAuto() {
        return lightenNonSelected;
    }

    public void setLightenNonSelectedAuto(boolean lightenNonSelectedAuto) {
        boolean oldValue = this.lightenNonSelected;
        if (oldValue != lightenNonSelectedAuto) {
            this.lightenNonSelected = lightenNonSelectedAuto;
            getRenderingOptions().ifPresent(options -> options.setLightenNonSelected(lightenNonSelectedAuto));
            firePropertyChange("lightenNonSelectedAuto", oldValue, lightenNonSelectedAuto);
        }
        firePropertyChange("lightenNonSelectedAuto", oldValue, lightenNonSelectedAuto);
    }

    public float getLightenNonSelectedFactor() {
        return lightenNonSelectedFactor;
    }

    public void setLightenNonSelectedFactor(float lightenNonSelectedFactor) {
        float oldValue = this.lightenNonSelectedFactor;
        if (oldValue != lightenNonSelectedFactor) {
            this.lightenNonSelectedFactor = lightenNonSelectedFactor;
            getRenderingOptions().ifPresent(options -> options.setLightenNonSelectedFactor(lightenNonSelectedFactor));
            firePropertyChange("lightenNonSelectedFactor", oldValue, lightenNonSelectedFactor);
        }
        firePropertyChange("lightenNonSelectedFactor", oldValue, lightenNonSelectedFactor);
    }

    @Override
    public boolean isEdgeSelectionColor() {
        return edgeSelectionColor;
    }

    public void setEdgeSelectionColor(boolean edgeSelectionColor) {
        boolean oldValue = this.edgeSelectionColor;
        if (oldValue != edgeSelectionColor) {
            this.edgeSelectionColor = edgeSelectionColor;
            getRenderingOptions().ifPresent(options -> options.setEdgeSelectionColor(edgeSelectionColor));
            firePropertyChange("edgeSelectionColor", oldValue, edgeSelectionColor);
        }

        firePropertyChange("edgeSelectionColor", oldValue, edgeSelectionColor);
    }

    @Override
    public Color getEdgeInSelectionColor() {
        return edgeInSelectionColor;
    }

    public void setEdgeInSelectionColor(Color edgeInSelectionColor) {
        Color oldValue = this.edgeInSelectionColor;
        if (oldValue != edgeInSelectionColor) {
            this.edgeInSelectionColor = edgeInSelectionColor;
            getRenderingOptions().ifPresent(options -> options.setEdgeInSelectionColor(edgeInSelectionColor));
            firePropertyChange("edgeInSelectionColor", oldValue, edgeInSelectionColor);
        }
    }

    @Override
    public Color getEdgeOutSelectionColor() {
        return edgeOutSelectionColor;
    }

    public void setEdgeOutSelectionColor(Color edgeOutSelectionColor) {
        Color oldValue = this.edgeOutSelectionColor;
        if (oldValue != edgeOutSelectionColor) {
            this.edgeOutSelectionColor = edgeOutSelectionColor;
            getRenderingOptions().ifPresent(options -> options.setEdgeOutSelectionColor(edgeOutSelectionColor));
            firePropertyChange("edgeOutSelectionColor", oldValue, edgeOutSelectionColor);
        }
    }

    @Override
    public Color getEdgeBothSelectionColor() {
        return edgeBothSelectionColor;
    }

    public void setEdgeBothSelectionColor(Color edgeBothSelectionColor) {
        Color oldValue = this.edgeBothSelectionColor;
        if (oldValue != edgeBothSelectionColor) {
            this.edgeBothSelectionColor = edgeBothSelectionColor;
            getRenderingOptions().ifPresent(options -> options.setEdgeBothSelectionColor(edgeBothSelectionColor));
            firePropertyChange("edgeBothSelectionColor", oldValue, edgeBothSelectionColor);
        }
    }

    @Override
    public float getNodeScale() {
        return nodeScale;
    }

    public void setNodeScale(float nodeScale) {
        float oldValue = this.nodeScale;
        if (oldValue != nodeScale) {
            this.nodeScale = nodeScale;
            getRenderingOptions().ifPresent(options -> options.setNodeScale(nodeScale));
            firePropertyChange("nodeScale", oldValue, nodeScale);
        }
    }

    @Override
    public float getEdgeScale() {
        return edgeScale;
    }

    public void setEdgeScale(float edgeScale) {
        float oldValue = this.edgeScale;
        if (oldValue != edgeScale) {
            this.edgeScale = edgeScale;
            getRenderingOptions().ifPresent(options -> options.setEdgeScale(edgeScale));
            firePropertyChange("edgeScale", oldValue, edgeScale);
        }
    }

    @Override
    public boolean isUseEdgeWeight() {
        return edgeWeightEnabled;
    }

    public void setUseEdgeWeight(boolean useEdgeWeight) {
        boolean oldValue = this.edgeWeightEnabled;
        if (oldValue != useEdgeWeight) {
            this.edgeWeightEnabled = useEdgeWeight;
            getRenderingOptions().ifPresent(options -> options.setEdgeWeightEnabled(useEdgeWeight));
            firePropertyChange("useEdgeWeight", oldValue, useEdgeWeight);
        }
    }

    // TEXT

    @Override
    public boolean isShowNodeLabels() {
        return showNodeLabels;
    }

    public void setShowNodeLabels(boolean showNodeLabels) {
        boolean oldValue = this.showNodeLabels;
        if (oldValue != showNodeLabels) {
            this.showNodeLabels = showNodeLabels;
            getRenderingOptions().ifPresent(options -> options.setShowNodeLabels(showNodeLabels));
            firePropertyChange("showNodeLabels", oldValue, showNodeLabels);
        }
    }

    @Override
    public boolean isShowEdgeLabels() {
        return showEdgeLabels;
    }

    public void setShowEdgeLabels(boolean showEdgeLabels) {
        boolean oldValue = this.showEdgeLabels;
        if (oldValue != showEdgeLabels) {
            this.showEdgeLabels = showEdgeLabels;
            getRenderingOptions().ifPresent(options -> options.setShowEdgeLabels(showEdgeLabels));
            firePropertyChange("showEdgeLabels", oldValue, showEdgeLabels);
        }
    }

    @Override
    public LabelColorMode getNodeLabelColorMode() {
        return nodeLabelColorMode;
    }

    public void setNodeLabelColorMode(LabelColorMode nodeLabelColorMode) {
        LabelColorMode oldValue = this.nodeLabelColorMode;
        if (oldValue != nodeLabelColorMode) {
            this.nodeLabelColorMode = nodeLabelColorMode;
            firePropertyChange("nodeLabelColorMode", oldValue, nodeLabelColorMode);
        }
    }

    @Override
    public LabelSizeMode getNodeLabelSizeMode() {
        return nodeLabelSizeMode;
    }

    public void setNodeLabelSizeMode(LabelSizeMode nodeLabelSizeMode) {
        LabelSizeMode oldValue = this.nodeLabelSizeMode;
        if (oldValue != nodeLabelSizeMode) {
            this.nodeLabelSizeMode = nodeLabelSizeMode;
            firePropertyChange("nodeLabelSizeMode", oldValue, nodeLabelSizeMode);
        }
    }

    @Override
    public Font getNodeLabelFont() {
        return nodeLabelFont;
    }

    public void setNodeLabelFont(Font nodeLabelFont) {
        Font oldValue = this.nodeLabelFont;
        if (oldValue != nodeLabelFont) {
            this.nodeLabelFont = nodeLabelFont;
            firePropertyChange("nodeLabelFont", oldValue, nodeLabelFont);
        }
    }

    @Override
    public Font getEdgeLabelFont() {
        return edgeLabelFont;
    }

    public void setEdgeLabelFont(Font edgeLabelFont) {
        Font oldValue = this.edgeLabelFont;
        if (oldValue != edgeLabelFont) {
            this.edgeLabelFont = edgeLabelFont;
            firePropertyChange("edgeLabelFont", oldValue, edgeLabelFont);
        }
    }

    @Override
    public float getNodeLabelScale() {
        return nodeLabelScale;
    }

    public void setNodeLabelScale(float nodeLabelScale) {
        float oldValue = this.nodeLabelScale;
        if (oldValue != nodeLabelScale) {
            this.nodeLabelScale = nodeLabelScale;
            firePropertyChange("nodeLabelScale", oldValue, nodeLabelScale);
        }
    }

    @Override
    public float getEdgeLabelScale() {
        return edgeLabelSize;
    }

    public void setEdgeLabelScale(float edgeLabelScale) {
        float oldValue = this.edgeLabelSize;
        if (oldValue != edgeLabelScale) {
            this.edgeLabelSize = edgeLabelScale;
            firePropertyChange("edgeLabelScale", oldValue, edgeLabelScale);
        }
    }

    @Override
    public boolean isHideNonSelectedLabels() {
        return hideNonSelectedLabels;
    }

    public void setHideNonSelectedLabels(boolean hideNonSelectedLabels) {
        boolean oldValue = this.hideNonSelectedLabels;
        if (oldValue != hideNonSelectedLabels) {
            this.hideNonSelectedLabels = hideNonSelectedLabels;
            firePropertyChange("hideNonSelectedLabels", oldValue, hideNonSelectedLabels);
        }
    }

    @Override
    public Column[] getNodeLabelColumns() {
        return nodeLabelColumns;
    }

    public void setNodeLabelColumns(Column[] nodeLabelColumns) {
        Column[] oldValue = this.nodeLabelColumns;
        if (oldValue != nodeLabelColumns) {
            this.nodeLabelColumns = nodeLabelColumns;
            firePropertyChange("nodeLabelColumns", oldValue, nodeLabelColumns);
        }
    }

    @Override
    public Column[] getEdgeLabelColumns() {
        return edgeLabelColumns;
    }

    public void setEdgeLabelColumns(Column[] edgeLabelColumns) {
        Column[] oldValue = this.edgeLabelColumns;
        if (oldValue != edgeLabelColumns) {
            this.edgeLabelColumns = edgeLabelColumns;
            firePropertyChange("edgeLabelColumns", oldValue, edgeLabelColumns);
        }
    }

    //EVENTS

    public void fireSelectionChange() {
        //Copy to avoid possible concurrent modification:
        final VisualizationPropertyChangeListener[] listenersCopy =
            vizController.listeners.toArray(new VisualizationPropertyChangeListener[0]);

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
        final VisualizationPropertyChangeListener[] listenersCopy =
            vizController.listeners.toArray(new VisualizationPropertyChangeListener[0]);

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
    public boolean isNodeSelection() {
        return selectionModel.isNodeSelection();
    }

    @Override
    public boolean isSingleNodeSelection() {
        return selectionModel.isNodeSelection() && selectionModel.isSingleNodeSelection();
    }

    @Override
    public List<Node> getSelectedNodes() {
        return selectionModel.getSelectedNodes();
    }

    @Override
    public List<Edge> getSelectedEdges() {
        return selectionModel.getSelectedEdges();
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
                    } else if ("edgeSelectionColor".equalsIgnoreCase(name)) {
                        setEdgeSelectionColor(Boolean.parseBoolean(reader.getAttributeValue(null, "value")));

                    } else if ("backgroundcolor".equalsIgnoreCase(name)) {
                        setBackgroundColor(ColorUtils.decode(reader.getAttributeValue(null, "value")));
                    } else if ("edgeInSelectionColor".equalsIgnoreCase(name)) {
                        setEdgeInSelectionColor(
                            ColorUtils.decode(reader.getAttributeValue(null, "value")));
                    } else if ("edgeOutSelectionColor".equalsIgnoreCase(name)) {
                        setEdgeOutSelectionColor(
                            ColorUtils.decode(reader.getAttributeValue(null, "value")));
                    } else if ("edgeBothSelectionColor".equalsIgnoreCase(name)) {
                        setEdgeBothSelectionColor(
                            ColorUtils.decode(reader.getAttributeValue(null, "value")));

                    } else if ("edgeScale".equalsIgnoreCase(name)) {
                        setEdgeScale(Float.parseFloat(reader.getAttributeValue(null, "value")));
                    } else if ("screenshotMaker".equalsIgnoreCase(name)) {
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

        writer.writeStartElement("edgeSelectionColor");
        writer.writeAttribute("value", String.valueOf(isEdgeSelectionColor()));
        writer.writeEndElement();

        //Colors
        writer.writeStartElement("backgroundcolor");
        writer.writeAttribute("value", ColorUtils.encode(getBackgroundColor()));
        writer.writeEndElement();

        writer.writeStartElement("edgeInSelectionColor");
        writer.writeAttribute("value", ColorUtils.encode(getEdgeInSelectionColor()));
        writer.writeEndElement();

        writer.writeStartElement("edgeOutSelectionColor");
        writer.writeAttribute("value", ColorUtils.encode(getEdgeOutSelectionColor()));
        writer.writeEndElement();

        writer.writeStartElement("edgeBothSelectionColor");
        writer.writeAttribute("value", ColorUtils.encode(getEdgeBothSelectionColor()));
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
