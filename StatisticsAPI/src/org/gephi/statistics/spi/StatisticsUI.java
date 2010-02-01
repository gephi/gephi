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
 * Statistics and Metrics UI integration information. Implement this interface
 * for defining a new metric in the user interface.
 * <p>
 * One could define multiple <code>StatisticsUI</code> that relies on a single
 * algorithm. StatisticsUIs therefore exist in the system alone, and wait for
 * <code>setup()</code> method to be called to turn on with a compatible
 * Statistics instance.
 * <p>
 * Implementors must add <b>@ServiceProvider</b> annotation to be found by the system.
 * @author Patrick J. McSweeney
 * @see StatisticsBuilder
 */
public interface StatisticsUI {

    public static final String CATEGORY_NETWORK_OVERVIEW = NbBundle.getMessage(StatisticsUI.class, "StatisticsUI.category.networkOverview");
    public static final String CATEGORY_NODE_OVERVIEW = NbBundle.getMessage(StatisticsUI.class, "StatisticsUI.category.nodeOverview");
    public static final String CATEGORY_EDGE_OVERVIEW = NbBundle.getMessage(StatisticsUI.class, "StatisticsUI.category.edgeOverview");

    /**
     * Returns a settings panel instance.
     * @return              a settings panel instance
     */
    public JPanel getSettingsPanel();

    /**
     * Push a statistics instance to the UI to load its settings. Note that this
     * method is always called after <code>getSettingsPanel</code> and before the
     * panel is displayed.
     * @param statistics    the statistics instance that is linked to the UI
     */
    public void setup(Statistics statistics);

    /**
     * Notify the settings panel has been closed and that the settings values
     * can be saved to the statistics instance.
     */
    public void unsetup();

    /**
     * Returns the statistics' class this UI belongs to.
     * @return              the statistics' class this UI belongs to
     */
    public Class<? extends Statistics> getStatisticsClass();

    /**
     * Returns this statistics result as a String, if exists
     * @return              this statistics' result string
     */
    public String getValue();

    /**
     * Returns this statistics display name
     * @return              this statistics' display name.
     */
    public String getDisplayName();

    /**
     * Returns the category of this metric. Default category can be used, see
     * <ul>
     * <li>{@link StatisticsUI#CATEGORY_NETWORK_OVERVIEW}</li>
     * <li>{@link StatisticsUI#CATEGORY_NODE_OVERVIEW}</li>
     * <li>{@link StatisticsUI#CATEGORY_EDGE_OVERVIEW}</li></ul>
     * Returns a custom String for defining a new category.
     * @return              this statistics' category
     */
    public String getCategory();

    /**
     * Returns a position value, around 1 and 1000, that indicates the position
     * of the Statistics in the UI. Less means upper.
     * @return              this statistics' position value
     */
    public int getPosition();
}

