package org.gephi.visualization.api;

import java.awt.Color;
import java.util.List;
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

    boolean isUniColorSelected();

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

    boolean isBlocked();

    boolean isRectangleSelection();

    boolean isDirectMouseSelection();

    boolean isCustomSelection();

    boolean isSelectionEnabled();
}