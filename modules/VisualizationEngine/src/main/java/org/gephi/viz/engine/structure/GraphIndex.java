package org.gephi.viz.engine.structure;

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.graph.api.Rect2D;
import org.gephi.viz.engine.spi.ElementsCallback;
import org.gephi.viz.engine.status.GraphRenderingOptions;

/**
 *
 * @author Eduardo Ramos
 */
public interface GraphIndex {

    Graph getVisibleGraph();

    int getNodeCount();

    int getEdgeCount();

    Rect2D getGraphBoundaries();

    /**
     * Passes the visible nodes in the view boundaries to the callback, under the graph read lock.
     *
     * @return false when a graph writer kept the lock and the callback wasn't called
     */
    boolean getVisibleNodes(ElementsCallback<Node> callback, GraphRenderingOptions graphRenderingOptions,
                            Rect2D viewBoundaries);

    /**
     * Passes the visible edges in the view boundaries to the callback, under the graph read lock.
     *
     * @return false when a graph writer kept the lock and the callback wasn't called
     */
    boolean getVisibleEdges(ElementsCallback<Edge> callback, GraphRenderingOptions graphRenderingOptions,
                            Rect2D viewBoundaries);

    NodeIterable getNodesUnderPosition(float x, float y);

    NodeIterable getNodesUnderPosition(float x, float y, float nodeScale);

    NodeIterable getNodesInsideRectangle(Rect2D rect);

    NodeIterable getNodesInsideRectangle(Rect2D rect, float nodeScale);

    NodeIterable getNodesInsideCircle(float x, float y, float radius);

    NodeIterable getNodesInsideCircle(float x, float y, float radius, float nodeScale);

    EdgeIterable getEdgesInsideRectangle(Rect2D rect);

    EdgeIterable getEdgesInsideCircle(float x, float y, float radius);
}
