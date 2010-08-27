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
import org.gephi.statistics.plugin.Modularity;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = StatisticsUI.class)
public class ModularityUI implements StatisticsUI {

    private final StatSettings settings = new StatSettings();
    private ModularityPanel panel;
    private Modularity mod;

    public JPanel getSettingsPanel() {
        panel = new ModularityPanel();
        return panel;
    }

    public void setup(Statistics statistics) {
        this.mod = (Modularity) statistics;
        if (panel != null) {
            settings.load(mod);
            panel.setRandomize(mod.getRandom());
        }
    }

    public void unsetup() {
        if (panel != null) {
            mod.setRandom(panel.isRandomize());
            settings.save(mod);
        }
        mod = null;
        panel = null;
    }

    public Class<? extends Statistics> getStatisticsClass() {
        return Modularity.class;
    }

    public String getValue() {
        DecimalFormat df = new DecimalFormat("###.###");
        return "" + df.format(mod.getModularity());
    }

    public String getDisplayName() {
        return "Modularity";
    }

    public String getCategory() {
        return StatisticsUI.CATEGORY_NETWORK_OVERVIEW;
    }

    public int getPosition() {
        return 600;
    }

    private static class StatSettings {

        private boolean randomize = true;

        private void save(Modularity stat) {
            this.randomize = stat.getRandom();
        }

        private void load(Modularity stat) {
            stat.setRandom(randomize);
        }
    }
}
