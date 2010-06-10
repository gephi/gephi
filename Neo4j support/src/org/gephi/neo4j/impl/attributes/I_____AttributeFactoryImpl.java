package org.gephi.neo4j.impl.attributes;


import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeRowFactory;
import org.gephi.data.attributes.api.AttributeValue;
import org.gephi.data.attributes.api.AttributeValueFactory;
import org.gephi.neo4j.impl.attributes.same.AttributeValueImpl;


public class I_____AttributeFactoryImpl implements AttributeRowFactory, AttributeValueFactory{

    public AttributeRow newNodeRow() {
        return new _____AttributeRowImpl(GraphElement.NODE);
    }

    public AttributeRow newEdgeRow() {
        return new _____AttributeRowImpl(GraphElement.EDGE);
    }

    public AttributeRow newRowForTable(String tableName) {
        //TODO implement newRowForTable
        return null;
    }

    public AttributeValue newValue(AttributeColumn column, Object value) {
        return new AttributeValueImpl(column, value);
    }
}
