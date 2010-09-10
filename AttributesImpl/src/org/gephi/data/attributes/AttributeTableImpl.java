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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeEvent;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.event.ColumnEvent;
import org.gephi.data.attributes.spi.AttributeValueDelegateProvider;
import org.gephi.data.attributes.type.TypeConvertor;
import org.gephi.data.properties.PropertiesColumn;

/**
 *
 * @author Mathieu Bastian
 * @author Martin Škurla
 */
public class AttributeTableImpl implements AttributeTable {

    protected String name;
    protected final AbstractAttributeModel model;
    //Listeners
    //Columns
    protected final List<AttributeColumnImpl> columns = new ArrayList<AttributeColumnImpl>();
    protected final Map<AttributeColumn, AttributeColumn> columnsSet = new HashMap<AttributeColumn, AttributeColumn>();
    protected final Map<String, AttributeColumnImpl> columnsMap = new HashMap<String, AttributeColumnImpl>();
    //Version
    protected int version = 0;

    public AttributeTableImpl(AbstractAttributeModel model, String name) {
        this.name = name;
        this.model = model;
    }

    public synchronized AttributeColumnImpl[] getColumns() {
        return columns.toArray(new AttributeColumnImpl[]{});
    }

    public synchronized int countColumns() {
        return columns.size();
    }

    public AttributeColumn addPropertiesColumn(PropertiesColumn propertiesColumn) {
        return addColumn(propertiesColumn.getId(),
                propertiesColumn.getTitle(),
                propertiesColumn.getType(),
                propertiesColumn.getOrigin(),
                propertiesColumn.getDefaultValue());
    }

    public AttributeColumnImpl addColumn(String id, AttributeType type) {
        return addColumn(id, id, type, AttributeOrigin.DATA, null, null);
    }

    public AttributeColumnImpl addColumn(String id, AttributeType type, AttributeOrigin origin) {
        return addColumn(id, id, type, origin, null, null);
    }

    public AttributeColumnImpl addColumn(String id, String title, AttributeType type, AttributeOrigin origin, Object defaultValue) {
        return addColumn(id, title, type, origin, defaultValue, null);
    }

    public AttributeColumn addColumn(String id, String title, AttributeType type, AttributeValueDelegateProvider attributeValueDelegateProvider, Object defaultValue) {
        return addColumn(id, title, type, AttributeOrigin.DELEGATE, defaultValue, attributeValueDelegateProvider);
    }

    private synchronized AttributeColumnImpl addColumn(String id, String title, AttributeType type, AttributeOrigin origin, Object defaultValue, AttributeValueDelegateProvider attributeValueDelegateProvider) {
        if (title == null || title.isEmpty() || hasColumn(title)) {
            throw new IllegalArgumentException("The title can't be null, empty or already existing in the table");
        }

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
        AttributeColumnImpl column = new AttributeColumnImpl(this, columns.size(), id, title, type, origin, defaultValue, attributeValueDelegateProvider);
        columns.add(column);
        columnsMap.put(id, column);
        if (title != null && !title.equals(id)) {
            columnsMap.put(title.toLowerCase(), column);
        }
        columnsSet.put(column, column);

        //Version
        version++;

        model.fireAttributeEvent(
                new ColumnEvent(AttributeEvent.EventType.ADD_COLUMN, column));

        return column;
    }

    public synchronized void removeColumn(AttributeColumn column) {
        int index = columns.indexOf(column);
        if (index == -1) {
            return;
        }

        //update indexes of the next columns of the one to delete:
        AttributeColumnImpl c;
        for (index = index + 1; index < columns.size(); index++) {
            c = columns.get(index);
            c.index--;
        }
        //Remove from collections
        columns.remove((AttributeColumnImpl) column);
        columnsMap.remove(column.getId());
        if (column.getTitle() != null && !column.getTitle().equals(column.getId())) {
            columnsMap.remove(column.getTitle());
        }
        columnsSet.remove(column);

        model.fireAttributeEvent(
                new ColumnEvent(AttributeEvent.EventType.REMOVE_COLUMN, (AttributeColumnImpl) column));

        //Version
        version++;
    }

    private synchronized void replaceColumn(AttributeColumn source, AttributeColumnImpl target) {
        int index = columns.indexOf(source);
        if (index == -1) {
            return;
        }
        //Remove from collections
        columnsMap.remove(source.getId());
        if (source.getTitle() != null && !source.getTitle().equals(source.getId())) {
            columnsMap.remove(source.getTitle());
        }
        columnsSet.remove(source);

        //Add
        columns.set(index, target);
        columnsMap.put(target.id, target);
        if (target.title != null && !target.title.equals(target.id)) {
            columnsMap.put(target.title.toLowerCase(), target);
        }
        columnsSet.put(target, target);

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
        AttributeColumnImpl col = columnsMap.get(id);
        if (col == null) {
            return columnsMap.get(id.toLowerCase());
        }
        return col;
    }

    public synchronized AttributeColumnImpl getColumn(String title, AttributeType type) {
        AttributeColumnImpl c = columnsMap.get(title.toLowerCase());
        if (c != null && c.getType().equals(type)) {
            return c;
        }
        return null;
    }

    public synchronized AttributeColumn getColumn(AttributeColumn column) {
        return columnsSet.get(column);
    }

    public synchronized boolean hasColumn(String title) {
        return columnsMap.containsKey(title.toLowerCase());
    }

    public synchronized int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AttributeFactoryImpl getFactory() {
        return model.getFactory();
    }

    public AbstractAttributeModel getModel() {
        return model;
    }

    public synchronized void mergeTable(AttributeTable table) {
        for (AttributeColumn column : table.getColumns()) {
            AttributeColumn existingCol = getColumn(column);
            if (existingCol == null) {
                existingCol = getColumn(column.getTitle());
            }
            if (existingCol == null) {
                addColumn(column.getId(), column.getTitle(), column.getType(), column.getOrigin(), column.getDefaultValue());
            } else if (column.getType().isDynamicType() && TypeConvertor.getStaticType(column.getType()).equals(existingCol.getType())) {
                //The column exists but has the underlying static type
                //Change type
                AttributeColumnImpl newCol = new AttributeColumnImpl(this, existingCol.getIndex(), existingCol.getId(), existingCol.getTitle(), column.getType(), existingCol.getOrigin(), column.getDefaultValue(), null);
                replaceColumn(existingCol, newCol);
            }
        }
    }
}
