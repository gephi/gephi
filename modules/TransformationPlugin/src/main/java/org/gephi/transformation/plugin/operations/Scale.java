package org.gephi.transformation.plugin.operations;

import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.transformation.spi.TransformationOperation;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = TransformationOperation.class)
public class Scale implements TransformationOperation {
    public float factor;

    public Scale(){
        this.factor =1.f;
    }
    public Scale(float factor) {
        this.factor = factor;
    }

    public float getFactor() {
        return factor;
    }

    public void setFactor(float factor) {
        this.factor = factor;
    }

    @Override
    public void transformation(Graph graph) {
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
    }
}
