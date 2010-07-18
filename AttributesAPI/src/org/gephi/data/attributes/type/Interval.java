/*
Copyright 2008-2010 Gephi
Authors : Cezary Bartosiak
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
package org.gephi.data.attributes.type;

/**
 * This class represents an interval with some value.
 *
 * @author Cezary Bartosiak
 * 
 * @param <T> type of data
 */
public final class Interval<T> implements Comparable<Interval<T>> {
	private double low;   // the left endpoint
	private double high;  // the right endpoint
	private T      value; // the value stored in this interval

	/**
	 * Constructs a new interval instance.
	 *
	 * <p>Note that {@code value} cannot be null if you want use this
	 * {@code interval} as a value storage. If it is null some estimators
	 * could not work and generate exceptions.
	 *
	 * @param low   the left endpoint
	 * @param high  the right endpoint
	 * @param value the value stored in this interval
	 *
	 * @throws IllegalArgumentException if {@code low} > {@code high}.
	 */
	public Interval(double low, double high, T value) {
		if (low > high)
			throw new IllegalArgumentException(
						"The left endpoint of the interval must be less than " +
						"the right endpoint.");

		this.low   = low;
		this.high  = high;
		this.value = value;
	}

	/**
	 * Constructs a new interval instance with no value.
	 *
	 * @param low  the left endpoint
	 * @param high the right endpoint
	 *
	 * @throws IllegalArgumentException if {@code low} > {@code high}.
	 */
	public Interval(double low, double high) {
		if (low > high)
			throw new IllegalArgumentException(
						"The left endpoint of the interval must be less than " +
						"the right endpoint.");

		this.low   = low;
		this.high  = high;
		this.value = null;
	}

	/**
	 * Compares this interval with the specified interval for order.
	 *
	 * <p>Any two intervals <i>i</i> and <i>i'</i> satisfy the {@code interval
	 * trichotomy}; that is, exactly one of the following three properties
	 * holds:
	 * <ol>
	 * <li>
	 * <i>i</i> and <i>i'</i> overlap;
	 *
	 * <li>
	 * <i>i</i> is to the left of <i>i'</i> (<i>i.high < i'.low</i>);
	 *
	 * <li>
	 * <i>i</i> is to the right of <i>i'</i> (<i>i'.high < i.low</i>).
	 * </ol>
	 * 
	 * <p>Note that if two intervals are equal ({@code i.low = i'.low} and
	 * {@code i.high = i'.high}), they overlap as well. But if they simply
	 * overlap (for instance {@code i.low < i'.low} and {@code i.high >
	 * i'.high}) they aren't equal.
	 * 
	 * @param interval the interval to be compared
	 * 
	 * @return a negative integer, zero, or a positive integer as this interval
	 *         is to the left of, overlaps with, or is to the right of the
	 *         specified interval.
	 *
	 * @throws NullPointerException if {@code interval} is null.
	 */
	public int compareTo(Interval<T> interval) {
		if (interval == null)
			throw new NullPointerException("Interval cannot be null.");

		if (high < interval.low)
			return -1;
		if (interval.high < low)
			return 1;
		return 0;
	}

	/**
	 * Returns the left endpoint.
	 *
	 * @return the left endpoint.
	 */
	public double getLow() {
		return low;
	}

	/**
	 * Returns the right endpoint.
	 * 
	 * @return the right endpoint.
	 */
	public double getHigh() {
		return high;
	}

	/**
	 * Returns the value stored in this interval.
	 *
	 * @return the value stored in this interval.
	 */
	public T getValue() {
		return value;
	}

	/**
	 * Sets the left endpoint.
	 *
	 * @param low the left endpoint
	 */
	public void setLow(double low) {
		this.low = low;
	}

	/**
	 * Sets the right endpoint.
	 *
	 * @param high the right endpoint
	 */
	public void setHigh(double high) {
		this.high = high;
	}

	/**
	 * Sets the value stored in this interval.
	 *
	 * @param value an object of type {@code T} to be stored in
	 */
	public void setValue(T value) {
		this.value = value;
	}

	/**
	 * Compares this interval with the specified object for equality.
	 * 
	 * <p>Note that two intervals are equal if {@code i.low = i'.low} and
	 * {@code i.high = i'.high}.
	 *
	 * @param obj object to which this interval is to be compared
	 *
	 * @return {@code true} if and only if the specified {@code Object} is a
	 *         {@code Interval} whose low and high are equal to this
     *         {@code Interval's}.
	 *
	 * @see #compareTo(org.gephi.data.attributes.type.Interval)
     * @see #hashCode
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj.getClass().equals(this.getClass())) {
			Interval<T> interval = (Interval<T>)obj;
			if (low == interval.low && high == interval.high)
				return true;
		}
		return false;
	}

	/**
	 * Return a hashcode of this interval.
	 * 
	 * @return a hashcode of this interval.
	 */
	@Override
	public int hashCode() {
		return (int)(Double.doubleToLongBits(low) ^
					 Double.doubleToLongBits(high));
	}

	/**
	 * Returns a string representation of this interval in a format
	 * {@code [low, high, value]}.
	 * 
	 * @return a string representation of this interval.
	 */
	@Override
	public String toString() {
		return "[" + low + ", " + high + ", " + value + "]";
	}
}
