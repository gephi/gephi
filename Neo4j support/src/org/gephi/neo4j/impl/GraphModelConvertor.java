package org.gephi.neo4j.impl;

import java.util.HashMap;
import java.util.Map;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.graph.api.Attributes;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.neo4j.graphdb.Relationship;
import org.openide.util.Lookup;

public class GraphModelConvertor {

    private static GraphModelConvertor singleton;
    private static GraphModel graphModel;
    private static Map<Long, Integer> idMapper;

    private GraphModelConvertor() {
    }

    public static GraphModelConvertor getInstance() {
        if (singleton == null) {
            singleton = new GraphModelConvertor();
            GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
            graphModel = graphController.getModel();
        }

        idMapper = new HashMap<Long, Integer>();

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
        Integer gephiNodeId = idMapper.get(neoNode.getId());

        org.gephi.graph.api.Node gephiNode;
        if (gephiNodeId != null) {
            gephiNode = graphModel.getGraph().getNode(gephiNodeId);
        } else {
            gephiNode = graphModel.factory().newNode();
            graphModel.getGraph().addNode(gephiNode);

            AttributeController attributeController =
                    Lookup.getDefault().lookup(AttributeController.class);

//            Attributes attributess = gephiNode.getNodeData().getAttributes();
//            attributess.setValue("id", /*"" + */gephiNode.getId());

            for (String neoPropertyKey : neoNode.getPropertyKeys()) {
                Object neoPropertyValue = neoNode.getProperty(neoPropertyKey);

                System.out.println("neo property value: " + neoPropertyValue);
                System.out.println("attributeType: " + AttributeType.parse(neoPropertyValue));

                AttributeTable nodeTable = attributeController.getModel().getNodeTable();
                if (!nodeTable.hasColumn(neoPropertyKey)) {
                    nodeTable.addColumn(neoPropertyKey, AttributeType.parse(neoPropertyValue), AttributeOrigin.PROPERTY);
                }

                Attributes attributes = gephiNode.getNodeData().getAttributes();
                attributes.setValue(neoPropertyKey, neoPropertyValue);
            }

            idMapper.put(neoNode.getId(), gephiNode.getId());
        }

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
    public void createGephiEdge(org.gephi.graph.api.Node startGephiNode,
            org.gephi.graph.api.Node endGephiNode,
            Relationship neoRelationship) {
        Edge gephiEdge = graphModel.factory().newEdge(startGephiNode, endGephiNode);
        graphModel.getGraph().addEdge(gephiEdge);

//        AttributeController attributeController =
//                    Lookup.getDefault().lookup(AttributeController.class);

        for (String neoPropertyKey : neoRelationship.getPropertyKeys()) {
            Object neoPropertyValue = neoRelationship.getProperty(neoPropertyKey);

//            AttributeTable edgeTable = attributeController.getModel().getEdgeTable();
//            edgeTable.addColumn(neoPropertyKey, AttributeType.parse(neoPropertyValue), AttributeOrigin.PROPERTY);

            Attributes attributes = gephiEdge.getEdgeData().getAttributes();
            attributes.setValue(neoPropertyKey, neoPropertyValue);
        }
    }
    //public void create
}
