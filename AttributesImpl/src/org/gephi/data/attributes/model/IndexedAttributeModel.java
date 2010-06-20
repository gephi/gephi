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
package org.gephi.data.attributes.model;

import org.gephi.data.attributes.AbstractAttributeModel;
import org.gephi.data.attributes.api.AttributeType;

/**
 *
 * @author Mathieu Bastian
 * @author Martin Škurla
 */
public class IndexedAttributeModel extends AbstractAttributeModel {

    protected DataIndex dataIndex;

    public IndexedAttributeModel() {
        dataIndex = new DataIndex();
        createPropertiesColumn();
    }

    @Override
    public Object getManagedValue(Object obj, AttributeType attributeType) {
        return dataIndex.pushData(obj);
    }

    @Override
    public void clear() {
        super.clear();
        dataIndex.clear();
    }
}
