/*
Copyright 2008 WebAtlas
Authors : Patrick J. McSweeney
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

import org.gephi.statistics.GraphDistance;
import org.gephi.statistics.api.Statistics;
import org.gephi.statistics.api.StatisticsBuilder;
import org.gephi.statistics.ui.GraphDistancePanel;
import org.gephi.statistics.ui.GraphDistancePanel.GraphDistanceUI;
import org.gephi.statistics.ui.api.StatisticsUI;
import org.openide.util.Lookup;

/**
 *
 * @author pjmcswee
 */
public class GraphDistanceBuilder implements StatisticsBuilder {

    GraphDistance gd = new GraphDistance();
    GraphDistanceUI gdui = new GraphDistanceUI();

    /**
     *
     * @return
     */
    public String toString() {
        return gd.getName();
    }

    /**
     *
     * @return
     */
    public Statistics getStatistics() {
        return gd;//return (Statistics) Lookup.getDefault().lookupAll(GraphDistance.class);
    }

    /**
     *
     * @return
     */
    public StatisticsUI getUI() {
        return gdui;// return (StatisticsUI) Lookup.getDefault().lookupAll(GraphDistancePanel.class);
    }
}
