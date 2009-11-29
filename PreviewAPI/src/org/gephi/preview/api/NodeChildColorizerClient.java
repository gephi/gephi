package org.gephi.preview.api;

import org.gephi.preview.api.util.Holder;

/**
 *
 * @author jeremy
 */
public interface NodeChildColorizerClient extends ColorizerClient {

    public Holder<Color> getParentColorHolder();
}
