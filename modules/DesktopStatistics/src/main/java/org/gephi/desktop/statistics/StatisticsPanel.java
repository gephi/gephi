/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>, 
          Patick J. McSweeney <pjmcswee@syr.edu>
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
package org.gephi.desktop.statistics;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import org.gephi.desktop.statistics.api.StatisticsModelUI;
import org.gephi.statistics.spi.StatisticsUI;
import org.gephi.ui.components.JSqueezeBoxPanel;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 * @author Patick J. McSweeney
 */
public class StatisticsPanel extends JPanel {

    //Data
    private StatisticsCategory[] categories;
    private List<UIFrontEnd> frontEnds;
    //UI
    private JSqueezeBoxPanel squeezeBoxPanel;

    public StatisticsPanel() {
        initComponents();
        initCategories();
        initFrontEnds();
    }

    public void refreshModel(StatisticsModelUI model) {
        boolean needRefreshVisible = false;
        for (UIFrontEnd entry : frontEnds) {
            entry.getFrontEnd().refreshModel(model);
            if (model != null) {
                boolean visible = model.isStatisticsUIVisible(entry.getStatisticsUI());
                if (visible != entry.visible) {
                    needRefreshVisible = true;
                    entry.setVisible(visible);
                }
            }
        }
        if (needRefreshVisible) {
            refreshFrontEnd();
        }
    }

    private void refreshFrontEnd() {
        squeezeBoxPanel.cleanPanels();
        for (StatisticsCategory category : categories) {
            //Find uis in this category
            List<UIFrontEnd> uis = new ArrayList<>();
            for (UIFrontEnd uife : frontEnds) {
                if (uife.getCategory().equals(category) && uife.isVisible()) {
                    uis.add(uife);
                }
            }

            if (uis.size() > 0) {
                //Sort it by position
                Collections.sort(uis, new Comparator() {

                    @Override
                    public int compare(Object o1, Object o2) {
                        Integer p1 = ((UIFrontEnd) o1).getStatisticsUI().getPosition();
                        Integer p2 = ((UIFrontEnd) o2).getStatisticsUI().getPosition();
                        return p1.compareTo(p2);
                    }
                });

                MigLayout migLayout = new MigLayout("insets 0");
                migLayout.setColumnConstraints("[grow,fill]");
                migLayout.setRowConstraints("[pref!]");
                JPanel innerPanel = new JPanel(migLayout);

                for (UIFrontEnd sui : uis) {
                    innerPanel.add(sui.frontEnd, "wrap");
                }

                squeezeBoxPanel.addPanel(innerPanel, category.getName());
            }
        }
    }

    private void initFrontEnds() {

        StatisticsUI[] statisticsUIs = Lookup.getDefault().lookupAll(StatisticsUI.class).toArray(new StatisticsUI[0]);
        frontEnds = new ArrayList<>();

        for (StatisticsCategory category : categories) {
            //Find uis in this category
            List<StatisticsUI> uis = new ArrayList<>();
            for (StatisticsUI sui : statisticsUIs) {
                if (sui.getCategory().equals(category.getName())) {
                    uis.add(sui);
                }
            }

            if (uis.size() > 0) {
                //Sort it by position
                Collections.sort(uis, new Comparator() {

                    @Override
                    public int compare(Object o1, Object o2) {
                        Integer p1 = ((StatisticsUI) o1).getPosition();
                        Integer p2 = ((StatisticsUI) o2).getPosition();
                        return p1.compareTo(p2);
                    }
                });

                MigLayout migLayout = new MigLayout("insets 0");
                migLayout.setColumnConstraints("[grow,fill]");
                migLayout.setRowConstraints("[pref!]");
                JPanel innerPanel = new JPanel(migLayout);

                for (StatisticsUI sui : uis) {
                    StatisticsFrontEnd frontEnd = new StatisticsFrontEnd(sui);
                    UIFrontEnd uife = new UIFrontEnd(sui, frontEnd, category);
                    frontEnds.add(uife);
                    innerPanel.add(frontEnd, "wrap");
                }

                squeezeBoxPanel.addPanel(innerPanel, category.getName());
            }
        }
    }

    private void initCategories() {
        Map<String, StatisticsCategory> cats = new LinkedHashMap<>();
        cats.put(StatisticsUI.CATEGORY_NETWORK_OVERVIEW, new StatisticsCategory(StatisticsUI.CATEGORY_NETWORK_OVERVIEW, 100));
        cats.put(StatisticsUI.CATEGORY_NODE_OVERVIEW, new StatisticsCategory(StatisticsUI.CATEGORY_NODE_OVERVIEW, 200));
        cats.put(StatisticsUI.CATEGORY_EDGE_OVERVIEW, new StatisticsCategory(StatisticsUI.CATEGORY_EDGE_OVERVIEW, 300));
        cats.put(StatisticsUI.CATEGORY_DYNAMIC, new StatisticsCategory(StatisticsUI.CATEGORY_DYNAMIC, 400));
        
        int position = 500;
        for (StatisticsUI uis : Lookup.getDefault().lookupAll(StatisticsUI.class)) {
            String category = uis.getCategory();
            if (!cats.containsKey(category)) {
                cats.put(category, new StatisticsCategory(category, position));
                position += 100;
            }
        }

        categories = cats.values().toArray(new StatisticsCategory[0]);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        squeezeBoxPanel = new JSqueezeBoxPanel();
        add(squeezeBoxPanel, BorderLayout.CENTER);
    }

    public StatisticsCategory[] getCategories() {
        return categories;
    }

    private static class UIFrontEnd {

        private StatisticsUI statisticsUI;
        private StatisticsFrontEnd frontEnd;
        private StatisticsCategory category;
        private boolean visible;

        public UIFrontEnd(StatisticsUI statisticsUI, StatisticsFrontEnd frontEnd, StatisticsCategory category) {
            this.statisticsUI = statisticsUI;
            this.frontEnd = frontEnd;
            this.category = category;
            this.visible = true;
        }

        public boolean isVisible() {
            return visible;
        }

        public void setVisible(boolean visible) {
            this.visible = visible;
        }

        public StatisticsFrontEnd getFrontEnd() {
            return frontEnd;
        }

        public StatisticsUI getStatisticsUI() {
            return statisticsUI;
        }

        public StatisticsCategory getCategory() {
            return category;
        }
    }
}
