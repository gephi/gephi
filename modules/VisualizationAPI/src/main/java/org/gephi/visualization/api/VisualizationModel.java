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

package org.gephi.visualization.api;

import java.awt.Color;
import java.awt.Font;
import java.util.Collection;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Estimator;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Node;
import org.gephi.project.spi.Model;

/**
 * Entry point to access and configure visualization settings.
 * <p>
 * It also includes selection-related methods like {@link #getSelectedNodes()}.
 * <p>
 * One model exists for each workspace.
 *
 * @author Mathieu Bastian
 */
public interface VisualizationModel extends Model {

    /**
     * Returns the screenshot model.
     *
     * @return the screenshot model
     */
    ScreenshotModel getScreenshotModel();

    /**
     * Returns the current zoom level.
     * <p>
     * Default value is <code>0.3</code>.
     *
     * @return the zoom level
     */
    float getZoom();

    /**
     * Returns the current frames per second.
     *
     * @return the FPS
     */
    int getFps();

    /**
     * Returns whether neighbors are automatically selected when a node is selected.
     * <p>
     * Default value is <code>true</code>.
     *
     * @return <code>true</code> if auto-select neighbors is enabled, <code>false</code> otherwise
     */
    boolean isAutoSelectNeighbors();

    /**
     * Returns the background color of the visualization canvas.
     * <p>
     * Default value is <code>Color.WHITE</code> for light themes and <code>Color.DARK_GRAY</code> for dark themes.
     *
     * @return the background color
     */
    Color getBackgroundColor();

    /**
     * Returns whether the background color is dark.
     * <p>
     * Default value is <code>false</code> for light themes and <code>true</code> for dark themes.
     * <p>
     * The value is determined based on the luminance of the background color.
     *
     * @return <code>true</code> if background is dark, <code>false</code> otherwise
     */
    boolean isBackgroundColorDark();

    /**
     * Returns whether non-selected elements are automatically lightened.
     * <p>
     * Default value is <code>true</code>.
     *
     * @return <code>true</code> if lightening is enabled, <code>false</code> otherwise
     */
    boolean isLightenNonSelectedAuto();

    /**
     * Returns the node size scaling factor.
     * <p>
     * Default value is <code>1.0</code>.
     *
     * @return the node scale
     */
    float getNodeScale();

    // Edges

    /**
     * Returns whether edges are visible.
     * <p>
     * Default value is <code>true</code>.
     *
     * @return <code>true</code> if edges are shown, <code>false</code> otherwise
     */
    boolean isShowEdges();

    /**
     * Returns the edge color mode.
     * <p>
     * Default value is <code>EdgeColorMode.SOURCE</code>.
     *
     * @return the edge color mode
     */
    EdgeColorMode getEdgeColorMode();

    /**
     * Returns whether non-selected edges are hidden.
     * <p>
     * Default value is <code>false</code>.
     *
     * @return <code>true</code> if non-selected edges are hidden, <code>false</code> otherwise
     */
    boolean isHideNonSelectedEdges();

    /**
     * Returns whether selected edges use custom selection colors.
     * <p>
     * Default value is <code>false</code>.
     *
     * @return <code>true</code> if selection colors are enabled, <code>false</code> otherwise
     */
    boolean isEdgeSelectionColor();

    /**
     * Returns the color for selected incoming edges.
     * <p>
     * Default value is <code>new Color(32, 95, 154, 255)</code>.
     *
     * @return the incoming edge selection color
     */
    Color getEdgeInSelectionColor();

    /**
     * Returns the color for selected outgoing edges.
     * <p>
     * Default value is <code>new Color(196, 66, 79, 255)</code>.
     *
     * @return the outgoing edge selection color
     */
    Color getEdgeOutSelectionColor();

    /**
     * Returns the color for selected bidirectional edges.
     * <p>
     * Default value is <code>new Color(248, 215, 83, 255)</code>.
     *
     * @return the bidirectional edge selection color
     */
    Color getEdgeBothSelectionColor();

    /**
     * Returns the edge thickness scaling factor.
     * <p>
     * Default value is <code>2.0</code>.
     *
     * @return the edge scale
     */
    float getEdgeScale();

    /**
     * Returns whether edge weights affect edge thickness.
     * <p>
     * Default value is <code>true</code>.
     *
     * @return <code>true</code> if edge weight is used, <code>false</code> otherwise
     */
    boolean isUseEdgeWeight();

    /**
     * Returns whether edge weight rescaling is enabled.
     * <p>
     * Default value is <code>true</code>.
     *
     * @return <code>true</code> if edge weight rescaling is enabled, <code>false</code> otherwise
     */
    boolean isRescaleEdgeWeight();

    /**
     * Returns the estimator used for dynamic edge weights.
     *
     * @return the edge weight estimator or <code>null</code> if not applicable
     */
    Estimator getEdgeWeightEstimator();

    // Selection

    /**
     * Returns the currently selected nodes.
     *
     * @return the selected nodes
     */
    Collection<Node> getSelectedNodes();

    /**
     * Returns the mouse selection diameter in pixels.
     * <p>
     * Default value is <code>1</code>.
     *
     * @return the selection diameter
     */
    int getMouseSelectionDiameter();

    /**
     * Returns whether the mouse selection diameter scales with zoom.
     * <p>
     * Default value is <code>false</code>.
     *
     * @return <code>true</code> if zoom proportional, <code>false</code> otherwise
     */
    boolean isMouseSelectionZoomProportional();

    /**
     * Returns whether rectangle selection mode is active.
     *
     * @return <code>true</code> if rectangle selection is active, <code>false</code> otherwise
     */
    boolean isRectangleSelection();

    /**
     * Returns whether direct mouse selection mode is active.
     *
     * @return <code>true</code> if direct mouse selection is active, <code>false</code> otherwise
     */
    boolean isDirectMouseSelection();

    /**
     * Returns whether custom selection mode is active.
     *
     * @return <code>true</code> if custom selection is active, <code>false</code> otherwise
     */
    boolean isCustomSelection();

    /**
     * Returns whether any selection mode is enabled.
     *
     * @return <code>true</code> if selection is enabled, <code>false</code> otherwise
     */
    boolean isSelectionEnabled();

    /**
     * Returns whether node selection mode is active.
     *
     * @return <code>true</code> if node selection is active, <code>false</code> otherwise
     */
    boolean isNodeSelection();

    /**
     * Returns whether single node selection mode is active.
     *
     * @return <code>true</code> if single node selection is active, <code>false</code> otherwise
     */
    boolean isSingleNodeSelection();

    // Node Labels

    /**
     * Returns whether node labels are visible.
     * <p>
     * Default value is <code>false</code>.
     *
     * @return <code>true</code> if node labels are shown, <code>false</code> otherwise
     */
    boolean isShowNodeLabels();

    /**
     * Returns the node label color mode.
     * <p>
     * Default value is <code>LabelColorMode.SELF</code>.
     *
     * @return the node label color mode
     */
    LabelColorMode getNodeLabelColorMode();

    /**
     * Returns the node label size mode.
     * <p>
     * Default value is <code>LabelSizeMode.ZOOM</code>.
     *
     * @return the node label size mode
     */
    LabelSizeMode getNodeLabelSizeMode();

    /**
     * Returns the font used for node labels.
     * <p>
     * Default value is Arial Bold 32.
     *
     * @return the node label font
     */
    Font getNodeLabelFont();

    /**
     * Returns whether node labels are constrained to fit within node size.
     * <p>
     * Default value is <code>false</code>.
     *
     * @return <code>true</code> if fit to node size is enabled, <code>false</code> otherwise
     */
    boolean isNodeLabelFitToNodeSize();

    /**
     * Returns the node label size scaling factor.
     * <p>
     * Default value is <code>0.5</code>.
     *
     * @return the node label scale
     */
    float getNodeLabelScale();

    /**
     * Returns whether non-selected node labels are hidden.
     * <p>
     * Default value is <code>false</code>.
     *
     * @return <code>true</code> if non-selected labels are hidden, <code>false</code> otherwise
     */
    boolean isHideNonSelectedNodeLabels();

    /**
     * Returns whether node label overlap avoidance is enabled.
     * <p>
     * Default value is <code>true</code>.
     *
     * @return <code>true</code> if overlap avoidance is enabled, <code>false</code> otherwise
     */
    boolean isAvoidNodeLabelOverlap();

    /**
     * Returns the columns used to generate node labels.
     * <p>
     * Default value is the node label column.
     *
     * @return the node label columns
     */
    Column[] getNodeLabelColumns();

    /**
     * Returns the label for the given node, based on {@link #getNodeLabelColumns()}.
     *
     * @param node the node
     * @param view the graph view
     * @return the node label
     */
    String getNodeLabel(Node node, GraphView view);

    /**
     * Returns the label for the given edge, based on {@link #getEdgeLabelColumns()}.
     *
     * @param edge the edge
     * @param view the graph view
     * @return the edge label
     */
    String getEdgeLabel(Edge edge, GraphView view);

    // Edge Labels

    /**
     * Returns whether edge labels are visible.
     * <p>
     * Default value is <code>false</code>.
     *
     * @return <code>true</code> if edge labels are shown, <code>false</code> otherwise
     */
    boolean isShowEdgeLabels();

    /**
     * Returns the edge label color mode.
     * <p>
     * Default value is <code>LabelColorMode.SELF</code>.
     *
     * @return the edge label color mode
     */
    LabelColorMode getEdgeLabelColorMode();

    /**
     * Returns the edge label size mode.
     * <p>
     * Default value is <code>LabelSizeMode.ZOOM</code>.
     *
     * @return the edge label size mode
     */
    LabelSizeMode getEdgeLabelSizeMode();

    /**
     * Returns the font used for edge labels.
     * <p>
     * Default value is Arial Bold 32.
     *
     * @return the edge label font
     */
    Font getEdgeLabelFont();

    /**
     * Returns the edge label size scaling factor.
     * <p>
     * Default value is <code>0.5</code>.
     *
     * @return the edge label scale
     */
    float getEdgeLabelScale();

    /**
     * Returns whether non-selected edge labels are hidden.
     * <p>
     * Default value is <code>false</code>.
     *
     * @return <code>true</code> if non-selected labels are hidden, <code>false</code> otherwise
     */
    boolean isHideNonSelectedEdgeLabels();

    /**
     * Returns the columns used to generate edge labels.
     * <p>
     * Default value is the edge label column.
     *
     * @return the edge label columns
     */
    Column[] getEdgeLabelColumns();
}