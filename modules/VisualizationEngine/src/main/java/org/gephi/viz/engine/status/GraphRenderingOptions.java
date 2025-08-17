package org.gephi.viz.engine.status;

import java.awt.*;

/**
 *
 * @author Eduardo Ramos
 */
public interface GraphRenderingOptions {

    //Show:
    boolean DEFAULT_SHOW_NODES = true;
    boolean DEFAULT_SHOW_EDGES = true;
    boolean DEFAULT_SHOW_NODE_LABELS = false;
    boolean DEFAULT_SHOW_EDGE_LABELS = false;

    //Edges:
    float DEFAULT_EDGE_SCALE = 2f;
    boolean DEFAULT_ENABLE_EDGE_SELECTION_COLOR = false;
    Color DEFAULT_EDGE_IN_SELECTION_COLOR = new Color(32, 95, 154, 255);
    Color DEFAULT_EDGE_OUT_SELECTION_COLOR = new Color(196, 66, 79, 255);
    Color DEFAULT_EDGE_BOTH_SELECTION_COLOR = new Color(248, 215, 83, 255);

    //Selection:
    boolean DEFAULT_HIDE_NON_SELECTED = false;
    boolean DEFAULT_LIGHTEN_NON_SELECTED = true;
    boolean DEFAULT_AUTO_SELECT_NEIGHBOURS = true;
    float DEFAULT_LIGHTEN_NON_SELECTED_FACTOR = 0.9f;

    float getEdgeScale();

    void setEdgeScale(float edgeScale);

    boolean isShowNodes();

    void setShowNodes(boolean showNodes);

    boolean isShowEdges();

    void setShowEdges(boolean showEdges);

    boolean isShowNodeLabels();

    void setShowNodeLabels(boolean showNodeLabels);

    boolean isShowEdgeLabels();

    void setShowEdgeLabels(boolean showEdgeLabels);

    boolean isHideNonSelected();

    void setHideNonSelected(boolean hideNonSelected);

    boolean isLightenNonSelected();

    void setLightenNonSelected(boolean lightenNonSelected);

    float getLightenNonSelectedFactor();

    void setLightenNonSelectedFactor(float lightenNonSelectedFactor);

    boolean isAutoSelectNeighbours();

    void setAutoSelectNeighbours(boolean autoSelectNeighbours);

    boolean isEdgeSelectionColor();

    void setEdgeSelectionColor(boolean edgeSelectionColor);

    Color getEdgeBothSelectionColor();

    void setEdgeBothSelectionColor(Color color);

    Color getEdgeOutSelectionColor();

    void setEdgeOutSelectionColor(Color color);

    Color getEdgeInSelectionColor();

    void setEdgeInSelectionColor(Color color);
}
