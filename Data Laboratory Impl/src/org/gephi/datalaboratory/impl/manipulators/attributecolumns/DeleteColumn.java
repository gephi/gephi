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
import javax.swing.JOptionPane;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.datalaboratory.api.AttributeColumnsController;
import org.gephi.datalaboratory.api.DataTablesController;
import org.gephi.datalaboratory.spi.attributecolumns.AttributeColumnsManipulator;
import org.gephi.datalaboratory.spi.attributecolumns.AttributeColumnsManipulatorUI;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * AttributeColumnsManipulator that deletes a AttributeColumn of a AttributeTable.
 * Only allows to delete columns with DATA AttributeOrigin.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
@ServiceProvider(service = AttributeColumnsManipulator.class)
public class DeleteColumn implements AttributeColumnsManipulator {

    public void execute(AttributeTable table, AttributeColumn column) {
        if (JOptionPane.showConfirmDialog(null, NbBundle.getMessage(ClearColumnData.class, "ClearColumnData.confirmation.message", column.getTitle()), getName(), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            Lookup.getDefault().lookup(AttributeColumnsController.class).deleteAttributeColumn(table, column);
            Lookup.getDefault().lookup(DataTablesController.class).selectTable(table);
        }
    }

    public String getName() {
        return NbBundle.getMessage(DeleteColumn.class, "DeleteColumn.name");
    }

    public String getDescription() {
        return "";
    }

    public boolean canManipulateColumn(AttributeTable table, AttributeColumn column) {
        return Lookup.getDefault().lookup(AttributeColumnsController.class).canDeleteColumn(column);
    }

    public AttributeColumnsManipulatorUI getUI() {
        return null;
    }

    public int getType() {
        return 0;
    }

    public int getPosition() {
        return 0;
    }

    public Image getIcon() {
        return ImageUtilities.loadImage("org/gephi/datalaboratory/impl/manipulators/resources/table-delete-column.png");
    }
}
