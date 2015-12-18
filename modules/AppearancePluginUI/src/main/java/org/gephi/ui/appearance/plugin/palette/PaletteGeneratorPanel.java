/*
 Copyright 2008-2013 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
 Website : http://www.gephi.org

 This file is part of Gephi.

 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

 Copyright 2013 Gephi Consortium. All rights reserved.

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

 Portions Copyrighted 2013 Gephi Consortium.
 */
package org.gephi.ui.appearance.plugin.palette;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import org.gephi.appearance.plugin.palette.Palette;
import org.gephi.appearance.plugin.palette.PaletteManager;
import org.gephi.appearance.plugin.palette.Preset;

/**
 *
 * @author mbastian
 */
public class PaletteGeneratorPanel extends javax.swing.JPanel {

    private Preset selectedPreset;
    private Palette selectedPalette;

    public PaletteGeneratorPanel() {
        initComponents();

        //Preset Model
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        for (Preset preset : PaletteManager.getInstance().getPresets()) {
            model.addElement(preset);
        }
        selectedPreset = (Preset) model.getElementAt(0);
        presetCombo.setModel(model);

        limitColorsCheckbox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    limitColorSpinner.setEnabled(true);
                } else {
                    limitColorSpinner.setEnabled(false);
                }
            }
        });

        presetCombo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (presetCombo.getSelectedItem() != selectedPreset) {
                    selectedPreset = (Preset) presetCombo.getSelectedItem();
                }
            }
        });

        //Generate
        generateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generate();
            }
        });
    }

    private void generate() {
        int colorCount = Integer.parseInt(colorCountLabel.getText());
        int paletteCount = colorCount;
        if (limitColorsCheckbox.isSelected()) {
            paletteCount = ((Number) limitColorSpinner.getValue()).intValue();
        }
        selectedPalette = PaletteManager.getInstance().generatePalette(paletteCount, selectedPreset);
        if (paletteCount < colorCount) {
            Color[] cols = Arrays.copyOf(selectedPalette.getColors(), colorCount);
            for (int i = paletteCount; i < cols.length; i++) {
                cols[i] = Color.LIGHT_GRAY;
            }
            selectedPalette = new Palette(cols);
        }

        String[] columnNames = new String[]{"Color"};
        DefaultTableModel model = new DefaultTableModel(columnNames, colorCount) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        colorTable.setModel(model);

        TableColumn colorCol = colorTable.getColumnModel().getColumn(0);
        colorCol.setCellRenderer(new ColorCellRenderer());

        int row = 0;
        for (Color c : selectedPalette.getColors()) {
            model.setValueAt(c, row++, 0);
        }
    }

    public void setup(int colorsCount) {
        colorCountLabel.setText(String.valueOf(colorsCount));
        limitColorSpinner.setModel(new javax.swing.SpinnerNumberModel(Math.min(colorsCount, 8), 1, colorsCount, 1));
    }

    public Palette getSelectedPalette() {
        return selectedPalette;
    }

    class ColorCellRenderer extends JLabel implements TableCellRenderer {

        public ColorCellRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Color c = (Color) value;
            setBackground(c);
            return this;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        labelColorCount = new javax.swing.JLabel();
        colorCountLabel = new javax.swing.JLabel();
        labelPreset = new javax.swing.JLabel();
        presetCombo = new javax.swing.JComboBox();
        generateButton = new javax.swing.JButton();
        centerScrollPanel = new javax.swing.JScrollPane();
        centerPanel = new javax.swing.JPanel();
        colorTable = new javax.swing.JTable();
        limitColorsCheckbox = new javax.swing.JCheckBox();
        limitColorSpinner = new javax.swing.JSpinner();

        labelColorCount.setText(org.openide.util.NbBundle.getMessage(PaletteGeneratorPanel.class, "PaletteGeneratorPanel.labelColorCount.text")); // NOI18N

        labelPreset.setText(org.openide.util.NbBundle.getMessage(PaletteGeneratorPanel.class, "PaletteGeneratorPanel.labelPreset.text")); // NOI18N

        generateButton.setText(org.openide.util.NbBundle.getMessage(PaletteGeneratorPanel.class, "PaletteGeneratorPanel.generateButton.text")); // NOI18N

        centerScrollPanel.setBorder(null);
        centerScrollPanel.setOpaque(false);

        centerPanel.setLayout(new java.awt.GridBagLayout());

        colorTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        colorTable.setOpaque(false);
        colorTable.setRowHeight(22);
        colorTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        colorTable.setShowHorizontalLines(false);
        colorTable.setShowVerticalLines(false);
        colorTable.setTableHeader(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        centerPanel.add(colorTable, gridBagConstraints);

        centerScrollPanel.setViewportView(centerPanel);

        limitColorsCheckbox.setSelected(true);
        limitColorsCheckbox.setText(org.openide.util.NbBundle.getMessage(PaletteGeneratorPanel.class, "PaletteGeneratorPanel.limitColorsCheckbox.text")); // NOI18N
        limitColorsCheckbox.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);

        limitColorSpinner.setModel(new javax.swing.SpinnerNumberModel(8, 1, null, 1));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(centerScrollPanel)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(presetCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 73, Short.MAX_VALUE)
                        .addComponent(generateButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(labelPreset)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(limitColorSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(labelColorCount)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(colorCountLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(limitColorsCheckbox)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(labelColorCount)
                        .addComponent(colorCountLabel))
                    .addComponent(limitColorsCheckbox))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(28, 28, 28)
                        .addComponent(labelPreset))
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(limitColorSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(presetCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(generateButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(centerScrollPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 191, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel centerPanel;
    private javax.swing.JScrollPane centerScrollPanel;
    private javax.swing.JLabel colorCountLabel;
    private javax.swing.JTable colorTable;
    private javax.swing.JButton generateButton;
    private javax.swing.JLabel labelColorCount;
    private javax.swing.JLabel labelPreset;
    private javax.swing.JSpinner limitColorSpinner;
    private javax.swing.JCheckBox limitColorsCheckbox;
    private javax.swing.JComboBox presetCombo;
    // End of variables declaration//GEN-END:variables
}
