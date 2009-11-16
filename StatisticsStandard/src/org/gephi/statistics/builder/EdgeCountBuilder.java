/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke, Sebastien Heymann
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
package org.gephi.statistics.builder;

import org.gephi.statistics.EdgeCount;
import org.gephi.statistics.api.Statistics;
import org.gephi.statistics.api.StatisticsBuilder;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * The statistics builder the graph edge count statistics
 * @author Sebastien Heymann
 */
@ServiceProvider(service=StatisticsBuilder.class)
public class EdgeCountBuilder implements StatisticsBuilder {

    public String getName() {
        return NbBundle.getMessage(EdgeCountBuilder.class, "EdgeCount.name");
    }

    public Statistics getStatistics() {
        return new EdgeCount();
    }

    public Class<? extends Statistics> getStatisticsClass() {
        return EdgeCount.class;
    }

}
