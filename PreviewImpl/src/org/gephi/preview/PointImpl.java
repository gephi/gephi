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
package org.gephi.preview;

import org.gephi.preview.api.Point;
import org.gephi.preview.util.Vector;

/**
 * Point implementation.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public class PointImpl implements Point {

    private final Float x;
    private final Float y;

    /**
     * Constructor.
     *
     * @param x  the x coordinate
     * @param y  the y coordinate
     */
    public PointImpl(Float x, Float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Constructor.
     *
     * @param vector  the source vector
     */
    public PointImpl(Vector vector) {
        this.x = vector.x;
        this.y = vector.y;
    }

    public Float getX() {
        return x;
    }

    public Float getY() {
        return y;
    }
}
