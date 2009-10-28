package org.gephi.preview.api.color.colorizer;

import org.gephi.preview.api.Holder;
import org.gephi.preview.api.color.Color;

/**
 *
 * @author jeremy
 */
public interface EdgeChildColorizerClient extends ColorizerClient {

    public Holder<Color> getParentColorHolder();

    public EdgeColorizerClient getParentEdge();
}
