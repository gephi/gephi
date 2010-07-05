package org.gephi.neo4j.impl;


import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.spi.AttributeValueDelegateProvider;
import org.gephi.data.properties.PropertiesColumn;
import org.neo4j.graphdb.GraphDatabaseService;


public class Neo4jDelegateProviderImpl implements AttributeValueDelegateProvider {
    private static final Neo4jDelegateProviderImpl instance;

    private static GraphDatabaseService graphDB;

    
    static {
        instance = new Neo4jDelegateProviderImpl();
    }

    private Neo4jDelegateProviderImpl() {}

    @Override
    public Object getNodeValue(AttributeColumn attributeColumn, Object id) {
        return graphDB.getNodeById((Long) id).getProperty(attributeColumn.getId());
    }

    @Override
    public Object getEdgeValue(AttributeColumn attributeColumn, Object id) {
        if (attributeColumn.getId().equals(PropertiesColumn.NEO4J_RELATIONSHIP_TYPE.getId()))
            return graphDB.getRelationshipById((Long) id).getType().name();
        else
            return graphDB.getRelationshipById((Long) id).getProperty(attributeColumn.getId());
    }

    public static Neo4jDelegateProviderImpl getInstance() {
        return instance;
    }

    public static void setGraphDB(GraphDatabaseService graphDB) {
        Neo4jDelegateProviderImpl.graphDB = graphDB;
    }
}
