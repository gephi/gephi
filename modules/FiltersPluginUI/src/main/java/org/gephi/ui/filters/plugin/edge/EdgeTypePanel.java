/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gephi.ui.filters.plugin.edge;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.AbstractMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.DefaultComboBoxModel;
import javax.swing.SwingUtilities;
import org.gephi.filters.plugin.edge.EdgeTypeBuilder;
import org.gephi.filters.plugin.edge.EdgeTypeBuilder.EdgeTypeFilter;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * @author mbastian
 */
public class EdgeTypePanel extends javax.swing.JPanel {

    private static final RequestProcessor RP = new RequestProcessor("EdgeTypePanel", 1);
    private final RequestProcessor.Task loadTask = RP.create(this::loadLabels);
    private final RequestProcessor.Task selectTask = RP.create(this::applySelection);
    //Latest selected label (null is a valid label), resolved off the EDT because getEdgeType() takes the graph lock
    private final AtomicReference<Map.Entry<EdgeTypeFilter, Object>> selection = new AtomicReference<>();
    private volatile EdgeTypeFilter filter;
    private javax.swing.JComboBox comboBox;

    /**
     * Creates new form EdgeTypePanel
     */
    public EdgeTypePanel() {
        initComponents();
        comboBox.getAccessibleContext().setAccessibleName(
            NbBundle.getMessage(EdgeTypePanel.class, "EdgeTypePanel.comboBox.accessibleName"));

        comboBox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    select(e.getItem());
                }
            }
        });
    }

    public void setup(final EdgeTypeBuilder.EdgeTypeFilter filter) {
        this.filter = filter;
        loadTask.schedule(0);
    }

    private void loadLabels() {
        final EdgeTypeFilter f = filter;
        final Object[] labels;
        try {
            //Labels and edge counts are read under the graph lock, never on the EDT
            labels = f.getEdgeTypeLabels().toArray();
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
            return;
        }
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                if (f != filter) {
                    return;
                }
                DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel();
                for (Object o : labels) {
                    comboBoxModel.addElement(o);
                }
                comboBox.setModel(comboBoxModel);

                if (comboBoxModel.getSize() > 0) {
                    select(comboBoxModel.getSelectedItem());
                }
            }
        });
    }

    private void select(Object label) {
        selection.set(new AbstractMap.SimpleImmutableEntry<>(filter, label));
        selectTask.schedule(0);
    }

    private void applySelection() {
        Map.Entry<EdgeTypeFilter, Object> selected = selection.getAndSet(null);
        if (selected != null) {
            try {
                selected.getKey().setEdgeTypeLabel(selected.getValue());
            } catch (Exception e) {
                Exceptions.printStackTrace(e);
            }
        }
    }

    private void initComponents() {
        comboBox = new javax.swing.JComboBox();

        setLayout(new java.awt.BorderLayout());

        comboBox.setModel(new DefaultComboBoxModel());
        add(comboBox, java.awt.BorderLayout.CENTER);
    }
}
