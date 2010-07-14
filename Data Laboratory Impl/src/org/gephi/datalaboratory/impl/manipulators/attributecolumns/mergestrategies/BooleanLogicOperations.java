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
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.datalaboratory.api.AttributeColumnsMergeStrategiesController;
import org.gephi.datalaboratory.api.AttributeColumnsMergeStrategiesController.BooleanOperations;
import org.gephi.datalaboratory.impl.manipulators.attributecolumns.mergestrategies.ui.BooleanLogicOperationsUI;
import org.gephi.datalaboratory.spi.ManipulatorUI;
import org.gephi.datalaboratory.spi.attributecolumns.mergestrategies.AttributeColumnsMergeStrategy;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * 
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public class BooleanLogicOperations implements AttributeColumnsMergeStrategy{
    private AttributeTable table;
    private AttributeColumn[] columns;
    private String newColumnTitle;
    private BooleanOperations[] booleanOperations;    

    public void setup(AttributeTable table, AttributeColumn[] columns) {
        this.columns=columns;
        this.table=table;
    }

    public void execute() {
        Lookup.getDefault().lookup(AttributeColumnsMergeStrategiesController.class).booleanLogicOperationsMerge(table, columns, booleanOperations, newColumnTitle);
    }

    public String getName() {
        return NbBundle.getMessage(BooleanLogicOperations.class, "BooleanLogicOperations.name");
    }

    public String getDescription() {
        return NbBundle.getMessage(BooleanLogicOperations.class, "BooleanLogicOperations.description");
    }

    public boolean canExecute() {
        for(AttributeColumn column:columns){
            if(column.getType()!=AttributeType.BOOLEAN){
                return false;
            }
        }
        return true;
    }

    public ManipulatorUI getUI() {
        return new BooleanLogicOperationsUI();
    }

    public int getType() {
        return 0;
    }

    public int getPosition() {
        return 0;
    }

    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("org/gephi/datalaboratory/impl/manipulators/resources/script-binary.png",true);
    }

    public BooleanOperations[] getBooleanOperations() {
        return booleanOperations;
    }

    public void setBooleanOperations(BooleanOperations[] booleanOperations) {
        this.booleanOperations = booleanOperations;
    }

    public String getNewColumnTitle() {
        return newColumnTitle;
    }

    public void setNewColumnTitle(String newColumnTitle) {
        this.newColumnTitle = newColumnTitle;
    }

    public AttributeColumn[] getColumns() {
        return columns;
    }
}
