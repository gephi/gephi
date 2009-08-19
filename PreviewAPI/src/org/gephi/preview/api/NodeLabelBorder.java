package org.gephi.preview.api;

import processing.core.PVector;

/**
 *
 * @author jeremy
 */
public interface NodeLabelBorder {

    public Color getColor();

    public PVector getPosition();

    public NodeLabel getLabel();

}
