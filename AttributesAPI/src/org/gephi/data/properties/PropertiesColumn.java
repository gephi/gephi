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

import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeType;

/**
 * Enum that define static <code>AttributeColumn</code> indexes, like <code>ID</code>
 * or <code>LABEL</code>. Use these enum to find the index of these columns in
 * node and edge table.
 * <h4>Get nodes ID column
 * <pre>
 * AttributeColumn col = nodeTable.getColumn(PropertiesColumn.NODE_ID.getIndex());
 *
 * @author Mathieu Bastian
 * @author Martin Škurla
 */
public enum PropertiesColumn {

    NODE_ID                (0, "id",            AttributeType.STRING, AttributeOrigin.PROPERTY, null),
    NODE_LABEL             (1, "label",         AttributeType.STRING, AttributeOrigin.PROPERTY, null),
    NODE_TIMEINTERVAL      (2, "time interval", AttributeType.TIME_INTERVAL, AttributeOrigin.PROPERTY, null),
    EDGE_ID                (0, "id",            AttributeType.STRING, AttributeOrigin.PROPERTY, null),
    EDGE_LABEL             (1, "label",         AttributeType.STRING, AttributeOrigin.PROPERTY, null),
    NEO4J_RELATIONSHIP_TYPE(2, "neo4j_rt",      AttributeType.STRING, AttributeOrigin.DELEGATE, null),
    EDGE_TIMEINTERVAL      (3, "time interval", AttributeType.TIME_INTERVAL, AttributeOrigin.PROPERTY, null){
        @Override
        public String getTitle() {
            return "Neo4j Relationship Type";
        }
    };
    
    private final int index;
    private final String id;
    private final AttributeType type;
    private final AttributeOrigin origin;
    private final Object defaultValue;

    PropertiesColumn(int index, String id, AttributeType attributeType, AttributeOrigin origin, Object defaultValue) {
        this.index = index;
        this.id = id;
        this.type = attributeType;
        this.origin = origin;
        this.defaultValue = defaultValue;
    }

    public int getIndex() {
        return index;
    }

    public String getId() {
        return id;
    }

    /**
     * Returns column title which will be showed to user in AttributeTables. Default title is derived
     * from id uppercasing first character. For multiword titles, getTitle() method in appropriate enum
     * constant object should be overridden.
     *
     * @return title
     */
    public String getTitle() {
        return Character.toUpperCase(id.charAt(0)) + id.substring(1, id.length());
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public AttributeType getType() {
        return type;
    }

    public AttributeOrigin getOrigin() {
        return origin;
    }
}

