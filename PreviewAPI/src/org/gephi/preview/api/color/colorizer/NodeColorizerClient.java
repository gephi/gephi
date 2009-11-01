package org.gephi.preview.api.color.colorizer;

import org.gephi.preview.api.color.Color;

/**
 *
 * @author jeremy
 */
public interface NodeColorizerClient extends ColorizerClient {

    public Color getOriginalColor();
}
