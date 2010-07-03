package org.gephi.neo4j.api;


import java.util.Collection;
import org.neo4j.graphdb.GraphDatabaseService;


/**
 * Imports data from local or remote Neo4j database.
 *
 * @author Martin Škurla
 */
public interface Neo4jImporter {
    /**
     * Imports data from local Neo4j database.
     *
     * @param neo4jDirectory Neo4j directory
     */

    void importDatabase(GraphDatabaseService graphDB);

    void importDatabase(GraphDatabaseService graphDB, long startNodeId, TraversalOrder order, int maxDepth);

    void importDatabase(GraphDatabaseService graphDB, long startNodeId, TraversalOrder order, int maxDepth,
            Collection<RelationshipInfo> relationshipInfos);

    void importDatabase(GraphDatabaseService graphDB, long startNodeId, TraversalOrder order, int maxDepth,
            Collection<RelationshipInfo> relationshipInfos, Collection<FilterInfo> filterInfos);
}
