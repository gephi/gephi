package org.gephi.transformation.plugin.operations;

import org.gephi.graph.api.Node;
import org.gephi.transformation.spi.TransformationOperation;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = TransformationOperation.class)
public class MirrorYAxis extends Mirror {
    public MirrorYAxis() {
        this.nodeGetFunction =  Node::x;
        this.nodeSetFunction = Node::setX;
    }
}
