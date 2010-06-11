package org.gephi.neo4j.api;


import java.io.File;


/**
 * Exports data to local or remote Neo4j database.
 *
 * @author Martin Škurla
 */
public interface Neo4jExporter {
    /**
     * Exports data to local Neo4j database.
     *
     * @param neo4jDirectory Neo4j directory
     */
    void exportLocal(File neo4jDirectory);

    /**
     * Exports data to remote Neo4j database.
     *
     * @param resourceURI URI of the remote Neo4j database
     */
    void exportRemote(String resourceURI);

    /**
     * Exports data to remote Neo4j database.
     *
     * @param resourceURI URI of the remote Neo4j database
     * @param login       login
     * @param password    password
     */
    void exportRemote(String resourceURI, String login, String password);
}
