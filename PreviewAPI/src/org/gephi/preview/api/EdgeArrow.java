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
 * Interface of an edge arrow.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public interface EdgeArrow {

    /**
     * Returns the edge arrow's color.
     *
     * @return the edge arrow's color
     */
    public Color getColor();

    /**
     * Returns the edge arrow's first point.
     *
     * @return the edge arrow's first point
     */
    public Point getPt1();

    /**
     * Returns the edge arrow's second point.
     *
     * @return the edge arrow's second point
     */
    public Point getPt2();

    /**
     * Returns the edge arrow's third point.
     *
     * @return the edge arrow's third point
     */
    public Point getPt3();
}
