package org.gephi.preview.util;

import org.gephi.preview.api.util.HAlign;
import processing.core.PApplet;

/**
 * Class providing methods to render an horizontal alignment for different
 * supports.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public enum HAlignImpl implements HAlign {

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
