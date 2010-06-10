/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
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
package org.gephi.desktop.importer;

import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import org.gephi.desktop.importer.api.ImportControllerUI;

import org.gephi.neo4j.api.Neo4jImporter;
import org.gephi.utils.longtask.api.LongTaskExecutor;
import org.gephi.utils.longtask.spi.LongTask;

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

        JMenuItem neo4jLocalDatabaseMenuItem = new JMenuItem(new AbstractAction("Neo4j local database") {

            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Choose local Neo4j directory");
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int dialogResult = fileChooser.showOpenDialog(null);

                if (dialogResult == JFileChooser.APPROVE_OPTION) {
                    final File neo4jDirectory = fileChooser.getSelectedFile();
                    final Neo4jImporter neo4jImporter = Lookup.getDefault().lookup(Neo4jImporter.class);

                    LongTaskExecutor executor = new LongTaskExecutor(true);
                    executor.execute((LongTask) neo4jImporter, new Runnable() {
                        @Override
                        public void run() {
                            neo4jImporter.importLocal(neo4jDirectory);
                        }
                    });
                }
            }
        });
        menu.add(neo4jLocalDatabaseMenuItem);

        JMenuItem neo4jRemoteDatabaseMenuItem = new JMenuItem(new AbstractAction("Neo4j remote database") {

            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "Not implemented yet...", "Neo4j remote database support", JOptionPane.ERROR_MESSAGE);
            }
        });
        menu.add(neo4jRemoteDatabaseMenuItem);

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
