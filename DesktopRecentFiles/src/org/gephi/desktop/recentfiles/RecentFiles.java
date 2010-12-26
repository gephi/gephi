/*
Copyright 2008-2010 Gephi
Authors : Sébastien Heymann <sebastien.heymann@gephi.org>
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
package org.gephi.desktop.recentfiles;

import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.gephi.desktop.importer.api.ImportControllerUI;
import org.gephi.desktop.mrufiles.api.MostRecentFiles;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

/**
 *
 * @author Sébastien Heymann
 */
public class RecentFiles extends CallableSystemAction {

    @Override
    public void performAction() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getName() {
        return "recentfiles";
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

    @Override
    public JMenuItem getMenuPresenter() {
        JMenu menu = new JMenu(NbBundle.getMessage(RecentFiles.class, "CTL_OpenRecentFiles"));

        MostRecentFiles mru = Lookup.getDefault().lookup(MostRecentFiles.class);
        for (String filePath : mru.getMRUFileList()) {
            final File file = new File(filePath);
            if (file.exists()) {
                final String fileName = file.getName();
                JMenuItem menuItem = new JMenuItem(new AbstractAction(fileName) {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        ImportControllerUI importController = Lookup.getDefault().lookup(ImportControllerUI.class);
                        importController.importFile(FileUtil.toFileObject(file));
                    }
                });
                menu.add(menuItem);
            }
        }
        return menu;
    }
}
