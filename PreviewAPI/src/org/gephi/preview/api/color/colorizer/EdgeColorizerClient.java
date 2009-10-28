package org.gephi.preview.api.color.colorizer;

import org.gephi.preview.api.Node;

/**
 *
 * @author jeremy
 */
public interface EdgeColorizerClient extends ColorizerClient {

    public Node getNode1();

    public Node getNode2();
}
