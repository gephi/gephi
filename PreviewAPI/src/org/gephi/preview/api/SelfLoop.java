package org.gephi.preview.api;

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
