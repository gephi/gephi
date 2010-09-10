/*
Copyright 2008-2010 Gephi
Authors : Yi Du <duyi001@gmail.com>
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
package org.gephi.desktop.spigot;

import java.awt.Component;
import java.util.NoSuchElementException;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.gephi.io.importer.spi.ImporterWizardUI;
import org.gephi.io.importer.spi.SpigotImporter;
import org.openide.WizardDescriptor;
import org.openide.util.Lookup;

public class SpigotWizardIterator implements WizardDescriptor.Iterator {

    private int index;
    private WizardDescriptor.Panel[] originalPanels;
    private WizardDescriptor.Panel[] panels;
    private ImporterWizardUI currentWizardUI;

    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private WizardDescriptor.Panel[] getPanels() {
        if (panels == null) {
            panels = new WizardDescriptor.Panel[]{
                        new SpigotWizardPanel1(),
                        new SpigotWizardPanel2()
                    };
            String[] steps = new String[panels.length];
            for (int i = 0; i < panels.length; i++) {
                Component c = panels[i].getComponent();
                // Default step name to component name of panel.
                steps[i] = c.getName();
                if (c instanceof JComponent) { // assume Swing components
                    JComponent jc = (JComponent) c;
                    // Sets step number of a component
                    // TODO if using org.openide.dialogs >= 7.8, can use WizardDescriptor.PROP_*:
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(i));
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
            originalPanels = panels;
        }
        return panels;
    }

    public WizardDescriptor.Panel current() {
        WizardDescriptor.Panel p = getPanels()[index];
//        if(p.getComponent() instanceof SetupablePanel){
//                ((SetupablePanel)(p.getComponent())).setup
//                        (currentSpigotSupport.generateImporter());
//        }
        return p;
    }

    public String name() {
        return index + 1 + ". from " + getPanels().length;
    }

    public boolean hasNext() {
        return index < getPanels().length - 1;
    }

    public boolean hasPrevious() {
        return index > 0;
    }

    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        //change panel if the current is the  first panel
        if (index == 0) {
            for (ImporterWizardUI wizardUi : Lookup.getDefault().lookupAll(ImporterWizardUI.class)) {
                SpigotVisualPanel1 visual1 = ((SpigotVisualPanel1) current().getComponent());
                if (visual1.getCurrentCategory().equals(wizardUi.getCategory())
                        && visual1.getCurrentSpigot().equals(wizardUi.getDisplayName())) {
                    WizardDescriptor.Panel[] spigotPanels = wizardUi.getPanels();
                    WizardDescriptor.Panel tempFirstPanel = panels[0];
                    panels = new WizardDescriptor.Panel[spigotPanels.length + 1];
                    panels[0] = tempFirstPanel;
                    for (int i = 0; i < spigotPanels.length; i++) {
                        panels[i + 1] = spigotPanels[i];
                    }
                    currentWizardUI = wizardUi;
                }
            }
            //
            repaintLeftComponent();
        }
        index++;
    }

    /**
     * ? might used for repainting
     */
    private void repaintLeftComponent() {
        String[] steps = new String[panels.length];
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            // Default step name to component name of panel.
            steps[i] = c.getName();
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Sets step number of a component
                // TODO if using org.openide.dialogs >= 7.8, can use WizardDescriptor.PROP_*:
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(i));
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

    public void previousPanel() {
        //change panel if the previous panel is the first
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        if (index == 1) {
            panels = originalPanels;
            repaintLeftComponent();
        }
        index--;
    }

    // If nothing unusual changes in the middle of the wizard, simply:
    public void addChangeListener(ChangeListener l) {
    }

    public void removeChangeListener(ChangeListener l) {
    }

    public ImporterWizardUI getCurrentWizardUI() {
        return currentWizardUI;
    }

    void unsetupPanels(SpigotImporter importer) {
        for (int i = 1; i < panels.length; i++) {
            currentWizardUI.unsetup(importer, panels[i]);
        }
    }
}
