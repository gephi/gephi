package org.gephi.neo4j.impl;


import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.spi.AttributeValueDelegateProvider;
import org.gephi.data.properties.PropertiesColumn;
import org.neo4j.graphdb.GraphDatabaseService;
import org.openide.util.lookup.ServiceProvider;


@ServiceProvider(service=AttributeValueDelegateProvider.class)
public class Neo4jDelegateProviderImpl implements AttributeValueDelegateProvider {
    GraphDatabaseService graphDB;

    
    @Override
    public PropertiesColumn getDelegateIdColumn() {
        return PropertiesColumn.NEO4J_ID;
    }

    @Override
    public Object getNodeValue(AttributeColumn attributeColumn, Object id) {
        return graphDB.getRelationshipById((Long) id).getProperty(attributeColumn.getId());
    }

    @Override
    public Object getEdgeValue(AttributeColumn attributeColumn, Object id) {
        return graphDB.getNodeById((Long) id).getProperty(attributeColumn.getId());
    }
}
