package org.gephi.preview.api;

import processing.core.PVector;

/**
 *
 * @author jeremy
 */
public interface Node {

    public Color getColor();

    public PVector getPosition();

    public Float getDiameter();

    public NodeLabelBorder getLabelBorder();

    public NodeLabel getLabel();
}
