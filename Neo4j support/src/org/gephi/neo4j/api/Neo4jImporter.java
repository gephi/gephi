package org.gephi.neo4j.api;


import java.io.File;


public interface Neo4jImporter {
    void importLocal(File neo4jDirectory);
    void importRemote(String resourceURI);
    void importRemote(String resourceURI, String login, String password);
}
