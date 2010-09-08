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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.type.DynamicBigDecimal;
import org.gephi.data.attributes.type.DynamicBigInteger;
import org.gephi.data.attributes.type.DynamicBoolean;
import org.gephi.data.attributes.type.DynamicByte;
import org.gephi.data.attributes.type.DynamicCharacter;
import org.gephi.data.attributes.type.DynamicDouble;
import org.gephi.data.attributes.type.DynamicFloat;
import org.gephi.data.attributes.type.DynamicInteger;
import org.gephi.data.attributes.type.DynamicLong;
import org.gephi.data.attributes.type.DynamicShort;
import org.gephi.data.attributes.type.DynamicString;
import org.gephi.data.attributes.type.DynamicType;
import org.gephi.data.attributes.type.Interval;
import org.gephi.data.attributes.type.TimeInterval;
import org.openide.util.Exceptions;

/**
 * Contains only static, and toolkit functions, like type conversion
 * for the needs of dynamic stuff.
 *
 * @author Cezary Bartosiak
 */
public final class DynamicUtilities {
	/**
	 * Used for import (parses XML date strings).
	 *
	 * @param str a string to parse from
	 *
	 * @return date as a double.
	 *
	 * @throws IllegalArgumentException if {@code str} is not a valid {@code XMLGregorianCalendar}.
	 * @throws NullPointerException     if {@code str} is null.
	 */
	public static double getDoubleFromXMLDateString(String str) {
		try {
			DatatypeFactory dateFactory = DatatypeFactory.newInstance();
			return dateFactory.newXMLGregorianCalendar(str.length() > 23 ? str.substring(0, 23) : str).
					toGregorianCalendar().getTimeInMillis();
		}
		catch (DatatypeConfigurationException ex) {
			Exceptions.printStackTrace(ex);
			return 0.0;
		}
	}
	
	/**
	 * Used for export (writes XML date strings).
	 * 
	 * @param d a double to convert from
	 *
	 * @return an XML date string.
	 *
	 * @throws IllegalArgumentException if {@code d} is infinite.
	 */
	public static String getXMLDateStringFromDouble(double d) {
		try {
			DatatypeFactory dateFactory = DatatypeFactory.newInstance();
			if (Double.isInfinite(d))
				throw new IllegalArgumentException("The passed double cannot be infinite.");
			GregorianCalendar gc = new GregorianCalendar();
			gc.setTimeInMillis((long) d);
			return dateFactory.newXMLGregorianCalendar(gc).toXMLFormat().substring(0, 23);
		}
		catch (DatatypeConfigurationException ex) {
			Exceptions.printStackTrace(ex);
			return "";
		}
	}

	/**
	 * Returns a new {@code DynamicType} instance that contains a given
	 * {@code Interval} in.
	 *
	 * @param in interval to add (could be null)
	 *
	 * @return a new {@code DynamicType} instance that contains a given
	 *         {@code Interval} in.
	 */
	public static DynamicType createDynamicObject(AttributeType type, Interval in) {
		return createDynamicObject(type, null, in);
	}

	/**
	 * Returns a new {@code DynamicType} instance with intervals given by
	 * {@code List<Interval>} in.
	 *
	 * @param in intervals to add (could be null)
	 *
	 * @return a new {@code DynamicType} instance with intervals given by
	 *         {@code List<Interval>} in.
	 */
	public static DynamicType createDynamicObject(AttributeType type, List<Interval> in) {
		return createDynamicObject(type, null, in);
	}

	/**
	 * Returns a shallow copy of {@code source}.
	 *
	 * @param source an object to copy from (could be null, then completely new
	 *               instance is created)
	 *
	 * @return a shallow copy of {@code source}.
	 */
	public static DynamicType createDynamicObject(AttributeType type, DynamicType source) {
		return createDynamicObject(type, source, (Interval)null, (Interval)null);
	}

	/**
	 * Returns a shallow copy of {@code source} that contains a given
	 * {@code Interval} in.
	 *
	 * @param source an object to copy from (could be null, then completely new
	 *               instance is created)
	 * @param in     interval to add (could be null)
	 *
	 * @return a shallow copy of {@code source} that contains a given
	 *         {@code Interval} in.
	 */
	public static DynamicType createDynamicObject(AttributeType type, DynamicType source, Interval in) {
		return createDynamicObject(type, source, in, null);
	}

	/**
	 * Returns a shallow copy of {@code source} that contains a given
	 * {@code Interval} in. Before add it removes from the newly created
	 * object all intervals that overlap with a given {@code Interval} out.
	 *
	 * @param source an object to copy from (could be null, then completely new
	 *               instance is created)
	 * @param in     interval to add (could be null)
	 * @param out    interval to remove (could be null)
	 *
	 * @return a shallow copy of {@code source} that contains a given
	 *         {@code Interval} in. Before add it removes from the newly created
	 *         object all intervals that overlap with a given {@code Interval} out.
	 */
	public static DynamicType createDynamicObject(AttributeType type, DynamicType source, Interval in, Interval out) {
		ArrayList<Interval> lin  = null;
		ArrayList<Interval> lout = null;

		if (in != null) {
			lin = new ArrayList<Interval>();
			lin.add(in);
		}
		if (out != null) {
			lout = new ArrayList<Interval>();
			lout.add(out);
		}

		return createDynamicObject(type, source, lin, lout);
	}

	/**
	 * Returns a shallow copy of {@code source} with additional intervals
	 * given by {@code List<Interval>} in.
	 *
	 * @param source an object to copy from (could be null, then completely new
	 *               instance is created)
	 * @param in     intervals to add (could be null)
	 *
	 * @return a shallow copy of {@code source} with additional intervals
	 *         given by {@code List<Interval>} in.
	 */
	public static DynamicType createDynamicObject(AttributeType type, DynamicType source, List<Interval> in) {
		return createDynamicObject(type, source, in, null);
	}

	/**
	 * Returns a shallow copy of {@code source} with additional intervals
	 * given by {@code List<Interval>} in. Before add it removes from the
	 * newly created object all intervals that overlap with intervals given by
	 * {@code List<Interval>} out.
	 * <p>
	 * It can return {@code null} if type is not dynamic.
	 *
	 * @param source an object to copy from (could be null, then completely new
	 *               instance is created)
	 * @param in     intervals to add (could be null)
	 * @param out    intervals to remove (could be null)
	 *
	 * @return a shallow copy of {@code source} with additional intervals
	 *         given by {@code List<Interval>} in. Before add it removes from the
	 *         newly created object all intervals that overlap with intervals given by
	 *         {@code List<Interval>} out. It can return {@code null} if type
	 *         is not dynamic.
	 */
	public static DynamicType createDynamicObject(AttributeType type, DynamicType source, List<Interval> in,
			List<Interval> out) {
		if (!type.isDynamicType())
			return null;

		switch (type) {
			case DYNAMIC_BYTE: {
				ArrayList<Interval<Byte>> lin = null;
				if (in != null) {
					lin = new ArrayList<Interval<Byte>>();
					for (Interval interval : in)
						lin.add(new Interval<Byte>(interval.getLow(), interval.getHigh(), (Byte)interval.getValue()));
				}
				ArrayList<Interval<Byte>> lout = null;
				if (out != null) {
					lout = new ArrayList<Interval<Byte>>();
					for (Interval interval : out)
						lout.add(new Interval<Byte>(interval.getLow(), interval.getHigh(), (Byte)interval.getValue()));
				}
				return new DynamicByte((DynamicByte)source, lin, lout);
			}
			case DYNAMIC_SHORT: {
				ArrayList<Interval<Short>> lin = null;
				if (in != null) {
					lin = new ArrayList<Interval<Short>>();
					for (Interval interval : in)
						lin.add(new Interval<Short>(interval.getLow(), interval.getHigh(),
							(Short)interval.getValue()));
				}
				ArrayList<Interval<Short>> lout = null;
				if (out != null) {
					lout = new ArrayList<Interval<Short>>();
					for (Interval interval : out)
						lout.add(new Interval<Short>(interval.getLow(), interval.getHigh(),
							(Short)interval.getValue()));
				}
				return new DynamicShort((DynamicShort)source, lin, lout);
			}
			case DYNAMIC_INT: {
				ArrayList<Interval<Integer>> lin = null;
				if (in != null) {
					lin = new ArrayList<Interval<Integer>>();
					for (Interval interval : in)
						lin.add(new Interval<Integer>(interval.getLow(), interval.getHigh(),
							(Integer)interval.getValue()));
				}
				ArrayList<Interval<Integer>> lout = null;
				if (out != null) {
					lout = new ArrayList<Interval<Integer>>();
					for (Interval interval : out)
						lout.add(new Interval<Integer>(interval.getLow(), interval.getHigh(),
							(Integer)interval.getValue()));
				}
				return new DynamicInteger((DynamicInteger)source, lin, lout);
			}
			case DYNAMIC_LONG: {
				ArrayList<Interval<Long>> lin = null;
				if (in != null) {
					lin = new ArrayList<Interval<Long>>();
					for (Interval interval : in)
						lin.add(new Interval<Long>(interval.getLow(), interval.getHigh(), (Long)interval.getValue()));
				}
				ArrayList<Interval<Long>> lout = null;
				if (out != null) {
					lout = new ArrayList<Interval<Long>>();
					for (Interval interval : out)
						lout.add(new Interval<Long>(interval.getLow(), interval.getHigh(), (Long)interval.getValue()));
				}
				return new DynamicLong((DynamicLong)source, lin, lout);
			}
			case DYNAMIC_FLOAT: {
				ArrayList<Interval<Float>> lin = null;
				if (in != null) {
					lin = new ArrayList<Interval<Float>>();
					for (Interval interval : in)
						lin.add(new Interval<Float>(interval.getLow(), interval.getHigh(), (Float)interval.getValue()));
				}
				ArrayList<Interval<Float>> lout = null;
				if (out != null) {
					lout = new ArrayList<Interval<Float>>();
					for (Interval interval : out)
						lout.add(new Interval<Float>(interval.getLow(), interval.getHigh(),
							(Float)interval.getValue()));
				}
				return new DynamicFloat((DynamicFloat)source, lin, lout);
			}
			case DYNAMIC_DOUBLE: {
				ArrayList<Interval<Double>> lin = null;
				if (in != null) {
					lin = new ArrayList<Interval<Double>>();
					for (Interval interval : in)
						lin.add(new Interval<Double>(interval.getLow(), interval.getHigh(),
							(Double)interval.getValue()));
				}
				ArrayList<Interval<Double>> lout = null;
				if (out != null) {
					lout = new ArrayList<Interval<Double>>();
					for (Interval interval : out)
						lout.add(new Interval<Double>(interval.getLow(), interval.getHigh(),
							(Double)interval.getValue()));
				}
				return new DynamicDouble((DynamicDouble)source, lin, lout);
			}
			case DYNAMIC_BOOLEAN: {
				ArrayList<Interval<Boolean>> lin = null;
				if (in != null) {
					lin = new ArrayList<Interval<Boolean>>();
					for (Interval interval : in)
						lin.add(new Interval<Boolean>(interval.getLow(), interval.getHigh(),
							(Boolean)interval.getValue()));
				}
				ArrayList<Interval<Boolean>> lout = null;
				if (out != null) {
					lout = new ArrayList<Interval<Boolean>>();
					for (Interval interval : out)
						lout.add(new Interval<Boolean>(interval.getLow(), interval.getHigh(),
							(Boolean)interval.getValue()));
				}
				return new DynamicBoolean((DynamicBoolean)source, lin, lout);
			}
			case DYNAMIC_CHAR: {
				ArrayList<Interval<Character>> lin = null;
				if (in != null) {
					lin = new ArrayList<Interval<Character>>();
					for (Interval interval : in)
						lin.add(new Interval<Character>(interval.getLow(), interval.getHigh(),
							(Character)interval.getValue()));
				}
				ArrayList<Interval<Character>> lout = null;
				if (out != null) {
					lout = new ArrayList<Interval<Character>>();
					for (Interval interval : out)
						lout.add(new Interval<Character>(interval.getLow(), interval.getHigh(),
							(Character)interval.getValue()));
				}
				return new DynamicCharacter((DynamicCharacter)source, lin, lout);
			}
			case DYNAMIC_STRING: {
				ArrayList<Interval<String>> lin = null;
				if (in != null) {
					lin = new ArrayList<Interval<String>>();
					for (Interval interval : in)
						lin.add(new Interval<String>(interval.getLow(), interval.getHigh(),
							(String)interval.getValue()));
				}
				ArrayList<Interval<String>> lout = null;
				if (out != null) {
					lout = new ArrayList<Interval<String>>();
					for (Interval interval : out)
						lout.add(new Interval<String>(interval.getLow(), interval.getHigh(),
							(String)interval.getValue()));
				}
				return new DynamicString((DynamicString)source, lin, lout);
			}
			case DYNAMIC_BIGINTEGER: {
				ArrayList<Interval<BigInteger>> lin = null;
				if (in != null) {
					lin = new ArrayList<Interval<BigInteger>>();
					for (Interval interval : in)
						lin.add(new Interval<BigInteger>(interval.getLow(), interval.getHigh(),
							(BigInteger)interval.getValue()));
				}
				ArrayList<Interval<BigInteger>> lout = null;
				if (out != null) {
					lout = new ArrayList<Interval<BigInteger>>();
					for (Interval interval : out)
						lout.add(new Interval<BigInteger>(interval.getLow(), interval.getHigh(),
							(BigInteger)interval.getValue()));
				}
				return new DynamicBigInteger((DynamicBigInteger)source, lin, lout);
			}
			case DYNAMIC_BIGDECIMAL: {
				ArrayList<Interval<BigDecimal>> lin = null;
				if (in != null) {
					lin = new ArrayList<Interval<BigDecimal>>();
					for (Interval interval : in)
						lin.add(new Interval<BigDecimal>(interval.getLow(), interval.getHigh(),
							(BigDecimal)interval.getValue()));
				}
				ArrayList<Interval<BigDecimal>> lout = null;
				if (out != null) {
					lout = new ArrayList<Interval<BigDecimal>>();
					for (Interval interval : out)
						lout.add(new Interval<BigDecimal>(interval.getLow(), interval.getHigh(),
							(BigDecimal)interval.getValue()));
				}
				return new DynamicBigDecimal((DynamicBigDecimal)source, lin, lout);
			}
			case TIME_INTERVAL: {
				ArrayList<Double[]> lin = null;
				if (in != null) {
					lin = new ArrayList<Double[]>();
					for (Interval interval : in)
						lin.add(new Double[] { interval.getLow(), interval.getHigh() });
				}
				ArrayList<Double[]> lout = null;
				if (out != null) {
					lout = new ArrayList<Double[]>();
					for (Interval interval : out)
						lout.add(new Double[] { interval.getLow(), interval.getHigh() });
				}
				return new TimeInterval((TimeInterval)source, lin, lout);
			}
			default:
				return null;
		}
	}

	/**
	 * It checks intervals of the {@code source} and make it fit to the given interval,
	 * possibly removing intervals out of the window and
	 * changing low or high of intervals to fit.
	 *
	 * @param source a {@code DynamicType} to be performed
	 * @param interval a given interval
	 *
	 * @return a fitted {@code DynamicType} instance.
	 *
	 * @throws NullPointerException if {@code source} is null.
	 */
	public static DynamicType fitToInterval(DynamicType source, Interval interval) {
		if (source == null)
			throw new NullPointerException("The source cannot be null.");
		
		List<Interval> sIntervals = source.getIntervals(interval);
		List<Interval> tIntervals = new ArrayList<Interval>();
		for (Interval i : sIntervals) {
			double  iLow   = i.getLow();
			double  iHigh  = i.getHigh();
			boolean ilopen = i.isLowExcluded();
			boolean iropen = i.isHighExcluded();
			if (i.getLow() < interval.getLow())
				iLow = interval.getLow();
			if (i.getHigh() > interval.getHigh())
				iHigh = interval.getHigh();
			if (interval.isLowExcluded())
				ilopen = true;
			if (interval.isHighExcluded())
				iropen = true;
			tIntervals.add(new Interval(iLow, iHigh, ilopen, iropen, i.getValue()));
		}

		return createDynamicObject(AttributeType.parse(source), tIntervals);
	}

	/**
	 * It checks intervals of the {@code source} and make it fit to the given interval
	 * [{@code low}, {@code high}], possibly removing intervals out of the window and
	 * changing low or high of intervals to fit.
	 *
	 * @param source a {@code DynamicType} to be performed
	 * @param low    the left endpoint
	 * @param high   the right endpoint
	 *
	 * @return a fitted {@code DynamicType} instance.
	 *
	 * @throws NullPointerException     if {@code source} is null.
	 * @throws IllegalArgumentException if {@code low} > {@code high}.
	 */
	public static DynamicType fitToInterval(DynamicType source, double low, double high) {
		return fitToInterval(source, new Interval(low, high));
	}
}
