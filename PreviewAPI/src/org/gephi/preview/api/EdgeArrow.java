package org.gephi.preview.api;

import org.gephi.preview.api.color.Color;
import processing.core.PVector;

/**
 *
 * @author jeremy
 */
public interface EdgeArrow {

    public Color getColor();

    public PVector getPt1();

    public PVector getPt2();

    public PVector getPt3();
}
