package org.gephi.preview.api;

import processing.core.PVector;

/**
 *
 * @author jeremy
 */
public interface EdgeMiniLabel {

    public Color getColor();

    public HAlign getHAlign();

    public PVector getPosition();

    public float getAngle();

    public String getValue();
}
