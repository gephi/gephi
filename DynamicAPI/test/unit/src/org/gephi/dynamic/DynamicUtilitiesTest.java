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

import org.gephi.data.attributes.type.DynamicDouble;
import javax.xml.datatype.DatatypeConfigurationException;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.type.DynamicType;
import org.gephi.data.attributes.type.Interval;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit test for DynamicUtilities class.
 *
 * @author Cezary Bartosiak
 */
public class DynamicUtilitiesTest {
	public DynamicUtilitiesTest() { }

	@BeforeClass
	public static void setUpClass() throws Exception { }

	@AfterClass
	public static void tearDownClass() throws Exception { }

	@Before
	public void setUp() { }

	@After
	public void tearDown() { }

	@Test
	public void testCalendar() {
		System.out.println("testCalendar()");
		String date = "2000-01-01";
		try {
			double d     = DynamicUtilities.getDoubleFromXMLDateString(date);
			String date2 = DynamicUtilities.getXMLDateStringFromDouble(d);
			assertEquals(date, date2);
		}
		catch (DatatypeConfigurationException e) {
			fail(e.getMessage());
		}
		System.out.println();
	}

	@Test
	public void testCreateDynamicObjectMethods() {
		System.out.println("createDynamicObject(AttributeType, Interval)");
		AttributeType    type      = AttributeType.DYNAMIC_DOUBLE;
		Interval<Double> in        = new Interval(1.0, 2.0, 3.0);
		DynamicType      result    = DynamicUtilities.createDynamicObject(type, in);
		DynamicType      expResult = new DynamicDouble(new Interval<Double>(1.0, 2.0, 3.0));
		assertEquals(expResult, result);
		System.out.println("result:    " + result.toString());
		System.out.println("expResult: " + expResult.toString());
		System.out.println();
	}

	@Test
	public void testFitToInterval() {
		System.out.println("fitToInterval(DynamicType, double, double)");
		DynamicType expResult = new DynamicDouble(new Interval<Double>(1.0, 2.0, 0.0));
		DynamicType result    = DynamicUtilities.fitToInterval(
				new DynamicDouble(new Interval<Double>(1.0, 3.0, 0.0)),
				1.0, 2.0);
		assertEquals(expResult, result);
		System.out.println("result:    " + result.toString());
		System.out.println("expResult: " + expResult.toString());
		System.out.println();
	}
}