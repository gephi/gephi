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
package org.gephi.ui.components;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.utils.NumberUtils;

/**
 *
 * @author Mathieu Bastian
 */
public class JRangeSliderPanel extends javax.swing.JPanel {

    public static final String LOWER_BOUND = "lowerbound";
    public static final String UPPER_BOUND = "upperbound";
    private static final int SLIDER_MAXIMUM = 1000;
    private String lowerBound = "N/A";
    private String upperBound = "N/A";
    private Range range;

    /**
     * Creates new form JRangeSliderPanel
     */
    public JRangeSliderPanel() {
        initComponents();
        ((JRangeSlider) rangeSlider).setUpperValue(1000);
        rangeSlider.setOpaque(false);
        lowerBoundTextField.setOpaque(false);
        lowerBoundTextField.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        upperBoundTextField.setOpaque(false);
        upperBoundTextField.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        lowerBoundTextField.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                lowerBoundTextField.selectAll();
            }
        });
        lowerBoundTextField.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!lowerBoundTextField.getText().equals(lowerBound)) {
                    lowerBound = lowerBoundTextField.getText();
                    if (range != null) {
                        range.setLowerBound(lowerBound);
                        firePropertyChange(LOWER_BOUND, null, lowerBound);
                    }
                } else {
                    lowerBound = lowerBoundTextField.getText();
                }
                refreshBoundTexts();
                JRangeSliderPanel.this.requestFocusInWindow();
            }
        });
        upperBoundTextField.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                upperBoundTextField.selectAll();
            }
        });
        upperBoundTextField.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!upperBoundTextField.getText().equals(upperBound)) {
                    upperBound = upperBoundTextField.getText();
                    if (range != null) {
                        range.setUpperBound(upperBound);
                        firePropertyChange(UPPER_BOUND, null, upperBound);
                    }
                } else {
                    upperBound = upperBoundTextField.getText();
                }
                refreshBoundTexts();
                JRangeSliderPanel.this.requestFocusInWindow();
            }
        });

        rangeSlider.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                JRangeSlider source = (JRangeSlider) e.getSource();
                if (!source.getValueIsAdjusting()) {
                    if (range != null) {
                        range.refreshBounds();
                        refreshBoundTexts();
                    }
                }
            }
        });
    }

    private void refreshBoundTexts() {
        if (range != null) {
            lowerBound = range.lowerBound.toString();
            upperBound = range.upperBound.toString();
            lowerBoundTextField.setText(lowerBound);
            upperBoundTextField.setText(upperBound);
        }
    }

    public JRangeSlider getSlider() {
        return (JRangeSlider) rangeSlider;
    }

    public Range getRange() {
        return range;
    }

    public void setRange(Range range) {
        if (!range.min.equals(range.max)) {
            this.range = range;
            rangeSlider.setEnabled(true);
            range.refreshSlider();
            refreshBoundTexts();
        } else {
            lowerBound = range.lowerBound.toString();
            upperBound = range.upperBound.toString();
            lowerBoundTextField.setText(lowerBound);
            upperBoundTextField.setText(upperBound);
            rangeSlider.setEnabled(false);
        }

    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        rangeSlider = new JRangeSlider();
        lowerBoundTextField = new javax.swing.JTextField();
        upperBoundTextField = new javax.swing.JTextField();

        setOpaque(false);
        setLayout(new java.awt.GridBagLayout());

        rangeSlider.setMaximum(1000);
        rangeSlider.setValue(0);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(rangeSlider, gridBagConstraints);

        lowerBoundTextField.setText(org.openide.util.NbBundle.getMessage(JRangeSliderPanel.class, "JRangeSliderPanel.lowerBoundTextField.text")); // NOI18N
        lowerBoundTextField.setBorder(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        add(lowerBoundTextField, gridBagConstraints);

        upperBoundTextField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        upperBoundTextField.setText(org.openide.util.NbBundle.getMessage(JRangeSliderPanel.class, "JRangeSliderPanel.upperBoundTextField.text")); // NOI18N
        upperBoundTextField.setBorder(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        add(upperBoundTextField, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField lowerBoundTextField;
    private javax.swing.JSlider rangeSlider;
    private javax.swing.JTextField upperBoundTextField;
    // End of variables declaration//GEN-END:variables

    public static class Range<T extends Number & Comparable> {

        private final JRangeSliderPanel slider;
        private final Class<T> type;
        private T min;
        private T max;
        private T lowerBound;
        private T upperBound;
        private int sliderLowValue = -1;
        private int sliderUpValue = -1;

        public Range(JRangeSliderPanel slider, T min, T max, T lowerBound, T upperBound, Class<T> type) {
            this.slider = slider;
            this.min = min;
            this.max = max;
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
            this.type = type;
        }

        public Range(JRangeSliderPanel slider, T min, T max, Class<T> type) {
            this(slider, min, max, min, max, type);
        }

        public T getLowerBound() {
            return lowerBound;
        }

        public T getUpperBound() {
            return upperBound;
        }

        private void setLowerBound(String bound) {
            try {
                T v = (T) NumberUtils.parseNumber(bound, type);

                if (v.compareTo(min) < 0) {
                    lowerBound = min;
                } else if (v.compareTo(upperBound) > 0) {
                    lowerBound = upperBound;
                } else {
                    lowerBound = v;
                }
            } catch (Exception ex) {
            }
            refreshSlider();
        }

        private void setUpperBound(String bound) {
            try {
                T v = (T) NumberUtils.parseNumber(bound, type);
                if (v.compareTo(max) > 0) {
                    upperBound = max;
                } else if (v.compareTo(lowerBound) < 0) {
                    upperBound = lowerBound;
                } else {
                    upperBound = v;
                }
            } catch (Exception e) {
            }
            refreshSlider();
        }

        private void refreshSlider() {
            BigDecimal lowerBoundBigDecimal = new BigDecimal(lowerBound.toString());
            BigDecimal upperBoundBigDecimal = new BigDecimal(upperBound.toString());
            BigDecimal minBigDecimal = new BigDecimal(min.toString());
            BigDecimal maxBigDecimal = new BigDecimal(max.toString());

            double normalizedLow = (lowerBoundBigDecimal.subtract(minBigDecimal))
                    .divide(maxBigDecimal.subtract(minBigDecimal), RoundingMode.HALF_UP)
                    .doubleValue();
            double normalizedUp = (upperBoundBigDecimal.subtract(minBigDecimal))
                    .divide(maxBigDecimal.subtract(minBigDecimal), RoundingMode.HALF_UP)
                    .doubleValue();

            sliderLowValue = (int) (normalizedLow * SLIDER_MAXIMUM);
            sliderUpValue = (int) (normalizedUp * SLIDER_MAXIMUM);
            slider.getSlider().setValues(sliderLowValue, sliderUpValue);
        }

        private void refreshBounds() {
            boolean lowerChanged = slider.getSlider().getValue() != sliderLowValue;
            boolean upperChanged = slider.getSlider().getUpperValue() != sliderUpValue;
            sliderLowValue = slider.getSlider().getValue();
            sliderUpValue = slider.getSlider().getUpperValue();

            double normalizedLow = slider.getSlider().getValue() / (double) SLIDER_MAXIMUM;
            double normalizedUp = slider.getSlider().getUpperValue() / (double) SLIDER_MAXIMUM;

            if (lowerChanged || upperChanged) {
                BigDecimal minBigDecimal = new BigDecimal(min.toString());
                BigDecimal maxBigDecimal = new BigDecimal(max.toString());

                if (lowerChanged) {
                    BigDecimal newLowerBound = (BigDecimal.valueOf(normalizedLow).multiply(maxBigDecimal.subtract(minBigDecimal))).add(minBigDecimal);

                    if (type.equals(Double.class) || type.equals(Float.class) || type.equals(BigDecimal.class)) {
                        lowerBound = NumberUtils.parseNumber(newLowerBound.toString(), type);
                    } else {
                        lowerBound = NumberUtils.parseNumber(newLowerBound.setScale(0, RoundingMode.HALF_UP).toBigInteger().toString(), type);
                    }

                    slider.firePropertyChange(LOWER_BOUND, null, lowerBound);
                }

                if (upperChanged) {
                    BigDecimal newUpperBound = (BigDecimal.valueOf(normalizedUp).multiply(maxBigDecimal.subtract(minBigDecimal))).add(minBigDecimal);

                    if (type.equals(Double.class) || type.equals(Float.class) || type.equals(BigDecimal.class)) {
                        upperBound = NumberUtils.parseNumber(newUpperBound.toString(), type);
                    } else {
                        upperBound = NumberUtils.parseNumber(newUpperBound.setScale(0, RoundingMode.HALF_UP).toBigInteger().toString(), type);
                    }

                    slider.firePropertyChange(UPPER_BOUND, null, upperBound);
                }
            }
        }

        public static Range build(JRangeSliderPanel slider, Number min, Number max) {
            return build(slider, min, max, min, max);
        }

        public static Range build(JRangeSliderPanel slider, Number min, Number max, Number lowerBound, Number upperBound) {
            if (min instanceof Double) {
                return new Range(slider, min, max, lowerBound, upperBound, Double.class);
            } else if (min instanceof Float) {
                return new Range(slider, min, max, lowerBound, upperBound, Float.class);
            } else if (min instanceof Long) {
                return new Range(slider, min, max, lowerBound, upperBound, Long.class);
            } else if (min instanceof Integer) {
                return new Range(slider, min, max, lowerBound, upperBound, Integer.class);
            } else if (min instanceof Short) {
                return new Range(slider, min, max, lowerBound, upperBound, Short.class);
            } else if (min instanceof Byte) {
                return new Range(slider, min, max, lowerBound, upperBound, Byte.class);
            } else if (min instanceof BigDecimal) {
                return new Range(slider, min, max, lowerBound, upperBound, BigDecimal.class);
            } else if (min instanceof BigInteger) {
                return new Range(slider, min, max, lowerBound, upperBound, BigInteger.class);
            } else {
                throw new UnsupportedOperationException("Unsupported number type " + min.getClass().getName());
            }
        }
    }
}
