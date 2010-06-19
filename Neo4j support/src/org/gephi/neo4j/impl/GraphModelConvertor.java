package org.gephi.neo4j.impl;


import java.util.HashMap;
import java.util.Map;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.api.AttributeValue;
import org.gephi.graph.api.Attributes;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Relationship;
import org.openide.util.Lookup;


/**
 *
 * @author Martin Škurla
 */
public class GraphModelConvertor {

    private static GraphModelConvertor singleton;
    private static GraphModel graphModel;
    private static AttributeModel attributeModel;

    private static GraphDatabaseService graphDB;
    private static Map<Long, Integer> neoToGephiIdMapper;
    private static Map<Integer, Long> gephiToNeoIdMapper;

    private GraphModelConvertor() {
    }

    public static GraphModelConvertor getInstance(GraphDatabaseService graphDB) {
        if (singleton == null) {
            singleton = new GraphModelConvertor();
            
            GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
            graphModel = graphController.getModel();

            AttributeController attributeController = Lookup.getDefault().lookup(AttributeController.class);
            attributeModel = attributeController.getModel();
        }

        GraphModelConvertor.graphDB = graphDB;

        neoToGephiIdMapper = new HashMap<Long, Integer>();
        gephiToNeoIdMapper = new HashMap<Integer, Long>();

        return singleton;
    }

    /**
     * Creates Gephi node representation from Neo4j node and all its property data. If Gephi node
     * doesn't already exist, it will be created with all attached data from Neo4j node, otherwise
     * it will not be created again.
     *
     * @param neoNode Neo4j node
     * @return Gephi node
     */
    public org.gephi.graph.api.Node createGephiNodeFromNeoNode(org.neo4j.graphdb.Node neoNode) {
        Integer gephiNodeId = neoToGephiIdMapper.get(neoNode.getId());

        org.gephi.graph.api.Node gephiNode;
        if (gephiNodeId != null) {
            gephiNode = graphModel.getGraph().getNode(gephiNodeId);
        } else {
            gephiNode = graphModel.factory().newNode();
            graphModel.getGraph().addNode(gephiNode);

            fillGephiNodeDataWithNeoNodeData(gephiNode, neoNode);

            neoToGephiIdMapper.put(neoNode.getId(), gephiNode.getId());
        }

        return gephiNode;
    }

    private void fillGephiNodeDataWithNeoNodeData(org.gephi.graph.api.Node gephiNode, org.neo4j.graphdb.Node neoNode) {
        AttributeTable nodeTable = attributeModel.getNodeTable();
        Attributes attributes = gephiNode.getNodeData().getAttributes();

        Object neoNodeId = neoNode.getId();
        for (String neoPropertyKey : neoNode.getPropertyKeys()) {
            Object neoPropertyValue = neoNode.getProperty(neoPropertyKey);

            if (!nodeTable.hasColumn(neoPropertyKey))
                nodeTable.addColumn(neoPropertyKey, neoPropertyKey, AttributeType.parse(neoPropertyValue), AttributeOrigin.DELEGATE, null, Neo4jDelegateProviderImpl.getInstance());

            if (nodeTable.getColumn(neoPropertyKey).getOrigin() == AttributeOrigin.DELEGATE)
                attributes.setValue(neoPropertyKey, neoNodeId);
            else
                attributes.setValue(neoPropertyKey, neoPropertyValue);
        }
    }

    /**
     * Creates Gephi edge betweeen two Gephi nodes. Graph is traversing through all relationships
     * (edges), so for every Neo4j relationship a Gephi edge will be created.
     *
     * @param startGephiNode  start Gephi node
     * @param endGephiNode    end Gephi node
     * @param neoRelationship Neo4j relationship
     */
    public void createGephiEdge(org.gephi.graph.api.Node startGephiNode,
            org.gephi.graph.api.Node endGephiNode, Relationship neoRelationship) {
        Edge gephiEdge = graphModel.factory().newEdge(startGephiNode, endGephiNode);
        graphModel.getGraph().addEdge(gephiEdge);

        fillGephiEdgeDataWithNeoRelationshipData(gephiEdge, neoRelationship);
    }

    private void fillGephiEdgeDataWithNeoRelationshipData(Edge gephiEdge, Relationship neoRelationship) {
        AttributeTable edgeTable = attributeModel.getEdgeTable();
        Attributes attributes = gephiEdge.getEdgeData().getAttributes();

        Object neoRelationshipId = neoRelationship.getId();
        for (String neoPropertyKey : neoRelationship.getPropertyKeys()) {
            Object neoPropertyValue = neoRelationship.getProperty(neoPropertyKey);

            if (!edgeTable.hasColumn(neoPropertyKey))
                edgeTable.addColumn(neoPropertyKey, neoPropertyKey, AttributeType.parse(neoPropertyValue), AttributeOrigin.DELEGATE, null, Neo4jDelegateProviderImpl.getInstance());

            if (edgeTable.getColumn(neoPropertyKey).getOrigin() == AttributeOrigin.DELEGATE)
                attributes.setValue(neoPropertyKey, neoRelationshipId);
            else
                attributes.setValue(neoPropertyKey, neoPropertyValue);
        }
    }

    public org.neo4j.graphdb.Node createNeoNodeFromGephiNode(org.gephi.graph.api.Node gephiNode) {
        Long neoNodeId = gephiToNeoIdMapper.get(gephiNode.getId());

        org.neo4j.graphdb.Node neoNode;
        if (neoNodeId != null)
            return graphDB.getNodeById(neoNodeId);
        else {
            neoNode = graphDB.createNode();
            fillNeoNodeDataFromGephiNodeData(neoNode, gephiNode);

            gephiToNeoIdMapper.put(gephiNode.getId(), neoNode.getId());
        }
        
        return neoNode;
    }

    private void fillNeoNodeDataFromGephiNodeData(org.neo4j.graphdb.Node neoNode, org.gephi.graph.api.Node gephiNode) {
        Attributes attributes = gephiNode.getNodeData().getAttributes();

        for (AttributeValue attributeValue : ((AttributeRow)attributes).getValues())
            neoNode.setProperty(attributeValue.getColumn().getId(), attributeValue.getValue());
    }

    public void createNeoRelationship(org.neo4j.graphdb.Node startNeoNode, org.neo4j.graphdb.Node endNeoNode, Edge gephiEdge) {
        Relationship neoRelationship =
                startNeoNode.createRelationshipTo(endNeoNode, DynamicRelationshipType.withName(""));

        fillNeoRelationshipDataFromGephiEdgeData(neoRelationship, gephiEdge);
    }

    private void fillNeoRelationshipDataFromGephiEdgeData(Relationship neoRelationship, Edge gephiEdge) {
        Attributes attributes = gephiEdge.getEdgeData().getAttributes();

        for (AttributeValue attributeValue : ((AttributeRow)attributes).getValues()) {
            if (attributeValue.getValue() != null)
                neoRelationship.setProperty(attributeValue.getColumn().getId(), attributeValue.getValue());
        }
    }
}
