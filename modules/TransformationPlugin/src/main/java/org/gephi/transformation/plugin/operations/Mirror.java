package org.gephi.transformation.plugin.operations;

import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.transformation.spi.TransformationOperation;

import java.util.function.BiConsumer;
import java.util.function.Function;


abstract public class Mirror implements TransformationOperation {
    protected Function<Node, Float> nodeGetFunction;
    protected BiConsumer<Node, Float> nodeSetFunction;


    @Override
    public void transformation(Graph graph) {
        float barycenter = 0.f;
        for (Node n : graph.getNodes()) {
            barycenter = nodeGetFunction.apply(n);
        }
        barycenter /= graph.getNodeCount();
        for (Node node : graph.getNodes()) {
            if (node.isFixed()) {
                return;
            }
            float delta = ((nodeGetFunction.apply(node) - barycenter) * -1.0f);
            nodeSetFunction.accept(node, barycenter + delta);
        }
    }
}
