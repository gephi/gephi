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
package org.gephi.datalaboratory.impl.manipulators.generalactions;

import javax.swing.Icon;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.datalaboratory.api.AttributesController;
import org.gephi.datalaboratory.api.DataTablesController;
import org.gephi.datalaboratory.impl.manipulators.generalactions.ui.SearchReplaceUI;
import org.gephi.datalaboratory.spi.ManipulatorUI;
import org.gephi.datalaboratory.spi.generalactions.GeneralActionsManipulator;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * GeneralActionsManipulator that shows a UI for doing search/replace tasks with normal and regex features.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
@ServiceProvider(service=GeneralActionsManipulator.class)
public class SearchReplace implements GeneralActionsManipulator {

    public void execute() {
        SearchReplaceUI ui=Lookup.getDefault().lookup(SearchReplaceUI.class);
        if(Lookup.getDefault().lookup(DataTablesController.class).isNodeTableMode()){
            ui.setMode(SearchReplaceUI.Mode.NODES_TABLE);
        }else{
            ui.setMode(SearchReplaceUI.Mode.EDGES_TABLE);
        }
        DialogDescriptor dd=new DialogDescriptor(ui, getName());
        dd.setModal(false);
        dd.setOptions(new Object[]{NbBundle.getMessage(SearchReplace.class, "SearchReplace.window.close")});
        DialogDisplayer.getDefault().notify(dd);
    }

    public String getName() {
        return NbBundle.getMessage(SearchReplace.class, "SearchReplace.name");
    }

    public String getDescription() {
        return "";
    }

    public boolean canExecute() {
        AttributeTable currentTable=getCurrentTable();
        return Lookup.getDefault().lookup(AttributesController.class).getTableRowsCount(currentTable) > 0;//Make sure that there is at least 1 row
    }

    public ManipulatorUI getUI() {
        return null;
    }

    public int getType() {
        return 100;
    }

    public int getPosition() {
        return 0;
    }

    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("org/gephi/datalaboratory/impl/manipulators/resources/binocular--pencil.png", true);
    }

    private AttributeTable getCurrentTable(){
        if(Lookup.getDefault().lookup(DataTablesController.class).isNodeTableMode()){
            return Lookup.getDefault().lookup(AttributeController.class).getModel().getNodeTable();
        }else{
            return Lookup.getDefault().lookup(AttributeController.class).getModel().getEdgeTable();
        }
    }
}
