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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.gephi.graph.api.AttributeUtils;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Estimator;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Node;
import org.gephi.project.api.Workspace;
import org.gephi.ui.utils.ColorUtils;
import org.gephi.ui.utils.FontUtils;
import org.gephi.ui.utils.UIUtils;
import org.gephi.visualization.api.EdgeColorMode;
import org.gephi.visualization.api.LabelColorMode;
import org.gephi.visualization.api.LabelSizeMode;
import org.gephi.visualization.api.VisualizationModel;
import org.gephi.visualization.api.VisualizationPropertyChangeListener;
import org.gephi.visualization.screenshot.ScreenshotModelImpl;
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.jogl.JOGLRenderingTarget;
import org.gephi.viz.engine.status.GraphRenderingOptions;
import org.gephi.viz.engine.status.GraphRenderingOptionsImpl;
import org.gephi.viz.engine.status.GraphSelection;
import org.gephi.viz.engine.util.text.TextLabelBuilder;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.openide.util.Lookup;

/**
 * @author Mathieu Bastian
 */
public class VizModel implements VisualizationModel {

    private final VizController vizController;
    private final Workspace workspace;
    private final GraphModel graphModel;

    //Global
    private float zoom;
    private Vector2fc pan;
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
    private boolean edgeRescaleWeightEnabled;

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
    private boolean hideNonSelectedNodeLabels;
    private boolean fitNodeLabelsToNodeSize;
    private boolean avoidNodeLabelOverlap;
    private Column[] nodeLabelColumns = new Column[0];

    //Edge Labels
    private boolean showEdgeLabels;
    private Font edgeLabelFont;
    private float edgeLabelScale;
    private LabelColorMode edgeLabelColorMode;
    private LabelSizeMode edgeLabelSizeMode;
    private boolean hideNonSelectedEdgeLabels;
    private Column[] edgeLabelColumns = new Column[0];

    // Selection
    private final SelectionModelImpl selectionModel;
    private final ScreenshotModelImpl screenshotModel;

    public VizModel(VizController controller, Workspace workspace) {
        this.vizController = controller;
        this.workspace = workspace;
        this.graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel(workspace);
        this.selectionModel = new SelectionModelImpl(this);
        this.screenshotModel = new ScreenshotModelImpl(this);

        // Initialize default values
        defaultValues();
    }

    private void defaultValues() {
        //Global
        if (UIUtils.isDarkLookAndFeel()) {
            this.backgroundColor = VizConfig.getDefaultDarkBackgroundColor();
        } else {
            this.backgroundColor = VizConfig.getDefaultBackgroundColor();
        }
        this.zoom = VizConfig.getDefaultZoom();
        this.pan = VizConfig.getDefaultPan();

        //Edges
        this.showEdges = VizConfig.isDefaultShowEdges();
        this.edgeScale = VizConfig.getDefaultEdgeScale();
        this.edgeSelectionColor = VizConfig.isDefaultEdgeSelectionColor();
        this.edgeInSelectionColor = VizConfig.getDefaultEdgeInSelectedColor();
        this.edgeOutSelectionColor = VizConfig.getDefaultEdgeOutSelectedColor();
        this.edgeBothSelectionColor = VizConfig.getDefaultEdgeBothSelectedColor();
        this.edgeColorMode = VizConfig.getDefaultEdgeColorMode();
        this.edgeWeightEnabled = VizConfig.isDefaultUseEdgeWeight();
        this.edgeRescaleWeightEnabled = VizConfig.isDefaultRescaleEdgeWeight();

        //Nodes
        this.nodeScale = VizConfig.getDefaultNodeScale();

        //Selection
        this.autoSelectNeighbours = VizConfig.isDefaultAutoSelectNeighbor();
        this.hideNonSelectedEdges = VizConfig.isDefaultHideNonSelectedEdges();
        this.lightenNonSelected = VizConfig.isDefaultLightenNonSelectedAuto();
        this.lightenNonSelectedFactor = VizConfig.getDefaultLightenNonSelectedFactor();

        //Node Labels
        this.showNodeLabels = VizConfig.isDefaultShowNodeLabels();
        this.nodeLabelColorMode = VizConfig.getDefaultNodeLabelColorMode();
        this.nodeLabelSizeMode = VizConfig.getDefaultNodeLabelSizeMode();
        this.nodeLabelFont = VizConfig.getDefaultNodeLabelFont();
        this.nodeLabelScale = VizConfig.getDefaultNodeLabelScale();
        this.hideNonSelectedNodeLabels = VizConfig.isDefaultHideNonSelectedNodeLabels();
        this.fitNodeLabelsToNodeSize = VizConfig.isDefaultFitNodeLabelsToNodeSize();
        this.avoidNodeLabelOverlap = VizConfig.isDefaultAvoidNodeLabelOverlap();
        this.nodeLabelColumns = new Column[] {this.graphModel.defaultColumns().nodeLabel()};

        //Edge Labels
        this.showEdgeLabels = VizConfig.isDefaultShowEdgeLabels();
        this.edgeLabelColorMode = VizConfig.getDefaultEdgeLabelColorMode();
        this.edgeLabelSizeMode = VizConfig.getDefaultEdgeLabelSizeMode();
        this.edgeLabelFont = VizConfig.getDefaultEdgeLabelFont();
        this.edgeLabelScale = VizConfig.getDefaultEdgeLabelScale();
        this.hideNonSelectedEdgeLabels = VizConfig.isDefaultHideNonSelectedEdgeLabels();
        this.edgeLabelColumns = new Column[] {this.graphModel.defaultColumns().edgeLabel()};
    }

    public GraphRenderingOptions toGraphRenderingOptions() {
        GraphRenderingOptionsImpl options = new GraphRenderingOptionsImpl();
        options.setZoom(getZoom());
        options.setPan(getPan());
        options.setAutoSelectNeighbours(isAutoSelectNeighbors());
        options.setBackgroundColor(getBackgroundColor());
        options.setEdgeBothSelectionColor(getEdgeBothSelectionColor());
        options.setEdgeInSelectionColor(getEdgeInSelectionColor());
        options.setEdgeOutSelectionColor(getEdgeOutSelectionColor());
        options.setEdgeColorMode(GraphRenderingOptions.EdgeColorMode.valueOf(getEdgeColorMode().name()));
        options.setEdgeScale(getEdgeScale());
        options.setEdgeSelectionColor(isEdgeSelectionColor());
        options.setEdgeWeightEnabled(isUseEdgeWeight());
        options.setEdgeRescaleWeightEnabled(isRescaleEdgeWeight());
        options.setHideNonSelectedEdges(isHideNonSelectedEdges());
        options.setLightenNonSelected(isLightenNonSelectedAuto());
        options.setLightenNonSelectedFactor(getLightenNonSelectedFactor());
        options.setNodeScale(getNodeScale());
        options.setShowEdges(isShowEdges());
        options.setShowEdgeLabels(isShowEdgeLabels());
        options.setShowNodeLabels(isShowNodeLabels());
        options.setNodeLabelSizeMode(GraphRenderingOptions.LabelSizeMode.valueOf(getNodeLabelSizeMode().name()));
        options.setNodeLabelColorMode(GraphRenderingOptions.LabelColorMode.valueOf(getNodeLabelColorMode().name()));
        options.setNodeLabelFont(getNodeLabelFont());
        options.setNodeLabelScale(getNodeLabelScale());
        options.setNodeLabelFitToNodeSize(isNodeLabelFitToNodeSize());
        options.setHideNonSelectedNodeLabels(isHideNonSelectedNodeLabels());
        options.setAvoidNodeLabelOverlap(isAvoidNodeLabelOverlap());
        options.setNodeLabelColumns(getNodeLabelColumns());
        options.setEdgeLabelColorMode(GraphRenderingOptions.LabelColorMode.valueOf(getEdgeLabelColorMode().name()));
        options.setEdgeLabelSizeMode(GraphRenderingOptions.LabelSizeMode.valueOf(getEdgeLabelSizeMode().name()));
        options.setEdgeLabelFont(getEdgeLabelFont());
        options.setEdgeLabelScale(getEdgeLabelScale());
        options.setHideNonSelectedEdgeLabels(isHideNonSelectedEdgeLabels());
        options.setEdgeLabelColumns(getEdgeLabelColumns());
        return options;
    }

    public GraphSelection toGraphSelection() {
        return selectionModel.toGraphSelection();
    }

    public void unsetup() {
        getEngine().ifPresent(d -> {
            GraphRenderingOptions options = d.getRenderingOptions();
            this.zoom = options.getZoom();
            this.pan = new Vector2f(options.getPan());
        });
    }

    public SelectionModelImpl getSelectionModel() {
        return selectionModel;
    }

    public ScreenshotModelImpl getScreenshotModel() {
        return screenshotModel;
    }

    @Override
    public Workspace getWorkspace() {
        return workspace;
    }

    public Optional<VizEngine<JOGLRenderingTarget, NEWTEvent>> getEngine() {
        return vizController.getCanvasManager().getEngine()
            .filter(e -> e.getGraphModel() == graphModel);
    }

    private Optional<GraphRenderingOptions> getRenderingOptions() {
        return getEngine().map(VizEngine::getRenderingOptions);
    }

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

    public Vector2fc getPan() {
        return pan;
    }

    public void setPan(Vector2f pan) {
        Vector2fc oldValue = this.pan;
        if (!pan.equals(oldValue)) {
            this.pan = pan;
            getEngine().ifPresent(vizEngine -> vizEngine.setTranslate(pan));
            firePropertyChange("pan", oldValue, pan);
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

    @Override
    public boolean isBackgroundColorDark() {
        return org.gephi.viz.engine.util.ColorUtils.isColorDark(backgroundColor.getRGBComponents(null));
    }

    private void setBackgroundColor(float[] bgColor) {
        Color color = new Color(bgColor[0], bgColor[1], bgColor[2], bgColor[3]);
        setBackgroundColor(color);
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
                options.setEdgeColorMode(GraphRenderingOptions.EdgeColorMode.valueOf(edgeColorMode.name()));
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

    public Estimator getEdgeWeightEstimator() {
        if (AttributeUtils.isDynamicType(graphModel.getConfiguration().getEdgeWeightType())) {
            return graphModel.getEdgeTable().getColumn("weight").getEstimator();
        }
        return null;
    }

    public void setEdgeWeightEstimator(Estimator estimator) {
        if (AttributeUtils.isDynamicType(graphModel.getConfiguration().getEdgeWeightType())) {
            Estimator oldValue = graphModel.getEdgeTable().getColumn("weight").getEstimator();
            graphModel.getEdgeTable().getColumn("weight").setEstimator(estimator);
            firePropertyChange("edgeWeightEstimator", oldValue, estimator);
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

    @Override
    public boolean isRescaleEdgeWeight() {
        return edgeRescaleWeightEnabled;
    }

    public void setEdgeRescaleWeightEnabled(boolean edgeRescaleWeightEnabled) {
        boolean oldValue = this.edgeRescaleWeightEnabled;
        if (oldValue != edgeRescaleWeightEnabled) {
            this.edgeRescaleWeightEnabled = edgeRescaleWeightEnabled;
            getRenderingOptions().ifPresent(options -> options.setEdgeRescaleWeightEnabled(edgeRescaleWeightEnabled));
            firePropertyChange("edgeRescaleWeightEnabled", oldValue, edgeRescaleWeightEnabled);
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
            getRenderingOptions().ifPresent(options -> options.setNodeLabelColorMode(
                GraphRenderingOptions.LabelColorMode.valueOf(nodeLabelColorMode.name())));
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
            getRenderingOptions().ifPresent(options -> options.setNodeLabelSizeMode(
                GraphRenderingOptions.LabelSizeMode.valueOf(nodeLabelSizeMode.name()))
            );
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
            getRenderingOptions().ifPresent(options -> options.setNodeLabelFont(nodeLabelFont));
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
            getRenderingOptions().ifPresent(options -> options.setEdgeLabelFont(edgeLabelFont));
            firePropertyChange("edgeLabelFont", oldValue, edgeLabelFont);
        }
    }

    @Override
    public LabelColorMode getEdgeLabelColorMode() {
        return edgeLabelColorMode;
    }

    public void setEdgeLabelColorMode(LabelColorMode edgeLabelColorMode) {
        LabelColorMode oldValue = this.edgeLabelColorMode;
        if (oldValue != edgeLabelColorMode) {
            this.edgeLabelColorMode = edgeLabelColorMode;
            getRenderingOptions().ifPresent(options -> options.setEdgeLabelColorMode(
                GraphRenderingOptions.LabelColorMode.valueOf(edgeLabelColorMode.name())));
            firePropertyChange("edgeLabelColorMode", oldValue, edgeLabelColorMode);
        }
    }

    @Override
    public LabelSizeMode getEdgeLabelSizeMode() {
        return edgeLabelSizeMode;
    }

    public void setEdgeLabelSizeMode(LabelSizeMode edgeLabelSizeMode) {
        LabelSizeMode oldValue = this.edgeLabelSizeMode;
        if (oldValue != edgeLabelSizeMode) {
            this.edgeLabelSizeMode = edgeLabelSizeMode;
            getRenderingOptions().ifPresent(options -> options.setEdgeLabelSizeMode(
                GraphRenderingOptions.LabelSizeMode.valueOf(edgeLabelSizeMode.name()))
            );
            firePropertyChange("edgeLabelSizeMode", oldValue, edgeLabelSizeMode);
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
            getRenderingOptions().ifPresent(options -> options.setNodeLabelScale(nodeLabelScale));
            firePropertyChange("nodeLabelScale", oldValue, nodeLabelScale);
        }
    }

    @Override
    public float getEdgeLabelScale() {
        return edgeLabelScale;
    }

    public void setEdgeLabelScale(float edgeLabelScale) {
        float oldValue = this.edgeLabelScale;
        if (oldValue != edgeLabelScale) {
            this.edgeLabelScale = edgeLabelScale;
            getRenderingOptions().ifPresent(options -> options.setEdgeLabelScale(edgeLabelScale));
            firePropertyChange("edgeLabelScale", oldValue, edgeLabelScale);
        }
    }

    @Override
    public boolean isHideNonSelectedNodeLabels() {
        return hideNonSelectedNodeLabels;
    }

    public void setHideNonSelectedNodeLabels(boolean hideNonSelectedNodeLabels) {
        boolean oldValue = this.hideNonSelectedNodeLabels;
        if (oldValue != hideNonSelectedNodeLabels) {
            this.hideNonSelectedNodeLabels = hideNonSelectedNodeLabels;
            getRenderingOptions().ifPresent(options -> options.setHideNonSelectedNodeLabels(hideNonSelectedNodeLabels));
            firePropertyChange("hideNonSelectedNodeLabels", oldValue, hideNonSelectedNodeLabels);
        }
    }

    @Override
    public boolean isHideNonSelectedEdgeLabels() {
        return hideNonSelectedEdgeLabels;
    }

    public void setHideNonSelectedEdgeLabels(boolean hideNonSelectedEdgeLabels) {
        boolean oldValue = this.hideNonSelectedEdgeLabels;
        if (oldValue != hideNonSelectedEdgeLabels) {
            this.hideNonSelectedEdgeLabels = hideNonSelectedEdgeLabels;
            getRenderingOptions().ifPresent(options -> options.setHideNonSelectedEdgeLabels(hideNonSelectedEdgeLabels));
            firePropertyChange("hideNonSelectedEdgeLabels", oldValue, hideNonSelectedEdgeLabels);
        }
    }

    @Override
    public boolean isNodeLabelFitToNodeSize() {
        return fitNodeLabelsToNodeSize;
    }

    public void setNodeLabelFitToNodeSize(boolean fitNodeLabelsToNodeSize) {
        boolean oldValue = this.fitNodeLabelsToNodeSize;
        if (oldValue != fitNodeLabelsToNodeSize) {
            this.fitNodeLabelsToNodeSize = fitNodeLabelsToNodeSize;
            getRenderingOptions().ifPresent(options -> options.setNodeLabelFitToNodeSize(fitNodeLabelsToNodeSize));
            firePropertyChange("nodeLabelFitToNodeSize", oldValue, fitNodeLabelsToNodeSize);
        }
    }

    @Override
    public boolean isAvoidNodeLabelOverlap() {
        return avoidNodeLabelOverlap;
    }

    public void setAvoidNodeLabelOverlap(boolean avoidNodeLabelOverlap) {
        boolean oldValue = this.avoidNodeLabelOverlap;
        if (oldValue != avoidNodeLabelOverlap) {
            this.avoidNodeLabelOverlap = avoidNodeLabelOverlap;
            getRenderingOptions().ifPresent(options -> options.setAvoidNodeLabelOverlap(avoidNodeLabelOverlap));
            firePropertyChange("avoidNodeLabelOverlap", oldValue, avoidNodeLabelOverlap);
        }
    }

    @Override
    public Column[] getNodeLabelColumns() {
        return nodeLabelColumns;
    }

    @Override
    public String getNodeLabel(Node node, GraphView view) {
        return TextLabelBuilder.buildText(node, view, getNodeLabelColumns());
    }

    @Override
    public String getEdgeLabel(Edge edge, GraphView view) {
        return TextLabelBuilder.buildText(edge, view, getEdgeLabelColumns());
    }

    public void setNodeLabelColumns(Column[] nodeLabelColumns) {
        Column[] oldValue = this.nodeLabelColumns;
        if (oldValue != nodeLabelColumns) {
            this.nodeLabelColumns = nodeLabelColumns;
            getRenderingOptions().ifPresent(options -> options.setNodeLabelColumns(nodeLabelColumns));
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
            getRenderingOptions().ifPresent(options -> options.setEdgeLabelColumns(edgeLabelColumns));
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
    public Collection<Node> getSelectedNodes() {
        return selectionModel.getSelectedNodes();
    }

    //XML
    public void readXML(XMLStreamReader reader, Workspace workspace) throws XMLStreamException {
        boolean end = false;
        while (reader.hasNext() && !end) {
            int type = reader.next();

            switch (type) {
                case XMLStreamReader.START_ELEMENT:
                    String name = reader.getLocalName();
                    // Sub-models
                    if ("screenshotModel".equalsIgnoreCase(name)) {
                        screenshotModel.readXML(reader);
                    } else if ("selectionModel".equalsIgnoreCase(name)) {
                        selectionModel.readXML(reader);
                    // Legacy: old TextModelImpl persisted under <textmodel> inside <vizmodel>
                    } else if ("textmodel".equalsIgnoreCase(name)) {
                        readLegacyTextModel(reader);
                    // Legacy: old <screenshotMaker> was a self-closing element with inline attributes
                    } else if ("screenshotMaker".equalsIgnoreCase(name)) {
                        readLegacyScreenshotMaker(reader);
                    // Global
                    } else if ("cameraposition".equalsIgnoreCase(name)) {
                        String x = reader.getAttributeValue(null, "x");
                        String y = reader.getAttributeValue(null, "y");
                        if (x != null && y != null) {
                            this.pan = new Vector2f(Float.parseFloat(x), Float.parseFloat(y));
                        }
                    } else if ("zoom".equalsIgnoreCase(name)) {
                        this.zoom = Float.parseFloat(reader.getAttributeValue(null, "value"));
                    // Edges
                    } else if ("showedges".equalsIgnoreCase(name)) {
                        setShowEdges(Boolean.parseBoolean(reader.getAttributeValue(null, "value")));
                    } else if ("edgeScale".equalsIgnoreCase(name)) {
                        setEdgeScale(Float.parseFloat(reader.getAttributeValue(null, "value")));
                    } else if ("nodeScale".equalsIgnoreCase(name)) {
                        setNodeScale(Float.parseFloat(reader.getAttributeValue(null, "value")));
                    } else if ("edgeColorMode".equalsIgnoreCase(name)) {
                        String v = reader.getAttributeValue(null, "value");
                        if (v != null) {
                            try {
                                setEdgeColorMode(EdgeColorMode.valueOf(v));
                            } catch (IllegalArgumentException ignored) {
                            }
                        }
                    } else if ("edgeWeightEnabled".equalsIgnoreCase(name)) {
                        setUseEdgeWeight(Boolean.parseBoolean(reader.getAttributeValue(null, "value")));
                    } else if ("edgeRescaleWeightEnabled".equalsIgnoreCase(name)) {
                        setEdgeRescaleWeightEnabled(Boolean.parseBoolean(reader.getAttributeValue(null, "value")));
                    // Selection
                    } else if ("autoselectneighbor".equalsIgnoreCase(name)) {
                        setAutoSelectNeighbors(Boolean.parseBoolean(reader.getAttributeValue(null, "value")));
                    } else if ("hidenonselectededges".equalsIgnoreCase(name)) {
                        setHideNonSelectedEdges(Boolean.parseBoolean(reader.getAttributeValue(null, "value")));
                    } else if ("lightennonselectedauto".equalsIgnoreCase(name)) {
                        setLightenNonSelectedAuto(Boolean.parseBoolean(reader.getAttributeValue(null, "value")));
                    } else if ("lightenNonSelectedFactor".equalsIgnoreCase(name)) {
                        setLightenNonSelectedFactor(Float.parseFloat(reader.getAttributeValue(null, "value")));
                    } else if ("edgeSelectionColor".equalsIgnoreCase(name)) {
                        setEdgeSelectionColor(Boolean.parseBoolean(reader.getAttributeValue(null, "value")));
                    // Colors
                    } else if ("backgroundcolor".equalsIgnoreCase(name)) {
                        setBackgroundColor(ColorUtils.decode(reader.getAttributeValue(null, "value")));
                    } else if ("edgeInSelectionColor".equalsIgnoreCase(name)) {
                        setEdgeInSelectionColor(ColorUtils.decode(reader.getAttributeValue(null, "value")));
                    } else if ("edgeOutSelectionColor".equalsIgnoreCase(name)) {
                        setEdgeOutSelectionColor(ColorUtils.decode(reader.getAttributeValue(null, "value")));
                    } else if ("edgeBothSelectionColor".equalsIgnoreCase(name)) {
                        setEdgeBothSelectionColor(ColorUtils.decode(reader.getAttributeValue(null, "value")));
                    // Node Labels
                    } else if ("showNodeLabels".equalsIgnoreCase(name)) {
                        setShowNodeLabels(Boolean.parseBoolean(reader.getAttributeValue(null, "value")));
                    } else if ("nodeLabelFont".equalsIgnoreCase(name)) {
                        String v = reader.getAttributeValue(null, "value");
                        if (v != null) {
                            setNodeLabelFont(Font.decode(v));
                        }
                    } else if ("nodeLabelScale".equalsIgnoreCase(name)) {
                        setNodeLabelScale(Float.parseFloat(reader.getAttributeValue(null, "value")));
                    } else if ("nodeLabelColorMode".equalsIgnoreCase(name)) {
                        String v = reader.getAttributeValue(null, "value");
                        if (v != null) {
                            try {
                                setNodeLabelColorMode(LabelColorMode.valueOf(v));
                            } catch (IllegalArgumentException ignored) {
                            }
                        }
                    } else if ("nodeLabelSizeMode".equalsIgnoreCase(name)) {
                        String v = reader.getAttributeValue(null, "value");
                        if (v != null) {
                            try {
                                setNodeLabelSizeMode(LabelSizeMode.valueOf(v));
                            } catch (IllegalArgumentException ignored) {
                            }
                        }
                    } else if ("hideNonSelectedNodeLabels".equalsIgnoreCase(name)) {
                        setHideNonSelectedNodeLabels(Boolean.parseBoolean(reader.getAttributeValue(null, "value")));
                    } else if ("fitNodeLabelsToNodeSize".equalsIgnoreCase(name)) {
                        setNodeLabelFitToNodeSize(Boolean.parseBoolean(reader.getAttributeValue(null, "value")));
                    } else if ("avoidNodeLabelOverlap".equalsIgnoreCase(name)) {
                        setAvoidNodeLabelOverlap(Boolean.parseBoolean(reader.getAttributeValue(null, "value")));
                    } else if ("nodeLabelColumns".equalsIgnoreCase(name)) {
                        Column[] cols = readLabelColumns(reader, "nodeLabelColumns", graphModel.getNodeTable());
                        if (cols != null) {
                            setNodeLabelColumns(cols);
                        }
                    // Edge Labels
                    } else if ("showEdgeLabels".equalsIgnoreCase(name)) {
                        setShowEdgeLabels(Boolean.parseBoolean(reader.getAttributeValue(null, "value")));
                    } else if ("edgeLabelFont".equalsIgnoreCase(name)) {
                        String v = reader.getAttributeValue(null, "value");
                        if (v != null) {
                            setEdgeLabelFont(Font.decode(v));
                        }
                    } else if ("edgeLabelScale".equalsIgnoreCase(name)) {
                        setEdgeLabelScale(Float.parseFloat(reader.getAttributeValue(null, "value")));
                    } else if ("edgeLabelColorMode".equalsIgnoreCase(name)) {
                        String v = reader.getAttributeValue(null, "value");
                        if (v != null) {
                            try {
                                setEdgeLabelColorMode(LabelColorMode.valueOf(v));
                            } catch (IllegalArgumentException ignored) {
                            }
                        }
                    } else if ("edgeLabelSizeMode".equalsIgnoreCase(name)) {
                        String v = reader.getAttributeValue(null, "value");
                        if (v != null) {
                            try {
                                setEdgeLabelSizeMode(LabelSizeMode.valueOf(v));
                            } catch (IllegalArgumentException ignored) {
                            }
                        }
                    } else if ("hideNonSelectedEdgeLabels".equalsIgnoreCase(name)) {
                        setHideNonSelectedEdgeLabels(Boolean.parseBoolean(reader.getAttributeValue(null, "value")));
                    } else if ("edgeLabelColumns".equalsIgnoreCase(name)) {
                        Column[] cols = readLabelColumns(reader, "edgeLabelColumns", graphModel.getEdgeTable());
                        if (cols != null) {
                            setEdgeLabelColumns(cols);
                        }
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

    /**
     * Reads the legacy {@code <textmodel>} element written by Gephi 0.10 and earlier, mapping its
     * content onto the equivalent fields of the current model.
     */
    private void readLegacyTextModel(XMLStreamReader reader) throws XMLStreamException {
        List<Column> nodeCols = new ArrayList<>();
        List<Column> edgeCols = new ArrayList<>();
        boolean inNodeColumns = false;
        boolean inEdgeColumns = false;
        boolean readNodeSizeFactor = false;
        boolean readEdgeSizeFactor = false;
        boolean end = false;

        while (reader.hasNext() && !end) {
            int type = reader.next();
            switch (type) {
                case XMLStreamReader.START_ELEMENT:
                    String name = reader.getLocalName();
                    if ("shownodelabels".equalsIgnoreCase(name)) {
                        setShowNodeLabels(Boolean.parseBoolean(reader.getAttributeValue(null, "enable")));
                    } else if ("showedgelabels".equalsIgnoreCase(name)) {
                        setShowEdgeLabels(Boolean.parseBoolean(reader.getAttributeValue(null, "enable")));
                    } else if ("selectedOnly".equalsIgnoreCase(name)) {
                        // Old "show selected labels only" maps to hiding non-selected labels
                        boolean selectedOnly = Boolean.parseBoolean(reader.getAttributeValue(null, "value"));
                        setHideNonSelectedNodeLabels(selectedOnly);
                        setHideNonSelectedEdgeLabels(selectedOnly);
                    } else if ("nodefont".equalsIgnoreCase(name)) {
                        // Old format stored font as separate name/size/style attributes
                        String fontName = reader.getAttributeValue(null, "name");
                        int fontSize = Integer.parseInt(reader.getAttributeValue(null, "size"));
                        int fontStyle = Integer.parseInt(reader.getAttributeValue(null, "style"));
                        setNodeLabelFont(new Font(fontName, fontStyle, fontSize));
                    } else if ("edgefont".equalsIgnoreCase(name)) {
                        String fontName = reader.getAttributeValue(null, "name");
                        int fontSize = Integer.parseInt(reader.getAttributeValue(null, "size"));
                        int fontStyle = Integer.parseInt(reader.getAttributeValue(null, "style"));
                        setEdgeLabelFont(new Font(fontName, fontStyle, fontSize));
                    } else if ("nodesizefactor".equalsIgnoreCase(name)) {
                        readNodeSizeFactor = true;
                    } else if ("edgesizefactor".equalsIgnoreCase(name)) {
                        readEdgeSizeFactor = true;
                    } else if ("colormode".equalsIgnoreCase(name)) {
                        // ObjectColorMode → OBJECT; everything else (TextColorMode, UniqueColorMode) → SELF
                        String cls = reader.getAttributeValue(null, "class");
                        LabelColorMode colorMode =
                            "ObjectColorMode".equals(cls) ? LabelColorMode.OBJECT : LabelColorMode.SELF;
                        setNodeLabelColorMode(colorMode);
                        setEdgeLabelColorMode(colorMode);
                    } else if ("sizemode".equalsIgnoreCase(name)) {
                        // FixedSizeMode → SCREEN (constant pixels); everything else → ZOOM
                        String cls = reader.getAttributeValue(null, "class");
                        LabelSizeMode sizeMode =
                            "FixedSizeMode".equals(cls) ? LabelSizeMode.SCREEN : LabelSizeMode.ZOOM;
                        setNodeLabelSizeMode(sizeMode);
                        setEdgeLabelSizeMode(sizeMode);
                    } else if ("nodecolumns".equalsIgnoreCase(name)) {
                        inNodeColumns = true;
                    } else if ("edgecolumns".equalsIgnoreCase(name)) {
                        inEdgeColumns = true;
                    } else if ("column".equalsIgnoreCase(name)) {
                        String id = reader.getAttributeValue(null, "id");
                        if (id != null) {
                            if (inNodeColumns) {
                                Column col = graphModel.getNodeTable().getColumn(id);
                                if (col != null) {
                                    nodeCols.add(col);
                                }
                            } else if (inEdgeColumns) {
                                Column col = graphModel.getEdgeTable().getColumn(id);
                                if (col != null) {
                                    edgeCols.add(col);
                                }
                            }
                        }
                    }
                    // nodecolor/edgecolor: no equivalent in new model, intentionally skipped
                    break;
                case XMLStreamReader.CHARACTERS:
                    // nodesizefactor and edgesizefactor used text content in old format
                    if (!reader.isWhiteSpace()) {
                        if (readNodeSizeFactor) {
                            setNodeLabelScale(Float.parseFloat(reader.getText()));
                        } else if (readEdgeSizeFactor) {
                            setEdgeLabelScale(Float.parseFloat(reader.getText()));
                        }
                    }
                    break;
                case XMLStreamReader.END_ELEMENT:
                    readNodeSizeFactor = false;
                    readEdgeSizeFactor = false;
                    if ("nodecolumns".equalsIgnoreCase(reader.getLocalName())) {
                        inNodeColumns = false;
                    } else if ("edgecolumns".equalsIgnoreCase(reader.getLocalName())) {
                        inEdgeColumns = false;
                    } else if ("textmodel".equalsIgnoreCase(reader.getLocalName())) {
                        end = true;
                    }
                    break;
            }
        }

        if (!nodeCols.isEmpty()) {
            setNodeLabelColumns(nodeCols.toArray(new Column[0]));
        }
        if (!edgeCols.isEmpty()) {
            setEdgeLabelColumns(edgeCols.toArray(new Column[0]));
        }
    }

    /**
     * Reads the legacy {@code <screenshotMaker>} element written by Gephi 0.10 and earlier.
     * The old element was self-closing with all data as inline attributes. The old {@code width},
     * {@code height} and {@code antialiasing} attributes have no equivalent in the new model and
     * are intentionally ignored.
     */
    private void readLegacyScreenshotMaker(XMLStreamReader reader) {
        String transparent = reader.getAttributeValue(null, "transparent");
        if (transparent != null) {
            screenshotModel.setTransparentBackground(Boolean.parseBoolean(transparent));
        }
        String autoSave = reader.getAttributeValue(null, "autosave");
        if (autoSave != null) {
            screenshotModel.setAutoSave(Boolean.parseBoolean(autoSave));
        }
        String path = reader.getAttributeValue(null, "path");
        if (path != null && !path.isEmpty()) {
            screenshotModel.setDefaultDirectory(new java.io.File(path));
        }
    }

    private Column[] readLabelColumns(XMLStreamReader reader, String endElement,
                                      org.gephi.graph.api.Table table) throws XMLStreamException {
        List<Column> cols = new ArrayList<>();
        while (reader.hasNext()) {
            int type = reader.next();
            if (type == XMLStreamReader.START_ELEMENT && "column".equalsIgnoreCase(reader.getLocalName())) {
                String id = reader.getAttributeValue(null, "id");
                if (id != null) {
                    Column col = table.getColumn(id);
                    if (col != null) {
                        cols.add(col);
                    }
                }
            } else if (type == XMLStreamReader.END_ELEMENT &&
                endElement.equalsIgnoreCase(reader.getLocalName())) {
                break;
            }
        }
        return cols.isEmpty() ? null : cols.toArray(new Column[0]);
    }

    public void writeXML(XMLStreamWriter writer) throws XMLStreamException {
        // Global
        writer.writeStartElement("cameraposition");
        writer.writeAttribute("x", Float.toString(pan.x()));
        writer.writeAttribute("y", Float.toString(pan.y()));
        writer.writeAttribute("z", Float.toString(5000f)); // Keep for backward compatibility, not used anymore
        writer.writeEndElement();

        writer.writeStartElement("zoom");
        writer.writeAttribute("value", String.valueOf(zoom));
        writer.writeEndElement();

        // Edges
        writer.writeStartElement("showedges");
        writer.writeAttribute("value", String.valueOf(isShowEdges()));
        writer.writeEndElement();

        writer.writeStartElement("edgeScale");
        writer.writeAttribute("value", String.valueOf(getEdgeScale()));
        writer.writeEndElement();

        writer.writeStartElement("nodeScale");
        writer.writeAttribute("value", String.valueOf(getNodeScale()));
        writer.writeEndElement();

        writer.writeStartElement("edgeColorMode");
        writer.writeAttribute("value", getEdgeColorMode().name());
        writer.writeEndElement();

        writer.writeStartElement("edgeWeightEnabled");
        writer.writeAttribute("value", String.valueOf(isUseEdgeWeight()));
        writer.writeEndElement();

        writer.writeStartElement("edgeRescaleWeightEnabled");
        writer.writeAttribute("value", String.valueOf(isRescaleEdgeWeight()));
        writer.writeEndElement();

        // Selection
        writer.writeStartElement("autoselectneighbor");
        writer.writeAttribute("value", String.valueOf(isAutoSelectNeighbors()));
        writer.writeEndElement();

        writer.writeStartElement("hidenonselectededges");
        writer.writeAttribute("value", String.valueOf(isHideNonSelectedEdges()));
        writer.writeEndElement();

        writer.writeStartElement("lightennonselectedauto");
        writer.writeAttribute("value", String.valueOf(isLightenNonSelectedAuto()));
        writer.writeEndElement();

        writer.writeStartElement("lightenNonSelectedFactor");
        writer.writeAttribute("value", String.valueOf(getLightenNonSelectedFactor()));
        writer.writeEndElement();

        writer.writeStartElement("edgeSelectionColor");
        writer.writeAttribute("value", String.valueOf(isEdgeSelectionColor()));
        writer.writeEndElement();

        // Colors
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

        // Node Labels
        writer.writeStartElement("showNodeLabels");
        writer.writeAttribute("value", String.valueOf(isShowNodeLabels()));
        writer.writeEndElement();

        writer.writeStartElement("nodeLabelFont");
        writer.writeAttribute("value", FontUtils.encode(getNodeLabelFont()));
        writer.writeEndElement();

        writer.writeStartElement("nodeLabelScale");
        writer.writeAttribute("value", String.valueOf(getNodeLabelScale()));
        writer.writeEndElement();

        writer.writeStartElement("nodeLabelColorMode");
        writer.writeAttribute("value", getNodeLabelColorMode().name());
        writer.writeEndElement();

        writer.writeStartElement("nodeLabelSizeMode");
        writer.writeAttribute("value", getNodeLabelSizeMode().name());
        writer.writeEndElement();

        writer.writeStartElement("hideNonSelectedNodeLabels");
        writer.writeAttribute("value", String.valueOf(isHideNonSelectedNodeLabels()));
        writer.writeEndElement();

        writer.writeStartElement("fitNodeLabelsToNodeSize");
        writer.writeAttribute("value", String.valueOf(isNodeLabelFitToNodeSize()));
        writer.writeEndElement();

        writer.writeStartElement("avoidNodeLabelOverlap");
        writer.writeAttribute("value", String.valueOf(isAvoidNodeLabelOverlap()));
        writer.writeEndElement();

        writer.writeStartElement("nodeLabelColumns");
        for (Column col : getNodeLabelColumns()) {
            writer.writeStartElement("column");
            writer.writeAttribute("id", col.getId());
            writer.writeEndElement();
        }
        writer.writeEndElement();

        // Edge Labels
        writer.writeStartElement("showEdgeLabels");
        writer.writeAttribute("value", String.valueOf(isShowEdgeLabels()));
        writer.writeEndElement();

        writer.writeStartElement("edgeLabelFont");
        writer.writeAttribute("value", FontUtils.encode(getEdgeLabelFont()));
        writer.writeEndElement();

        writer.writeStartElement("edgeLabelScale");
        writer.writeAttribute("value", String.valueOf(getEdgeLabelScale()));
        writer.writeEndElement();

        writer.writeStartElement("edgeLabelColorMode");
        writer.writeAttribute("value", getEdgeLabelColorMode().name());
        writer.writeEndElement();

        writer.writeStartElement("edgeLabelSizeMode");
        writer.writeAttribute("value", getEdgeLabelSizeMode().name());
        writer.writeEndElement();

        writer.writeStartElement("hideNonSelectedEdgeLabels");
        writer.writeAttribute("value", String.valueOf(isHideNonSelectedEdgeLabels()));
        writer.writeEndElement();

        writer.writeStartElement("edgeLabelColumns");
        for (Column col : getEdgeLabelColumns()) {
            writer.writeStartElement("column");
            writer.writeAttribute("id", col.getId());
            writer.writeEndElement();
        }
        writer.writeEndElement();

        // Screenshot model
        writer.writeStartElement("screenshotModel");
        screenshotModel.writeXML(writer);
        writer.writeEndElement();

        // Selection model
        writer.writeStartElement("selectionModel");
        selectionModel.writeXML(writer);
        writer.writeEndElement();
    }
}
