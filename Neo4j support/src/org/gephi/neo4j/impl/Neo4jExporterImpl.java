package org.gephi.neo4j.impl;


import java.io.File;
import org.gephi.neo4j.api.Neo4jExporter;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.lookup.ServiceProvider;


@ServiceProvider(service=Neo4jExporter.class)
public class Neo4jExporterImpl implements Neo4jExporter, LongTask {

    public void exportLocal(File neo4jDirectory) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void exportRemote(String resourceURI) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void exportRemote(String resourceURI, String login, String password) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean cancel() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setProgressTicket(ProgressTicket progressTicket) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
