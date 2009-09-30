package org.gephi.preview.color.colormode;

import org.gephi.preview.api.color.colorizer.ColorizerType;
import org.gephi.preview.api.color.colorizer.EdgeChildColorizer;

/**
 *
 * @author jeremy
 */
public class ParentEdgeColorMode implements EdgeChildColorizer {

    public ColorizerType getColorizerType() {
        return ColorizerType.PARENT_EDGE;
    }

//    private AbstractEdgeChild m_client;
//
//    @Override
//    public void setEdgeChild(AbstractEdgeChild client) {
//        m_client = client;
//    }
//
//    @Override
//    public void colorClient() {
//        m_client.setColor(new InheritedColor(
//                m_client.getParentEdge().getColorHolder()));
//    }
}
