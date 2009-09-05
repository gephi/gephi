package org.gephi.preview.color;

import org.gephi.preview.api.color.ColorizerType;
import org.gephi.preview.api.color.NodeColorizer;

/**
 *
 * @author jeremy
 */
public class NodeOriginalColorMode implements NodeColorizer {

    public ColorizerType getColorizerType() {
        return ColorizerType.NODE_ORIGINAL;
    }

//    private Node m_client;
//
//    @Override
//    public void setNode(Node client) {
//        m_client = client;
//    }
//
//    @Override
//    public void colorClient() {
//        m_client.setColor(m_client.getOriginalColor());
//    }

}
