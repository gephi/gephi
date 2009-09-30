package org.gephi.preview.color;

import org.gephi.preview.color.colormode.CustomColorMode;
import org.gephi.preview.color.colormode.EdgeBothBColorMode;
import org.gephi.preview.color.colormode.ParentNodeColorMode;
import org.gephi.preview.color.colormode.EdgeB1ColorMode;
import org.gephi.preview.color.colormode.NodeOriginalColorMode;
import org.gephi.preview.color.colormode.EdgeB2ColorMode;
import org.gephi.preview.color.colormode.ParentEdgeColorMode;
import org.gephi.preview.api.color.colorizer.ColorizerFactory;
import org.gephi.preview.api.color.colorizer.ColorizerType;
import org.gephi.preview.api.color.colorizer.EdgeChildColorizer;
import org.gephi.preview.api.color.colorizer.EdgeColorizer;
import org.gephi.preview.api.color.colorizer.GenericColorizer;
import org.gephi.preview.api.color.colorizer.NodeChildColorizer;
import org.gephi.preview.api.color.colorizer.NodeColorizer;

/**
 *
 * @author jeremy
 */
public class ColorizerFactoryImpl implements ColorizerFactory {

    public GenericColorizer createGenericColorizer(ColorizerType type) {
        switch (type) {
            case CUSTOM:
                return new CustomColorMode(
                        type.getCustomColorRed(),
                        type.getCustomColorGreen(),
                        type.getCustomColorBlue());
            default:
                throw new IllegalArgumentException("Provided colorizer type not supported.");
        }
    }

    public NodeColorizer createNodeColorizer(ColorizerType type) {
        switch (type) {
            case CUSTOM:
                return new CustomColorMode(
                        type.getCustomColorRed(),
                        type.getCustomColorGreen(),
                        type.getCustomColorBlue());
            case NODE_ORIGINAL:
                return new NodeOriginalColorMode();
            default:
                throw new IllegalArgumentException("Provided colorizer type not supported.");
        }
    }

    public NodeChildColorizer createNodeChildColorizer(ColorizerType type) {
        switch (type) {
            case CUSTOM:
                return new CustomColorMode(
                        type.getCustomColorRed(),
                        type.getCustomColorGreen(),
                        type.getCustomColorBlue());
            case PARENT_NODE:
                return new ParentNodeColorMode();
            default:
                throw new IllegalArgumentException("Provided colorizer type not supported.");
        }
    }

    public EdgeColorizer createEdgeColorizer(ColorizerType type) {
        switch (type) {
            case CUSTOM:
                return new CustomColorMode(
                        type.getCustomColorRed(),
                        type.getCustomColorGreen(),
                        type.getCustomColorBlue());
            case EDGE_B1:
                return new EdgeB1ColorMode();
            case EDGE_B2:
                return new EdgeB2ColorMode();
            case EDGE_BOTH:
                return new EdgeBothBColorMode();
            default:
                throw new IllegalArgumentException("Provided colorizer type not supported.");
        }
    }

    public EdgeChildColorizer createEdgeChildColorizer(ColorizerType type) {
        switch (type) {
            case CUSTOM:
                return new CustomColorMode(
                        type.getCustomColorRed(),
                        type.getCustomColorGreen(),
                        type.getCustomColorBlue());
            case EDGE_B1:
                return new EdgeB1ColorMode();
            case EDGE_B2:
                return new EdgeB2ColorMode();
            case EDGE_BOTH:
                return new EdgeBothBColorMode();
            case PARENT_EDGE:
                return new ParentEdgeColorMode();
            default:
                throw new IllegalArgumentException("Provided colorizer type not supported.");
        }
    }
}
