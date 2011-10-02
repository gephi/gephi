/*
Copyright 2008-2010 Gephi
Authors : Patick J. McSweeney <pjmcswee@syr.edu>,
          Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
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
    public static final String CATEGORY_DYNAMIC = NbBundle.getMessage(StatisticsUI.class, "StatisticsUI.category.dynamic");

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
     * <li>{@link StatisticsUI#CATEGORY_EDGE_OVERVIEW}</li>
     * <li>{@link StatisticsUI#CATEGORY_DYNAMIC}</li></ul>
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

