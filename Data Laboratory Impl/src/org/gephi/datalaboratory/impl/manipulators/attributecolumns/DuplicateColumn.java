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
import org.gephi.datalaboratory.impl.manipulators.attributecolumns.ui.DuplicateColumnUI;
import org.gephi.datalaboratory.spi.attributecolumns.AttributeColumnsManipulator;
import org.gephi.datalaboratory.spi.attributecolumns.AttributeColumnsManipulatorUI;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * AttributeColumnsManipulator that duplicate a AttributeColumn of a AttributeTable setting the same values for the rows.
 * Allows the user to select the title and AttributeType of the new column in the UI
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
@ServiceProvider(service = AttributeColumnsManipulator.class)
public class DuplicateColumn implements AttributeColumnsManipulator {
    private String title;
    private AttributeType columnType;

    public void execute(AttributeTable table, AttributeColumn column) {
        Lookup.getDefault().lookup(AttributeColumnsController.class).duplicateColumn(table, column, title, columnType);
    }

    public String getName() {
        return NbBundle.getMessage(DuplicateColumn.class, "DuplicateColumn.name");
    }

    public String getDescription() {
        return "";
    }

    public boolean canManipulateColumn(AttributeTable table, AttributeColumn column) {
        return true;
    }

    public AttributeColumnsManipulatorUI getUI() {
        return new DuplicateColumnUI();
    }

    public int getType() {
        return 0;
    }

    public int getPosition() {
        return 400;
    }

    public Image getIcon() {
        return ImageUtilities.loadImage("org/gephi/datalaboratory/impl/manipulators/resources/table-duplicate-column.png");
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public AttributeType getColumnType() {
        return columnType;
    }

    public void setColumnType(AttributeType columnType) {
        this.columnType = columnType;
    }
}
