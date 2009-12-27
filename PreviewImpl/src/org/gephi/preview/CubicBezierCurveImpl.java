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
