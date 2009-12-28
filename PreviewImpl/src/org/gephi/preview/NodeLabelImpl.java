package org.gephi.preview;

import java.awt.Font;
import org.gephi.preview.api.NodeLabel;
import org.gephi.preview.updaters.LabelShortenerClient;

/**
 * Implementation  of a preview node label.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public class NodeLabelImpl extends AbstractNodeChild
        implements NodeLabel, LabelShortenerClient {

    private final String originalValue;
    private String value;

    /**
     * Constructor.
     *
     * @param parent  the label's parent node
     * @param value   the label value
     */
    public NodeLabelImpl(NodeImpl parent, String value) {
        super(parent);
        originalValue = value;
    }

    public Font getFont() {
        return parent.getLabelFont();
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

    public void revertOriginalValue() {
        setValue(originalValue);
    }
}
