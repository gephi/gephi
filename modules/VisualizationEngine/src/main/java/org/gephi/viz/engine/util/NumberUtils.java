package org.gephi.viz.engine.util;

public class NumberUtils {

    public static boolean equalsEpsilon(float a, float b) {
        return equalsEpsilon(a, b, EPS);
    }

    public static final float EPS = 1e-5f;

    public static boolean equalsEpsilon(float a, float b, float epsilon) {
        return a == b ? true : Math.abs(a - b) < epsilon;
    }

}
