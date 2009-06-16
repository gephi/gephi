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
package org.gephi.desktop.generate;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import org.gephi.io.generator.Generator;
import org.gephi.io.generator.GeneratorController;
import org.gephi.ui.generator.GeneratorUI;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

/**
 *
 * @author Mathieu
 */
public class Generate extends CallableSystemAction {

    @Override
    public void performAction() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getName() {
        return "generate";
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

    @Override
    public JMenuItem getMenuPresenter() {
        JMenu menu = new JMenu(NbBundle.getMessage(Generate.class, "CTL_Generate"));

        final GeneratorController generatorController = Lookup.getDefault().lookup(GeneratorController.class);
        if (generatorController != null) {
            for (final Generator gen : generatorController.getGenerators()) {
                String menuName = gen.getName() + "...";
                JMenuItem menuItem = new JMenuItem(new AbstractAction(menuName) {

                    public void actionPerformed(ActionEvent e) {
                        String title = gen.getName();
                        GeneratorUI ui = gen.getUI();
                        if (ui != null) {
                            ui.setup(gen);
                            DialogDescriptor dd = new DialogDescriptor(ui.getPanel(), title);
                            Object result = DialogDisplayer.getDefault().notify(dd);
                            if (result != NotifyDescriptor.OK_OPTION) {
                                return;
                            }
                            ui.unsetup();
                        }

                        generatorController.generate(gen);
                    }
                });
                menu.add(menuItem);
            }
        }
        return menu;
    }
}
