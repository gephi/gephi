/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gephi.desktop.timeline;

import java.util.ArrayList;
import java.util.List;
import org.gephi.timeline.spi.TimelineDataProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author jbilcke
 */
@ServiceProvider(service = TimelineDataProvider.class)
public class TimelineDataProviderImpl implements TimelineDataProvider {

    private List<Integer> list;
    public TimelineDataProviderImpl() {
        list = new ArrayList<Integer>();
    }
    public synchronized Comparable getFirst() {
        return list.get(0);
    }

    public synchronized Comparable getLast() {
        return list.get(list.size()-1);
    }

    public synchronized int getLength() {
        return list.size();
    }

    public synchronized int getMaxValue() {
        return 100;
    }

    public synchronized int getRowValue(int row) {
        return list.get(row);
    }

}
