package org.gephi.preview;

import org.gephi.preview.api.NodeLabelBorder;
import org.gephi.preview.supervisor.NodeLabelBorderSupervisor;

/**
 *
 * @author jeremy
 */
public class NodeLabelBorderImpl extends AbstractNodeChild
        implements NodeLabelBorder {

    private final NodeLabelBorderSupervisor supervisor;

    public NodeLabelBorderImpl(NodeImpl parent) {
        super(parent);
        supervisor = parent.getLabelBorderSupervisor();

        supervisor.addNodeLabelBorder(this);
    }

    public final NodeLabelImpl getLabel() {
        return getParentNode().getLabel();
    }
}
