/*
Copyright 2008-2010 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
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
package org.gephi.datalaboratory.impl;

import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.datalaboratory.api.AttributeColumnsMergeStrategiesController;
import org.gephi.datalaboratory.api.AttributeColumnsController;
import org.gephi.graph.api.Attributes;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * Implementation of the AttributeColumnsMergeStrategiesController interface
 * declared in the Data Laboratory API.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 * @see AttributeColumnsMergeStrategiesController
 */
@ServiceProvider(service = AttributeColumnsMergeStrategiesController.class)
public class AttributeColumnsMergeStrategiesControllerImpl implements AttributeColumnsMergeStrategiesController {

    public AttributeColumn booleanLogicOperationsMerge(AttributeTable table, AttributeColumn[] columnsToMerge, BooleanOperations[] booleanOperations, String newColumnTitle) {
        if (table == null || columnsToMerge == null || booleanOperations == null || booleanOperations.length != columnsToMerge.length - 1) {
            throw new IllegalArgumentException();
        }
        AttributeColumnsController ac = Lookup.getDefault().lookup(AttributeColumnsController.class);
        AttributeColumn newColumn;
        newColumn = ac.addAttributeColumn(table, newColumnTitle, AttributeType.BOOLEAN);

        Boolean value;
        Boolean secondValue;

        for (Attributes row : ac.getTableAttributeRows(table)) {
            value = (Boolean) row.getValue(columnsToMerge[0].getIndex());
            value = value != null ? value : false;//Use false if null
            for (int i = 0; i < booleanOperations.length; i++) {
                secondValue = (Boolean) row.getValue(columnsToMerge[i + 1].getIndex());
                secondValue = secondValue != null ? secondValue : false;//Use false if null
                switch (booleanOperations[i]) {
                    case AND:
                        value = value && secondValue;
                        break;
                    case OR:
                        value = value || secondValue;
                        break;
                    case XOR:
                        value = value ^ secondValue;
                        break;
                    case NAND:
                        value = !(value && secondValue);
                        break;
                    case NOR:
                        value = !(value || secondValue);
                        break;
                }
            }
            row.setValue(newColumn.getIndex(), value);
        }

        return newColumn;
    }
}
