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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeEvent;
import org.gephi.data.attributes.api.AttributeListener;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.spi.AttributeValueDelegateProvider;
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
    protected final List<AttributeListener> listeners;
    protected final AttributeEventDispatchThread eventDispatchThread;
    //Columns
    protected final List<AttributeColumnImpl> columns = new ArrayList<AttributeColumnImpl>();
    protected final Map<AttributeColumn, AttributeColumn> columnsSet = new HashMap<AttributeColumn, AttributeColumn>();
    protected final Map<String, AttributeColumnImpl> columnsMap = new HashMap<String, AttributeColumnImpl>();
    //Version
    protected int version = 0;

    public AttributeTableImpl(AbstractAttributeModel model, String name) {
        this.name = name;
        this.model = model;
        this.listeners = Collections.synchronizedList(new ArrayList<AttributeListener>());
        this.eventDispatchThread = new AttributeEventDispatchThread();
        eventDispatchThread.start();
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
            columnsMap.put(title, column);
        }
        columnsSet.put(column, column);

        //Version
        version++;

        fireAttributeEvent(
                new AttributeEventImpl(AttributeEvent.EventType.ADD_COLUMN, this, column));

        return column;
    }

    public synchronized void removeColumn(AttributeColumn column) {
        //Remove from collections
        columns.remove((AttributeColumnImpl) column);
        columnsMap.remove(column.getId());
        if (column.getTitle() != null && !column.getTitle().equals(column.getId())) {
            columnsMap.remove(column.getTitle());
        }
        columnsSet.remove(column);

        fireAttributeEvent(
                new AttributeEventImpl(AttributeEvent.EventType.REMOVE_COLUMN, this, column));

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
                addColumn(column.getId(), column.getTitle(), column.getType(), column.getOrigin(), column.getDefaultValue());
            }
        }
    }

    public void addAttributeListener(AttributeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeAttributeListener(AttributeListener listener) {
        listeners.remove(listener);
    }

    private void fireAttributeEvent(AttributeEvent event) {
        eventDispatchThread.fireEvent(event);
    }

    protected class AttributeEventDispatchThread extends Thread {

        private boolean stop;
        private final LinkedBlockingQueue<AttributeEvent> eventQueue;
        private final Object lock = new Object();

        public AttributeEventDispatchThread() {
            super("AttributeEvent Dispatch Thread " + name);
            this.eventQueue = new LinkedBlockingQueue<AttributeEvent>();
        }

        @Override
        public void run() {
            while (!stop) {
                AttributeEvent evt;
                while ((evt = eventQueue.poll()) != null) {
                    for (AttributeListener l : listeners.toArray(new AttributeListener[0])) {
                        l.attributesChanged(evt);
                    }
                }

                while (eventQueue.isEmpty()) {
                    try {
                        synchronized (lock) {
                            lock.wait();
                        }
                    } catch (InterruptedException e) {
                    }
                }
            }
        }

        public void stop(boolean stop) {
            this.stop = stop;
        }

        public void fireEvent(AttributeEvent event) {
            eventQueue.add(event);
            synchronized (lock) {
                lock.notifyAll();
            }
        }
    }
}
