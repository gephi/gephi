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

    void getVisibleNodes(ElementsCallback<Node> callback, GraphRenderingOptions graphRenderingOptions,
                         Rect2D viewBoundaries);

    void getVisibleEdges(ElementsCallback<Edge> callback, GraphRenderingOptions graphRenderingOptions,
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
