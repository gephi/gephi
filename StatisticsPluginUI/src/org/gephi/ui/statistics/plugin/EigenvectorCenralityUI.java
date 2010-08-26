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
import org.gephi.statistics.plugin.EigenvectorCentrality;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author pjmcswee
 */
@ServiceProvider(service = StatisticsUI.class)
public class EigenvectorCenralityUI implements StatisticsUI {

    private EigenvectorCentralityPanel panel;
    private EigenvectorCentrality eigen;

    public JPanel getSettingsPanel() {
        panel = new EigenvectorCentralityPanel();
        return panel;
    }

    public void setup(Statistics statistics) {
        this.eigen = (EigenvectorCentrality) statistics;
        if (panel != null) {
            panel.setNumRuns(eigen.getNumRuns());
            panel.setDirected(eigen.isDirected());
        }
    }

    public void unsetup() {
        panel = null;
        eigen = null;
    }

    public Class<? extends Statistics> getStatisticsClass() {
        return EigenvectorCentrality.class;
    }

    public String getValue() {
        return null;
    }

    public String getDisplayName() {
        return "Eigenvector Centrality ";
    }

    public String getCategory() {
        return StatisticsUI.CATEGORY_NODE_OVERVIEW;
    }

    public int getPosition() {
        return 1000;
    }

}
