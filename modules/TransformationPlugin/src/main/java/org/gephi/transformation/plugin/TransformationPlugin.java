package org.gephi.transformation.plugin;

import java.util.function.BiConsumer;
import java.util.function.Function;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.transformation.spi.Transformation;

class TransformationPlugin implements Transformation {

    private void symetricalProcess(Graph graph, Function<Node, Float> nodeGetFunction,
                                   BiConsumer<Node, Float> nodeSetFunction) {


        graph.writeLock();
        try {
            float barycenter = 0.f;

            barycenter /= graph.getNodeCount();

            for (Node node : graph.getNodes()) {
                if (node.isFixed()) {
                    return;
                }
                float delta = ((nodeGetFunction.apply(node) - barycenter) * -1.0f);
                nodeSetFunction.accept(node, barycenter + delta);
            }
        } finally {
            graph.writeUnlock();
        }
    }

    @Override
    public void mirror_x(Graph graph) {
        this.symetricalProcess(graph, Node::y, Node::setY);
    }

    @Override
    public void mirror_y(Graph graph) {
        this.symetricalProcess(graph, Node::x, Node::setX);
    }

    private void rotation(Graph graph, float angle) {
        // Current rotation isn't centered on barycenter
        graph.readLock();
        try {
            final float sin = (float) Math.sin(-angle * Math.PI / 180);
            final float cos = (float) Math.cos(-angle * Math.PI / 180);

            float xMean = 0, yMean = 0;
            for (Node n : graph.getNodes()) {
                xMean += n.x();
                yMean += n.y();
            }
            xMean /= graph.getNodeCount();
            yMean /= graph.getNodeCount();


            for (Node n : graph.getNodes()) {
                if (!n.isFixed()) {
                    float dx = n.x() - xMean;
                    float dy = n.y() - yMean;

                    n.setX(xMean + dx * cos - dy * sin);
                    n.setY(yMean + dy * cos + dx * sin);
                }
            }
        } finally {
            graph.readUnlockAll();
        }
    }

    @Override
    public void rotate_left(Graph graph) {
        this.rotation(graph, -1f);
    }

    @Override
    public void rotate_right(Graph graph) {
        this.rotation(graph, 1f);
    }

    private void scale(Graph graph, float factor) {
        graph.readLock();
        try {
            float xMean = 0, yMean = 0;
            for (Node n : graph.getNodes()) {
                xMean += n.x();
                yMean += n.y();
            }
            xMean /= graph.getNodeCount();
            yMean /= graph.getNodeCount();

            for (Node n : graph.getNodes()) {
                if (!n.isFixed()) {
                    float dx = (n.x() - xMean) * factor;
                    float dy = (n.y() - yMean) * factor;

                    n.setX(xMean + dx);
                    n.setY(yMean + dy);
                }
            }
        } finally {
            graph.readUnlockAll();
        }
    }

    @Override
    public void extend(Graph graph) {
        this.scale(graph, 1.1f);
    }

    @Override
    public void reduce(Graph graph) {
        this.scale(graph, 0.9f);
    }
}