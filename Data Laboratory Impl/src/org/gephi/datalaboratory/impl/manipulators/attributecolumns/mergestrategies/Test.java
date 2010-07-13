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
import javax.swing.JOptionPane;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.datalaboratory.spi.ManipulatorUI;
import org.gephi.datalaboratory.spi.attributecolumns.mergestrategies.AttributeColumnsMergeStrategy;
import org.openide.util.ImageUtilities;

/**
 * Test
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public class Test implements AttributeColumnsMergeStrategy{
    private AttributeColumn[] columns;

    public void setup(AttributeTable table, AttributeColumn[] columns) {
        this.columns=columns;
    }

    public void execute() {
        JOptionPane.showMessageDialog(null, "Test");
    }

    public String getName() {
        return "Test";
    }

    public String getDescription() {
        return "Test test test Test test";
    }

    public boolean canExecute() {
        return true;
    }

    public ManipulatorUI getUI() {
        return null;
    }

    public int getType() {
        return 0;
    }

    public int getPosition() {
        return 0;
    }

    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("org/gephi/datalaboratory/impl/manipulators/resources/table-delete-column.png",true);
    }
}
