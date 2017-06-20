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
package org.gephi.ui.importer.plugin.spreadsheet;

import javax.swing.JPanel;
import org.gephi.io.importer.plugin.file.spreadsheet.ImporterSpreadsheetCSVBuilder;
import org.gephi.io.importer.plugin.file.spreadsheet.ImporterSpreadsheetExcel;
import org.gephi.io.importer.spi.Importer;
import org.gephi.io.importer.spi.ImporterUI;
import org.gephi.ui.importer.plugin.spreadsheet.wizard.ImportExcelUIWizard;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Eduardo Ramos
 */
@ServiceProvider(service = ImporterUI.class)
public class SpreadsheetImporterExcelUI implements ImporterUI, ImporterUI.WithWizard {

    private ImporterSpreadsheetExcel[] importers;
    private ImportExcelUIWizard wizard;

    @Override
    public void setup(Importer[] importers) {
        this.importers = (ImporterSpreadsheetExcel[]) importers;
        for (ImporterSpreadsheetExcel importer : this.importers) {
            importer.refreshAutoDetections();
        }
    }

    @Override
    public JPanel getPanel() {
        return null;
    }

    @Override
    public void unsetup(boolean update) {
        //NOOP
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(getClass(), "Spreadsheet.displayName", "Excel");
    }

    public String getIdentifier() {
        return ImporterSpreadsheetCSVBuilder.IDENTIFER;
    }

    @Override
    public boolean isUIForImporter(Importer importer) {
        return importer instanceof ImporterSpreadsheetExcel;
    }

    @Override
    public WizardDescriptor getWizardDescriptor() {
        this.wizard = new ImportExcelUIWizard(importers);
        return wizard.getDescriptor();
    }
}
