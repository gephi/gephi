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
package org.gephi.preview.util;

import org.gephi.preview.api.Point;
import processing.core.PVector;

/**
 * Vector implementation.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 * @see PVector
 */
public class Vector extends PVector {

    /**
     * Constructor.
     *
     * @param x  the x coordinate
     * @param y  the y coordinate
     */
    public Vector(float x, float y) {
        super(x, y);
    }

    /**
     * Constructor.
     *
     * @param vector  the source vector
     */
    public Vector(Vector vector) {
        this(vector.x, vector.y);
    }

    /**
     * Constructor.
     *
     * @param point  the source point
     */
    public Vector(Point point) {
        this(point.getX(), point.getY());
    }
}
