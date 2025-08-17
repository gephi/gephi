package org.gephi.viz.engine.structure;

import org.gephi.graph.api.*;

import java.util.function.Consumer;

/**
 *
 * @author Eduardo Ramos
 */
public interface GraphIndex {

    Graph getVisibleGraph();

    int getNodeCount();

    int getEdgeCount();

    float getEdgesMinWeight();

    float getEdgesMaxWeight();

    Rect2D getGraphBoundaries();

    NodeIterable getVisibleNodes();
    
    void getVisibleNodes(ElementsCallback<Node> callback);

    EdgeIterable getVisibleEdges();
    
    void getVisibleEdges(ElementsCallback<Edge> callback);

    NodeIterable getNodesUnderPosition(float x, float y);

    NodeIterable getNodesInsideRectangle(Rect2D rect);

    NodeIterable getNodesInsideCircle(float x, float y, float radius);

    EdgeIterable getEdgesInsideRectangle(Rect2D rect);

    EdgeIterable getEdgesInsideCircle(float x, float y, float radius);

    public interface ElementsCallback<T> extends Consumer<T> {

        /**
         * Called when going to start receiving elements
         *
         * @param graph Graph
         */
        void start(Graph graph);

        /**
         * Called for each element in the list
         *
         * @param element Element
         */
        @Override
        void accept(T element);

        /**
         * Called when finished receiving elements
         *
         * @param graph Graph
         */
        void end(Graph graph);
    }
}
