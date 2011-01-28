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

import org.gephi.preview.api.CubicBezierCurve;
import org.gephi.preview.api.Point;

/**
 * Implementation of a cubic Bézier curve.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public class CubicBezierCurveImpl implements CubicBezierCurve {

    private final Point pt1;
    private final Point pt2;
    private final Point pt3;
    private final Point pt4;

    /**
     * Constructor.
     *
     * @param pt1  the first boundary of the curve
     * @param pt2  the first checkpoint of the curve
     * @param pt3  the last checkpoint of the curve
     * @param pt4  the last boundary of the curve
     */
    public CubicBezierCurveImpl(Point pt1, Point pt2, Point pt3, Point pt4) {
        this.pt1 = pt1;
        this.pt2 = pt2;
        this.pt3 = pt3;
        this.pt4 = pt4;
    }

    /**
     * Returns the first boundary of the curve.
     *
     * @return the first boundary of the curve
     */
    public Point getPt1() {
        return pt1;
    }

    /**
     * Returns the first checkpoint of the curve.
     *
     * @return the first checkpoint of the curve
     */
    public Point getPt2() {
        return pt2;
    }

    /**
     * Returns the last checkpoint of the curve.
     *
     * @return the last checkpoint of the curve
     */
    public Point getPt3() {
        return pt3;
    }

    /**
     * Returns the last boundary of the curve.
     *
     * @return the last boundary of the curve
     */
    public Point getPt4() {
        return pt4;
    }
}
