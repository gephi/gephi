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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import net.miginfocom.swing.MigLayout;
import org.gephi.graph.api.TimeRepresentation;
import org.gephi.io.importer.plugin.file.spreadsheet.AbstractImporterSpreadsheet;
import org.gephi.io.importer.plugin.file.spreadsheet.process.SpreadsheetGeneralConfiguration.Mode;
import org.gephi.ui.utils.SupportedColumnTypeWrapper;
import org.gephi.ui.utils.TimeRepresentationWrapper;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

public final class WizardVisualPanel2 extends JPanel {

    private static final String ASSIGN_NEW_NODES_IDS_SAVED_PREFERENCES = "WizardVisualPanel2_assign_new_nodes_ids";
    private static final String CREATE_NEW_NODES_SAVED_PREFERENCES = "WizardVisualPanel2_create_new_nodes";
    private final WizardPanel2 wizard2;

    private JComboBox timeRepresentationComboBox = new JComboBox();
    private final ArrayList<JCheckBox> columnsCheckBoxes = new ArrayList<>();
    private final ArrayList<JComboBox> columnsComboBoxes = new ArrayList<>();

    private final AbstractImporterSpreadsheet importer;

    /**
     * Creates new form WizardVisualPanel2
     */
    public WizardVisualPanel2(final AbstractImporterSpreadsheet importer, final WizardPanel2 wizard2) {
        initComponents();
        this.importer = importer;
        this.wizard2 = wizard2;

        timeRepresentationComboBox = new JComboBox();
        for (TimeRepresentation value : TimeRepresentation.values()) {
            timeRepresentationComboBox.addItem(new TimeRepresentationWrapper(value));
        }

        timeRepresentationComboBox.setSelectedItem(new TimeRepresentationWrapper(importer.getTimeRepresentation()));

        timeRepresentationComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                importer.setTimeRepresentation(getSelectedTimeRepresentation());
                reloadSettings();
            }
        });
    }

    public void reloadSettings() {
        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new MigLayout("fillx"));
        createTimeRepresentationComboBox(settingsPanel);

        settingsPanel.add(new JSeparator(), "growx, wrap");

        loadColumns(settingsPanel);

        scroll.setViewportView(settingsPanel);
        wizard2.fireChangeEvent();//Enable/disable finish button
    }

    private void createTimeRepresentationComboBox(JPanel settingsPanel) {
        JLabel timeRepresentationLabel = new JLabel(getMessage("WizardVisualPanel2.timeRepresentationLabel.text"));

        settingsPanel.add(timeRepresentationLabel, "wrap");
        settingsPanel.add(timeRepresentationComboBox, "wrap 15px");
    }

    private void loadColumns(JPanel settingsPanel) {
        try {
            columnsCheckBoxes.clear();
            columnsComboBoxes.clear();
            JLabel columnsLabel = new JLabel(getMessage("WizardVisualPanel2.columnsLabel.text"));
            settingsPanel.add(columnsLabel, "wrap");

            final String[] headers = importer.getHeadersMap().keySet().toArray(new String[0]);

            final Mode mode = importer.getMode();

            for (String header : headers) {
                if (header.isEmpty()) {
                    continue;//Remove empty column headers:
                }

                JCheckBox columnCheckBox = new JCheckBox(header, true);

                if (importer.getMode() == Mode.EDGES_TABLE && (header.equalsIgnoreCase("source") || header.equalsIgnoreCase("target"))) {
                    columnCheckBox.setEnabled(false);
                }

                columnsCheckBoxes.add(columnCheckBox);
                JComboBox columnComboBox = new JComboBox();

                if (mode.isSpecialColumn(header)) {
                    settingsPanel.add(columnCheckBox, "wrap 15px");

                    //Special columns such as id, label, source and target... don't need a type selector
                    //The type is not used by the importer anyway
                    columnsComboBoxes.add(null);
                } else {
                    settingsPanel.add(columnCheckBox, "wrap");

                    columnsComboBoxes.add(columnComboBox);
                    fillComboBoxWithColumnTypes(header, columnComboBox);
                    settingsPanel.add(columnComboBox, "wrap 15px");
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void fillComboBoxWithColumnTypes(String column, JComboBox comboBox) {
        comboBox.removeAllItems();
        List<SupportedColumnTypeWrapper> supportedTypesWrappers = SupportedColumnTypeWrapper.buildOrderedSupportedTypesList(importer.getTimeRepresentation());

        for (SupportedColumnTypeWrapper supportedColumnTypeWrapper : supportedTypesWrappers) {
            comboBox.addItem(supportedColumnTypeWrapper);
        }

        Class defaultClass = importer.getColumnClass(column);
        if (defaultClass == null) {
            defaultClass = String.class;//Default
        }

        SupportedColumnTypeWrapper selection = new SupportedColumnTypeWrapper(defaultClass);
        if (!supportedTypesWrappers.contains(selection)) {
            selection = new SupportedColumnTypeWrapper(String.class);//Default
        }
        comboBox.setSelectedItem(selection);
    }

    public TimeRepresentation getSelectedTimeRepresentation() {
        return ((TimeRepresentationWrapper) timeRepresentationComboBox.getSelectedItem()).getTimeRepresentation();
    }

    public String[] getColumnsToImport() {
        ArrayList<String> columns = new ArrayList<>();
        for (JCheckBox columnCheckBox : columnsCheckBoxes) {
            if (columnCheckBox.isSelected()) {
                columns.add(columnCheckBox.getText());
            }
        }
        return columns.toArray(new String[0]);
    }

    public Class[] getColumnsToImportTypes() {
        ArrayList<Class> types = new ArrayList<>();
        for (int i = 0; i < columnsCheckBoxes.size(); i++) {
            if (columnsCheckBoxes.get(i).isSelected()) {

                JComboBox columnComboBox = columnsComboBoxes.get(i);
                Class type;

                if (columnComboBox != null) {
                    type = ((SupportedColumnTypeWrapper) columnComboBox.getSelectedItem()).getType();
                } else {
                    type = String.class;
                }
                types.add(type);
            }
        }
        return types.toArray(new Class[0]);
    }
    
    @Override
    public String getName() {
        return NbBundle.getMessage(WizardVisualPanel2.class, "WizardVisualPanel2.name");
    }

    private String getMessage(String resName) {
        return NbBundle.getMessage(WizardVisualPanel2.class, resName);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scroll = new javax.swing.JScrollPane();

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(scroll, javax.swing.GroupLayout.DEFAULT_SIZE, 290, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(scroll, javax.swing.GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane scroll;
    // End of variables declaration//GEN-END:variables
}
