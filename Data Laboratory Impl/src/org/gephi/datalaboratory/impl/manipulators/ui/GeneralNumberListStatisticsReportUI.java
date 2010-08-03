/*
Copyright 2008-2010 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
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
package org.gephi.datalaboratory.impl.manipulators.ui;

import java.math.BigDecimal;
import javax.swing.JPanel;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.datalaboratory.spi.DialogControls;
import org.gephi.datalaboratory.spi.Manipulator;
import org.gephi.datalaboratory.spi.ManipulatorUI;
import org.gephi.datalaboratory.spi.attributecolumns.AttributeColumnsManipulator;
import org.gephi.datalaboratory.spi.attributecolumns.AttributeColumnsManipulatorUI;
import org.gephi.ui.components.JFreeChartDialog;
import org.gephi.ui.components.SimpleHTMLReport;
import org.gephi.utils.ChartsUtils;
import org.gephi.utils.StatisticsUtils;
import org.jfree.chart.JFreeChart;
import org.openide.windows.WindowManager;

/**
 * UI for NumberColumnStatisticsReport AttributeColumnsManipulator.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public class GeneralNumberListStatisticsReportUI extends javax.swing.JPanel implements AttributeColumnsManipulatorUI, ManipulatorUI {

    private Number[] numbers;
    private String dataName;
    private String dialogTitle;
    private BigDecimal[] statistics;
    private JFreeChart boxPlot, scatterPlot, histogram;
    private JFreeChartDialog boxPlotDialog, scatterPlotDialog, histogramDialog;
    private SimpleHTMLReport reportDialog;
    private int histogramDivisions;
    private static final int MIN_HISTOGRAM_DIVISIONS = 1, MAX_HISTOGRAM_DIVISIONS = 50;

    /**
     * Constructor method to set all necessary information to build statistics, charts and dialog title.
     * @param numbers Numbers to build statistics and charts
     * @param dataName Name of the numbers data (column title for example)
     * @param dialogTitle Title of the dialog window
     */
    public GeneralNumberListStatisticsReportUI(Number[] numbers, String dataName, String dialogTitle) {
        initComponents();
        this.numbers = numbers;
        this.dataName = dataName;
        this.dialogTitle = dialogTitle;
        statistics = StatisticsUtils.getAllStatistics(numbers);
        for (int i = MIN_HISTOGRAM_DIVISIONS; i <= MAX_HISTOGRAM_DIVISIONS; i++) {
            divisionsComboBox.addItem(i);
        }
        divisionsComboBox.setSelectedIndex(9);
        setChartControlsEnabled(statistics != null);//Disable chart controls if no numbers available
    }

    public void setup(AttributeColumnsManipulator m, AttributeTable table, AttributeColumn column, DialogControls dialogControls) {
    }

    public void setup(Manipulator m, DialogControls dialogControls) {
    }

    private void setChartControlsEnabled(boolean enabled) {
        configureBoxPlotButton.setEnabled(enabled);
        configureScatterPlotButton.setEnabled(enabled);
        configureHistogramButton.setEnabled(enabled);
        useLinearRegression.setEnabled(enabled);
        useLinesCheckBox.setEnabled(enabled);
        divisionsLabel.setEnabled(enabled);
        divisionsComboBox.setEnabled(enabled);
    }

    public void unSetup() {
        if (reportDialog != null) {
            reportDialog.dispose();
        }
        if (boxPlotDialog != null) {
            boxPlotDialog.dispose();
        }
        if (scatterPlotDialog != null) {
            scatterPlotDialog.dispose();
        }
        if (histogramDialog != null) {
            histogramDialog.dispose();
        }
    }

    public String getDisplayName() {
        return dialogTitle;
    }

    public JPanel getSettingsPanel() {
        return this;
    }

    public boolean isModal() {
        return false;
    }

    private void prepareBoxPlot() {
        if (boxPlot == null) {
            boxPlot = ChartsUtils.buildBoxPlot(numbers, dataName);
        }
    }

    private void prepareScatterPlot() {
        if (scatterPlot == null) {
            scatterPlot = ChartsUtils.buildScatterPlot(numbers, dataName, useLinesCheckBox.isSelected(), useLinearRegression.isSelected());
        }
    }

    private void prepareHistogram() {
        histogram = ChartsUtils.buildHistogram(numbers, dataName, histogramDivisions);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        configureBoxPlotButton = new javax.swing.JButton();
        configureScatterPlotButton = new javax.swing.JButton();
        showReportButton = new javax.swing.JButton();
        useLinesCheckBox = new javax.swing.JCheckBox();
        useLinearRegression = new javax.swing.JCheckBox();
        jSeparator1 = new javax.swing.JSeparator();
        configureHistogramButton = new javax.swing.JButton();
        divisionsLabel = new javax.swing.JLabel();
        divisionsComboBox = new javax.swing.JComboBox();

        configureBoxPlotButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/datalaboratory/impl/manipulators/resources/wooden-box.png"))); // NOI18N
        configureBoxPlotButton.setText(org.openide.util.NbBundle.getMessage(GeneralNumberListStatisticsReportUI.class, "GeneralNumberListStatisticsReportUI.configureBoxPlotButton.text")); // NOI18N
        configureBoxPlotButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configureBoxPlotButtonActionPerformed(evt);
            }
        });

        configureScatterPlotButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/datalaboratory/impl/manipulators/resources/chart-up.png"))); // NOI18N
        configureScatterPlotButton.setText(org.openide.util.NbBundle.getMessage(GeneralNumberListStatisticsReportUI.class, "GeneralNumberListStatisticsReportUI.configureScatterPlotButton.text_1")); // NOI18N
        configureScatterPlotButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configureScatterPlotButtonActionPerformed(evt);
            }
        });

        showReportButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/datalaboratory/impl/manipulators/resources/application-block.png"))); // NOI18N
        showReportButton.setText(org.openide.util.NbBundle.getMessage(GeneralNumberListStatisticsReportUI.class, "GeneralNumberListStatisticsReportUI.showReportButton.text")); // NOI18N
        showReportButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showReportButtonActionPerformed(evt);
            }
        });

        useLinesCheckBox.setText(org.openide.util.NbBundle.getMessage(GeneralNumberListStatisticsReportUI.class, "GeneralNumberListStatisticsReportUI.useLinesCheckBox.text")); // NOI18N
        useLinesCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useLinesCheckBoxActionPerformed(evt);
            }
        });

        useLinearRegression.setText(org.openide.util.NbBundle.getMessage(GeneralNumberListStatisticsReportUI.class, "GeneralNumberListStatisticsReportUI.useLinearRegression.text")); // NOI18N
        useLinearRegression.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useLinearRegressionActionPerformed(evt);
            }
        });

        configureHistogramButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/datalaboratory/impl/manipulators/resources/chart.png"))); // NOI18N
        configureHistogramButton.setText(org.openide.util.NbBundle.getMessage(GeneralNumberListStatisticsReportUI.class, "GeneralNumberListStatisticsReportUI.configureHistogramButton.text")); // NOI18N
        configureHistogramButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configureHistogramButtonActionPerformed(evt);
            }
        });

        divisionsLabel.setText(org.openide.util.NbBundle.getMessage(GeneralNumberListStatisticsReportUI.class, "GeneralNumberListStatisticsReportUI.divisionsLabel.text")); // NOI18N

        divisionsComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                divisionsComboBoxItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(showReportButton)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(configureHistogramButton, javax.swing.GroupLayout.DEFAULT_SIZE, 163, Short.MAX_VALUE)
                            .addComponent(configureScatterPlotButton, javax.swing.GroupLayout.DEFAULT_SIZE, 163, Short.MAX_VALUE)
                            .addComponent(configureBoxPlotButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 163, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(useLinesCheckBox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(useLinearRegression))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addComponent(divisionsLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(divisionsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap())
            .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 393, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(configureBoxPlotButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(11, 11, 11)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(configureScatterPlotButton)
                    .addComponent(useLinesCheckBox)
                    .addComponent(useLinearRegression))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(configureHistogramButton)
                    .addComponent(divisionsLabel)
                    .addComponent(divisionsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 1, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(showReportButton)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void configureBoxPlotButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configureBoxPlotButtonActionPerformed
        prepareBoxPlot();
        if (boxPlotDialog != null) {
            boxPlotDialog.setVisible(true);
        } else {
            boxPlotDialog = new JFreeChartDialog(WindowManager.getDefault().getMainWindow(), boxPlot.getTitle().getText(), boxPlot, 300, 500);
        }
    }//GEN-LAST:event_configureBoxPlotButtonActionPerformed

    private void configureScatterPlotButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configureScatterPlotButtonActionPerformed
        prepareScatterPlot();
        if (scatterPlotDialog != null) {
            scatterPlotDialog.setVisible(true);
        } else {
            scatterPlotDialog = new JFreeChartDialog(WindowManager.getDefault().getMainWindow(), scatterPlot.getTitle().getText(), scatterPlot, 600, 400);
        }
    }//GEN-LAST:event_configureScatterPlotButtonActionPerformed

    private void showReportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showReportButtonActionPerformed
        prepareBoxPlot();
        prepareScatterPlot();
        if (histogram == null) {
            prepareHistogram();
        }
        final String html = ChartsUtils.getStatisticsReportHTML(dataName, statistics, boxPlot, scatterPlot, histogram, boxPlotDialog != null ? boxPlotDialog.getChartSize() : null, scatterPlotDialog != null ? scatterPlotDialog.getChartSize() : null, histogramDialog != null ? histogramDialog.getChartSize() : null);

        if (reportDialog != null) {
            reportDialog.dispose();
        }
        reportDialog = new SimpleHTMLReport(WindowManager.getDefault().getMainWindow(), html);
    }//GEN-LAST:event_showReportButtonActionPerformed

    private void useLinesCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useLinesCheckBoxActionPerformed
        if (scatterPlot != null) {
            ChartsUtils.setScatterPlotLinesEnabled(scatterPlot, useLinesCheckBox.isSelected());
        }
    }//GEN-LAST:event_useLinesCheckBoxActionPerformed

    private void useLinearRegressionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useLinearRegressionActionPerformed
        if (scatterPlot != null) {
            ChartsUtils.setScatterPlotLinearRegressionEnabled(scatterPlot, useLinearRegression.isSelected());
        }
    }//GEN-LAST:event_useLinearRegressionActionPerformed

    private void divisionsComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_divisionsComboBoxItemStateChanged
        this.histogramDivisions = divisionsComboBox.getSelectedIndex() + 1;
        if (histogramDialog != null) {
            prepareHistogram();
            histogramDialog.setChart(histogram);
        }
    }//GEN-LAST:event_divisionsComboBoxItemStateChanged

    private void configureHistogramButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configureHistogramButtonActionPerformed
        prepareHistogram();
        if (histogramDialog != null) {
            histogramDialog.setVisible(true);
        } else {
            histogramDialog = new JFreeChartDialog(WindowManager.getDefault().getMainWindow(), histogram.getTitle().getText(), histogram, 600, 400);
        }
    }//GEN-LAST:event_configureHistogramButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton configureBoxPlotButton;
    private javax.swing.JButton configureHistogramButton;
    private javax.swing.JButton configureScatterPlotButton;
    private javax.swing.JComboBox divisionsComboBox;
    private javax.swing.JLabel divisionsLabel;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JButton showReportButton;
    private javax.swing.JCheckBox useLinearRegression;
    private javax.swing.JCheckBox useLinesCheckBox;
    // End of variables declaration//GEN-END:variables
}
