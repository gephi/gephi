package org.gephi.preview.api;

import org.gephi.preview.api.util.Holder;

/**
 *
 * @author jeremy
 */
public interface EdgeChildColorizerClient extends ColorizerClient {

    public Holder<Color> getParentColorHolder();

    public EdgeColorizerClient getParentEdge();
}
