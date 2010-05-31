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
package org.gephi.data.attributes.type;

import java.util.ArrayList;
import java.util.List;
import org.gephi.data.attributes.api.Estimator;

/**
 * A special type which provides methods of getting/setting values of any time
 * interval. It is internally implemented using Interval Tree for efficiency.
 *
 * @author Cezary Bartosiak
 * 
 * @param <T> type of data
 */
public abstract class DynamicType<T> {
	protected IntervalTree<T> intervalTree;

	/**
	 * Constructs a new {@code DynamicType} instance.
	 */
	public DynamicType() {
		intervalTree = new IntervalTree<T>();
	}

	/**
	 * Returns the leftmost point.
	 *
	 * @return the leftmost point.
	 */
	public double getLow() {
		try {
			return intervalTree.minimum().getLow();
		}
		catch (Exception e) {
			return Double.POSITIVE_INFINITY;
		}
	}

	/**
	 * Returns the rightmost point.
	 *
	 * @return the rightmost point.
	 */
	public double getHigh() {
		try {
			return intervalTree.maximum().getHigh();
		}
		catch (Exception e) {
			return Double.NEGATIVE_INFINITY;
		}
	}

	/**
	 * Indicates if this instance is included in a [{@code low}, {@code high}]
	 * time interval.
	 *
	 * @param low  the left endpoint
	 * @param high the right endpoint
	 *
	 * @return {@code true} if and only if this instance.low >= low and
	 *         this instance.right <= high, otherwise {@code false}.
	 * 
	 * @throws IllegalArgumentException if {@code low} > {@code high}.
	 */
	public boolean isInRange(double low, double high) {
		if (low > high)
			throw new IllegalArgumentException(
						"The left endpoint of the interval must be less than " +
						"the right endpoint.");
		
		return getLow() >= low && getHigh() <= high;
	}

	/**
	 * Removes all intervals.
	 */
	public void clearValues() {
		clearValues(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
	}

	/**
	 * Removes all intervals overlapping with a [{@code low}, {@code high}]
	 * time interval.
	 *
	 * @param low  the left endpoint
	 * @param high the right endpoint
	 *
	 * @throws IllegalArgumentException if {@code low} > {@code high}.
	 */
	public void clearValues(double low, double high) {
		if (low > high)
			throw new IllegalArgumentException(
						"The left endpoint of the interval must be less than " +
						"the right endpoint.");

		intervalTree.delete(new Interval<T>(low, high, null));
	}

	/**
	 * Returns the estimated value of a set of values whose time intervals
	 * overlap with a [{@code -inf}, {@code inf}] time interval.
	 * {@code Estimator.FIRST} is used.
	 *
	 * @return the estimated value of a set of values whose time intervals
	 *         overlap with a [{@code -inf}, {@code inf}] time interval or
	 *         {@code null} if there are no intervals.
	 * 
	 * @see Estimator
	 */
	public T getValue() {
		return getValue(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
	}

	/**
	 * Returns the estimated value of a set of values whose time intervals
	 * overlap with a [{@code low}, {@code high}] time interval.
	 * {@code Estimator.FIRST} is used.
	 *
	 * @param low  the left endpoint
	 * @param high the right endpoint
	 *
	 * @return the estimated value of a set of values whose time intervals
	 *         overlap with a [{@code low}, {@code high}] time interval or
	 *         {@code null} if there are no intervals.
	 *
	 * @throws IllegalArgumentException if {@code low} > {@code high}.
	 * 
	 * @see Estimator
	 */
	public T getValue(double low, double high) {
		return getValue(low, high, Estimator.FIRST);
	}

	/**
	 * Returns the estimated value of a set of values whose time intervals
	 * overlap with a [{@code -inf}, {@code inf}] time interval.
	 *
	 * @param estimator used to estimate the result
	 *
	 * @return the estimated value of a set of values whose time intervals
	 *         overlap with a [{@code -inf}, {@code inf}] time interval or
	 *         {@code null} if there are no intervals.
	 *
	 * @throws UnsupportedOperationException if type {@code T} doesn't support
	 *                                       the given {@code estimator}.
	 *
	 * @see Estimator
	 */
	public T getValue(Estimator estimator) {
		return getValue(Double.NEGATIVE_INFINITY,
						Double.POSITIVE_INFINITY,
						estimator);
	}

	/**
	 * Returns the estimated value of a set of values whose time intervals
	 * overlap with a [{@code low}, {@code high}] time interval.
	 *
	 * @param low       the left endpoint
	 * @param high      the right endpoint
	 * @param estimator used to estimate the result
	 *
	 * @return the estimated value of a set of values whose time intervals
	 *         overlap with a [{@code low}, {@code high}] time interval or
	 *         {@code null} if there are no intervals.
	 * 
	 * @throws IllegalArgumentException      if {@code low} > {@code high}.
	 * @throws UnsupportedOperationException if type {@code T} doesn't support
	 *                                       the given {@code estimator}.
	 * 
	 * @see Estimator
	 */
	public abstract T getValue(double low, double high, Estimator estimator);

	/**
	 * Returns a list of all values stored in this instance.
	 *
	 * @return a list of all values stored in this instance.
	 */
	public List<T> getValues() {
		return getValues(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
	}

	/**
	 * Returns a list of values whose time intervals overlap with a
	 * [{@code low}, {@code high}] time interval.
	 *
	 * @param low   the left endpoint
	 * @param high  the right endpoint
	 * 
	 * @return a list of values whose time intervals overlap with a
	 *         [{@code low}, {@code high}] time interval.
	 *
	 * @throws IllegalArgumentException if {@code low} > {@code high}.
	 */
	public List<T> getValues(double low, double high) {
		if (low > high)
			throw new IllegalArgumentException(
						"The left endpoint of the interval must be less than " +
						"the right endpoint.");

		List<T> result = new ArrayList<T>();
		Interval<T> interval = new Interval<T>(low, high, null);
		for (Interval<T> i : intervalTree.search(interval))
			result.add(i.getValue());
		return result;
	}

	/**
	 * Adds a new interval [{@code -inf}, {@code inf}] with some {@code value}.
	 */
	public void setValue(T value) {
		setValue(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, value);
	}

	/**
	 * Adds a new interval [{@code low}, {@code high}] with some {@code value}.
	 *
	 * @param low   the left endpoint
	 * @param high  the right endpoint
	 * @param value an instance of type {@code T}
	 *
	 * @throws IllegalArgumentException if {@code low} > {@code high}.
	 */
	public void setValue(double low, double high, T value) {
		if (low > high)
			throw new IllegalArgumentException(
						"The left endpoint of the interval must be less than " +
						"the right endpoint.");

		intervalTree.insert(new Interval<T>(low, high, value));
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
		if (obj != null && obj.getClass().equals(this.getClass()) &&
				((DynamicType<T>)obj).intervalTree.equals(intervalTree))
			return true;
		return false;
	}

	/**
	 * Returns a hashcode of this instance.
	 *
	 * @return a hashcode of this instance.
	 */
	@Override
	public int hashCode() {
		return super.hashCode() + intervalTree.hashCode();
	}

	/**
	 * Returns a string representation of this instance in a format
	 * {@code [[low, high, value], ..., [low, high, value]]}. Intervals are
	 * ordered by its left endpoint.
	 * 
	 * @return a string representation of this instance.
	 */
	@Override
	public String toString() {
		return intervalTree.toString();
	}
}
