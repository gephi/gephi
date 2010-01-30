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
package org.gephi.data.properties;

/**
 * Enum that define static <code>AttributeColumn</code> indexes, like <code>ID</code>
 * or <code>LABEL</code>. Use these enum to find the index of these columns in
 * node and edge table.
 * <h4>Get nodes ID column
 * <pre>
 * AttributeColumn col = nodeTable.getColumn(PropertiesColumn.NODE_ID);
 *
 * @author Mathieu Bastian
 */
public enum PropertiesColumn {

    NODE_ID(0),
    NODE_LABEL(1),
    EDGE_ID(0),
    EDGE_LABEL(1);
    private final int index;

    PropertiesColumn(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}

