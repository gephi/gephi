package org.gephi.viz.engine.util.actions;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphLock;
import org.gephi.graph.api.NodeIterable;
import org.gephi.graph.api.Rect2D;
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.VizEngineModel;
import org.gephi.viz.engine.status.GraphRenderingOptions;
import org.gephi.viz.engine.status.GraphSelection;
import org.gephi.viz.engine.structure.GraphIndex;
import org.gephi.viz.engine.structure.GraphIndexImpl;
import org.joml.Vector2f;

/**
 * @author Eduardo Ramos
 */
public class InputActionsProcessor {

    private final VizEngine<?, ?> engine;

    public InputActionsProcessor(VizEngine<?, ?> engine) {
        this.engine = engine;
    }

    /**
     * @return false when a graph writer kept the lock and the selection wasn't changed
     */
    public boolean selectNodesWithinRadius(VizEngineModel model, float x, float y, float radius) {
        final float nodeScale = model.getRenderingOptions().getNodeScale();
        return selectNodes(model, graphIndex -> graphIndex.getNodesInsideCircle(x, y, radius, nodeScale));
    }

    /**
     * @return false when a graph writer kept the lock and the selection wasn't changed
     */
    public boolean selectNodesAndEdgesOnRectangle(VizEngineModel model, final Rect2D rectangle) {
        final float nodeScale = model.getRenderingOptions().getNodeScale();
        return selectNodes(model, graphIndex -> graphIndex.getNodesInsideRectangle(rectangle, nodeScale));
    }

    /**
     * @return false when a graph writer kept the lock and the selection wasn't changed
     */
    public boolean selectNodesAndEdgesUnderPosition(VizEngineModel model, Vector2f worldCoords) {
        final float nodeScale = model.getRenderingOptions().getNodeScale();
        return selectNodes(model,
            graphIndex -> graphIndex.getNodesUnderPosition(worldCoords.x, worldCoords.y, nodeScale));
    }

    public void clearSelection(VizEngineModel model) {
        model.getGraphSelection().clearSelectedNodes();
        model.getGraphSelection().clearSelectedEdges();
    }

    private boolean selectNodes(VizEngineModel model, Function<GraphIndex, NodeIterable> nodesQuery) {
        final GraphRenderingOptions renderingOptions = model.getRenderingOptions();
        final Graph graph = model.getGraphModel().getGraphVisible();
        final GraphSelection selection = model.getGraphSelection();

        // Selecting runs in the world update, which mustn't wait for a graph writer
        final GraphLock lock = graph.getLock();
        try {
            if (!lock.tryReadLock(GraphIndexImpl.READ_LOCK_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                return false;
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return false;
        }
        try {
            selection.setSelectedNodes(graph, nodesQuery.apply(model.getGraphIndex()),
                renderingOptions.isAutoSelectNeighbours(), renderingOptions.isShowEdges());
        } finally {
            lock.readUnlock();
        }
        return true;
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
