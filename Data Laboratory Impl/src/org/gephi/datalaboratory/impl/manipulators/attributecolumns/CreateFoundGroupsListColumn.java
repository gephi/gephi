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
import org.gephi.datalaboratory.api.AttributeColumnsController;
import org.gephi.datalaboratory.impl.manipulators.attributecolumns.ui.GeneralCreateColumnFromRegexUI;
import org.gephi.datalaboratory.spi.attributecolumns.AttributeColumnsManipulator;
import org.gephi.datalaboratory.spi.attributecolumns.AttributeColumnsManipulatorUI;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * AttributeColumnsManipulator that creates a new string list column from the given column and regular expression with values that are
 * the list of matching groups of the given regular expression.
 * Allows the user to select the title of the new column in the UI
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
@ServiceProvider(service = AttributeColumnsManipulator.class)
public class CreateFoundGroupsListColumn extends GeneralCreateColumnFromRegex {

    public void execute(AttributeTable table, AttributeColumn column) {
        if (pattern != null) {
            Lookup.getDefault().lookup(AttributeColumnsController.class).createFoundGroupsListColumn(table, column, title, pattern);
        }
    }

    public String getName() {
        return NbBundle.getMessage(CreateFoundGroupsListColumn.class, "CreateFoundGroupsListColumn.name");
    }

    public String getDescription() {
        return NbBundle.getMessage(CreateFoundGroupsListColumn.class, "CreateFoundGroupsListColumn.description");
    }

    public boolean canManipulateColumn(AttributeTable table, AttributeColumn column) {
        AttributeColumnsController ac = Lookup.getDefault().lookup(AttributeColumnsController.class);
        return ac.getTableRowsCount(table)>0;//Make sure that there is at least 1 row
    }

    public AttributeColumnsManipulatorUI getUI() {
        GeneralCreateColumnFromRegexUI ui = new GeneralCreateColumnFromRegexUI();
        ui.setMode(GeneralCreateColumnFromRegexUI.Mode.MATCHING_GROUPS);
        return ui;
    }

    public int getType() {
        return 200;
    }

    public int getPosition() {
        return 100;
    }

    public Image getIcon() {
        return ImageUtilities.loadImage("org/gephi/datalaboratory/impl/manipulators/resources/binocular--arrow.png");
    }
}
