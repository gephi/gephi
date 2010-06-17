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
package org.gephi.data.attributes.api;

import org.gephi.data.properties.PropertiesColumn;

/**
 * Meta-data that describes the origin of columns content. Default value is <b>DATA</b>.
 * <ul><li><b>PROPERTY:</b> The attribute is a static field like Label, X or Y.</li>
 * <li><b>DATA:</b> The attribute is a normal associated data to the object.</li>
 * <li><b>COMPUTED:</b> The attribute has been computed during the program execution.</li></ul>
 * <p>
 *
 * @author Mathieu Bastian
 * @author Martin Škurla
 */
public enum AttributeOrigin {

    PROPERTY("AttributeOrigin_property"),
    DATA    ("AttributeOrigin_data"),
    COMPUTED("AttributeOrigin_computed"),

    DELEGATE_NEO4J(PropertiesColumn.NEO4J_ID) {
        @Override
        public boolean isDelegate() {
            return true;
        }

        @Override
        public PropertiesColumn getPropertiesColumn() {
            return PropertiesColumn.NEO4J_ID;
        }
    };
    

    private final String label;
    private final PropertiesColumn propertiesColumn;

    AttributeOrigin(String label) {
        this.label = label;
        this.propertiesColumn = null;
    }

    AttributeOrigin(PropertiesColumn propertiesColumn) {
        this.label = null;
        System.out.println("propCol: " + propertiesColumn);
        this.propertiesColumn = propertiesColumn;
    }

    public boolean isDelegate() {
        return propertiesColumn != null;
    }

    public PropertiesColumn getPropertiesColumn() {
        return propertiesColumn;
    }
}
