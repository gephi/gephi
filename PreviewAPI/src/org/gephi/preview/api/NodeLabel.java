package org.gephi.preview.api;

import org.gephi.preview.api.color.Color;
import processing.core.PVector;

/**
 *
 * @author jeremy
 */
public interface NodeLabel {

    public Color getColor();

    public String getValue();

    public PVector getPosition();

}
