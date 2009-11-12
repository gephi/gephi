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
package org.gephi.statistics.api;

/**
 * The class that is responsible of instancing new Statistics
 * @author pjmcswee
 */
public interface StatisticsBuilder {

    /**
     * @return The name of the Statistics
     */
    public String getName();

    /**
     * @return the statistic from this builder.
     */
    public Statistics getStatistics();

    /**
     * @return the Statistics class this builder create
     */
    public Class<? extends Statistics> getStatisticsClass();
}
