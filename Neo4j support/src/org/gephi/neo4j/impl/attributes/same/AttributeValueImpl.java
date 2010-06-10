package org.gephi.neo4j.impl.attributes.same;


import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeValue;


public class AttributeValueImpl implements AttributeValue {

    private final AttributeColumn column;
    private final Object value;

    public AttributeValueImpl(AttributeColumn column, Object value) {
        this.column = column;
        this.value = value;
    }


    public AttributeColumn getColumn() {
        return column;
    }

    public Object getValue() {
        return value;
    }

}
