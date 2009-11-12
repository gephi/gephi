/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke, Patrick J. McSweeney
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

import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.graph.api.GraphModel;

/**
 *
 * @author Mathieu Bastian, Patrick J. McSweeney
 */
public interface Statistics {

    /**
     * Executes the Staistics
     * @param graphModel The graph topology
     * @param attributeModel The elements attributes, and where to write table results
     */
    public void execute(GraphModel graphModel, AttributeModel attributeModel);

    /**
     *
     * @return An HTML string that displays the results for this Statistics
     */
    public String getReport();
}
