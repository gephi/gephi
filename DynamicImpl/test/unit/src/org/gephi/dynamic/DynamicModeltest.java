/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.dynamic;

import java.util.List;
import org.gephi.data.attributes.type.Interval;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Mathieu Bastian
 */
public class DynamicModeltest {

    @Test
    public void testIndex() {

        DynamicIndex dynamicIndex = new DynamicIndex(null);

        Interval interval1 = new Interval(2000, 2001);
        Interval interval2 = new Interval(2000, 2001);
        Interval interval3 = new Interval(2002, 2005);
        Interval interval4 = new Interval(2002, 2005);
        Interval interval5 = new Interval(2003, 2006);

        dynamicIndex.add(interval1);
        dynamicIndex.add(interval2);
        dynamicIndex.add(interval3);
        dynamicIndex.add(interval4);
        dynamicIndex.add(interval5);

        assertEquals(3, dynamicIndex.getIntervals().size());

        dynamicIndex.remove(interval1);
        assertEquals(3, dynamicIndex.getIntervals().size());
        dynamicIndex.remove(interval1);
        assertEquals(2, dynamicIndex.getIntervals().size());

        Interval interval6 = new Interval(2000, 2010);
        Interval interval7 = new Interval(Double.NEGATIVE_INFINITY, 2015);
        Interval interval8 = new Interval(1991, Double.POSITIVE_INFINITY);
        Interval interval9 = new Interval(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        Interval interval10 = new Interval(1994, Double.POSITIVE_INFINITY);

        dynamicIndex.add(interval6);
        dynamicIndex.add(interval7);
        dynamicIndex.add(interval8);
        dynamicIndex.add(interval9);
        dynamicIndex.add(interval10);

        System.out.println(dynamicIndex.getMin());
        System.out.println(dynamicIndex.getMax());

        printIntervals(dynamicIndex.intervalTree.search(interval6));
        
        //printIntervals(dynamicIndex.getIntervals());
    }

    private void printIntervals(List<Interval<Integer>> intervals) {
        System.out.println("--");
        for (Interval<Integer> i : intervals) {
            System.out.println(i.toString());
        }
        System.out.println("#--");
    }
}
