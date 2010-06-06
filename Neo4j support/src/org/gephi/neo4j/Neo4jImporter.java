package org.gephi.neo4j;


import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipExpander;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.traversal.PruneEvaluator;
import org.neo4j.graphdb.traversal.ReturnFilter;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.kernel.TraversalFactory;
import org.openide.util.Lookup;


public final class Neo4jImporter {
    private static GraphDatabaseService graphDB;
    private static Map<Long, Integer> idMapper;
    private static GraphModel graphModel;

    private Neo4jImporter() {}

    public static void importLocal(File neo4jDirectory) {
        graphDB = new EmbeddedGraphDatabase(neo4jDirectory.getAbsolutePath());
        idMapper = new HashMap<Long, Integer>();

        Transaction transaction = graphDB.beginTx();

        try {
            importWholeGraph();
            transaction.success();
        }
        finally {
            transaction.finish();
        }

        graphDB.shutdown();
    }

    private static void importWholeGraph() {
        TraversalDescription traversalDescription = TraversalFactory.createTraversalDescription();
        RelationshipExpander relationshipExpander = TraversalFactory.expanderForAllTypes();

        org.neo4j.graphdb.Node startNode = graphDB.getReferenceNode();

        Traverser traverser = 
            traversalDescription.depthFirst()
                                .expand(relationshipExpander)
                                .filter(ReturnFilter.ALL)
                                .prune(PruneEvaluator.NONE)
                                .traverse(startNode);

        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        graphModel = graphController.getModel();
        graphModel.clear();
        graphModel.getGraph().clear();

        //createNewProject();

        // the algorithm traverses through all relationships
        for (Relationship neoRelationship : traverser.relationships())
            processRelationship(neoRelationship);

        showCurrentWorkspace();
    }

//    private static void createNewProject() {
//        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
//        pc.newProject();
//    }

    private static void showCurrentWorkspace() {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);

        Workspace workspace = pc.getCurrentWorkspace();
        if (workspace == null)
        //pc.newProject();
            workspace = pc.newWorkspace(pc.getCurrentProject());
        pc.openWorkspace(workspace);
    }

    private static void processRelationship(Relationship neoRelationship) {
        org.gephi.graph.api.Node gephiStartNode =
                GephiHelper.getGephiNodeFromNeoNode(neoRelationship.getStartNode());
        org.gephi.graph.api.Node gephiEndNode =
                GephiHelper.getGephiNodeFromNeoNode(neoRelationship.getEndNode());

        GephiHelper.createGephiEdge(gephiStartNode, gephiEndNode, neoRelationship);
    }

    private static class GephiHelper {
        private GephiHelper() {}

        /**
         * Creates Gephi node representation from Neo4j node and all its property data. If Gephi node
         * doesn't already exist, it will be created with all attached data from Neo4j node, otherwise
         * it will not be created again.
         *
         * @param neoNode Neo4j node
         * @return Gephi node
         */
        public static org.gephi.graph.api.Node getGephiNodeFromNeoNode(org.neo4j.graphdb.Node neoNode) {
            Integer gephiNodeId = idMapper.get(neoNode.getId());

            org.gephi.graph.api.Node gephiNode;
            if (gephiNodeId != null)
                gephiNode = graphModel.getGraph().getNode(gephiNodeId);
            else {
                gephiNode = graphModel.factory().newNode();
                graphModel.getGraph().addNode(gephiNode);

                for (String neoPropertyKey : neoNode.getPropertyKeys()) {
                    Object neoPropertyValue = neoNode.getProperty(neoPropertyKey);
                    // property keys and values don't have to be stored, because delegate mechanism
                    // will query all required data directly from Neo4j database

                    // only for testing purposes
                    gephiNode.getNodeData().setLabel(neoPropertyValue.toString());
                }

                idMapper.put(neoNode.getId(), gephiNode.getId());
            }

            // only for testing purposes
            if (gephiNode.getNodeData().getLabel() == null)
                gephiNode.getNodeData().setLabel("<START NODE>");

            return gephiNode;
        }

        /**
         * Creates Gephi edge betweeen two Gephi nodes. Graph is traversing through all relationships
         * (edges), so for every Neo4j relationship a Gephi edge will be created.
         *
         * @param startGephiNode  start Gephi node
         * @param endGephiNode    end Gephi node
         * @param neoRelationship Neo4j relationship
         */
        public static void createGephiEdge(org.gephi.graph.api.Node startGephiNode,
                                           org.gephi.graph.api.Node endGephiNode,
                                           Relationship neoRelationship) {
            Edge gephiEdge = graphModel.factory().newEdge(startGephiNode, endGephiNode);
            graphModel.getGraph().addEdge(gephiEdge);

            // property keys and values don't have to be stored, because delegate mechanism will query
            // all required data directly from Neo4j database

            // only for testing purposes
            gephiEdge.getEdgeData().setLabel(neoRelationship.getType().name());
        }
    }
}
