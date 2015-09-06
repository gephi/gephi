/*
 Copyright 2008-2010 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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
package org.gephi.visualization.component;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.ButtonModel;
import javax.swing.JCheckBox;
import net.miginfocom.swing.MigLayout;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.Origin;
import org.gephi.visualization.text.TextModelImpl;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
public class LabelAttributesPanel extends javax.swing.JPanel {

    //Settings
    private ButtonModel selectedModel;
    private boolean showProperties = true;
    //Model
    private TextModelImpl textModel;
    private AttributesCheckBox[] nodeCheckBoxs;
    private AttributesCheckBox[] edgeCheckBoxs;

    /**
     * Creates new form LabelAttributesPanel
     */
    public LabelAttributesPanel() {
        initComponents();
        selectedModel = nodesToggleButton.getModel();
        elementButtonGroup.setSelected(selectedModel, true);
        nodesToggleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (nodesToggleButton.isSelected()) {
                    selectedModel = nodesToggleButton.getModel();
                    refresh();
                }
            }
        });
        edgesToggleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (edgesToggleButton.isSelected()) {
                    selectedModel = edgesToggleButton.getModel();
                    refresh();
                }
            }
        });
        showPropertiesCheckbox.setSelected(showProperties);
        showPropertiesCheckbox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                showProperties = showPropertiesCheckbox.isSelected();
                refresh();
            }
        });
    }

    public void setup(TextModelImpl model) {
        this.textModel = model;
        refresh();
    }

    private void refresh() {
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);

        List<Column> availableColumns = new ArrayList<Column>();
        List<Column> selectedColumns = new ArrayList<Column>();
        AttributesCheckBox[] target;
        if (elementButtonGroup.getSelection() == nodesToggleButton.getModel()) {
            for (Column c : graphController.getGraphModel().getNodeTable()) {
                if (c.getOrigin().equals(Origin.DATA)) {
                    availableColumns.add(c);
                } else if (showProperties) {
                    if (c.getId().equalsIgnoreCase("label")) {
                        availableColumns.add(c);
                    }
                }
            }

            if (textModel.getNodeTextColumns() != null) {
                selectedColumns = Arrays.asList(textModel.getNodeTextColumns());
            }
            nodeCheckBoxs = new AttributesCheckBox[availableColumns.size()];
            target = nodeCheckBoxs;
        } else {
            for (Column c : graphController.getGraphModel().getEdgeTable()) {
                if (c.getOrigin().equals(Origin.DATA)) {
                    availableColumns.add(c);
                } else if (showProperties) {
                    if (c.getId().equalsIgnoreCase("label")) {
                        availableColumns.add(c);
                    }
                }
            }

            if (textModel.getEdgeTextColumns() != null) {
                selectedColumns = Arrays.asList(textModel.getEdgeTextColumns());
            }
            edgeCheckBoxs = new AttributesCheckBox[availableColumns.size()];
            target = edgeCheckBoxs;
        }
        contentPanel.removeAll();
        contentPanel.setLayout(new MigLayout("", "[pref!]"));
        for (int i = 0; i < availableColumns.size(); i++) {
            Column column = availableColumns.get(i);
            AttributesCheckBox c = new AttributesCheckBox(column, selectedColumns.contains(column));
            target[i] = c;
            contentPanel.add(c.getCheckBox(), "wrap");
        }
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    public void unsetup() {
        List<Column> nodeColumnsList = new ArrayList<Column>();
        List<Column> edgeColumnsList = new ArrayList<Column>();
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
            textModel.setTextColumns(nodeColumnsList.toArray(new Column[0]), edgeColumnsList.toArray(new Column[0]));
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
        private Column column;

        public AttributesCheckBox(Column column, boolean selected) {
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

        public Column getColumn() {
            return column;
        }
    }
}
