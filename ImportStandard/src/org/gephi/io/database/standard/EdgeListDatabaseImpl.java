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
package org.gephi.io.database.standard;

import org.gephi.data.properties.EdgeProperties;
import org.gephi.data.properties.NodeProperties;
import org.gephi.io.database.AbstractDatabase;
import org.gephi.io.importer.PropertyAssociation;

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
        properties.addNodePropertyAssociation(new PropertyAssociation<NodeProperties>(NodeProperties.ID, "id"));
        properties.addNodePropertyAssociation(new PropertyAssociation<NodeProperties>(NodeProperties.LABEL, "label"));
        properties.addNodePropertyAssociation(new PropertyAssociation<NodeProperties>(NodeProperties.X, "x"));
        properties.addNodePropertyAssociation(new PropertyAssociation<NodeProperties>(NodeProperties.Y, "y"));
        properties.addNodePropertyAssociation(new PropertyAssociation<NodeProperties>(NodeProperties.SIZE, "size"));

        //Default edge associations
        properties.addEdgePropertyAssociation(new PropertyAssociation<EdgeProperties>(EdgeProperties.ID, "id"));
        properties.addEdgePropertyAssociation(new PropertyAssociation<EdgeProperties>(EdgeProperties.SOURCE, "source"));
        properties.addEdgePropertyAssociation(new PropertyAssociation<EdgeProperties>(EdgeProperties.TARGET, "target"));
        properties.addEdgePropertyAssociation(new PropertyAssociation<EdgeProperties>(EdgeProperties.LABEL, "label"));
        properties.addEdgePropertyAssociation(new PropertyAssociation<EdgeProperties>(EdgeProperties.WEIGHT, "weight"));
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
