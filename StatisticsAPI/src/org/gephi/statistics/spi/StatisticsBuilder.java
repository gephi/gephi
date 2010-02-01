/*
Copyright 2008 WebAtlas
Authors : Patrick J. McSweeney (pjmcswee@syr.edu)
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
package org.gephi.statistics.spi;

/**
 * Statistics builder defines an statistics and is responsible for building
 * new instances.
 * <p>
 * Builders must add <b>@ServiceProvider</b> annotation to be found by the system.
 * <p>
 * To be fully integrated, one must also also implement {@link StatisticsUI}.
 *
 * @author Patrick J. McSweeney
 */
public interface StatisticsBuilder {

    /**
     * Returns the name of statistics
     * @return  the name of the statistics
     */
    public String getName();

    /**
     * Build a new statistics instance and return it
     * @return  a new statistics instance
     */
    public Statistics getStatistics();

    /**
     * Returns the statistics' class this UI belongs to.
     * @return  the statistics' class this UI belongs to
     */
    public Class<? extends Statistics> getStatisticsClass();
}
