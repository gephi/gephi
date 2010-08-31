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
package org.gephi.statistics.spi;

import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.graph.api.GraphModel;

/**
 * Define a Statistics/Metrics execution task, that performs analysis and write results
 * as new attribute columns and/or HTML report.
 *
 * @author Patrick J. McSweeney, Mathieu Bastian
 * @see StatisticsBuilder
 */
public interface Statistics {

    /**
     * Executes the statistics algorithm.
     * <p>
     * It is preferable to work on <b>visible</b> graphs, to be synchronized with the
     * visualization.
     * @param graphModel The graph topology
     * @param attributeModel The elements attributes, and where to write table results
     */
    public void execute(GraphModel graphModel, AttributeModel attributeModel);

    /**
     * Returns an HTML string that displays the statistics result. Can contains
     * complex HTML snippets and images.
     * @return An HTML string that displays the results for this Statistics
     */
    public String getReport();
}
