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
package org.gephi.ui.filters.topology;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.gephi.filters.api.Range;
import org.gephi.filters.spi.FilterProperty;
import org.gephi.ui.components.JRangeSliderPanel;
import org.gephi.ui.filters.JQuickHistogram;

/**
 *
 * @author Mathieu Bastian
 */
public class RangePanel extends javax.swing.JPanel {

    private JQuickHistogram histogram;
    private Object min = 0;
    private Object max = 100;

    public RangePanel() {
        initComponents();
        histogram = new JQuickHistogram();
        histogramPanel.add(histogram.getPanel());
        histogram.setConstraintHeight(30);
    }

    public void setup(final FilterProperty rangeProperty) {
        final JRangeSliderPanel r = (JRangeSliderPanel) rangeSliderPanel;
        //checkForUnitializedRange(rangeProperty);
        Range range = (Range) rangeProperty.getValue();

        r.setRange(new JRangeSliderPanel.Range(r, min, max, range.getLowerBound(), range.getUpperBound()));

        r.addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                try {
                    if (evt.getPropertyName().equals(JRangeSliderPanel.LOWER_BOUND)) {
                        Range newRange = new Range(r.getRange().getLowerBound(), r.getRange().getUpperBound());
                        rangeProperty.getProperty().setValue(newRange);
                        setupHistogram(newRange);
                    } else if (evt.getPropertyName().equals(JRangeSliderPanel.UPPER_BOUND)) {
                        Range newRange = new Range(r.getRange().getLowerBound(), r.getRange().getUpperBound());
                        rangeProperty.getProperty().setValue(newRange);
                        setupHistogram(newRange);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        setupHistogram(range);
    }

    private void setupHistogram(Range range) {
        histogram.clear();
        for (int i = 0; i < 1000; i++) {
            histogram.addData(Math.random() * 100);
        }
        histogram.sortData();
        float rangeLowerBound = 0f;
        float rangeUpperBound = 0f;
        if (range.getRangeType().equals(Integer.class)) {
            rangeLowerBound = ((Integer) range.getLowerBound()).floatValue();
            rangeUpperBound = ((Integer) range.getUpperBound()).floatValue();
        } else if (range.getRangeType().equals(Float.class)) {
            rangeLowerBound = ((Float) range.getLowerBound()).floatValue();
            rangeUpperBound = ((Float) range.getUpperBound()).floatValue();
        } else if (range.getRangeType().equals(Double.class)) {
            rangeLowerBound = ((Double) range.getLowerBound()).floatValue();
            rangeUpperBound = ((Double) range.getUpperBound()).floatValue();
        } else if (range.getRangeType().equals(Long.class)) {
            rangeLowerBound = ((Long) range.getLowerBound()).floatValue();
            rangeUpperBound = ((Long) range.getUpperBound()).floatValue();
        }
        //histogram.setRangeLowerBound(rangeLowerBound);
        //histogram.setRangeUpperBound(rangeUpperBound);
    }

    private void checkForUnitializedRange(FilterProperty rangeProperty) {
        try {
            if (rangeProperty.getValue() == null) {
                Range range = null;
                if (min instanceof Integer) {
                    range = new Range((Integer) min, (Integer) max);
                } else if (min instanceof Float) {
                    range = new Range((Float) min, (Float) max);
                } else if (min instanceof Double) {
                    range = new Range((Double) min, (Double) max);
                } else if (min instanceof Long) {
                    range = new Range((Long) min, (Long) max);
                }
                rangeProperty.getProperty().setValue(range);
            } else {
                Range range = (Range) rangeProperty.getValue();
                //Remove this
                if (range.getRangeType().equals(Integer.class)) {
                    min = new Integer(0);
                    max = new Integer(100);
                } else if (range.getRangeType().equals(Float.class)) {
                    min = new Float(0);
                    max = new Float(100);
                } else if (range.getRangeType().equals(Double.class)) {
                    min = new Double(0);
                    max = new Double(100);
                } else if (range.getRangeType().equals(Long.class)) {
                    min = new Long(0);
                    max = new Long(100);
                }
                //End
                if (range.getRangeType().equals(Integer.class)) {
                    Integer lowerBound = range.getLowerInteger();
                    Integer upperBound = range.getUpperInteger();
                    if ((Integer) min > lowerBound || (Integer) max < lowerBound || lowerBound.equals(upperBound)) {
                        lowerBound = (Integer) min;
                    }
                    if ((Integer) min > upperBound || (Integer) max < upperBound || lowerBound.equals(upperBound)) {
                        upperBound = (Integer) max;
                    }
                    range = new Range(lowerBound, upperBound);
                } else if (range.getRangeType().equals(Float.class)) {
                    Float lowerBound = range.getLowerFloat();
                    Float upperBound = range.getUpperFloat();
                    if ((Float) min > lowerBound || (Float) max < lowerBound || lowerBound.equals(upperBound)) {
                        lowerBound = (Float) min;
                    }
                    if ((Float) min > upperBound || (Float) max < upperBound || lowerBound.equals(upperBound)) {
                        upperBound = (Float) max;
                    }
                    range = new Range(lowerBound, upperBound);
                }
                if (range.getRangeType().equals(Double.class)) {
                    Double lowerBound = range.getLowerDouble();
                    Double upperBound = range.getUpperDouble();
                    if ((Double) min > lowerBound || (Double) max < lowerBound || lowerBound.equals(upperBound)) {
                        lowerBound = (Double) min;
                    }
                    if ((Double) min > upperBound || (Double) max < upperBound || lowerBound.equals(upperBound)) {
                        upperBound = (Double) max;
                    }
                    range = new Range(lowerBound, upperBound);
                }
                if (range.getRangeType().equals(Long.class)) {
                    Long lowerBound = range.getLowerLong();
                    Long upperBound = range.getUpperLong();
                    if ((Long) min > lowerBound || (Long) max < lowerBound || lowerBound.equals(upperBound)) {
                        lowerBound = (Long) min;
                    }
                    if ((Long) min > upperBound || (Long) max < upperBound || lowerBound.equals(upperBound)) {
                        upperBound = (Long) max;
                    }
                    range = new Range(lowerBound, upperBound);
                }
                if (!rangeProperty.getValue().equals(range)) {
                    rangeProperty.getProperty().setValue(range);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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

        rangeSliderPanel = new JRangeSliderPanel();
        histogramPanel = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());
        add(rangeSliderPanel, java.awt.BorderLayout.CENTER);

        histogramPanel.setLayout(new java.awt.BorderLayout());
        add(histogramPanel, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel histogramPanel;
    private javax.swing.JPanel rangeSliderPanel;
    // End of variables declaration//GEN-END:variables
}
