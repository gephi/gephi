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
package org.gephi.ui.filters.plugin.attribute;

import java.text.DecimalFormat;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.filters.plugin.attribute.AttributeEqualBuilder.EqualNumberFilter;
import org.gephi.filters.spi.FilterProperty;
import org.openide.util.WeakListeners;

/**
 *
 * @author Mathieu Bastian
 */
public class EqualNumberPanel extends javax.swing.JPanel implements ChangeListener {

    private EqualNumberFilter filter;

    public EqualNumberPanel() {
        initComponents();
    }

    @Override
    public void stateChanged(ChangeEvent evt) {
        FilterProperty match = filter.getProperties()[1];
        try {
            match.setValue((Number) valueSpinner.getValue());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setup(EqualNumberFilter f) {
        this.filter = f;
        new Thread(new Runnable() {

            @Override
            public void run() {
                setToolTipText(filter.getName() + " '" + filter.getColumn().getTitle() + "'");
                Number match = filter.getMatch();
                Number stepSize = null;
                final Comparable min = (Comparable) filter.getRange().getMinimum();
                final Comparable max = (Comparable) filter.getRange().getMaximum();
                Class type = filter.getColumn().getTypeClass();
                if (type.equals(Double.class)) {
                    match = (match != null ? match : (Double) min);
                    stepSize = .1;
                } else if (type.equals(Float.class)) {
                    match = (match != null ? match : (Float) min);
                    stepSize = .1f;
                } else if (type.equals(Long.class)) {
                    match = (match != null ? match : (Long) min);
                    stepSize = 1l;
                } else if (type.equals(Integer.class)) {
                    match = (match != null ? match : (Integer) min);
                    stepSize = 1;
                } else {
                    throw new IllegalArgumentException("Column must be number");
                }

                Number minNumber = (Number) min;
                Number maxNumber = (Number) max;
                if (match.doubleValue() < minNumber.doubleValue()) {
                    match = minNumber;
                    filter.getProperties()[1].setValue(minNumber);
                } else if (match.doubleValue() > maxNumber.doubleValue()) {
                    match = maxNumber;
                    filter.getProperties()[1].setValue(maxNumber);
                }

                final SpinnerNumberModel model = new SpinnerNumberModel(match, min, max, stepSize);
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        if (min.equals(Double.NEGATIVE_INFINITY) || min.equals(Integer.MIN_VALUE)) {
                            minLabel.setText("");
                            maxLabel.setText("");
                        } else {
                            DecimalFormat df = new DecimalFormat();
                            df.setMaximumFractionDigits(5);
                            minLabel.setText(df.format(min));
                            maxLabel.setText(df.format(max));
                        }

                        valueSpinner.setModel(model);
                        model.addChangeListener(WeakListeners.change(EqualNumberPanel.this, model));
                    }
                });
            }
        }).start();
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

        labelValue = new javax.swing.JLabel();
        valueSpinner = new javax.swing.JSpinner();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        minLabel = new javax.swing.JLabel();
        maxLabel = new javax.swing.JLabel();

        setOpaque(false);
        setLayout(new java.awt.GridBagLayout());

        labelValue.setText(org.openide.util.NbBundle.getMessage(EqualNumberPanel.class, "EqualNumberPanel.labelValue.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        add(labelValue, gridBagConstraints);

        valueSpinner.setPreferredSize(new java.awt.Dimension(75, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        add(valueSpinner, gridBagConstraints);

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 10));
        jLabel1.setText(org.openide.util.NbBundle.getMessage(EqualNumberPanel.class, "EqualNumberPanel.jLabel1.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(jLabel1, gridBagConstraints);

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 10));
        jLabel2.setText(org.openide.util.NbBundle.getMessage(EqualNumberPanel.class, "EqualNumberPanel.jLabel2.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(jLabel2, gridBagConstraints);

        minLabel.setFont(new java.awt.Font("Tahoma", 0, 10));
        minLabel.setText(org.openide.util.NbBundle.getMessage(EqualNumberPanel.class, "EqualNumberPanel.minLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(minLabel, gridBagConstraints);

        maxLabel.setFont(new java.awt.Font("Tahoma", 0, 10));
        maxLabel.setText(org.openide.util.NbBundle.getMessage(EqualNumberPanel.class, "EqualNumberPanel.maxLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(maxLabel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel labelValue;
    private javax.swing.JLabel maxLabel;
    private javax.swing.JLabel minLabel;
    private javax.swing.JSpinner valueSpinner;
    // End of variables declaration//GEN-END:variables
}
