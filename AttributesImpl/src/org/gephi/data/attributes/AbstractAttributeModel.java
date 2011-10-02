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
package org.gephi.data.attributes;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.gephi.data.attributes.api.AttributeListener;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeRowFactory;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.api.AttributeValueFactory;
import org.gephi.data.attributes.event.AbstractEvent;
import org.gephi.data.attributes.event.AttributeEventManager;
import org.gephi.data.properties.PropertiesColumn;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 * @author Martin Škurla
 */
public abstract class AbstractAttributeModel implements AttributeModel {

    //Classes
    private ConcurrentMap<String, AttributeTableImpl> tableMap;
    private AttributeTableImpl nodeTable;
    private AttributeTableImpl edgeTable;
    //Factory
    private AttributeFactoryImpl factory;
    //Events
    protected AttributeEventManager eventManager;

    //Data API
    public AbstractAttributeModel() {
        tableMap = new ConcurrentHashMap<String, AttributeTableImpl>();
        nodeTable = new AttributeTableImpl(this, NbBundle.getMessage(AttributeTableImpl.class, "NodeAttributeTable.name"));
        edgeTable = new AttributeTableImpl(this, NbBundle.getMessage(AttributeTableImpl.class, "EdgeAttributeTable.name"));
        tableMap.put(nodeTable.name, nodeTable);
        tableMap.put(edgeTable.name, edgeTable);
        factory = new AttributeFactoryImpl(this);
    }

    protected void createPropertiesColumn() {
        // !!! the position of PropertiesColumn enum constants in following arrays must be the same
        // !!! as index in each constant
        PropertiesColumn[] columnsForNodeTable = {PropertiesColumn.NODE_ID,
                                                  PropertiesColumn.NODE_LABEL};
        PropertiesColumn[] columnsForEdgeTable = {PropertiesColumn.EDGE_ID,
                                                  PropertiesColumn.EDGE_LABEL,
                                                  PropertiesColumn.EDGE_WEIGHT};

        for (PropertiesColumn columnForNodeTable : columnsForNodeTable)
            nodeTable.addPropertiesColumn(columnForNodeTable);

        for (PropertiesColumn columnForEdgeTable : columnsForEdgeTable)
            edgeTable.addPropertiesColumn(columnForEdgeTable);
    }

    public abstract Object getManagedValue(Object obj, AttributeType attributeType);

    public void clear() {
    }

    public AttributeTableImpl getNodeTable() {
        return nodeTable;
    }

    public AttributeTableImpl getEdgeTable() {
        return edgeTable;
    }

    public AttributeTableImpl getTable(String name) {
        AttributeTableImpl attTable = tableMap.get(name);
        if (attTable != null) {
            return attTable;
        }
        return null;
    }

    public AttributeTableImpl[] getTables() {
        return tableMap.values().toArray(new AttributeTableImpl[0]);
    }

    public AttributeRowFactory rowFactory() {
        return factory;
    }

    public AttributeValueFactory valueFactory() {
        return factory;
    }

    public AttributeFactoryImpl getFactory() {
        return factory;
    }

    public void addTable(AttributeTableImpl table) {
        tableMap.put(table.getName(), table);
    }

    public void addAttributeListener(AttributeListener listener) {
        eventManager.addAttributeListener(listener);
    }

    public void removeAttributeListener(AttributeListener listener) {
        eventManager.removeAttributeListener(listener);
    }

    public void fireAttributeEvent(AbstractEvent event) {
        eventManager.fireEvent(event);
    }

    public void mergeModel(AttributeModel model) {
        if (model.getNodeTable() != null) {
            nodeTable.mergeTable(model.getNodeTable());
        }
        if (model.getEdgeTable() != null) {
            edgeTable.mergeTable(model.getEdgeTable());
        }

        for (AttributeTable table : model.getTables()) {
            if (table != model.getNodeTable() && table != model.getEdgeTable()) {
                AttributeTable existingTable = tableMap.get(table.getName());
                if (existingTable != null) {
                    ((AttributeTableImpl) existingTable).mergeTable(table);
                } else {
                    AttributeTableImpl newTable = new AttributeTableImpl(this, table.getName());
                    tableMap.put(newTable.getName(), newTable);
                    newTable.mergeTable(table);
                }
            }
        }
    }
}
