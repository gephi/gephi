package org.gephi.preview;

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

    /**
     * Returns the node label's original value.
     *
     * @return the node label's original value
     */
    public String getOriginalValue() {
        return originalValue;
    }

    /**
     * Returns the node label's current value.
     *
     * @return the node label's current value
     */
    public String getValue() {
        return value;
    }

    /**
     * Defines the node label's current value.
     *
     * @param value  the node label's current value to set
     */
    public void setValue(String value) {
        this.value = value;
    }
}
