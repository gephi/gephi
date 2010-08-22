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
package org.gephi.statistics.api;

import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsBuilder;
import java.util.List;
import org.gephi.statistics.spi.StatisticsUI;
import org.gephi.utils.longtask.api.LongTaskListener;

/**
 * Controller for executing Statistics/Metrics algorithms.
 * <p>
 * This controller is a service and can therefore be found in Lookup:
 * <pre>StatisticsController sc = Lookup.getDefault().lookup(StatisticsController.class);</pre>
 * 
 * @author Patrick J. McSweeney, Mathieu Bastian
 * @see StatisticsBuilder
 */
public interface StatisticsController {

    /**
     * Returns the lists of <code>StatisticsBuilder</code> in the system.
     * @return              the builders list
     */
    public List<StatisticsBuilder> getStatistics();

    /**
     * Execute the statistics algorithm. If <code>statistics</code> implements
     * <code>LongTask</code>, execution is performed in a background thread and
     * therefore this method returns immedialtely.
     * @param statistics    the statistics algorithm instance
     * @param listener      a listener that is notified when execution finished
     */
    public void execute(Statistics statistics, LongTaskListener listener);

    /**
     * Finds the builder from the statistics class.
     * @param statistics    the statistics class
     * @return              the builder, or <code>null</code> if not found
     */
    public StatisticsBuilder getBuilder(Class<? extends Statistics> statistics);

    /**
     * Sets the visible state for a given <code>StatisticsUI</code>.
     * @param ui            the UI instance
     * @param visible       <code>true</code> to display the front-end
     */
    public void setStatisticsUIVisible(StatisticsUI ui, boolean visible);

    /**
     * Returns the current <code>StatisticsModel</code>, from the current
     * workspace
     * @return              the current <code>StatisticsModel</code>
     */
    public StatisticsModel getModel();
}
