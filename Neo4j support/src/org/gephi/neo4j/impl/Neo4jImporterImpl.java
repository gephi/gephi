package org.gephi.neo4j.impl;


import java.util.Collection;
import java.util.Collections;
import org.gephi.neo4j.api.FilterInfo;
import org.gephi.neo4j.api.Neo4jImporter;
import org.gephi.neo4j.api.RelationshipInfo;
import org.gephi.neo4j.api.TraversalOrder;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.ProgressTicket;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.traversal.PruneEvaluator;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.kernel.TraversalFactory;
import org.neo4j.remote.RemoteGraphDatabase;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;


@ServiceProvider(service=Neo4jImporter.class)
public final class Neo4jImporterImpl implements Neo4jImporter, LongTask {
    private GraphDatabaseService graphDB;
    private GraphModelConvertor graphModelConvertor;
    private Traverser traverser;
    
    private ProgressTicket progressTicket;
    private boolean cancelImport;


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
    public void importDatabase(GraphDatabaseService graphDB) {
        importDatabase(graphDB, -1, TraversalOrder.DEPTH_FIRST, Integer.MAX_VALUE);
    }

    @Override
    public void importDatabase(GraphDatabaseService graphDB, long startNodeId, TraversalOrder order, int maxDepth) {
        importDatabase(graphDB, startNodeId, order, maxDepth, Collections.<RelationshipInfo>emptyList());
    }

    public void importDatabase(GraphDatabaseService graphDB, long startNodeId, TraversalOrder order, int maxDepth,
            Collection<RelationshipInfo> relationshipInfos) {
        importDatabase(graphDB, startNodeId, order, maxDepth, relationshipInfos, Collections.<FilterInfo>emptyList());
    }

    public void importDatabase(GraphDatabaseService graphDB, long startNodeId, TraversalOrder order, int maxDepth, Collection<RelationshipInfo> relationshipInfos, Collection<FilterInfo> filterInfos) {
        this.graphDB = graphDB;

        String longTaskMessage = (graphDB instanceof RemoteGraphDatabase)
                ? NbBundle.getMessage(Neo4jImporterImpl.class, "CTL_Neo4j_RemoteImportMessage")
                : NbBundle.getMessage(Neo4jImporterImpl.class, "CTL_Neo4j_LocalImportMessage");

        progressTicket.setDisplayName(longTaskMessage);
        progressTicket.start();

        if (startNodeId != -1) {
            TraversalDescription traversalDescription = TraversalFactory.createTraversalDescription();
            PruneEvaluator pruneEvaluator = TraversalFactory.pruneAfterDepth(maxDepth);

            traversalDescription = order.update(traversalDescription);

            for (RelationshipInfo relationshipInfo : relationshipInfos)
                traversalDescription = traversalDescription.relationships(relationshipInfo.getRelationshipType(),
                                                                          relationshipInfo.getDirection());

            //TODO create ReturnFilter, add support for array parsing, validation

            traverser = traversalDescription.prune(pruneEvaluator)
                                            .traverse(graphDB.getNodeById(startNodeId));
        }
        else
             traverser = null;

        doImport();
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

        progressTicket.finish();
    }

    private void importGraph() {
        Neo4jDelegateProviderImpl.setGraphDB(graphDB);
        createNewProject();

        graphModelConvertor = GraphModelConvertor.getInstance(graphDB);

        if (traverser == null) {
            for (org.neo4j.graphdb.Node node : graphDB.getAllNodes()) {
                for (Relationship relationship : node.getRelationships(Direction.INCOMING)) {
                    if (cancelImport)
                        return;

                    processRelationship(relationship);
                }
            }
        }
        else {
            for (Relationship relationship : traverser.relationships()) {
                if (cancelImport)
                    return;

                processRelationship(relationship);
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
