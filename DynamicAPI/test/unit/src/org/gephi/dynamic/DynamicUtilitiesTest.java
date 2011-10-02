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

import java.util.List;
import org.gephi.data.attributes.type.DynamicInteger;
import org.gephi.data.attributes.type.DynamicDouble;
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

    public DynamicUtilitiesTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testCalendar() {
        System.out.println("testCalendar()");
        String date = "2000-01-01T00:00:00.000";
        double d = DynamicUtilities.getDoubleFromXMLDateString(date);
        String date2 = DynamicUtilities.getXMLDateStringFromDouble(d);
        assertEquals(date, date2);
        System.out.println();
    }

    @Test
    public void testCreateDynamicObjectMethods() {
        System.out.println("createDynamicObject(AttributeType, Interval)");
        AttributeType type = AttributeType.DYNAMIC_DOUBLE;
        Interval<Double> in = new Interval(1.0, 2.0, 3.0);
        DynamicType result = DynamicUtilities.createDynamicObject(type, in);
        DynamicType expResult = new DynamicDouble(new Interval<Double>(1.0, 2.0, 3.0));
        assertEquals(expResult, result);
        System.out.println("result:    " + result.toString());
        System.out.println("expResult: " + expResult.toString());
        System.out.println();
    }

    @Test
    public void testFitToInterval() {
        System.out.println("fitToInterval(DynamicType, double, double)");
        DynamicType expResult = new DynamicDouble(new Interval<Double>(1.0, 2.0, 0.0));
        DynamicType result = DynamicUtilities.fitToInterval(
                new DynamicDouble(new Interval<Double>(1.0, 3.0, 0.0)),
                1.0, 2.0);
        assertEquals(expResult, result);
        System.out.println("result:    " + result.toString());
        System.out.println("expResult: " + expResult.toString());
        System.out.println();
    }

    @Test
    public void testRemoveOverlapping1() {
        DynamicInteger instance = new DynamicInteger();
        instance = new DynamicInteger(instance, new Interval<Integer>(2002, Double.POSITIVE_INFINITY, 1));
        instance = new DynamicInteger(instance, new Interval<Integer>(2003, Double.POSITIVE_INFINITY, 2));
        instance = new DynamicInteger(instance, new Interval<Integer>(2004, Double.POSITIVE_INFINITY, 3));

        DynamicInteger result = (DynamicInteger) DynamicUtilities.removeOverlapping(instance);
        List<Interval<Integer>> intervalsResult = result.getIntervals(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        assertEquals(new Interval<Integer>(2002, 2003, false, true, 1), intervalsResult.get(0));
        assertEquals(new Interval<Integer>(2003, 2004, false, true, 2), intervalsResult.get(1));
        assertEquals(new Interval<Integer>(2004, Double.POSITIVE_INFINITY, false, false, 3), intervalsResult.get(2));
    }

    @Test
    public void testRemoveOverlapping2() {
        DynamicInteger instance = new DynamicInteger();
        instance = new DynamicInteger(instance, new Interval<Integer>(Double.NEGATIVE_INFINITY, 2002, 1));
        instance = new DynamicInteger(instance, new Interval<Integer>(Double.NEGATIVE_INFINITY, 2003, 2));
        instance = new DynamicInteger(instance, new Interval<Integer>(Double.NEGATIVE_INFINITY, 2004, 3));

        DynamicInteger result = (DynamicInteger) DynamicUtilities.removeOverlapping(instance);
        List<Interval<Integer>> intervalsResult = result.getIntervals(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        assertEquals(new Interval<Integer>(Double.NEGATIVE_INFINITY, 2002, false, false, 1), intervalsResult.get(0));
        assertEquals(new Interval<Integer>(2002, 2003, true, false, 2), intervalsResult.get(1));
        assertEquals(new Interval<Integer>(2003, 2004, true, false, 3), intervalsResult.get(2));
    }

    @Test
    public void testRemoveOverlapping3() {
        DynamicInteger instance = new DynamicInteger();
        instance = new DynamicInteger(instance, new Interval<Integer>(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1));
        instance = new DynamicInteger(instance, new Interval<Integer>(2002, 2003, 2));

        DynamicInteger result = (DynamicInteger) DynamicUtilities.removeOverlapping(instance);
        List<Interval<Integer>> intervalsResult = result.getIntervals(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        assertEquals(new Interval<Integer>(Double.NEGATIVE_INFINITY, 2002, false, true, 1), intervalsResult.get(0));
        assertEquals(new Interval<Integer>(2002, 2003, false, false, 2), intervalsResult.get(1));
        assertEquals(new Interval<Integer>(2003, Double.POSITIVE_INFINITY, true, false, 1), intervalsResult.get(2));
    }
}
