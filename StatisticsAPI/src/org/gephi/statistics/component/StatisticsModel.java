/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
public class StatisticsModel implements ComboBoxModel{
    
    Statistics selected;
    public StatisticsModel()
    {
        StatisticsController controller = Lookup.getDefault().lookup(StatisticsController.class);
        selected =  controller.getStatistics().get(0);
    }

    public void setSelectedItem(Object anItem) {
       selected = (Statistics)anItem;
    }

    public Object getSelectedItem() {
        return selected;
    }

    public int getSize() {
        StatisticsController controller = Lookup.getDefault().lookup(StatisticsController.class);
        return controller.getStatistics().size();
    }

    public Object getElementAt(int index) {
        StatisticsController controller = Lookup.getDefault().lookup(StatisticsController.class);
        return controller.getStatistics().get(index);
    }

    public void addListDataListener(ListDataListener l) {
    }

    public void removeListDataListener(ListDataListener l) {
    }


}
