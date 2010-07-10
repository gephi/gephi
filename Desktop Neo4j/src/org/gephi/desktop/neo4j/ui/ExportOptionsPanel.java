package org.gephi.desktop.neo4j.ui;

import java.awt.Component;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import org.gephi.desktop.neo4j.ui.util.GephiUtils;
import org.netbeans.validation.api.builtin.Validators;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.ValidationPanel;
import org.netbeans.validation.api.ui.ValidationStrategy;

/**
 *
 * @author Martin Škurla
 */
public class ExportOptionsPanel extends javax.swing.JPanel {
    private static final int PANEL_CHECKBOX_COLUMN_COUNT = 1;

    public ExportOptionsPanel() {
        initComponents();

        fillPanelWithCheckBoxes(exportEdgeColumnsContentPanel, GephiUtils.edgeColumnNames());
        fillPanelWithCheckBoxes(exportNodeColumnsContentPanel, GephiUtils.nodeColumnNames());
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        exportPanel = new javax.swing.JPanel();
        exportNodeColumnsPanel = new javax.swing.JPanel();
        exportNodeColumnsScrollPane = new javax.swing.JScrollPane();
        exportNodeColumnsContentPanel = new javax.swing.JPanel();
        relationshipTypeNamePanel = new javax.swing.JPanel();
        defaultValueLabel = new javax.swing.JLabel();
        defaultValueTextField = new javax.swing.JTextField();
        fromColumnLabel = new javax.swing.JLabel();
        fromColumnComboBox = new javax.swing.JComboBox();
        exportEdgeColumnsPanel = new javax.swing.JPanel();
        exportEdgeColumnsScrollPane = new javax.swing.JScrollPane();
        exportEdgeColumnsContentPanel = new javax.swing.JPanel();

        exportPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ExportOptionsPanel.class, "ExportOptionsPanel.exportPanel.border.title"))); // NOI18N

        exportNodeColumnsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ExportOptionsPanel.class, "ExportOptionsPanel.exportNodeColumnsPanel.border.title"))); // NOI18N

        javax.swing.GroupLayout exportNodeColumnsContentPanelLayout = new javax.swing.GroupLayout(exportNodeColumnsContentPanel);
        exportNodeColumnsContentPanel.setLayout(exportNodeColumnsContentPanelLayout);
        exportNodeColumnsContentPanelLayout.setHorizontalGroup(
            exportNodeColumnsContentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 150, Short.MAX_VALUE)
        );
        exportNodeColumnsContentPanelLayout.setVerticalGroup(
            exportNodeColumnsContentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 157, Short.MAX_VALUE)
        );

        exportNodeColumnsScrollPane.setViewportView(exportNodeColumnsContentPanel);

        javax.swing.GroupLayout exportNodeColumnsPanelLayout = new javax.swing.GroupLayout(exportNodeColumnsPanel);
        exportNodeColumnsPanel.setLayout(exportNodeColumnsPanelLayout);
        exportNodeColumnsPanelLayout.setHorizontalGroup(
            exportNodeColumnsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(exportNodeColumnsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE)
        );
        exportNodeColumnsPanelLayout.setVerticalGroup(
            exportNodeColumnsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(exportNodeColumnsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 159, Short.MAX_VALUE)
        );

        relationshipTypeNamePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ExportOptionsPanel.class, "ExportOptionsPanel.relationshipTypeNamePanel.border.title"))); // NOI18N

        defaultValueLabel.setText(org.openide.util.NbBundle.getMessage(ExportOptionsPanel.class, "ExportOptionsPanel.defaultValueLabel.text")); // NOI18N

        defaultValueTextField.setText(org.openide.util.NbBundle.getMessage(ExportOptionsPanel.class, "ExportOptionsPanel.default value.text")); // NOI18N
        defaultValueTextField.setName("default value"); // NOI18N

        fromColumnLabel.setText(org.openide.util.NbBundle.getMessage(ExportOptionsPanel.class, "ExportOptionsPanel.fromColumnLabel.text")); // NOI18N

        fromColumnComboBox.setModel(new DefaultComboBoxModel(GephiUtils.edgeColumnNames()));

        javax.swing.GroupLayout relationshipTypeNamePanelLayout = new javax.swing.GroupLayout(relationshipTypeNamePanel);
        relationshipTypeNamePanel.setLayout(relationshipTypeNamePanelLayout);
        relationshipTypeNamePanelLayout.setHorizontalGroup(
            relationshipTypeNamePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(relationshipTypeNamePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(relationshipTypeNamePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(fromColumnLabel)
                    .addComponent(defaultValueLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(relationshipTypeNamePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(defaultValueTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 230, Short.MAX_VALUE)
                    .addComponent(fromColumnComboBox, 0, 230, Short.MAX_VALUE))
                .addContainerGap())
        );
        relationshipTypeNamePanelLayout.setVerticalGroup(
            relationshipTypeNamePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(relationshipTypeNamePanelLayout.createSequentialGroup()
                .addGroup(relationshipTypeNamePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fromColumnComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fromColumnLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(relationshipTypeNamePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(defaultValueTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(defaultValueLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        exportEdgeColumnsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ExportOptionsPanel.class, "ExportOptionsPanel.exportEdgeColumnsPanel.border.title"))); // NOI18N

        javax.swing.GroupLayout exportEdgeColumnsContentPanelLayout = new javax.swing.GroupLayout(exportEdgeColumnsContentPanel);
        exportEdgeColumnsContentPanel.setLayout(exportEdgeColumnsContentPanelLayout);
        exportEdgeColumnsContentPanelLayout.setHorizontalGroup(
            exportEdgeColumnsContentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 150, Short.MAX_VALUE)
        );
        exportEdgeColumnsContentPanelLayout.setVerticalGroup(
            exportEdgeColumnsContentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 157, Short.MAX_VALUE)
        );

        exportEdgeColumnsScrollPane.setViewportView(exportEdgeColumnsContentPanel);

        javax.swing.GroupLayout exportEdgeColumnsPanelLayout = new javax.swing.GroupLayout(exportEdgeColumnsPanel);
        exportEdgeColumnsPanel.setLayout(exportEdgeColumnsPanelLayout);
        exportEdgeColumnsPanelLayout.setHorizontalGroup(
            exportEdgeColumnsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(exportEdgeColumnsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE)
        );
        exportEdgeColumnsPanelLayout.setVerticalGroup(
            exportEdgeColumnsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(exportEdgeColumnsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 159, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout exportPanelLayout = new javax.swing.GroupLayout(exportPanel);
        exportPanel.setLayout(exportPanelLayout);
        exportPanelLayout.setHorizontalGroup(
            exportPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(relationshipTypeNamePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(exportPanelLayout.createSequentialGroup()
                .addComponent(exportNodeColumnsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(exportEdgeColumnsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        exportPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {exportEdgeColumnsPanel, exportNodeColumnsPanel});

        exportPanelLayout.setVerticalGroup(
            exportPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(exportPanelLayout.createSequentialGroup()
                .addComponent(relationshipTypeNamePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(exportPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(exportNodeColumnsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(exportEdgeColumnsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        exportPanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {exportEdgeColumnsPanel, exportNodeColumnsPanel});

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(exportPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(exportPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void fillPanelWithCheckBoxes(JPanel panel, String[] checkBoxTexts) {
        panel.setLayout(new MigLayout("wrap " + PANEL_CHECKBOX_COLUMN_COUNT));

        for (String checkBoxText : checkBoxTexts)
            panel.add(new JCheckBox(checkBoxText, true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel defaultValueLabel;
    private javax.swing.JTextField defaultValueTextField;
    private javax.swing.JPanel exportEdgeColumnsContentPanel;
    private javax.swing.JPanel exportEdgeColumnsPanel;
    private javax.swing.JScrollPane exportEdgeColumnsScrollPane;
    private javax.swing.JPanel exportNodeColumnsContentPanel;
    private javax.swing.JPanel exportNodeColumnsPanel;
    private javax.swing.JScrollPane exportNodeColumnsScrollPane;
    private javax.swing.JPanel exportPanel;
    private javax.swing.JComboBox fromColumnComboBox;
    private javax.swing.JLabel fromColumnLabel;
    private javax.swing.JPanel relationshipTypeNamePanel;
    // End of variables declaration//GEN-END:variables

    public String getDefaultValue() {
        return defaultValueTextField.getText().trim();
    }

    public String getFromColumn() {
        return (String) fromColumnComboBox.getSelectedItem();
    }

    public Collection<String> getExportNodeColumnNames() {
        return getSelectedComboBoxNames(exportNodeColumnsContentPanel);
    }

    public Collection<String> getExportEdgeColumnNames() {
        return getSelectedComboBoxNames(exportEdgeColumnsContentPanel);
    }

    private Collection<String> getSelectedComboBoxNames(JPanel sourcePanel) {
        List<String> columnNames = new LinkedList<String>();

        for (Component component : sourcePanel.getComponents()) {
            if (component instanceof JCheckBox) {
                JCheckBox checkBox = (JCheckBox) component;

                if (checkBox.isSelected())
                    columnNames.add(checkBox.getText());
            }
        }

        return columnNames;
    }

    @SuppressWarnings("unchecked")
    public ValidationPanel createValidationPanel() {
        ValidationPanel validationPanel = new ValidationPanel();
        validationPanel.setInnerComponent(this);

        ValidationGroup group = validationPanel.getValidationGroup();
        group.add(defaultValueTextField, Validators.REQUIRE_NON_EMPTY_STRING);

        return validationPanel;
    }
}
