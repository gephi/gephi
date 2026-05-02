package org.gephi.viz.engine.spi;

import java.util.function.Consumer;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Rect2D;
import org.gephi.viz.engine.status.GraphRenderingOptions;
import org.gephi.viz.engine.status.GraphSelection;
import org.gephi.viz.engine.structure.GraphIndex;

public interface ElementsCallback<T> extends Consumer<T> {

    void run(GraphIndex graphIndex, GraphRenderingOptions renderingOptions, Rect2D viewBoundaries);

    /**
     * Called when going to start receiving elements
     *
     * @param graph Graph
     */
    void start(Graph graph, GraphRenderingOptions graphRenderingOptions, GraphSelection selection);

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

    /**
     * Reset to free up memory
     */
    void reset();
}
