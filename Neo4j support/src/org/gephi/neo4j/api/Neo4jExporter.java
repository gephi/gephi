package org.gephi.neo4j.api;


import org.neo4j.graphdb.GraphDatabaseService;


/**
 * Exports data to local or remote Neo4j database.
 *
 * @author Martin Škurla
 */
public interface Neo4jExporter {

    void exportDatabase(GraphDatabaseService graphDB);
}
