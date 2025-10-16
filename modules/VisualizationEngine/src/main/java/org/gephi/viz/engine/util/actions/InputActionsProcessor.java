package org.gephi.viz.engine.util.actions;

import org.gephi.graph.api.Graph;
import org.gephi.graph.api.NodeIterable;
import org.gephi.graph.api.Rect2D;
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.VizEngineModel;
import org.gephi.viz.engine.status.GraphRenderingOptions;
import org.gephi.viz.engine.status.GraphSelection;
import org.joml.Vector2f;

/**
 * @author Eduardo Ramos
 */
public class InputActionsProcessor {

    private final VizEngine<?, ?> engine;

    public InputActionsProcessor(VizEngine<?, ?> engine) {
        this.engine = engine;
    }

    public void selectNodesWithinRadius(VizEngineModel model, float x, float y, float radius) {
        final NodeIterable iterable = model.getGraphIndex().getNodesInsideCircle(x, y, radius);
        selectNodes(model, iterable);
    }

    public void selectNodesAndEdgesOnRectangle(VizEngineModel model, final Rect2D rectangle) {
        final NodeIterable iterable = model.getGraphIndex().getNodesInsideRectangle(rectangle);

        selectNodes(model, iterable);
    }

    public void selectNodesAndEdgesUnderPosition(VizEngineModel model, Vector2f worldCoords) {
        final NodeIterable iterable = model.getGraphIndex().getNodesUnderPosition(worldCoords.x, worldCoords.y);

        selectNodes(model, iterable);
    }

    public void clearSelection(VizEngineModel model) {
        model.getGraphSelection().clearSelectedNodes();
        model.getGraphSelection().clearSelectedEdges();
    }

    private void selectNodes(VizEngineModel model, final NodeIterable nodesIterable) {
        final GraphRenderingOptions renderingOptions = model.getRenderingOptions();
        final Graph graph = model.getGraphModel().getGraphVisible();
        final GraphSelection selection = model.getGraphSelection();

        selection.setSelectedNodes(graph, nodesIterable, renderingOptions.isAutoSelectNeighbours(),
            renderingOptions.isShowEdges());
    }

    public void processCameraMoveEvent(int xDiff, int yDiff) {
        float zoom = engine.getZoom();

        engine.translate(xDiff / zoom, -yDiff / zoom);
    }

    public void processZoomEvent(double zoomQuantity, int x, int y) {
        final float currentZoom = engine.getZoom();
        float newZoom = currentZoom;

        newZoom *= (float) Math.pow(1.1, zoomQuantity);
        if (newZoom < 0.001f) {
            newZoom = 0.001f;
        }

        if (newZoom > 1000f) {
            newZoom = 1000f;
        }

        //This does directional zoom, to follow where the mouse points:
        final Rect2D viewRect = engine.getViewBoundaries();
        final Vector2f center = new Vector2f(
            (viewRect.maxX + viewRect.minX) / 2,
            (viewRect.maxY + viewRect.minY) / 2
        );

        final Vector2f diff
            = engine.screenCoordinatesToWorldCoordinates(x, y)
            .sub(center);

        final Vector2f directionalZoomTranslation = new Vector2f(diff)
            .mul(currentZoom / newZoom)
            .sub(diff);

        engine.translate(directionalZoomTranslation);
        engine.setZoom(newZoom);
    }

    public void processCenterOnGraphEvent() {
        engine.centerOnGraph();
    }
}
