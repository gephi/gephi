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
package org.gephi.datalaboratory.impl.manipulators.attributecolumns.mergestrategies;

import javax.swing.Icon;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeUtils;
import org.gephi.datalaboratory.api.AttributeColumnsMergeStrategiesController;
import org.gephi.datalaboratory.impl.manipulators.attributecolumns.mergestrategies.ui.GeneralColumnTitleChooserUI;
import org.gephi.datalaboratory.spi.ManipulatorUI;
import org.gephi.datalaboratory.spi.attributecolumns.mergestrategies.AttributeColumnsMergeStrategy;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * AttributeColumnsMergeStrategy for any combination of number or number list columns that
 * calculates the thrid quartile (Q3) of all the values and creates a new BigDecimal column with the result of each row.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public class ThirdQuartileNumber implements AttributeColumnsMergeStrategy {

    private AttributeTable table;
    private AttributeColumn[] columns;
    private String columnTitle;

    public void setup(AttributeTable table, AttributeColumn[] columns) {
        this.table = table;
        this.columns = columns;
    }

    public void execute() {
        Lookup.getDefault().lookup(AttributeColumnsMergeStrategiesController.class).thirdQuartileNumberMerge(table, columns, columnTitle);
    }

    public String getName() {
        return NbBundle.getMessage(ThirdQuartileNumber.class, "ThirdQuartileNumber.name");
    }

    public String getDescription() {
        return NbBundle.getMessage(ThirdQuartileNumber.class, "ThirdQuartileNumber.description");
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
        return 300;
    }

    public Icon getIcon() {
        return null;
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
