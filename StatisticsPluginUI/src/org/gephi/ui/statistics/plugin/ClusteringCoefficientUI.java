/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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

import java.text.DecimalFormat;
import javax.swing.JPanel;
import org.gephi.statistics.plugin.ClusteringCoefficient;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
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
        }
    }

    public void unsetup() {
        clusteringCoefficient = null;
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
