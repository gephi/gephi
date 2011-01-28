/*
 * Copyright 2008-2010 Gephi
 * Authors : Cezary Bartosiak
 * Website : http://www.gephi.org
 *
 * This file is part of Gephi.
 *
 * Gephi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Gephi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.dynamic;

import java.util.TreeMap;
import org.gephi.data.attributes.type.Interval;
import org.gephi.dynamic.api.DynamicModelEvent;

/**
 *
 * @author Mathieu Bastian
 */
public class DynamicIndex {

    protected final TreeMap<Double, Integer> lowMap;
    protected final TreeMap<Double, Integer> highMap;
    protected final DynamicModelImpl model;

    public DynamicIndex(DynamicModelImpl model) {
        this.model = model;
        lowMap = new TreeMap<Double, Integer>();
        highMap = new TreeMap<Double, Integer>();
    }

    public synchronized void add(Interval interval) {
        Double low = interval.getLow();
        Double high = interval.getHigh();
        if (!Double.isInfinite(low)) {
            if (lowMap.get(low) != null) {
                Integer counter = new Integer(lowMap.get(low) + 1);
                lowMap.put(low, counter);
            } else {
                Double min = lowMap.isEmpty() ? Double.POSITIVE_INFINITY : lowMap.firstKey();
                lowMap.put(low, 1);
                if (low < min) {
                    fireEvent(new DynamicModelEvent(DynamicModelEvent.EventType.MIN_CHANGED, model, low));
                }
            }
        }
        if (!Double.isInfinite(high)) {
            if (highMap.get(high) != null) {
                Integer counter = new Integer(highMap.get(high) + 1);
                highMap.put(high, counter);
            } else {
                Double max = highMap.isEmpty() ? Double.NEGATIVE_INFINITY : highMap.lastKey();
                highMap.put(high, 1);
                if (high > max) {
                    fireEvent(new DynamicModelEvent(DynamicModelEvent.EventType.MAX_CHANGED, model, high));
                }
            }
        }
    }

    public synchronized void remove(Interval interval) {
        Double low = interval.getLow();
        Double high = interval.getHigh();
        if (!Double.isInfinite(low) && lowMap.get(low) != null) {
            Integer counter = new Integer(lowMap.get(low) - 1);
            if (counter == 0) {
                Double min = lowMap.firstKey();
                lowMap.remove(low);
                if (min.equals(low)) {
                    fireEvent(new DynamicModelEvent(DynamicModelEvent.EventType.MIN_CHANGED, model, getMin()));
                }
            } else {
                lowMap.put(low, counter);
            }
        }
        if (!Double.isInfinite(high) && highMap.get(high) != null) {
            Integer counter = new Integer(highMap.get(high) - 1);
            if (counter == 0) {
                Double max = highMap.lastKey();
                highMap.remove(high);
                if (max.equals(high)) {
                    fireEvent(new DynamicModelEvent(DynamicModelEvent.EventType.MAX_CHANGED, model, getMax()));
                }
            } else {
                highMap.put(high, counter);
            }
        }
    }

    public synchronized void clear() {
        lowMap.clear();
        highMap.clear();
    }

    public synchronized double getMin() {
        return lowMap.isEmpty() ? (highMap.isEmpty() ? Double.NEGATIVE_INFINITY : highMap.firstKey()) : lowMap.firstKey();
    }

    public synchronized double getMax() {
        return highMap.isEmpty() ? (lowMap.isEmpty() ? Double.POSITIVE_INFINITY : lowMap.lastKey()) : highMap.lastKey();
    }

    private void fireEvent(DynamicModelEvent event) {
        if (model != null) {
            model.controller.fireModelEvent(event);
        }
    }
}
