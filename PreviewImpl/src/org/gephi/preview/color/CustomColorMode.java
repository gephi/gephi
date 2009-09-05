package org.gephi.preview.color;

import org.gephi.preview.api.color.ColorizerType;
import org.gephi.preview.api.color.EdgeChildColorizer;
import org.gephi.preview.api.color.EdgeColorizer;
import org.gephi.preview.api.color.GenericColorizer;
import org.gephi.preview.api.color.NodeChildColorizer;
import org.gephi.preview.api.color.NodeColorizer;

/**
 *
 * @author jeremy
 */
public class CustomColorMode
        implements GenericColorizer, NodeColorizer, NodeChildColorizer, EdgeColorizer, EdgeChildColorizer {

    private final Color m_color;

//    private Colorized m_client;

    public CustomColorMode(int r, int g, int b) {
        m_color = new SimpleColor(r, g, b);
    }

    public ColorizerType getColorizerType() {
        ColorizerType c = ColorizerType.CUSTOM;
        c.setCustomColor(
                m_color.getRed(),
                m_color.getGreen(),
                m_color.getBlue());
        return c;
    }

//    @Override
//    public void setClient(Colorized client) {
//        m_client = client;
//    }
//
//    @Override
//    public void setNode(Node client) {
//        setClient(client);
//    }
//
//    @Override
//    public void setNodeChild(AbstractNodeChild client) {
//        setClient(client);
//    }
//
//    @Override
//    public void setEdge(ColorizedEdge client) {
//        setClient(client);
//    }
//
//    @Override
//    public void setEdgeChild(AbstractEdgeChild client) {
//        setClient(client);
//    }

//    @Override
//    public void colorClient() {
//        m_client.setColor(m_color);
//    }
}
