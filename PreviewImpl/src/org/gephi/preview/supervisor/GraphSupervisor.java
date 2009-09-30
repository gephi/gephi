package org.gephi.preview.supervisor;

/**
 *
 * @author jeremy
 */
public class GraphSupervisor {
    
    private final NodeSupervisor nSupervisor = new NodeSupervisor();

    public NodeSupervisor getNodeSupervisor() {
        return nSupervisor;
    }

    public NodeLabelSupervisor getNodeLabelSupervisor() {
        return nSupervisor.getNodeLabelSupervisor();
    }

    public NodeLabelBorderSupervisor getNodeLabelBorderSupervisor() {
        return nSupervisor.getNodeLabelBorderSupervisor();
    }
}
