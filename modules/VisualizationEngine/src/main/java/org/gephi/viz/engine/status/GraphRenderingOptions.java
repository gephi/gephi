package org.gephi.viz.engine.status;

import java.awt.Color;
import java.awt.Font;
import org.gephi.graph.api.Column;
import org.joml.Vector2fc;

/**
 *
 * @author Eduardo Ramos
 */
public interface GraphRenderingOptions {

    enum EdgeColorMode {
        SOURCE,
        TARGET,
        MIXED,
        SELF
    }

    enum LabelColorMode {
        SELF,
        OBJECT
    }

    enum LabelSizeMode {
        SCREEN,
        ZOOM
    }

    //Show:
    boolean DEFAULT_SHOW_NODES = true;
    boolean DEFAULT_SHOW_EDGES = true;
    boolean DEFAULT_SHOW_NODE_LABELS = false;
    boolean DEFAULT_SHOW_EDGE_LABELS = false;

    //Global
    float[] DEFAULT_BACKGROUND_COLOR = new float[] {1, 1, 1, 1};
    float DEFAULT_ZOOM = 0.3f;
    float DEFAULT_PAN_X = 0f;
    float DEFAULT_PAN_Y = 0f;

    //Nodes:
    float DEFAULT_NODE_SCALE = 1f;

    //Edges:
    float DEFAULT_EDGE_SCALE = 2f;
    boolean DEFAULT_ENABLE_EDGE_SELECTION_COLOR = false;
    Color DEFAULT_EDGE_IN_SELECTION_COLOR = new Color(32, 95, 154, 255);
    Color DEFAULT_EDGE_OUT_SELECTION_COLOR = new Color(196, 66, 79, 255);
    Color DEFAULT_EDGE_BOTH_SELECTION_COLOR = new Color(248, 215, 83, 255);
    EdgeColorMode DEFAULT_EDGE_COLOR_MODE = EdgeColorMode.SELF;
    boolean DEFAULT_EDGE_WEIGHT_ENABLED = true;

    //Node Labels
    boolean DEFAULT_NODE_LABEL_FIT_TO_NODE_SIZE = false;
    float DEFAULT_NODE_LABEL_SCALE = 1f;
    LabelColorMode DEFAULT_NODE_LABEL_COLOR_MODE = LabelColorMode.SELF;
    LabelSizeMode DEFAULT_NODE_LABEL_SIZE_MODE = LabelSizeMode.ZOOM;
    Font DEFAULT_NODE_LABEL_FONT = new Font("Arial", Font.BOLD, 32);
    boolean DEFAULT_HIDE_NON_SELECTED_NODE_LABELS = false;
    float DEFAULT_NODE_LABEL_FIT_TO_NODE_SIZE_FACTOR = 0.05f;
    boolean DEFAULT_AVOID_NODE_LABEL_OVERLAP = false;

    //Selection:
    boolean DEFAULT_HIDE_NON_SELECTED_EDGES = false;
    boolean DEFAULT_LIGHTEN_NON_SELECTED = true;
    boolean DEFAULT_AUTO_SELECT_NEIGHBOURS = true;
    float DEFAULT_LIGHTEN_NON_SELECTED_FACTOR = 0.9f;

    float[] getBackgroundColor();

    void setBackgroundColor(float[] backgroundColor);

    float getZoom();

    void setZoom(float zoom);

    Vector2fc getPan();

    void setPan(Vector2fc pan);

    boolean isLightenNonSelected();

    void setLightenNonSelected(boolean lightenNonSelected);

    float getLightenNonSelectedFactor();

    void setLightenNonSelectedFactor(float lightenNonSelectedFactor);

    boolean isAutoSelectNeighbours();

    void setAutoSelectNeighbours(boolean autoSelectNeighbours);

    // Nodes

    float getNodeScale();

    void setNodeScale(float nodeScale);

    boolean isShowNodes();

    void setShowNodes(boolean showNodes);

    // Edges

    boolean isShowEdges();

    void setShowEdges(boolean showEdges);

    float getEdgeScale();

    void setEdgeScale(float edgeScale);

    boolean isHideNonSelectedEdges();

    void setHideNonSelectedEdges(boolean hideNonSelected);

    boolean isEdgeSelectionColor();

    void setEdgeSelectionColor(boolean edgeSelectionColor);

    Color getEdgeBothSelectionColor();

    void setEdgeBothSelectionColor(Color color);

    Color getEdgeOutSelectionColor();

    void setEdgeOutSelectionColor(Color color);

    Color getEdgeInSelectionColor();

    void setEdgeInSelectionColor(Color color);

    EdgeColorMode getEdgeColorMode();

    void setEdgeColorMode(EdgeColorMode mode);

    boolean isEdgeWeightEnabled();

    void setEdgeWeightEnabled(boolean enabled);

    // Node Labels

    boolean isShowNodeLabels();

    void setShowNodeLabels(boolean showNodeLabels);

    Column[] getNodeLabelColumns();

    void setNodeLabelColumns(Column[] columns);

    LabelColorMode getNodeLabelColorMode();

    void setNodeLabelColorMode(LabelColorMode labelColorMode);

    LabelSizeMode getNodeLabelSizeMode();

    void setNodeLabelSizeMode(LabelSizeMode labelSizeMode);

    Font getNodeLabelFont();

    void setNodeLabelFont(Font font);

    float getNodeLabelScale();

    void setNodeLabelScale(float nodeLabelScale);

    boolean isNodeLabelFitToNodeSize();

    void setNodeLabelFitToNodeSize(boolean fitToNodeSize);

    float getNodeLabelFitToNodeSizeFactor();

    void setNodeLabelFitToNodeSizeFactor(float factor);

    boolean isHideNonSelectedNodeLabels();

    void setHideNonSelectedNodeLabels(boolean hideNonSelected);

    boolean isAvoidNodeLabelOverlap();

    void setAvoidNodeLabelOverlap(boolean avoidOverlap);

    // Edge Labels

    boolean isShowEdgeLabels();

    void setShowEdgeLabels(boolean showEdgeLabels);

    Column[] getEdgeLabelColumns();

    void setEdgeLabelColumns(Column[] columns);

}
