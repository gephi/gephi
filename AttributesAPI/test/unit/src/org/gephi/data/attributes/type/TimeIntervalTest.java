/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.data.attributes.type;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Mathieu Bastian
 */
public class TimeIntervalTest {

    public TimeIntervalTest() {
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
    public void testIsInRangeUni() {
        TimeInterval instance = new TimeInterval(2.0, 5.0);
        assertEquals(false, instance.isInRange(0.0, 1.0));
        assertEquals(true, instance.isInRange(1.0, 10.0));
        assertEquals(true, instance.isInRange(0.0, 2.0));
        assertEquals(true, instance.isInRange(2.0, 2.0));
        assertEquals(true, instance.isInRange(1.0, 3.0));
        assertEquals(true, instance.isInRange(2.0, 3.0));
        assertEquals(true, instance.isInRange(3.0, 3.0));
        assertEquals(true, instance.isInRange(3.0, 4.0));
        assertEquals(true, instance.isInRange(3.0, 5.0));
        assertEquals(true, instance.isInRange(3.0, 6.0));
        assertEquals(true, instance.isInRange(5.0, 5.0));
        assertEquals(true, instance.isInRange(5.0, 6.0));
        assertEquals(false, instance.isInRange(6.0, 8.0));
        assertEquals(2.0, instance.getMin(), 0);
        assertEquals(5.0, instance.getMax(), 0);
    }

    @Test
    public void testIsInRangeMulti() {
        TimeInterval instance = new TimeInterval(new double[][]{{2.0, 5.0}, {7.0, 10.0}});
        assertEquals(false, instance.isInRange(0.0, 1.0));
        assertEquals(true, instance.isInRange(4.0, 8.0));
        assertEquals(true, instance.isInRange(6.0, 7.0));
        assertEquals(false, instance.isInRange(6.0, 6.0));
        assertEquals(false, instance.isInRange(6.0, 6.5));
        assertEquals(2.0, instance.getMin(), 0);
        assertEquals(10.0, instance.getMax(), 0);
    }

    @Test
    public void testParse() {
        TimeInterval t1 = new TimeInterval("0.2,1.2");
        assertEquals(0.2, t1.getStart(0), 0);
        assertEquals(1.2, t1.getEnd(0), 0);
        assertEquals(0.2, t1.getMin(), 0);
        assertEquals(1.2, t1.getMax(), 0);
        TimeInterval t2 = new TimeInterval("[0.2,1.2]");
        assertEquals(0.2, t2.getStart(0), 0);
        assertEquals(1.2, t2.getEnd(0), 0);
        TimeInterval t3 = new TimeInterval("[0.2,1.2][5.0,10.0]");
        assertEquals(0.2, t3.getStart(0), 0);
        assertEquals(1.2, t3.getEnd(0), 0);
        assertEquals(5.0, t3.getStart(1), 0);
        assertEquals(10.0, t3.getEnd(1), 0);
        assertEquals(0.2, t3.getMin(), 0);
        assertEquals(10.0, t3.getMax(), 0);
    }

    @Test
    public void testEquals() {
        TimeInterval t1 = new TimeInterval(2.0, 5.0);
        TimeInterval t2 = new TimeInterval(2.0, 5.0);
        assertEquals(t1, t2);

        assertEquals(t1.hashCode(), t2.hashCode());
    }

    @Test
    public void testInfinity() {
        TimeInterval instance = new TimeInterval(2.0, Double.POSITIVE_INFINITY);
        assertEquals(true, instance.isInRange(1.0, 10.0));
        assertEquals(true, instance.isInRange(8.0, 10.0));

         TimeInterval instance2 = new TimeInterval(Double.NEGATIVE_INFINITY, 5.0);
        assertEquals(true, instance2.isInRange(1.0, 10.0));
        assertEquals(true, instance2.isInRange(-8.0, -3.0));

        TimeInterval instance3 = new TimeInterval(new double[][]{{Double.NEGATIVE_INFINITY, 4.0}, {7.0, Double.POSITIVE_INFINITY}});
        assertEquals(true, instance3.isInRange(4.0, 8.0));
        assertEquals(false, instance3.isInRange(5.0, 6.0));
        assertEquals(Double.NEGATIVE_INFINITY, instance3.getMin(), 0);
        assertEquals(Double.POSITIVE_INFINITY, instance3.getMax(), 0);

        TimeInterval t1 = new TimeInterval("0.2, ");
        assertEquals(0.2, t1.getStart(0), 0);
        assertEquals(Double.POSITIVE_INFINITY, t1.getEnd(0), 0);
        assertEquals(0.2, t1.getMin(), 0);
        assertEquals(Double.POSITIVE_INFINITY, t1.getMax(), 0);

        TimeInterval t2 = new TimeInterval("[0.2, ]");
        assertEquals(0.2, t2.getStart(0), 0);
        assertEquals(Double.POSITIVE_INFINITY, t2.getEnd(0), 0);
    }
}
