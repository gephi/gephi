/*
Copyright 2008-2010 Gephi
Authors : Jeremy Subtil <jeremy.subtil@gephi.org>
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
package org.gephi.preview.api;

/**
 * Interface of a preview edge.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public interface Edge {

    /**
     * Returns whether or not the edge has a label.
     *
     * @return true if the edge has a label
     */
    public boolean hasLabel();

    /**
     * Returns the edge's thickness.
     *
     * @return the edge's thickness
     */
    public Float getThickness();

    /**
     * Returns the edge's thickness scale
     *
     * @return the edge's thickness
     */
    public Float getScale();

    /**
     * Returns the edge's color.
     *
     * @return the edge's color
     */
    public Color getColor();

    /**
     * Returns the edge's label.
     *
     * @return the edge's label
     */
    public EdgeLabel getLabel();

    /**
     * Returns the edge's node 1.
     *
     * @return the edge's node 1
     */
    public Node getNode1();

    /**
     * Returns the edge's node 2.
     *
     * @return the edge's node 2
     */
    public Node getNode2();

    /**
     * Returns an iterable on the curve list of the edge.
     *
     * @return an iterable on the curve list of the edge
     */
    public Iterable<CubicBezierCurve> getCurves();

    /**
     * Returns whether or not the edge should be displayed as a curve.
     *
     * @return true if the edge should be displayed as a curve
     */
    public Boolean isCurved();

    /**
     * Returns whether or not the edge's label should be displayed.
     *
     * @return true if the edge's label should be displayed
     */
    public Boolean showLabel();
}
