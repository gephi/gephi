package org.gephi.transformation.plugin;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.transformation.spi.Transformation;

class TransformationPlugin implements Transformation {

    private void symetricalProcess(Graph graph,Function<Node, Float> nodeGetFunction, BiConsumer<Node, Float> nodeSetFunction)  {


        graph.writeLock();
        try {
            final float barycenter = Arrays.stream(graph.getNodes().toArray())
                .parallel()
                .map(nodeGetFunction)
                .reduce(0.f,Float::sum) / graph.getNodeCount();


            Arrays.stream(graph.getNodes().toArray()).parallel().forEach( node -> {
                if (node.isFixed()) return;
                float delta = (nodeGetFunction.apply(node) - barycenter) * -1;
                nodeSetFunction.accept(node,barycenter + delta);
            });
        } finally {
            graph.writeUnlock();
        }
    }
    @Override
    public void mirror_x(Graph graph) {
        this.symetricalProcess(graph,Node::y,Node::setY);
    }

    @Override
    public void mirror_y(Graph graph) {
        this.symetricalProcess(graph,Node::x,Node::setX);
    }

    private void rotation(Graph graph,float angle) {
        // Current rotation isn't centered on barycenter
        graph.readLock();
        try {
            final double sin = Math.sin(-angle * Math.PI / 180);
            final double cos = Math.cos(-angle * Math.PI / 180);
            double px = 0f;
            double py = 0f;

            Arrays.stream(graph.getNodes().toArray()).parallel().forEach(n -> {
                if (!n.isFixed()) {
                    double dx = n.x() - px;
                    double dy = n.y() - py;

                    n.setX((float) (px + dx * cos - dy * sin));
                    n.setY((float) (py + dy * cos + dx * sin));
                }
            });
        } finally {
            graph.readUnlockAll();
        }
    }
    @Override
    public void rotate_left(Graph graph) {
            this.rotation(graph,-1f);
    }

    @Override
    public void rotate_right(Graph graph) {
        this.rotation(graph,1f);
    }

    private void scale(Graph graph,float factor) {
        graph.readLock();
        try {
            double xMean = 0, yMean = 0;
            for (Node n : graph.getNodes()) {
                xMean += n.x();
                yMean += n.y();
            }
            xMean /= graph.getNodeCount();
            yMean /= graph.getNodeCount();

            for (Node n : graph.getNodes()) {
                if (!n.isFixed()) {
                    double dx = (n.x() - xMean) * factor;
                    double dy = (n.y() - yMean) * factor;

                    n.setX((float) (xMean + dx));
                    n.setY((float) (yMean + dy));
                }
            }
        } finally {
            graph.readUnlockAll();
        }
    }
    @Override
    public void extend(Graph graph) {
        this.scale(graph,1.1f);
    }

    @Override
    public void reduce(Graph graph) {
        this.scale(graph,0.9f);
    }
}