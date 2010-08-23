/*
Copyright 2008-2010 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
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
package org.gephi.datalaboratory.impl.manipulators.generalactions.ui;

import com.csvreader.CsvReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.datalaboratory.impl.manipulators.generalactions.ui.ImportCSVUIWizardAction.Mode;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

public final class ImportCSVUIVisualPanel2 extends JPanel {

    private final ImportCSVUIWizardPanel2 wizard2;
    private Character separator;
    private File file;
    private ImportCSVUIWizardAction.Mode mode;
    private JCheckBox[] columnsCheckBoxes;
    private JComboBox[] columnsComboBoxes;
    private AttributeTable table;
    private Charset charset;
    //Nodes table settings:
    private JCheckBox assignNewNodeIds;
    //Edges table settings:
    private JCheckBox createNewNodes;

    /** Creates new form ImportCSVUIVisualPanel2 */
    public ImportCSVUIVisualPanel2(ImportCSVUIWizardPanel2 wizard2) {
        initComponents();
        this.wizard2 = wizard2;
    }

    public void reloadSettings() {
        if (separator != null && file != null && file.exists() && mode != null && charset != null) {
            JPanel settingsPanel = new JPanel();
            settingsPanel.setLayout(new MigLayout());
            loadDescription(settingsPanel);
            switch (mode) {
                case NODES_TABLE:
                    table = Lookup.getDefault().lookup(AttributeController.class).getModel().getNodeTable();
                    loadColumns(settingsPanel);
                    loadNodesTableSettings(settingsPanel);
                    break;
                case EDGES_TABLE:
                    table = Lookup.getDefault().lookup(AttributeController.class).getModel().getEdgeTable();
                    loadColumns(settingsPanel);
                    loadEdgesTableSettings(settingsPanel);
                    break;
            }

            scroll.setViewportView(settingsPanel);
        }
        wizard2.fireChangeEvent();//Enable/disable finish button
    }

    private void loadDescription(JPanel settingsPanel) {
        JLabel descriptionLabel = new JLabel();
        switch (mode) {
            case NODES_TABLE:
                descriptionLabel.setText(getMessage("ImportCSVUIVisualPanel2.nodes.description"));
                break;
            case EDGES_TABLE:
                descriptionLabel.setText(getMessage("ImportCSVUIVisualPanel2.edges.description"));
                break;
        }
        settingsPanel.add(descriptionLabel, "wrap 15px");
    }

    private void loadColumns(JPanel settingsPanel) {
        try {
            JLabel columnsLabel = new JLabel(getMessage("ImportCSVUIVisualPanel2.columnsLabel.text"));
            settingsPanel.add(columnsLabel, "wrap");

            CsvReader reader = new CsvReader(new FileInputStream(file), separator, charset);
            reader.setTrimWhitespace(false);
            reader.readHeaders();
            final String[] columns = reader.getHeaders();
            reader.close();

            boolean sourceFound = false, targetFound = false, typeFound=false;//Only first source and target columns found will be used as source and target nodes ids.
            columnsCheckBoxes = new JCheckBox[columns.length];
            columnsComboBoxes = new JComboBox[columns.length];
            for (int i = 0; i < columns.length; i++) {
                columnsCheckBoxes[i] = new JCheckBox(columns[i], true);
                settingsPanel.add(columnsCheckBoxes[i], "wrap");
                columnsComboBoxes[i] = new JComboBox();
                fillComboBoxWithColumnTypes(columns[i], columnsComboBoxes[i]);
                settingsPanel.add(columnsComboBoxes[i], "wrap 15px");

                if (mode == ImportCSVUIWizardAction.Mode.EDGES_TABLE && columns[i].equalsIgnoreCase("source") && !sourceFound) {
                    sourceFound = true;
                    //Do not allow to not select source column:
                    columnsCheckBoxes[i].setEnabled(false);
                    columnsComboBoxes[i].setEnabled(false);
                }
                if (mode == ImportCSVUIWizardAction.Mode.EDGES_TABLE && columns[i].equalsIgnoreCase("target") && !targetFound) {
                    targetFound = true;
                    //Do not allow to not select source column:
                    columnsCheckBoxes[i].setEnabled(false);
                    columnsComboBoxes[i].setEnabled(false);
                }
                if (mode == ImportCSVUIWizardAction.Mode.EDGES_TABLE && columns[i].equalsIgnoreCase("type") && !typeFound) {
                    typeFound = true;
                    //Do not allow to not select source column:
                    columnsComboBoxes[i].setEnabled(false);
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void fillComboBoxWithColumnTypes(String column, JComboBox comboBox) {
        comboBox.removeAllItems();
        for (AttributeType type : AttributeType.values()) {
            comboBox.addItem(type);
        }
        if (table.hasColumn(column)) {
            //Set type of the already existing column in the table and disable the edition:
            comboBox.setSelectedItem(table.getColumn(column).getType());
            comboBox.setEnabled(false);
        } else {
            comboBox.setSelectedItem(AttributeType.STRING);//Set STRING by default
        }
    }

    private void loadNodesTableSettings(JPanel settingsPanel) {
        assignNewNodeIds = new JCheckBox(getMessage("ImportCSVUIVisualPanel2.nodes.assign-ids-checkbox"), true);
        settingsPanel.add(assignNewNodeIds, "wrap");
    }

    private void loadEdgesTableSettings(JPanel settingsPanel) {
        createNewNodes = new JCheckBox(getMessage("ImportCSVUIVisualPanel2.edges.create-new-nodes-checkbox"), true);
        settingsPanel.add(createNewNodes, "wrap");
    }

    public boolean isValidCSV() {
        return true;
    }

    public String[] getColumnsToImport() {
        ArrayList<String> columns = new ArrayList<String>();
        for (JCheckBox columnCheckBox : columnsCheckBoxes) {
            if (columnCheckBox.isSelected()) {
                columns.add(columnCheckBox.getText());
            }
        }
        return columns.toArray(new String[0]);
    }

    public AttributeType[] getColumnsToImportTypes() {
        ArrayList<AttributeType> types = new ArrayList<AttributeType>();
        for (int i = 0; i < columnsCheckBoxes.length; i++) {
            if (columnsCheckBoxes[i].isSelected()) {
                types.add((AttributeType) columnsComboBoxes[i].getSelectedItem());
            }
        }
        return types.toArray(new AttributeType[0]);
    }

    public boolean getAssignNewNodeIds() {
        return assignNewNodeIds != null ? assignNewNodeIds.isSelected() : false;
    }

    public boolean getCreateNewNodes() {
        return createNewNodes != null ? createNewNodes.isSelected() : false;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(ImportCSVUIVisualPanel2.class, "ImportCSVUIVisualPanel2.name");
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public Character getSeparator() {
        return separator;
    }

    public void setSeparator(Character separator) {
        this.separator = separator;
    }

    public Charset getCharset() {
        return charset;
    }

    void setCharset(Charset charset) {
        this.charset = charset;
    }

    private String getMessage(String resName) {
        return NbBundle.getMessage(ImportCSVUIVisualPanel2.class, resName);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
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
