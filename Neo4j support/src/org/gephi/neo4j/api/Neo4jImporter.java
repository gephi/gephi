package org.gephi.neo4j.api;


import java.io.File;


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
    void importLocal(File neo4jDirectory);

    /**
     * Imports data from remote Neo4j database.
     * 
     * @param resourceURI URI of the remote Neo4j database
     */
    void importRemote(String resourceURI);

    /**
     * Imports data from remote Neo4j database.
     * 
     * @param resourceURI URI of the remote Neo4j database
     * @param login       login
     * @param password    password
     */
    void importRemote(String resourceURI, String login, String password);
}
