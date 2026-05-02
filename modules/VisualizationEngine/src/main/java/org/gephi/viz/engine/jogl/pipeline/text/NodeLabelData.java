package org.gephi.viz.engine.jogl.pipeline.text;

import org.gephi.graph.api.Node;
import org.gephi.viz.engine.util.structure.NodesCallback;

public class NodeLabelData extends AbstractLabelData<Node> {

    public NodeLabelData(NodesCallback nodesCallback) {
        super(nodesCallback);
    }

    public NodesCallback getNodesCallback() {
        return (NodesCallback) getElementsCallback();
    }
}
