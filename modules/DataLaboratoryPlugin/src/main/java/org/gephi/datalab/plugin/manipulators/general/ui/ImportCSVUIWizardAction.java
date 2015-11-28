/*
 Copyright 2008-2010 Gephi
 Authors : Eduardo Ramos <eduramiba@gmail.com>
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
package org.gephi.datalab.plugin.manipulators.general.ui;

import java.awt.Component;
import java.awt.Dialog;
import java.io.File;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import org.gephi.datalab.api.AttributeColumnsController;
import org.gephi.datalab.api.datatables.DataTablesController;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
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

    @Override
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
            Class[] columnTypes = (Class[]) wizardDescriptor.getProperty("columns-types");

            //Nodes import parameters:
            Boolean assignNewNodeIds = (Boolean) wizardDescriptor.getProperty("assign-new-node-ids");
            //Edges import parameters:
            Boolean createNewNodes = (Boolean) wizardDescriptor.getProperty("create-new-nodes");

            Graph graph = Lookup.getDefault().lookup(GraphController.class).getGraphModel().getGraph();
            AttributeColumnsController ac = Lookup.getDefault().lookup(AttributeColumnsController.class);
            DataTablesController dtc = Lookup.getDefault().lookup(DataTablesController.class);
            dtc.setAutoRefreshEnabled(false);
            
            try {
                switch ((Mode) wizardDescriptor.getProperty("mode")) {
                    case NODES_TABLE:
                        ac.importCSVToNodesTable(graph, file, separator, charset, columnNames, columnTypes, assignNewNodeIds);
                        break;
                    case EDGES_TABLE:
                        ac.importCSVToEdgesTable(graph, file, separator, charset, columnNames, columnTypes, createNewNodes);
                        break;
                }
                dtc.refreshCurrentTable();
            } catch(Exception e){
                Logger.getLogger("").log(Level.SEVERE, null, e);
            } finally {
                dtc.setAutoRefreshEnabled(true);
            }
        }
        step1.unSetup();
        step2.unSetup();
    }

    /**
     * Initialize panels representing individual wizard's steps and sets various properties for them influencing wizard appearance.
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

    @Override
    public String getName() {
        return NbBundle.getMessage(ImportCSVUIWizardAction.class, "ImportCSVUIWizardAction.name");
    }

    @Override
    public String iconResource() {
        return null;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
}
