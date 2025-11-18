package org.gephi.viz.engine.status;

import java.awt.Color;
import java.awt.Font;
import java.util.Objects;
import org.gephi.graph.api.Column;
import org.joml.Vector2f;
import org.joml.Vector2fc;

public class GraphRenderingOptionsImpl implements GraphRenderingOptions {

    //Show:
    private boolean showNodes = DEFAULT_SHOW_NODES;
    private boolean showEdges = DEFAULT_SHOW_EDGES;
    private boolean showNodeLabels = DEFAULT_SHOW_NODE_LABELS;
    private boolean showEdgeLabels = DEFAULT_SHOW_EDGE_LABELS;

    //Global
    private float[] backgroundColor = DEFAULT_BACKGROUND_COLOR;
    private float zoom = DEFAULT_ZOOM;
    private final Vector2f pan = new Vector2f(DEFAULT_PAN_X, DEFAULT_PAN_Y);

    //Edges
    private float edgeScale = DEFAULT_EDGE_SCALE;
    private boolean edgeSelectionColor = DEFAULT_ENABLE_EDGE_SELECTION_COLOR;
    private Color edgeBothSelectionColor = DEFAULT_EDGE_BOTH_SELECTION_COLOR;
    private Color edgeInSelectionColor = DEFAULT_EDGE_IN_SELECTION_COLOR;
    private Color edgeOutSelectionColor = DEFAULT_EDGE_OUT_SELECTION_COLOR;
    private EdgeColorMode edgeColorMode = DEFAULT_EDGE_COLOR_MODE;
    private boolean edgeWeightEnabled = DEFAULT_EDGE_WEIGHT_ENABLED;

    //Nodes
    private float nodeScale = DEFAULT_NODE_SCALE;

    //Node Labels
    private boolean nodeLabelFitToNodeSize = DEFAULT_NODE_LABEL_FIT_TO_NODE_SIZE;
    private float nodeLabelScale = DEFAULT_NODE_LABEL_SCALE;
    private LabelColorMode nodeLabelColorMode = DEFAULT_NODE_LABEL_COLOR_MODE;
    private LabelSizeMode nodeLabelSizeMode = DEFAULT_NODE_LABEL_SIZE_MODE;
    private Font nodeLabelFont = DEFAULT_NODE_LABEL_FONT;
    private boolean hideNonSelectedNodeLabels = DEFAULT_HIDE_NON_SELECTED_NODE_LABELS;
    private float nodeLabelFitToNodeSizeFactor = DEFAULT_NODE_LABEL_FIT_TO_NODE_SIZE_FACTOR;
    private boolean avoidNodeLabelOverlap = DEFAULT_AVOID_NODE_LABEL_OVERLAP;
    private Column[] nodeLabelColumns = new Column[0];

    //Edge Labels
    private LabelColorMode edgeLabelColorMode = DEFAULT_NODE_LABEL_COLOR_MODE;
    private LabelSizeMode edgeLabelSizeMode = DEFAULT_NODE_LABEL_SIZE_MODE;
    private Font edgeLabelFont = DEFAULT_NODE_LABEL_FONT;
    private float edgeLabelScale = DEFAULT_NODE_LABEL_SCALE;
    private boolean hideNonSelectedEdgeLabels = DEFAULT_HIDE_NON_SELECTED_NODE_LABELS;
    private Column[] edgeLabelColumns = new Column[0];

    //Selection:
    private boolean autoSelectNeighbours = DEFAULT_AUTO_SELECT_NEIGHBOURS;
    private boolean hideNonSelectedEdges = DEFAULT_HIDE_NON_SELECTED_EDGES;
    private boolean lightenNonSelected = DEFAULT_LIGHTEN_NON_SELECTED;
    private float lightenNonSelectedFactor = DEFAULT_LIGHTEN_NON_SELECTED_FACTOR;

    public GraphRenderingOptionsImpl() {
    }

    public GraphRenderingOptionsImpl(GraphRenderingOptions other) {
        Objects.requireNonNull(other, "other");

        // Show
        this.showNodes = other.isShowNodes();
        this.showEdges = other.isShowEdges();
        this.showNodeLabels = other.isShowNodeLabels();
        this.showEdgeLabels = other.isShowEdgeLabels();

        // Global
        float[] otherBg = other.getBackgroundColor();
        this.backgroundColor = otherBg.clone();
        this.zoom = other.getZoom();
        this.pan.set(other.getPan());

        // Edges
        this.edgeScale = other.getEdgeScale();
        this.edgeSelectionColor = other.isEdgeSelectionColor();
        this.edgeBothSelectionColor = other.getEdgeBothSelectionColor();
        this.edgeInSelectionColor = other.getEdgeInSelectionColor();
        this.edgeOutSelectionColor = other.getEdgeOutSelectionColor();
        this.edgeColorMode = other.getEdgeColorMode();
        this.edgeWeightEnabled = other.isEdgeWeightEnabled();

        // Nodes
        this.nodeScale = other.getNodeScale();

        // Node Labels
        this.nodeLabelFitToNodeSize = other.isNodeLabelFitToNodeSize();
        this.nodeLabelScale = other.getNodeLabelScale();
        this.nodeLabelColorMode = other.getNodeLabelColorMode();
        this.nodeLabelSizeMode = other.getNodeLabelSizeMode();
        this.nodeLabelFont = other.getNodeLabelFont();
        this.hideNonSelectedNodeLabels = other.isHideNonSelectedNodeLabels();
        this.nodeLabelFitToNodeSizeFactor = other.getNodeLabelFitToNodeSizeFactor();
        this.nodeLabelColumns = other.getNodeLabelColumns();
        this.avoidNodeLabelOverlap = other.isAvoidNodeLabelOverlap();

        // Edge Labels
        this.edgeLabelColorMode = other.getEdgeLabelColorMode();
        this.edgeLabelSizeMode = other.getEdgeLabelSizeMode();
        this.edgeLabelFont = other.getEdgeLabelFont();
        this.edgeLabelScale = other.getEdgeLabelScale();
        this.hideNonSelectedEdgeLabels = other.isHideNonSelectedEdgeLabels();
        this.edgeLabelColumns = other.getEdgeLabelColumns();

        // Selection
        this.autoSelectNeighbours = other.isAutoSelectNeighbours();
        this.hideNonSelectedEdges = other.isHideNonSelectedEdges();
        this.lightenNonSelected = other.isLightenNonSelected();
        this.lightenNonSelectedFactor = other.getLightenNonSelectedFactor();
    }

    @Override
    public float[] getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color color) {
        Objects.requireNonNull(color, "backgroundColor can't be null");
        float[] backgroundColorComponents = new float[4];
        color.getRGBComponents(backgroundColorComponents);
        this.backgroundColor = backgroundColorComponents;
    }

    @Override
    public void setBackgroundColor(float[] backgroundColor) {
        Objects.requireNonNull(backgroundColor, "backgroundColor can't be null");
        this.backgroundColor = backgroundColor;
    }

    @Override
    public float getZoom() {
        return zoom;
    }

    @Override
    public void setZoom(float zoom) {
        this.zoom = zoom;
    }

    @Override
    public Vector2fc getPan() {
        return pan;
    }

    @Override
    public void setPan(Vector2fc value) {
        pan.set(value);
    }

    @Override
    public boolean isLightenNonSelected() {
        return lightenNonSelected;
    }

    @Override
    public void setLightenNonSelected(boolean lightenNonSelected) {
        this.lightenNonSelected = lightenNonSelected;
    }

    @Override
    public float getLightenNonSelectedFactor() {
        return lightenNonSelectedFactor;
    }

    @Override
    public void setLightenNonSelectedFactor(float lightenNonSelectedFactor) {
        if (lightenNonSelectedFactor < 0) {
            lightenNonSelectedFactor = 0;
        }

        if (lightenNonSelectedFactor > 1) {
            lightenNonSelectedFactor = 1;
        }

        this.lightenNonSelectedFactor = lightenNonSelectedFactor;
    }

    @Override
    public boolean isAutoSelectNeighbours() {
        return autoSelectNeighbours;
    }

    @Override
    public void setAutoSelectNeighbours(boolean autoSelectNeighbours) {
        this.autoSelectNeighbours = autoSelectNeighbours;
    }

    // Nodes

    @Override
    public float getNodeScale() {
        return nodeScale;
    }

    @Override
    public void setNodeScale(float nodeScale) {
        if (Float.isNaN(nodeScale) || Float.isInfinite(nodeScale)) {
            nodeScale = DEFAULT_NODE_SCALE;
        }

        if (nodeScale <= 0f) {
            throw new IllegalArgumentException("nodeScale should be > 0");
        }

        this.nodeScale = nodeScale;
    }

    @Override
    public boolean isShowNodes() {
        return showNodes;
    }

    @Override
    public void setShowNodes(boolean showNodes) {
        this.showNodes = showNodes;
    }

    // Edges

    @Override
    public boolean isShowEdges() {
        return showEdges;
    }

    @Override
    public void setShowEdges(boolean showEdges) {
        this.showEdges = showEdges;
    }

    @Override
    public float getEdgeScale() {
        return edgeScale;
    }

    @Override
    public void setEdgeScale(float edgeScale) {
        if (Float.isNaN(edgeScale) || Float.isInfinite(edgeScale)) {
            nodeScale = DEFAULT_NODE_SCALE;
        }

        if (edgeScale <= 0) {
            throw new IllegalArgumentException("edgeScale should be > 0");
        }

        this.edgeScale = edgeScale;
    }

    @Override
    public boolean isHideNonSelectedEdges() {
        return hideNonSelectedEdges;
    }

    @Override
    public void setHideNonSelectedEdges(boolean hideNonSelected) {
        this.hideNonSelectedEdges = hideNonSelected;
    }


    @Override
    public boolean isEdgeSelectionColor() {
        return edgeSelectionColor;
    }

    @Override
    public void setEdgeSelectionColor(boolean edgeSelectionColor) {
        this.edgeSelectionColor = edgeSelectionColor;
    }

    @Override
    public Color getEdgeBothSelectionColor() {
        return edgeBothSelectionColor;
    }

    @Override
    public void setEdgeBothSelectionColor(Color color) {
        Objects.requireNonNull(color, "edge both selection color can't be null");
        this.edgeBothSelectionColor = color;
    }

    @Override
    public Color getEdgeOutSelectionColor() {
        return edgeOutSelectionColor;
    }

    @Override
    public void setEdgeOutSelectionColor(Color color) {
        Objects.requireNonNull(color, "edge out selection color can't be null");
        this.edgeOutSelectionColor = color;
    }

    @Override
    public Color getEdgeInSelectionColor() {
        return edgeInSelectionColor;
    }

    @Override
    public void setEdgeInSelectionColor(Color color) {
        Objects.requireNonNull(color, "edge in selection color can't be null");
        this.edgeInSelectionColor = color;
    }

    @Override
    public EdgeColorMode getEdgeColorMode() {
        return edgeColorMode;
    }

    @Override
    public void setEdgeColorMode(EdgeColorMode mode) {
        this.edgeColorMode = Objects.requireNonNull(mode, "mode can't be null");
    }

    @Override
    public boolean isEdgeWeightEnabled() {
        return edgeWeightEnabled;
    }

    @Override
    public void setEdgeWeightEnabled(boolean enabled) {
        this.edgeWeightEnabled = enabled;
    }

    // Node Labels

    @Override
    public boolean isShowNodeLabels() {
        return showNodeLabels;
    }

    @Override
    public void setShowNodeLabels(boolean showNodeLabels) {
        this.showNodeLabels = showNodeLabels;
    }

    @Override
    public Column[] getNodeLabelColumns() {
        return nodeLabelColumns;
    }

    @Override
    public void setNodeLabelColumns(Column[] nodeLabelColumns) {
        this.nodeLabelColumns = Objects.requireNonNull(nodeLabelColumns, "nodeLabelColumns can't be null");
    }

    @Override
    public boolean isNodeLabelFitToNodeSize() {
        return nodeLabelFitToNodeSize;
    }

    @Override
    public void setNodeLabelFitToNodeSize(boolean fitToNodeSize) {
        this.nodeLabelFitToNodeSize = fitToNodeSize;
    }

    @Override
    public float getNodeLabelScale() {
        return nodeLabelScale;
    }

    @Override
    public float getNodeLabelFitToNodeSizeFactor() {
        return nodeLabelFitToNodeSizeFactor;
    }

    @Override
    public void setNodeLabelFitToNodeSizeFactor(float factor) {
        this.nodeLabelFitToNodeSizeFactor = factor;
    }

    @Override
    public void setNodeLabelScale(float nodeLabelScale) {
        if (nodeLabelScale <= 0f || Float.isNaN(nodeLabelScale) || Float.isInfinite(nodeLabelScale)) {
            throw new IllegalArgumentException("nodeLabelScale should be > 0");
        }

        this.nodeLabelScale = nodeLabelScale;
    }

    @Override
    public LabelColorMode getNodeLabelColorMode() {
        return nodeLabelColorMode;
    }

    @Override
    public void setNodeLabelColorMode(LabelColorMode labelColorMode) {
        this.nodeLabelColorMode = Objects.requireNonNull(labelColorMode, "labelColorMode can't be null");
    }

    @Override
    public LabelSizeMode getNodeLabelSizeMode() {
        return nodeLabelSizeMode;
    }

    @Override
    public void setNodeLabelSizeMode(LabelSizeMode labelSizeMode) {
        this.nodeLabelSizeMode = Objects.requireNonNull(labelSizeMode, "labelSizeMode can't be null");
    }

    @Override
    public Font getNodeLabelFont() {
        return nodeLabelFont;
    }

    @Override
    public void setNodeLabelFont(Font font) {
        this.nodeLabelFont = Objects.requireNonNull(font, "font can't be null");
    }

    @Override
    public boolean isHideNonSelectedNodeLabels() {
        return hideNonSelectedNodeLabels;
    }

    @Override
    public void setHideNonSelectedNodeLabels(boolean hideNonSelected) {
        this.hideNonSelectedNodeLabels = hideNonSelected;
    }

    @Override
    public boolean isAvoidNodeLabelOverlap() {
        return avoidNodeLabelOverlap;
    }

    @Override
    public void setAvoidNodeLabelOverlap(boolean avoidOverlap) {
        this.avoidNodeLabelOverlap = avoidOverlap;
    }

    // Edge Labels

    @Override
    public boolean isShowEdgeLabels() {
        return showEdgeLabels;
    }

    @Override
    public void setShowEdgeLabels(boolean showEdgeLabels) {
        this.showEdgeLabels = showEdgeLabels;
    }

    @Override
    public Column[] getEdgeLabelColumns() {
        return edgeLabelColumns;
    }

    @Override
    public void setEdgeLabelColumns(Column[] edgeLabelColumns) {
        this.edgeLabelColumns = Objects.requireNonNull(edgeLabelColumns, "edgeLabelColumns can't be null");
    }

    @Override
    public LabelColorMode getEdgeLabelColorMode() {
        return edgeLabelColorMode;
    }

    @Override
    public void setEdgeLabelColorMode(LabelColorMode labelColorMode) {
        this.edgeLabelColorMode = Objects.requireNonNull(labelColorMode, "labelColorMode can't be null");
    }

    @Override
    public LabelSizeMode getEdgeLabelSizeMode() {
        return edgeLabelSizeMode;
    }

    @Override
    public void setEdgeLabelSizeMode(LabelSizeMode labelSizeMode) {
        this.edgeLabelSizeMode = Objects.requireNonNull(labelSizeMode, "labelSizeMode can't be null");
    }

    @Override
    public Font getEdgeLabelFont() {
        return edgeLabelFont;
    }

    @Override
    public void setEdgeLabelFont(Font font) {
        this.edgeLabelFont = Objects.requireNonNull(font, "font can't be null");
    }

    @Override
    public float getEdgeLabelScale() {
        return edgeLabelScale;
    }

    @Override
    public void setEdgeLabelScale(float edgeLabelScale) {
        if (edgeLabelScale <= 0f || Float.isNaN(edgeLabelScale) || Float.isInfinite(edgeLabelScale)) {
            throw new IllegalArgumentException("edgeLabelScale should be > 0");
        }

        this.edgeLabelScale = edgeLabelScale;
    }

    @Override
    public boolean isHideNonSelectedEdgeLabels() {
        return hideNonSelectedEdgeLabels;
    }

    @Override
    public void setHideNonSelectedEdgeLabels(boolean hideNonSelected) {
        this.hideNonSelectedEdgeLabels = hideNonSelected;
    }
}
