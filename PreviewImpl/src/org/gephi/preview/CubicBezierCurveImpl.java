package org.gephi.preview;

import org.gephi.preview.api.CubicBezierCurve;
import processing.core.PVector;

/**
 * Implementation of a cubic Bézier curve.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public class CubicBezierCurveImpl implements CubicBezierCurve {

    private final PVector m_pt1;
    private final PVector m_pt2;
    private final PVector m_pt3;
    private final PVector m_pt4;

	/**
	 * Constructor.
	 *
	 * @param pt1  the first boundary of the curve
	 * @param pt2  the first checkpoint of the curve
	 * @param pt3  the last checkpoint of the curve
	 * @param pt4  the last boundary of the curve
	 */
    public CubicBezierCurveImpl(PVector pt1, PVector pt2, PVector pt3, PVector pt4) {
        m_pt1 = pt1;
        m_pt2 = pt2;
        m_pt3 = pt3;
        m_pt4 = pt4;
    }

	/**
	 * Returns the first boundary of the curve.
	 *
	 * @return the first boundary of the curve
	 */
    public final PVector getPt1() {
        return m_pt1;
    }

	/**
	 * Returns the first checkpoint of the curve.
	 *
	 * @return the first checkpoint of the curve
	 */
    public final PVector getPt2() {
        return m_pt2;
    }

	/**
	 * Returns the last checkpoint of the curve.
	 *
	 * @return the last checkpoint of the curve
	 */
    public final PVector getPt3() {
        return m_pt3;
    }

	/**
	 * Returns the last boundary of the curve.
	 *
	 * @return the last boundary of the curve
	 */
    public final PVector getPt4() {
        return m_pt4;
    }
}
