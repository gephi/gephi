package org.gephi.transformation.operation.mirror;

import org.gephi.graph.api.Node;
import org.gephi.transformation.spi.TransformationOperation;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = TransformationOperation.class)
public class MirrorXAxis extends Mirror {
    public MirrorXAxis() {
        super(Node::y,Node::setY);
    }
}
