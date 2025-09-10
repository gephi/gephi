package org.gephi.transformation.operation.mirror;

import java.util.function.BiConsumer;
import java.util.function.Function;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.transformation.spi.TransformationOperation;


abstract public class Mirror implements TransformationOperation {
    final protected Function<Node, Float> nodeGetFunction;
    final protected BiConsumer<Node, Float> nodeSetFunction;

    public Mirror(Function<Node, Float> nodeGetFunction, BiConsumer<Node, Float> nodeSetFunction) {
        this.nodeGetFunction = nodeGetFunction;
        this.nodeSetFunction = nodeSetFunction;
    }

    @Override
    public void transformation(Graph graph) {
        float barycenter = 0.f;
        for (Node n : graph.getNodes()) {
            barycenter += nodeGetFunction.apply(n);
        }
        barycenter /= graph.getNodeCount();
        for (Node node : graph.getNodes()) {
            if (!node.isFixed()) {
                float delta = ((nodeGetFunction.apply(node) - barycenter) * -1.0f);
                nodeSetFunction.accept(node, barycenter + delta);
            }
        }
    }
}
