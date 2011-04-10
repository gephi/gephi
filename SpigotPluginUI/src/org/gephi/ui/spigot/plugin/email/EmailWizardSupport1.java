/*
Copyright 2008-2010 Gephi
Authors : Yi Du <duyi001@gmail.com>
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

package org.gephi.ui.spigot.plugin.email;

import org.gephi.io.importer.spi.Importer;
import org.gephi.io.importer.spi.ImporterWizardUI;
import org.gephi.io.importer.spi.SpigotImporter;
import org.gephi.io.spigot.plugin.EmailImporter;
import org.openide.WizardDescriptor.Panel;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Yi Du
 */

@ServiceProvider(service=ImporterWizardUI.class)
public class EmailWizardSupport1 implements ImporterWizardUI{
    private Panel[] panels = null;
//    private EmailImporter currentImporter = null;

    @Override
    public String getDescription() {
        return NbBundle.getMessage(EmailWizardSupport1.class, "EmailWizardSupport1.Description");
    }

    @Override
    public Panel[] getPanels() {
        if (panels == null) {
            panels = new Panel[2];
            panels[0] = new EmailWizardPanel1();
            panels[1] = new EmailWizardPanel2();
        }
        return panels;
    }

//    public int getNumOfPanels() {
//        return 2;
//    }

//
//    public SpigotImporter generateImporter() {
//        if(currentImporter == null)
//            currentImporter = new EmailImporter();
//        return currentImporter;
//    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(EmailWizardSupport1.class, "EmailWizardSupport1.SubType");
    }

    @Override
    public String getCategory() {
        return NbBundle.getMessage(EmailWizardSupport1.class, "EmailWizardSupport.Type");
    }

    @Override
    public void setup(Panel panel) {
        //TODO
        return;
    }

    @Override
    public void unsetup(SpigotImporter importer, Panel panel) {
        ((EmailVisualPanel1) ((Panel) panels[0]).getComponent()).unsetup(importer);
        ((EmailVisualPanel2) ((Panel) panels[1]).getComponent()).unsetup(importer);
        return;
    }

    @Override
    public boolean isUIForImporter(Importer importer) {
        return importer instanceof EmailImporter;
    }
}
