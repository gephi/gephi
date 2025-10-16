package org.gephi.viz.engine.structure;

import java.util.function.Predicate;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.graph.api.Rect2D;
import org.gephi.graph.api.SpatialIndex;
import org.joml.Intersectionf;

/**
 * <p>
 * TODO: make intersection functions customizable for different shape handling</p>
 * <p>
 *
 * @author Eduardo Ramos
 */
public class GraphIndexImpl implements GraphIndex {

    private final GraphModel graphModel;

    public GraphIndexImpl(GraphModel graphModel) {
        this.graphModel = graphModel;
    }

    public Graph getVisibleGraph() {
        return graphModel.getGraphVisible();
    }

    @Override
    public int getNodeCount() {
        return getVisibleGraph().getNodeCount();
    }

    @Override
    public int getEdgeCount() {
        return getVisibleGraph().getEdgeCount();
    }

    @Override
    public void getVisibleNodes(ElementsCallback<Node> callback, Rect2D viewBoundaries) {
        graphModel.getGraph().readLock();
        final Graph visibleGraph = getVisibleGraph();
        callback.start(visibleGraph);

        SpatialIndex spatialIndex = visibleGraph.getSpatialIndex();
        spatialIndex.spatialIndexReadLock();
        spatialIndex.getApproximateNodesInArea(viewBoundaries).parallelStream().forEach(
            callback);
        spatialIndex.spatialIndexReadUnlock();

        callback.end(visibleGraph);
        graphModel.getGraph().readUnlock();
    }

    @Override
    public void getVisibleEdges(ElementsCallback<Edge> callback, Rect2D viewBoundaries) {
        graphModel.getGraph().readLock();
        final Graph visibleGraph = getVisibleGraph();
        callback.start(visibleGraph);
        SpatialIndex spatialIndex = visibleGraph.getSpatialIndex();
        spatialIndex.spatialIndexReadLock();
        spatialIndex.getApproximateEdgesInArea(viewBoundaries).parallelStream().forEach(
            callback);
        spatialIndex.spatialIndexReadUnlock();

        callback.end(visibleGraph);
        graphModel.getGraph().readUnlock();
    }

    @Override
    public NodeIterable getNodesUnderPosition(float x, float y) {
        return getVisibleGraph().getSpatialIndex().getNodesInArea(getCircleRect2D(x, y, 0),
            node -> {
                final float size = node.size();

                return Intersectionf.testPointCircle(x, y, node.x(), node.y(), size * size);
            });
    }

    @Override
    public NodeIterable getNodesInsideCircle(float centerX, float centerY, float radius) {
        return getVisibleGraph().getSpatialIndex().getNodesInArea(getCircleRect2D(centerX, centerY, radius), node -> Intersectionf.testCircleCircle(centerX, centerY, radius, node.x(), node.y(), node.size()));
    }

    @Override
    public NodeIterable getNodesInsideRectangle(Rect2D rect) {
        return getVisibleGraph().getSpatialIndex().getNodesInArea(rect, node -> {
            final float size = node.size();

            return Intersectionf.testAarCircle(rect.minX, rect.minY, rect.maxX, rect.maxY, node.x(), node.y(),
                size * size);
        });
    }

    @Override
    public EdgeIterable getEdgesInsideRectangle(Rect2D rect) {
        return getVisibleGraph().getSpatialIndex().getEdgesInArea(rect, edge -> {
            final Node source = edge.getSource();
            final Node target = edge.getTarget();

            //TODO: take width into account!
            return Intersectionf.testAarLine(rect.minX, rect.minY, rect.maxX, rect.maxY, source.x(), source.y(),
                target.x(), target.y());
        });
    }

    @Override
    public EdgeIterable getEdgesInsideCircle(float centerX, float centerY, float radius) {
        return getVisibleGraph().getSpatialIndex().getEdgesInArea(getCircleRect2D(centerX, centerY, radius), edge -> {
                final Node source = edge.getSource();
                final Node target = edge.getTarget();

                float x0 = source.x();
                float y0 = source.y();
                float x1 = target.x();
                float y1 = target.y();

                //TODO: take width into account!
                return Intersectionf.testLineCircle(y0 - y1, x1 - x0, (x0 - x1) * y0 + (y1 - y0) * x0, centerX, centerY,
                    radius);
            });
    }

    @Override
    public Rect2D getGraphBoundaries() {
        return getVisibleGraph().getSpatialIndex().getBoundaries();
    }

    private Rect2D getCircleRect2D(float x, float y, float radius) {
        return new Rect2D(x - radius, y - radius, x + radius, y + radius);
    }
}
