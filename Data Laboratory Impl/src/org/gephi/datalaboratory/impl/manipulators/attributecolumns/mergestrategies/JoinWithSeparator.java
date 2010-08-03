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
package org.gephi.datalaboratory.impl.manipulators.attributecolumns.mergestrategies;

import javax.swing.Icon;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.datalaboratory.api.AttributeColumnsMergeStrategiesController;
import org.gephi.datalaboratory.impl.manipulators.attributecolumns.mergestrategies.ui.JoinWithSeparatorUI;
import org.gephi.datalaboratory.spi.ManipulatorUI;
import org.gephi.datalaboratory.spi.attributecolumns.mergestrategies.AttributeColumnsMergeStrategy;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * AttributeColumnsMergeStrategy that joins columns of any type into a new column
 * using the separator string that the user provides.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public class JoinWithSeparator implements AttributeColumnsMergeStrategy{
    private static final String DEFAULT_SEPARATOR=",";

    private AttributeTable table;
    private AttributeColumn[] columns;
    private String newColumnTitle, separator=DEFAULT_SEPARATOR;

    public void setup(AttributeTable table, AttributeColumn[] columns) {
        this.table=table;
        this.columns=columns;
    }

    public void execute() {
        Lookup.getDefault().lookup(AttributeColumnsMergeStrategiesController.class).joinWithSeparatorMerge(table, columns, null, newColumnTitle, separator);
    }

    public String getName() {
        return NbBundle.getMessage(JoinWithSeparator.class, "JoinWithSeparator.name");
    }

    public String getDescription() {
        return NbBundle.getMessage(JoinWithSeparator.class, "JoinWithSeparator.description");
    }

    public boolean canExecute() {
        return true;
    }

    public ManipulatorUI getUI() {
        return new JoinWithSeparatorUI();
    }

    public int getType() {
        return 0;
    }

    public int getPosition() {
        return 0;
    }

    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("org/gephi/datalaboratory/impl/manipulators/resources/join.png",true);
    }

    public String getNewColumnTitle() {
        return newColumnTitle;
    }

    public void setNewColumnTitle(String newColumnTitle) {
        this.newColumnTitle = newColumnTitle;
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public AttributeTable getTable() {
        return table;
    }
}
