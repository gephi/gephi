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

import java.math.BigInteger;
import java.util.Hashtable;
import java.util.List;
import org.gephi.data.attributes.api.Estimator;

/**
 * Represents {@link Byte} type which can have got different values in
 * different time intervals.
 *
 * @author Cezary Bartosiak
 */
public final class DynamicByte extends DynamicType<Byte> {
	/**
	 * Constructs a new {@code DynamicType} instance with no intervals.
	 */
	public DynamicByte() {
		super();
	}

	/**
	 * Constructs a new {@code DynamicType} instance that contains a given
	 * {@code Interval<T>} in.
	 * 
	 * @param in interval to add (could be null)
	 */
	public DynamicByte(Interval<Byte> in) {
		super(in);
	}

	/**
	 * Constructs a new {@code DynamicType} instance with intervals given by
	 * {@code List<Interval<T>>} in.
	 * 
	 * @param in intervals to add (could be null)
	 */
	public DynamicByte(List<Interval<Byte>> in) {
		super(in);
	}

	/**
	 * Constructs a shallow copy of {@code source}.
	 *
	 * @param source an object to copy from (could be null, then completely new
	 *               instance is created)
	 */
	public DynamicByte(DynamicByte source) {
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
	public DynamicByte(DynamicByte source, Interval<Byte> in) {
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
	public DynamicByte(DynamicByte source, Interval<Byte> in,
			Interval<Byte> out) {
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
	public DynamicByte(DynamicByte source, List<Interval<Byte>> in) {
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
	public DynamicByte(DynamicByte source, List<Interval<Byte>> in,
			List<Interval<Byte>> out) {
		super(source, in, out);
	}

	@Override
	public Byte getValue(double low, double high, Estimator estimator) {
		if (low > high)
			throw new IllegalArgumentException(
						"The left endpoint of the interval must be less than " +
						"the right endpoint.");

		List<Byte> values = getValues(low, high);
		if (values.isEmpty())
			return null;

		switch (estimator) {
			case AVERAGE:
				BigInteger total = BigInteger.valueOf(0);
				for (int i = 0; i < values.size(); ++i)
					total = total.add(BigInteger.valueOf(values.get(i)));
				return total.divide(BigInteger.valueOf(values.size())).
						byteValue();
			case MEDIAN:
				if (values.size() % 2 == 1)
					return values.get(values.size() / 2);
				BigInteger bi = BigInteger.valueOf(values.get(
								values.size() / 2 - 1));
				bi = bi.add(BigInteger.valueOf(values.get(values.size() / 2)));
				return bi.divide(BigInteger.valueOf(2)).byteValue();
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
				BigInteger sum = BigInteger.valueOf(0);
				for (int i = 0; i < values.size(); ++i)
					sum = sum.add(BigInteger.valueOf(values.get(i)));
				return sum.byteValue();
			case MIN:
				BigInteger minimum = BigInteger.valueOf(values.get(0));
				for (int i = 1; i < values.size(); ++i)
					if (minimum.compareTo(BigInteger.valueOf(
							values.get(i))) > 0)
						minimum = BigInteger.valueOf(values.get(i));
				return minimum.byteValue();
			case MAX:
				BigInteger maximum = BigInteger.valueOf(values.get(0));
				for (int i = 1; i < values.size(); ++i)
					if (maximum.compareTo(BigInteger.valueOf(
							values.get(i))) < 0)
						maximum = BigInteger.valueOf(values.get(i));
				return maximum.byteValue();
			case FIRST:
				return values.get(0);
			case LAST:
				return values.get(values.size() - 1);
			default:
				throw new IllegalArgumentException("Unknown estimator.");
		}
	}
}
