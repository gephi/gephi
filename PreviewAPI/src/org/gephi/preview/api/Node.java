package org.gephi.preview.api;

import org.gephi.preview.api.color.Color;
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

    public Color getBorderColor();

    public Float getBorderWidth();

    public Boolean showLabels();

    public Boolean showLabelBorders();

    public Holder<Color> getColorHolder();
}
