/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
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
package org.gephi.visualization.hull;

import java.util.Arrays;
import java.util.Comparator;
import org.gephi.graph.api.NodeData;

/**
 *
 * @author Mathieu
 */
public class AlgoHull {

    private static final Comparator<NodeData> LEFT_RIGHT = new Comparator<NodeData>() {

        public int compare(NodeData p1, NodeData p2) {
            if (p1.x() < p2.x()) {
                return -1;
            }
            if (p1.x() > p2.x()) {
                return +1;
            }
            if (p1.y() < p2.y()) {
                return -1;
            }
            if (p1.y() > p2.y()) {
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
    private static boolean isRightTurn(NodeData a, NodeData b, NodeData c) {
        double det = b.x() * c.y() + a.x() * b.y() + a.y() * c.x();
        det -= b.y() * c.x() + a.x() * c.y() + a.y() * b.x();
        return det < 0;
    }

    public static NodeData[] calculate(NodeData[] points) {
        NodeData[] temp = new NodeData[points.length];
        System.arraycopy(points, 0, temp, 0, points.length);
        Arrays.sort(temp, LEFT_RIGHT);
        if (points.length < 3) {
            return temp;
        }

        NodeData[] upperHull = new NodeData[temp.length];
        int ucount = 0;
        upperHull[ucount++] = temp[0];
        upperHull[ucount++] = temp[1];
        for (int i = 2; i < temp.length; i++) {
            while ((ucount >= 2) && !isRightTurn(upperHull[ucount - 2], upperHull[ucount - 1], temp[i])) {
                ucount--;
            }
            upperHull[ucount++] = temp[i];
        }

        NodeData[] lowerHull = new NodeData[temp.length];
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

        NodeData[] result = new NodeData[ucount];
        for (int i = 0; i < ucount; i++) {
            result[i] = upperHull[i];
        }

        return result;
    }
}
