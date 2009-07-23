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

import org.gephi.statistics.ClusteringCoefficient;
import org.gephi.statistics.api.Statistics;
import org.gephi.statistics.api.StatisticsBuilder;
import org.gephi.statistics.ui.ClusteringCoefficientPanel.ClusteringCoefficientUI;
import org.gephi.statistics.ui.api.StatisticsUI;
import org.openide.util.Lookup;

/**
 *
 * @author pjmcswee
 */
public class ClusteringCoefficientBuilder implements StatisticsBuilder {

    ClusteringCoefficient cc = new ClusteringCoefficient();
    ClusteringCoefficientUI ccui = new ClusteringCoefficientUI();

    /**
     * 
     * @return
     */
    public String toString() {
        return cc.getName();
    }

    /**
     *
     * @return
     */
    public Statistics getStatistics() {
        return cc;//Statistics) Lookup.getDefault().lookupAll(ClusteringCoefficient.class);
    }

    /**
     *
     * @return
     */
    public StatisticsUI getUI() {
        return ccui; //return (StatisticsUI) Lookup.getDefault().lookupAll(ClusteringCoefficientPanel.class);
    }
}
