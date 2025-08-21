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
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.util.EdgeIterableFilteredWrapper;
import org.gephi.viz.engine.util.NodeIterableFilteredWrapper;
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
    private final Column edgeWeightColumn;

    public GraphIndexImpl(GraphModel graphModel) {
        this.graphModel = graphModel;
        this.edgeWeightColumn = graphModel.getEdgeTable().getColumn("weight"); //Default weight column
    }

    public Graph getVisibleGraph() {
        return graphModel.getGraphVisible();
    }


//    public void indexEdges() {
//        final Graph visibleGraph = getVisibleGraph();
//        visibleGraph.readLock();
//        if (visibleGraph.getEdgeCount() > 0) {
//            edgesMinWeight =
//                graphModel.getEdgeIndex(graphModel.getVisibleView()).getMinValue(edgeWeightColumn).floatValue();
//            edgesMaxWeight =
//                graphModel.getEdgeIndex(graphModel.getVisibleView()).getMaxValue(edgeWeightColumn).floatValue();
//        } else {
//            edgesMinWeight = edgesMaxWeight = 1;
//        }
//        visibleGraph.readUnlock();
//    }

    @Override
    public int getNodeCount() {
        return getVisibleGraph().getNodeCount();
    }

    @Override
    public int getEdgeCount() {
        return getVisibleGraph().getEdgeCount();
    }

    @Override
    public float getEdgesMinWeight() {
        Number minValue = graphModel.getEdgeIndex(getVisibleGraph().getView()).getMinValue(edgeWeightColumn);
        return minValue != null ? minValue.floatValue() : 1f;
    }

    @Override
    public float getEdgesMaxWeight() {
        Number maxValue = graphModel.getEdgeIndex(getVisibleGraph().getView()).getMaxValue(edgeWeightColumn);
        return maxValue != null ? maxValue.floatValue() : 1f;
    }

    @Override
    public void getVisibleNodes(ElementsCallback<Node> callback, Rect2D viewBoundaries) {
        callback.start(getVisibleGraph());

        final NodeIterable nodeIterable = getVisibleGraph().getSpatialIndex().getNodesInArea(viewBoundaries);
        try {
            for (Node node : nodeIterable) {
                callback.accept(node);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            nodeIterable.doBreak();
        }

        callback.end(getVisibleGraph());
    }

    @Override
    public void getVisibleEdges(ElementsCallback<Edge> callback, Rect2D viewBoundaries) {
        final Graph visibleGraph = getVisibleGraph();
        callback.start(visibleGraph);
        final EdgeIterable edgeIterable = visibleGraph.getSpatialIndex().getEdgesInArea(viewBoundaries);
        try {
            for (Edge edge : edgeIterable) {
                callback.accept(edge);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            edgeIterable.doBreak();
        }

        callback.end(visibleGraph);
    }

    @Override
    public NodeIterable getNodesUnderPosition(float x, float y) {
        return filterNodeIterable(getVisibleGraph().getSpatialIndex().getNodesInArea(getCircleRect2D(x, y, 0)),
            node -> {
                final float size = node.size();

                return Intersectionf.testPointCircle(x, y, node.x(), node.y(), size * size);
            });
    }

    @Override
    public NodeIterable getNodesInsideCircle(float centerX, float centerY, float radius) {
        return filterNodeIterable(
            getVisibleGraph().getSpatialIndex().getNodesInArea(getCircleRect2D(centerX, centerY, radius)), node -> {
                return Intersectionf.testCircleCircle(centerX, centerY, radius, node.x(), node.y(), node.size());
            });
    }

    @Override
    public NodeIterable getNodesInsideRectangle(Rect2D rect) {
        return filterNodeIterable(getVisibleGraph().getSpatialIndex().getNodesInArea(rect), node -> {
            final float size = node.size();

            return Intersectionf.testAarCircle(rect.minX, rect.minY, rect.maxX, rect.maxY, node.x(), node.y(),
                size * size);
        });
    }

    @Override
    public EdgeIterable getEdgesInsideRectangle(Rect2D rect) {
        return filterEdgeIterable(getVisibleGraph().getSpatialIndex().getEdgesInArea(rect), edge -> {
            final Node source = edge.getSource();
            final Node target = edge.getTarget();

            //TODO: take width into account!
            return Intersectionf.testAarLine(rect.minX, rect.minY, rect.maxX, rect.maxY, source.x(), source.y(),
                target.x(), target.y());
        });
    }

    @Override
    public EdgeIterable getEdgesInsideCircle(float centerX, float centerY, float radius) {
        return filterEdgeIterable(
            getVisibleGraph().getSpatialIndex().getEdgesInArea(getCircleRect2D(centerX, centerY, radius)), edge -> {
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

    private NodeIterable filterNodeIterable(NodeIterable nodesIterable, Predicate<Node> predicate) {
        return new NodeIterableFilteredWrapper(nodesIterable, predicate);
    }

    private EdgeIterable filterEdgeIterable(EdgeIterable edgesIterable, Predicate<Edge> predicate) {
        return new EdgeIterableFilteredWrapper(edgesIterable, predicate);
    }
}
