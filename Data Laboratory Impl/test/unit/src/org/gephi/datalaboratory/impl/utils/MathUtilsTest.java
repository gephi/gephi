/*
Copyright 2008-2010 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.datalaboratory.impl.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test for MathUtils
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public class MathUtilsTest {

    /**
     * Test of average method, of class MathUtils.
     */
    @Test
    public void testAverage() {
        assertEquals(MathUtils.average(new Integer[]{1,2,3,4,5,6,7,8,9,10}), new BigDecimal("5.5"));
        assertEquals(MathUtils.average(new Short[]{33,null,67,null}), new BigDecimal("50"));
        assertEquals(MathUtils.average(new Long[]{10000000000000l,null,30000000000000l,null}), new BigDecimal("20000000000000"));
        BigInteger[] bigBigIntegerArrayTest=new BigInteger[420000];
        Arrays.fill(bigBigIntegerArrayTest, new BigInteger("42"));
        assertEquals(MathUtils.average(bigBigIntegerArrayTest), new BigDecimal("42"));
        ArrayList<BigDecimal> bigBigDecimalCollectionTest=new ArrayList<BigDecimal>();
        BigDecimal[] bigBigDecimalArrayTest=new BigDecimal[210000];
        Arrays.fill(bigBigDecimalArrayTest, new BigDecimal("2112"));
        bigBigDecimalCollectionTest.addAll(Arrays.asList(bigBigDecimalArrayTest));
        assertEquals(MathUtils.average(bigBigDecimalCollectionTest), new BigDecimal("2112"));
        assertEquals(MathUtils.average(new Number[]{Byte.valueOf((byte)33),Short.valueOf((short)6),67,45l,64466.1234d}), new BigDecimal("12923.42468"));
    }

    @Test
    public void testSum(){
        assertEquals(MathUtils.sum(new Byte[]{10,20,30,40,60,27,-30,-25,-5}),new BigDecimal("127"));
    }

    @Test
    public void testMinValue(){
        assertEquals(MathUtils.minValue(new Long[]{34l,45l,2l,0l,34535l,6346l}),new Long(0l));
    }

    @Test
    public void testMaxValue(){
        assertEquals(MathUtils.maxValue(Arrays.asList(new Long[]{34l,45l,2l,0l,34535l,6346l})),new Long(34535l));
    }
}