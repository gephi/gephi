package org.gephi.neo4j.impl;


import org.gephi.data.attributes.api.AttributeColumn;
//import org.gephi.data.attributes.spi.AttributeValueDelegateProvider;
import org.gephi.data.attributes.spi.AttributeValueDelegateProvider;
//import org.gephi.data.properties.PropertiesColumn;
import org.neo4j.graphdb.GraphDatabaseService;
//import org.neo4j.graphdb.NotFoundException;
//import org.openide.util.lookup.ServiceProvider;


//@ServiceProvider(service=AttributeValueDelegateProvider.class)
public class Neo4jDelegateProviderImpl implements AttributeValueDelegateProvider {
    private static final Neo4jDelegateProviderImpl instance;

    private static GraphDatabaseService graphDB;

    
    static {
        instance = new Neo4jDelegateProviderImpl();
    }

    private Neo4jDelegateProviderImpl() {}

//    @Override
//    public PropertiesColumn getDelegateIdColumn() {
//        System.out.println("> getDelegateIdColumn()");
//
//        return null;//PropertiesColumn.NEO4J_ID;;
//    }

    @Override
    public Object getNodeValue(AttributeColumn attributeColumn, Object id) {
        System.out.println(">> getNodeValue()");
        System.out.println("graphdb: " + graphDB);
        System.out.println("id: " + id);
//
//        try {
            org.neo4j.graphdb.Node node = graphDB.getNodeById((Long) id);
            String propertyName = attributeColumn.getId();
            Object result = node.getProperty(propertyName);
//        }
//        catch (NotFoundException e) {
//            e.printStackTrace();
//        }

        //return graphDB.getRelationshipById((Long) id).getProperty(attributeColumn.getId());
        return graphDB.getNodeById((Long) id).getProperty(attributeColumn.getId());
    }

    @Override
    public Object getEdgeValue(AttributeColumn attributeColumn, Object id) {
//        System.out.println(">>> getEdgeValue()");
//
//        org.neo4j.graphdb.Relationship relationship = graphDB.getRelationshipById((Long) id);
//        String propertyName = attributeColumn.getId();
//        Object result = relationship.getProperty(propertyName);

        //return graphDB.getNodeById((Long) id).getProperty(attributeColumn.getId());
        return graphDB.getRelationshipById((Long) id).getProperty(attributeColumn.getId());//TODO rozpisat a zistit kde presne je chyba
    }

    public static Neo4jDelegateProviderImpl getInstance() {
        return instance;
    }

    public static void setGraphDB(GraphDatabaseService graphDB) {
//        System.out.println("set graphDB...");
        Neo4jDelegateProviderImpl.graphDB = graphDB;
    }
}
