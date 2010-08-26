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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final List<StatisticsUI> invisibleList;
    private final List<Statistics> runningList;
    private final Map<StatisticsUI, String> resultMap;
    private final Map<Class, String> reportMap;
    //Listeners
    private final List<ChangeListener> listeners;

    public StatisticsModelImpl() {
        invisibleList = new ArrayList<StatisticsUI>();
        runningList = Collections.synchronizedList(new ArrayList<Statistics>());
        listeners = new ArrayList<ChangeListener>();
        resultMap = new HashMap<StatisticsUI, String>();
        reportMap = new HashMap<Class, String>();
    }

    public void addReport(Statistics statistics) {
        if (!reportMap.containsKey(statistics.getClass())) {
            reportMap.put(statistics.getClass(), statistics.getReport());
            fireChangeEvent();
        }
    }

    public void addResult(StatisticsUI ui) {
        if (!resultMap.containsKey(ui) && ui.getValue() != null) {
            resultMap.put(ui, ui.getValue());
            fireChangeEvent();
        }
    }

    public String getReport(StatisticsUI statisticsUI) {
        return reportMap.get(statisticsUI.getStatisticsClass());
    }

    public String getResult(StatisticsUI statisticsUI) {
        return resultMap.get(statisticsUI);
    }

    public boolean isStatisticsUIVisible(StatisticsUI statisticsUI) {
        return !invisibleList.contains(statisticsUI);
    }

    public boolean isRunning(StatisticsUI statisticsUI) {
        for (Statistics s : runningList.toArray(new Statistics[0])) {
            if (statisticsUI.getStatisticsClass().equals(s.getClass())) {
                return true;
            }
        }
        return false;
    }

    public void setRunning(Statistics statistics, boolean running) {
        if (!running) {
            if (runningList.remove(statistics)) {
                fireChangeEvent();
            }
        } else if (!runningList.contains(statistics)) {
            runningList.add(statistics);
            fireChangeEvent();
        }
    }

    public Statistics getRunning(StatisticsUI statisticsUI) {
        for (Statistics s : runningList.toArray(new Statistics[0])) {
            if (statisticsUI.getStatisticsClass().equals(s)) {
                return s;
            }
        }
        return null;
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
