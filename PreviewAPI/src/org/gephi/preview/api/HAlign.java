package org.gephi.preview.api;

import processing.core.PApplet;

/**
 *
 * @author jeremy
 */
public enum HAlign {

    LEFT,
    RIGHT;

    public String toCSS() {
        switch (this) {
            default:
            case LEFT:
                return "text-anchor: start";
            case RIGHT:
                return "text-anchor: end";
        }
    }

    public int toProcessing() {
        switch (this) {
            default:
            case LEFT:
                return PApplet.LEFT;
            case RIGHT:
                return PApplet.RIGHT;
        }
    }
}
