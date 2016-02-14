/*
Copyright 2008-2010 Gephi
Authors : Yi Du <duyi001@gmail.com>
          Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
 */
package org.gephi.desktop.importer;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import org.gephi.desktop.importer.api.ImportControllerUI;
import org.gephi.io.importer.spi.ImporterWizardUI;
import org.gephi.io.importer.spi.WizardImporter;
import org.gephi.io.importer.spi.WizardImporterBuilder;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

@ActionID(id = "org.gephi.desktop.importer.ImportWizard", category = "File")
@ActionRegistration(displayName = "#CTL_ImportWizard", iconInMenu = true)
@ActionReference(path = "Menu/File", position = 675)
public final class ImportWizard implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        WizardIterator wizardIterator = new WizardIterator();
        WizardDescriptor wizardDescriptor = new WizardDescriptor(wizardIterator);
        wizardDescriptor.setTitleFormat(new MessageFormat("{0} ({1})"));
        wizardDescriptor.setTitle(NbBundle.getMessage(getClass(), "ImportWizard.wizard.title"));
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        dialog.setVisible(true);
        dialog.toFront();

        boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            ImporterWizardUI wizardUI = wizardIterator.getCurrentWizardUI();

            //Get Importer
            WizardImporter importer = null;
            for (WizardImporterBuilder wizardBuilder : Lookup.getDefault().lookupAll(WizardImporterBuilder.class)) {
                WizardImporter im = wizardBuilder.buildImporter();
                if (wizardUI.isUIForImporter(im)) {
                    importer = im;
                }
            }

            if (importer == null) {
                NotifyDescriptor.Message msg = new NotifyDescriptor.Message(NbBundle.getMessage(getClass(), "ImportWizard.error_no_matching_importer"), NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notify(msg);
                return;
            }

            //Unsetup
            wizardIterator.unsetupPanels(importer);

            ImportControllerUI importControllerUI = Lookup.getDefault().lookup(ImportControllerUI.class);
            importControllerUI.importWizard(importer);
        }
    }
}
