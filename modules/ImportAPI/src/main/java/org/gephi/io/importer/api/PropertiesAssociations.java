/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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

package org.gephi.io.importer.api;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Maps column titles to known node and edge properties for database importers.
 * <p>
 * This allows importers to declare which database column corresponds to which graph property (e.g. id, label,
 * color, position).
 */
public final class PropertiesAssociations implements Serializable {

    private final Map<String, NodeProperties> titleToNodeProperty = new HashMap<>();
    private final Map<String, EdgeProperties> titleToEdgeProperty = new HashMap<>();

    /**
     * Associates a column title with a node property.
     *
     * @param property the node property to associate
     * @param title    the column title that maps to this property
     */
    public void addNodePropertyAssociation(NodeProperties property, String title) {
        titleToNodeProperty.put(title, property);
    }

    /**
     * Associates a column title with an edge property.
     *
     * @param property the edge property to associate
     * @param title    the column title that maps to this property
     */
    public void addEdgePropertyAssociation(EdgeProperties property, String title) {
        titleToEdgeProperty.put(title, property);
    }

    /**
     * Returns the node property associated with the given column title, or {@code null} if none is found.
     *
     * @param title column title
     * @return node property or null
     */
    public NodeProperties getNodeProperty(String title) {
        return titleToNodeProperty.getOrDefault(title, null);
    }

    /**
     * Returns the edge property associated with the given column title, or {@code null} if none is found.
     *
     * @param title column title
     * @return edge property or null
     */
    public EdgeProperties getEdgeProperty(String title) {
        return titleToEdgeProperty.getOrDefault(title, null);
    }

    /**
     * Returns a human-readable summary of all node and edge property associations.
     *
     * @return formatted string listing all associations
     */
    public String getInfos() {
        StringBuilder builder = new StringBuilder("***Node Properties Associations***\n");
        for (Map.Entry<String, NodeProperties> entry : titleToNodeProperty.entrySet()) {
            builder.append("Property ")
                .append(entry.getValue().name())
                .append(" = ")
                .append(entry.getKey())
                .append(" Column\n");
        }
        builder.append("*********************************\n");
        builder.append("***Edge Properties Associations***\n");
        for (Map.Entry<String, EdgeProperties> entry : titleToEdgeProperty.entrySet()) {
            builder.append("Property ")
                .append(entry.getValue().name())
                .append(" = ")
                .append(entry.getKey())
                .append(" Column\n");
        }
        builder.append("*********************************\n");
        return builder.toString();
    }

    public enum NodeProperties {
        X, Y, Z, R, G, B, COLOR, SIZE, ID, LABEL, FIXED, START, END, START_OPEN, END_OPEN
    }

    public enum EdgeProperties {
        R, G, B, COLOR, WEIGHT, ID, LABEL, ALPHA, SOURCE, TARGET, START, END, START_OPEN, END_OPEN
    }
}
