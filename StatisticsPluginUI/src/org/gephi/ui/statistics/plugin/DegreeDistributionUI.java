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
import org.gephi.statistics.plugin.DegreeDistribution;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=StatisticsUI.class)
public class DegreeDistributionUI implements StatisticsUI {

    private DegreeDistributionPanel panel;
    private DegreeDistribution degreeDistribution;

    public JPanel getSettingsPanel() {
        panel = new DegreeDistributionPanel();
        return panel;
    }

    public void setup(Statistics statistics) {
        this.degreeDistribution = (DegreeDistribution) statistics;
        if(panel!=null) {
            panel.setDirected(degreeDistribution.isDirected());
        }
    }

    public void unsetup() {
        degreeDistribution = null;
        panel = null;
    }

    public Class<? extends Statistics> getStatisticsClass() {
        return DegreeDistribution.class;
    }

    public String getValue() {
        DecimalFormat df = new DecimalFormat("###.###");
        return "" + df.format(degreeDistribution.getCombinedPowerLaw());
    }

    public String getDisplayName() {
        return "Degree Power Law";
    }

    public String getCategory() {
        return StatisticsUI.CATEGORY_NODE_OVERVIEW;
    }

    public int getPosition() {
        return 100;
    }
}
