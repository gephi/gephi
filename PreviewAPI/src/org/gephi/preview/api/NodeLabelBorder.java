package org.gephi.preview.api;

import org.gephi.preview.api.color.Color;
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
