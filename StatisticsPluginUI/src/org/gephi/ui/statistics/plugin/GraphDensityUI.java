/*
Copyright 2008-2010 Gephi
Authors : Patick J. McSweeney <pjmcswee@syr.edu>
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
import org.gephi.statistics.plugin.GraphDensity;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = StatisticsUI.class)
public class GraphDensityUI implements StatisticsUI {

    /** */
    private GraphDensityPanel panel;
    /** */
    private GraphDensity graphDensity;

    public JPanel getSettingsPanel() {
        panel = new GraphDensityPanel();
        return panel;
    }

    public void setup(Statistics statistics) {
        this.graphDensity = (GraphDensity) statistics;
        if (panel != null) {
            panel.setDirected(graphDensity.getDirected());
        }
    }

    public void unsetup() {
        graphDensity = null;
        panel = null;
    }

    public Class<? extends Statistics> getStatisticsClass() {
        return GraphDensity.class;
    }

    public String getValue() {
        DecimalFormat df = new DecimalFormat("###.###");
        return "" + df.format(graphDensity.getDensity());
    }

    public String getDisplayName() {
        return "Graph Density";
    }

    public String getCategory() {
        return StatisticsUI.CATEGORY_NETWORK_OVERVIEW;
    }

    public int getPosition() {
        return 200;
    }
}
