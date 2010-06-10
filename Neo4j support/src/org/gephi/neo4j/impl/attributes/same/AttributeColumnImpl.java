package org.gephi.neo4j.impl.attributes.same;


import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.neo4j.impl.attributes.I_____AttributeTableImpl;


public class AttributeColumnImpl implements AttributeColumn {

    protected final I_____AttributeTableImpl table;
    protected final int index;
    protected final String id;
    protected final String title;
    protected final AttributeType type;
    protected final AttributeOrigin origin;
    protected final AttributeValueImpl defaultValue;

    public AttributeColumnImpl(I_____AttributeTableImpl table, int index, String id, String title, AttributeType attributeType, AttributeOrigin origin, Object defaultValue) {
        this.table = table;
        this.index = index;
        this.id = id;
        this.type = attributeType;
        this.title = title;
        this.origin = origin;
        this.defaultValue = new AttributeValueImpl(this, defaultValue);
    }

    
    public AttributeType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public int getIndex() {
        return index;
    }

    public AttributeOrigin getOrigin() {
        return origin;
    }

    public String getId() {
        return id;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    @Override
    public String toString() {
        return title + " (" + type.toString() + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AttributeColumn) {
            AttributeColumnImpl o = (AttributeColumnImpl) obj;
            return id.equals(o.id) && o.type == type;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (this.id != null ? this.id.hashCode() : 0);
        hash = 53 * hash + (this.type != null ? this.type.hashCode() : 0);
        return hash;
    }
}
