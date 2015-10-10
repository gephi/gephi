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
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Mathieu Bastian
 */
public final class PropertiesAssociations implements Serializable {

    public enum NodeProperties {

        X, Y, Z, R, G, B, COLOR, SIZE, ID, LABEL, FIXED, START, END, START_OPEN, END_OPEN;
    }

    public enum EdgeProperties {

        R, G, B, COLOR, WEIGHT, ID, LABEL, ALPHA, SOURCE, TARGET, START, END, START_OPEN, END_OPEN;
    }
    //PropertiesAssociations association
    private final List<PropertyAssociation<NodeProperties>> nodePropertyAssociations = new LinkedList<PropertyAssociation<NodeProperties>>();
    private final List<PropertyAssociation<EdgeProperties>> edgePropertyAssociations = new LinkedList<PropertyAssociation<EdgeProperties>>();

    public void addEdgePropertyAssociation(EdgeProperties property, String title) {
        PropertyAssociation<EdgeProperties> association = new PropertyAssociation<EdgeProperties>(property, title);
        /*if (edgePropertyAssociations.contains(association)) {
            return;
        }
        //Avoid any double
        for (Iterator<PropertyAssociation<EdgeProperties>> itr = edgePropertyAssociations.iterator(); itr.hasNext();) {
            PropertyAssociation<EdgeProperties> p = itr.next();
            if (p.getTitle().equalsIgnoreCase(association.getTitle())) {
                itr.remove();
            } else if (p.getProperty().equals(association.getProperty())) {
                itr.remove();
            }
        }*/
        edgePropertyAssociations.add(association);
    }

    public void addNodePropertyAssociation(NodeProperties property, String title) {
        PropertyAssociation<NodeProperties> association = new PropertyAssociation<NodeProperties>(property, title);
        /*if (nodePropertyAssociations.contains(association)) {
            return;
        }
        //Avoid any double
        for (Iterator<PropertyAssociation<NodeProperties>> itr = nodePropertyAssociations.iterator(); itr.hasNext();) {
            PropertyAssociation<NodeProperties> p = itr.next();
            if (p.getTitle().equalsIgnoreCase(association.getTitle())) {
                itr.remove();
            } else if (p.getProperty().equals(association.getProperty())) {
                itr.remove();
            }
        }*/
        nodePropertyAssociations.add(association);
    }

    PropertyAssociation<EdgeProperties>[] getEdgePropertiesAssociation() {
        return edgePropertyAssociations.toArray(new PropertyAssociation[0]);
    }

    PropertyAssociation<NodeProperties>[] getNodePropertiesAssociation() {
        return nodePropertyAssociations.toArray(new PropertyAssociation[0]);
    }

    public NodeProperties getNodeProperty(String title) {
        for (PropertyAssociation<NodeProperties> p : nodePropertyAssociations) {
            if (p.getTitle().equalsIgnoreCase(title)) {
                return p.getProperty();
            }
        }
        return null;
    }

    public EdgeProperties getEdgeProperty(String title) {
        for (PropertyAssociation<EdgeProperties> p : edgePropertyAssociations) {
            if (p.getTitle().equalsIgnoreCase(title)) {
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

    public String getInfos() {
        String res = "***Node Properties Associations***\n";
        for (PropertyAssociation<NodeProperties> p : nodePropertyAssociations) {
            res += "Property " + p.getProperty().toString() + " = " + p.getTitle() + " Column\n";
        }
        res += "*********************************\n";
        res = "***Edge Properties Associations***\n";
        for (PropertyAssociation<EdgeProperties> p : edgePropertyAssociations) {
            res += "Property " + p.getProperty().toString() + " = " + p.getTitle() + " Column\n";
        }
        res += "*********************************\n";
        return res;
    }
}
