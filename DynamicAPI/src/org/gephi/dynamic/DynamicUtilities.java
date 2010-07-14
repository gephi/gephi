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

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

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
	 * @throws DatatypeConfigurationException if the implementation of {@code DatatypeFactory}
	 *                                        is not available or cannot be instantiated.
	 * @throws IllegalArgumentException       if {@code str} is not a valid {@code XMLGregorianCalendar}.
	 * @throws NullPointerException           if {@code str} is null.
	 */
	public static double getDoubleFromXMLDateString(String str) throws DatatypeConfigurationException {
		DatatypeFactory dateFactory = DatatypeFactory.newInstance();
		return dateFactory.newXMLGregorianCalendar(str).toGregorianCalendar().getTimeInMillis();
	}
	
	/**
	 * Used for export (writes XML date strings).
	 * 
	 * @param d a double to convert from
	 *
	 * @return an XML date string.
	 *
	 * @throws DatatypeConfigurationException if the implementation of {@code DatatypeFactory}
	 *                                        is not available or cannot be instantiated.
	 * @throws IllegalArgumentException       if {@code d} is infinite.
	 */
	public static String getXMLDateStringFromDouble(double d) throws DatatypeConfigurationException {
		DatatypeFactory dateFactory = DatatypeFactory.newInstance();
		if (Double.isInfinite(d))
			throw new IllegalArgumentException("The passed double cannot be infinite.");
		Duration             dur = dateFactory.newDuration((long)d);
		XMLGregorianCalendar xgc = dateFactory.newXMLGregorianCalendar();
		xgc.add(dur);
		return xgc.toXMLFormat();
	}
}
