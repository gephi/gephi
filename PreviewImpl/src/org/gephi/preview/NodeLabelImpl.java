package org.gephi.preview;

import org.gephi.preview.api.NodeLabel;
import org.gephi.preview.supervisor.NodeLabelSupervisor;
import org.gephi.preview.util.LabelShortenerClient;

/**
 *
 * @author jeremy
 */
public class NodeLabelImpl extends AbstractNodeChild
        implements NodeLabel, LabelShortenerClient {

    private final NodeLabelSupervisor supervisor;
    private final String originalValue;
    private String value;
    
    public NodeLabelImpl(NodeImpl parent, String value) {
        super(parent);
        supervisor = parent.getLabelSupervisor();
        originalValue = value;

        supervisor.addNodeLabel(this);
    }

    @Override
    public final String getOriginalValue() {
        return originalValue;
    }

    public final String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }
}
