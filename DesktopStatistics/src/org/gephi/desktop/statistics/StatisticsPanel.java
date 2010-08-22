/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>, 
          Patick J. McSweeney <pjmcswee@syr.edu>
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
import org.gephi.statistics.api.StatisticsModel;
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

    public void refreshModel(StatisticsModel model) {
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
            List<UIFrontEnd> uis = new ArrayList<UIFrontEnd>();
            for (UIFrontEnd uife : frontEnds) {
                if (uife.getCategory().equals(category) && uife.isVisible()) {
                    uis.add(uife);
                }
            }

            if (uis.size() > 0) {
                //Sort it by position
                Collections.sort(uis, new Comparator() {

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
        frontEnds = new ArrayList<UIFrontEnd>();

        for (StatisticsCategory category : categories) {
            //Find uis in this category
            List<StatisticsUI> uis = new ArrayList<StatisticsUI>();
            for (StatisticsUI sui : statisticsUIs) {
                if (sui.getCategory().equals(category.getName())) {
                    uis.add(sui);
                }
            }

            if (uis.size() > 0) {
                //Sort it by position
                Collections.sort(uis, new Comparator() {

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
        Map<String, StatisticsCategory> cats = new LinkedHashMap<String, StatisticsCategory>();
        cats.put(StatisticsUI.CATEGORY_NETWORK_OVERVIEW, new StatisticsCategory(StatisticsUI.CATEGORY_NETWORK_OVERVIEW, 100));
        cats.put(StatisticsUI.CATEGORY_NODE_OVERVIEW, new StatisticsCategory(StatisticsUI.CATEGORY_NODE_OVERVIEW, 200));
        cats.put(StatisticsUI.CATEGORY_EDGE_OVERVIEW, new StatisticsCategory(StatisticsUI.CATEGORY_EDGE_OVERVIEW, 300));

        int position = 400;
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
