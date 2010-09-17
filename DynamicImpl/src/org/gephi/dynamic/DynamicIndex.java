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

import java.util.List;
import org.gephi.data.attributes.type.Interval;
import org.gephi.data.attributes.type.IntervalTree;
import org.gephi.dynamic.api.DynamicModelEvent;

/**
 *
 * @author Mathieu Bastian
 */
public class DynamicIndex {

    protected IntervalTree<Integer> intervalTree;
    protected final DynamicModelImpl model;

    public DynamicIndex(DynamicModelImpl model) {
        this.model = model;
        intervalTree = new IntervalTree<Integer>();
    }

    public synchronized void add(Interval interval) {
        Interval<Integer> existingInterval = searchInterval(interval);
        if (existingInterval != null) {
            Integer counter = new Integer(1 + existingInterval.getValue());
            intervalTree.delete(existingInterval);
            existingInterval = new Interval<Integer>(existingInterval.getLow(), existingInterval.getHigh(), existingInterval.isLowExcluded(), existingInterval.isHighExcluded(), counter);
            intervalTree.insert(existingInterval);
        } else {
            double min = intervalTree.getLow();
            double max = intervalTree.getHigh();

            Interval<Integer> intInterval = new Interval<Integer>(interval.getLow(), interval.getHigh(), interval.isLowExcluded(), interval.isHighExcluded(), new Integer(1));
            intervalTree.insert(intInterval);

            if (intInterval.getLow() < min) {
                fireEvent(new DynamicModelEvent(DynamicModelEvent.EventType.MIN_CHANGED, model, intInterval.getLow()));
            }
            if (intInterval.getHigh() > max) {
                fireEvent(new DynamicModelEvent(DynamicModelEvent.EventType.MAX_CHANGED, model, intInterval.getHigh()));
            }
        }
    }

    public synchronized void remove(Interval interval) {
        Interval<Integer> existingInterval = searchInterval(interval);
        if (existingInterval != null) {
            if (existingInterval.getValue().intValue() == 1) {
                double min = intervalTree.getLow();
                double max = intervalTree.getHigh();

                intervalTree.delete(existingInterval);

                if (existingInterval.getLow() == min) {
                    fireEvent(new DynamicModelEvent(DynamicModelEvent.EventType.MIN_CHANGED, model, intervalTree.getLow()));
                }
                if (existingInterval.getHigh() == max) {
                    fireEvent(new DynamicModelEvent(DynamicModelEvent.EventType.MAX_CHANGED, model, intervalTree.getHigh()));
                }
            } else {
                Integer counter = new Integer(existingInterval.getValue() - 1);
                intervalTree.delete(existingInterval);
                existingInterval = new Interval<Integer>(existingInterval.getLow(), existingInterval.getHigh(), existingInterval.isLowExcluded(), existingInterval.isHighExcluded(), counter);
                intervalTree.insert(existingInterval);
            }
        }
    }

    private Interval<Integer> searchInterval(Interval interval) {
        List<Interval<Integer>> list = intervalTree.search(interval);
        if (list.size() == 1 && list.get(0).equals(interval)) {
            return list.get(0);
        } else if (list.size() > 1) {
            for (Interval i : list) {
                if (i.equals(interval)) {
                    return i;
                }
            }
        }
        return null;
    }

    public synchronized void clear() {
        intervalTree = new IntervalTree<Integer>();
    }

    public synchronized double getMin() {
        return intervalTree.minimumNotInfinite();
    }

    public synchronized double getMax() {
        return intervalTree.maximumNotInfinite();
    }

    public List<Interval<Integer>> getIntervals() {
        return intervalTree.search(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    /**
     * Returns a list of intervals which overlap with a given time interval.
     *
     * @param interval a given time interval
     *
     * @return a list of intervals which overlap with a given time interval.
     */
    public List<Interval<Integer>> getIntervals(Interval<Integer> interval) {
        return intervalTree.search(interval);
    }

    /**
     * Returns a list of intervals which overlap with a
     * [{@code low}, {@code high}] time interval.
     *
     * @param low  the left endpoint
     * @param high the right endpoint
     *
     * @return a list of intervals which overlap with a
     *         [{@code low}, {@code high}] time interval.
     *
     * @throws IllegalArgumentException if {@code low} > {@code high}.
     */
    public List<Interval<Integer>> getIntervals(double low, double high) {
        return intervalTree.search(low, high);
    }

    /**
     * Compares this instance with the specified object for equality.
     *
     * <p>Note that two {@code DynamicType} instances are equal if they have got
     * the same type {@code T} and their interval trees are equal.
     *
     * @param obj object to which this instance is to be compared
     *
     * @return {@code true} if and only if the specified {@code Object} is a
     *         {@code DynamicType} which has the same type {@code T} and an
     *         equal interval tree.
     *
     * @see #hashCode
     */
    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj.getClass().equals(this.getClass())
                && ((DynamicIndex) obj).intervalTree.equals(intervalTree)) {
            return true;
        }
        return false;
    }

    /**
     * Returns a hashcode of this instance.
     *
     * @return a hashcode of this instance.
     */
    @Override
    public int hashCode() {
        return intervalTree.hashCode();
    }

    /**
     * Returns a string representation of this instance in a format
     * {@code <[low, high, value], ..., [low, high, value]>}. Intervals are
     * ordered by its left endpoint.
     *
     * @return a string representation of this instance.
     */
    @Override
    public String toString() {
        return intervalTree.toString();
    }

    private void fireEvent(DynamicModelEvent event) {
        if (model != null) {
            model.controller.fireModelEvent(event);
        }
    }
}
