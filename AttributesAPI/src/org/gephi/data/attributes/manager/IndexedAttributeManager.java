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
package org.gephi.data.attributes.manager;

import org.gephi.data.attributes.AbstractAttributeManager;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.type.StringList;
import org.gephi.data.network.api.SyncReader;

/**
 *
 * @author Mathieu Bastian
 */
public class IndexedAttributeManager extends AbstractAttributeManager {

    protected DataIndex dataIndex;

    public IndexedAttributeManager(SyncReader reader) {
        super(reader);
        dataIndex = new DataIndex();
    }

    @Override
    public Object getManagedValue(Object obj, AttributeType attributeType) {
        switch (attributeType) {
            case BOOLEAN:
                return dataIndex.pushData((Boolean) obj);
            case FLOAT:
                return dataIndex.pushData((Float) obj);
            case INT:
                return dataIndex.pushData((Integer) obj);
            case STRING:
                return dataIndex.pushData((String) obj);
            case LIST_STRING:
                return dataIndex.pushData((StringList) obj);
        }
        return obj;
    }

    @Override
    public void clear() {
        super.clear();
        dataIndex.clear();
    }
}
