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
package org.gephi.ui.statistics.plugin.dynamic;

import javax.swing.JPanel;
import org.gephi.statistics.plugin.dynamic.DynamicClusteringCoefficient;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = StatisticsUI.class)
public class DynamicClusteringCoefficientUI implements StatisticsUI {

    private final StatSettings settings = new StatSettings();
    private DynamicClusteringCoefficient clusetingCoefficient;
    private DynamicClusteringCoefficientPanel panel;

    public JPanel getSettingsPanel() {
        panel = new DynamicClusteringCoefficientPanel();
        return panel;
    }

    public void setup(Statistics statistics) {
        this.clusetingCoefficient = (DynamicClusteringCoefficient) statistics;
        if (panel != null) {
            settings.load(clusetingCoefficient);
            panel.setDirected(clusetingCoefficient.isDirected());
            panel.setAverageOnly(clusetingCoefficient.isAverageOnly());
        }
    }

    public void unsetup() {
        if (panel != null) {
            clusetingCoefficient.setDirected(panel.isDirected());
            clusetingCoefficient.setAverageOnly(panel.isAverageOnly());
            settings.save(clusetingCoefficient);
        }
        clusetingCoefficient = null;
    }

    public Class<? extends Statistics> getStatisticsClass() {
        return DynamicClusteringCoefficient.class;
    }

    public String getValue() {
        return "";
    }

    public String getDisplayName() {
        return NbBundle.getMessage(getClass(), "DynamicClusteringCoefficientUI.name");
    }

    public String getCategory() {
        return StatisticsUI.CATEGORY_DYNAMIC;
    }

    public int getPosition() {
        return 400;
    }

    private static class StatSettings {

        private boolean averageOnly = false;
        private double window = 0.0;
        private double tick = 0.0;

        private void save(DynamicClusteringCoefficient stat) {
            this.averageOnly = stat.isAverageOnly();
            this.window = stat.getWindow();
            this.tick = stat.getTick();
        }

        private void load(DynamicClusteringCoefficient stat) {
            stat.setAverageOnly(averageOnly);
            stat.setWindow(window);
            stat.setTick(tick);
        }
    }
}
