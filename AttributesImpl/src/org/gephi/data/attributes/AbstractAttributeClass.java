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
import java.util.List;
import java.util.Map;
import org.gephi.data.attributes.api.AttributeClass;
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
public abstract class AbstractAttributeClass implements AttributeClass, Lookup.Provider {

    protected String name;
    protected AbstractAttributeManager manager;

    //Lookup
    protected AbstractLookup lookup;
    protected InstanceContent instanceContent;

    //Columns
    protected List<AttributeColumnImpl> columns = new ArrayList<AttributeColumnImpl>();
    protected Map<String, AttributeColumnImpl> columnsMap = new HashMap<String, AttributeColumnImpl>();

    //Version
    protected int version = 0;

    public AbstractAttributeClass(AbstractAttributeManager manager, String name) {
        this.name = name;
        this.manager = manager;
        this.instanceContent = new InstanceContent();
        this.lookup = new AbstractLookup(instanceContent);
    }

    public abstract String getName();

    public synchronized AttributeColumnImpl[] getAttributeColumns() {
        return columns.toArray(new AttributeColumnImpl[]{});
    }

    public synchronized int countAttributeColumns() {
        return columns.size();
    }

    public AttributeColumnImpl addAttributeColumn(String id, AttributeType type) {
        return addAttributeColumn(id, id, type, AttributeOrigin.DATA, null);
    }

    public AttributeColumnImpl addAttributeColumn(String id, AttributeType type, AttributeOrigin origin) {
        return addAttributeColumn(id, id, type, origin, null);
    }

    public synchronized AttributeColumnImpl addAttributeColumn(String id, String title, AttributeType type, AttributeOrigin origin, Object defaultValue) {
        if(defaultValue!=null) {
            if(defaultValue.getClass() != type.getType()) {
                if(defaultValue.getClass() == String.class) {
                    defaultValue = type.parse((String)defaultValue);
                } else {
                    throw new IllegalArgumentException("The default value type cannot be cast to the type");
                }
            }
            defaultValue = manager.getManagedValue(defaultValue, type);
        }
        AttributeColumnImpl column = new AttributeColumnImpl(columns.size(), id, title, type, origin, defaultValue);
        columns.add(column);
        columnsMap.put(id, column);
        if (title != null && !title.equals(id)) {
            columnsMap.put(title, column);
        }
        instanceContent.add(column);

        //Version
        version++;

        return column;
    }

    public synchronized void removeAttributeColumn(AttributeColumn column) {
        //Remove from collections
        columns.remove(column);
        columnsMap.remove(column.getId());
        if (column.getTitle() != null && !column.getTitle().equals(column.getId())) {
            columnsMap.remove(column.getTitle());
        }
        instanceContent.remove(column);

        //Version
        version++;
    }

    public synchronized AttributeColumnImpl getAttributeColumn(int index) {
        if (index >= 0 && index < columns.size()) {
            return columns.get(index);
        }

        return null;
    }

    public synchronized AttributeColumnImpl getAttributeColumn(String id) {
        return columnsMap.get(id);
    }

    public synchronized AttributeColumnImpl getAttributeColumn(String title, AttributeType type) {
        AttributeColumnImpl c = columnsMap.get(title);
        if (c != null && c.getAttributeType().equals(type)) {
            return c;
        }
        return null;
    }

    public synchronized boolean hasAttributeColumn(String title) {
        return columnsMap.containsKey(title);
    }

    public synchronized int getVersion() {
        return version;
    }

    public Lookup getLookup() {
        return lookup;
    }

    public AttributeFactoryImpl getFactory() {
        return manager.getFactory();
    }
}
