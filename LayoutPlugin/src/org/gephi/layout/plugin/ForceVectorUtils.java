/*
Copyright 2008-2010 Gephi
Authors : Mathieu Jacomy
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.gephi.layout.plugin;

import org.gephi.graph.api.NodeData;
import org.gephi.graph.api.Spatial;

/**
 *
 * @author Mathieu Jacomy
 */
public class ForceVectorUtils {

    public static float distance(Spatial n1, Spatial n2) {
        return (float) Math.hypot(n1.x() - n2.x(), n1.y() - n2.y());
    }

    public static void fcBiRepulsor(NodeData N1, NodeData N2, double c) {
        double xDist = N1.x() - N2.x();	// distance en x entre les deux noeuds
        double yDist = N1.y() - N2.y();
        double dist = (float) Math.sqrt(xDist * xDist + yDist * yDist);	// distance tout court

        if (dist > 0) {
            double f = repulsion(c, dist);

            ForceVectorNodeLayoutData N1L = N1.getLayoutData();
            ForceVectorNodeLayoutData N2L = N2.getLayoutData();

            N1L.dx += xDist / dist * f;
            N1L.dy += yDist / dist * f;

            N2L.dx -= xDist / dist * f;
            N2L.dy -= yDist / dist * f;
        }
    }

    public static void fcBiRepulsor_y(NodeData N1, NodeData N2, double c, double verticalization) {
        double xDist = N1.x() - N2.x();	// distance en x entre les deux noeuds
        double yDist = N1.y() - N2.y();
        double dist = (float) Math.sqrt(xDist * xDist + yDist * yDist);	// distance tout court

        if (dist > 0) {
            double f = repulsion(c, dist);

            ForceVectorNodeLayoutData N1L = N1.getLayoutData();
            ForceVectorNodeLayoutData N2L = N2.getLayoutData();

            N1L.dx += xDist / dist * f;
            N1L.dy += verticalization * yDist / dist * f;

            N2L.dx -= xDist / dist * f;
            N2L.dy -= verticalization * yDist / dist * f;
        }
    }

    public static void fcBiRepulsor_noCollide(NodeData N1, NodeData N2, double c) {
        double xDist = N1.x() - N2.x();	// distance en x entre les deux noeuds
        double yDist = N1.y() - N2.y();
        double dist = (float) Math.sqrt(xDist * xDist + yDist * yDist) - N1.getSize() - N2.getSize();	// distance (from the border of each node)

        if (dist > 0) {
            double f = repulsion(c, dist);

            ForceVectorNodeLayoutData N1L = N1.getLayoutData();
            ForceVectorNodeLayoutData N2L = N2.getLayoutData();

            N1L.dx += xDist / dist * f;
            N1L.dy += yDist / dist * f;

            N2L.dx -= xDist / dist * f;
            N2L.dy -= yDist / dist * f;
        } else if (dist != 0) {
            double f = -c;	//flat repulsion

            ForceVectorNodeLayoutData N1L = N1.getLayoutData();
            ForceVectorNodeLayoutData N2L = N2.getLayoutData();

            N1L.dx += xDist / dist * f;
            N1L.dy += yDist / dist * f;

            N2L.dx -= xDist / dist * f;
            N2L.dy -= yDist / dist * f;
        }
    }

    public static void fcUniRepulsor(NodeData N1, NodeData N2, double c) {
        double xDist = N1.x() - N2.x();	// distance en x entre les deux noeuds
        double yDist = N1.y() - N2.y();
        double dist = (float) Math.sqrt(xDist * xDist + yDist * yDist);	// distance tout court

        if (dist > 0) {
            double f = repulsion(c, dist);

            ForceVectorNodeLayoutData N2L = N2.getLayoutData();

            N2L.dx -= xDist / dist * f;
            N2L.dy -= yDist / dist * f;
        }
    }

    public static void fcBiAttractor(NodeData N1, NodeData N2, double c) {
        double xDist = N1.x() - N2.x();	// distance en x entre les deux noeuds
        double yDist = N1.y() - N2.y();
        double dist = (float) Math.sqrt(xDist * xDist + yDist * yDist);	// distance tout court

        if (dist > 0) {
            double f = attraction(c, dist);

            ForceVectorNodeLayoutData N1L = N1.getLayoutData();
            ForceVectorNodeLayoutData N2L = N2.getLayoutData();

            N1L.dx += xDist / dist * f;
            N1L.dy += yDist / dist * f;

            N2L.dx -= xDist / dist * f;
            N2L.dy -= yDist / dist * f;
        }
    }

    public static void fcBiAttractor_noCollide(NodeData N1, NodeData N2, double c) {
        double xDist = N1.x() - N2.x();	// distance en x entre les deux noeuds
        double yDist = N1.y() - N2.y();
        double dist = (float) Math.sqrt(xDist * xDist + yDist * yDist) - N1.getSize() - N2.getSize();	// distance (from the border of each node)

        if (dist > 0) {
            double f = attraction(c, dist);

            ForceVectorNodeLayoutData N1L = N1.getLayoutData();
            ForceVectorNodeLayoutData N2L = N2.getLayoutData();

            N1L.dx += xDist / dist * f;
            N1L.dy += yDist / dist * f;

            N2L.dx -= xDist / dist * f;
            N2L.dy -= yDist / dist * f;
        }
    }

    public static void fcBiFlatAttractor(NodeData N1, NodeData N2, double c) {
        double xDist = N1.x() - N2.x();	// distance en x entre les deux noeuds
        double yDist = N1.y() - N2.y();
        double dist = (float) Math.sqrt(xDist * xDist + yDist * yDist);	// distance tout court

        if (dist > 0) {
            double f = -c;

            ForceVectorNodeLayoutData N1L = N1.getLayoutData();
            ForceVectorNodeLayoutData N2L = N2.getLayoutData();

            N1L.dx += xDist / dist * f;
            N1L.dy += yDist / dist * f;

            N2L.dx -= xDist / dist * f;
            N2L.dy -= yDist / dist * f;
        }
    }

    public static void fcUniAttractor(NodeData N1, NodeData N2, float c) {
        double xDist = N1.x() - N2.x();	// distance en x entre les deux noeuds
        double yDist = N1.y() - N2.y();
        double dist = (float) Math.sqrt(xDist * xDist + yDist * yDist);	// distance tout court

        if (dist > 0) {
            double f = attraction(c, dist);

            ForceVectorNodeLayoutData N2L = N2.getLayoutData();

            N2L.dx -= xDist / dist * f;
            N2L.dy -= yDist / dist * f;
        }
    }

    protected static double attraction(double c, double dist) {
        return 0.01 * -c * dist;
    }

    protected static double repulsion(double c, double dist) {
        return 0.001 * c / dist;
    }
}
