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
package org.gephi.data.attributes.model;

import org.gephi.data.attributes.AbstractAttributeModel;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.event.AttributeEventManager;

/**
 *
 * @author Mathieu Bastian
 * @author Martin Škurla
 */
public class IndexedAttributeModel extends AbstractAttributeModel {

    protected DataIndex dataIndex;

    public IndexedAttributeModel() {
        dataIndex = new DataIndex();
        eventManager = new AttributeEventManager(this);
        createPropertiesColumn();

        eventManager.start();
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
