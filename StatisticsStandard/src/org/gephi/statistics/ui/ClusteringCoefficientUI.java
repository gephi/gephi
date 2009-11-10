/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
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
package org.gephi.statistics.ui;

import java.text.DecimalFormat;
import javax.swing.JPanel;
import org.gephi.statistics.ClusteringCoefficient;
import org.gephi.statistics.api.Statistics;
import org.gephi.statistics.ui.api.StatisticsUI;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = StatisticsUI.class)
public class ClusteringCoefficientUI implements StatisticsUI {

    private ClusteringCoefficientPanel panel;
    private ClusteringCoefficient clusteringCoefficient;

    public JPanel getSettingsPanel() {
        panel = new ClusteringCoefficientPanel();
        return panel;
    }

    public void setup(Statistics statistics) {
        this.clusteringCoefficient = (ClusteringCoefficient) statistics;
        if (panel != null) {
            panel.setDirected(clusteringCoefficient.isDirected());
            panel.setBruteForce(clusteringCoefficient.isBruteForce());
        }
    }

    public void unsetup() {
        //Set params
        clusteringCoefficient.setDirected(panel.isDirected());
        clusteringCoefficient.setBruteForce(panel.isBruteForce());
        panel = null;
    }

    public Class<? extends Statistics> getStatisticsClass() {
        return ClusteringCoefficient.class;
    }

    public String getValue() {
        DecimalFormat df = new DecimalFormat("###.###");
        return "" + df.format(clusteringCoefficient.getAverageClusteringCoefficient());
    }

    public String getDisplayName() {
        return "Avg. Clustering Coefficient";
    }

    public String getCategory() {
        return StatisticsUI.CATEGORY_NODE_OVERVIEW;
    }

    public int getPosition() {
        return 300;
    }
}
