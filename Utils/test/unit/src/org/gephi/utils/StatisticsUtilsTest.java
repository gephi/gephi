package org.gephi.utils;

/*
Copyright 2008-2010 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
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
