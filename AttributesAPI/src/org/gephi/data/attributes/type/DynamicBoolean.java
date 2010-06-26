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

import java.util.Hashtable;
import java.util.List;
import org.gephi.data.attributes.api.Estimator;

/**
 * Represents {@link Boolean} type which can have got different values in
 * different time intervals.
 * 
 * @author Cezary Bartosiak
 */
public final class DynamicBoolean extends DynamicType<Boolean> {
	/**
	 * Constructs a new {@code DynamicType} instance with no intervals.
	 */
	public DynamicBoolean() {
		super();
	}

	/**
	 * Constructs a new {@code DynamicType} instance that contains a given
	 * {@code Interval<T>} in.
	 * 
	 * @param in interval to add (could be null)
	 */
	public DynamicBoolean(Interval<Boolean> in) {
		super(in);
	}

	/**
	 * Constructs a new {@code DynamicType} instance with intervals given by
	 * {@code List<Interval<T>>} in.
	 * 
	 * @param in intervals to add (could be null)
	 */
	public DynamicBoolean(List<Interval<Boolean>> in) {
		super(in);
	}

	/**
	 * Constructs a shallow copy of {@code source}.
	 *
	 * @param source an object to copy from (could be null, then completely new
	 *               instance is created)
	 */
	public DynamicBoolean(DynamicBoolean source) {
		super(source);
	}

	/**
	 * Constructs a shallow copy of {@code source} that contains a given
	 * {@code Interval<T>} in.
	 *
	 * @param source an object to copy from (could be null, then completely new
	 *               instance is created)
	 * @param in     interval to add (could be null)
	 */
	public DynamicBoolean(DynamicBoolean source, Interval<Boolean> in) {
		super(source, in);
	}

	/**
	 * Constructs a shallow copy of {@code source} that contains a given
	 * {@code Interval<T>} in. Before add it removes from the newly created
	 * object all intervals that overlap with a given {@code Interval<T>} out.
	 *
	 * @param source an object to copy from (could be null, then completely new
	 *               instance is created)
	 * @param in     interval to add (could be null)
	 * @param out    interval to remove (could be null)
	 */
	public DynamicBoolean(DynamicBoolean source, Interval<Boolean> in, Interval<Boolean> out) {
		super(source, in, out);
	}

	/**
	 * Constructs a shallow copy of {@code source} with additional intervals
	 * given by {@code List<Interval<T>>} in.
	 *
	 * @param source an object to copy from (could be null, then completely new
	 *               instance is created)
	 * @param in     intervals to add (could be null)
	 */
	public DynamicBoolean(DynamicBoolean source, List<Interval<Boolean>> in) {
		super(source, in);
	}

	/**
	 * Constructs a shallow copy of {@code source} with additional intervals
	 * given by {@code List<Interval<T>>} in. Before add it removes from the
	 * newly created object all intervals that overlap with intervals given by
	 * {@code List<Interval<T>>} out.
	 *
	 * @param source an object to copy from (could be null, then completely new
	 *               instance is created)
	 * @param in     intervals to add (could be null)
	 * @param out    intervals to remove (could be null)
	 */
	public DynamicBoolean(DynamicBoolean source, List<Interval<Boolean>> in, List<Interval<Boolean>> out) {
		super(source, in, out);
	}

	@Override
	public Boolean getValue(double low, double high, Estimator estimator) {
		if (low > high)
			throw new IllegalArgumentException(
						"The left endpoint of the interval must be less than " +
						"the right endpoint.");

		List<Boolean> values = getValues(low, high);
		if (values.isEmpty())
			return null;

		switch (estimator) {
			case AVERAGE:
				throw new UnsupportedOperationException(
							"Not supported estimator");
			case MEDIAN:
				if (values.size() % 2 == 1)
					return values.get(values.size() / 2);
				return values.get(values.size() / 2 - 1);
			case MODE:
				Hashtable<Integer, Integer> map =
						new Hashtable<Integer, Integer>();
				for (int i = 0; i < values.size(); ++i) {
					int prev = 0;
					if (map.containsKey(values.get(i).hashCode()))
						prev = map.get(values.get(i).hashCode());
					map.put(values.get(i).hashCode(), prev + 1);
				}
				int max   = map.get(values.get(0).hashCode());
				int index = 0;
				for (int i = 1; i < values.size(); ++i)
					if (max < map.get(values.get(i).hashCode())) {
						max   = map.get(values.get(i).hashCode());
						index = i;
					}
				return values.get(index);
			case SUM:
				throw new UnsupportedOperationException(
							"Not supported estimator");
			case MIN:
				Boolean minimum = values.get(0);
				for (int i = 1; i < values.size(); ++i)
					if (minimum.compareTo(values.get(i)) > 0)
						minimum = values.get(i);
				return minimum;
			case MAX:
				Boolean maximum = values.get(0);
				for (int i = 1; i < values.size(); ++i)
					if (maximum.compareTo(values.get(i)) < 0)
						maximum = values.get(i);
				return maximum;
			case FIRST:
				return values.get(0);
			case LAST:
				return values.get(values.size() - 1);
			default:
				throw new IllegalArgumentException("Unknown estimator.");
		}
	}
}
