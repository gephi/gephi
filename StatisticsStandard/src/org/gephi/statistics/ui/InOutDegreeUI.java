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
import org.gephi.statistics.InOutDegree;
import org.gephi.statistics.api.Statistics;
import org.gephi.statistics.ui.api.StatisticsUI;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = StatisticsUI.class)
public class InOutDegreeUI implements StatisticsUI {

    private InOutDegree inOutDegree;

    public JPanel getSettingsPanel() {
        return null;
    }

    public void setup(Statistics statistics) {
        this.inOutDegree = (InOutDegree) statistics;
    }

    public void unsetup() {
    }

    public Class<? extends Statistics> getStatisticsClass() {
        return InOutDegree.class;
    }

    public String getValue() {
        DecimalFormat df = new DecimalFormat("###.###");
        return "" + df.format(inOutDegree.getAverageDegree());
    }

    public String getDisplayName() {
        return "In/Out Degree";
    }

    public String getCategory() {
        return StatisticsUI.CATEGORY_NETWORK_OVERVIEW;
    }

    public int getPosition() {
        return 1;
    }
}
