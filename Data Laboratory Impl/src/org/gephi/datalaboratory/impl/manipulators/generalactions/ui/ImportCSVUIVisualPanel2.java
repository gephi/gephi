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

import com.csvreader.CsvReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
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

    private Character separator;
    private File file;
    private ImportCSVUIWizardAction.Mode mode;
    private JCheckBox[] columnsCheckBoxes;
    private JComboBox[] columnsComboBoxes;
    private AttributeTable table;
    private Charset charset;

    /** Creates new form ImportCSVUIVisualPanel2 */
    public ImportCSVUIVisualPanel2() {
        initComponents();
        columnsPanel.setLayout(new MigLayout());
        settingsPanel.setLayout(new MigLayout());
    }

    public void reloadSettings() {
        if (separator != null && file != null && file.exists() && mode != null && charset != null) {
            switch (mode) {
                case NODES_TABLE:
                    loadNodesSettings();
                    break;
                case EDGES_TABLE:
                    loadEdgesSettings();
                    break;
            }
            loadColumns();
        }
    }

    private void loadColumns() {
        try {
            CsvReader reader = new CsvReader(new FileInputStream(file), separator, charset);
            reader.setTrimWhitespace(false);
            reader.readHeaders();
            final String[] columns = reader.getHeaders();

            columnsCheckBoxes = new JCheckBox[columns.length];
            columnsComboBoxes = new JComboBox[columns.length];
            columnsPanel.removeAll();
            for (int i = 0; i < columns.length; i++) {
                columnsCheckBoxes[i] = new JCheckBox(columns[i], true);
                columnsPanel.add(columnsCheckBoxes[i]);
                columnsComboBoxes[i] = new JComboBox();
                fillComboBoxWithColumnTypes(columns[i], columnsComboBoxes[i]);
                columnsPanel.add(columnsComboBoxes[i], "wrap");
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

    private void loadNodesSettings() {
        table = Lookup.getDefault().lookup(AttributeController.class).getModel().getNodeTable();
        descriptionLabel.setText(getMessage("ImportCSVUIVisualPanel2.nodes.description"));

    }

    private void loadEdgesSettings() {
        table = Lookup.getDefault().lookup(AttributeController.class).getModel().getEdgeTable();
        descriptionLabel.setText(getMessage("ImportCSVUIVisualPanel2.edges.description"));
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

        descriptionLabel = new javax.swing.JLabel();
        scroll = new javax.swing.JScrollPane();
        columnsPanel = new javax.swing.JPanel();
        settingsPanel = new javax.swing.JPanel();
        columnsLabel = new javax.swing.JLabel();

        descriptionLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(descriptionLabel, null);

        columnsPanel.setLayout(new java.awt.GridLayout(1, 0));
        scroll.setViewportView(columnsPanel);

        settingsPanel.setLayout(new java.awt.GridLayout());

        org.openide.awt.Mnemonics.setLocalizedText(columnsLabel, org.openide.util.NbBundle.getMessage(ImportCSVUIVisualPanel2.class, "ImportCSVUIVisualPanel2.columnsLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(descriptionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE)
                    .addComponent(scroll, javax.swing.GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE)
                    .addComponent(settingsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE)
                    .addComponent(columnsLabel))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(descriptionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(columnsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scroll, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(settingsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 46, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel columnsLabel;
    private javax.swing.JPanel columnsPanel;
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JScrollPane scroll;
    private javax.swing.JPanel settingsPanel;
    // End of variables declaration//GEN-END:variables
}
