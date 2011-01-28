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
import java.util.List;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.api.Estimator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit test for DynamicType class.
 *
 * @author Cezary Bartosiak
 */
public class DynamicTypeTest {
	public DynamicTypeTest() { }

	@BeforeClass
	public static void setUpClass() throws Exception { }

	@AfterClass
	public static void tearDownClass() throws Exception { }

	@Before
	public void setUp() { }

	@After
	public void tearDown() { }

	@Test
	public void testCopyConstructors() {
		System.out.println("copyConstructors");
		DynamicDouble expected1 = makeTree1();
		DynamicDouble instance1 = makeTree1();
		DynamicDouble expected2 = makeTree1_in();
		DynamicDouble instance2 = new DynamicDouble(makeTree1(),
				new Interval<Double>(-2.0, -1.0, 10.0));
		DynamicDouble expected3 = makeTree1_out();
		DynamicDouble instance3 = new DynamicDouble(makeTree1(),
				null,
				new Interval<Double>(0.0, 30.0));
		DynamicDouble expected4 = makeTree1_in_out();
		DynamicDouble instance4 = new DynamicDouble(makeTree1(),
				new Interval<Double>(-2.0, -1.0, 10.0),
				new Interval<Double>(0.0, 30.0));
		assertEquals(expected1, instance1);
		assertEquals(expected2, instance2);
		assertEquals(expected3, instance3);
		assertEquals(expected4, instance4);
		System.out.println("instance1: " + instance1);
		System.out.println("instance2: " + instance2);
		System.out.println("instance3: " + instance3);
		System.out.println("instance4: " + instance4);
		System.out.println();
	}

	@Test
	public void testGetLow() {
		System.out.println("getLow()");
		DynamicDouble instance1 = makeTree1();
		DynamicDouble instance2 = new DynamicDouble();
		Double expResult1 = 0.0;
		Double result1    = instance1.getLow();
		Double expResult2 = Double.NEGATIVE_INFINITY;
		Double result2    = instance2.getLow();
		assertEquals(expResult1, result1);
		assertEquals(expResult2, result2);
		System.out.println("expResult1: " + expResult1);
		System.out.println("result1:    " + result1);
		System.out.println("expResult2: " + expResult2);
		System.out.println("result2:    " + result2);
		System.out.println();
	}

	@Test
	public void testGetHigh() {
		System.out.println("getHigh()");
		DynamicDouble instance1 = makeTree1();
		DynamicDouble instance2 = new DynamicDouble();
		Double expResult1 = 30.0;
		Double result1    = instance1.getHigh();
		Double expResult2 = Double.POSITIVE_INFINITY;
		Double result2    = instance2.getHigh();
		assertEquals(expResult1, result1);
		assertEquals(expResult2, result2);
		System.out.println("expResult1: " + expResult1);
		System.out.println("result1:    " + result1);
		System.out.println("expResult2: " + expResult2);
		System.out.println("result2:    " + result2);
		System.out.println();
	}

	@Test
	public void testIsInRange() {
		System.out.println("isInRange()");
		DynamicDouble instance = makeTree1();
		Boolean expResult1 = false;
		Boolean result1    = instance.isInRange(11.0, 14.0);
		Boolean expResult2 = true;
		Boolean result2    = instance.isInRange(9.0, 12.0);
		assertEquals(expResult1, result1);
		assertEquals(expResult2, result2);
		System.out.println("expResult1: " + expResult1);
		System.out.println("result1:    " + result1);
		System.out.println("expResult2: " + expResult2);
		System.out.println("result2:    " + result2);
		System.out.println();
	}

	@Test
	public void testGetValue_0args() {
		System.out.println("getValue()");
		DynamicDouble instance = makeTree1();
		Double value1 = instance.getValue();
		Double value2 = 0.0;
		assertEquals(value1, value2);
		System.out.println("value1: " + value1);
		System.out.println("value2: " + value2);
		System.out.println();
	}

	@Test
	public void testGetValue_double_double() {
		System.out.println("getValue(double, double)");
		DynamicDouble instance = makeTree1();
		Double value1 = instance.getValue(14, 16);
		Double value2 = 4.0;
		assertEquals(value1, value2);
		System.out.println("value1: " + value1);
		System.out.println("value2: " + value2);
		System.out.println();
	}

	@Test
	public void testGetValue_Estimator() {
		System.out.println("getValue(Estimator)");
		DynamicDouble    dInstance = makeTree1();
		DynamicInteger   nInstance = makeTreeInteger();
		DynamicBoolean   bInstance = makeTreeBoolean();
		DynamicCharacter cInstance = makeTreeCharacter();
		DynamicString    sInstance = makeTreeString();
		Double    valueRealAverage1    = dInstance.getValue(Estimator.AVERAGE);
		Double    valueRealAverage2    = 4.5;
		Double    valueRealMedian1     = dInstance.getValue(Estimator.MEDIAN);
		Double    valueRealMedian2     = 4.5;
		Integer   valueIntegerAverage1 = nInstance.getValue(Estimator.AVERAGE);
		Integer   valueIntegerAverage2 = 4;
		Integer   valueIntegerMedian1  = nInstance.getValue(Estimator.MEDIAN);
		Integer   valueIntegerMedian2  = 4;
		Integer   valueIntegerSum1     = nInstance.getValue(Estimator.SUM);
		Integer   valueIntegerSum2     = 45;
		Integer   valueIntegerMin1     = nInstance.getValue(Estimator.MIN);
		Integer   valueIntegerMin2     = 0;
		Integer   valueIntegerMax1     = nInstance.getValue(Estimator.MAX);
		Integer   valueIntegerMax2     = 9;
		Boolean   valueBooleanMin1     = bInstance.getValue(Estimator.MIN);
		Boolean   valueBooleanMin2     = false;
		Boolean   valueBooleanMax1     = bInstance.getValue(Estimator.MAX);
		Boolean   valueBooleanMax2     = true;
		Character valueCharacterMin1   = cInstance.getValue(Estimator.MIN);
		Character valueCharacterMin2   = '0';
		Character valueCharacterMax1   = cInstance.getValue(Estimator.MAX);
		Character valueCharacterMax2   = '9';
		String    valueStringMedian1   = sInstance.getValue(Estimator.MEDIAN);
		String    valueStringMedian2   = "5";
		String    valueStringMode1     = sInstance.getValue(Estimator.MODE);
		String    valueStringMode2     = "0_repeat";
		String    valueStringMin1      = sInstance.getValue(Estimator.MIN);
		String    valueStringMin2      = "0";
		String    valueStringMax1      = sInstance.getValue(Estimator.MAX);
		String    valueStringMax2      = "9";
		String    valueStringFirst1    = sInstance.getValue(Estimator.FIRST);
		String    valueStringFirst2    = "0";
		String    valueStringLast1     = sInstance.getValue(Estimator.LAST);
		String    valueStringLast2     = "0_repeat";
		assertEquals(valueRealAverage1,    valueRealAverage2);
		assertEquals(valueRealMedian1,     valueRealMedian2);
		assertEquals(valueIntegerAverage1, valueIntegerAverage2);
		assertEquals(valueIntegerMedian1,  valueIntegerMedian2);
		assertEquals(valueIntegerSum1,     valueIntegerSum2);
		assertEquals(valueIntegerMin1,     valueIntegerMin2);
		assertEquals(valueIntegerMax1,     valueIntegerMax2);
		assertEquals(valueBooleanMin1,     valueBooleanMin2);
		assertEquals(valueBooleanMax1,     valueBooleanMax2);
		assertEquals(valueCharacterMin1,   valueCharacterMin2);
		assertEquals(valueCharacterMax1,   valueCharacterMax2);
		assertEquals(valueStringMedian1,   valueStringMedian2);
		assertEquals(valueStringMode1,     valueStringMode2);
		assertEquals(valueStringMin1,      valueStringMin2);
		assertEquals(valueStringMax1,      valueStringMax2);
		assertEquals(valueStringFirst1,    valueStringFirst2);
		assertEquals(valueStringLast1,     valueStringLast2);
		System.out.println("valueRealAverage1:    " + valueRealAverage1);
		System.out.println("valueRealAverage2:    " + valueRealAverage2);
		System.out.println("valueRealMedian1:     " + valueRealMedian1);
		System.out.println("valueRealMedian2:     " + valueRealMedian2);
		System.out.println("valueIntegerAverage1: " + valueIntegerAverage1);
		System.out.println("valueIntegerAverage2: " + valueIntegerAverage2);
		System.out.println("valueIntegerMedian1:  " + valueIntegerMedian1);
		System.out.println("valueIntegerMedian2:  " + valueIntegerMedian2);
		System.out.println("valueIntegerSum1:     " + valueIntegerSum1);
		System.out.println("valueIntegerSum2:     " + valueIntegerSum2);
		System.out.println("valueIntegerMin1:     " + valueIntegerMin1);
		System.out.println("valueIntegerMin2:     " + valueIntegerMin2);
		System.out.println("valueIntegerMax1:     " + valueIntegerMax1);
		System.out.println("valueIntegerMax2:     " + valueIntegerMax2);
		System.out.println("valueBooleanMin1:     " + valueBooleanMin1);
		System.out.println("valueBooleanMin2:     " + valueBooleanMin2);
		System.out.println("valueBooleanMax1:     " + valueBooleanMax1);
		System.out.println("valueBooleanMax2:     " + valueBooleanMax2);
		System.out.println("valueCharacterMin1:   " + valueCharacterMin1);
		System.out.println("valueCharacterMin2:   " + valueCharacterMin2);
		System.out.println("valueCharacterMax1:   " + valueCharacterMax1);
		System.out.println("valueCharacterMax2:   " + valueCharacterMax2);
		System.out.println("valueStringMedian1:   " + valueStringMedian1);
		System.out.println("valueStringMedian2:   " + valueStringMedian2);
		System.out.println("valueStringMode1:     " + valueStringMode1);
		System.out.println("valueStringMode2:     " + valueStringMode2);
		System.out.println("valueStringMin1:      " + valueStringMin1);
		System.out.println("valueStringMin2:      " + valueStringMin2);
		System.out.println("valueStringMax1:      " + valueStringMax1);
		System.out.println("valueStringMax2:      " + valueStringMax2);
		System.out.println("valueStringFirst1:    " + valueStringFirst1);
		System.out.println("valueStringFirst2:    " + valueStringFirst2);
		System.out.println("valueStringLast1:     " + valueStringLast1);
		System.out.println("valueStringLast2:     " + valueStringLast2);
		System.out.println();
	}

	@Test
	public void testGetValue_3args() {
		System.out.println("getValue(double, double, Estimator)");
		DynamicDouble instance = makeTree1();
		Double valueRealAverage1 = instance.getValue(1, 9, Estimator.AVERAGE);
		Double valueRealAverage2 = 1.5;
		Double valueRealSum1     = instance.getValue(1, 9, Estimator.SUM);
		Double valueRealSum2     = 6.0;
		assertEquals(valueRealAverage1, valueRealAverage2);
		assertEquals(valueRealSum1,     valueRealSum2);
		System.out.println("valueRealAverage1: " + valueRealAverage1);
		System.out.println("valueRealAverage2: " + valueRealAverage2);
		System.out.println("valueRealSum1:     " + valueRealSum1);
		System.out.println("valueRealSum2:     " + valueRealSum2);
		System.out.println();
	}

	@Test
	public void testGetValues_0args() {
		System.out.println("getValues()");
		DynamicDouble instance = makeTree3();
		List<Double> values1 = instance.getValues();
		List<Double> values2 = new ArrayList<Double>();
		for (int i = 0; i < 4; ++i)
			values2.add((double)i);
		assertEquals(values1, values2);
		System.out.println("values1: " + values1);
		System.out.println("values2: " + values2);
		System.out.println();
	}

	@Test
	public void testGetValues_double_double() {
		System.out.println("getValues(double, double)");
		DynamicDouble instance = makeTree1();
		List<Double> values11 = instance.getValues(
				Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		List<Double> values12 = new ArrayList<Double>();
		for (int i = 0; i < 10; ++i)
			values12.add((double)i);
		List<Double> values21 = instance.getValues(0, 9);
		List<Double> values22 = new ArrayList<Double>();
		for (int i = 0; i < 4; ++i)
			values22.add((double)i);
		List<Double> values31 = instance.getValues(14, 19);
		List<Double> values32 = new ArrayList<Double>();
		for (int i = 4; i < 8; ++i)
			values32.add((double)i);
		List<Double> values41 = instance.getValues(24, 31);
		List<Double> values42 = new ArrayList<Double>();
		for (int i = 8; i < 10; ++i)
			values42.add((double)i);
		assertEquals(values11, values12);
		assertEquals(values21, values22);
		assertEquals(values31, values32);
		assertEquals(values41, values42);
		System.out.println("values11: " + values11);
		System.out.println("values12: " + values12);
		System.out.println("values21: " + values21);
		System.out.println("values22: " + values22);
		System.out.println("values31: " + values31);
		System.out.println("values32: " + values32);
		System.out.println("values41: " + values41);
		System.out.println("values42: " + values42);
		System.out.println();
	}

	@Test
	public void testGetIntervals() {
		System.out.println("getIntervals(double, double)");
		DynamicDouble instance = makeTree1();
		List<Interval<Double>> list = instance.getIntervals(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		String result = "<empty>";
		if (!list.isEmpty()) {
			StringBuilder sb = new StringBuilder("<");
			sb.append(list.get(0).toString());
			for (int i = 1; i < list.size(); ++i)
				sb.append("; ").append(list.get(i).toString());
			sb.append(">");
			result = sb.toString();
		}
		String expResult = instance.toString();
		assertEquals(result, expResult);
		System.out.println("result:    " + result);
		System.out.println("expResult: " + expResult);
		System.out.println();
	}

	@Test
	public void testGetUnderlyingType() {
		System.out.println("getUnderlyingType");
		DynamicType dInstance = makeTree1();
		DynamicType nInstance = makeTreeInteger();
		DynamicType bInstance = makeTreeBoolean();
		DynamicType cInstance = makeTreeCharacter();
		DynamicType sInstance = makeTreeString();
		assertEquals(dInstance.getUnderlyingType(), Double.class);
		assertEquals(nInstance.getUnderlyingType(), Integer.class);
		assertEquals(bInstance.getUnderlyingType(), Boolean.class);
		assertEquals(cInstance.getUnderlyingType(), Character.class);
		assertEquals(sInstance.getUnderlyingType(), String.class);
		System.out.println("dInstance: " + dInstance.getUnderlyingType());
		System.out.println("nInstance: " + nInstance.getUnderlyingType());
		System.out.println("bInstance: " + bInstance.getUnderlyingType());
		System.out.println("cInstance: " + cInstance.getUnderlyingType());
		System.out.println("sInstance: " + sInstance.getUnderlyingType());
		System.out.println();
	}

	@Test
	public void testEquals() {
		System.out.println("equals(Object)");
		DynamicDouble instance1 = makeTree1();
		DynamicDouble instance2 = makeTree1();
		DynamicDouble instance3 = makeTree2();
		DynamicDouble instance4 = makeTree3();
		boolean expResult1 = true;
		boolean result1    = instance1.equals(instance1);
		boolean expResult2 = true;
		boolean result2    = instance1.equals(instance2);
		boolean expResult3 = false;
		boolean result3    = instance2.equals(instance3);
		boolean expResult4 = false;
		boolean result4    = instance3.equals(instance4);
		assertEquals(expResult1, result1);
		assertEquals(expResult2, result2);
		assertEquals(expResult3, result3);
		assertEquals(expResult4, result4);
		System.out.println();
	}

	@Test
	public void testHashCode() {
		System.out.println("hashCode()");
		DynamicDouble instance1 = makeTree1();
		DynamicDouble instance2 = makeTree1();
		assertEquals(instance1.hashCode(), instance1.hashCode());
		assertEquals(instance1.hashCode(), instance2.hashCode());
		System.out.println("instance1.hashcode(): " + instance1.hashCode());
		System.out.println("instance2.hashcode(): " + instance2.hashCode());
		System.out.println();
	}

	@Test
	public void testToString() {
		System.out.println("toString()");
		DynamicDouble instance  = makeELboundsTree();
		StringBuilder expResult = new StringBuilder("<");
		expResult.append("[0.1, 0.2, 1.0); ");
		expResult.append("[0.2, 0.3, 2.0); ");
		expResult.append("(0.3, 0.4, 3.0)");
		expResult.append(">");
		String result = instance.toString();
		assertEquals(expResult.toString(), result);
		System.out.println("expResult: " + expResult);
		System.out.println("result:    " + result);
		System.out.println();
	}   

	@Test
	public void testDeserialization() {
		System.out.println("deserialization");
		DynamicDouble instance1 = makeELboundsTree();
		DynamicDouble instance2 = (DynamicDouble)AttributeType.DYNAMIC_DOUBLE.parse(instance1.toString());
		DynamicDouble instance3 = new DynamicDouble();
		DynamicDouble instance4 = (DynamicDouble)AttributeType.DYNAMIC_DOUBLE.parse(instance3.toString());
		TimeInterval  instance5 = makeTimeInterval();
		TimeInterval  instance6 = (TimeInterval)AttributeType.TIME_INTERVAL.parse(instance5.toString());
		assertEquals(instance1, instance2);
		assertEquals(instance3, instance4);
		assertEquals(instance5, instance6);
		System.out.println("instance1: " + instance1);
		System.out.println("instance2: " + instance2);
		System.out.println("instance3: " + instance3);
		System.out.println("instance4: " + instance4);
		System.out.println("instance5: " + instance5);
		System.out.println("instance6: " + instance6);
		System.out.println();
	}

	@Test
	public void testOrder() {
		DynamicInteger instance = new DynamicInteger();
		instance = new DynamicInteger(instance, new Interval<Integer>(2009, 2010, 1));
		instance = new DynamicInteger(instance, new Interval<Integer>(2006, 2007, 2));
		instance = new DynamicInteger(instance, new Interval<Integer>(2001, 2002, 3));

		List<Interval<Integer>> intervals = instance.getIntervals(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		assertEquals(new Interval<Integer>(2001, 2002, 3), intervals.get(0));
		assertEquals(new Interval<Integer>(2006, 2007, 2), intervals.get(1));
		assertEquals(new Interval<Integer>(2009, 2010, 1), intervals.get(2));
	}

	private TimeInterval makeTimeInterval() {
		List<Interval> intervals = new ArrayList<Interval>();
		intervals.add(new Interval<Double>(0.1, 0.2, false, true));
		intervals.add(new Interval<Double>(0.2, 0.3, false, true));
		intervals.add(new Interval<Double>(0.3, 0.4, true,  true));
		return new TimeInterval(intervals);
	}

	private DynamicDouble makeELboundsTree() {
		List<Interval<Double>> intervals = new ArrayList<Interval<Double>>();
		intervals.add(new Interval<Double>(0.1, 0.2, false, true, 1.0));
		intervals.add(new Interval<Double>(0.2, 0.3, false, true, 2.0));
		intervals.add(new Interval<Double>(0.3, 0.4, true,  true, 3.0));
		return new DynamicDouble(intervals);
	}

	private DynamicDouble makeTree1() {
		List<Interval<Double>> intervals = new ArrayList<Interval<Double>>();
		intervals.add(new Interval<Double>(26.0, 26.0, 9.0));
		intervals.add(new Interval<Double>(0.0,  3.0,  0.0));
		intervals.add(new Interval<Double>(6.0,  10.0, 2.0));
		intervals.add(new Interval<Double>(5.0,  8.0,  1.0));
		intervals.add(new Interval<Double>(8.0,  9.0,  3.0));
		intervals.add(new Interval<Double>(17.0, 19.0, 6.0));
		intervals.add(new Interval<Double>(16.0, 21.0, 5.0));
		intervals.add(new Interval<Double>(15.0, 23.0, 4.0));
		intervals.add(new Interval<Double>(25.0, 30.0, 8.0));
		intervals.add(new Interval<Double>(19.0, 20.0, 7.0));
		return new DynamicDouble(intervals);
	}

	private DynamicDouble makeTree1_in() {
		List<Interval<Double>> intervals = new ArrayList<Interval<Double>>();
		intervals.add(new Interval<Double>( 26.0,  26.0, 9.0));
		intervals.add(new Interval<Double>( 0.0,   3.0,  0.0));
		intervals.add(new Interval<Double>( 6.0,   10.0, 2.0));
		intervals.add(new Interval<Double>( 5.0,   8.0,  1.0));
		intervals.add(new Interval<Double>( 8.0,   9.0,  3.0));
		intervals.add(new Interval<Double>( 17.0,  19.0, 6.0));
		intervals.add(new Interval<Double>( 16.0,  21.0, 5.0));
		intervals.add(new Interval<Double>( 15.0,  23.0, 4.0));
		intervals.add(new Interval<Double>( 25.0,  30.0, 8.0));
		intervals.add(new Interval<Double>( 19.0,  20.0, 7.0));
		intervals.add(new Interval<Double>(-2.0,  -1.0,  10.0));
		return new DynamicDouble(intervals);
	}

	private DynamicDouble makeTree1_out() {
		List<Interval<Double>> intervals = new ArrayList<Interval<Double>>();
		return new DynamicDouble(intervals);
	}

	private DynamicDouble makeTree1_in_out() {
		List<Interval<Double>> intervals = new ArrayList<Interval<Double>>();
		intervals.add(new Interval<Double>(-2.0, -1.0, 10.0));
		return new DynamicDouble(intervals);
	}

	private DynamicDouble makeTree2() {
		List<Interval<Double>> intervals = new ArrayList<Interval<Double>>();
		intervals.add(new Interval<Double>(3.0,  7.0,  0.0));
		intervals.add(new Interval<Double>(4.0,  5.0,  1.0));
		intervals.add(new Interval<Double>(6.0,  6.0,  2.0));
		intervals.add(new Interval<Double>(9.0,  10.0, 3.0));
		intervals.add(new Interval<Double>(11.0, 12.0, 4.0));
		intervals.add(new Interval<Double>(15.0, 20.0, 5.0));
		intervals.add(new Interval<Double>(16.0, 20.0, 6.0));
		intervals.add(new Interval<Double>(18.0, 21.0, 7.0));
		intervals.add(new Interval<Double>(24.0, 29.0, 8.0));
		intervals.add(new Interval<Double>(30.0, 31.0, 9.0));
		return new DynamicDouble(intervals);
	}

	private DynamicDouble makeTree3() {
		List<Interval<Double>> intervals = new ArrayList<Interval<Double>>();
		intervals.add(new Interval<Double>(0.0,  3.0,  0.0));
		intervals.add(new Interval<Double>(5.0,  8.0,  1.0));
		intervals.add(new Interval<Double>(6.0,  10.0, 2.0));
		intervals.add(new Interval<Double>(8.0,  9.0,  3.0));
		return new DynamicDouble(intervals);
	}

	private DynamicInteger makeTreeInteger() {
		List<Interval<Integer>> intervals = new ArrayList<Interval<Integer>>();
		intervals.add(new Interval<Integer>(0.0,  3.0,  0));
		intervals.add(new Interval<Integer>(5.0,  8.0,  1));
		intervals.add(new Interval<Integer>(6.0,  10.0, 2));
		intervals.add(new Interval<Integer>(8.0,  9.0,  3));
		intervals.add(new Interval<Integer>(15.0, 23.0, 4));
		intervals.add(new Interval<Integer>(16.0, 21.0, 5));
		intervals.add(new Interval<Integer>(17.0, 19.0, 6));
		intervals.add(new Interval<Integer>(19.0, 20.0, 7));
		intervals.add(new Interval<Integer>(25.0, 30.0, 8));
		intervals.add(new Interval<Integer>(26.0, 26.0, 9));
		return new DynamicInteger(intervals);
	}

	private DynamicBoolean makeTreeBoolean() {
		List<Interval<Boolean>> intervals = new ArrayList<Interval<Boolean>>();
		intervals.add(new Interval<Boolean>(0.0,  3.0,  false));
		intervals.add(new Interval<Boolean>(5.0,  8.0,  false));
		intervals.add(new Interval<Boolean>(6.0,  10.0, false));
		intervals.add(new Interval<Boolean>(8.0,  9.0,  false));
		intervals.add(new Interval<Boolean>(15.0, 23.0, false));
		intervals.add(new Interval<Boolean>(16.0, 21.0, false));
		intervals.add(new Interval<Boolean>(17.0, 19.0, true));
		intervals.add(new Interval<Boolean>(19.0, 20.0, true));
		intervals.add(new Interval<Boolean>(25.0, 30.0, true));
		intervals.add(new Interval<Boolean>(26.0, 26.0, true));
		return new DynamicBoolean(intervals);
	}

	private DynamicCharacter makeTreeCharacter() {
		List<Interval<Character>> intervals =
				new ArrayList<Interval<Character>>();
		intervals.add(new Interval<Character>(0.0,  3.0,  '0'));
		intervals.add(new Interval<Character>(5.0,  8.0,  '1'));
		intervals.add(new Interval<Character>(6.0,  10.0, '2'));
		intervals.add(new Interval<Character>(8.0,  9.0,  '3'));
		intervals.add(new Interval<Character>(15.0, 23.0, '4'));
		intervals.add(new Interval<Character>(16.0, 21.0, '5'));
		intervals.add(new Interval<Character>(17.0, 19.0, '6'));
		intervals.add(new Interval<Character>(19.0, 20.0, '7'));
		intervals.add(new Interval<Character>(25.0, 30.0, '8'));
		intervals.add(new Interval<Character>(26.0, 26.0, '9'));
		return new DynamicCharacter(intervals);
	}

	private DynamicString makeTreeString() {
		List<Interval<String>> intervals = new ArrayList<Interval<String>>();
		intervals.add(new Interval<String>(0.0,  3.0,  "0"));
		intervals.add(new Interval<String>(5.0,  8.0,  "1"));
		intervals.add(new Interval<String>(6.0,  10.0, "2"));
		intervals.add(new Interval<String>(8.0,  9.0,  "3"));
		intervals.add(new Interval<String>(15.0, 23.0, "4"));
		intervals.add(new Interval<String>(16.0, 21.0, "5"));
		intervals.add(new Interval<String>(17.0, 19.0, "6"));
		intervals.add(new Interval<String>(19.0, 20.0, "7"));
		intervals.add(new Interval<String>(25.0, 30.0, "8"));
		intervals.add(new Interval<String>(26.0, 26.0, "9"));
		intervals.add(new Interval<String>(26.0, 26.0, "0_repeat"));
		intervals.add(new Interval<String>(27.0, 27.0, "0_repeat"));
		return new DynamicString(intervals);
	}
}
