/*
Copyright 2008-2010 Gephi
Authors : Patick J. McSweeney <pjmcswee@syr.edu>,
          Mathieu Bastian <mathieu.bastian@gephi.org>
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
package org.gephi.statistics;

import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.api.StatisticsModel;
import org.gephi.statistics.spi.StatisticsUI;

/**
 *
 * @author Mathieu Bastian
 * @author Patrick J. McSweeney
 */
public class StatisticsModelImpl implements StatisticsModel {

    //Model
    private List<StatisticsUI> invisibleList;
    private List<StatisticsUI> runningList;
    private List<Statistics> statisticsList;
    //Listeners
    private List<ChangeListener> listeners;

    public StatisticsModelImpl() {
        invisibleList = new ArrayList<StatisticsUI>();
        runningList = new ArrayList<StatisticsUI>();
        listeners = new ArrayList<ChangeListener>();
        statisticsList = new ArrayList<Statistics>();
    }

    public Statistics[] getStatistics() {
        return statisticsList.toArray(new Statistics[0]);
    }

    public void addStatistics(Statistics statistics) {
        if (!statisticsList.contains(statistics)) { //Add the Statistic instance to statisticsList only if it not already in there
            statisticsList.add(statistics);
            fireChangeEvent();
        }
    }

    public Statistics getStatistics(StatisticsUI statisticsUI) {
        Class c = statisticsUI.getStatisticsClass();
        for (Statistics b : statisticsList) {
            if (b.getClass().equals(c)) {
                return b;
            }
        }
        return null;
    }

    public boolean isStatisticsUIVisible(StatisticsUI statisticsUI) {
        return !invisibleList.contains(statisticsUI);
    }

    public boolean isRunning(StatisticsUI statisticsUI) {
        return runningList.contains(statisticsUI);
    }

    public void setRunning(StatisticsUI statisticsUI, boolean running) {
        if (!running) {
            if (runningList.remove(statisticsUI)) {
                fireChangeEvent();
            }
        } else if (!runningList.contains(statisticsUI)) {
            runningList.add(statisticsUI);
            fireChangeEvent();
        }
    }

    public void setVisible(StatisticsUI statisticsUI, boolean visible) {
        if (visible) {
            if (invisibleList.remove(statisticsUI)) {
                fireChangeEvent();
            }
        } else if (!invisibleList.contains(statisticsUI)) {
            invisibleList.add(statisticsUI);
            fireChangeEvent();
        }
    }

    public void addChangeListener(ChangeListener changeListener) {
        if (!listeners.contains(changeListener)) {
            listeners.add(changeListener);
        }
    }

    public void removeChangeListener(ChangeListener changeListener) {
        listeners.remove(changeListener);
    }

    public void fireChangeEvent() {
        ChangeEvent evt = new ChangeEvent(this);
        for (ChangeListener listener : listeners) {
            listener.stateChanged(evt);
        }
    }
}
