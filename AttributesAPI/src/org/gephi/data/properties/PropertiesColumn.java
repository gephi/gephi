/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>, Martin Škurla <bujacik@gmail.com>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
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
 * </pre>
 * @author Mathieu Bastian
 * @author Martin Škurla
 */
public enum PropertiesColumn {

    NODE_ID                (0, "id",            AttributeType.STRING, AttributeOrigin.PROPERTY, null),
    NODE_LABEL             (1, "label",         AttributeType.STRING, AttributeOrigin.PROPERTY, null),
    EDGE_ID                (0, "id",            AttributeType.STRING, AttributeOrigin.PROPERTY, null),
    EDGE_LABEL             (1, "label",         AttributeType.STRING, AttributeOrigin.PROPERTY, null),
    EDGE_WEIGHT            (2, "weight",        AttributeType.FLOAT, AttributeOrigin.PROPERTY, 1f),
    NEO4J_RELATIONSHIP_TYPE(3, "neo4j_rt",      AttributeType.STRING, AttributeOrigin.DELEGATE, null){
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

