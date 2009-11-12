/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
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
package org.gephi.visualization.component;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.ButtonModel;
import javax.swing.JCheckBox;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.visualization.opengl.text.TextModel;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
public class LabelAttributesPanel extends javax.swing.JPanel {

    //Settings
    private ButtonModel selectedModel;
    private boolean showProperties = false;
    //Model
    private TextModel textModel;
    private AttributesCheckBox[] nodeCheckBoxs;
    private AttributesCheckBox[] edgeCheckBoxs;

    /** Creates new form LabelAttributesPanel */
    public LabelAttributesPanel() {
        initComponents();
        selectedModel = nodesToggleButton.getModel();
        elementButtonGroup.setSelected(selectedModel, true);
        nodesToggleButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (nodesToggleButton.isSelected()) {
                    selectedModel = nodesToggleButton.getModel();
                    refresh();
                }
            }
        });
        edgesToggleButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (edgesToggleButton.isSelected()) {
                    selectedModel = edgesToggleButton.getModel();
                    refresh();
                }
            }
        });
        showPropertiesCheckbox.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                showProperties = showPropertiesCheckbox.isSelected();
                refresh();
            }
        });
    }

    public void setup(TextModel model) {
        this.textModel = model;
        refresh();
    }

    private void refresh() {
        AttributeController attributeController = Lookup.getDefault().lookup(AttributeController.class);

        List<AttributeColumn> availableColumns = new ArrayList<AttributeColumn>();
        List<AttributeColumn> selectedColumns = new ArrayList<AttributeColumn>();
        AttributesCheckBox[] target;
        if (elementButtonGroup.getSelection() == nodesToggleButton.getModel()) {
            for (AttributeColumn c : attributeController.getModel().getNodeTable().getColumns()) {
                if (showProperties || c.getOrigin().equals(AttributeOrigin.DATA)) {
                    availableColumns.add(c);
                }
            }

            if (textModel.getNodeTextColumns() != null) {
                selectedColumns = Arrays.asList(textModel.getNodeTextColumns());
            }
            nodeCheckBoxs = new AttributesCheckBox[availableColumns.size()];
            target = nodeCheckBoxs;
        } else {
            for (AttributeColumn c : attributeController.getModel().getEdgeTable().getColumns()) {
                if (showProperties || c.getOrigin().equals(AttributeOrigin.DATA)) {
                    availableColumns.add(c);
                }
            }

            if (textModel.getEdgeTextColumns() != null) {
                selectedColumns = Arrays.asList(textModel.getEdgeTextColumns());
            }
            edgeCheckBoxs = new AttributesCheckBox[availableColumns.size()];
            target = edgeCheckBoxs;
        }
        contentPanel.removeAll();
        contentPanel.setLayout(new GridLayout(availableColumns.size(), 1));
        for (int i = 0; i < availableColumns.size(); i++) {
            AttributeColumn column = availableColumns.get(i);
            AttributesCheckBox c = new AttributesCheckBox(column, selectedColumns.contains(column));
            target[i] = c;
            contentPanel.add(c.getCheckBox());
        }
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    public void unsetup() {
        List<AttributeColumn> nodeColumnsList = new ArrayList<AttributeColumn>();
        List<AttributeColumn> edgeColumnsList = new ArrayList<AttributeColumn>();
        if (nodeCheckBoxs != null) {
            for (AttributesCheckBox c : nodeCheckBoxs) {
                if (c.isSelected()) {
                    nodeColumnsList.add(c.getColumn());
                }
            }
        }
        if (edgeCheckBoxs != null) {
            for (AttributesCheckBox c : edgeCheckBoxs) {
                if (c.isSelected()) {
                    edgeColumnsList.add(c.getColumn());
                }
            }
        }
        if (edgeColumnsList.size() > 0 || nodeColumnsList.size() > 0) {
            textModel.setTextColumns(nodeColumnsList.toArray(new AttributeColumn[0]), edgeColumnsList.toArray(new AttributeColumn[0]));
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        elementButtonGroup = new javax.swing.ButtonGroup();
        controlPanel = new javax.swing.JPanel();
        nodesToggleButton = new javax.swing.JToggleButton();
        edgesToggleButton = new javax.swing.JToggleButton();
        contentScrollPane = new javax.swing.JScrollPane();
        contentPanel = new javax.swing.JPanel();
        labelComment = new javax.swing.JLabel();
        showPropertiesCheckbox = new javax.swing.JCheckBox();

        controlPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 0));

        elementButtonGroup.add(nodesToggleButton);
        nodesToggleButton.setText(org.openide.util.NbBundle.getMessage(LabelAttributesPanel.class, "LabelAttributesPanel.nodesToggleButton.text")); // NOI18N
        controlPanel.add(nodesToggleButton);

        elementButtonGroup.add(edgesToggleButton);
        edgesToggleButton.setText(org.openide.util.NbBundle.getMessage(LabelAttributesPanel.class, "LabelAttributesPanel.edgesToggleButton.text")); // NOI18N
        controlPanel.add(edgesToggleButton);

        contentPanel.setLayout(new java.awt.GridLayout());
        contentScrollPane.setViewportView(contentPanel);

        labelComment.setText(org.openide.util.NbBundle.getMessage(LabelAttributesPanel.class, "LabelAttributesPanel.labelComment.text")); // NOI18N

        showPropertiesCheckbox.setText(org.openide.util.NbBundle.getMessage(LabelAttributesPanel.class, "LabelAttributesPanel.showPropertiesCheckbox.text")); // NOI18N
        showPropertiesCheckbox.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        showPropertiesCheckbox.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        showPropertiesCheckbox.setMargin(new java.awt.Insets(2, 2, 2, 0));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(contentScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 313, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(controlPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 94, Short.MAX_VALUE)
                        .addComponent(showPropertiesCheckbox))
                    .addComponent(labelComment))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(controlPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(13, 13, 13)
                        .addComponent(labelComment))
                    .addComponent(showPropertiesCheckbox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(contentScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 197, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel contentPanel;
    private javax.swing.JScrollPane contentScrollPane;
    private javax.swing.JPanel controlPanel;
    private javax.swing.JToggleButton edgesToggleButton;
    private javax.swing.ButtonGroup elementButtonGroup;
    private javax.swing.JLabel labelComment;
    private javax.swing.JToggleButton nodesToggleButton;
    private javax.swing.JCheckBox showPropertiesCheckbox;
    // End of variables declaration//GEN-END:variables

    private static class AttributesCheckBox {

        private JCheckBox checkBox;
        private AttributeColumn column;

        public AttributesCheckBox(AttributeColumn column, boolean selected) {
            checkBox = new JCheckBox(column.getTitle(), selected);
            this.column = column;
        }

        public void setSelected(boolean selected) {
            checkBox.setSelected(selected);
        }

        public boolean isSelected() {
            return checkBox.isSelected();
        }

        public JCheckBox getCheckBox() {
            return checkBox;
        }

        public AttributeColumn getColumn() {
            return column;
        }
    }
}
