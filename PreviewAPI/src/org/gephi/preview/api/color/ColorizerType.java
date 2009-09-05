package org.gephi.preview.api.color;

/**
 *
 * @author jeremy
 */
public enum ColorizerType {

    CUSTOM,
    NODE_ORIGINAL,
    PARENT_NODE,
    EDGE_BOTH,
    EDGE_B1,
    EDGE_B2,
    PARENT_EDGE;

    private int r = 0;
    private int g = 0;
    private int b = 0;

    public void setCustomColor(int r, int g, int b) {
        if (this != CUSTOM)
            throw new IllegalStateException("Call only available for the CUSTOM value");

        this.r = r;
        this.g = g;
        this.b = b;
    }

    public void setCustomColor(java.awt.Color color) {
        if (this != CUSTOM)
            throw new IllegalStateException("Call only available for the CUSTOM value");

        r = color.getRed();
        g = color.getGreen();
        b = color.getBlue();
    }

    public int getCustomColorRed() {
        if (this != CUSTOM)
            throw new IllegalStateException("Call only available for the CUSTOM value");

        return r;
    }

    public int getCustomColorGreen() {
        if (this != CUSTOM)
            throw new IllegalStateException("Call only available for the CUSTOM value");

        return g;
    }

    public int getCustomColorBlue() {
        if (this != CUSTOM)
            throw new IllegalStateException("Call only available for the CUSTOM value");

        return b;
    }

    public java.awt.Color getCustomColor() {
        if (this != CUSTOM)
            throw new IllegalStateException("Call only available for the CUSTOM value");

        return new java.awt.Color(r, g, b);
    }
}
