/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.gephi.ui.filters.plugin.graph;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import javax.swing.SwingUtilities;
import org.gephi.filters.api.Range;
import org.gephi.filters.plugin.RangeFilter;
import org.gephi.ui.components.JRangeSliderPanel;
import org.gephi.ui.components.richtooltip.RichTooltip;
import org.gephi.ui.filters.plugin.JQuickHistogram;

/**
 *
 * @author Mathieu Bastian
 */
public class RangePanel extends javax.swing.JPanel {

    private JQuickHistogram histogram;
    //Info
    private Object[] values;
    private RangeFilter filter;

    public RangePanel() {
        initComponents();
        histogram = new JQuickHistogram();
        histogramPanel.add(histogram.getPanel());
        histogram.setConstraintHeight(30);
    }

    public void setup(final RangeFilter rangeFilter) {
        this.filter = rangeFilter;
        new Thread(new Runnable() {

            public void run() {
                final JRangeSliderPanel r = (JRangeSliderPanel) rangeSliderPanel;
                values = rangeFilter.getValues();
                final Range range = (Range) rangeFilter.getRangeProperty().getValue();

                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        r.setRange(new JRangeSliderPanel.Range(
                                r, rangeFilter.getMinimum(), rangeFilter.getMaximum(), range.getLowerBound(), range.getUpperBound()));
                    }
                });


                r.addPropertyChangeListener(new PropertyChangeListener() {

                    public void propertyChange(PropertyChangeEvent evt) {
                        try {
                            if (evt.getPropertyName().equals(JRangeSliderPanel.LOWER_BOUND)) {
                                final Range newRange = new Range(r.getRange().getLowerBound(), r.getRange().getUpperBound());
                                filter.getRangeProperty().setValue(newRange);
                                new Thread(new Runnable() {

                                    public void run() {
                                        setupHistogram(filter, newRange);
                                    }
                                }).start();
                            } else if (evt.getPropertyName().equals(JRangeSliderPanel.UPPER_BOUND)) {
                                final Range newRange = new Range(r.getRange().getLowerBound(), r.getRange().getUpperBound());
                                filter.getRangeProperty().setValue(newRange);
                                new Thread(new Runnable() {

                                    public void run() {
                                        setupHistogram(filter, newRange);
                                    }
                                }).start();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                setupHistogram(rangeFilter, range);
            }
        }).start();

        //Tooltip
        /*histogram.getPanel().addMouseListener(new MouseAdapter() {

        RichTooltip richTooltip;

        @Override
        public void mouseEntered(MouseEvent e) {
        new Thread(new Runnable() {

        public void run() {
        richTooltip = buildTooltip();

        SwingUtilities.invokeLater(new Runnable() {

        public void run() {
        if (richTooltip != null) {
        richTooltip.showTooltip(histogram.getPanel());
        }
        }
        });
        }
        }).start();
        }

        @Override
        public void mouseExited(MouseEvent e) {
        if (richTooltip != null) {
        richTooltip.hideTooltip();
        richTooltip = null;
        }
        }
        });*/
    }

    private void setupHistogram(final RangeFilter rangeFilter, final Range range) {
        histogram.clear();
        for (int i = 0; i < values.length; i++) {
            histogram.addData(values[i]);
        }
        histogram.sortData();
        double rangeLowerBound = 0.0;
        double rangeUpperBound = 0.0;
        if (range.getRangeType().equals(Integer.class)) {
            rangeLowerBound = ((Integer) range.getLowerBound()).doubleValue();
            rangeUpperBound = ((Integer) range.getUpperBound()).doubleValue();
        } else if (range.getRangeType().equals(Float.class)) {
            rangeLowerBound = ((Float) range.getLowerBound()).doubleValue();
            rangeUpperBound = ((Float) range.getUpperBound()).doubleValue();
        } else if (range.getRangeType().equals(Double.class)) {
            rangeLowerBound = ((Double) range.getLowerBound()).doubleValue();
            rangeUpperBound = ((Double) range.getUpperBound()).doubleValue();
        } else if (range.getRangeType().equals(Long.class)) {
            rangeLowerBound = ((Long) range.getLowerBound()).doubleValue();
            rangeUpperBound = ((Long) range.getUpperBound()).doubleValue();
        }
        histogram.setLowerBound(rangeLowerBound);
        histogram.setUpperBound(rangeUpperBound);

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                revalidate();
                repaint();
            }
        });
    }

    private RichTooltip buildTooltip() {
        if (histogram.countValues() == 0) {
            return null;
        }
        NumberFormat formatter = DecimalFormat.getNumberInstance();
        formatter.setMaximumFractionDigits(3);
        String average = formatter.format(histogram.getAverage());
        String averageInRange = formatter.format(histogram.getAverageInRange());
        RichTooltip richTooltip = new RichTooltip();
        richTooltip.setTitle("Statistics (In-Range)");
        richTooltip.addDescriptionSection("<html><b># of Values:</b> " + histogram.countValues() + " (" + histogram.countInRange() + ")");
        richTooltip.addDescriptionSection("<html><b>Average:</b> " + average + " (" + averageInRange + ")");
        richTooltip.addDescriptionSection("<html><b>Median:</b> " + histogram.getMedian() + " (" + histogram.getMedianInRange() + ")");
        return richTooltip;
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

        setOpaque(false);
        setLayout(new java.awt.BorderLayout());

        rangeSliderPanel.setOpaque(false);
        add(rangeSliderPanel, java.awt.BorderLayout.CENTER);

        histogramPanel.setOpaque(false);
        histogramPanel.setLayout(new java.awt.BorderLayout());
        add(histogramPanel, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel histogramPanel;
    private javax.swing.JPanel rangeSliderPanel;
    // End of variables declaration//GEN-END:variables
}
