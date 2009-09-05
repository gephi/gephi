package org.gephi.preview.api;

import org.gephi.preview.api.color.Color;

/**
 *
 * @author jeremy
 */
public interface SelfLoop {

    public Node getNode();

    public float getThickness();

    public Color getColor();

    public CubicBezierCurve getCurve();
}
