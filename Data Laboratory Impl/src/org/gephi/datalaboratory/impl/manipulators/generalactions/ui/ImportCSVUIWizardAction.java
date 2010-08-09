/*
Copyright 2008-2010 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
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
package org.gephi.datalaboratory.impl.manipulators.generalactions.ui;

import java.awt.Component;
import java.awt.Dialog;
import java.io.File;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import javax.swing.JComponent;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.datalaboratory.api.AttributeColumnsController;
import org.gephi.datalaboratory.api.DataTablesController;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.lookup.ServiceProvider;

// An example action demonstrating how the wizard could be called from within
// your code. You can copy-paste the code below wherever you need.
@ServiceProvider(service = ImportCSVUIWizardAction.class)
public final class ImportCSVUIWizardAction extends CallableSystemAction {

    public enum Mode {

        NODES_TABLE,
        EDGES_TABLE
    }
    private WizardDescriptor.Panel[] panels;
    private ImportCSVUIWizardPanel1 step1;
    private ImportCSVUIWizardPanel2 step2;
    private WizardDescriptor wizardDescriptor;

    public void performAction() {
        wizardDescriptor = new WizardDescriptor(getPanels());
        step1.setWizardDescriptor(wizardDescriptor);
        step2.setWizardDescriptor(wizardDescriptor);
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}"));
        wizardDescriptor.setTitle(getName());
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        dialog.setVisible(true);
        dialog.toFront();
        boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            //General parameters:
            File file = (File) wizardDescriptor.getProperty("file");
            Character separator = (Character) wizardDescriptor.getProperty("separator");
            Charset charset = (Charset) wizardDescriptor.getProperty("charset");
            String[] columnNames = (String[]) wizardDescriptor.getProperty("columns-names");
            AttributeType[] columnTypes = (AttributeType[]) wizardDescriptor.getProperty("columns-types");

            //Nodes import parameters:
            Boolean assignNewNodeIds = (Boolean) wizardDescriptor.getProperty("assign-new-node-ids");
            //Edges import parameters:
            Boolean createNewNodes = (Boolean) wizardDescriptor.getProperty("create-new-nodes");

            AttributeColumnsController ac = Lookup.getDefault().lookup(AttributeColumnsController.class);
            switch ((Mode) wizardDescriptor.getProperty("mode")) {
                case NODES_TABLE:
                    ac.importCSVToNodesTable(file, separator, charset, columnNames, columnTypes, assignNewNodeIds);
                    break;
                case EDGES_TABLE:
                    ac.importCSVToEdgesTable(file, separator, charset, columnNames, columnTypes, createNewNodes);
                    break;
            }
            Lookup.getDefault().lookup(DataTablesController.class).refreshCurrentTable();
        }
    }

    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private WizardDescriptor.Panel[] getPanels() {
        if (panels == null) {
            panels = new WizardDescriptor.Panel[]{
                        step1 = new ImportCSVUIWizardPanel1(),
                        step2 = new ImportCSVUIWizardPanel2()
                    };
            String[] steps = new String[panels.length];


            for (int i = 0; i
                    < panels.length; i++) {
                Component c = panels[i].getComponent();
                // Default step name to component name of panel. Mainly useful
                // for getting the name of the target chooser to appear in the
                // list of steps.
                steps[i] = c.getName();


                if (c instanceof JComponent) { // assume Swing components
                    JComponent jc = (JComponent) c;
                    // Sets step number of a component
                    // TODO if using org.openide.dialogs >= 7.8, can use WizardDescriptor.PROP_*:
                    jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i));
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
        return panels;
    }

    public String getName() {
        return NbBundle.getMessage(ImportCSVUIWizardAction.class, "ImportCSVUIWizardAction.name");
    }

    @Override
    public String iconResource() {
        return null;
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
}
