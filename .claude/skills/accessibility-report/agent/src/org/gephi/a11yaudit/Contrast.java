package org.gephi.a11yaudit;

import java.awt.Color;

/**
 * WCAG 2.x relative luminance and contrast ratio.
 */
final class Contrast {

    private Contrast() {
    }

    static double ratio(Color a, Color b) {
        if (a == null || b == null) {
            return Double.NaN;
        }
        return ratio(a.getRGB(), b.getRGB());
    }

    static double ratio(int rgbA, int rgbB) {
        double la = luminance(rgbA);
        double lb = luminance(rgbB);
        return (Math.max(la, lb) + 0.05) / (Math.min(la, lb) + 0.05);
    }

    static double luminance(int rgb) {
        return 0.2126 * channel((rgb >> 16) & 0xff) + 0.7152 * channel((rgb >> 8) & 0xff)
            + 0.0722 * channel(rgb & 0xff);
    }

    private static double channel(int v) {
        double c = v / 255.0;
        return c <= 0.04045 ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4);
    }
}
