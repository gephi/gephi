/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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

        //printIntervals(dynamicIndex.intervalTree.search(interval6));
        
        //printIntervals(dynamicIndex.getIntervals());
    }

    @Test
    public void testIndex2() {

        DynamicIndex dynamicIndex = new DynamicIndex(null);

        Interval interval1 = new Interval(2, Double.POSITIVE_INFINITY);
        Interval interval2 = new Interval(2, 5);
        Interval interval3 = new Interval(2, 3);

        dynamicIndex.add(interval1);
        dynamicIndex.add(interval2);
        dynamicIndex.add(interval2);
        dynamicIndex.add(interval2);
        dynamicIndex.add(interval2);
        dynamicIndex.add(interval3);

    }

    private void printIntervals(List<Interval<Integer>> intervals) {
        System.out.println("--");
        for (Interval<Integer> i : intervals) {
            System.out.println(i.toString());
        }
        System.out.println("#--");
    }
}
