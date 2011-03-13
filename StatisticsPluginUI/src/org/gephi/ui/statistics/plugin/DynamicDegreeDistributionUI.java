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
package org.gephi.ui.statistics.plugin;

import javax.swing.JPanel;
import org.gephi.statistics.plugin.DynamicDegreeDistribution;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
import org.openide.util.lookup.ServiceProvider;

/**
 * UI for dynamic degree distribution.
 *
 * @author Cezary Bartosiak
 */
@ServiceProvider(service = StatisticsUI.class)
public class DynamicDegreeDistributionUI implements StatisticsUI {

    private DynamicDegreeDistributionPanel panel;
    private DynamicDegreeDistribution metric;

    public JPanel getSettingsPanel() {
        panel = new DynamicDegreeDistributionPanel();
        return panel;
    }

    public void setup(Statistics statistics) {
        metric = (DynamicDegreeDistribution) statistics;
        if (panel != null) {
            panel.setDirected(metric.isDirected());
            panel.setTimeInterval(metric.getTimeInterval());
            panel.setWindow(metric.getWindow());
            panel.setEstimator(metric.getEstimator());
        }
    }

    public void unsetup() {
        if (panel != null) {
            metric.setDirected(panel.isDirected());
            metric.setTimeInterval(panel.getTimeInterval());
            metric.setWindow(panel.getWindow());
            metric.setEstimator(panel.getEstimator());
        }
        metric = null;
        panel = null;
    }

    public Class<? extends Statistics> getStatisticsClass() {
        return DynamicDegreeDistribution.class;
    }

    public String getValue() {
        return null;
    }

    public String getDisplayName() {
        return "Dynamic Degree Power Law";
    }

    public String getCategory() {
        return StatisticsUI.CATEGORY_NODE_OVERVIEW;
    }

    public int getPosition() {
        return 101;
    }
}
