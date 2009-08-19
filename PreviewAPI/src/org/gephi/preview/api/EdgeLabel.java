package org.gephi.preview.api;

import processing.core.PVector;

/**
 *
 * @author jeremy
 */
public interface EdgeLabel {

    public Color getColor();

    public PVector getPosition();

    public float getAngle();

    public String getValue();
}
