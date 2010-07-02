package org.gephi.neo4j.api;


import org.neo4j.graphdb.traversal.TraversalDescription;


/**
 *
 * @author Martin Škurla
 */
public enum TraversalOrder {
    DEPTH_FIRST {
        @Override
        public TraversalDescription update(TraversalDescription traversalDescription) {
            return traversalDescription.depthFirst();
        }
    },
    BREADTH_FIRST {
        @Override
        public TraversalDescription update(TraversalDescription traversalDescription) {
            return traversalDescription.breadthFirst();
        }
    };

    public abstract TraversalDescription update(TraversalDescription traversalDescription);
}
