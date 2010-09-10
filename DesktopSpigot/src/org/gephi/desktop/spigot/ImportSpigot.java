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
package org.gephi.desktop.spigot;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import org.gephi.desktop.importer.api.ImportControllerUI;
import org.gephi.io.importer.spi.ImporterWizardUI;
import org.gephi.io.importer.spi.SpigotImporter;
import org.gephi.io.importer.spi.SpigotImporterBuilder;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

public final class ImportSpigot implements ActionListener {

    public void actionPerformed(ActionEvent e) {
        SpigotWizardIterator wizardIterator = new SpigotWizardIterator();
        WizardDescriptor wizardDescriptor = new WizardDescriptor(wizardIterator);
        wizardDescriptor.setTitleFormat(new MessageFormat("{0} ({1})"));
        wizardDescriptor.setTitle(NbBundle.getMessage(getClass(), "ImportSpigot.wizard.title"));
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        dialog.setVisible(true);
        dialog.toFront();

        boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            ImporterWizardUI wizardUI = wizardIterator.getCurrentWizardUI();

            //Get Importer
            SpigotImporter importer = null;
            for (SpigotImporterBuilder spigotBuilder : Lookup.getDefault().lookupAll(SpigotImporterBuilder.class)) {
                SpigotImporter im = spigotBuilder.buildImporter();
                if (wizardUI.isUIForImporter(im)) {
                    importer = im;
                }
            }

            if (importer == null) {
                NotifyDescriptor.Message msg = new NotifyDescriptor.Message(NbBundle.getMessage(getClass(), "ImportSpigot.error_no_matching_importer"), NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notify(msg);
                return;
            }

            //Unsetup
            wizardIterator.unsetupPanels(importer);

            ImportControllerUI importControllerUI = Lookup.getDefault().lookup(ImportControllerUI.class);
            importControllerUI.importSpigot(importer);
        }
    }
}
