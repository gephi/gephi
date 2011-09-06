/*
 * Copyright 2008-2010 Gephi
 * Authors : Cezary Bartosiak
 *           Mathieu Bastian <mathieu.bastian@gephi.org>
 * Website : http://www.gephi.org
 * 
 * This file is part of Gephi.
 *
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
package org.gephi.statistics.spi;

import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.type.Interval;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphView;

/**
 * Define a dynamic statistics implementation. A Dynamic Statistics uses
 * a sliding window on a dynamc network to compute results.
 * <p>
 * The dynamic statistic execution is a three-steps process:
 * <ol><li>The <code>execute()</code> method is called to init the statistic
 * with the graph and attribute model.</li>
 * <li>For every interval the <code>loop()</code> method is called with the
 * network at this interval as parameter.</li>
 * <li>The <code>end()</code> method is finally called.</li></ol>
 * <p>
 * 
 * @author Mathieu Bastian
 */
public interface DynamicStatistics extends Statistics {

    /**
     * First method to be executed in the dynamic statistic process. Initialize
     * the statistics with the graph and attributes. The graph model holds the
     * graph structure and the attribute model the attribute columns.
     * @param graphModel the graph model
     * @param attributeModel the attribute model
     */
    public void execute(GraphModel graphModel, AttributeModel attributeModel);

    /**
     * Iteration of the dynamic statistics algorithm on a new interval. The 
     * graph window is a snapshot of the graph at the current <code>interval</code>.
     * @param window a snapshot of the graph at the current interval
     * @param interval the interval of the current snapshot
     */
    public void loop(GraphView window, Interval interval);

    /**
     * Called at the end of the process after all loops.
     */
    public void end();

    /**
     * Sets the minimum and maximum bound
     * @param bounds the min and max bounds
     */
    public void setBounds(Interval bounds);

    /**
     * Sets the window duration
     * @param window the window duration
     */
    public void setWindow(double window);

    /**
     * Sets the tick. The tick is how much the window is moved to the right 
     * at each iteration.
     * @param tick the tick
     */
    public void setTick(double tick);

    /**
     * Returns the window duration
     * @return the window duration
     */
    public double getWindow();

    /**
     * Returns the tick. The tick is how much the window is moved to the right 
     * at each iteration.
     * @return the tick
     */
    public double getTick();

    /**
     * Returns the min and max bounds.
     * @return the bounds
     */
    public Interval getBounds();
}
