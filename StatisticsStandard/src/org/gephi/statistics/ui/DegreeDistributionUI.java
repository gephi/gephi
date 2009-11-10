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
import org.gephi.statistics.DegreeDistribution;
import org.gephi.statistics.api.Statistics;
import org.gephi.statistics.ui.api.StatisticsUI;
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
        //Set params
        degreeDistribution.setDirected(panel.isDirected());
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
