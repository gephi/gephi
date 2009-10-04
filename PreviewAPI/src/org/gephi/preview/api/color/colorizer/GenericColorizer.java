package org.gephi.preview.api.color.colorizer;

import org.gephi.preview.api.color.Color;

/**
 *
 * @author jeremy
 */
public interface GenericColorizer extends Colorizer {

    public void color(ColorizerClient client);

	public Color getColor();

	public java.awt.Color getAwtColor();
}
