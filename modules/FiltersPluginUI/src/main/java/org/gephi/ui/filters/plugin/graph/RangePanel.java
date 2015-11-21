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
package org.gephi.ui.filters.plugin.graph;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.gephi.filters.api.Range;
import org.gephi.filters.spi.RangeFilter;
import org.gephi.ui.components.JRangeSliderPanel;
import org.gephi.ui.components.richtooltip.RichTooltip;
import org.gephi.ui.filters.plugin.JQuickHistogram;

/**
 *
 * @author Mathieu Bastian
 */
public class RangePanel extends javax.swing.JPanel {

    private final JQuickHistogram histogram;
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
        final Range range = (Range) rangeFilter.getRangeProperty().getValue();
        if (range == null) {
            //Do nothing
        } else {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    final JRangeSliderPanel rangeSlider = (JRangeSliderPanel) rangeSliderPanel;
                    values = range.getValues();

                    rangeSlider.addPropertyChangeListener(new PropertyChangeListener() {

                        @Override
                        public void propertyChange(PropertyChangeEvent evt) {
                            try {
                                if (evt.getPropertyName().equals(JRangeSliderPanel.LOWER_BOUND)) {
                                    Range oldRange = (Range) filter.getRangeProperty().getValue();
                                    final Range newRange = new Range((Number) rangeSlider.getRange().getLowerBound(), (Number) rangeSlider.getRange().getUpperBound(), oldRange.getMinimum(), oldRange.getMaximum(), oldRange.getValues());
                                    if (!oldRange.equals(newRange)) {
                                        filter.getRangeProperty().setValue(newRange);
                                        new Thread(new Runnable() {

                                            @Override
                                            public void run() {
                                                setupHistogram(filter, newRange);
                                            }
                                        }).start();
                                    }
                                } else if (evt.getPropertyName().equals(JRangeSliderPanel.UPPER_BOUND)) {
                                    final Range oldRange = (Range) filter.getRangeProperty().getValue();
                                    final Range newRange = new Range((Number) rangeSlider.getRange().getLowerBound(), (Number) rangeSlider.getRange().getUpperBound(), oldRange.getMinimum(), oldRange.getMaximum(), oldRange.getValues());
                                    if (!oldRange.equals(newRange)) {
                                        filter.getRangeProperty().setValue(newRange);
                                        new Thread(new Runnable() {

                                            @Override
                                            public void run() {
                                                setupHistogram(filter, newRange);
                                            }
                                        }).start();
                                    }
                                }
                            } catch (Exception e) {
                                Logger.getLogger("").log(Level.SEVERE, "Error with range slider", e);
                            }
                        }
                    });

                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            rangeSlider.setRange(new JRangeSliderPanel.Range(
                                    rangeSlider, range.getMinimum(), range.getMaximum(), range.getLowerBound(), range.getUpperBound()));
                        }
                    });
                    setupHistogram(rangeFilter, range);
                }
            }).start();
        }


        //Tooltip
        /*
         * histogram.getPanel().addMouseListener(new MouseAdapter() {
         *
         * RichTooltip richTooltip;
         *
         * @Override public void mouseEntered(MouseEvent e) { new Thread(new
         * Runnable() {
         *
         * public void run() { richTooltip = buildTooltip();
         *
         * SwingUtilities.invokeLater(new Runnable() {
         *
         * public void run() { if (richTooltip != null) {
         * richTooltip.showTooltip(histogram.getPanel()); } } }); } }).start();
         * }
         *
         * @Override public void mouseExited(MouseEvent e) { if (richTooltip !=
         * null) { richTooltip.hideTooltip(); richTooltip = null; } } });
         */
    }

    private void setupHistogram(final RangeFilter rangeFilter, final Range range) {
        histogram.clear();
        for (Object value : values) {
            histogram.addData(value);
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
            rangeLowerBound = ((Double) range.getLowerBound());
            rangeUpperBound = ((Double) range.getUpperBound());
        } else if (range.getRangeType().equals(Long.class)) {
            rangeLowerBound = ((Long) range.getLowerBound()).doubleValue();
            rangeUpperBound = ((Long) range.getUpperBound()).doubleValue();
        }
        histogram.setLowerBound(rangeLowerBound);
        histogram.setUpperBound(rangeUpperBound);

        SwingUtilities.invokeLater(new Runnable() {

            @Override
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

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
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
