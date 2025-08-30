package org.gephi.visualization.api;

import java.awt.Color;
import java.awt.Font;
import java.util.List;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.gephi.project.spi.Model;

public interface VisualisationModel extends Model {

    ScreenshotModel getScreenshotModel();

    float getZoom();

    int getFps();

    boolean isAutoSelectNeighbors();

    Color getBackgroundColor();

    boolean isLightenNonSelectedAuto();

    float getNodeScale();

    // Edges

    boolean isShowEdges();

    EdgeColorMode getEdgeColorMode();

    boolean isHideNonSelectedEdges();

    boolean isEdgeSelectionColor();

    Color getEdgeInSelectionColor();

    Color getEdgeOutSelectionColor();

    Color getEdgeBothSelectionColor();

    float getEdgeScale();

    boolean isUseEdgeWeight();

    // Selection

    List<Node> getSelectedNodes();

    List<Edge> getSelectedEdges();

    int getMouseSelectionDiameter();

    boolean isMouseSelectionZoomProportional();

    boolean isRectangleSelection();

    boolean isDirectMouseSelection();

    boolean isCustomSelection();

    boolean isSelectionEnabled();

    boolean isNodeSelection();

    boolean isSingleNodeSelection();

    // Text

    boolean isShowNodeLabels();

    boolean isShowEdgeLabels();

    LabelColorMode getNodeLabelColorMode();

    LabelSizeMode getNodeLabelSizeMode();

    Font getNodeLabelFont();

    Font getEdgeLabelFont();

    float getNodeLabelScale();

    float getEdgeLabelScale();

    boolean isHideNonSelectedLabels();

    Column[] getEdgeLabelColumns();

    Column[] getNodeLabelColumns();
}