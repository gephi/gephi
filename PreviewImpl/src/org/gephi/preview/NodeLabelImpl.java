package org.gephi.preview;

import java.awt.Font;
import org.gephi.preview.api.NodeLabel;
import org.gephi.preview.updaters.LabelFontAdjusterClient;
import org.gephi.preview.updaters.LabelShortenerClient;

/**
 * Implementation  of a preview node label.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public class NodeLabelImpl extends AbstractNodeChild
        implements NodeLabel, LabelShortenerClient, LabelFontAdjusterClient {

    private final String originalValue;
    private final float labelSizeFactor;
    private String value;
    private Font font;

    /**
     * Constructor.
     *
     * @param parent  the label's parent node
     * @param value   the label value
     */
    public NodeLabelImpl(NodeImpl parent, String value, float labelSize) {
        super(parent);
        this.originalValue = value;
        this.labelSizeFactor = labelSize;
    }

    public Font getFont() {
        return font;
    }

    public Font getBaseFont() {
        return parent.getBaseLabelFont();
    }

    public float getSizeFactor() {
        return labelSizeFactor;
    }

    public String getOriginalValue() {
        return originalValue;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public void revertOriginalValue() {
        setValue(originalValue);
    }
}
