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
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Estimator;
import org.gephi.graph.api.Node;
import org.gephi.project.api.Workspace;

/**
 * Main controller for visualization settings and operations.
 * <p>
 * Provides access to the visualization model and methods to modify visualization settings.
 *
 * @author Mathieu Bastian
 */
public interface VisualizationController {

    /**
     * Returns the visualization model for the current workspace.
     *
     * @return the current visualization model
     */
    VisualizationModel getModel();

    /**
     * Returns the visualization model for the specified workspace.
     *
     * @param workspace the workspace
     * @return the visualization model for the workspace
     */
    VisualizationModel getModel(Workspace workspace);

    /**
     * Returns the screenshot controller.
     *
     * @return the screenshot controller
     */
    ScreenshotController getScreenshotController();

    /**
     * Sets the zoom level.
     *
     * @param zoom the zoom level
     */
    void setZoom(float zoom);

    /**
     * Sets whether neighbors are automatically selected when a node is selected.
     *
     * @param autoSelectNeighbors <code>true</code> to enable, <code>false</code> to disable
     */
    void setAutoSelectNeighbors(boolean autoSelectNeighbors);

    /**
     * Sets the background color of the visualization canvas.
     *
     * @param color the background color
     */
    void setBackgroundColor(Color color);

    /**
     * Sets the node size scaling factor.
     *
     * @param nodeScale the node scale
     */
    void setNodeScale(float nodeScale);

    /**
     * Sets whether edges are visible.
     *
     * @param showEdges <code>true</code> to show edges, <code>false</code> to hide them
     */
    void setShowEdges(boolean showEdges);

    /**
     * Sets the edge color mode.
     *
     * @param mode the edge color mode
     */
    void setEdgeColorMode(EdgeColorMode mode);

    /**
     * Sets whether selected edges use custom selection colors.
     *
     * @param edgeSelectionColor <code>true</code> to enable selection colors, <code>false</code> to disable
     */
    void setEdgeSelectionColor(boolean edgeSelectionColor);

    /**
     * Sets the color for selected incoming edges.
     *
     * @param edgeInSelectionColor the incoming edge selection color
     */
    void setEdgeInSelectionColor(Color edgeInSelectionColor);

    /**
     * Sets the color for selected outgoing edges.
     *
     * @param edgeOutSelectionColor the outgoing edge selection color
     */
    void setEdgeOutSelectionColor(Color edgeOutSelectionColor);

    /**
     * Sets the color for selected bidirectional edges.
     *
     * @param edgeBothSelectionColor the bidirectional edge selection color
     */
    void setEdgeBothSelectionColor(Color edgeBothSelectionColor);

    /**
     * Sets the edge thickness scaling factor.
     *
     * @param edgeScale the edge scale
     */
    void setEdgeScale(float edgeScale);

    /**
     * Sets whether edge weights affect edge thickness.
     *
     * @param useEdgeWeight <code>true</code> to enable, <code>false</code> to disable
     */
    void setUseEdgeWeight(boolean useEdgeWeight);

    /**
     * Sets whether edge weights are rescaled to fit within the specified min and max thickness.
     *
     * @param rescaleEdgeWeight <code>true</code> to enable, <code>false</code> to disable
     */
    void setRescaleEdgeWeight(boolean rescaleEdgeWeight);

    /**
     * Sets whether non-selected elements are automatically lightened.
     *
     * @param lightenNonSelectedAuto <code>true</code> to enable, <code>false</code> to disable
     */
    void setLightenNonSelectedAuto(boolean lightenNonSelectedAuto);

    /**
     * Sets whether non-selected edges are hidden.
     *
     * @param hideNonSelectedEdges <code>true</code> to hide, <code>false</code> to show
     */
    void setHideNonSelectedEdges(boolean hideNonSelectedEdges);

    /**
     * Sets the estimator used for dynamic edge weights.
     *
     * @param estimator the edge weight estimator
     */
    void setEdgeWeightEstimator(Estimator estimator);

    /**
     * Centers the view on the entire graph.
     */
    void centerOnGraph();

    /**
     * Centers the view on the origin (0, 0) with default dimensions.
     */
    void centerOnZero();

    /**
     * Centers the view on the specified rectangular area.
     *
     * @param x      the x coordinate
     * @param y      the y coordinate
     * @param width  the width
     * @param height the height
     */
    void centerOn(float x, float y, float width, float height);

    /**
     * Centers the view on the specified node.
     *
     * @param node the node to center on
     */
    void centerOnNode(Node node);

    /**
     * Centers the view on the specified edge.
     *
     * @param edge the edge to center on
     */
    void centerOnEdge(Edge edge);

    /**
     * Adds a property change listener.
     *
     * @param listener the listener to add
     */
    void addPropertyChangeListener(VisualizationPropertyChangeListener listener);

    /**
     * Removes a property change listener.
     *
     * @param listener the listener to remove
     */
    void removePropertyChangeListener(VisualizationPropertyChangeListener listener);

    /**
     * Adds a visualization event listener.
     *
     * @param listener the listener to add
     */
    void addListener(VisualizationEventListener listener);

    /**
     * Removes a visualization event listener.
     *
     * @param listener the listener to remove
     */
    void removeListener(VisualizationEventListener listener);

    // Selection

    /**
     * Disables all selection modes.
     */
    void disableSelection();

    /**
     * Enables rectangle selection mode.
     */
    void setRectangleSelection();

    /**
     * Enables direct mouse selection mode.
     */
    void setDirectMouseSelection();

    /**
     * Enables custom selection mode.
     */
    void setCustomSelection();

    /**
     * Enables node selection mode.
     *
     * @param singleNode <code>true</code> for single node selection, <code>false</code> for multiple
     */
    void setNodeSelection(boolean singleNode);

    /**
     * Sets the mouse selection diameter in pixels.
     *
     * @param diameter the selection diameter
     */
    void setMouseSelectionDiameter(int diameter);

    /**
     * Sets whether the mouse selection diameter scales with zoom.
     *
     * @param proportional <code>true</code> to enable zoom proportional, <code>false</code> otherwise
     */
    void setMouseSelectionZoomProportional(boolean proportional);

    /**
     * Resets the current selection.
     */
    void resetSelection();

    /**
     * Selects the specified nodes.
     *
     * @param nodes the nodes to select or <code>null</code> to clear node selection
     */
    void selectNodes(Node[] nodes);

    /**
     * Selects the specified edges.
     *
     * @param edges the edges to select or <code>null</code> to clear edge selection
     */
    void selectEdges(Edge[] edges);

    // Node Labels

    /**
     * Sets whether node labels are visible.
     *
     * @param showNodeLabels <code>true</code> to show node labels, <code>false</code> to hide them
     */
    void setShowNodeLabels(boolean showNodeLabels);

    /**
     * Sets the font used for node labels.
     *
     * @param font the node label font
     */
    void setNodeLabelFont(Font font);

    /**
     * Sets the node label size scaling factor.
     *
     * @param scale the node label scale
     */
    void setNodeLabelScale(float scale);

    /**
     * Sets whether non-selected node labels are hidden.
     *
     * @param hideNonSelected <code>true</code> to hide, <code>false</code> to show
     */
    void setHideNonSelectedNodeLabels(boolean hideNonSelected);

    /**
     * Sets the node label color mode.
     *
     * @param mode the node label color mode
     */
    void setNodeLabelColorMode(LabelColorMode mode);

    /**
     * Sets the node label size mode.
     *
     * @param mode the node label size mode
     */
    void setNodeLabelSizeMode(LabelSizeMode mode);

    /**
     * Sets the columns used to generate node labels.
     *
     * @param columns the node label columns
     */
    void setNodeLabelColumns(Column[] columns);

    /**
     * Sets whether node labels are constrained to fit within node size.
     *
     * @param fitToNodeSize <code>true</code> to enable, <code>false</code> to disable
     */
    void setNodeLabelFitToNodeSize(boolean fitToNodeSize);

    /**
     * Sets whether node label overlap avoidance is enabled.
     *
     * @param avoidOverlap <code>true</code> to enable, <code>false</code> to disable
     */
    void setAvoidNodeLabelOverlap(boolean avoidOverlap);

    // Edge Labels

    /**
     * Sets whether edge labels are visible.
     *
     * @param showEdgeLabels <code>true</code> to show edge labels, <code>false</code> to hide them
     */
    void setShowEdgeLabels(boolean showEdgeLabels);

    /**
     * Sets the font used for edge labels.
     *
     * @param font the edge label font
     */
    void setEdgeLabelFont(Font font);

    /**
     * Sets the edge label size scaling factor.
     *
     * @param scale the edge label scale
     */
    void setEdgeLabelScale(float scale);

    /**
     * Sets the edge label color mode.
     *
     * @param mode the edge label color mode
     */
    void setEdgeLabelColorMode(LabelColorMode mode);

    /**
     * Sets the edge label size mode.
     *
     * @param mode the edge label size mode
     */
    void setEdgeLabelSizeMode(LabelSizeMode mode);

    /**
     * Sets whether non-selected edge labels are hidden.
     *
     * @param hideNonSelected <code>true</code> to hide, <code>false</code> to show
     */
    void setHideNonSelectedEdgeLabels(boolean hideNonSelected);

    /**
     * Sets the columns used to generate edge labels.
     *
     * @param columns the edge label columns
     */
    void setEdgeLabelColumns(Column[] columns);
}
