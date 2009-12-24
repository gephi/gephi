package org.gephi.io.exporter.preview.util;

/**
 * Enum representing a set of lenght units.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public enum LengthUnit {

    CENTIMETER,
    MILLIMETER,
    INCH,
    PERCENTAGE;

    @Override
    public String toString() {
        switch (this) {
            case CENTIMETER:
                return "cm";
            case MILLIMETER:
                return "mm";
            case INCH:
                return "in";
            default:
            case PERCENTAGE:
                return "%";
        }
    }
}
