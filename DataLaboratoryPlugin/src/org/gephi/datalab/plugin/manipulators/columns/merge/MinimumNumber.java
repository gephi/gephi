/*
Copyright 2008-2010 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.datalab.plugin.manipulators.columns.merge;

import javax.swing.Icon;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeUtils;
import org.gephi.datalab.api.AttributeColumnsMergeStrategiesController;
import org.gephi.datalab.plugin.manipulators.columns.merge.ui.GeneralColumnTitleChooserUI;
import org.gephi.datalab.spi.ManipulatorUI;
import org.gephi.datalab.spi.columns.merge.AttributeColumnsMergeStrategy;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * AttributeColumnsMergeStrategy for any combination of number or number list columns that
 * calculates the minimum value of all the values and creates a new BigDecimal column with the result of each row.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public class MinimumNumber implements AttributeColumnsMergeStrategy {

    private AttributeTable table;
    private AttributeColumn[] columns;
    private String columnTitle;

    public void setup(AttributeTable table, AttributeColumn[] columns) {
        this.table = table;
        this.columns = columns;
    }

    public void execute() {
        Lookup.getDefault().lookup(AttributeColumnsMergeStrategiesController.class).minValueNumbersMerge(table, columns, columnTitle);
    }

    public String getName() {
        return NbBundle.getMessage(MinimumNumber.class, "MinimumNumber.name");
    }

    public String getDescription() {
        return NbBundle.getMessage(MinimumNumber.class, "MinimumNumber.description");
    }

    public boolean canExecute() {
        return AttributeUtils.getDefault().areAllNumberOrNumberListColumns(columns);
    }

    public ManipulatorUI getUI() {
        return new GeneralColumnTitleChooserUI();
    }

    public int getType() {
        return 100;
    }

    public int getPosition() {
        return 600;
    }

    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("org/gephi/datalab/plugin/manipulators/resources/minus-white.png", true);
    }

    public AttributeTable getTable() {
        return table;
    }

    public String getColumnTitle() {
        return columnTitle;
    }

    public void setColumnTitle(String columnTitle) {
        this.columnTitle = columnTitle;
    }
}
