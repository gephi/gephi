package org.gephi.neo4j.impl.attributes;


import java.util.HashMap;
import java.util.Map;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeRowFactory;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeValueFactory;


public class I_____AttributeModelImpl implements AttributeModel {
    private static final String EDGE_TABLE_NAME = "edge table";
    private static final String NODE_TABLE_NAME = "node table";

    private final Map<String, AttributeTable> nameToTableMapper;
    private final AttributeTable nodeTable;
    private final AttributeTable edgeTable;
    private final I_____AttributeFactoryImpl factoryImpl;


    public I_____AttributeModelImpl() {
        nodeTable = new I_____AttributeTableImpl(NODE_TABLE_NAME);
        edgeTable = new I_____AttributeTableImpl(EDGE_TABLE_NAME);

        nameToTableMapper = new HashMap<String, AttributeTable>();
        nameToTableMapper.put(nodeTable.getName(), nodeTable);
        nameToTableMapper.put(edgeTable.getName(), edgeTable);

        factoryImpl = new I_____AttributeFactoryImpl();
    }


    public AttributeTable getNodeTable() {
        return nodeTable;
    }

    public AttributeTable getEdgeTable() {
        return edgeTable;
    }

    public AttributeTable getTable(String name) {
        return nameToTableMapper.get(name);
    }

    public AttributeTable[] getTables() {
        return nameToTableMapper.values().toArray(new AttributeTable [0]);
    }

    public AttributeValueFactory valueFactory() {
        return factoryImpl;
    }

    public AttributeRowFactory rowFactory() {
        return factoryImpl;
    }

    public void mergeModel(AttributeModel model) {
        //TODO implements mergeModel
    }

}
