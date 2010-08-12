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
package org.gephi.datalaboratory.impl.manipulators.attributecolumns;

import java.awt.Image;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.datalaboratory.api.AttributeColumnsController;
import org.gephi.datalaboratory.api.DataTablesController;
import org.gephi.datalaboratory.spi.attributecolumns.AttributeColumnsManipulator;
import org.gephi.datalaboratory.spi.attributecolumns.AttributeColumnsManipulatorUI;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * AttributeColumnsManipulator that negates the not null values of a boolean or boolean list column.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
@ServiceProvider(service = AttributeColumnsManipulator.class)
public class NegateBooleanColumn implements AttributeColumnsManipulator {

    public void execute(AttributeTable table, AttributeColumn column) {
        Lookup.getDefault().lookup(AttributeColumnsController.class).negateBooleanColumn(table, column);
        Lookup.getDefault().lookup(DataTablesController.class).refreshCurrentTable();
    }

    public String getName() {
        return NbBundle.getMessage(NegateBooleanColumn.class, "NegateBooleanColumn.name");
    }

    public String getDescription() {
        return "";
    }

    public boolean canManipulateColumn(AttributeTable table, AttributeColumn column) {
        return column.getType()==AttributeType.BOOLEAN||column.getType()==AttributeType.LIST_BOOLEAN;
    }

    public AttributeColumnsManipulatorUI getUI(AttributeTable table,AttributeColumn column) {
        return null;
    }

    public int getType() {
        return 300;
    }

    public int getPosition() {
        return 0;
    }

    public Image getIcon() {
        return ImageUtilities.loadImage("org/gephi/datalaboratory/impl/manipulators/resources/ui-check-boxes.png");
    }
}
