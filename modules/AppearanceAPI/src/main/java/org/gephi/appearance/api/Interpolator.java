/*
 Copyright 2008-2010 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
 Website : http://www.gephi.org

 This file is part of Gephi.

 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

 Copyright 2011 Gephi Consortium. All rights reserved.

 The contents of this file are subject to the terms of either the GNU
 General Public License Version 3 only ("GPL") or the Common
 Development and Distribution License("CDDL") (collectively, the
 "License"). You may not use this file except in compliance with the
 License. You can obtain a copy of the License at
 http://gephi.org/about/legal/license-notice/
 or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
 specific language governing permissions and limitations under the
 License.  When distributing the software, include this License Header
 Notice in each file and include the License files at
 /cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
 License Header, with the fields enclosed by brackets [] replaced by
 your own identifying information:
 "Portions Copyrighted [year] [name of copyright owner]"

 If you wish your version of this file to be governed by only the CDDL
 or only the GPL Version 3, indicate your decision by adding
 "[Contributor] elects to include this software in this distribution
 under the [CDDL or GPL Version 3] license." If you do not indicate a
 single choice of license, a recipient has the option to distribute
 your version of this file under either the CDDL, the GPL Version 3 or
 to extend the choice of license to its licensees as provided above.
 However, if you add GPL Version 3 code and therefore, elected the GPL
 Version 3 license, then the option applies only if the new code is
 made subject to such option by the copyright holder.

 Contributor(s):

 Portions Copyrighted 2011 Gephi Consortium.
 */
package org.gephi.appearance.api;

import java.awt.geom.Point2D;

/**
 * Abstract class that defines the single {@link #interpolate(float)} method.
 * This abstract class is implemented by built-in interpolators accessible as
 * static classes.
 */
public abstract class Interpolator {

    /**
     * Linear interpolation <code>x = interpolate(x)</code>
     */
    public static final Interpolator LINEAR = new Interpolator() {
        @Override
        public float interpolate(float x) {
            return x;
        }
    };
    /**
     * Log2 interpolation
     * <code>Math.log(1 + x)/Math.log(2) = interpolate(x)</code>
     */
    public static final Interpolator LOG2 = new Interpolator() {
        @Override
        public float interpolate(float x) {
            return (float) (Math.log(1 + x) / Math.log(2));
        }
    };

    /**
     * Builds a bezier interpolator with two control points (px1, py1) and (px2,
     * py2). The points should all be in range [0, 1].
     *
     * @param px1 the x-coordinate of first control point, between [0, 1]
     * @param py1 the y-coordinate of first control point, between [0, 1]
     * @param px2 the x-coordinate of second control point, between [0, 1]
     * @param py2 the y-coordinate of second control point, between [0, 1]
     * @return new bezier interpolator
     */
    public static Interpolator newBezierInterpolator(float px1, float py1, float px2, float py2) {
        return new BezierInterpolator(px1, py1, px2, py2);
    }

    /**
     * This function takes an input value between 0 and 1 and returns another
     * value, also between 0 and 1.
     *
     * @param x a value between 0 and 1
     * @return a value between 0 and 1. Values outside of this boundary may be
     * clamped to the interval [0,1] and cause undefined results.
     */
    public abstract float interpolate(float x);

    /**
     * Bezier curve interpolator.
     * <p>
     * Basically, a cubic Bezier curve is created with start point (0,0) and
     * endpoint (1,1). The other two control points (px1, py1) and (px2, py2)
     * are given by the user, where px1, py1, px1, and px2 are all in the range
     * [0,1].
     * </p>
     */
    //Author David C. Browne
    public static class BezierInterpolator extends Interpolator {

        /**
         * the coordinates of the 2 2D control points for a cubic Bezier curve,
         * with implicit start point (0,0) and end point (1,1) -- each
         * individual coordinate value must be in range [0,1]
         */
        private final float x1, y1, x2, y2;
        /**
         * do the input control points form a line with (0,0) and (1,1), i.e.,
         * x1 == y1 and x2 == y2 -- if so, then all x(t) == y(t) for the curve
         */
        private final boolean isCurveLinear;
        /**
         * power of 2 sample size for lookup table of x values
         */
        private static final int SAMPLE_SIZE = 16;
        /**
         * difference in t used to calculate each of the xSamples values --
         * power of 2 sample size should provide exact representation of this
         * value and its integer multiples (integer in range of [0..SAMPLE_SIZE]
         */
        private static final float SAMPLE_INCREMENT = 1f / SAMPLE_SIZE;
        /**
         * x values for the bezier curve, sampled at increments of 1/SAMPLE_SIZE
         * -- this is used to find the good initial guess for parameter t, given
         * an x
         */
        private final float[] xSamples = new float[SAMPLE_SIZE + 1];

        /**
         * constructor -- cubic bezier curve will be represented by control
         * points (0,0) (px1,py1) (px2,py2) (1,1) -- px1, py1, px2, py2 all in
         * range [0,1]
         *
         * @param px1 is x-coordinate of first control point, in range [0,1]
         * @param py1 is y-coordinate of first control point, in range [0,1]
         * @param px2 is x-coordinate of second control point, in range [0,1]
         * @param py2 is y-coordinate of second control point, in range [0,1]
         */
        public BezierInterpolator(float px1, float py1, float px2, float py2) {
            // check user input for precondition
            if (px1 < 0 || px1 > 1 || py1 < 0 || py1 > 1
                    || px2 < 0 || px2 > 1 || py2 < 0 || py2 > 1) {
                throw new IllegalArgumentException("control point coordinates must "
                        + "all be in range [0,1]");
            }

            // save control point data
            x1 = px1;
            y1 = py1;
            x2 = px2;
            y2 = py2;

            // calc linearity/identity curve
            isCurveLinear = ((x1 == y1) && (x2 == y2));

            // make the array of x value samples
            if (!isCurveLinear) {
                for (int i = 0; i < SAMPLE_SIZE + 1; ++i) {
                    xSamples[i] = eval(i * SAMPLE_INCREMENT, x1, x2);
                }
            }
        }

        public Point2D getControl1() {
            return new Point2D.Float(x1, y1);
        }

        public Point2D getControl2() {
            return new Point2D.Float(x2, y2);
        }

        /**
         * get the y-value of the cubic bezier curve that corresponds to the x
         * input
         *
         * @param x is x-value of cubic bezier curve, in range [0,1]
         * @return corresponding y-value of cubic bezier curve -- in range [0,1]
         */
        @Override
        public float interpolate(float x) {
            // check user input for precondition
            if (x < 0) {
                x = 0;
            } else if (x > 1) {
                x = 1;
            }

            // check quick exit identity cases (linear curve or curve endpoints)
            if (isCurveLinear || x == 0 || x == 1) {
                return x;
            }

            // find the t parameter for a given x value, and use this t to calculate
            // the corresponding y value
            return eval(findTForX(x), y1, y2);
        }

        /**
         * use Bernstein basis to evaluate 1D cubic Bezier curve (quicker and
         * more numerically stable than power basis) -- 1D control coordinates
         * are (0, p1, p2, 1), where p1 and p2 are in range [0,1], and there is
         * no ordering constraint on p1 and p2, i.e., p1 <= p2 does not have to
         * be true @param t is the pa
         *
         * ramaterized value in range [0,1] @param p1 is 1st control point
         * coordinate in range [0,1] @param p2 is 2nd control point coor
         *
         * d
         * inate in range [0,1] @return the value of the Bezier curve at
         * parameter t
         */
        private float eval(float t, float p1, float p2) {
            // Use optimzied version of the normal Bernstein basis form of Bezier:
            // (3*(1-t)*(1-t)*t*p1)+(3*(1-t)*t*t*p2)+(t*t*t), since p0=0, p3=1
            // The above unoptimized version is best using -server, but since we are
            // probably doing client-side animation, this is faster.
            float compT = 1 - t;
            return t * (3 * compT * (compT * p1 + t * p2) + (t * t));
        }

        /**
         * evaluate Bernstein basis derivative of 1D cubic Bezier curve, where
         * 1D control points are (0, p1, p2, 1), where p1 and p2 are in range
         * [0,1], and there is no ordering constraint on p1 and p2, i.e., p1 <=
         * p2 does not have to be true @param t is the paramaterized
         *
         * value in range [0,1] @param p1 is 1st control point coordinate in
         * range [0,1] @param p2 is 2nd control point coo
         *
         * r
         * dinate in range [0,1] @return the value of the Bezier curve at
         * parameter t
         */
        private float evalDerivative(float t, float p1, float p2) {
            // use optimzed version of Berstein basis Bezier derivative:
            // (3*(1-t)*(1-t)*p1)+(6*(1-t)*t*(p2-p1))+(3*t*t*(1-p2)), since p0=0, p3=1
            // The above unoptimized version is best using -server, but since we are
            // probably doing client-side animation, this is faster.
            float compT = 1 - t;
            return 3 * (compT * (compT * p1 + 2 * t * (p2 - p1)) + t * t * (1 - p2));
        }

        /**
         * find an initial good guess for what parameter t might produce the
         * x-value on the Bezier curve -- uses linear interpolation on the
         * x-value sample array that was created on construction
         *
         * @param x is x-value of cubic bezier curve, in range [0,1]
         * @return a good initial guess for parameter t (in range [0,1]) that
         * gives x
         */
        private float getInitialGuessForT(float x) {
            // find which places in the array that x would be sandwiched between,
            // and then linearly interpolate a reasonable value of t -- array values
            // are ascending (or at least never descending) -- binary search is
            // probably more trouble than it is worth here
            for (int i = 1; i < SAMPLE_SIZE + 1; ++i) {
                if (xSamples[i] >= x) {
                    float xRange = xSamples[i] - xSamples[i - 1];
                    if (xRange == 0) {
                        // no change in value between samples, so use earlier time
                        return (i - 1) * SAMPLE_INCREMENT;
                    } else {
                        // linearly interpolate the time value
                        return ((i - 1) + ((x - xSamples[i - 1]) / xRange))
                                * SAMPLE_INCREMENT;
                    }
                }
            }

            // shouldn't get here since 0 <= x <= 1, and xSamples[0] == 0 and
            // xSamples[SAMPLE_SIZE] == 1 (using power of 2 SAMPLE_SIZE for more
            // exact increment arithmetic)
            return 1;
        }

        /**
         * find the parameter t that produces the given x-value for the curve --
         * uses Newton-Raphson to refine the value as opposed to subdividing
         * until we are within some tolerance
         *
         * @param x is x-value of cubic bezier curve, in range [0,1]
         * @return the parameter t (in range [0,1]) that produces x
         */
        private float findTForX(float x) {
            // get an initial good guess for t
            float t = getInitialGuessForT(x);

            // use Newton-Raphson to refine the value for t -- for this constrained
            // Bezier with float accuracy (7 digits), any value not converged by 4
            // iterations is cycling between values, which can minutely affect the
            // accuracy of the last digit
            final int numIterations = 4;
            for (int i = 0; i < numIterations; ++i) {
                // stop if this value of t gives us exactly x
                float xT = (eval(t, x1, x2) - x);
                if (xT == 0) {
                    break;
                }

                // stop if derivative is 0
                float dXdT = evalDerivative(t, x1, x2);
                if (dXdT == 0) {
                    break;
                }

                // refine t
                t -= xT / dXdT;
            }

            return t;
        }
    }
}
