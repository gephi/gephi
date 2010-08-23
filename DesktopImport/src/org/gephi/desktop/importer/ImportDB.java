/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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
package org.gephi.desktop.importer;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.gephi.desktop.importer.api.ImportControllerUI;
import org.gephi.io.importer.spi.DatabaseImporterBuilder;
import org.gephi.io.importer.spi.ImporterUI;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

/**
 *
 * @author Mathieu
 */
public class ImportDB extends CallableSystemAction {

    @Override
    public void performAction() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getName() {
        return "importDB";
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

    @Override
    public JMenuItem getMenuPresenter() {
        JMenu menu = new JMenu(NbBundle.getMessage(ImportDB.class, "CTL_ImportDB"));

        final ImportControllerUI importController = Lookup.getDefault().lookup(ImportControllerUI.class);
        if (importController != null) {
            for (final DatabaseImporterBuilder dbb : Lookup.getDefault().lookupAll(DatabaseImporterBuilder.class)) {
                ImporterUI ui = importController.getImportController().getUI(dbb.buildImporter());
                String menuName = dbb.getName();
                if (ui != null) {
                    menuName = ui.getDisplayName();
                }
                JMenuItem menuItem = new JMenuItem(new AbstractAction(menuName) {

                    public void actionPerformed(ActionEvent e) {
                        importController.importDatabase(dbb.buildImporter());
                    }
                });
                menu.add(menuItem);
            }
        }
        return menu;
    }
}
