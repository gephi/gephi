/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>, Martin Škurla <bujacik@gmail.com>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.gephi.data.attributes;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeRowFactory;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.api.AttributeValueFactory;
import org.gephi.data.properties.PropertiesColumn;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 * @author Martin Škurla
 */
public abstract class AbstractAttributeModel implements AttributeModel {

    //Classes
    private ConcurrentMap<String, AttributeTableImpl> tableMap;
    private AttributeTableImpl nodeTable;
    private AttributeTableImpl edgeTable;
    //Factory
    private AttributeFactoryImpl factory;

    //Data API
    public AbstractAttributeModel() {
        tableMap = new ConcurrentHashMap<String, AttributeTableImpl>();
        nodeTable = new AttributeTableImpl(this, NbBundle.getMessage(AttributeTableImpl.class, "NodeAttributeTable.name"));
        edgeTable = new AttributeTableImpl(this, NbBundle.getMessage(AttributeTableImpl.class, "EdgeAttributeTable.name"));
        tableMap.put(nodeTable.name, nodeTable);
        tableMap.put(edgeTable.name, edgeTable);
        factory = new AttributeFactoryImpl(this);
    }

    protected void createPropertiesColumn() {
        // !!! the position of PropertiesColumn enum constants in following arrays must be the same
        // !!! as index in each constant
        PropertiesColumn[] columnsForNodeTable = {PropertiesColumn.NODE_ID,
                                                  PropertiesColumn.NODE_LABEL};
        PropertiesColumn[] columnsForEdgeTable = {PropertiesColumn.EDGE_ID,
                                                  PropertiesColumn.EDGE_LABEL,
                                                  PropertiesColumn.EDGE_WEIGHT};

        for (PropertiesColumn columnForNodeTable : columnsForNodeTable)
            nodeTable.addPropertiesColumn(columnForNodeTable);

        for (PropertiesColumn columnForEdgeTable : columnsForEdgeTable)
            edgeTable.addPropertiesColumn(columnForEdgeTable);
    }

    public abstract Object getManagedValue(Object obj, AttributeType attributeType);

    public void clear() {
    }

    public AttributeTableImpl getNodeTable() {
        return nodeTable;
    }

    public AttributeTableImpl getEdgeTable() {
        return edgeTable;
    }

    public AttributeTableImpl getTable(String name) {
        AttributeTableImpl attTable = tableMap.get(name);
        if (attTable != null) {
            return attTable;
        }
        return null;
    }

    public AttributeTableImpl[] getTables() {
        return tableMap.values().toArray(new AttributeTableImpl[0]);
    }

    public AttributeRowFactory rowFactory() {
        return factory;
    }

    public AttributeValueFactory valueFactory() {
        return factory;
    }

    public AttributeFactoryImpl getFactory() {
        return factory;
    }

    public void addTable(AttributeTableImpl table) {
        tableMap.put(table.getName(), table);
    }

    public void mergeModel(AttributeModel model) {
        if (model.getNodeTable() != null) {
            nodeTable.mergeTable(model.getNodeTable());
        }
        if (model.getEdgeTable() != null) {
            edgeTable.mergeTable(model.getEdgeTable());
        }

        for (AttributeTable table : model.getTables()) {
            if (table != model.getNodeTable() && table != model.getEdgeTable()) {
                AttributeTable existingTable = tableMap.get(table.getName());
                if (existingTable != null) {
                    ((AttributeTableImpl) existingTable).mergeTable(table);
                } else {
                    AttributeTableImpl newTable = new AttributeTableImpl(this, table.getName());
                    tableMap.put(newTable.getName(), newTable);
                    newTable.mergeTable(table);
                }
            }
        }
    }
}
