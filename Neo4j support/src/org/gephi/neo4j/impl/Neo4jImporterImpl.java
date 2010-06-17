package org.gephi.neo4j.impl;


import java.io.File;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import org.gephi.neo4j.api.Neo4jImporter;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.ProgressTicket;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.kernel.management.PrimitiveMBean;
import org.neo4j.remote.RemoteGraphDatabase;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;


@ServiceProvider(service=Neo4jImporter.class)
public final class Neo4jImporterImpl implements Neo4jImporter, LongTask {
    private GraphDatabaseService graphDB;
    private GraphModelConvertor graphModelConvertor;
    
    private ProgressTicket progressTicket;
    private boolean cancelImport;//TODO finish implementing canceling task


    @Override
    public boolean cancel() {
        cancelImport = true;
        return true;
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        cancelImport = false;
        this.progressTicket = progressTicket;
    }

    @Override
    public void importLocal(File neo4jDirectory) {
        progressTicket.setDisplayName("Importing data from local Neo4j database");
        progressTicket.start();

        graphDB = new EmbeddedGraphDatabase(neo4jDirectory.getAbsolutePath());
        doImport();
    }

    @Override
    public void importRemote(String resourceURI) {
        importRemote(resourceURI, null, null);
    }

    @Override
    public void importRemote(String resourceURI, String login, String password) {
        progressTicket.setDisplayName("Importing data from remote Neo4j database");
        progressTicket.start();

        try {
            if (login == null && password == null)
                graphDB = new RemoteGraphDatabase(resourceURI);
            else
                graphDB = new RemoteGraphDatabase(resourceURI, login, password);

            doImport();
        }
        catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void doImport() {
        Transaction transaction = graphDB.beginTx();
        try {
            importGraph();
            transaction.success();
        }
        finally {
            transaction.finish();
        }

        graphDB.shutdown();
        progressTicket.finish();
    }

    private void importGraph() {
//        TraversalDescription traversalDescription = TraversalFactory.createTraversalDescription();
//        RelationshipExpander relationshipExpander = TraversalFactory.expanderForAllTypes();
//
//        org.neo4j.graphdb.Node referenceNode = graphDB.getReferenceNode();

//        Traverser traverser =
//            traversalDescription.depthFirst()
//                                .expand(relationshipExpander)
//                                .filter(ReturnFilter.ALL)
//                                .prune(PruneEvaluator.NONE)
//                                .traverse(referenceNode);

        createNewProject();
        //TODO solve problem with projects and workspace, how to treat canceling task?

        graphModelConvertor = GraphModelConvertor.getInstance(graphDB);
        // the algorithm traverses through all relationships
//        for (Relationship neoRelationship : traverser.relationships()) {
//            if (cancelImport)
//                break;
//
//            processRelationship(neoRelationship);
//        }

        EmbeddedGraphDatabase embeddedGraphDatabase = (EmbeddedGraphDatabase) graphDB;
        PrimitiveMBean primitives = embeddedGraphDatabase.getManagementBean(PrimitiveMBean.class);

        Set<Long> processedRelationships = new HashSet<Long>((int) primitives.getNumberOfRelationshipIdsInUse());
        for (org.neo4j.graphdb.Node node : graphDB.getAllNodes()) {
            for (Relationship relationship : node.getRelationships()) {
                if (!processedRelationships.contains(relationship.getId())) {
                    processedRelationships.add(relationship.getId());
                    processRelationship(relationship);
                }
            }
        }

        if (!cancelImport)
            showCurrentWorkspace();
    }

    private void createNewProject() {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
    }

    private void showCurrentWorkspace() {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);

        Workspace workspace = pc.getCurrentWorkspace();
        if (workspace == null)
            workspace = pc.newWorkspace(pc.getCurrentProject());
        pc.openWorkspace(workspace);
    }

    private void processRelationship(Relationship neoRelationship) {
        org.gephi.graph.api.Node gephiStartNode =
                graphModelConvertor.createGephiNodeFromNeoNode(neoRelationship.getStartNode());
        org.gephi.graph.api.Node gephiEndNode =
                graphModelConvertor.createGephiNodeFromNeoNode(neoRelationship.getEndNode());

        graphModelConvertor.createGephiEdge(gephiStartNode, gephiEndNode, neoRelationship);
    }
}
