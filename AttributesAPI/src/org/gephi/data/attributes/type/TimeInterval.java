/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian, Cezary Bartosiak
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
import org.gephi.data.attributes.api.AttributeUtils;
import org.gephi.data.attributes.api.Estimator;

/**
 * Complex type for specifying time interval. An, interval is two
 * <code>double</code> with <code>low</code> inferior or equal to
 * <code>high</code>. Thus intervals can have got included or excluded
 * bounds.
 *
 * @author Mathieu Bastian, Cezary Bartosiak
 */
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
        super(new Interval<Double[]>(low, high, lopen, ropen));
    }

    /**
     * Constructs a new {@code DynamicType} instance that contains a given
     * {@code interval} [{@code low}, {@code high}].
     *
     * @param low  the left endpoint
     * @param high the right endpoint
     */
    public TimeInterval(double low, double high) {
        super(new Interval<Double[]>(low, high));
    }

    /**
     * Constructs a new {@code DynamicType} instance with intervals given by
     * {@code List<Interval>} in.
     *
     * @param in intervals to add (could be null)
     */
    public TimeInterval(List<Interval> in) {
        super(getList(in));
    }

    /**
     * Constructs a deep copy of {@code source}.
     *
     * @param source an object to copy from (could be null, then completely new
     *               instance is created)
     */
    public TimeInterval(TimeInterval source) {
        super(source);
    }

    /**
     * Constructs a deep copy of {@code source} that contains a given
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
        super(source, new Interval<Double[]>(low, high, lopen, ropen));
    }

    /**
     * Constructs a deep copy of {@code source} that contains a given
     * {@code interval} [{@code low}, {@code high}].
     *
     * @param source an object to copy from (could be null, then completely new
     *               instance is created)
     * @param low    the left endpoint
     * @param high   the right endpoint
     */
    public TimeInterval(TimeInterval source, double low, double high) {
        super(source, new Interval<Double[]>(low, high));
    }

    /**
     * Constructs a deep copy of {@code source} that contains a given
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
                new Interval<Double[]>(alow, ahigh, alopen, aropen),
                new Interval<Double[]>(rlow, rhigh, blopen, bropen));
    }

    /**
     * Constructs a deep copy of {@code source} that contains a given
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
                new Interval<Double[]>(alow, ahigh),
                new Interval<Double[]>(rlow, rhigh));
    }

    /**
     * Constructs a deep copy of {@code source} with additional intervals
     * given by {@code List<Interval>} in.
     *
     * @param source an object to copy from (could be null, then completely new
     *               instance is created)
     * @param in     intervals to add (could be null)
     */
    public TimeInterval(TimeInterval source, List<Interval> in) {
        super(source, getList(in));
    }

    /**
     * Constructs a deep copy of {@code source} with additional intervals
     * given by {@code List<Interval>} in. Before add it removes from the
     * newly created object all intervals that overlap with intervals given by
     * {@code List<Interval>} out.
     *
     * @param source an object to copy from (could be null, then completely new
     *               instance is created)
     * @param in     intervals to add (could be null)
     * @param out    intervals to remove (could be null)
     */
    public TimeInterval(TimeInterval source, List<Interval> in, List<Interval> out) {
        super(source, getList(in), getList(out));
    }

    private static List<Interval<Double[]>> getList(List<Interval> arg) {
        if (arg == null) {
            return null;
        }
        List<Interval<Double[]>> list = new ArrayList<Interval<Double[]>>();
        for (Interval item : arg) {
            list.add(new Interval<Double[]>(item.getLow(), item.getHigh(),
                    item.isLowExcluded(), item.isHighExcluded()));
        }
        return list;
    }

    @Override
    public Double[] getValue(Interval interval, Estimator estimator) {
        List<Double[]> values = getValues(interval);
        if (values.isEmpty()) {
            return null;
        }

        switch (estimator) {
            case AVERAGE:
                throw new UnsupportedOperationException(
                        "Not supported estimator");
            case MEDIAN:
                if (values.size() % 2 == 1) {
                    return values.get(values.size() / 2);
                }
                return values.get(values.size() / 2 - 1);
            case MODE:
                Hashtable<Integer, Integer> map =
                        new Hashtable<Integer, Integer>();
                for (int i = 0; i < values.size(); ++i) {
                    int prev = 0;
                    if (map.containsKey(values.get(i).hashCode())) {
                        prev = map.get(values.get(i).hashCode());
                    }
                    map.put(values.get(i).hashCode(), prev + 1);
                }
                int max = map.get(values.get(0).hashCode());
                int index = 0;
                for (int i = 1; i < values.size(); ++i) {
                    if (max < map.get(values.get(i).hashCode())) {
                        max = map.get(values.get(i).hashCode());
                        index = i;
                    }
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
    public List<Double[]> getValues(Interval interval) {
        List<Double[]> result = new ArrayList<Double[]>();
        for (Interval<Double[]> i : intervalTree.search(interval)) {
            result.add(new Double[]{i.getLow(), i.getHigh()});
        }
        return result;
    }

    @Override
    public Class getUnderlyingType() {
        return Double[].class;
    }

    @Override
    public String toString(boolean timesAsDoubles) {
        if (timesAsDoubles) {
            return toString();
        } else {
            return toStringTimesAsDates();
        }
    }

    /**
     * Returns a string representation of this instance in a format
     * {@code <[low, high], ..., [low, high]>}. Intervals are
     * ordered by its left endpoint.
     *
     * Times are always shown as dates
     *
     * @return a string representation of this instance.
     */
    public String toStringTimesAsDates() {
        List<Interval<Double[]>> list = getIntervals(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        if (!list.isEmpty()) {
            StringBuilder sb = new StringBuilder("<");
            sb.append(list.get(0).isLowExcluded() ? "(" : "[").append(AttributeUtils.getXMLDateStringFromDouble(list.get(0).getLow())).append(", ").
                    append(AttributeUtils.getXMLDateStringFromDouble(list.get(0).getHigh())).append(list.get(0).isHighExcluded() ? ")" : "]");
            for (int i = 1; i < list.size(); ++i) {
                sb.append(", ").append(list.get(i).isLowExcluded() ? "(" : "[").append(AttributeUtils.getXMLDateStringFromDouble(list.get(i).getLow())).append(", ").
                        append(AttributeUtils.getXMLDateStringFromDouble(list.get(i).getHigh())).append(list.get(i).isHighExcluded() ? ")" : "]");
            }
            sb.append(">");
            return sb.toString();
        }
        return "<empty>";
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
            for (int i = 1; i < list.size(); ++i) {
                sb.append("; ").append(list.get(i).isLowExcluded() ? "(" : "[").append(list.get(i).getLow()).append(", ").
                        append(list.get(i).getHigh()).append(list.get(i).isHighExcluded() ? ")" : "]");
            }
            sb.append(">");
            return sb.toString();
        }
        return "<empty>";
    }
}
