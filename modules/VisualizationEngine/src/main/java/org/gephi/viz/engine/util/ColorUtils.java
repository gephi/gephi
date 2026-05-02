package org.gephi.viz.engine.util;

public class ColorUtils {

    public static boolean isColorDark(float[] rgba) {
        // Using the luminance formula to determine if color is dark
        double luminance = rgba[0] * .2126 + rgba[1] * .7152 + rgba[2] * .0722;
        return luminance < 0.5f;
    }
}
