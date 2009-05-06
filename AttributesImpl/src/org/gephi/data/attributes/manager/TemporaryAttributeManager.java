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
import org.gephi.data.network.api.SyncReader;

/**
 * Specific manager for temporary storing of attributes. This is typically used when new attributes are
 * imported in the system. No index system is required.
 * <p>
 *
 * @author Mathieu Bastian
 * @see IndexedAttributeManager
 */
public class TemporaryAttributeManager extends AbstractAttributeManager {

    public TemporaryAttributeManager(SyncReader reader) {
        super(reader);
    }

    @Override
    public Object getManagedValue(Object obj, AttributeType attributeType) {
        return obj;
    }
}

