/*
Copyright 2008 WebAtlas
Authors : Patick J. McSweeney
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
package org.gephi.statistics.component;

import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataListener;
import org.gephi.statistics.api.Statistics;
import org.gephi.statistics.api.StatisticsController;
import org.openide.util.Lookup;

/**
 * 
 * @author pjmcswee
 */
public class StatisticsModel implements ComboBoxModel {

    Statistics selected;

    /**
     * 
     */
    public StatisticsModel() {
        StatisticsController controller = Lookup.getDefault().lookup(StatisticsController.class);
        selected = controller.getStatistics().get(0);
    }

    /**
     *
     * @param anItem
     */
    public void setSelectedItem(Object anItem) {
        selected = ((Statistics) anItem);
    }

    /**
     *
     * @return
     */
    public Object getSelectedItem() {
        return selected;
    }

    /**
     *
     * @return
     */
    public int getSize() {
        StatisticsController controller = Lookup.getDefault().lookup(StatisticsController.class);
        return controller.getStatistics().size();
    }

    /**
     *
     * @param index
     * @return
     */
    public Object getElementAt(int index) {
        StatisticsController controller = Lookup.getDefault().lookup(StatisticsController.class);
        return controller.getStatistics().get(index);
    }

    /**
     *
     * @param l
     */
    public void addListDataListener(ListDataListener l) {
    }

    /**
     *
     * @param l
     */
    public void removeListDataListener(ListDataListener l) {
    }
}
