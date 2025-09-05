package org.gephi.transformation.plugin.operations;

import org.gephi.graph.api.Node;
import org.gephi.transformation.spi.TransformationOperation;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = TransformationOperation.class)
public class MirrorXAxis extends Mirror {
    public MirrorXAxis() {
        this.nodeGetFunction = Node::y;
        this.nodeSetFunction = Node::setY;
    }
}
