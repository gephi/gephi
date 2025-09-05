package org.gephi.transformation.plugin.operations;

import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.transformation.spi.TransformationOperation;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = TransformationOperation.class)
public class Rotation implements TransformationOperation {
    private float angle;

    public float getAngle() {
        return angle;
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }

    public Rotation() {
        this.angle = 0.f;
    }

    public Rotation(float angle) {
        this.angle = angle;
    }

    @Override
    public void transformation(Graph graph) {
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
    }
}
