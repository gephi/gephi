/*
Copyright 2008 WebAtlas
Authors : Patrick J. McSweeney (pjmcswee@syr.edu)
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
package org.gephi.statistics.spi;

import javax.swing.JPanel;
import org.openide.util.NbBundle;

/**
 *
 * @author Patrick J. McSweeney
 */
public interface StatisticsUI {

    public static final String CATEGORY_NETWORK_OVERVIEW = NbBundle.getMessage(StatisticsUI.class, "StatisticsUI.category.networkOverview");
    public static final String CATEGORY_NODE_OVERVIEW = NbBundle.getMessage(StatisticsUI.class, "StatisticsUI.category.nodeOverview");
    public static final String CATEGORY_EDGE_OVERVIEW = NbBundle.getMessage(StatisticsUI.class, "StatisticsUI.category.edgeOverview");

    public JPanel getSettingsPanel();

    public void setup(Statistics statistics);

    public void unsetup();

    public Class<? extends Statistics> getStatisticsClass();

    public String getValue();

    /**
     * @return the display name.
     */
    public String getDisplayName();

    /**
     * @return The category of this metric.
     */
    public String getCategory();

    public int getPosition();
}

