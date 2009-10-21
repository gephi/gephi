package org.gephi.preview.api;

import processing.core.PVector;

/**
 * Interface of a cubic Bézier curve.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public interface CubicBezierCurve {

    /**
	 * Returns the first boundary of the curve.
	 *
	 * @return the first boundary of the curve
	 */
	public PVector getPt1();

	/**
	 * Returns the first checkpoint of the curve.
	 *
	 * @return the first checkpoint of the curve
	 */
    public PVector getPt2();

	/**
	 * Returns the last checkpoint of the curve.
	 *
	 * @return the last checkpoint of the curve
	 */
    public PVector getPt3();

	/**
	 * Returns the last boundary of the curve.
	 *
	 * @return the last boundary of the curve
	 */
    public PVector getPt4();

}
