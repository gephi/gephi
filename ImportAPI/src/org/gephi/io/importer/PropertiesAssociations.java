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
package org.gephi.io.importer;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.gephi.data.properties.EdgeProperties;
import org.gephi.data.properties.NodeProperties;

/**
 *
 * @author Mathieu Bastian
 */
public final class PropertiesAssociations {

    //PropertiesAssociations association
    private List<PropertyAssociation<NodeProperties>> nodePropertyAssociations = new LinkedList<PropertyAssociation<NodeProperties>>();
    private List<PropertyAssociation<EdgeProperties>> edgePropertyAssociations = new LinkedList<PropertyAssociation<EdgeProperties>>();

    public void addEdgePropertyAssociation(PropertyAssociation<EdgeProperties> association) {
        if (edgePropertyAssociations.contains(association)) {
            return;
        }
        //Avoid any double
        for (Iterator<PropertyAssociation<EdgeProperties>> itr = edgePropertyAssociations.iterator(); itr.hasNext();) {
            PropertyAssociation<EdgeProperties> p = itr.next();
            if (p.getTitle().equals(association.getTitle())) {
                itr.remove();
            } else if (p.getProperty().equals(association.getProperty())) {
                itr.remove();
            }
        }
        edgePropertyAssociations.add(association);
    }

    public void addNodePropertyAssociation(PropertyAssociation<NodeProperties> association) {
        if (nodePropertyAssociations.contains(association)) {
            return;
        }
        //Avoid any double
        for (Iterator<PropertyAssociation<NodeProperties>> itr = nodePropertyAssociations.iterator(); itr.hasNext();) {
            PropertyAssociation<NodeProperties> p = itr.next();
            if (p.getTitle().equals(association.getTitle())) {
                itr.remove();
            } else if (p.getProperty().equals(association.getProperty())) {
                itr.remove();
            }
        }
        nodePropertyAssociations.add(association);
    }

    public void removeEdgePropertyAssociation(PropertyAssociation<EdgeProperties> association) {
        edgePropertyAssociations.remove(association);
    }

    public void removeNodePropertyAssociation(PropertyAssociation<NodeProperties> association) {
        nodePropertyAssociations.remove(association);
    }

    public PropertyAssociation<EdgeProperties>[] getEdgePropertiesAssociation() {
        return edgePropertyAssociations.toArray(new PropertyAssociation[0]);
    }

    public PropertyAssociation<NodeProperties>[] getNodePropertiesAssociation() {
        return nodePropertyAssociations.toArray(new PropertyAssociation[0]);
    }

    public NodeProperties getNodeProperty(String title) {
        for (PropertyAssociation<NodeProperties> p : nodePropertyAssociations) {
            if (p.getTitle().equals(title)) {
                return p.getProperty();
            }
        }
        return null;
    }

    public EdgeProperties getEdgeProperty(String title) {
        for (PropertyAssociation<EdgeProperties> p : edgePropertyAssociations) {
            if (p.getTitle().equals(title)) {
                return p.getProperty();
            }
        }
        return null;
    }

    public String getNodePropertyTitle(NodeProperties property) {
        for (PropertyAssociation<NodeProperties> p : nodePropertyAssociations) {
            if (p.getProperty().equals(property)) {
                return p.getTitle();
            }
        }
        return null;
    }

    public String getEdgePropertyTitle(EdgeProperties property) {
        for (PropertyAssociation<EdgeProperties> p : edgePropertyAssociations) {
            if (p.getProperty().equals(property)) {
                return p.getTitle();
            }
        }
        return null;
    }
}
