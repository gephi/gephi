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
package org.gephi.statistics.ui;

import javax.swing.JPanel;
import org.gephi.statistics.EdgeCount;
import org.gephi.statistics.api.Statistics;
import org.gephi.statistics.ui.api.StatisticsUI;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Sebastien Heymann
 */
@ServiceProvider(service = StatisticsUI.class)
public class EdgeCountUI implements StatisticsUI {

    private EdgeCount edgeCount;

    public JPanel getSettingsPanel() {
        return null;
    }

    public void setup(Statistics statistics) {
        this.edgeCount = (EdgeCount) statistics;
    }

    public void unsetup() {
    }

    public Class<? extends Statistics> getStatisticsClass() {
        return EdgeCount.class;
    }

    public String getValue() {
        return "" + edgeCount.getEdgeCount();
    }

    public String getDisplayName() {
        return "# Edges";
    }

    public String getCategory() {
        return StatisticsUI.CATEGORY_EDGE_OVERVIEW;
    }

    public int getPosition() {
        return 1;
    }

}
