package org.gephi.visualization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.gephi.viz.engine.status.GraphSelection;

public class SelectionModelImpl {

    // Model
    private final VizModel visualisationModel;
    // Settings
    private int mouseSelectionDiameter;
    private boolean mouseSelectionZoomProportional;
    //States
    private boolean rectangleSelection = false;
    private boolean selectionEnable = true;
    private boolean customSelection = false;
    private boolean blocked = false;
    // TODO: Check if needed
    protected boolean wasAutoSelectNeighbors = false;
    protected boolean wasRectangleSelection = false;
    protected boolean wasDirectSelection = false;

    public SelectionModelImpl(VizModel visualisationModel) {
        this.visualisationModel = visualisationModel;

        // Settings
        this.mouseSelectionDiameter = 1;
    }

    protected Optional<GraphSelection> currentEngineSelectionModel() {
        return visualisationModel.getEngine().map(engine -> {
            return engine.getLookup().lookup(GraphSelection.class);
        });
    }

    public List<Node> getSelectedNodes() {
        return currentEngineSelectionModel()
            .map(selection -> (List<Node>) new ArrayList<>(selection.getSelectedNodes()))
            .orElse(Collections.emptyList());
    }

    public List<Edge> getSelectedEdges() {
        return currentEngineSelectionModel()
            .map(selection -> (List<Edge>) new ArrayList<>(selection.getSelectedEdges()))
            .orElse(Collections.emptyList());
    }

    public int getMouseSelectionDiameter() {
        return mouseSelectionDiameter;
    }

    protected void setMouseSelectionDiameter(int mouseSelectionDiameter) {
        this.mouseSelectionDiameter = mouseSelectionDiameter;
    }

    public boolean isMouseSelectionZoomProportional() {
        return mouseSelectionZoomProportional;
    }

    protected void setMouseSelectionZoomProportional(boolean mouseSelectionZoomProportional) {
        this.mouseSelectionZoomProportional = mouseSelectionZoomProportional;
    }

    public boolean isBlocked() {
        return blocked;
    }

    protected void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    protected void setSelectionEnable(boolean selectionEnable) {
        this.selectionEnable = selectionEnable;
    }

    protected void setCustomSelection(boolean customSelection) {
        this.customSelection = customSelection;
    }

    public boolean isRectangleSelection() {
        return selectionEnable && rectangleSelection;
    }

    public boolean isDirectMouseSelection() {
        return selectionEnable && !rectangleSelection;
    }

    public boolean isCustomSelection() {
        return customSelection;
    }

    public boolean isSelectionEnabled() {
        return selectionEnable;
    }
}
