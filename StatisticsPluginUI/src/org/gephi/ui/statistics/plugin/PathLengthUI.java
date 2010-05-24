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
package org.gephi.ui.statistics.plugin;

import java.text.DecimalFormat;
import javax.swing.JPanel;
import org.gephi.statistics.plugin.GraphDistance;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = StatisticsUI.class)
public class PathLengthUI implements StatisticsUI {

    private GraphDistancePanel panel;
    private GraphDistance graphDistance;

    public JPanel getSettingsPanel() {
        panel = new GraphDistancePanel();
        return panel;
    }

    public void setup(Statistics statistics) {
        this.graphDistance = (GraphDistance) statistics;
        if (panel != null) {
            panel.setDirected(graphDistance.isDirected());
            panel.doNormalize(graphDistance.useRelative());
        }
    }

    public void unsetup() {
        //Set params
        graphDistance.setDirected(panel.isDirected());
        graphDistance.setRelative(panel.normalize());
        panel = null;
    }

    public Class<? extends Statistics> getStatisticsClass() {
        return GraphDistance.class;
    }

    public String getValue() {
        DecimalFormat df = new DecimalFormat("###.###");
        return "" + df.format(graphDistance.getPathLength());
    }

    public String getDisplayName() {
        return "Average Path Length";
    }

    public String getCategory() {
        return StatisticsUI.CATEGORY_EDGE_OVERVIEW;
    }

    public int getPosition() {
        return 200;
    }
}
