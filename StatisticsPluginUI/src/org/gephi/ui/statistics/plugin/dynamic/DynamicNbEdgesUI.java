/*
Copyright 2008-2011 Gephi
Authors : Sébastien Heymann <sebastien.heymann@gephi.org>
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
import org.gephi.statistics.plugin.dynamic.DynamicNbEdges;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Sébastien Heymann
 */
@ServiceProvider(service = StatisticsUI.class)
public class DynamicNbEdgesUI implements StatisticsUI {

    private final StatSettings settings = new StatSettings();
    private DynamicNbEdges nbEdges;
    private DynamicNbEdgesPanel panel;

    public JPanel getSettingsPanel() {
        panel = new DynamicNbEdgesPanel();
        return panel;
    }

    public void setup(Statistics statistics) {
        this.nbEdges = (DynamicNbEdges) statistics;
        if (panel != null) {
            settings.load(nbEdges);
        }
    }

    public void unsetup() {
        if (panel != null) {
            settings.save(nbEdges);
        }
        nbEdges = null;
    }

    public Class<? extends Statistics> getStatisticsClass() {
        return DynamicNbEdges.class;
    }

    public String getValue() {
        return "";
    }

    public String getDisplayName() {
        return NbBundle.getMessage(getClass(), "DynamicNbEdgesUI.name");
    }

    public String getCategory() {
        return StatisticsUI.CATEGORY_DYNAMIC;
    }

    public int getPosition() {
        return 200;
    }

    private static class StatSettings {

        private double window = 0.0;
        private double tick = 0.0;

        private void save(DynamicNbEdges stat) {
            this.window = stat.getWindow();
            this.tick = stat.getTick();
        }

        private void load(DynamicNbEdges stat) {
            stat.setWindow(window);
            stat.setTick(tick);
        }
    }
}
