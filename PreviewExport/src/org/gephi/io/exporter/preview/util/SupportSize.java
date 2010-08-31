package org.gephi.io.exporter.preview.util;

/**
 * Implementation of the size of an export support.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public class SupportSize {

    private final Integer width;
    private final Integer height;
    private final LengthUnit lengthUnit;

    /**
     * Constructor.
     *
     * @param width       the support's width
     * @param height      the support's height
     * @param lengthUnit  the lenght unit
     */
    public SupportSize(int width, int height, LengthUnit lengthUnit) {
        this.width = width;
        this.height = height;
        this.lengthUnit = lengthUnit;
    }

    public Integer getWidthInt() {
        return width;
    }

    public Integer getHeightInt() {
        return height;
    }

    /**
     * Returns the support's width.
     *
     * @return the support's width
     */
    public String getWidth() {
        return width.toString() + lengthUnit.toString();
    }

    /**
     * Returns the support's height.
     *
     * @return the support's height
     */
    public String getHeight() {
        return height.toString() + lengthUnit.toString();
    }
}
