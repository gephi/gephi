/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.data.attributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeType;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Mathieu Bastian
 */
public class AttributeTableImpl implements AttributeTable, Lookup.Provider {

    protected String name;
    protected AbstractAttributeModel model;
    //Lookup
    protected AbstractLookup lookup;
    protected InstanceContent instanceContent;
    //Columns
    protected List<AttributeColumnImpl> columns = new ArrayList<AttributeColumnImpl>();
    protected Map<AttributeColumn, AttributeColumn> columnsSet = new HashMap<AttributeColumn, AttributeColumn>();
    protected Map<String, AttributeColumnImpl> columnsMap = new HashMap<String, AttributeColumnImpl>();
    //Version
    protected int version = 0;

    public AttributeTableImpl(AbstractAttributeModel model, String name) {
        this.name = name;
        this.model = model;
        this.instanceContent = new InstanceContent();
        this.lookup = new AbstractLookup(instanceContent);
    }

    public synchronized AttributeColumnImpl[] getColumns() {
        return columns.toArray(new AttributeColumnImpl[]{});
    }

    public synchronized int countColumns() {
        return columns.size();
    }

    public AttributeColumnImpl addColumn(String id, AttributeType type) {
        return addColumn(id, id, type, AttributeOrigin.DATA, null);
    }

    public AttributeColumnImpl addColumn(String id, AttributeType type, AttributeOrigin origin) {
        return addColumn(id, id, type, origin, null);
    }

    public synchronized AttributeColumnImpl addColumn(String id, String title, AttributeType type, AttributeOrigin origin, Object defaultValue) {
        if (defaultValue != null) {
            if (defaultValue.getClass() != type.getType()) {
                if (defaultValue.getClass() == String.class) {
                    defaultValue = type.parse((String) defaultValue);
                } else {
                    throw new IllegalArgumentException("The default value type cannot be cast to the type");
                }
            }
            defaultValue = model.getManagedValue(defaultValue, type);
        }
        AttributeColumnImpl column = new AttributeColumnImpl(columns.size(), id, title, type, origin, defaultValue);
        columns.add(column);
        columnsMap.put(id, column);
        if (title != null && !title.equals(id)) {
            columnsMap.put(title, column);
        }
        instanceContent.add(column);
        columnsSet.put(column, column);

        //Version
        version++;

        return column;
    }

    public synchronized void removeColumn(AttributeColumn column) {
        //Remove from collections
        columns.remove(column);
        columnsMap.remove(column.getId());
        if (column.getTitle() != null && !column.getTitle().equals(column.getId())) {
            columnsMap.remove(column.getTitle());
        }
        instanceContent.remove(column);
        columnsSet.remove(column);

        //Version
        version++;
    }

    public synchronized AttributeColumnImpl getColumn(int index) {
        if (index >= 0 && index < columns.size()) {
            return columns.get(index);
        }

        return null;
    }

    public synchronized AttributeColumnImpl getColumn(String id) {
        return columnsMap.get(id);
    }

    public synchronized AttributeColumnImpl getColumn(String title, AttributeType type) {
        AttributeColumnImpl c = columnsMap.get(title);
        if (c != null && c.getType().equals(type)) {
            return c;
        }
        return null;
    }

    public synchronized AttributeColumn getColumn(AttributeColumn column) {
        return columnsSet.get(column);

    }

    public synchronized boolean hasColumn(String title) {
        return columnsMap.containsKey(title);
    }

    public synchronized int getVersion() {
        return version;
    }

    public String getName() {
        return name;
    }

    public Lookup getLookup() {
        return lookup;
    }

    public AttributeFactoryImpl getFactory() {
        return model.getFactory();
    }

    public synchronized void mergeTable(AttributeTable table) {
        for (AttributeColumn column : table.getColumns()) {
            AttributeColumn existingCol = getColumn(column.getId(), column.getType());
            if (existingCol == null) {
                addColumn(column.getId(), column.getTitle(), column.getType(), column.getOrigin(), column.getDefaultValue());
            }
        }
    }
}
