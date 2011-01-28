/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>, 
Patick J. McSweeney <pjmcswee@syr.edu>
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
package org.gephi.desktop.statistics;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsBuilder;
import org.gephi.statistics.api.StatisticsController;
import org.gephi.statistics.api.StatisticsModel;
import org.gephi.statistics.spi.StatisticsUI;
import org.gephi.ui.components.SimpleHTMLReport;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.longtask.api.LongTaskListener;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 *
 * @author Mathieu Bastian
 * @author Patick J. McSweeney
 */
public class StatisticsFrontEnd extends javax.swing.JPanel {

    private StatisticsUI statisticsUI;
    private final String RUN;
    private final String CANCEL;
    private final ImageIcon RUN_ICON;
    private final ImageIcon STOP_ICON;
    private Statistics currentStatistics;
    private StatisticsModel currentModel;

    //Img
    ;

    public StatisticsFrontEnd(StatisticsUI ui) {
        initComponents();
        RUN = NbBundle.getMessage(StatisticsFrontEnd.class, "StatisticsFrontEnd.runStatus.run");
        CANCEL = NbBundle.getMessage(StatisticsFrontEnd.class, "StatisticsFrontEnd.runStatus.cancel");
        initUI(ui);

        runButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (runButton.getText().equals(RUN)) {
                    run();
                } else {
                    cancel();
                }
            }
        });

        reportButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                showReport();
            }
        });

        RUN_ICON = ImageUtilities.loadImageIcon("org/gephi/desktop/statistics/resources/run.png", false);
        STOP_ICON = ImageUtilities.loadImageIcon("org/gephi/desktop/statistics/resources/stop.png", false);
    }

    private void initUI(StatisticsUI ui) {
        this.statisticsUI = ui;
        displayLabel.setText(ui.getDisplayName());
        busyLabel.setVisible(false);
        runButton.setEnabled(false);
        runButton.setText(RUN);
        //runButton.setIcon(RUN_ICON);
        reportButton.setEnabled(false);
    }

    public void refreshModel(StatisticsModel model) {
        currentModel = model;
        if (model == null) {
            runButton.setText(RUN);
            //runButton.setIcon(RUN_ICON);
            runButton.setEnabled(false);
            busyLabel.setBusy(false);
            busyLabel.setVisible(false);
            reportButton.setEnabled(false);
            resultLabel.setText("");
            currentStatistics = null;
            return;
        }
        runButton.setEnabled(true);
        if (model.isRunning(statisticsUI)) {
            runButton.setText(CANCEL);
            //runButton.setIcon(STOP_ICON);
            busyLabel.setVisible(true);
            busyLabel.setBusy(true);
            reportButton.setEnabled(false);
            resultLabel.setText("");
            if (currentStatistics == null) {
                currentStatistics = currentModel.getRunning(statisticsUI);
            }
        } else {
            runButton.setText(RUN);
            //runButton.setIcon(RUN_ICON);
            busyLabel.setBusy(false);
            busyLabel.setVisible(false);
            currentStatistics = null;
            refreshResult(model);
        }
    }

    private void refreshResult(StatisticsModel model) {
        //Find a computed stats
        String result = model.getResult(statisticsUI);

        if (result != null) {
            resultLabel.setText(result);
        } else {
            resultLabel.setText("");
        }

        String report = model.getReport(statisticsUI.getStatisticsClass());
        reportButton.setEnabled(report != null);
    }

    private void run() {
        //Create Statistics
        StatisticsController controller = Lookup.getDefault().lookup(StatisticsController.class);
        StatisticsBuilder builder = controller.getBuilder(statisticsUI.getStatisticsClass());
        currentStatistics = builder.getStatistics();
        if (currentStatistics != null) {
            LongTaskListener listener = new LongTaskListener() {

                public void taskFinished(LongTask task) {
                    showReport();
                }
            };

            JPanel settingsPanel = statisticsUI.getSettingsPanel();
            if (settingsPanel != null) {
                statisticsUI.setup(currentStatistics);
                DialogDescriptor dd = new DialogDescriptor(settingsPanel, NbBundle.getMessage(StatisticsTopComponent.class, "StatisticsFrontEnd.settingsPanel.title", builder.getName()));
                if (DialogDisplayer.getDefault().notify(dd).equals(NotifyDescriptor.OK_OPTION)) {
                    statisticsUI.unsetup();
                    controller.execute(currentStatistics, listener);
                }
            } else {
                statisticsUI.setup(currentStatistics);
                controller.execute(currentStatistics, listener);
            }
        }
    }

    private void cancel() {
        if (currentStatistics != null && currentStatistics instanceof LongTask) {
            LongTask longTask = (LongTask) currentStatistics;
            longTask.cancel();
        }
    }

    private void showReport() {
        final String report = currentModel.getReport(statisticsUI.getStatisticsClass());
        if (report != null) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    SimpleHTMLReport dialog = new SimpleHTMLReport(WindowManager.getDefault().getMainWindow(), report);
                }
            });
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

        busyLabel = new org.jdesktop.swingx.JXBusyLabel(new Dimension(16, 16));
        displayLabel = new javax.swing.JLabel();
        resultLabel = new javax.swing.JLabel();
        toolbar = new javax.swing.JToolBar();
        runButton = new javax.swing.JButton();
        reportButton = new javax.swing.JButton();

        setOpaque(false);
        setLayout(new java.awt.GridBagLayout());

        busyLabel.setText(org.openide.util.NbBundle.getMessage(StatisticsFrontEnd.class, "StatisticsFrontEnd.busyLabel.text")); // NOI18N
        busyLabel.setMinimumSize(new java.awt.Dimension(16, 16));
        busyLabel.setPreferredSize(new java.awt.Dimension(16, 16));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(busyLabel, gridBagConstraints);

        displayLabel.setText(org.openide.util.NbBundle.getMessage(StatisticsFrontEnd.class, "StatisticsFrontEnd.displayLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        add(displayLabel, gridBagConstraints);

        resultLabel.setText(org.openide.util.NbBundle.getMessage(StatisticsFrontEnd.class, "StatisticsFrontEnd.resultLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 3);
        add(resultLabel, gridBagConstraints);

        toolbar.setFloatable(false);
        toolbar.setRollover(true);
        toolbar.setOpaque(false);

        runButton.setText(org.openide.util.NbBundle.getMessage(StatisticsFrontEnd.class, "StatisticsFrontEnd.runButton.text")); // NOI18N
        runButton.setFocusable(false);
        runButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        runButton.setOpaque(false);
        runButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolbar.add(runButton);

        reportButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/statistics/resources/report.png"))); // NOI18N
        reportButton.setToolTipText(org.openide.util.NbBundle.getMessage(StatisticsFrontEnd.class, "StatisticsFrontEnd.reportButton.toolTipText")); // NOI18N
        reportButton.setFocusable(false);
        reportButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        reportButton.setOpaque(false);
        reportButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolbar.add(reportButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        add(toolbar, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.jdesktop.swingx.JXBusyLabel busyLabel;
    private javax.swing.JLabel displayLabel;
    private javax.swing.JButton reportButton;
    private javax.swing.JLabel resultLabel;
    private javax.swing.JButton runButton;
    private javax.swing.JToolBar toolbar;
    // End of variables declaration//GEN-END:variables
}
