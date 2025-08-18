package org.gephi.viz.engine.status;

import java.awt.Color;
import java.util.Objects;

public class GraphRenderingOptionsImpl implements GraphRenderingOptions {

    //Show:
    private boolean showNodes = DEFAULT_SHOW_NODES;
    private boolean showEdges = DEFAULT_SHOW_EDGES;
    private boolean showNodeLabels = DEFAULT_SHOW_NODE_LABELS;
    private boolean showEdgeLabels = DEFAULT_SHOW_EDGE_LABELS;

    //Edges
    private float edgeScale = DEFAULT_EDGE_SCALE;
    private boolean edgeSelectionColor = DEFAULT_ENABLE_EDGE_SELECTION_COLOR;
    private Color edgeBothSelectionColor = DEFAULT_EDGE_BOTH_SELECTION_COLOR;
    private Color edgeInSelectionColor = DEFAULT_EDGE_IN_SELECTION_COLOR;
    private Color edgeOutSelectionColor = DEFAULT_EDGE_OUT_SELECTION_COLOR;

    //Selection:
    private boolean autoSelectNeighbours = DEFAULT_AUTO_SELECT_NEIGHBOURS;
    private boolean hideNonSelected = DEFAULT_HIDE_NON_SELECTED;
    private boolean lightenNonSelected = DEFAULT_LIGHTEN_NON_SELECTED;
    private float lightenNonSelectedFactor = DEFAULT_LIGHTEN_NON_SELECTED_FACTOR;

    @Override
    public float getEdgeScale() {
        return edgeScale;
    }

    @Override
    public void setEdgeScale(float edgeScale) {
        if (edgeScale <= 0) {
            throw new IllegalArgumentException("edgeScale should be > 0");
        }

        this.edgeScale = edgeScale;
    }

    @Override
    public boolean isShowNodes() {
        return showNodes;
    }

    @Override
    public void setShowNodes(boolean showNodes) {
        this.showNodes = showNodes;
    }

    @Override
    public boolean isShowEdges() {
        return showEdges;
    }

    @Override
    public void setShowEdges(boolean showEdges) {
        this.showEdges = showEdges;
    }

    @Override
    public boolean isShowNodeLabels() {
        return showNodeLabels;
    }

    @Override
    public void setShowNodeLabels(boolean showNodeLabels) {
        this.showNodeLabels = showNodeLabels;
    }

    @Override
    public boolean isShowEdgeLabels() {
        return showEdgeLabels;
    }

    @Override
    public void setShowEdgeLabels(boolean showEdgeLabels) {
        this.showEdgeLabels = showEdgeLabels;
    }

    @Override
    public boolean isHideNonSelected() {
        return hideNonSelected;
    }

    @Override
    public void setHideNonSelected(boolean hideNonSelected) {
        this.hideNonSelected = hideNonSelected;
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
        Objects.requireNonNull(color, "color");
        this.edgeBothSelectionColor = color;
    }

    @Override
    public Color getEdgeOutSelectionColor() {
        return edgeOutSelectionColor;
    }

    @Override
    public void setEdgeOutSelectionColor(Color color) {
        Objects.requireNonNull(color, "color");
        this.edgeOutSelectionColor = color;
    }

    @Override
    public Color getEdgeInSelectionColor() {
        return edgeInSelectionColor;
    }

    @Override
    public void setEdgeInSelectionColor(Color color) {
        Objects.requireNonNull(color, "color");
        this.edgeInSelectionColor = color;
    }

}
