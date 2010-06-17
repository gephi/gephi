package org.gephi.neo4j.impl;


import java.io.File;
import java.net.URISyntaxException;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.neo4j.api.Neo4jExporter;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.ProgressTicket;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.remote.RemoteGraphDatabase;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;


/**
 *
 * @author Martin Škurla
 */
@ServiceProvider(service=Neo4jExporter.class)
public class Neo4jExporterImpl implements Neo4jExporter, LongTask {
    private GraphDatabaseService graphDB;
    private GraphModelConvertor graphModelConvertor;

    private ProgressTicket progressTicket;
    private boolean cancelExport;


    @Override
    public boolean cancel() {
        cancelExport = true;
        return true;
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        cancelExport = false;
        this.progressTicket = progressTicket;
    }

    public void exportLocal(File neo4jDirectory) {
        progressTicket.setDisplayName("Exporting data to local Neo4j database");
        progressTicket.start();

        graphDB = new EmbeddedGraphDatabase(neo4jDirectory.getAbsolutePath());
        doExport();
    }

    public void exportRemote(String resourceURI) {
        exportRemote(resourceURI, null, null);
    }

    public void exportRemote(String resourceURI, String login, String password) {
        progressTicket.setDisplayName("Exporting data to remote Neo4j database");
        progressTicket.start();

        try {
            if (login == null && password == null)
                graphDB = new RemoteGraphDatabase(resourceURI);
            else
                graphDB = new RemoteGraphDatabase(resourceURI, login, password);

            doExport();
        }
        catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void doExport() {
        Transaction transaction = graphDB.beginTx();
        try {
            exportGraph();

            if (!cancelExport)
                transaction.success();
        }
        finally {
            transaction.finish();
        }

        graphDB.shutdown();
        progressTicket.finish();
    }

    private void exportGraph() {
        graphModelConvertor = GraphModelConvertor.getInstance(graphDB);
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();

        // the algorithm traverses through all edges
        for (Edge gephiEdge : graphModel.getGraph().getEdges()) {
            org.neo4j.graphdb.Node neoStartNode =
                    graphModelConvertor.createNeoNodeFromGephiNode(gephiEdge.getSource());
            org.neo4j.graphdb.Node neoEndNode =
                    graphModelConvertor.createNeoNodeFromGephiNode(gephiEdge.getTarget());

            graphModelConvertor.createNeoRelationship(neoStartNode, neoEndNode, gephiEdge);
        }
    }
}
