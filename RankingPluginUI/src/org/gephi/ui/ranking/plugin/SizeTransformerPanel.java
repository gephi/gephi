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
package org.gephi.ui.ranking.plugin;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.ranking.api.Ranking;
import org.gephi.ranking.plugin.transformer.AbstractSizeTransformer;
import org.gephi.ranking.api.Transformer;
import org.gephi.ui.components.JRangeSlider;
import org.openide.util.NbPreferences;

/**
 *
 * @author Mathieu Bastian
 */
public class SizeTransformerPanel extends javax.swing.JPanel {

    private static final int SLIDER_MAXIMUM = 100;
    private AbstractSizeTransformer sizeTransformer;
    private Ranking ranking;

    public SizeTransformerPanel(Transformer transformer, Ranking ranking) {
        initComponents();

        final String MIN_SIZE = "SizeTransformerPanel_" + transformer.getClass().getSimpleName() + "_min";
        final String MAX_SIZE = "SizeTransformerPanel_" + transformer.getClass().getSimpleName() + "_max";

        sizeTransformer = (AbstractSizeTransformer) transformer;
        this.ranking = ranking;

        float minSizeStart = NbPreferences.forModule(SizeTransformerPanel.class).getFloat(MIN_SIZE, sizeTransformer.getMinSize());
        float maxSizeStart = NbPreferences.forModule(SizeTransformerPanel.class).getFloat(MAX_SIZE, sizeTransformer.getMaxSize());
        sizeTransformer.setMinSize(minSizeStart);
        sizeTransformer.setMaxSize(maxSizeStart);

        minSize.setValue(minSizeStart);
        maxSize.setValue(maxSizeStart);
        minSize.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                sizeTransformer.setMinSize((Float) minSize.getValue());
                NbPreferences.forModule(SizeTransformerPanel.class).putFloat(MIN_SIZE, (Float) minSize.getValue());
            }
        });
        maxSize.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                sizeTransformer.setMaxSize((Float) maxSize.getValue());
                NbPreferences.forModule(SizeTransformerPanel.class).putFloat(MAX_SIZE, (Float) maxSize.getValue());
            }
        });

        //Range
        JRangeSlider slider = (JRangeSlider) rangeSlider;
        slider.setMinimum(0);
        slider.setMaximum(SLIDER_MAXIMUM);
        slider.setValue(0);
        slider.setUpperValue(SLIDER_MAXIMUM);
        slider.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                JRangeSlider source = (JRangeSlider) e.getSource();
                if (!source.getValueIsAdjusting()) {
                    setRangeValues();
                }
            }
        });
        refreshRangeValues();
    }

    private void setRangeValues() {
        JRangeSlider slider = (JRangeSlider) rangeSlider;
        float low = slider.getValue() / 100f;
        float high = slider.getUpperValue() / 100f;
        sizeTransformer.setLowerBound(low);
        sizeTransformer.setUpperBound(high);

        lowerBoundLabel.setText(ranking.unNormalize(sizeTransformer.getLowerBound()).toString());
        upperBoundLabel.setText(ranking.unNormalize(sizeTransformer.getUpperBound()).toString());
    }

    private void refreshRangeValues() {
        JRangeSlider slider = (JRangeSlider) rangeSlider;
        slider.setValue((int) (sizeTransformer.getLowerBound() * 100f));
        slider.setUpperValue((int) (sizeTransformer.getUpperBound() * 100f));

        lowerBoundLabel.setText(ranking.unNormalize(sizeTransformer.getLowerBound()).toString());
        upperBoundLabel.setText(ranking.unNormalize(sizeTransformer.getUpperBound()).toString());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        labelMinSize = new javax.swing.JLabel();
        minSize = new javax.swing.JSpinner();
        labelMaxSize = new javax.swing.JLabel();
        maxSize = new javax.swing.JSpinner();
        labelRange = new javax.swing.JLabel();
        rangeSlider = new JRangeSlider();
        upperBoundLabel = new javax.swing.JLabel();
        lowerBoundLabel = new javax.swing.JLabel();

        setPreferredSize(new java.awt.Dimension(225, 114));

        labelMinSize.setText(org.openide.util.NbBundle.getMessage(SizeTransformerPanel.class, "SizeTransformerPanel.labelMinSize.text")); // NOI18N

        minSize.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(1.0f), Float.valueOf(0.1f), null, Float.valueOf(0.5f)));

        labelMaxSize.setText(org.openide.util.NbBundle.getMessage(SizeTransformerPanel.class, "SizeTransformerPanel.labelMaxSize.text")); // NOI18N

        maxSize.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(4.0f), Float.valueOf(0.5f), null, Float.valueOf(0.5f)));

        labelRange.setText(org.openide.util.NbBundle.getMessage(SizeTransformerPanel.class, "SizeTransformerPanel.labelRange.text")); // NOI18N

        rangeSlider.setFocusable(false);
        rangeSlider.setOpaque(false);

        upperBoundLabel.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        upperBoundLabel.setForeground(new java.awt.Color(102, 102, 102));
        upperBoundLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        upperBoundLabel.setText(org.openide.util.NbBundle.getMessage(SizeTransformerPanel.class, "SizeTransformerPanel.upperBoundLabel.text")); // NOI18N

        lowerBoundLabel.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        lowerBoundLabel.setForeground(new java.awt.Color(102, 102, 102));
        lowerBoundLabel.setText(org.openide.util.NbBundle.getMessage(SizeTransformerPanel.class, "SizeTransformerPanel.lowerBoundLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(labelMinSize)
                        .addGap(8, 8, 8)
                        .addComponent(minSize, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(labelMaxSize)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(maxSize, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(labelRange)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(rangeSlider, 0, 0, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(lowerBoundLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(upperBoundLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addContainerGap(19, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(minSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(maxSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGap(12, 12, 12))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                            .addComponent(labelMinSize)
                            .addGap(18, 18, 18)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(labelMaxSize)
                        .addGap(18, 18, 18)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelRange, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rangeSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lowerBoundLabel)
                    .addComponent(upperBoundLabel))
                .addContainerGap(20, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel labelMaxSize;
    private javax.swing.JLabel labelMinSize;
    private javax.swing.JLabel labelRange;
    private javax.swing.JLabel lowerBoundLabel;
    private javax.swing.JSpinner maxSize;
    private javax.swing.JSpinner minSize;
    private javax.swing.JSlider rangeSlider;
    private javax.swing.JLabel upperBoundLabel;
    // End of variables declaration//GEN-END:variables
}
