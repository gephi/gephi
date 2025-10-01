package org.gephi.layout.plugin.mirror;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.layout.plugin.AbstractLayout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutProperty;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

public class MirrorLayout extends AbstractLayout {
    private record MirrorTransformation(Function<Node, Float> nodeGetFunction, BiConsumer<Node, Float> nodeSetFunction,
                                        String name) {
    }


    private Graph graph;
    private boolean xAxis;
    private final MirrorTransformation xAxisTransformation = new MirrorTransformation(Node::y, Node::setY, "X axis");
    private final MirrorTransformation yAxisTransformation = new MirrorTransformation(Node::x, Node::setX, "Y axis");

    private boolean yAxis;

    public MirrorLayout(LayoutBuilder layoutBuilder, boolean xAxis, boolean yAxis) {
        super(layoutBuilder);
        this.xAxis = xAxis;
        this.yAxis = yAxis;

    }

    @Override
    public void initAlgo() {
        setConverged(false);
    }

    @Override
    public void goAlgo() {
        graph = graphModel.getGraphVisible();
        graph.readLock();
        try {
            float xMean = 0.f, yMean = 0.f;
            for (Node n : graph.getNodes()) {
                if (yAxis) {
                    xMean += yAxisTransformation.nodeGetFunction.apply(n);
                }
                if (xAxis) {
                    yMean += xAxisTransformation.nodeGetFunction.apply(n);
                }
            }
            xMean /= graph.getNodeCount();
            yMean /= graph.getNodeCount();
            for (Node node : graph.getNodes()) {
                if (!node.isFixed()) {
                    if (yAxis) {
                        float delta = ((yAxisTransformation.nodeGetFunction.apply(node) - xMean) * -1.0f);
                        yAxisTransformation.nodeSetFunction.accept(node, xMean + delta);
                    }
                    if (xAxis) {
                        float delta = ((xAxisTransformation.nodeGetFunction.apply(node) - yMean) * -1.0f);
                        xAxisTransformation.nodeSetFunction.accept(node, yMean + delta);
                    }
                }
            }
            setConverged(true);
        } finally {
            graph.readUnlockAll();
        }
    }

    @Override
    public void endAlgo() {

    }

    @Override
    public LayoutProperty[] getProperties() {
        List<LayoutProperty> properties = new ArrayList<>();
        try {
            properties.add(LayoutProperty.createProperty(
                this, Boolean.class,
                NbBundle.getMessage(getClass(), "mirror.xaxis.name"),
                null,
                "mirror.xaxis.name",
                NbBundle.getMessage(getClass(), "mirror.xaxis.desc"),
                "isxAxis", "setxAxis"));
            properties.add(LayoutProperty.createProperty(
                this, Boolean.class,
                NbBundle.getMessage(getClass(), "mirror.yaxis.name"),
                null,
                "mirror.yaxis.name",
                NbBundle.getMessage(getClass(), "mirror.yaxis.desc"),
                "isyAxis", "setyAxis"));
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
        return properties.toArray(new LayoutProperty[0]);
    }

    @Override
    public void resetPropertiesValues() {
        this.setxAxis(false);
        this.setyAxis(true);
    }

    public Boolean isxAxis() {
        return xAxis;
    }

    public void setxAxis(Boolean xAxis) {
        this.xAxis = xAxis;
    }

    public Boolean isyAxis() {
        return yAxis;
    }

    public void setyAxis(Boolean yAxis) {
        this.yAxis = yAxis;
    }
}
