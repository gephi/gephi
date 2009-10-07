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
package org.gephi.ui.ranking.transformer;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.ranking.SizeTransformer;
import org.gephi.ranking.Transformer;
import org.gephi.ui.components.JRangeSlider;

/**
 *
 * @author Mathieu Bastian
 */
public class SizeTransformerPanel extends javax.swing.JPanel {

    private static final int SLIDER_MAXIMUM = 100;
    private SizeTransformer sizeTransformer;

    public SizeTransformerPanel(Transformer transformer) {
        initComponents();

        sizeTransformer = (SizeTransformer) transformer;

        minSize.setValue(sizeTransformer.getMinSize());
        maxSize.setValue(sizeTransformer.getMaxSize());
        minSize.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                sizeTransformer.setMinSize((Float) minSize.getValue());
            }
        });
        maxSize.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                sizeTransformer.setMaxSize((Float) maxSize.getValue());
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
        Object minVal = sizeTransformer.getMinimumValue();
        Object maxVal = sizeTransformer.getMaximumValue();
        if (minVal instanceof Float) {
            Float low = slider.getValue() * ((Float) maxVal - (Float) minVal) / SLIDER_MAXIMUM;
            Float up = slider.getUpperValue() * ((Float) maxVal - (Float) minVal) / SLIDER_MAXIMUM;
            sizeTransformer.setLowerBound(low);
            sizeTransformer.setUpperBound(up);
        } else if (minVal instanceof Double) {
            Double low = slider.getValue() * ((Double) maxVal - (Double) minVal) / SLIDER_MAXIMUM;
            Double up = slider.getUpperValue() * ((Double) maxVal - (Double) minVal) / SLIDER_MAXIMUM;
            sizeTransformer.setLowerBound(low);
            sizeTransformer.setUpperBound(up);
        } else if (minVal instanceof Integer) {
            Integer low = slider.getValue() * ((Integer) maxVal - (Integer) minVal) / SLIDER_MAXIMUM;
            Integer up = slider.getUpperValue() * ((Integer) maxVal - (Integer) minVal) / SLIDER_MAXIMUM;
            sizeTransformer.setLowerBound(low);
            sizeTransformer.setUpperBound(up);
        } else if (minVal instanceof Long) {
            Integer low = slider.getValue() * ((Integer) maxVal - (Integer) minVal) / SLIDER_MAXIMUM;
            Integer up = slider.getUpperValue() * ((Integer) maxVal - (Integer) minVal) / SLIDER_MAXIMUM;
            sizeTransformer.setLowerBound(low);
            sizeTransformer.setUpperBound(up);
        }
    }

    private void refreshRangeValues() {
        JRangeSlider slider = (JRangeSlider) rangeSlider;
        Object minVal = sizeTransformer.getMinimumValue();
        Object maxVal = sizeTransformer.getMaximumValue();
        if (minVal instanceof Float) {
            slider.setValue((int) (((Float) sizeTransformer.getLowerBound()) * SLIDER_MAXIMUM / ((Float) maxVal - (Float) minVal)));
            slider.setUpperValue((int) (((Float) sizeTransformer.getUpperBound()) * SLIDER_MAXIMUM / ((Float) maxVal - (Float) minVal)));
        } else if (minVal instanceof Double) {
            slider.setValue((int) (((Double) sizeTransformer.getLowerBound()) * SLIDER_MAXIMUM / ((Double) maxVal - (Double) minVal)));
            slider.setUpperValue((int) (((Double) sizeTransformer.getUpperBound()) * SLIDER_MAXIMUM / ((Double) maxVal - (Double) minVal)));
        } else if (minVal instanceof Integer) {
            slider.setValue((int) (((Integer) sizeTransformer.getLowerBound()) * SLIDER_MAXIMUM / ((Integer) maxVal - (Integer) minVal)));
            slider.setUpperValue((int) (((Integer) sizeTransformer.getUpperBound()) * SLIDER_MAXIMUM / ((Integer) maxVal - (Integer) minVal)));
        } else if (minVal instanceof Long) {
            slider.setValue((int) (((Long) sizeTransformer.getLowerBound()) * SLIDER_MAXIMUM / ((Long) maxVal - (Long) minVal)));
            slider.setUpperValue((int) (((Long) sizeTransformer.getUpperBound()) * SLIDER_MAXIMUM / ((Long) maxVal - (Long) minVal)));
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

        labelMinSize = new javax.swing.JLabel();
        minSize = new javax.swing.JSpinner();
        labelMaxSize = new javax.swing.JLabel();
        maxSize = new javax.swing.JSpinner();
        labelRange = new javax.swing.JLabel();
        rangeSlider = new JRangeSlider();

        labelMinSize.setText(org.openide.util.NbBundle.getMessage(SizeTransformerPanel.class, "SizeTransformerPanel.labelMinSize.text")); // NOI18N

        minSize.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(1.0f), Float.valueOf(0.1f), null, Float.valueOf(0.5f)));

        labelMaxSize.setText(org.openide.util.NbBundle.getMessage(SizeTransformerPanel.class, "SizeTransformerPanel.labelMaxSize.text")); // NOI18N

        maxSize.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(4.0f), Float.valueOf(0.5f), null, Float.valueOf(0.5f)));

        labelRange.setText(org.openide.util.NbBundle.getMessage(SizeTransformerPanel.class, "SizeTransformerPanel.labelRange.text")); // NOI18N

        rangeSlider.setOpaque(false);

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
                        .addComponent(minSize, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(labelMaxSize)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(maxSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(labelRange)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(rangeSlider, 0, 0, Short.MAX_VALUE)))
                .addContainerGap(50, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelMinSize)
                    .addComponent(minSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelMaxSize)
                    .addComponent(maxSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelRange, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rangeSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(25, 25, 25))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel labelMaxSize;
    private javax.swing.JLabel labelMinSize;
    private javax.swing.JLabel labelRange;
    private javax.swing.JSpinner maxSize;
    private javax.swing.JSpinner minSize;
    private javax.swing.JSlider rangeSlider;
    // End of variables declaration//GEN-END:variables
}
