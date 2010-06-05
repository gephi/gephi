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

        public static org.gephi.graph.api.Node getGephiNodeFromNeoNode(org.neo4j.graphdb.Node neoNode) {
            Integer gephiNodeId = idMapper.get(neoNode.getId());

            org.gephi.graph.api.Node gephiNode;
            if (gephiNodeId != null)
                gephiNode = graphModel.getGraph().getNode(gephiNodeId);
            else {
                gephiNode = graphModel.factory().newNode();
                graphModel.getGraph().addNode(gephiNode);

                idMapper.put(neoNode.getId(), gephiNode.getId());
            }

            for (String neoPropertyKey : neoNode.getPropertyKeys()) {
                Object neoPropertyValue = neoNode.getProperty(neoPropertyKey);

                gephiNode.getNodeData().setLabel(neoPropertyValue.toString());
            }
            if (gephiNode.getNodeData().getLabel() == null)
                gephiNode.getNodeData().setLabel("<START NODE>");

            return gephiNode;
        }

        public static void createGephiEdge(org.gephi.graph.api.Node startGephiNode,
                                           org.gephi.graph.api.Node endGephiNode,
                                           Relationship neoRelationship) {
            Edge gephiEdge;

            gephiEdge = graphModel.factory().newEdge(startGephiNode, endGephiNode);
            graphModel.getGraph().addEdge(gephiEdge);

            gephiEdge.getEdgeData().setLabel(neoRelationship.getType().name());
        }
    }
}
