package org.gephi.preview.color;

import org.gephi.preview.api.color.ColorizerType;
import org.gephi.preview.api.color.NodeChildColorizer;

/**
 *
 * @author jeremy
 */
public class ParentNodeColorMode implements NodeChildColorizer {

    public ColorizerType getColorizerType() {
        return ColorizerType.PARENT_NODE;
    }

//    private AbstractNodeChild m_client;
//
//    @Override
//    public void setNodeChild(AbstractNodeChild client) {
//        m_client = client;
//    }
//
//    @Override
//    public void colorClient() {
//        m_client.setColor(new InheritedColor(
//                m_client.getParentNode().getColorHolder()));
//    }
}
