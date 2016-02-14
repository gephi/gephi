/*
Copyright 2008-2015 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
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
package org.gephi.datalab.plugin.manipulators.general.ui;

import java.util.List;
import java.util.MissingResourceException;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import org.gephi.datalab.plugin.manipulators.general.ManageColumnEstimators;
import org.gephi.datalab.spi.DialogControls;
import org.gephi.datalab.spi.Manipulator;
import org.gephi.datalab.spi.ManipulatorUI;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Estimator;
import org.gephi.graph.api.types.TimeMap;
import org.openide.util.NbBundle;

/**
 * UI for ManageColumnEstimators.
 *
 * @author Eduardo Ramos
 */
public class ManageColumnEstimatorsUI extends javax.swing.JPanel implements ManipulatorUI {

    private ManageColumnEstimators manipulator;
    private ColumnEstimator[] columnsEstimators;

    /**
     * Creates new form GeneralChooseColumnsUI
     */
    public ManageColumnEstimatorsUI() {
        initComponents();
    }

    @Override
    public void setup(Manipulator m, DialogControls dialogControls) {
        this.manipulator = (ManageColumnEstimators) m;
        descriptionLabel.setText(manipulator.getDescription());
        refreshColumns();
    }

    @Override
    public void unSetup() {
        Column[] columns = new Column[columnsEstimators.length];
        Estimator[] estimators = new Estimator[columnsEstimators.length];
        for (int i = 0; i < columnsEstimators.length; i++) {
            columns[i] = columnsEstimators[i].column;
            estimators[i] = columnsEstimators[i].getEstimator();
        }
        
        manipulator.setup(columns, estimators);
    }

    @Override
    public String getDisplayName() {
        return manipulator.getName();
    }

    @Override
    public JPanel getSettingsPanel() {
        return this;
    }

    @Override
    public boolean isModal() {
        return true;
    }

    private void refreshColumns() {
        List<Column> columns = manipulator.getColumns();
        columnsEstimators = new ColumnEstimator[columns.size()];
        contentPanel.removeAll();
        contentPanel.setLayout(new MigLayout("", "[pref!]"));
        for (int i = 0; i < columnsEstimators.length; i++) {
            columnsEstimators[i] = new ColumnEstimator(columns.get(i));
            contentPanel.add(columnsEstimators[i].label, "wrap");
            contentPanel.add(columnsEstimators[i].comboBox, "wrap");
        }
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private static class EstimatorWrapper {

        private final Estimator estimator;

        public EstimatorWrapper(Estimator estimator) {
            this.estimator = estimator;
        }

        @Override
        public String toString() {
            if (estimator == null) {
                return "---";
            }

            try {
                return NbBundle.getMessage(ManageColumnEstimatorsUI.class, "ManageColumnEstimatorsUI.estimator." + estimator.name());
            } catch (MissingResourceException missingResourceException) {
                return estimator.name();//In case of new estimators without translation
            }
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 37 * hash + (this.estimator != null ? this.estimator.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final EstimatorWrapper other = (EstimatorWrapper) obj;
            if (this.estimator != other.estimator) {
                return false;
            }
            return true;
        }
    }

    private static class ColumnEstimator {

        private final JLabel label;
        private final JComboBox comboBox;
        private final Column column;

        public ColumnEstimator(Column column) {
            this.column = column;
            this.comboBox = new JComboBox();
            this.label = new JLabel(column.getTitle());

            initAvailableEstimators();
            Estimator currentEstimator = column.getEstimator();
            comboBox.setSelectedItem(new EstimatorWrapper(currentEstimator));
        }

        private void initAvailableEstimators() {
            Class<? extends TimeMap> type = column.getTypeClass();
            try {
                TimeMap dummy = type.newInstance();
                for (Estimator estimator : Estimator.values()) {
                    if (dummy.isSupported(estimator)) {
                        comboBox.addItem(new EstimatorWrapper(estimator));
                    }
                }
            } catch (InstantiationException ex) {
                throw new RuntimeException(ex);
            } catch (IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }
        }

        public Estimator getEstimator() {
            return ((EstimatorWrapper) comboBox.getSelectedItem()).estimator;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        contentScrollPane = new javax.swing.JScrollPane();
        contentPanel = new javax.swing.JPanel();
        descriptionLabel = new javax.swing.JLabel();

        contentPanel.setLayout(new java.awt.GridLayout(1, 0));
        contentScrollPane.setViewportView(contentPanel);

        descriptionLabel.setText(null);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(descriptionLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 319, Short.MAX_VALUE)
                    .addComponent(contentScrollPane, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 319, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(descriptionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(contentScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel contentPanel;
    private javax.swing.JScrollPane contentScrollPane;
    private javax.swing.JLabel descriptionLabel;
    // End of variables declaration//GEN-END:variables
}
