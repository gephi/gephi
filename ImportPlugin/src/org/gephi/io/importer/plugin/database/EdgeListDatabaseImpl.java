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
package org.gephi.io.importer.plugin.database;

import org.gephi.io.importer.api.AbstractDatabase;
import org.gephi.io.importer.api.EdgeListDatabase;
import org.gephi.io.importer.api.PropertiesAssociations.EdgeProperties;
import org.gephi.io.importer.api.PropertiesAssociations.NodeProperties;

/**
 *
 * @author Mathieu Bastian
 */
public class EdgeListDatabaseImpl extends AbstractDatabase implements EdgeListDatabase {

    private String nodeQuery;
    private String edgeQuery;
    private String nodeAttributesQuery;
    private String edgeAttributesQuery;

    public EdgeListDatabaseImpl() {

        //Default node associations
        properties.addNodePropertyAssociation(NodeProperties.ID, "id");
        properties.addNodePropertyAssociation(NodeProperties.LABEL, "label");
        properties.addNodePropertyAssociation(NodeProperties.X, "x");
        properties.addNodePropertyAssociation(NodeProperties.Y, "y");
        properties.addNodePropertyAssociation(NodeProperties.SIZE, "size");

        //Default edge associations
        properties.addEdgePropertyAssociation(EdgeProperties.ID, "id");
        properties.addEdgePropertyAssociation(EdgeProperties.SOURCE, "source");
        properties.addEdgePropertyAssociation(EdgeProperties.TARGET, "target");
        properties.addEdgePropertyAssociation(EdgeProperties.LABEL, "label");
        properties.addEdgePropertyAssociation(EdgeProperties.WEIGHT, "weight");
    }

    public String getEdgeAttributesQuery() {
        return edgeAttributesQuery;
    }

    public void setEdgeAttributesQuery(String edgeAttributesQuery) {
        this.edgeAttributesQuery = edgeAttributesQuery;
    }

    public String getEdgeQuery() {
        return edgeQuery;
    }

    public void setEdgeQuery(String edgeQuery) {
        this.edgeQuery = edgeQuery;
    }

    public String getNodeAttributesQuery() {
        return nodeAttributesQuery;
    }

    public void setNodeAttributesQuery(String nodeAttributesQuery) {
        this.nodeAttributesQuery = nodeAttributesQuery;
    }

    public String getNodeQuery() {
        return nodeQuery;
    }

    public void setNodeQuery(String nodeQuery) {
        this.nodeQuery = nodeQuery;
    }
}
