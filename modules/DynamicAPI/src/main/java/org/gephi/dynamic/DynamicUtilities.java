/*
 * Copyright 2008-2010 Gephi
 * Authors : Cezary Bartosiak
 * Website : http://www.gephi.org
 *
 * This file is part of Gephi.
 *
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
package org.gephi.dynamic;

/**
 * Contains only static, and toolkit functions, like type conversion for the
 * needs of dynamic stuff.
 *
 * @author Cezary Bartosiak
 */
public final class DynamicUtilities {
//    private static DatatypeFactory dateFactory;
//
//    static {
//        try {
//            dateFactory = DatatypeFactory.newInstance();
//        } catch (DatatypeConfigurationException ex) {
//        }
//    }
//
//    /**
//     * Used for import (parses XML date strings).
//     *
//     * @param str a string to parse from
//     *
//     * @return date as a double.
//     *
//     * @throws IllegalArgumentException if {@code str} is not a valid
//     * {@code XMLGregorianCalendar}.
//     * @throws NullPointerException if {@code str} is null.
//     */
//    public static double getDoubleFromXMLDateString(String str) {
//        try {
//            return dateFactory.newXMLGregorianCalendar(str.length() > 10 ? str.substring(0, 10) : str).
//                    toGregorianCalendar().getTimeInMillis();
//        } catch (IllegalArgumentException ex) {
//            //Try simple format
//            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//            try {
//                Date date = dateFormat.parse(str.length() > 10 ? str.substring(0, 10) : str);
//                return date.getTime();
//            } catch (ParseException ex1) {
//                Exceptions.printStackTrace(ex1);
//                return 0.0;
//            }
//        }
//    }
//
//    /**
//     * Used for import (parses XML date strings).
//     *
//     * @param str a string to parse from
//     *
//     * @return date as a double.
//     *
//     * @throws IllegalArgumentException if {@code str} is not a valid
//     * {@code XMLGregorianCalendar}.
//     * @throws NullPointerException if {@code str} is null.
//     */
//    public static double getDoubleFromXMLDateTimeString(String str) {
//        try {
//            return dateFactory.newXMLGregorianCalendar(str.length() > 23 ? str.substring(0, 23) : str).
//                    toGregorianCalendar().getTimeInMillis();
//        } catch (IllegalArgumentException ex) {
//            //Try simple format
//            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            try {
//                Date date = dateFormat.parse(str);
//                return date.getTime();
//            } catch (ParseException ex1) {
//                Exceptions.printStackTrace(ex1);
//                return 0.0;
//            }
//        }
//    }
//
//    /**
//     * Used for import (parses XML date strings).
//     *
//     * @param str a string to parse from
//     *
//     * @return date as a double.
//     *
//     * @throws IllegalArgumentException if {@code str} is not a valid
//     * {@code XMLGregorianCalendar}.
//     * @throws NullPointerException if {@code str} is null.
//     */
//    public static double getDoubleFromDate(Date date) {
//        return date.getTime();
//    }
//
//    /**
//     * Used for get a date from the low-level double
//     *
//     * @param d a double to convert from
//     *
//     * @return an date instance
//     *
//     * @throws IllegalArgumentException if {@code d} is infinite.
//     */
//    public static Date getDateFromDouble(double d) {
//        if (d == Double.NEGATIVE_INFINITY || d == Double.POSITIVE_INFINITY) {
//            throw new IllegalArgumentException("date can' be infinite");
//        }
//        GregorianCalendar gc = new GregorianCalendar();
//        gc.setTimeInMillis((long) d);
//        return dateFactory.newXMLGregorianCalendar(gc).toGregorianCalendar().getTime();
//    }
//
//    /**
//     * Used for export (writes XML date strings).
//     *
//     * @param d a double to convert from
//     *
//     * @return an XML date string.
//     *
//     * @throws IllegalArgumentException if {@code d} is infinite.
//     */
//    public static String getXMLDateStringFromDouble(double d) {
//        if (d == Double.NEGATIVE_INFINITY) {
//            return "-Infinity";
//        } else if (d == Double.POSITIVE_INFINITY) {
//            return "Infinity";
//        }
//        GregorianCalendar gc = new GregorianCalendar();
//        gc.setTimeInMillis((long) d);
//        return dateFactory.newXMLGregorianCalendar(gc).toXMLFormat().substring(0, 23);
//    }
//
//    /**
//     * Returns a new {@code DynamicType} instance that contains a given
//     * {@code Interval} in.
//     *
//     * @param in interval to add (could be null)
//     *
//     * @return a new {@code DynamicType} instance that contains a given
//     * {@code Interval} in.
//     */
//    public static DynamicType createDynamicObject(AttributeType type, Interval in) {
//        return createDynamicObject(type, null, in);
//    }
//
//    /**
//     * Returns a new {@code DynamicType} instance with intervals given by
//     * {@code List<Interval>} in.
//     *
//     * @param in intervals to add (could be null)
//     *
//     * @return a new {@code DynamicType} instance with intervals given by
//     * {@code List<Interval>} in.
//     */
//    public static DynamicType createDynamicObject(AttributeType type, List<Interval> in) {
//        return createDynamicObject(type, null, in);
//    }
//
//    /**
//     * Returns a deep copy of {@code source}.
//     *
//     * @param source an object to copy from (could be null, then completely new
//     * instance is created)
//     *
//     * @return a deep copy of {@code source}.
//     */
//    public static DynamicType createDynamicObject(AttributeType type, DynamicType source) {
//        return createDynamicObject(type, source, (Interval) null, (Interval) null);
//    }
//
//    /**
//     * Returns a deep copy of {@code source} that contains a given
//     * {@code Interval} in.
//     *
//     * @param source an object to copy from (could be null, then completely new
//     * instance is created)
//     * @param in interval to add (could be null)
//     *
//     * @return a deep copy of {@code source} that contains a given
//     * {@code Interval} in.
//     */
//    public static DynamicType createDynamicObject(AttributeType type, DynamicType source, Interval in) {
//        return createDynamicObject(type, source, in, null);
//    }
//
//    /**
//     * Returns a deep copy of {@code source} that contains a given
//     * {@code Interval} in. Before add it removes from the newly created object
//     * all intervals that overlap with a given {@code Interval} out.
//     *
//     * @param source an object to copy from (could be null, then completely new
//     * instance is created)
//     * @param in interval to add (could be null)
//     * @param out interval to remove (could be null)
//     *
//     * @return a deep copy of {@code source} that contains a given
//     * {@code Interval} in. Before add it removes from the newly created object
//     * all intervals that overlap with a given {@code Interval} out.
//     */
//    public static DynamicType createDynamicObject(AttributeType type, DynamicType source, Interval in, Interval out) {
//        ArrayList<Interval> lin = null;
//        ArrayList<Interval> lout = null;
//
//        if (in != null) {
//            lin = new ArrayList<Interval>();
//            lin.add(in);
//        }
//        if (out != null) {
//            lout = new ArrayList<Interval>();
//            lout.add(out);
//        }
//
//        return createDynamicObject(type, source, lin, lout);
//    }
//
//    /**
//     * Returns a deep copy of {@code source} with additional intervals given by
//     * {@code List<Interval>} in.
//     *
//     * @param source an object to copy from (could be null, then completely new
//     * instance is created)
//     * @param in intervals to add (could be null)
//     *
//     * @return a deep copy of {@code source} with additional intervals given by
//     * {@code List<Interval>} in.
//     */
//    public static DynamicType createDynamicObject(AttributeType type, DynamicType source, List<Interval> in) {
//        return createDynamicObject(type, source, in, null);
//    }
//
//    /**
//     * Returns a deep copy of {@code source} with additional intervals given by
//     * {@code List<Interval>} in. Before add it removes from the newly created
//     * object all intervals that overlap with intervals given by
//     * {@code List<Interval>} out. <p> It can return {@code null} if type is not
//     * dynamic.
//     *
//     * @param source an object to copy from (could be null, then completely new
//     * instance is created)
//     * @param in intervals to add (could be null)
//     * @param out intervals to remove (could be null)
//     *
//     * @return a deep copy of {@code source} with additional intervals given by
//     * {@code List<Interval>} in. Before add it removes from the newly created
//     * object all intervals that overlap with intervals given by
//     * {@code List<Interval>} out. It can return {@code null} if type is not
//     * dynamic.
//     */
//    public static DynamicType createDynamicObject(AttributeType type, DynamicType source, List<Interval> in,
//            List<Interval> out) {
//        if (!type.isDynamicType()) {
//            return null;
//        }
//
//        switch (type) {
//            case DYNAMIC_BYTE: {
//                ArrayList<Interval<Byte>> lin = null;
//                if (in != null) {
//                    lin = new ArrayList<Interval<Byte>>();
//                    for (Interval interval : in) {
//                        lin.add(new Interval<Byte>(interval.getLow(), interval.getHigh(),
//                                interval.isLowExcluded(), interval.isHighExcluded(), (Byte) interval.getValue()));
//                    }
//                }
//                ArrayList<Interval<Byte>> lout = null;
//                if (out != null) {
//                    lout = new ArrayList<Interval<Byte>>();
//                    for (Interval interval : out) {
//                        lout.add(new Interval<Byte>(interval.getLow(), interval.getHigh(),
//                                interval.isLowExcluded(), interval.isHighExcluded(), (Byte) interval.getValue()));
//                    }
//                }
//                return new DynamicByte((DynamicByte) source, lin, lout);
//            }
//            case DYNAMIC_SHORT: {
//                ArrayList<Interval<Short>> lin = null;
//                if (in != null) {
//                    lin = new ArrayList<Interval<Short>>();
//                    for (Interval interval : in) {
//                        lin.add(new Interval<Short>(interval.getLow(), interval.getHigh(),
//                                interval.isLowExcluded(), interval.isHighExcluded(), (Short) interval.getValue()));
//                    }
//                }
//                ArrayList<Interval<Short>> lout = null;
//                if (out != null) {
//                    lout = new ArrayList<Interval<Short>>();
//                    for (Interval interval : out) {
//                        lout.add(new Interval<Short>(interval.getLow(), interval.getHigh(),
//                                interval.isLowExcluded(), interval.isHighExcluded(), (Short) interval.getValue()));
//                    }
//                }
//                return new DynamicShort((DynamicShort) source, lin, lout);
//            }
//            case DYNAMIC_INT: {
//                ArrayList<Interval<Integer>> lin = null;
//                if (in != null) {
//                    lin = new ArrayList<Interval<Integer>>();
//                    for (Interval interval : in) {
//                        lin.add(new Interval<Integer>(interval.getLow(), interval.getHigh(),
//                                interval.isLowExcluded(), interval.isHighExcluded(), (Integer) interval.getValue()));
//                    }
//                }
//                ArrayList<Interval<Integer>> lout = null;
//                if (out != null) {
//                    lout = new ArrayList<Interval<Integer>>();
//                    for (Interval interval : out) {
//                        lout.add(new Interval<Integer>(interval.getLow(), interval.getHigh(),
//                                interval.isLowExcluded(), interval.isHighExcluded(), (Integer) interval.getValue()));
//                    }
//                }
//                return new DynamicInteger((DynamicInteger) source, lin, lout);
//            }
//            case DYNAMIC_LONG: {
//                ArrayList<Interval<Long>> lin = null;
//                if (in != null) {
//                    lin = new ArrayList<Interval<Long>>();
//                    for (Interval interval : in) {
//                        lin.add(new Interval<Long>(interval.getLow(), interval.getHigh(),
//                                interval.isLowExcluded(), interval.isHighExcluded(), (Long) interval.getValue()));
//                    }
//                }
//                ArrayList<Interval<Long>> lout = null;
//                if (out != null) {
//                    lout = new ArrayList<Interval<Long>>();
//                    for (Interval interval : out) {
//                        lout.add(new Interval<Long>(interval.getLow(), interval.getHigh(),
//                                interval.isLowExcluded(), interval.isHighExcluded(), (Long) interval.getValue()));
//                    }
//                }
//                return new DynamicLong((DynamicLong) source, lin, lout);
//            }
//            case DYNAMIC_FLOAT: {
//                ArrayList<Interval<Float>> lin = null;
//                if (in != null) {
//                    lin = new ArrayList<Interval<Float>>();
//                    for (Interval interval : in) {
//                        lin.add(new Interval<Float>(interval.getLow(), interval.getHigh(),
//                                interval.isLowExcluded(), interval.isHighExcluded(), (Float) interval.getValue()));
//                    }
//                }
//                ArrayList<Interval<Float>> lout = null;
//                if (out != null) {
//                    lout = new ArrayList<Interval<Float>>();
//                    for (Interval interval : out) {
//                        lout.add(new Interval<Float>(interval.getLow(), interval.getHigh(),
//                                interval.isLowExcluded(), interval.isHighExcluded(), (Float) interval.getValue()));
//                    }
//                }
//                return new DynamicFloat((DynamicFloat) source, lin, lout);
//            }
//            case DYNAMIC_DOUBLE: {
//                ArrayList<Interval<Double>> lin = null;
//                if (in != null) {
//                    lin = new ArrayList<Interval<Double>>();
//                    for (Interval interval : in) {
//                        lin.add(new Interval<Double>(interval.getLow(), interval.getHigh(),
//                                interval.isLowExcluded(), interval.isHighExcluded(), (Double) interval.getValue()));
//                    }
//                }
//                ArrayList<Interval<Double>> lout = null;
//                if (out != null) {
//                    lout = new ArrayList<Interval<Double>>();
//                    for (Interval interval : out) {
//                        lout.add(new Interval<Double>(interval.getLow(), interval.getHigh(),
//                                interval.isLowExcluded(), interval.isHighExcluded(), (Double) interval.getValue()));
//                    }
//                }
//                return new DynamicDouble((DynamicDouble) source, lin, lout);
//            }
//            case DYNAMIC_BOOLEAN: {
//                ArrayList<Interval<Boolean>> lin = null;
//                if (in != null) {
//                    lin = new ArrayList<Interval<Boolean>>();
//                    for (Interval interval : in) {
//                        lin.add(new Interval<Boolean>(interval.getLow(), interval.getHigh(),
//                                interval.isLowExcluded(), interval.isHighExcluded(), (Boolean) interval.getValue()));
//                    }
//                }
//                ArrayList<Interval<Boolean>> lout = null;
//                if (out != null) {
//                    lout = new ArrayList<Interval<Boolean>>();
//                    for (Interval interval : out) {
//                        lout.add(new Interval<Boolean>(interval.getLow(), interval.getHigh(),
//                                interval.isLowExcluded(), interval.isHighExcluded(), (Boolean) interval.getValue()));
//                    }
//                }
//                return new DynamicBoolean((DynamicBoolean) source, lin, lout);
//            }
//            case DYNAMIC_CHAR: {
//                ArrayList<Interval<Character>> lin = null;
//                if (in != null) {
//                    lin = new ArrayList<Interval<Character>>();
//                    for (Interval interval : in) {
//                        lin.add(new Interval<Character>(interval.getLow(), interval.getHigh(),
//                                interval.isLowExcluded(), interval.isHighExcluded(), (Character) interval.getValue()));
//                    }
//                }
//                ArrayList<Interval<Character>> lout = null;
//                if (out != null) {
//                    lout = new ArrayList<Interval<Character>>();
//                    for (Interval interval : out) {
//                        lout.add(new Interval<Character>(interval.getLow(), interval.getHigh(),
//                                interval.isLowExcluded(), interval.isHighExcluded(), (Character) interval.getValue()));
//                    }
//                }
//                return new DynamicCharacter((DynamicCharacter) source, lin, lout);
//            }
//            case DYNAMIC_STRING: {
//                ArrayList<Interval<String>> lin = null;
//                if (in != null) {
//                    lin = new ArrayList<Interval<String>>();
//                    for (Interval interval : in) {
//                        lin.add(new Interval<String>(interval.getLow(), interval.getHigh(),
//                                interval.isLowExcluded(), interval.isHighExcluded(), (String) interval.getValue()));
//                    }
//                }
//                ArrayList<Interval<String>> lout = null;
//                if (out != null) {
//                    lout = new ArrayList<Interval<String>>();
//                    for (Interval interval : out) {
//                        lout.add(new Interval<String>(interval.getLow(), interval.getHigh(),
//                                interval.isLowExcluded(), interval.isHighExcluded(), (String) interval.getValue()));
//                    }
//                }
//                return new DynamicString((DynamicString) source, lin, lout);
//            }
//            case DYNAMIC_BIGINTEGER: {
//                ArrayList<Interval<BigInteger>> lin = null;
//                if (in != null) {
//                    lin = new ArrayList<Interval<BigInteger>>();
//                    for (Interval interval : in) {
//                        lin.add(new Interval<BigInteger>(interval.getLow(), interval.getHigh(),
//                                interval.isLowExcluded(), interval.isHighExcluded(), (BigInteger) interval.getValue()));
//                    }
//                }
//                ArrayList<Interval<BigInteger>> lout = null;
//                if (out != null) {
//                    lout = new ArrayList<Interval<BigInteger>>();
//                    for (Interval interval : out) {
//                        lout.add(new Interval<BigInteger>(interval.getLow(), interval.getHigh(),
//                                interval.isLowExcluded(), interval.isHighExcluded(), (BigInteger) interval.getValue()));
//                    }
//                }
//                return new DynamicBigInteger((DynamicBigInteger) source, lin, lout);
//            }
//            case DYNAMIC_BIGDECIMAL: {
//                ArrayList<Interval<BigDecimal>> lin = null;
//                if (in != null) {
//                    lin = new ArrayList<Interval<BigDecimal>>();
//                    for (Interval interval : in) {
//                        lin.add(new Interval<BigDecimal>(interval.getLow(), interval.getHigh(),
//                                interval.isLowExcluded(), interval.isHighExcluded(), (BigDecimal) interval.getValue()));
//                    }
//                }
//                ArrayList<Interval<BigDecimal>> lout = null;
//                if (out != null) {
//                    lout = new ArrayList<Interval<BigDecimal>>();
//                    for (Interval interval : out) {
//                        lout.add(new Interval<BigDecimal>(interval.getLow(), interval.getHigh(),
//                                interval.isLowExcluded(), interval.isHighExcluded(), (BigDecimal) interval.getValue()));
//                    }
//                }
//                return new DynamicBigDecimal((DynamicBigDecimal) source, lin, lout);
//            }
//            case TIME_INTERVAL: {
//                ArrayList<Interval> lin = null;
//                if (in != null) {
//                    lin = new ArrayList<Interval>();
//                    for (Interval interval : in) {
//                        lin.add(new Interval(interval.getLow(), interval.getHigh(),
//                                interval.isLowExcluded(), interval.isHighExcluded()));
//                    }
//                }
//                ArrayList<Interval> lout = null;
//                if (out != null) {
//                    lout = new ArrayList<Interval>();
//                    for (Interval interval : out) {
//                        lout.add(new Interval(interval.getLow(), interval.getHigh(),
//                                interval.isLowExcluded(), interval.isHighExcluded()));
//                    }
//                }
//                return new TimeInterval((TimeInterval) source, lin, lout);
//            }
//            default:
//                return null;
//        }
//    }
//
//    /**
//     * It checks intervals of the {@code source} and make it fit to the given
//     * interval, possibly removing intervals out of the window and changing low
//     * or high of intervals to fit.
//     *
//     * @param source a {@code DynamicType} to be performed
//     * @param interval a given interval
//     *
//     * @return a fitted {@code DynamicType} instance.
//     *
//     * @throws NullPointerException if {@code source} is null.
//     */
//    public static DynamicType fitToInterval(DynamicType source, Interval interval) {
//        if (source == null) {
//            throw new NullPointerException("The source cannot be null.");
//        }
//
//        List<Interval> sIntervals = source.getIntervals(interval);
//        List<Interval> tIntervals = new ArrayList<Interval>();
//        for (Interval i : sIntervals) {
//            double iLow = i.getLow();
//            double iHigh = i.getHigh();
//            boolean ilopen = i.isLowExcluded();
//            boolean iropen = i.isHighExcluded();
//            if (i.getLow() < interval.getLow()) {
//                iLow = interval.getLow();
//            }
//            if (i.getHigh() > interval.getHigh()) {
//                iHigh = interval.getHigh();
//            }
//            if (interval.isLowExcluded()) {
//                ilopen = true;
//            }
//            if (interval.isHighExcluded()) {
//                iropen = true;
//            }
//            tIntervals.add(new Interval(iLow, iHigh, ilopen, iropen, i.getValue()));
//        }
//
//        return createDynamicObject(AttributeType.parse(source), tIntervals);
//    }
//
//    /**
//     * It checks intervals of the {@code source} and make it fit to the given
//     * interval [{@code low}, {@code high}], possibly removing intervals out of
//     * the window and changing low or high of intervals to fit.
//     *
//     * @param source a {@code DynamicType} to be performed
//     * @param low the left endpoint
//     * @param high the right endpoint
//     *
//     * @return a fitted {@code DynamicType} instance.
//     *
//     * @throws NullPointerException if {@code source} is null.
//     * @throws IllegalArgumentException if {@code low} > {@code high}.
//     */
//    public static DynamicType fitToInterval(DynamicType source, double low, double high) {
//        return fitToInterval(source, new Interval(low, high));
//    }
//
//    /**
//     * Returns the visible time interval of
//     * <code>dynamicModel</code> if it is not [-inf, +inf]. Returns
//     * <code>null</null> in other cases.
//     *
//     * @param dynamicModel the dynamic model
//     *
//     * @return the valid visible interval, or <code>null</code>.
//     */
//    public static TimeInterval getVisibleInterval(DynamicModel dynamicModel) {
//        if (dynamicModel != null) {
//            TimeInterval ti = dynamicModel.getVisibleInterval();
//            if (ti != null && !(Double.isInfinite(ti.getLow()) && Double.isInfinite(ti.getHigh()))) {
//                return ti;
//            }
//        }
//        return null;
//    }
//
//    public static Object getDynamicValue(Object value, double low, double high) {
//        if (value != null && value instanceof DynamicType) {
//            DynamicType dynamicType = (DynamicType) value;
//            Estimator estimator = Estimator.FIRST;
//            if (Number.class.isAssignableFrom(dynamicType.getUnderlyingType())) {
//                estimator = Estimator.AVERAGE;
//            }
//            return dynamicType.getValue(low, high, estimator);
//        }
//        return value;
//    }
//
//    public static DynamicType removeOverlapping(DynamicType dynamicType) {
//        Comparator<Interval> comparator = new Comparator<Interval>() {
//            @Override
//            public int compare(Interval o1, Interval o2) {
//                if (o1.getLow() < o2.getLow()) {
//                    return -1;
//                }
//                if (o2.getLow() < o1.getLow()) {
//                    return 1;
//                }
//                if (o1.getHigh() < o2.getHigh()) {
//                    return -1;
//                }
//                if (o2.getHigh() < o1.getHigh()) {
//                    return 1;
//                }
//                return 0;
//            }
//        };
//
//        List<Interval> intervals = dynamicType.getIntervals(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
//        Collections.sort(intervals, comparator);
//        boolean overlap = true;
//        while (overlap) {
//            overlap = false;
//            for (int i = 0; i < intervals.size() - 1; i++) {
//                Interval interval = intervals.get(i);
//                Interval next = intervals.get(i + 1);
//                if (interval.getLow() == next.getLow()) {
//                    intervals.set(i + 1, createInterval(dynamicType, interval.getHigh(), next.getHigh(), true,
//                            next.isHighExcluded(), next.getValue()));
//                    overlap = true;
//                    break;
//                }
//                if (interval.getHigh() == next.getHigh()) {
//                    intervals.set(i, createInterval(dynamicType, interval.getLow(), next.getLow(),
//                            interval.isLowExcluded(), true, interval.getValue()));
//                    overlap = true;
//                    break;
//                }
//                if (next.getLow() < interval.getLow() && next.getHigh() > interval.getHigh()) {
//                    intervals.set(i + 1, createInterval(dynamicType, interval.getHigh(), next.getHigh(), true,
//                            next.isHighExcluded(), interval.getValue()));
//                    overlap = true;
//                    break;
//                }
//                if ((next.getLow() < interval.getHigh() || (next.getLow() == interval.getHigh()
//                        && !interval.isHighExcluded())) && next.getHigh() < interval.getHigh()) {
//                    intervals.set(i, createInterval(dynamicType, interval.getLow(), next.getLow(),
//                            interval.isLowExcluded(), true, interval.getValue()));
//                    intervals.add(i + 2, createInterval(dynamicType, next.getHigh(), interval.getHigh(), true,
//                            interval.isHighExcluded(), interval.getValue()));
//                    overlap = true;
//                    break;
//                }
//                if (next.getLow() == interval.getHigh() && !interval.isHighExcluded() && !next.isLowExcluded()) {
//                    intervals.set(i, createInterval(dynamicType, interval.getLow(), interval.getHigh(),
//                            interval.isLowExcluded(), true, interval.getValue()));
//                    overlap = true;
//                    break;
//                }
//                if (next.getLow() < interval.getHigh()) {
//                    intervals.set(i, createInterval(dynamicType, interval.getLow(), next.getHigh(),
//                            interval.isLowExcluded(), true, interval.getValue()));
//                    overlap = true;
//                    break;
//                }
//            }
//        }
//        return createDynamicObject(AttributeType.parse(dynamicType), intervals);
//    }
//
//    public static Interval createInterval(DynamicType dynamicType, double low, double high, boolean lopen,
//            boolean ropen, Object value) {
//        if (dynamicType instanceof TimeInterval) {
//            return new Interval<Double[]>(low, high, lopen, ropen, new Double[]{low, high});
//        }
//        if (dynamicType instanceof DynamicBigDecimal) {
//            return new Interval<BigDecimal>(low, high, lopen, ropen, (BigDecimal) value);
//        }
//        if (dynamicType instanceof DynamicBigInteger) {
//            return new Interval<BigInteger>(low, high, lopen, ropen, (BigInteger) value);
//        }
//        if (dynamicType instanceof DynamicBoolean) {
//            return new Interval<Boolean>(low, high, lopen, ropen, (Boolean) value);
//        }
//        if (dynamicType instanceof DynamicByte) {
//            return new Interval<Byte>(low, high, lopen, ropen, (Byte) value);
//        }
//        if (dynamicType instanceof DynamicCharacter) {
//            return new Interval<Character>(low, high, lopen, ropen, (Character) value);
//        }
//        if (dynamicType instanceof DynamicDouble) {
//            return new Interval<Double>(low, high, lopen, ropen, (Double) value);
//        }
//        if (dynamicType instanceof DynamicFloat) {
//            return new Interval<Float>(low, high, lopen, ropen, (Float) value);
//        }
//        if (dynamicType instanceof DynamicInteger) {
//            return new Interval<Integer>(low, high, lopen, ropen, (Integer) value);
//        }
//        if (dynamicType instanceof DynamicLong) {
//            return new Interval<Long>(low, high, lopen, ropen, (Long) value);
//        }
//        if (dynamicType instanceof DynamicShort) {
//            return new Interval<Short>(low, high, lopen, ropen, (Short) value);
//        }
//        if (dynamicType instanceof DynamicString) {
//            return new Interval<String>(low, high, lopen, ropen, (String) value);
//        }
//        return null;
//    }
//
//    public static int getNodeCount(Graph graph, Interval timeInterval) {
//        int nodeCount = 0;
//        for (Node n : graph.getNodes()) {
//            TimeInterval tnode = (TimeInterval) n.getAttributes().getValue(DynamicModel.TIMEINTERVAL_COLUMN);
//            if (tnode.isInRange(timeInterval.getLow(), timeInterval.getHigh())) {
//                nodeCount++;
//            }
//        }
//        return nodeCount;
//    }
//
//    public static int getEdgeCount(Graph graph, Interval timeInterval) {
//        int edgeCount = 0;
//        for (Edge e : ((HierarchicalGraph) graph).getEdgesAndMetaEdges()) {
//            TimeInterval tedge = (TimeInterval) e.getAttributes().getValue(DynamicModel.TIMEINTERVAL_COLUMN);
//            if (tedge.isInRange(timeInterval.getLow(), timeInterval.getHigh())) {
//
//                TimeInterval tsource = (TimeInterval) e.getSource().getAttributes().getValue(DynamicModel.TIMEINTERVAL_COLUMN);
//                TimeInterval tdest = (TimeInterval) e.getTarget().getAttributes().getValue(DynamicModel.TIMEINTERVAL_COLUMN);
//                if (tsource.isInRange(timeInterval.getLow(), timeInterval.getHigh())
//                        && tdest.isInRange(timeInterval.getLow(), timeInterval.getHigh())) {
//                    edgeCount++;
//                }
//
//            }
//        }
//        return edgeCount;
//    }
}
