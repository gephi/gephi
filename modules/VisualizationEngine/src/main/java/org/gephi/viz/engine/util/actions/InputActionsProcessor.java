package org.gephi.viz.engine.util.actions;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.graph.api.Rect2D;
import org.gephi.viz.engine.VizEngine;
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

    public void selectNodesWithinRadius(float x,float y,float radius) {
        final NodeIterable iterable = engine.getGraphIndex().getNodesInsideCircle(x,y,radius);
        selectNodes(iterable);
    }
    public void selectNodesAndEdgesOnRectangle(final Rect2D rectangle) {
        final NodeIterable iterable = engine.getGraphIndex().getNodesInsideRectangle(rectangle);

        selectNodes(iterable);
    }

    public void selectNodesAndEdgesUnderPosition(Vector2f worldCoords) {
        final NodeIterable iterable = engine.getGraphIndex().getNodesUnderPosition(worldCoords.x, worldCoords.y);

        selectNodes(iterable);
    }

    public void clearSelection() {
        engine.getGraphSelection().clearSelectedNodes();
        engine.getGraphSelection().clearSelectedEdges();
    }

    private void selectNodes(final NodeIterable nodesIterable) {
        GraphSelection.GraphSelectionMode mode = engine.getGraphSelection().getMode();
        final GraphRenderingOptions renderingOptions = engine.getRenderingOptions();
        final Graph graph = engine.getGraphModel().getGraphVisible();
        final GraphSelection selection = engine.getGraphSelection();

        final Iterator<Node> iterator = nodesIterable.iterator();
        final Set<Node> selectionNodes = new HashSet<>();
        final Set<Node> selectionNeighbours = new HashSet<>();
        final Set<Edge> selectionEdges = new HashSet<>();

        final boolean selectNeighbours = renderingOptions.isAutoSelectNeighbours() &&
            mode != GraphSelection.GraphSelectionMode.SINGLE_NODE_SELECTION;
        try {
            while (iterator.hasNext()) {
                final Node node = iterator.next();

                selectionNodes.add(node);
                Collection<Edge> edges = graph.getEdges(node).toCollection();
                selectionEdges.addAll(edges);
                if (selectNeighbours) {
                    for (Edge edge : edges) {
                        Node oppositeNode = graph.getOpposite(node, edge);
                        if (oppositeNode != null && oppositeNode != node) {
                            selectionNeighbours.add(oppositeNode);
                        }
                    }
                }
            }

            selection.setSelectedNodes(selectionNodes, selectionNeighbours);
            selection.setSelectedEdges(selectionEdges);
        } finally {
            if (iterator.hasNext()) {
                nodesIterable.doBreak();
            }
        }
    }

    public void processCameraMoveEvent(int xDiff, int yDiff) {
        float zoom = engine.getZoom();

        engine.translate(xDiff / zoom, -yDiff / zoom);
    }

    public void processZoomEvent(double zoomQuantity, int x, int y) {
        final float currentZoom = engine.getZoom();
        float newZoom = currentZoom;

        newZoom *= Math.pow(1.1, zoomQuantity);
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
