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
package org.gephi.layout;

/**
 *
 * @author Mathieu Jacomy
 */
public class ForceVectorUtils {

    public static void fcBiRepulsor(NodeLayout N1, NodeLayout N2, double c) {
        double xDist = N1.x() - N2.x();	// distance en x entre les deux noeuds
        double yDist = N1.y() - N2.y();
        double dist = (float) Math.sqrt(xDist * xDist + yDist * yDist);	// distance tout court

        if (dist > 0) {
            double f = repulsion(c, dist);

            N1.dx += xDist / dist * f;
            N1.dy += yDist / dist * f;

            N2.dx -= xDist / dist * f;
            N2.dy -= yDist / dist * f;
        }
    }

    public static void fcBiRepulsor_y(NodeLayout N1, NodeLayout N2, double c, double verticalization) {
        double xDist = N1.x() - N2.x();	// distance en x entre les deux noeuds
        double yDist = N1.y() - N2.y();
        double dist = (float) Math.sqrt(xDist * xDist + yDist * yDist);	// distance tout court

        if (dist > 0) {
            double f = repulsion(c, dist);

            N1.dx += xDist / dist * f;
            N1.dy += verticalization * yDist / dist * f;

            N2.dx -= xDist / dist * f;
            N2.dy -= verticalization * yDist / dist * f;
        }
    }

    public static void fcBiRepulsor_noCollide(NodeLayout N1, NodeLayout N2, double c) {
        double xDist = N1.x() - N2.x();	// distance en x entre les deux noeuds
        double yDist = N1.y() - N2.y();
        double dist = (float) Math.sqrt(xDist * xDist + yDist * yDist) - N1.size() - N2.size();	// distance (from the border of each node)

        if (dist > 0) {
            double f = repulsion(c, dist);

            N1.dx += xDist / dist * f;
            N1.dy += yDist / dist * f;

            N2.dx -= xDist / dist * f;
            N2.dy -= yDist / dist * f;
        } else if (dist != 0) {
            double f = -c;	//flat repulsion

            N1.dx += xDist / dist * f;
            N1.dy += yDist / dist * f;

            N2.dx -= xDist / dist * f;
            N2.dy -= yDist / dist * f;
        }
    }

    public static void fcUniRepulsor(NodeLayout N1, NodeLayout N2, double c) {
        double xDist = N1.x() - N2.x();	// distance en x entre les deux noeuds
        double yDist = N1.y() - N2.y();
        double dist = (float) Math.sqrt(xDist * xDist + yDist * yDist);	// distance tout court

        if (dist > 0) {
            double f = repulsion(c, dist);

            N2.dx -= xDist / dist * f;
            N2.dy -= yDist / dist * f;
        }
    }

    public static void fcBiAttractor(NodeLayout N1, NodeLayout N2, double c) {
        double xDist = N1.x() - N2.x();	// distance en x entre les deux noeuds
        double yDist = N1.y() - N2.y();
        double dist = (float) Math.sqrt(xDist * xDist + yDist * yDist);	// distance tout court

        if (dist > 0) {
            double f = attraction(c, dist);

            N1.dx += xDist / dist * f;
            N1.dy += yDist / dist * f;

            N2.dx -= xDist / dist * f;
            N2.dy -= yDist / dist * f;
        }
    }

    public static void fcBiAttractor_noCollide(NodeLayout N1, NodeLayout N2, double c) {
        double xDist = N1.x() - N2.x();	// distance en x entre les deux noeuds
        double yDist = N1.y() - N2.y();
        double dist = (float) Math.sqrt(xDist * xDist + yDist * yDist) - N1.size() - N2.size();	// distance (from the border of each node)

        if (dist > 0) {
            double f = attraction(c, dist);

            N1.dx += xDist / dist * f;
            N1.dy += yDist / dist * f;

            N2.dx -= xDist / dist * f;
            N2.dy -= yDist / dist * f;
        }
    }

    public static void fcBiFlatAttractor(NodeLayout N1, NodeLayout N2, double c) {
        double xDist = N1.x() - N2.x();	// distance en x entre les deux noeuds
        double yDist = N1.y() - N2.y();
        double dist = (float) Math.sqrt(xDist * xDist + yDist * yDist);	// distance tout court

        if (dist > 0) {
            double f = -c;

            N1.dx += xDist / dist * f;
            N1.dy += yDist / dist * f;

            N2.dx -= xDist / dist * f;
            N2.dy -= yDist / dist * f;
        }
    }

    public static void fcUniAttractor(NodeLayout N1, NodeLayout N2, float c) {
        double xDist = N1.x() - N2.x();	// distance en x entre les deux noeuds
        double yDist = N1.y() - N2.y();
        double dist = (float) Math.sqrt(xDist * xDist + yDist * yDist);	// distance tout court

        if (dist > 0) {
            double f = attraction(c, dist);

            N2.dx -= xDist / dist * f;
            N2.dy -= yDist / dist * f;
        }
    }

    protected static double attraction(double c, double dist) {
        return 0.01 * -c * dist;
    }

    protected static double repulsion(double c, double dist) {
        return 0.001 * c / dist;
    }
}
