/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke, Patrick J. McSweeney (pjmcswee@syr.edu)
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
package org.gephi.statistics.controller;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.util.ArrayList;
import org.gephi.statistics.api.*;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import org.gephi.graph.api.GraphController;
import org.gephi.statistics.ui.api.StatisticsUI;
import org.gephi.utils.longtask.LongTask;
import org.gephi.utils.longtask.LongTaskExecutor;
import org.gephi.utils.longtask.LongTaskListener;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian, Patrick J. McSweeney
 */
public class StatisticsControllerImpl implements StatisticsController, LongTaskListener {

    private List<Statistics> statistics;

    /**
     *
     */
    public StatisticsControllerImpl() {
        statistics = new ArrayList<Statistics>(Lookup.getDefault().lookupAll(Statistics.class));

    }

    /**
    *
    * @param statistics
    */
    private void complete(final Statistics statistics) {
        final GraphController graphController = Lookup.getDefault().lookup(GraphController.class);

        if(statistics instanceof LongTask)
        {
            LongTaskExecutor executor = new LongTaskExecutor(true, statistics.getName(), 10);
            executor.setLongTaskListener(this);
            executor.execute((LongTask)statistics, new Runnable() {
                 public void run() {
                     statistics.execute(graphController); }
             },statistics.getName(),null);
        }
        else
        {
            statistics.execute(graphController);
            StatisticsReporterImpl reporter = new StatisticsReporterImpl(statistics);

        }
    }

    /**
     * 
     * @param statistics
     */
    public void execute(final Statistics statistics) {

        if (statistics.isParamerizable()) {
            final JDialog dialog = new JDialog((JDialog) null, statistics.toString());
            Container container = dialog.getContentPane();
            final StatisticsUI ui = statistics.getUI();
            ui.setup(statistics);
            container.add(ui.getPanel());
            JButton next = new JButton("Run");

            next.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    ui.unsetup();
                    dialog.dispose();
                    complete(statistics);

                }
            });

            JButton cancel = new JButton("Cancel");

            JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
            buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
            buttonPane.add(Box.createHorizontalGlue());
            buttonPane.add(cancel);
            buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
            buttonPane.add(next);

            container.add(buttonPane, BorderLayout.PAGE_END);

            Dimension dimension = new Dimension(500, 250);
            dialog.setSize(dimension);
            dialog.setLocationRelativeTo(null);
            dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            dialog.setDefaultLookAndFeelDecorated(true);
            dialog.pack();
            dialog.setVisible(true);
        } else {
            complete(statistics);
        }

    }

    /**
     * 
     * @return
     */
    public List<Statistics> getStatistics() {
        return statistics;
    }

    /**
     *
     * @param task
     */
    public void taskFinished(LongTask task) {
        Statistics statistics = (Statistics)task;
        StatisticsReporterImpl reporter = new StatisticsReporterImpl(statistics);
    }
}
