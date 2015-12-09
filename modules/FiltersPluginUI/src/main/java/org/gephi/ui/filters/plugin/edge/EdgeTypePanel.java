/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.ui.filters.plugin.edge;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.DefaultComboBoxModel;
import org.gephi.filters.plugin.edge.EdgeTypeBuilder;
import org.gephi.filters.plugin.edge.EdgeTypeBuilder.EdgeTypeFilter;

/**
 *
 * @author mbastian
 */
public class EdgeTypePanel extends javax.swing.JPanel {

    private EdgeTypeFilter filter;

    /**
     * Creates new form EdgeTypePanel
     */
    public EdgeTypePanel() {
        initComponents();

        comboBox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    filter.setEdgeTypeLabel(e.getItem());
                }
            }
        });
    }

    public void setup(final EdgeTypeBuilder.EdgeTypeFilter filter) {
        this.filter = filter;

        DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel();
        for (Object o : filter.getEdgeTypeLabels()) {
            comboBoxModel.addElement(o);
        }
        comboBox.setModel(comboBoxModel);
        
        if(comboBoxModel.getSize() > 0) {
            filter.setEdgeTypeLabel(comboBoxModel.getSelectedItem());
        }
    }

    private void initComponents() {
        comboBox = new javax.swing.JComboBox();

        setLayout(new java.awt.BorderLayout());

        comboBox.setModel(new DefaultComboBoxModel());
        add(comboBox, java.awt.BorderLayout.CENTER);
    }
    private javax.swing.JComboBox comboBox;
}
