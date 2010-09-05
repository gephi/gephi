/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.dynamic;

import java.util.List;
import org.gephi.data.attributes.type.Interval;
import org.junit.Test;

/**
 *
 * @author Mathieu Bastian
 */
public class DynamicModeltest {

    @Test
    public void testIndex() {

        DynamicIndex dynamicIndex = new DynamicIndex();

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

        printIntervals(dynamicIndex.getIntervals());
    }

    private void printIntervals(List<Interval<Integer>> intervals) {
        for (Interval<Integer> i : intervals) {
            System.out.println(i.toString());
        }
    }
}
