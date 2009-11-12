package org.gephi.preview;

import org.gephi.preview.api.NodeLabelBorder;

/**
 *
 * @author jeremy
 */
public class NodeLabelBorderImpl extends AbstractNodeChild
        implements NodeLabelBorder {

    public NodeLabelBorderImpl(NodeImpl parent) {
        super(parent);
    }

    public final NodeLabelImpl getLabel() {
        return parent.getLabel();
    }
}
