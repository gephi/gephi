/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.desktop.spigot;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.gephi.desktop.importer.api.ImportControllerUI;
import org.gephi.io.importer.spi.DatabaseImporterBuilder;
import org.gephi.io.importer.spi.ImporterUI;
import org.gephi.io.importer.spi.SpigotImporterBuilder;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

/**
 *
 * @author Mathieu Bastian
 */
public class ImportSpigot extends CallableSystemAction {

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
        JMenu menu = new JMenu(NbBundle.getMessage(ImportSpigot.class, "CTL_ImportSpigot"));

        final ImportControllerUI importController = Lookup.getDefault().lookup(ImportControllerUI.class);
        if (importController != null) {
            for (final SpigotImporterBuilder sb : Lookup.getDefault().lookupAll(SpigotImporterBuilder.class)) {
                ImporterUI ui = importController.getImportController().getUI(sb.buildImporter());
                String menuName = sb.getName();
                if (ui != null) {
                    menuName = ui.getDisplayName();
                }
                JMenuItem menuItem = new JMenuItem(new AbstractAction(menuName) {

                    public void actionPerformed(ActionEvent e) {
                        importController.importSpigot(sb.buildImporter());
                    }
                });
                menu.add(menuItem);
            }
        }
        return menu;
    }
}
