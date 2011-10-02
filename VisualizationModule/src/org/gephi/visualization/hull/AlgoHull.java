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
package org.gephi.visualization.hull;

import java.util.Arrays;
import java.util.Comparator;
import org.gephi.graph.api.Node;

/**
 *
 * @author Mathieu Bastian
 */
public class AlgoHull {

    private static final Comparator<Node> LEFT_RIGHT = new Comparator<Node>() {

        public int compare(Node p1, Node p2) {
            if (p1.getNodeData().x() < p2.getNodeData().x()) {
                return -1;
            }
            if (p1.getNodeData().x() > p2.getNodeData().x()) {
                return +1;
            }
            if (p1.getNodeData().y() < p2.getNodeData().y()) {
                return -1;
            }
            if (p1.getNodeData().y() > p2.getNodeData().y()) {
                return +1;
            }
            return 0;
        }
    };

    /**
     * Returns true if the three points form a real right turn (i.e. they do not
     * form a left turn, nor are they collinear).
     * <p>
     * May return an incorrect result if the points are very close to each other
     * or if one point lies very close to the line described by the other two.
     */
    private static boolean isRightTurn(Node a, Node b, Node c) {
        double det = b.getNodeData().x() * c.getNodeData().y() + a.getNodeData().x() * b.getNodeData().y() + a.getNodeData().y() * c.getNodeData().x();
        det -= b.getNodeData().y() * c.getNodeData().x() + a.getNodeData().x() * c.getNodeData().y() + a.getNodeData().y() * b.getNodeData().x();
        return det < 0;
    }

    public static Node[] calculate(Node[] points) {
        Node[] temp = new Node[points.length];
        System.arraycopy(points, 0, temp, 0, points.length);
        Arrays.sort(temp, LEFT_RIGHT);
        if (points.length < 3) {
            return temp;
        }

        Node[] upperHull = new Node[temp.length];
        int ucount = 0;
        upperHull[ucount++] = temp[0];
        upperHull[ucount++] = temp[1];
        for (int i = 2; i < temp.length; i++) {
            while ((ucount >= 2) && !isRightTurn(upperHull[ucount - 2], upperHull[ucount - 1], temp[i])) {
                ucount--;
            }
            upperHull[ucount++] = temp[i];
        }

        Node[] lowerHull = new Node[temp.length];
        int lcount = 0;
        lowerHull[lcount++] = temp[temp.length - 1];
        lowerHull[lcount++] = temp[temp.length - 2];
        for (int i = temp.length - 3; i >= 0; i--) {
            while ((lcount >= 2) && !isRightTurn(lowerHull[lcount - 2], lowerHull[lcount - 1], temp[i])) {
                lcount--;
            }
            lowerHull[lcount++] = temp[i];
        }

        for (int i = 1; i < lcount - 1; i++) {
            upperHull[ucount++] = lowerHull[i];
        }

        Node[] result = new Node[ucount];
        for (int i = 0; i < ucount; i++) {
            result[i] = upperHull[i];
        }

        return result;
    }
}
