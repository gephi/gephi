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
import org.gephi.graph.api.Node;
import org.gephi.project.api.Workspace;

/**
 * @author Mathieu Bastian
 */
public interface VisualizationController {

    VisualisationModel getModel();

    VisualisationModel getModel(Workspace workspace);

    ScreenshotController getScreenshotController();

    void setZoom(float zoom);

    void setAutoSelectNeighbors(boolean autoSelectNeighbors);

    void setBackgroundColor(Color color);

    void setNodeScale(float nodeScale);

    void setShowEdges(boolean showEdges);

    void setEdgeColorMode(EdgeColorMode mode);

    void setEdgeSelectionColor(boolean edgeSelectionColor);

    void setEdgeInSelectionColor(Color edgeInSelectionColor);

    void setEdgeOutSelectionColor(Color edgeOutSelectionColor);

    void setEdgeBothSelectionColor(Color edgeBothSelectionColor);

    void setEdgeScale(float edgeScale);

    void setUseEdgeWeight(boolean useEdgeWeight);

    void setLightenNonSelectedAuto(boolean lightenNonSelectedAuto);

    void setHideNonSelectedEdges(boolean hideNonSelectedEdges);

    void centerOnGraph();

    void centerOnZero();

    void centerOn(float x, float y, float width, float height);

    void centerOnNode(Node node);

    void centerOnEdge(Edge edge);

    void addPropertyChangeListener(VisualizationPropertyChangeListener listener);

    void removePropertyChangeListener(VisualizationPropertyChangeListener listener);

    void addListener(VisualizationEventListener listener);

    void removeListener(VisualizationEventListener listener);

    // Selection

    void disableSelection();

    void setRectangleSelection();

    void setDirectMouseSelection();

    void setCustomSelection();

    void setNodeSelection(boolean singleNode);

    void setMouseSelectionDiameter(int diameter);

    void setMouseSelectionZoomProportional(boolean proportional);

    void resetSelection();

    void selectNodes(Node[] nodes);

    void selectEdges(Edge[] edges);

    // Text

    void setShowEdgeLabels(boolean showEdgeLabels);

    void setShowNodeLabels(boolean showNodeLabels);

    void setNodeLabelFont(Font font);

    void setNodeLabelScale(float scale);

    void setEdgeLabelFont(Font font);

    void setEdgeLabelScale(float scale);

    void setHideNonSelectedLabels(boolean hideNonSelected);

    void setNodeLabelColorMode(LabelColorMode mode);

    void setNodeLabelSizeMode(LabelSizeMode mode);

    void setNodeLabelColumns(Column[] columns);

    void setEdgeLabelColumns(Column[] columns);
}
