/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>, 
Patick J. McSweeney <pjmcswee@syr.edu>
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
package org.gephi.desktop.statistics;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.desktop.statistics.api.StatisticsControllerUI;
import org.gephi.desktop.statistics.api.StatisticsModelUI;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.statistics.api.StatisticsController;
import org.gephi.statistics.spi.DynamicStatistics;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsBuilder;
import org.gephi.statistics.spi.StatisticsUI;
import org.gephi.ui.components.SimpleHTMLReport;
import org.gephi.utils.longtask.api.LongTaskListener;
import org.gephi.utils.longtask.spi.LongTask;
import org.netbeans.validation.api.ui.ValidationPanel;
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
    private StatisticsModelUI currentModel;

    //Img

    public StatisticsFrontEnd(StatisticsUI ui) {
        initComponents();
        RUN = NbBundle.getMessage(StatisticsFrontEnd.class, "StatisticsFrontEnd.runStatus.run");
        CANCEL = NbBundle.getMessage(StatisticsFrontEnd.class, "StatisticsFrontEnd.runStatus.cancel");
        initUI(ui);

        runButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (runButton.getText().equals(RUN)) {
                    run();
                } else {
                    cancel();
                }
            }
        });

        reportButton.addActionListener(new ActionListener() {

            @Override
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
        displayLabel.setToolTipText(ui.getShortDescription());
        busyLabel.setVisible(false);
        runButton.setEnabled(false);
        runButton.setText(RUN);
        //runButton.setIcon(RUN_ICON);
        reportButton.setEnabled(false);
    }

    public void refreshModel(StatisticsModelUI model) {
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

    private void refreshResult(StatisticsModelUI model) {
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
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        GraphModel graphModel = graphController.getGraphModel();

        //Create Statistics
        StatisticsController controller = Lookup.getDefault().lookup(StatisticsController.class);
        StatisticsControllerUI controllerUI = Lookup.getDefault().lookup(StatisticsControllerUI.class);
        StatisticsBuilder builder = controller.getBuilder(statisticsUI.getStatisticsClass());
        currentStatistics = builder.getStatistics();
        if (currentStatistics != null) {
            if (currentStatistics instanceof DynamicStatistics && !graphModel.isDynamic()) {
                DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(NbBundle.getMessage(StatisticsFrontEnd.class, "StatisticsFrontEnd.notDynamicGraph"), NotifyDescriptor.WARNING_MESSAGE));
                return;
            }

            LongTaskListener listener = new LongTaskListener() {

                @Override
                public void taskFinished(LongTask task) {
                    showReport();
                }
            };
            JPanel settingsPanel = statisticsUI.getSettingsPanel();
            if (currentStatistics instanceof DynamicStatistics) {
                DynamicSettingsPanel dynamicPanel = new DynamicSettingsPanel();
                statisticsUI.setup(currentStatistics);
                dynamicPanel.setup((DynamicStatistics) currentStatistics);

                JPanel dynamicSettingsPanel = DynamicSettingsPanel.createCounpoundPanel(dynamicPanel, settingsPanel);
                final DialogDescriptor dd = new DialogDescriptor(dynamicSettingsPanel, NbBundle.getMessage(StatisticsTopComponent.class, "StatisticsFrontEnd.settingsPanel.title", builder.getName()));
                if (dynamicSettingsPanel instanceof ValidationPanel) {
                    ValidationPanel vp = (ValidationPanel) dynamicSettingsPanel;
                    vp.addChangeListener(new ChangeListener() {

                        @Override
                        public void stateChanged(ChangeEvent e) {
                            dd.setValid(!((ValidationPanel) e.getSource()).isProblem());
                        }
                    });
                }

                if (DialogDisplayer.getDefault().notify(dd).equals(NotifyDescriptor.OK_OPTION)) {
                    dynamicPanel.unsetup((DynamicStatistics) currentStatistics);
                    statisticsUI.unsetup();
                    controllerUI.execute(currentStatistics, listener);
                }
            } else if (settingsPanel != null) {
                statisticsUI.setup(currentStatistics);

                final DialogDescriptor dd = new DialogDescriptor(settingsPanel, NbBundle.getMessage(StatisticsTopComponent.class, "StatisticsFrontEnd.settingsPanel.title", builder.getName()));
                if (settingsPanel instanceof ValidationPanel) {
                    ValidationPanel vp = (ValidationPanel) settingsPanel;
                    vp.addChangeListener(new ChangeListener() {

                        @Override
                        public void stateChanged(ChangeEvent e) {
                            dd.setValid(!((ValidationPanel) e.getSource()).isProblem());
                        }
                    });
                }
                if (DialogDisplayer.getDefault().notify(dd).equals(NotifyDescriptor.OK_OPTION)) {
                    statisticsUI.unsetup();
                    controllerUI.execute(currentStatistics, listener);
                }
            } else {
                statisticsUI.setup(currentStatistics);
                controllerUI.execute(currentStatistics, listener);
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

                @Override
                public void run() {
                    SimpleHTMLReport dialog = new SimpleHTMLReport(WindowManager.getDefault().getMainWindow(), report);
                }
            });
        }
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
        runButton.setMargin(new java.awt.Insets(1, 2, 1, 2));
        runButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolbar.add(runButton);

        reportButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/statistics/resources/report.png"))); // NOI18N
        reportButton.setToolTipText(org.openide.util.NbBundle.getMessage(StatisticsFrontEnd.class, "StatisticsFrontEnd.reportButton.toolTipText")); // NOI18N
        reportButton.setFocusable(false);
        reportButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
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
