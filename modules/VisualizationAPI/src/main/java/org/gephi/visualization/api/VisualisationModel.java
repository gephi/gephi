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

    boolean isAutoSelectNeighbors();

    Color getBackgroundColor();

    boolean isShowEdges();

    boolean isEdgeHasUniColor();

    float[] getEdgeUniColor();

    boolean isHideNonSelectedEdges();

    boolean isLightenNonSelectedAuto();

    boolean isEdgeSelectionColor();

    float[] getEdgeInSelectionColor();

    float[] getEdgeOutSelectionColor();

    float[] getEdgeBothSelectionColor();

    float getEdgeScale();

    // Selection

    List<Node> getSelectedNodes();

    List<Edge> getSelectedEdges();

    int getMouseSelectionDiameter();

    boolean isMouseSelectionZoomProportional();

    boolean isRectangleSelection();

    boolean isDirectMouseSelection();

    boolean isCustomSelection();

    boolean isSelectionEnabled();

    boolean isSingleNodeSelection();

    // Text

    boolean isShowNodeLabels();

    boolean isShowEdgeLabels();

    Color getNodeLabelColor();

    Color getEdgeLabelColor();

    LabelColorMode getNodeLabelColorMode();

    LabelSizeMode getNodeLabelSizeMode();

    Font getNodeLabelFont();

    Font getEdgeLabelFont();

    float getNodeLabelSize();

    float getEdgeLabelSize();

    boolean isHideNonSelectedLabels();

    Column[] getEdgeLabelColumns();

    Column[] getNodeLabelColumns();
}