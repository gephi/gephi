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

import org.gephi.project.api.Workspace;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsBuilder;
import org.gephi.statistics.spi.StatisticsUI;
import org.gephi.utils.longtask.api.LongTaskListener;
import org.gephi.utils.longtask.spi.LongTask;

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
     * Execute the statistics algorithm in a background thread and notify
     * <code>listener</code> when finished. The <code>statistics</code> should
     * implement {@link LongTask}.
     * @param statistics    the statistics algorithm instance
     * @param listener      a listener that is notified when execution finished
     * @throws IllegalArgumentException if <code>statistics</code> doesn't
     * implement {@link LongTask}
     */
    public void execute(Statistics statistics, LongTaskListener listener);

    /**
     * Executes <code>statistics</code> in the current thread.
     * @param statistics    the statistics to execute
     */
    public void execute(Statistics statistics);
    
    /**
     * Finds the builder from the statistics class.
     * @param statistics    the statistics class
     * @return              the builder, or <code>null</code> if not found
     */
    public StatisticsBuilder getBuilder(Class<? extends Statistics> statistics);

    /**
     * Returns the current <code>StatisticsModel</code>, from the current
     * workspace
     * @return              the current <code>StatisticsModel</code>
     */
    public StatisticsModel getModel();
    
    /**
     * Returns the <code>StatisticsModel</code> for <code>workspace</code>
     * @param               workspace the workspace to return the model for
     * @return              the <code>StatisticsModel</code> associated to
     *                      <code>workspace</code>
     */
    public StatisticsModel getModel(Workspace workspace);
}
