/*
Copyright 2008-2010 Gephi
Authors : Cezary Bartosiak
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
package org.gephi.data.attributes.type;

import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit test for IntervalTree class.
 *
 * @author Cezary Bartosiak
 */
public class IntervalTreeTest {
	public IntervalTreeTest() { }

	@BeforeClass
	public static void setUpClass() throws Exception { }

	@AfterClass
	public static void tearDownClass() throws Exception { }
	
	@Before
	public void setUp() { }
	
	@After
	public void tearDown() { }

	@Test
	public void testClass() {
		System.out.println("Class");
		
		IntervalTree<Integer> itree = new IntervalTree<Integer>();
        itree.insert(new Interval<Integer>(1.0, 1.0));
        itree.insert(new Interval<Integer>(5.0, 5.0));
        itree.insert(new Interval<Integer>(7.0, 7.0));
        itree.delete(new Interval<Integer>(5.0, 5.0));
        itree.insert(new Interval<Integer>(5.0, 6.0));
        itree.delete(new Interval<Integer>(5.0, 7.0));
        itree.insert(new Interval<Integer>(5.0, 7.0));
        itree.delete(new Interval<Integer>(1.0, 1.0));
        itree.insert(new Interval<Integer>(1.0, 2.0));
        itree.delete(new Interval<Integer>(1.0, 2.0));
        itree.insert(new Interval<Integer>(1.0, 2.0));
		List<Interval<Integer>> list = new ArrayList<Interval<Integer>>();
		list.add(new Interval<Integer>(5.0, 7.0));
		assertEquals(itree.search(4.0, 5.0), list);
        System.out.println("itree.search(4.0, 5.0): " + itree.search(4.0, 5.0));
		System.out.println("list: " + list);
		System.out.println();
	}
}
