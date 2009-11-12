package org.gephi.preview.api.util;

import processing.core.PApplet;

/**
 * Specification of an horizontal alignment.
 *
 * @author Jérémy Subtil <jeremy.subtil@gaphi.org>
 */
public enum HAlign {

    LEFT,
    RIGHT;

    /**
     * Formats the alignment as a string for a CSS target.
     *
     * @return the alignment formatted as a string
     */
    public String toCSS() {
        switch (this) {
            default:
            case LEFT:
                return "text-anchor: start";
            case RIGHT:
                return "text-anchor: end";
        }
    }

    /**
     * Formats the alignment as a string for a Processing target.
     *
     * @return the alignment formatted as a string
     */
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
