package org.gephi.neo4j.impl.attributes;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeListener;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.neo4j.impl.attributes.same.AttributeColumnImpl;


public class I_____AttributeTableImpl implements AttributeTable {
    private static final String DEFAULT_COLUMN_NAME = "";
    private static final Object DEFAULT_COLUMN_VALUE = null;

    private static int columnCounter = 0;

    private final Map<String,  AttributeColumn> idToColumnMapper;
    private final Map<String,  AttributeColumn> titleToColumnMapper;
    private final Map<Integer, AttributeColumn> indexToColumnMapper;
    private final String name;


    public I_____AttributeTableImpl(String name) {
        idToColumnMapper = new HashMap<String, AttributeColumn>();
        titleToColumnMapper = new HashMap<String, AttributeColumn>();
        indexToColumnMapper = new HashMap<Integer, AttributeColumn>();
        this.name = name;
    }


    private static synchronized int getNextId() {
        return columnCounter++;
    }

    public String getName() {
        return name;
    }

    public AttributeColumn[] getColumns() {
        List<AttributeColumn> list = new ArrayList<AttributeColumn>();
        list.addAll(idToColumnMapper.values());

        return list.toArray(new AttributeColumn [0]);
    }

    public int countColumns() {
        return idToColumnMapper.values().size();
    }

    public AttributeColumn addColumn(String id, AttributeType type) {
        return addColumn(id, type, AttributeOrigin.PROPERTY);
    }

    public AttributeColumn addColumn(String id, AttributeType type, AttributeOrigin origin) {
        return addColumn(id, DEFAULT_COLUMN_NAME, type, origin, DEFAULT_COLUMN_VALUE);
    }

    public AttributeColumn addColumn(String id, String title, AttributeType type, AttributeOrigin origin, Object defaultValue) {
        int index = getNextId();

        AttributeColumn attributeColumn = new AttributeColumnImpl(this, index, id, title, type, origin, defaultValue);

        idToColumnMapper   .put(id,    attributeColumn);
        titleToColumnMapper.put(title, attributeColumn);
        indexToColumnMapper.put(index, attributeColumn);

        return attributeColumn;
    }

    public void removeColumn(AttributeColumn column) {
        idToColumnMapper   .remove(column.getId());
        titleToColumnMapper.remove(column.getTitle());
        indexToColumnMapper.remove(column.getIndex());

        //TODO remove also any values connected with this column from database?????
    }

    public AttributeColumn getColumn(int index) {
        return indexToColumnMapper.get(index);
    }

    public AttributeColumn getColumn(String id) {
        return idToColumnMapper.get(id);
    }

    public AttributeColumn getColumn(String title, AttributeType type) {
        AttributeColumn attributeColumn = titleToColumnMapper.get(title);

        return (attributeColumn.getType() == type) ? attributeColumn : null;
    }

    public boolean hasColumn(String title) {
        return titleToColumnMapper.containsKey(title);
    }

    public void addAttributeListener(AttributeListener listener) {
        //TODO implement addAttributeListener
    }

    public void removeAttributeListener(AttributeListener listener) {
        //TODO implement removeAttributeListener
    }

    public void mergeTable(AttributeTable table) {
        //TODO implement mergeTable
    }
}
