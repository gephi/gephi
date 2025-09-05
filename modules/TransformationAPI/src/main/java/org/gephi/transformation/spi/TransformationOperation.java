package org.gephi.transformation.spi;

import org.gephi.graph.api.Graph;

public interface TransformationOperation {
    void transformation(Graph graph);

    default void apply(Graph graph) {
        graph.writeLock();
        try {
            this.transformation(graph);
        } catch (Exception e) {
            graph.writeUnlock();
            throw new RuntimeException(e);
        } finally {
            graph.writeUnlock();
        }
    }
}
