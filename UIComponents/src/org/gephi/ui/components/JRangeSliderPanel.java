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
package org.gephi.ui.components;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author Mathieu Bastian
 */
public class JRangeSliderPanel extends javax.swing.JPanel {

    private static final int SLIDER_MAXIMUM = 1000;
    private String lowerBound = "NaN";
    private String upperBound = "NaN";
    private Range range;

    /** Creates new form JRangeSliderPanel */
    public JRangeSliderPanel() {
        initComponents();
        ((JRangeSlider)rangeSlider).setUpperValue(1000);

        lowerBoundTextField.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                lowerBoundTextField.setEnabled(true);
                lowerBoundTextField.selectAll();
            }
        });
        lowerBoundTextField.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (!lowerBoundTextField.getText().equals(lowerBound)) {
                    lowerBound = lowerBoundTextField.getText();
                    if (range != null) {
                        range.setLowerBound(lowerBound);
                        firePropertyChange("lowerBound", null, lowerBound);
                    }
                } else {
                    lowerBound = lowerBoundTextField.getText();
                }
                refreshBoundTexts();
                lowerBoundTextField.setEnabled(false);
            }
        });
        lowerBoundTextField.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                lowerBoundTextField.setEnabled(false);
            }
        });
        upperBoundTextField.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                upperBoundTextField.setEnabled(true);
                upperBoundTextField.selectAll();
            }
        });
        upperBoundTextField.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (!upperBoundTextField.getText().equals(upperBound)) {
                    upperBound = upperBoundTextField.getText();
                    if (range != null) {
                        range.setUpperBound(upperBound);
                        firePropertyChange("upperBound", null, upperBound);
                    }
                } else {
                    upperBound = upperBoundTextField.getText();
                }
                refreshBoundTexts();
                upperBoundTextField.setEnabled(false);
            }
        });
        upperBoundTextField.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                upperBoundTextField.setEnabled(false);
            }
        });

        rangeSlider.addChangeListener(new ChangeListener() {

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

    public void setRange(Range range) {
        if (!range.min.equals(range.max)) {
            this.range = range;
            range.refreshSlider();
            refreshBoundTexts();
        } else {
            lowerBound = "NaN";
            upperBound = "NaN";
            lowerBoundTextField.setText(lowerBound);
            upperBoundTextField.setText(upperBound);
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

        rangeSlider = new JRangeSlider();
        lowerBoundTextField = new javax.swing.JTextField();
        upperBoundTextField = new javax.swing.JTextField();

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
        lowerBoundTextField.setEnabled(false);
        lowerBoundTextField.setOpaque(false);
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
        upperBoundTextField.setEnabled(false);
        upperBoundTextField.setOpaque(false);
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

    public static class Range {

        private JRangeSlider slider;
        private Object min;
        private Object max;
        private Object lowerBound;
        private Object upperBound;
        private int sliderLowValue = -1;
        private int sliderUpValue = -1;

        public Range(JRangeSlider slider, Object min, Object max) {
            this.slider = slider;
            this.min = min;
            this.max = max;
            this.lowerBound = min;
            this.upperBound = max;
        }

        public Range(JRangeSlider slider, Object min, Object max, Object lowerBound, Object upperBound) {
            this.slider = slider;
            this.min = min;
            this.max = max;
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
        }

        private void setLowerBound(String bound) {
            if (min instanceof Float) {
                try {
                    Float l = Float.parseFloat(bound);
                    if (l < (Float) min) {
                        lowerBound = min;
                    } else if (l > (Float) upperBound) {
                        lowerBound = upperBound;
                    } else {
                        lowerBound = l;
                    }
                } catch (Exception e) {
                }
            } else if (min instanceof Double) {
                try {
                    Double l = Double.parseDouble(bound);
                    if (l < (Double) min) {
                        lowerBound = min;
                    } else if (l > (Double) upperBound) {
                        lowerBound = upperBound;
                    } else {
                        lowerBound = l;
                    }
                } catch (Exception e) {
                }
            } else if (min instanceof Integer) {
                try {
                    Integer l = Integer.parseInt(bound);
                    if (l < (Integer) min) {
                        lowerBound = min;
                    } else if (l > (Integer) upperBound) {
                        lowerBound = upperBound;
                    } else {
                        lowerBound = l;
                    }
                } catch (Exception e) {
                }
            } else if (min instanceof Long) {
                try {
                    Long l = Long.parseLong(bound);
                    if (l < (Long) min) {
                        lowerBound = min;
                    } else if (l > (Long) upperBound) {
                        lowerBound = upperBound;
                    } else {
                        lowerBound = l;
                    }
                } catch (Exception e) {
                }
            }
            refreshSlider();
        }

        private void setUpperBound(String bound) {
            if (min instanceof Float) {
                try {
                    Float l = Float.parseFloat(bound);
                    if (l > (Float) max) {
                        upperBound = max;
                    } else if (l < (Float) lowerBound) {
                        upperBound = lowerBound;
                    } else {
                        upperBound = l;
                    }
                } catch (Exception e) {
                }
            } else if (min instanceof Double) {
                try {
                    Double l = Double.parseDouble(bound);
                    if (l > (Double) max) {
                        upperBound = max;
                    } else if (l < (Double) lowerBound) {
                        upperBound = lowerBound;
                    } else {
                        upperBound = l;
                    }
                } catch (Exception e) {
                }
            } else if (min instanceof Integer) {
                try {
                    Integer l = Integer.parseInt(bound);
                    if (l > (Integer) max) {
                        upperBound = max;
                    } else if (l < (Integer) lowerBound) {
                        upperBound = lowerBound;
                    } else {
                        upperBound = l;
                    }
                } catch (Exception e) {
                }
            } else if (min instanceof Long) {
                try {
                    Long l = Long.parseLong(bound);
                    if (l > (Long) max) {
                        upperBound = max;
                    } else if (l < (Long) lowerBound) {
                        upperBound = lowerBound;
                    } else {
                        upperBound = l;
                    }
                } catch (Exception e) {
                }
            }
            refreshSlider();
        }

        private void refreshSlider() {
            double normalizedLow = 0.;
            double normalizedUp = 1.;
            if (min instanceof Float) {
                normalizedLow = ((Float) lowerBound - (Float) min) / ((Float) max - (Float) min);
                normalizedUp = ((Float) upperBound - (Float) min) / ((Float) max - (Float) min);
            } else if (min instanceof Double) {
                normalizedLow = ((Double) lowerBound - (Double) min) / ((Double) max - (Double) min);
                normalizedUp = ((Double) upperBound - (Double) min) / ((Double) max - (Double) min);
            } else if (min instanceof Integer) {
                normalizedLow = ((Integer) lowerBound - (Integer) min) / (double) ((Integer) max - (Integer) min);
                normalizedUp = ((Integer) upperBound - (Integer) min) / (double) ((Integer) max - (Integer) min);
            } else if (min instanceof Long) {
                normalizedLow = ((Long) lowerBound - (Long) min) / (double) ((Long) max - (Long) min);
                normalizedUp = ((Long) upperBound - (Long) min) / (double) ((Long) max - (Long) min);
            }
            sliderLowValue = (int) (normalizedLow * SLIDER_MAXIMUM);
            sliderUpValue = (int) (normalizedUp * SLIDER_MAXIMUM);
            slider.setValue(sliderLowValue);
            slider.setUpperValue(sliderUpValue);
        }

        private void refreshBounds() {
            boolean lowerChanged = slider.getValue() != sliderLowValue;
            boolean upperChanged = slider.getUpperValue() != sliderUpValue;
            sliderLowValue = slider.getValue();
            sliderUpValue = slider.getUpperValue();

            double normalizedLow = slider.getValue() / (double) SLIDER_MAXIMUM;
            double normalizedUp = slider.getUpperValue() / (double) SLIDER_MAXIMUM;
            if (min instanceof Float) {
                lowerBound = lowerChanged ? new Float((normalizedLow * ((Float) max - (Float) min)) + (Float) min) : lowerBound;
                upperBound = upperChanged ? new Float((normalizedUp * ((Float) max - (Float) min)) + (Float) min) : upperBound;
            } else if (min instanceof Double) {
                lowerBound = lowerChanged ? new Double((normalizedLow * ((Double) max - (Double) min)) + (Double) min) : lowerBound;
                upperBound = upperChanged ? new Double((normalizedUp * ((Double) max - (Double) min)) + (Double) min) : upperBound;
            } else if (min instanceof Integer) {
                lowerBound = lowerChanged ? new Integer((int) ((normalizedLow * ((Integer) max - (Integer) min)) + (Integer) min)) : lowerBound;
                upperBound = upperChanged ? new Integer((int) ((normalizedUp * ((Integer) max - (Integer) min)) + (Integer) min)) : upperBound;
            } else if (min instanceof Long) {
                lowerBound = lowerChanged ? new Long((long) ((normalizedLow * ((Long) max - (Long) min)) + (Long) min)) : lowerBound;
                upperBound = upperChanged ? new Long((long) ((normalizedUp * ((Long) max - (Long) min)) + (Long) min)) : upperBound;
            }
        }
    }
}
