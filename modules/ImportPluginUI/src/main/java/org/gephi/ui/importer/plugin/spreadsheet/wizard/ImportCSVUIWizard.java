/*
Copyright 2008-2016 Gephi
Authors : Eduardo Ramos <eduardo.ramos@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2016 Gephi Consortium. All rights reserved.

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

Portions Copyrighted 2016 Gephi Consortium.
 */
package org.gephi.ui.importer.plugin.spreadsheet.wizard;

import java.awt.Component;
import javax.swing.JComponent;
import org.gephi.io.importer.plugin.file.spreadsheet.ImporterSpreadsheetCSV;
import org.openide.WizardDescriptor;

public final class ImportCSVUIWizard {

    private final ImporterSpreadsheetCSV[] importers;
    private WizardDescriptor.Panel[] panels;
    private WizardDescriptor wizardDescriptor;

    public ImportCSVUIWizard(ImporterSpreadsheetCSV[] importers) {
        this.importers = importers;
        initDescriptor();
    }

    public WizardDescriptor getDescriptor() {
        return wizardDescriptor;
    }

    public void initDescriptor() {
        buildPanels();
        wizardDescriptor = new WizardDescriptor(panels);
    }

    /**
     * Initialize panels representing individual wizard's steps and sets various properties for them influencing wizard appearance.
     */
    private void buildPanels() {
        panels = new WizardDescriptor.Panel[importers.length * 2];
        for (int i = 0; i < importers.length; i++) {
            ImporterSpreadsheetCSV importer = importers[i];
            WizardPanel1CSV step1 = new WizardPanel1CSV(importer);
            WizardPanel2 step2 = new WizardPanel2(importer);
            
            panels[i * 2] = step1;
            panels[i * 2 + 1] = step2;
        }
        
        String[] steps = new String[panels.length];

        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            
            steps[i] = c.getName();
            
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Sets step number of a component
                // TODO if using org.openide.dialogs >= 7.8, can use WizardDescriptor.PROP_*:
                jc.putClientProperty("WizardPanel_contentSelectedIndex", i);
                // Sets steps names for a panel
                jc.putClientProperty("WizardPanel_contentData", steps);
                // Turn on subtitle creation on each step
                jc.putClientProperty("WizardPanel_autoWizardStyle", Boolean.TRUE);
                // Show steps on the left side with the image on the background
                jc.putClientProperty("WizardPanel_contentDisplayed", Boolean.TRUE);
                // Turn on numbering of all steps
                jc.putClientProperty("WizardPanel_contentNumbered", Boolean.TRUE);
            }
        }
    }
}
