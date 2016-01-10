/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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
package org.gephi.ui.statistics.plugin.dynamic;

import javax.swing.JPanel;
import org.gephi.statistics.plugin.dynamic.DynamicDegree;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = StatisticsUI.class)
public class DynamicDegreeUI implements StatisticsUI {

    private final StatSettings settings = new StatSettings();
    private DynamicDegree degree;
    private DynamicDegreePanel panel;

    @Override
    public JPanel getSettingsPanel() {
        panel = new DynamicDegreePanel();
        return panel;
    }

    @Override
    public void setup(Statistics statistics) {
        this.degree = (DynamicDegree) statistics;
        if (panel != null) {
            settings.load(degree);
            panel.setDirected(degree.isDirected());
            panel.setAverageOnly(degree.isAverageOnly());
        }
    }

    @Override
    public void unsetup() {
        if (panel != null) {
            degree.setDirected(panel.isDirected());
            degree.setAverageOnly(panel.isAverageOnly());
            settings.save(degree);
        }
        degree = null;
        panel = null;
    }

    @Override
    public Class<? extends Statistics> getStatisticsClass() {
        return DynamicDegree.class;
    }

    @Override
    public String getValue() {
        return "";
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(getClass(), "DynamicDegreeUI.name");
    }

    @Override
    public String getCategory() {
        return StatisticsUI.CATEGORY_DYNAMIC;
    }

    @Override
    public int getPosition() {
        return 300;
    }

    @Override
    public String getShortDescription() {
        return NbBundle.getMessage(getClass(), "DynamicDegreeUI.shortDescription");
    }

    private static class StatSettings {

        private boolean averageOnly = false;
        private double window = 0.0;
        private double tick = 0.0;

        private void save(DynamicDegree stat) {
            this.averageOnly = stat.isAverageOnly();
            this.window = stat.getWindow();
            this.tick = stat.getTick();
        }

        private void load(DynamicDegree stat) {
            stat.setAverageOnly(averageOnly);
            stat.setWindow(window);
            stat.setTick(tick);
        }
    }
}
