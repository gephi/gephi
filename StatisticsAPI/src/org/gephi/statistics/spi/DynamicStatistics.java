/*
Copyright 2008-2011 Gephi
Authors : Cezary Bartosiak
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
package org.gephi.statistics.spi;

import org.gephi.data.attributes.api.Estimator;
import org.gephi.data.attributes.type.TimeInterval;

/**
 * An interface for dynamic statistics.
 * 
 * @author Cezary Bartosiak
 */
interface DynamicStatistics extends Statistics {

    /**
     * Sets a time interval for which the statistics is executed.
     *
     * @param timeInterval the {@code TimeInterval} to set
     */
    public void setTimeInterval(TimeInterval timeInterval);

    /**
     * Sets the window - duration of one slice. It can be for instance
     * one year, one month etc.
     *
     * @param window duration of one slice
     *
     * @throws IllegalArgumentException if {@code window} <= {@code 0} or
     *                                  {@code window} > {@code high} - {@code low}.
     */
    public void setWindow(double window);

    /**
     * Sets the {@code Estimator} used to estimate snapshot graphs.
     *
     * @param estimator the {@code Estimator} used to estimate snapshot graphs
     *
     * @throws IllegalArgumentException if the given {@code Estimator} is other than
     *                                  {@code MEDIAN}, {@code MODE}, {@code FIRST} or
     *                                  {@code LAST}.
     */
    public void setEstimator(Estimator estimator);

    /**
     * Returns the time interval for which the statistics is executed.
     *
     * @return the time interval for which the statistics is executed.
     */
    public TimeInterval getTimeInterval();

    /**
     * Returns the duration of one slice.
     *
     * @return the duration of one slice.
     */
    public double getWindow();

    /**
     * Returns the {@code Estimator} used to estimate snapshot graphs.
     *
     * @return the {@code Estimator} used to estimate snapshot graphs.
     */
    public Estimator getEstimator();
}
