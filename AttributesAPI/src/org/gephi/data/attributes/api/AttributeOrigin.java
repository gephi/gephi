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
    DELEGATE(null);  

    private final String label;

    AttributeOrigin(String label) {
        this.label = label;
    }
}
