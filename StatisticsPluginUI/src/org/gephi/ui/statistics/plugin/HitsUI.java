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

import javax.swing.JPanel;
import org.gephi.statistics.plugin.Hits;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = StatisticsUI.class)
public class HitsUI implements StatisticsUI {

    private HitsPanel panel;
    private Hits hits;

    public JPanel getSettingsPanel() {
        panel = new HitsPanel();
        return panel;
    }

    public void setup(Statistics statistics) {
        this.hits = (Hits) statistics;
        if (panel != null) {
            panel.setEpsilon(hits.getEpsilon());
            panel.setDirected(!hits.getUndirected());
        }
    }

    public void unsetup() {
        if (panel != null) {
            hits.setEpsilon(panel.getEpsilon());
            hits.setUndirected(!panel.isDirected());
        }
        panel = null;
        hits = null;
    }

    public Class<? extends Statistics> getStatisticsClass() {
        return Hits.class;
    }

    public String getValue() {
        return null;
    }

    public String getDisplayName() {
        return "HITS";
    }

    public String getCategory() {
        return StatisticsUI.CATEGORY_NETWORK_OVERVIEW;
    }

    public int getPosition() {
        return 500;
    }
}
