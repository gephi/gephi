/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.dynamic;

import java.util.List;
import org.gephi.data.attributes.type.Interval;
import org.gephi.data.attributes.type.IntervalTree;

/**
 *
 * @author Mathieu Bastian
 */
public class DynamicIndex {

    protected IntervalTree<Integer> intervalTree;

    /**
     * Constructs a new {@code DynamicType} instance with no intervals.
     */
    public DynamicIndex() {
        intervalTree = new IntervalTree<Integer>();
    }

    public void add(Interval interval) {
        List<Interval<Integer>> intervals = intervalTree.search(interval);
        if (intervals.size() == 1 && intervals.get(0).equals(interval)) {
            Interval<Integer> existingInterval = intervals.get(0);
            Integer counter = new Integer(1 + existingInterval.getValue());
            intervalTree.delete(existingInterval);
            existingInterval = new Interval<Integer>(existingInterval.getLow(), existingInterval.getHigh(), existingInterval.isLowExcluded(), existingInterval.isHighExcluded(), counter);
            intervalTree.insert(existingInterval);
        } else {
            Interval<Integer> intInterval = new Interval<Integer>(interval.getLow(), interval.getHigh(), interval.isLowExcluded(), interval.isHighExcluded(), new Integer(1));
            intervalTree.insert(intInterval);
        }
    }

    public void remove(Interval interval) {
        List<Interval<Integer>> intervals = intervalTree.search(interval);
        if (intervals.size() == 1 && intervals.get(0).equals(interval)) {
            Interval<Integer> existingInterval = intervals.get(0);
            if (existingInterval.getValue().intValue() == 1) {
                intervalTree.delete(existingInterval);
            } else {
                Integer counter = new Integer(1 - existingInterval.getValue());
                intervalTree.delete(existingInterval);
                existingInterval = new Interval<Integer>(existingInterval.getLow(), existingInterval.getHigh(), existingInterval.isLowExcluded(), existingInterval.isHighExcluded(), counter);
                intervalTree.insert(existingInterval);
            }
        }
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
}
