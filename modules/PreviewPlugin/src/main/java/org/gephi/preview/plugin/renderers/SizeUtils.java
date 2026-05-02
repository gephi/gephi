package org.gephi.preview.plugin.renderers;

import org.gephi.preview.api.Item;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.plugin.items.NodeItem;

public class SizeUtils {

    // Same as overview
    protected static float defaultBorderWidthFactor = 0.16f;

    /**
     * Get the node size to render, taking into account the node size and the node scale factor.
     *
     * @param item       the node item
     * @param properties the preview properties
     * @return the node size to render
     */
    public static float getNodeSize(Item item, PreviewProperties properties) {
        float scale = properties.getFloatValue(PreviewProperty.NODE_SCALE_FACTOR);
        Float size = item.getData(NodeItem.SIZE);
        return size * scale;
    }

    /**
     * Get the node border width to render, taking into account the node size and the node border width factor.
     *
     * @param properties the preview properties
     * @param nodeSize   the node size to render
     * @return the node border width to render
     */
    public static float getBorderWidth(PreviewProperties properties, float nodeSize) {
        if (properties.getBooleanValue(PreviewProperty.NODE_BORDER_FIXED)) {
            return properties.getFloatValue(PreviewProperty.NODE_BORDER_WIDTH);
        } else {
            return nodeSize * defaultBorderWidthFactor / 2f;
        }
    }
}
