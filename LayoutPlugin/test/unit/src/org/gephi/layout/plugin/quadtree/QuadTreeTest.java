/*
Copyright 2008-2010 Gephi
Authors : Helder Suzuki <heldersuzuki@gephi.org>
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
package org.gephi.layout.plugin.quadtree;

import org.gephi.layout.plugin.force.quadtree.QuadTree;
import static org.junit.Assert.*;

/**
 *
 * @author Helder Suzuki <heldersuzuki@gephi.org>
 */
public class QuadTreeTest {

    public static final double eps = 1e-6;

    public void checkMassCenterConservation(QuadTree tree) {
        if (tree.isIsLeaf()) {
            return;
        }

        float x = 0, y = 0;
        int totalMass = 0;
        for (QuadTree child : tree.getChildren()) {
            x += child.x() * child.mass();
            y += child.y() * child.mass();
            totalMass += child.mass();
        }

        assertEquals(totalMass, tree.mass());
        assertEquals(x / totalMass, tree.x(), eps);
        assertEquals(y / totalMass, tree.y(), eps);
    }

    @org.junit.Test
    public void testMaxLevel0() {
        QuadTree tree = new QuadTree(0, 0, 10, 0);
        assertEquals(tree.mass(), 0);

        assertTrue(tree.addNode(new TestNode(1, 1)));
        assertEquals(1, tree.mass());
        assertEquals(1, tree.x(), eps);
        assertEquals(1, tree.y(), eps);

        assertTrue(tree.addNode(new TestNode(9, 9)));
        assertEquals(tree.mass(), 2);
        assertEquals(5, tree.x(), eps);
        assertEquals(5, tree.y(), eps);
    }

    @org.junit.Test
    public void testMaxLevel1() {
        QuadTree tree = new QuadTree(0, 0, 10, 1);
        assertEquals(0, tree.mass());

        assertTrue(tree.addNode(new TestNode(1, 1)));
        assertEquals(1, tree.mass());
        assertEquals(1, tree.x(), eps);
        assertEquals(1, tree.y(), eps);

        assertTrue(tree.addNode(new TestNode(9, 9)));
        assertEquals(2, tree.mass());
        assertEquals(5, tree.y(), eps);

        checkMassCenterConservation(tree);
    }

    @org.junit.Test
    public void testLevel1Leaf() {
        QuadTree tree = new QuadTree(0, 0, 10, 1);
        assertEquals(0, tree.mass());

        assertTrue(tree.addNode(new TestNode(7, 7)));
        assertTrue(tree.addNode(new TestNode(7, 8)));
        assertTrue(tree.addNode(new TestNode(8, 7)));
        assertTrue(tree.addNode(new TestNode(8, 8)));

        assertTrue(tree.addNode(new TestNode(1, 1)));
        checkMassCenterConservation(tree);
    }
}

