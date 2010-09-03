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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import org.gephi.data.attributes.api.Estimator;

/**
 * Complex type for specifying time interval. An, interval is two
 * <code>double</code> with <code>start</code> inferior or equal to
 * <code>end</code>. Thus intervals are inclusive.
 *
 * <p>
 * This type accepts multiple, overlapping intervals.
 *
 * @author Mathieu Bastian, Cezary Bartosiak
 */
//Brute-force implementation
public final class TimeInterval extends DynamicType<Double[]> {
	/**
	 * Constructs a new {@code DynamicType} instance with no intervals.
	 */
	public TimeInterval() {
		super();
	}

	/**
	 * Constructs a new {@code DynamicType} instance that contains a given
	 * {@code interval}.
	 *
	 * @param low   the left endpoint
	 * @param high  the right endpoint
	 * @param lopen indicates if the left endpoint is excluded (true in this case)
	 * @param ropen indicates if the right endpoint is excluded (true in this case)
	 */
	public TimeInterval(double low, double high, boolean lopen, boolean ropen) {
		super(new Interval<Double[]>(low, high, lopen, ropen, new Double[] { low, high }));
	}

	/**
	 * Constructs a new {@code DynamicType} instance that contains a given
	 * {@code interval} [{@code low}, {@code high}].
	 *
	 * @param low  the left endpoint
	 * @param high the right endpoint
	 */
	public TimeInterval(double low, double high) {
		super(new Interval<Double[]>(low, high, new Double[] { low, high }));
	}

	/**
	 * Constructs a new {@code DynamicType} instance with intervals given by
	 * {@code List<Double[]>} in.
	 *
	 * @param in intervals to add (could be null)
	 */
	public TimeInterval(List<Double[]> in) {
		super(getList(in));
	}

	/**
	 * Constructs a shallow copy of {@code source}.
	 *
	 * @param source an object to copy from (could be null, then completely new
	 *               instance is created)
	 */
	public TimeInterval(TimeInterval source) {
		super(source);
	}

	/**
	 * Constructs a shallow copy of {@code source} that contains a given
	 * {@code interval}.
	 *
	 * @param source an object to copy from (could be null, then completely new
	 *               instance is created)
	 * @param low    the left endpoint
	 * @param high   the right endpoint
	 * @param lopen  indicates if the left endpoint is excluded (true in this case)
	 * @param ropen  indicates if the right endpoint is excluded (true in this case)
	 */
	public TimeInterval(TimeInterval source, double low, double high, boolean lopen, boolean ropen) {
		super(source, new Interval<Double[]>(low, high, lopen, ropen, new Double[] { low, high }));
	}

	/**
	 * Constructs a shallow copy of {@code source} that contains a given
	 * {@code interval} [{@code low}, {@code high}].
	 *
	 * @param source an object to copy from (could be null, then completely new
	 *               instance is created)
	 * @param low    the left endpoint
	 * @param high   the right endpoint
	 */
	public TimeInterval(TimeInterval source, double low, double high) {
		super(source, new Interval<Double[]>(low, high, new Double[] { low, high }));
	}

	/**
	 * Constructs a shallow copy of {@code source} that contains a given
	 * {@code interval} [{@code alow}, {@code ahigh}]. Before add it removes
	 * from the newly created object all intervals that overlap with a given
	 * {@code interval} [{@code rlow}, {@code rhigh}].
	 *
	 * @param source an object to copy from (could be null, then completely new
	 *               instance is created)
	 * @param alow   the left endpoint of the interval to add
	 * @param ahigh  the right endpoint of the interval to add
	 * @param alopen indicates if the left endpoint of the interval to add is excluded (true in this case)
	 * @param aropen indicates if the right endpoint of the interval to add is excluded (true in this case)
	 * @param rlow   the left endpoint of the interval to remove
	 * @param rhigh  the right endpoint of the interval to remove
	 * @param blopen indicates if the left endpoint of the interval to remove is excluded (true in this case)
	 * @param bropen indicates if the right endpoint of the interval to remove is excluded (true in this case)
	 */
	public TimeInterval(TimeInterval source, double alow, double ahigh, boolean alopen, boolean aropen,
			double rlow, double rhigh, boolean blopen, boolean bropen) {
		super(source,
			new Interval<Double[]>(alow, ahigh, alopen, aropen, new Double[] { alow, ahigh }),
			new Interval<Double[]>(rlow, rhigh, blopen, bropen, new Double[] { rlow, rhigh }));
	}

	/**
	 * Constructs a shallow copy of {@code source} that contains a given
	 * {@code interval} [{@code alow}, {@code ahigh}]. Before add it removes
	 * from the newly created object all intervals that overlap with a given
	 * {@code interval} [{@code rlow}, {@code rhigh}].
	 *
	 * @param source an object to copy from (could be null, then completely new
	 *               instance is created)
	 * @param alow   the left endpoint of the interval to add
	 * @param ahigh  the right endpoint of the interval to add
	 * @param rlow   the left endpoint of the interval to remove
	 * @param rhigh  the right endpoint of the interval to remove
	 */
	public TimeInterval(TimeInterval source, double alow, double ahigh, double rlow, double rhigh) {
		super(source,
			new Interval<Double[]>(alow, ahigh, new Double[] { alow, ahigh }),
			new Interval<Double[]>(rlow, rhigh, new Double[] { rlow, rhigh }));
	}

	/**
	 * Constructs a shallow copy of {@code source} with additional intervals
	 * given by {@code List<Double[]>} in.
	 *
	 * @param source an object to copy from (could be null, then completely new
	 *               instance is created)
	 * @param in     intervals to add (could be null)
	 */
	public TimeInterval(TimeInterval source, List<Double[]> in) {
		super(source, getList(in));
	}

	/**
	 * Constructs a shallow copy of {@code source} with additional intervals
	 * given by {@code List<Double[]>} in. Before add it removes from the
	 * newly created object all intervals that overlap with intervals given by
	 * {@code List<Double[]>} out.
	 *
	 * @param source an object to copy from (could be null, then completely new
	 *               instance is created)
	 * @param in     intervals to add (could be null)
	 * @param out    intervals to remove (could be null)
	 */
	public TimeInterval(TimeInterval source, List<Double[]> in, List<Double[]> out) {
		super(source, getList(in), getList(out));
	}

	private static List<Interval<Double[]>> getList(List<Double[]> arg) {
		if (arg == null)
			return null;
		List<Interval<Double[]>> list = new ArrayList<Interval<Double[]>>();
		for (Double[] item : arg)
			list.add(new Interval<Double[]>(item[0], item[1], item));
		return list;
	}

	@Override
	public Double[] getValue(Interval<Double[]> interval, Estimator estimator) {
		List<Double[]> values = getValues(interval);
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
				throw new UnsupportedOperationException(
							"Not supported estimator");
			case MAX:
				throw new UnsupportedOperationException(
							"Not supported estimator");
			case FIRST:
				return values.get(0);
			case LAST:
				return values.get(values.size() - 1);
			default:
				throw new IllegalArgumentException("Unknown estimator.");
		}
	}

	@Override
	public Double[] getValue(double low, double high, Estimator estimator) {
		if (low > high)
			throw new IllegalArgumentException(
						"The left endpoint of the interval must be less than " +
						"the right endpoint.");

		return getValue(new Interval<Double[]>(low, high, false, false), estimator);
	}

	/**
	 * Returns a string representation of this instance in a format
	 * {@code <[low, high], ..., [low, high]>}. Intervals are
	 * ordered by its left endpoint.
	 *
	 * @return a string representation of this instance.
	 */
	@Override
	public String toString() {
		List<Interval<Double[]>> list = getIntervals(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		if (!list.isEmpty()) {
			StringBuilder sb = new StringBuilder("<");
			sb.append(list.get(0).isLowExcluded() ? "(" : "[").append(list.get(0).getLow()).append(", ").
					append(list.get(0).getHigh()).append(list.get(0).isHighExcluded() ? ")" : "]");
			for (int i = 1; i < list.size(); ++i)
				sb.append(", ").append(list.get(i).isLowExcluded()? "(" :"[").append(list.get(i).getLow()).append(", ").
						append(list.get(i).getHigh()).append(list.get(i).isHighExcluded() ? ")" : "]");
			sb.append(">");
			return sb.toString();
		}
		return "<empty>";
	}

	@Override
	public Class getUnderlyingType() {
		return Double[].class;
	}
}
