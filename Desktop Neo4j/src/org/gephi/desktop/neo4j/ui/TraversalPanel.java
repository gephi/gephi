package org.gephi.desktop.neo4j.ui;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.neo4j.api.TraversalOrder;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.NotFoundException;
import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.Validator;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.ValidationPanel;

/**
 *
 * @author Martin Šlurla
 */
public class TraversalPanel extends javax.swing.JPanel {
    private final GraphDatabaseService graphDB;

    public TraversalPanel(GraphDatabaseService graphDB) {
        initComponents();
        this.graphDB = graphDB;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        orderButtonGroup = new javax.swing.ButtonGroup();
        maxDepthButtonGroup = new javax.swing.ButtonGroup();
        startNodeButtonGroup = new javax.swing.ButtonGroup();
        traversePanel = new javax.swing.JPanel();
        orderPanel = new javax.swing.JPanel();
        breadthFirstOrderRadioButton = new javax.swing.JRadioButton();
        depthFirstOrderRadioButton = new javax.swing.JRadioButton();
        maxDepthPanel = new javax.swing.JPanel();
        concreteMaxDepthRadioButton = new javax.swing.JRadioButton();
        endOfGraphMaxDepthRadioButton = new javax.swing.JRadioButton();
        maxDepthSpinner = new javax.swing.JSpinner();
        relationshipsPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jComboBox1 = new javax.swing.JComboBox();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jRadioButton3 = new javax.swing.JRadioButton();
        filterPanel = new javax.swing.JPanel();
        startNodePanel = new javax.swing.JPanel();
        idStartNodeRadioButton = new javax.swing.JRadioButton();
        indexStartNodeRadioButton = new javax.swing.JRadioButton();
        indexValueStartNodeTextField = new javax.swing.JTextField();
        indexKeyStartNodeTextField = new javax.swing.JTextField();
        indexValueStartNodeLabel = new javax.swing.JLabel();
        idStartNodeTextField = new javax.swing.JTextField();

        traversePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(TraversalPanel.class, "TraversalPanel.traversePanel.border.title"))); // NOI18N

        orderPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(TraversalPanel.class, "TraversalPanel.orderPanel.border.title"))); // NOI18N

        orderButtonGroup.add(breadthFirstOrderRadioButton);
        breadthFirstOrderRadioButton.setText(org.openide.util.NbBundle.getMessage(TraversalPanel.class, "TraversalPanel.breadthFirstOrderRadioButton.text")); // NOI18N

        orderButtonGroup.add(depthFirstOrderRadioButton);
        depthFirstOrderRadioButton.setSelected(true);
        depthFirstOrderRadioButton.setText(org.openide.util.NbBundle.getMessage(TraversalPanel.class, "TraversalPanel.depthFirstOrderRadioButton.text")); // NOI18N

        javax.swing.GroupLayout orderPanelLayout = new javax.swing.GroupLayout(orderPanel);
        orderPanel.setLayout(orderPanelLayout);
        orderPanelLayout.setHorizontalGroup(
            orderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(orderPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(orderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(depthFirstOrderRadioButton)
                    .addComponent(breadthFirstOrderRadioButton))
                .addContainerGap(30, Short.MAX_VALUE))
        );
        orderPanelLayout.setVerticalGroup(
            orderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(orderPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(depthFirstOrderRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(breadthFirstOrderRadioButton)
                .addContainerGap(34, Short.MAX_VALUE))
        );

        maxDepthPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(TraversalPanel.class, "TraversalPanel.maxDepthPanel.border.title"))); // NOI18N

        maxDepthButtonGroup.add(concreteMaxDepthRadioButton);
        concreteMaxDepthRadioButton.setText(org.openide.util.NbBundle.getMessage(TraversalPanel.class, "TraversalPanel.concreteMaxDepthRadioButton.text")); // NOI18N

        maxDepthButtonGroup.add(endOfGraphMaxDepthRadioButton);
        endOfGraphMaxDepthRadioButton.setSelected(true);
        endOfGraphMaxDepthRadioButton.setText(org.openide.util.NbBundle.getMessage(TraversalPanel.class, "TraversalPanel.endOfGraphMaxDepthRadioButton.text")); // NOI18N

        javax.swing.GroupLayout maxDepthPanelLayout = new javax.swing.GroupLayout(maxDepthPanel);
        maxDepthPanel.setLayout(maxDepthPanelLayout);
        maxDepthPanelLayout.setHorizontalGroup(
            maxDepthPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(maxDepthPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(maxDepthPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(maxDepthPanelLayout.createSequentialGroup()
                        .addComponent(concreteMaxDepthRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(maxDepthSpinner))
                    .addComponent(endOfGraphMaxDepthRadioButton))
                .addContainerGap(28, Short.MAX_VALUE))
        );
        maxDepthPanelLayout.setVerticalGroup(
            maxDepthPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(maxDepthPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(maxDepthPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(concreteMaxDepthRadioButton)
                    .addComponent(maxDepthSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(endOfGraphMaxDepthRadioButton)
                .addContainerGap(31, Short.MAX_VALUE))
        );

        relationshipsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(TraversalPanel.class, "TraversalPanel.relationshipsPanel.border.title"))); // NOI18N

        jLabel1.setText(org.openide.util.NbBundle.getMessage(TraversalPanel.class, "TraversalPanel.jLabel1.text")); // NOI18N

        jLabel2.setText(org.openide.util.NbBundle.getMessage(TraversalPanel.class, "TraversalPanel.jLabel2.text")); // NOI18N

        jButton1.setText(org.openide.util.NbBundle.getMessage(TraversalPanel.class, "TraversalPanel.jButton1.text")); // NOI18N

        jButton2.setText(org.openide.util.NbBundle.getMessage(TraversalPanel.class, "TraversalPanel.jButton2.text")); // NOI18N

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jRadioButton1.setText(org.openide.util.NbBundle.getMessage(TraversalPanel.class, "TraversalPanel.jRadioButton1.text")); // NOI18N

        jRadioButton2.setText(org.openide.util.NbBundle.getMessage(TraversalPanel.class, "TraversalPanel.jRadioButton2.text")); // NOI18N

        jRadioButton3.setText(org.openide.util.NbBundle.getMessage(TraversalPanel.class, "TraversalPanel.jRadioButton3.text")); // NOI18N

        javax.swing.GroupLayout relationshipsPanelLayout = new javax.swing.GroupLayout(relationshipsPanel);
        relationshipsPanel.setLayout(relationshipsPanelLayout);
        relationshipsPanelLayout.setHorizontalGroup(
            relationshipsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(relationshipsPanelLayout.createSequentialGroup()
                .addGroup(relationshipsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(relationshipsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(relationshipsPanelLayout.createSequentialGroup()
                            .addGap(128, 128, 128)
                            .addComponent(jButton1)
                            .addGap(35, 35, 35)
                            .addComponent(jButton2))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, relationshipsPanelLayout.createSequentialGroup()
                            .addGroup(relationshipsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(relationshipsPanelLayout.createSequentialGroup()
                                    .addGap(25, 25, 25)
                                    .addComponent(jLabel1))
                                .addGroup(relationshipsPanelLayout.createSequentialGroup()
                                    .addGap(34, 34, 34)
                                    .addComponent(jLabel2)))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 197, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(relationshipsPanelLayout.createSequentialGroup()
                        .addGap(58, 58, 58)
                        .addGroup(relationshipsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(relationshipsPanelLayout.createSequentialGroup()
                                .addGap(45, 45, 45)
                                .addComponent(jRadioButton3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(35, 35, 35))
                            .addComponent(jRadioButton1))
                        .addGap(15, 15, 15)
                        .addComponent(jRadioButton2)))
                .addContainerGap(104, Short.MAX_VALUE))
        );
        relationshipsPanelLayout.setVerticalGroup(
            relationshipsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(relationshipsPanelLayout.createSequentialGroup()
                .addGap(42, 42, 42)
                .addGroup(relationshipsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(relationshipsPanelLayout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addComponent(jLabel1)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel2))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(relationshipsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2))
                .addGap(40, 40, 40)
                .addGroup(relationshipsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(relationshipsPanelLayout.createSequentialGroup()
                        .addGroup(relationshipsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(relationshipsPanelLayout.createSequentialGroup()
                                .addGap(11, 11, 11)
                                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jRadioButton3))
                        .addGap(18, 18, 18)
                        .addComponent(jRadioButton1))
                    .addComponent(jRadioButton2))
                .addGap(34, 34, 34))
        );

        filterPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(TraversalPanel.class, "TraversalPanel.filterPanel.border.title"))); // NOI18N

        javax.swing.GroupLayout filterPanelLayout = new javax.swing.GroupLayout(filterPanel);
        filterPanel.setLayout(filterPanelLayout);
        filterPanelLayout.setHorizontalGroup(
            filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 501, Short.MAX_VALUE)
        );
        filterPanelLayout.setVerticalGroup(
            filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 77, Short.MAX_VALUE)
        );

        startNodePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(TraversalPanel.class, "TraversalPanel.startNodePanel.border.title"))); // NOI18N

        startNodeButtonGroup.add(idStartNodeRadioButton);
        idStartNodeRadioButton.setSelected(true);
        idStartNodeRadioButton.setText(org.openide.util.NbBundle.getMessage(TraversalPanel.class, "TraversalPanel.idStartNodeRadioButton.text")); // NOI18N

        startNodeButtonGroup.add(indexStartNodeRadioButton);
        indexStartNodeRadioButton.setText(org.openide.util.NbBundle.getMessage(TraversalPanel.class, "TraversalPanel.indexStartNodeRadioButton.text")); // NOI18N

        indexValueStartNodeTextField.setText(org.openide.util.NbBundle.getMessage(TraversalPanel.class, "TraversalPanel.index value.text")); // NOI18N
        indexValueStartNodeTextField.setName("index value"); // NOI18N

        indexKeyStartNodeTextField.setText(org.openide.util.NbBundle.getMessage(TraversalPanel.class, "TraversalPanel.index key.text")); // NOI18N
        indexKeyStartNodeTextField.setName("index key"); // NOI18N

        indexValueStartNodeLabel.setText(org.openide.util.NbBundle.getMessage(TraversalPanel.class, "TraversalPanel.indexValueStartNodeLabel.text")); // NOI18N

        idStartNodeTextField.setText(org.openide.util.NbBundle.getMessage(TraversalPanel.class, "TraversalPanel.node id.text")); // NOI18N
        idStartNodeTextField.setName("node id"); // NOI18N

        javax.swing.GroupLayout startNodePanelLayout = new javax.swing.GroupLayout(startNodePanel);
        startNodePanel.setLayout(startNodePanelLayout);
        startNodePanelLayout.setHorizontalGroup(
            startNodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(startNodePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(startNodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(idStartNodeRadioButton)
                    .addGroup(startNodePanelLayout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(indexValueStartNodeLabel))
                    .addComponent(indexStartNodeRadioButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(startNodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(indexValueStartNodeTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE)
                    .addComponent(indexKeyStartNodeTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE)
                    .addComponent(idStartNodeTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE))
                .addContainerGap())
        );
        startNodePanelLayout.setVerticalGroup(
            startNodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(startNodePanelLayout.createSequentialGroup()
                .addGroup(startNodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(startNodePanelLayout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(idStartNodeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(idStartNodeRadioButton))
                .addGap(3, 3, 3)
                .addGroup(startNodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(startNodePanelLayout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(indexKeyStartNodeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(indexStartNodeRadioButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(startNodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE, false)
                    .addComponent(indexValueStartNodeLabel)
                    .addComponent(indexValueStartNodeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12))
        );

        javax.swing.GroupLayout traversePanelLayout = new javax.swing.GroupLayout(traversePanel);
        traversePanel.setLayout(traversePanelLayout);
        traversePanelLayout.setHorizontalGroup(
            traversePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(filterPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(relationshipsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(traversePanelLayout.createSequentialGroup()
                .addComponent(startNodePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(orderPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(maxDepthPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        traversePanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {maxDepthPanel, orderPanel});

        traversePanelLayout.setVerticalGroup(
            traversePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(traversePanelLayout.createSequentialGroup()
                .addGroup(traversePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(maxDepthPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(orderPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(startNodePanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 114, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(relationshipsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(filterPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(traversePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(traversePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton breadthFirstOrderRadioButton;
    private javax.swing.JRadioButton concreteMaxDepthRadioButton;
    private javax.swing.JRadioButton depthFirstOrderRadioButton;
    private javax.swing.JRadioButton endOfGraphMaxDepthRadioButton;
    private javax.swing.JPanel filterPanel;
    private javax.swing.JRadioButton idStartNodeRadioButton;
    private javax.swing.JTextField idStartNodeTextField;
    private javax.swing.JTextField indexKeyStartNodeTextField;
    private javax.swing.JRadioButton indexStartNodeRadioButton;
    private javax.swing.JLabel indexValueStartNodeLabel;
    private javax.swing.JTextField indexValueStartNodeTextField;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JRadioButton jRadioButton3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.ButtonGroup maxDepthButtonGroup;
    private javax.swing.JPanel maxDepthPanel;
    private javax.swing.JSpinner maxDepthSpinner;
    private javax.swing.ButtonGroup orderButtonGroup;
    private javax.swing.JPanel orderPanel;
    private javax.swing.JPanel relationshipsPanel;
    private javax.swing.ButtonGroup startNodeButtonGroup;
    private javax.swing.JPanel startNodePanel;
    private javax.swing.JPanel traversePanel;
    // End of variables declaration//GEN-END:variables

    public int getMaxDepth() {
        return endOfGraphMaxDepthRadioButton.isSelected() ? Integer.MAX_VALUE
                                                          : (Integer) maxDepthSpinner.getValue();
    }

    public TraversalOrder getOrder() {
        return depthFirstOrderRadioButton.isSelected() ? TraversalOrder.DEPTH_FIRST
                                                       : TraversalOrder.BREADTH_FIRST;
    }

    public long getStartNodeId() {
        return Integer.parseInt(idStartNodeTextField.getText());
    }

    public ValidationPanel createValidationPanel() {
        ValidationPanel validationPanel = new ValidationPanel();
        validationPanel.setInnerComponent(this);
        ValidationGroup group = validationPanel.getValidationGroup();

        //Validators
        group.add(this.idStartNodeTextField, new NodeIdValidator());

        return validationPanel;
    }

    private class NodeIdValidator implements Validator<String>{
        @Override
        public boolean validate(Problems problems, String string, String value) {
            if (idStartNodeRadioButton.isSelected()) {
                int nodeId;

                try {
                    nodeId = Integer.parseInt(value);
                }
                catch (NumberFormatException nfe) {
                    problems.add("not number...");
                    return false;
                }

                try {
                    graphDB.getNodeById(nodeId);
                }
                catch (NotFoundException nfe) {
                    problems.add("Node with id '" + nodeId + "' doesn't exist");
                    return false;
                }
            }

            return true;
        }
    }
}
