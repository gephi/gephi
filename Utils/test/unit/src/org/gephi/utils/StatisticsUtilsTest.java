package org.gephi.utils;

/*
Copyright 2008-2010 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test for StatisticsUtils
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public class StatisticsUtilsTest {

    /**
     * Test of average method, of class StatisticsUtils.
     */
    @Test
    public void testAverage() {
        assertEquals(StatisticsUtils.average(new Integer[]{1,2,3,4,5,6,7,8,9,10}), new BigDecimal("5.5"));
        assertEquals(StatisticsUtils.average(new Short[]{33,null,67,null}), new BigDecimal("50"));
        assertEquals(StatisticsUtils.average(new Long[]{10000000000000l,null,30000000000000l,null}), new BigDecimal("20000000000000"));
        BigInteger[] bigBigIntegerArrayTest=new BigInteger[420000];
        Arrays.fill(bigBigIntegerArrayTest, new BigInteger("42"));
        assertEquals(StatisticsUtils.average(bigBigIntegerArrayTest), new BigDecimal("42"));
        ArrayList<Number> bigBigDecimalCollectionTest=new ArrayList<Number>();
        Number[] bigBigDecimalArrayTest=new Number[210000];
        Arrays.fill(bigBigDecimalArrayTest, new BigDecimal("2112"));
        bigBigDecimalCollectionTest.addAll(Arrays.asList(bigBigDecimalArrayTest));
        assertEquals(StatisticsUtils.average(bigBigDecimalCollectionTest), new BigDecimal("2112"));
        assertEquals(StatisticsUtils.average(new Number[]{Byte.valueOf((byte)33),Short.valueOf((short)6),67,45l,64466.1234d}), new BigDecimal("12923.42468"));
    }

    @Test
    public void testMedian(){
        assertEquals(StatisticsUtils.median(new Integer[]{7,6,5,4,3,2,1}),new BigDecimal("4"));
        assertEquals(StatisticsUtils.median(new Integer[]{1,2,3,4}),new BigDecimal("2.5"));
        assertEquals(StatisticsUtils.median(new Integer[]{22}),new BigDecimal("22"));
    }

    @Test
    public void testSum(){
        assertEquals(StatisticsUtils.sum(new Byte[]{10,20,30,40,60,27,-30,-25,-5}),new BigDecimal("127"));
    }

    @Test
    public void testMinValue(){
        assertEquals(StatisticsUtils.minValue(new Long[]{34l,45l,2l,0l,34535l,6346l}),new BigDecimal("0"));
        assertEquals(StatisticsUtils.minValue(new Integer[]{34,45,2,0,34535,6346}),new BigDecimal("0"));
        assertEquals(StatisticsUtils.minValue(new Number[]{34l,45l,2l,0l,34535l,6346l}),new BigDecimal("0"));
    }

    @Test
    public void testMaxValue(){
        assertEquals(StatisticsUtils.maxValue(Arrays.asList(new Number[]{34l,(byte)6,2l,0d,34535,BigInteger.valueOf(6346)})),new BigDecimal("34535"));
        assertEquals(StatisticsUtils.maxValue(new Number[]{34,45,2,0,34535.0,6346}),new BigDecimal("34535.0"));
    }

    @Test
    public void testGetAllStatistics(){
        assertNull(StatisticsUtils.getAllStatistics(new Number[]{}));
        Number[] numbers={7,5,3,2,4,6,1};
        BigDecimal[] results=StatisticsUtils.getAllStatistics(numbers);
        assertEquals(results[0], new BigDecimal("4"));
        assertEquals(results[1], new BigDecimal("2.5"));
        assertEquals(results[2], new BigDecimal("4"));
        assertEquals(results[3], new BigDecimal("5.5"));
        assertEquals(results[4], new BigDecimal("3.0"));
        assertEquals(results[5], new BigDecimal("28"));
        assertEquals(results[6], new BigDecimal("1"));
        assertEquals(results[7], new BigDecimal("7"));
    }
}