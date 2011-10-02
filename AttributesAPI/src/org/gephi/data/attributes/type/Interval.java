/*
Copyright 2008-2010 Gephi
Authors : Cezary Bartosiak
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
 */
package org.gephi.data.attributes.type;

import org.gephi.data.attributes.api.AttributeUtils;

/**
 * This class represents an interval with some value.
 *
 * @author Cezary Bartosiak
 *
 * @param <T> type of data
 */
public final class Interval<T> implements Comparable<Interval> {

    private double low;   // the left endpoint
    private double high;  // the right endpoint
    private boolean lopen; // indicates if the left endpoint is excluded
    private boolean ropen; // indicates if the right endpoint is excluded
    private T value; // the value stored in this interval

    /**
     * Constructs a new interval instance 
     *
     * <p>Note that {@code value} cannot be null if you want use this
     * {@code interval} as a value storage. If it is null some estimators
     * could not work and generate exceptions.
     *
     * @param interval  the interval to copy the values from
     * @param value     the value stored in this interval
     *
     * @throws IllegalArgumentException if {@code low} > {@code high}.
     */
    public Interval(Interval interval, T value) {
        this.low = interval.low;
        this.high = interval.high;
        this.lopen = interval.lopen;
        this.ropen = interval.ropen;
        this.value = value;
    }

    /**
     * Constructs a new interval instance.
     *
     * <p>Note that {@code value} cannot be null if you want use this
     * {@code interval} as a value storage. If it is null some estimators
     * could not work and generate exceptions.
     *
     * @param low   the left endpoint
     * @param high  the right endpoint
     * @param lopen indicates if the left endpoint is excluded (true in this case)
     * @param ropen indicates if the right endpoint is excluded (true in this case)
     * @param value the value stored in this interval
     *
     * @throws IllegalArgumentException if {@code low} > {@code high}.
     */
    public Interval(double low, double high, boolean lopen, boolean ropen, T value) {
        if (low > high) {
            throw new IllegalArgumentException(
                    "The left endpoint of the interval must be less than "
                    + "the right endpoint.");
        }

        this.low = low;
        this.high = high;
        this.lopen = lopen;
        this.ropen = ropen;
        this.value = value;
    }

    /**
     * Constructs a new interval instance with no value.
     *
     * @param low  the left endpoint
     * @param high the right endpoint
     * @param lopen indicates if the left endpoint is excluded (true in this case)
     * @param ropen indicates if the right endpoint is excluded (true in this case)
     *
     * @throws IllegalArgumentException if {@code low} > {@code high}.
     */
    public Interval(double low, double high, boolean lopen, boolean ropen) {
        this(low, high, lopen, ropen, null);
    }

    /**
     * Constructs a new interval instance with left and right endpoints included
     * by default.
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
        this(low, high, false, false, value);
    }

    /**
     * Constructs a new interval instance with no value and left and right
     * endpoints included by default.
     *
     * @param low  the left endpoint
     * @param high the right endpoint
     *
     * @throws IllegalArgumentException if {@code low} > {@code high}.
     */
    public Interval(double low, double high) {
        this(low, high, false, false, null);
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
     * i'.high}) they aren't equal. Remember that if two intervals are equal,
     * they have got the same bounds excluded or included.
     * 
     * @param interval the interval to be compared
     * 
     * @return a negative integer, zero, or a positive integer as this interval
     *         is to the left of, overlaps with, or is to the right of the
     *         specified interval.
     *
     * @throws NullPointerException if {@code interval} is null.
     */
    public int compareTo(Interval interval) {
        if (interval == null) {
            throw new NullPointerException("Interval cannot be null.");
        }

        if (high < interval.low || high <= interval.low && (ropen || interval.lopen)) {
            return -1;
        }
        if (interval.high < low || interval.high <= low && (interval.ropen || lopen)) {
            return 1;
        }
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
     * Indicates if the left endpoint is excluded.
     *
     * @return {@code true} if the left endpoint is excluded,
     *         {@code false} otherwise.
     */
    public boolean isLowExcluded() {
        return lopen;
    }

    /**
     * Indicates if the right endpoint is excluded.
     *
     * @return {@code true} if the right endpoint is excluded,
     *         {@code false} otherwise.
     */
    public boolean isHighExcluded() {
        return ropen;
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
     * Compares this interval with the specified object for equality.
     *
     * <p>Note that two intervals are equal if {@code i.low = i'.low} and
     * {@code i.high = i'.high} and they have got the bounds excluded/included.
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
            Interval<T> interval = (Interval<T>) obj;
            if (low == interval.low && high == interval.high
                    && lopen == interval.lopen && ropen == interval.ropen) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (int) (Double.doubleToLongBits(this.low) ^ (Double.doubleToLongBits(this.low) >>> 32));
        hash = 97 * hash + (int) (Double.doubleToLongBits(this.high) ^ (Double.doubleToLongBits(this.high) >>> 32));
        hash = 97 * hash + (this.lopen ? 1 : 0);
        hash = 97 * hash + (this.ropen ? 1 : 0);
        return hash;
    }

    /**
     * Creates a string representation of the interval with its value.
     *
     * @param timesAsDoubles indicates if times should be shown as doubles or dates
     *
     * @return a string representation with times as doubles or dates.
     */
    public String toString(boolean timesAsDoubles) {
        if (timesAsDoubles) {
            return (lopen ? "(" : "[") + low + ", " + high + ", " + value + (ropen ? ")" : "]");
        } else {
            return (lopen ? "(" : "[") + AttributeUtils.getXMLDateStringFromDouble(low) + ", "
                    + AttributeUtils.getXMLDateStringFromDouble(high) + ", " + value + (ropen ? ")" : "]");
        }
    }

    /**
     * Returns a string representation of this interval in one of the formats:
     * <ol>
     * <li>
     * {@code [low, high, value]}
     * <li>
     * {@code (low, high, value]}
     * <li>
     * {@code [low, high, value)}
     * <li>
     * {@code (low, high, value)}
     * </ol>
     *
     * <p>Times are always shown as doubles</p>
     *
     * @return a string representation of this interval.
     */
    @Override
    public String toString() {
        return toString(true);
    }
}
