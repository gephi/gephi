package org.gephi.preview.api.color.colorizer;

import org.gephi.preview.api.Node;

/**
 *
 * @author jeremy
 */
public interface NodeChildColorizerClient extends ColorizerClient {

    public Node getParentNode();
}
